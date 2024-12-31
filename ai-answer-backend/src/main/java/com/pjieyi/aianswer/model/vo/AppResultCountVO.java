package com.pjieyi.aianswer.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计同一个应用内用户答题结果中每个评分结果对应的数量
 */
@Data
public class AppResultCountVO implements Serializable {

    //结果名称
    private String resultName;

    //结果数量
    private Integer resultCount;

    private static final long serialVersionUID = 1L;
}
