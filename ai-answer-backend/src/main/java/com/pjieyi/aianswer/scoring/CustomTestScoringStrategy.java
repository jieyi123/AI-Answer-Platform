package com.pjieyi.aianswer.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pjieyi.aianswer.model.dto.question.QuestionContentDTO;
import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.Question;
import com.pjieyi.aianswer.model.entity.ScoringResult;
import com.pjieyi.aianswer.model.entity.UserAnswer;
import com.pjieyi.aianswer.model.vo.QuestionVO;
import com.pjieyi.aianswer.service.QuestionService;
import com.pjieyi.aianswer.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义测评类应用评分策略
 */
@ScoringStrategyConfig(appType = 1, scoringStrategy = 0)
public class CustomTestScoringStrategy implements ScoringStrategy {

    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    /**
     * @param choices 答案列表
     * @param app     答题应用
     * @return 答题记录
     */
    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        // 根据app查询题目和题目结果信息
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
        );
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        // 初始化一个map，用来存储每个选项的计数  I-5 M-3
        Map<String, Integer> optionCount = new HashMap<>();

        // 遍历题目列表
        // for (QuestionContentDTO questionContentDTO : questionContent) {
        //     // 遍历答案列表
        //     for (String choice : choices) {
        //         // 遍历题目中的选项
        //         for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
        //             // 如果result属性不在optionCount中，初始化为0
        //             if (option.getKey().equals(choice)) {
        //                 String result = option.getResult();
        //                 if (!optionCount.containsKey(result)) {
        //                     optionCount.put(result, 1);
        //                 } else {
        //                     // 在optionCount中增加计数
        //                     optionCount.put(result, optionCount.get(result) + 1);
        //                 }
        //             }
        //         }
        //     }
        // }

        for (int i = 0; i < choices.size(); i++) {
            QuestionContentDTO questionContentDTO = questionContent.get(i);
            for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                if (option.getKey().equals(choices.get(i))) {
                    String result = option.getResult();
                    if (!optionCount.containsKey(result)) {
                        optionCount.put(result, 1);
                    } else {
                        // 在optionCount中增加计数
                        optionCount.put(result, optionCount.get(result) + 1);
                    }
                }
            }
        }

        // 初始化最高分数与对应的评分结果记录
        ScoringResult scoringResult = scoringResultList.get(0);
        int maxScore = 0;
        // 遍历评分结果
        for (ScoringResult result : scoringResultList) {
            List<String> resultProp = JSONUtil.toList(result.getResultProp(), String.class);
            int score = resultProp.stream().mapToInt(prop -> optionCount.getOrDefault(prop, 0)).sum();
            // 如果分数高于当前最高分数，更新最高分数和最高分数对应的评分结果
            if (maxScore < score) {
                maxScore = score;
                scoringResult = result;
            }
        }

        // 构造返回值
        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setResultId(scoringResult.getId());
        userAnswer.setResultName(scoringResult.getResultName());
        userAnswer.setResultDesc(scoringResult.getResultDesc());
        userAnswer.setResultPicture(scoringResult.getResultPicture());
        return userAnswer;
    }
}
