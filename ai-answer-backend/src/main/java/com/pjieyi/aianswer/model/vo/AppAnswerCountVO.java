package com.pjieyi.aianswer.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计每个应用回答数量
 */
@Data
public class AppAnswerCountVO implements Serializable {

    //应用id
    private Long appId;

    //应用数量
    private Integer answerCount;

    private static final long serialVersionUID = 1L;
}
