package com.pjieyi.aianswer.scoring;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pjieyi.aianswer.model.dto.question.QuestionContentDTO;
import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.Question;
import com.pjieyi.aianswer.model.entity.ScoringResult;
import com.pjieyi.aianswer.model.entity.UserAnswer;
import com.pjieyi.aianswer.model.vo.QuestionVO;
import com.pjieyi.aianswer.service.QuestionService;
import com.pjieyi.aianswer.service.ScoringResultService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 自定义打分类应用评分策略
 */
@ScoringStrategyConfig(appType = 0, scoringStrategy = 0)
public class CustomScoreScoringStrategy implements ScoringStrategy{


    @Resource
    private QuestionService questionService;

    @Resource
    private ScoringResultService scoringResultService;

    /**
     *
     * @param choices 答案列表
     * @param app 答题应用
     * @return  答题记录
     */
    @Override
    public UserAnswer doScore(List<String> choices, App app) {
        //根据app查询题目和题目结果信息
        Long appId = app.getId();
        Question question = questionService.getOne(
                Wrappers.lambdaQuery(Question.class).eq(Question::getAppId, appId)
        );
        List<ScoringResult> scoringResultList = scoringResultService.list(
                Wrappers.lambdaQuery(ScoringResult.class).eq(ScoringResult::getAppId, appId)
        );
        //统计用户得分
        int totalScore=0;
        QuestionVO questionVO = QuestionVO.objToVo(question);
        List<QuestionContentDTO> questionContent = questionVO.getQuestionContent();
        //遍历题目列表
        // for (QuestionContentDTO questionContentDTO : questionContent) {
        //     //遍历答案列表
        //     for (String choice : choices){
        //         //遍历题目中的答案
        //         for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
        //             if (option.getKey().equals(choice)){
        //                 Integer score = Optional.of(option.getScore()).orElse(0);
        //                 totalScore+=score;
        //             }
        //         }
        //     }
        // }
        for (int i = 0; i < choices.size(); i++) {
            QuestionContentDTO questionContentDTO = questionContent.get(i);
            //遍历题目中的答案
            for (QuestionContentDTO.Option option : questionContentDTO.getOptions()) {
                if (option.getKey().equals(choices.get(i))){
                    Integer score = Optional.of(option.getScore()).orElse(0);
                    totalScore+=score;
                }
            }
        }
        //遍历得分结果，找到第一个用户分数大于得分范围的结果，作为最终结果
        ScoringResult scoringResult = scoringResultList.get(0);
        for (ScoringResult result : scoringResultList) {
            if (totalScore>=result.getResultScoreRange()){
                scoringResult=result;
                break;
            }
        }
        //构造返回值
        UserAnswer userAnswer=new UserAnswer();
        userAnswer.setAppId(appId);
        userAnswer.setAppType(app.getAppType());
        userAnswer.setChoices(JSONUtil.toJsonStr(choices));
        userAnswer.setScoringStrategy(app.getScoringStrategy());
        userAnswer.setResultId(scoringResult.getId());
        userAnswer.setResultName(scoringResult.getResultName());
        userAnswer.setResultDesc(scoringResult.getResultDesc());
        userAnswer.setResultPicture(scoringResult.getResultPicture());
        userAnswer.setResultScore(totalScore);
        return userAnswer;
    }
}
