package com.pjieyi.aianswer.model.dto.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目选项
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class QuestionContentDTO {

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目选项列表
     */
    private List<Option> options;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Option {
        private String result;
        private int score;
        private String value;
        private String key;
    }
}
