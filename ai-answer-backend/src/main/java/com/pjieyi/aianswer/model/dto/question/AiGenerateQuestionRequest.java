package com.pjieyi.aianswer.model.dto.question;

import lombok.Data;

import java.io.Serializable;

/**
 * AI题目生成请求
 */
@Data
public class AiGenerateQuestionRequest implements Serializable {

    //应用id
    private Long appId;

    //题目数
    Integer questionNum=4;

    //选项数
    Integer optionNum=2;

}
