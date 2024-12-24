package com.pjieyi.aianswer.model.dto.question;

import com.pjieyi.aianswer.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询题目请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;


    /**
     * 题目内容(json格式)
     */
    private List<QuestionContentDTO> questionContent;


    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 应用Id
     */
    private Long appId;


    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}