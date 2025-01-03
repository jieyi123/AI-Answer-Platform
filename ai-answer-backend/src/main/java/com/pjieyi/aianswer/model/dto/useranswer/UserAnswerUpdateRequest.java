package com.pjieyi.aianswer.model.dto.useranswer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 更新用户答题记录请求
 *
 */
@Data
public class UserAnswerUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;



    /**
     * 用户答案（JSON 数组）
     */
    private List<String> choices;



    private static final long serialVersionUID = 1L;
}