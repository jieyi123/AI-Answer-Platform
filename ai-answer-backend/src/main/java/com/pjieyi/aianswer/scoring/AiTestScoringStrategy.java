package com.pjieyi.aianswer.scoring;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.pjieyi.aianswer.common.ErrorCode;
import com.pjieyi.aianswer.exception.BusinessException;
import com.pjieyi.aianswer.manager.AIManager;
import com.pjieyi.aianswer.model.dto.question.QuestionAnswerDTO;
import com.pjieyi.aianswer.model.dto.question.QuestionContentDTO;
import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.Question;
import com.pjieyi.aianswer.model.entity.UserAnswer;
import com.pjieyi.aianswer.model.vo.QuestionVO;
import com.pjieyi.aianswer.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 1)
public class AiTestScoringStrategy implements ScoringStrategy {


    @Resource
    private QuestionService questionService;

    @Resource
    private AIManager aiManager;

    private final Cache<String, String> aiAnswerCache =
            Caffeine.newBuilder().initialCapacity(1024) // 缓存容量
                    .expireAfterAccess(5L, TimeUnit.MINUTES) // 过期时间5分钟
                    .build();

    @Resource
    private RedissonClient redissonClient;

    private static final String AI_ANSWER_LOCK = "AI_ANSWER_LOCK";

    /**
     * AI 评分系统消息
     */
    private static final String AI_TEST_SCORING_SYSTEM_MESSAGE = "你是一位严谨的判题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "题目和用户回答的列表：格式为 [{\"title\": \"题目\",\"answer\": \"用户回答\"}]\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来对用户进行评价：\n" +
            "1. 要求：需要给出一个明确的评价结果，包括评价名称（尽量简短）和评价描述（尽量详细，大于 200 字）\n" +
            "2. 严格按照下面的 json 格式输出评价名称和评价描述\n" +
            "```\n" +
            "{\"resultName\": \"评价名称\", \"resultDesc\": \"评价描述\"}\n" +
            "```\n" +
            "3. 返回格式必须为 JSON 对象\n";

    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        Long appId = app.getId();

        String choicesStr = JSONUtil.toJsonStr(choices);
        String cacheKey = this.buildCacheKey(appId, choicesStr);
        String aiAnswerCacheResult = aiAnswerCache.getIfPresent(cacheKey);
        // 定义锁
        RLock lock = redissonClient.getLock(AI_ANSWER_LOCK + ":" + cacheKey);
        // 尝试获取锁
        try {
            // 5秒内尝试获取锁，如果成功获取倒锁后，10秒后锁释放
            boolean tryLock = lock.tryLock(5, 15, TimeUnit.SECONDS);
            if (!tryLock) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取AI答案失败，请稍后再试");
            }
            // 抢到锁了，执行后续业务逻辑
            Question question = questionService.getOne(
                    Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
            );
            QuestionVO questionVO = QuestionVO.objToVo(question);
            // 没有缓存
            if (StringUtils.isAnyBlank(aiAnswerCacheResult)) {
                List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
                String userMessage = this.getAiTestScoringUserMessage(app, questionContent, choices);
                String result = aiManager.doRequest(AI_TEST_SCORING_SYSTEM_MESSAGE, userMessage, false, null);
                // 截取需要的 JSON 信息
                int start = result.indexOf("{");
                int end = result.lastIndexOf("}");
                aiAnswerCacheResult = result.substring(start, end + 1);
                aiAnswerCache.put(cacheKey, aiAnswerCacheResult);
            }
            UserAnswer userAnswer = JSONUtil.toBean(aiAnswerCacheResult, UserAnswer.class);
            userAnswer.setAppId(app.getId());
            userAnswer.setAppType(app.getAppType());
            userAnswer.setScoringStrategy(app.getScoringStrategy());
            userAnswer.setChoices(choicesStr);
            return userAnswer;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (lock != null && lock.isLocked()) {
                //释放掉自己的锁
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }

    }

    /**
     * AI 评分用户消息封装
     *
     * @param app
     * @param questionContentDTO
     * @param choices
     * @return
     */
    private String getAiTestScoringUserMessage(App app, List<QuestionContentDTO> questionContentDTO, List<String> choices) {
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        List<QuestionAnswerDTO> questionAnswerDTOList = new ArrayList<>();
        for (int i = 0; i < choices.size(); i++) {
            QuestionAnswerDTO questionAnswerDTO = new QuestionAnswerDTO();
            questionAnswerDTO.setTitle(questionContentDTO.get(i).getTitle());
            List<QuestionContentDTO.Option> options = questionContentDTO.get(i).getOptions();
            for (QuestionContentDTO.Option option : options) {
                if (option.getKey().equals(choices.get(i))) {
                    questionAnswerDTO.setUserAnswer(option.getValue());
                    break;
                }
            }
            questionAnswerDTOList.add(questionAnswerDTO);
        }
        String jsonStr = JSONUtil.toJsonStr(questionAnswerDTOList);
        userMessage.append(jsonStr).append("\n");
        return userMessage.toString();
    }

    /**
     * 构建缓存key
     *
     * @param appId      应用ID
     * @param choicesStr 答案列表  ["A","B","C"]
     * @return 返回一个 32 位 长度的十六进制字符串
     */
    private String buildCacheKey(Long appId, String choicesStr) {
        return DigestUtil.md5Hex(appId + ":" + choicesStr);
    }


}
