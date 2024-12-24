package com.pjieyi.aianswer.controller;

import com.pjieyi.aianswer.model.dto.question.QuestionContentDTO;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pjieyi.aianswer.annotation.AuthCheck;
import com.pjieyi.aianswer.common.BaseResponse;
import com.pjieyi.aianswer.common.DeleteRequest;
import com.pjieyi.aianswer.common.ErrorCode;
import com.pjieyi.aianswer.common.ResultUtils;
import com.pjieyi.aianswer.constant.UserConstant;
import com.pjieyi.aianswer.exception.BusinessException;
import com.pjieyi.aianswer.exception.ThrowUtils;
import com.pjieyi.aianswer.model.dto.question.*;
import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.Question;
import com.pjieyi.aianswer.model.entity.User;
import com.pjieyi.aianswer.model.vo.QuestionVO;
import com.pjieyi.aianswer.service.AppService;
import com.pjieyi.aianswer.service.QuestionService;
import com.pjieyi.aianswer.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    // region 增删改查

    /**
     * 创建题目
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        // 数据校验
        ThrowUtils.throwIf(questionAddRequest == null, ErrorCode.PARAMS_ERROR);
        List<QuestionContentDTO> questionContent = questionAddRequest.getQuestionContent();
        Long appId = questionAddRequest.getAppId();
        ThrowUtils.throwIf(CollUtil.isEmpty(questionContent), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        // 判断应用id是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        Question question = new Question();
        question.setAppId(appId);
        question.setQuestionContent(JSONUtil.toJsonStr(questionContent));
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newQuestionId = question.getId();
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新题目（仅管理员可用）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        if (questionUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = questionUpdateRequest.getId();
        List<QuestionContentDTO> questionContent = questionUpdateRequest.getQuestionContent();
        Long appId = questionUpdateRequest.getAppId();
        //校验参数
        ThrowUtils.throwIf(CollUtil.isEmpty(questionContent), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR);
        Question question = new Question();
        // 判断当前题目是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        // 判断应用id是否存在
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR,"应用不存在");
        // 操作数据库
        question.setId(id);
        question.setAppId(appId);
        question.setQuestionContent(JSONUtil.toJsonStr(questionContent));
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取题目（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Question question = questionService.getById(id);
        ThrowUtils.throwIf(question == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取题目列表（仅管理员可用）
     *
     * @param questionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的题目列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(questionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 获取封装类
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 编辑题目（给用户使用）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() ==null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<QuestionContentDTO> questionContentDTO = questionEditRequest.getQuestionContent();
        question.setQuestionContent(JSONUtil.toJsonStr(questionContentDTO));
        // 数据校验
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR,"当前题目不存在");
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = questionService.updateById(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
