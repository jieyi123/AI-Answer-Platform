package com.pjieyi.aianswer.controller;

import com.pjieyi.aianswer.common.BaseResponse;
import com.pjieyi.aianswer.common.ErrorCode;
import com.pjieyi.aianswer.common.ResultUtils;
import com.pjieyi.aianswer.exception.BusinessException;
import com.pjieyi.aianswer.mapper.UserAnswerMapper;
import com.pjieyi.aianswer.model.vo.AppAnswerCountVO;
import com.pjieyi.aianswer.model.vo.AppResultCountVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统计分析接口
 */
@RestController
@RequestMapping("/app/statistic")
@Slf4j
public class AppStatisticController {

    @Resource
    private UserAnswerMapper userAnswerMapper;


    //热门应用及回答数统计（top 10）
    @GetMapping("/answer_count")
    public BaseResponse<List<AppAnswerCountVO>> getAppAnswerCount(){
        return ResultUtils.success(userAnswerMapper.doAppAnswerCount());
    }

    //某应用回答结果分布统计
    @GetMapping("/answer_reslut_count")
    public BaseResponse<List<AppResultCountVO>> getAppAnswerResultCount(Long appId){
        if (appId==null || appId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(userAnswerMapper.doAppAnswerResultCount(appId));
    }


}
