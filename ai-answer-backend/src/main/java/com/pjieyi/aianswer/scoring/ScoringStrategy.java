package com.pjieyi.aianswer.scoring;

import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.UserAnswer;

import java.util.List;

/**
 * 评分策略
 */
public interface ScoringStrategy {

    /**
     * 执行评分
     * @param choices
     * @param app
     * @return
     */
    UserAnswer doScore(List<String> choices, App app);
}
