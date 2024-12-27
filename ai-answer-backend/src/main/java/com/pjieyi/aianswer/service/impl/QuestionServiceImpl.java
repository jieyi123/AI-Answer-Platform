package com.pjieyi.aianswer.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pjieyi.aianswer.common.ErrorCode;
import com.pjieyi.aianswer.constant.CommonConstant;
import com.pjieyi.aianswer.exception.ThrowUtils;
import com.pjieyi.aianswer.manager.AIManager;
import com.pjieyi.aianswer.mapper.QuestionMapper;
import com.pjieyi.aianswer.model.dto.question.AiGenerateQuestionRequest;
import com.pjieyi.aianswer.model.dto.question.QuestionContentDTO;
import com.pjieyi.aianswer.model.dto.question.QuestionQueryRequest;
import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.Question;
import com.pjieyi.aianswer.model.entity.User;
import com.pjieyi.aianswer.model.vo.QuestionVO;
import com.pjieyi.aianswer.model.vo.UserVO;
import com.pjieyi.aianswer.service.AppService;
import com.pjieyi.aianswer.service.QuestionService;
import com.pjieyi.aianswer.service.UserService;
import com.pjieyi.aianswer.utils.SqlUtils;
import com.zhipu.oapi.service.v4.model.ModelData;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;


    @Resource
    private AIManager aiManager;

    // region AI 生成题目功能
    private static final String GENERATE_QUESTION_SYSTEM_MESSAGE = "你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组";

    /**
     * 校验数据
     *
     * @param question
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        ThrowUtils.throwIf(question == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = question.getQuestionContent();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        Long id = questionQueryRequest.getId();
        List<QuestionContentDTO> questionContent = questionQueryRequest.getQuestionContent();
        String searchText = questionQueryRequest.getSearchText();
        Long appId = questionQueryRequest.getAppId();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();

        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("questionContent", searchText));
        }
        // 模糊查询
        if (CollUtil.isNotEmpty(questionContent)) {
            for (QuestionContentDTO questionContent1 : questionContent) {
                queryWrapper.like("questionContent", questionContent1.getTitle());
            }
        }

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appId), "appId", appId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        // 对象转封装类
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 1. 关联查询用户信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        questionVO.setUser(userVO);
        // endregion

        return questionVO;
    }

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        if (CollUtil.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<QuestionVO> questionVOList = questionList.stream().map(QuestionVO::objToVo).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        questionVOList.forEach(questionVO -> {
            Long userId = questionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUser(userService.getUserVO(user));
        });
        // endregion
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    /**
     * AI生成题目
     *
     * @param questionRequest
     * @return
     */
    @Override
    public List<QuestionContentDTO> aiGenerateQuestion(AiGenerateQuestionRequest questionRequest) {
        Long appId = questionRequest.getAppId();
        Integer questionNum = questionRequest.getQuestionNum();
        Integer optionNum = questionRequest.getOptionNum();
        ThrowUtils.throwIf(appId==null,ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        String userMessage=getGenerateQuestionUserMessage(app,questionNum,optionNum);
        String result = aiManager.doRequest(GENERATE_QUESTION_SYSTEM_MESSAGE,userMessage,null);
        // 截取需要的 JSON 信息
        int start = result.indexOf("[");
        int end = result.lastIndexOf("]");
        String json = result.substring(start, end + 1);
        return JSONUtil.toList(json, QuestionContentDTO.class);
    }

    @Override
    public SseEmitter aiGenerateQuestionSse(AiGenerateQuestionRequest questionRequest) {

        Long appId = questionRequest.getAppId();
        Integer questionNum = questionRequest.getQuestionNum();
        Integer optionNum = questionRequest.getOptionNum();
        ThrowUtils.throwIf(appId==null,ErrorCode.PARAMS_ERROR);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app==null,ErrorCode.NOT_FOUND_ERROR);
        String userMessage=getGenerateQuestionUserMessage(app,questionNum,optionNum);
        //原子类，保证线程安全
        AtomicInteger flag=new AtomicInteger(0);
        //0表示永不超时
        SseEmitter emitter=new SseEmitter(0L);
        StringBuilder contentBuilder=new StringBuilder();
        Flowable<ModelData> flowable = aiManager.doRequestStream(GENERATE_QUESTION_SYSTEM_MESSAGE, userMessage, null);
        flowable.observeOn(Schedulers.io())
                .map(chunk->chunk.getChoices().get(0).getDelta().getContent())
                .flatMap(message->{
                    List<Character> characters=new ArrayList<>();
                    for (char c : message.toCharArray()){
                        characters.add(c);
                    }
                    return Flowable.fromIterable(characters);
                })
                .doOnNext(c->{
                    //识别第一个{ 表示AI开始传输数据
                    if (c=='{'){
                        flag.addAndGet(1);
                    }
                    if (flag.get()>0){
                        contentBuilder.append(c);
                    }
                    if (c=='}'){
                        flag.addAndGet(-1);
                        if (flag.get()==0){
                            //累积单套题目满足 json 格式后，sse 推送至前端
                            //sse 需要压缩成当行 json，sse 无法识别换行
                            emitter.send(JSONUtil.toJsonStr(contentBuilder.toString()));
                            //清空当前题目，重新拼接下一道题目
                            contentBuilder.setLength(0);
                        }
                    }
                })
                .doOnComplete(emitter::complete).subscribe();;
        return emitter;
    }

    private String getGenerateQuestionUserMessage(App app,int questionNum,int optionNum){
        StringBuilder userMessage = new StringBuilder();
        userMessage.append(app.getAppName()).append("\n");
        userMessage.append(app.getAppDesc()).append("\n");
        userMessage.append(questionNum).append("\n");
        userMessage.append(optionNum).append("\n");
        return userMessage.toString();
    }


}
