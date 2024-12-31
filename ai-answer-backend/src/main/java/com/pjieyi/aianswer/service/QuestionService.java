package com.pjieyi.aianswer.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pjieyi.aianswer.model.dto.question.AiGenerateQuestionRequest;
import com.pjieyi.aianswer.model.dto.question.QuestionContentDTO;
import com.pjieyi.aianswer.model.dto.question.QuestionQueryRequest;
import com.pjieyi.aianswer.model.entity.Question;
import com.pjieyi.aianswer.model.vo.QuestionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目服务
 *
 */
public interface QuestionService extends IService<Question> {

    /**
     * 校验数据
     *
     * @param question
     * @param add 对创建的数据进行校验
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest
     * @return
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    
    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);

    /**
     * AI生成题目
     *
     * @param questionRequest
     * @return
     */
    List<QuestionContentDTO> aiGenerateQuestion(AiGenerateQuestionRequest questionRequest);

    /**
     * AI生成题目 (流式调用)
     * @param questionRequest
     * @return
     */
    SseEmitter aiGenerateQuestionSse(AiGenerateQuestionRequest questionRequest);

    /**
     * 测试线程池隔离
     * @param questionRequest
     * @param isVip
     * @return
     */
    SseEmitter aiGenerateQuestionSseTest(AiGenerateQuestionRequest questionRequest,Boolean isVip);
}
