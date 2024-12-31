package com.pjieyi.aianswer.mapper;

import com.pjieyi.aianswer.model.entity.UserAnswer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pjieyi.aianswer.model.vo.AppAnswerCountVO;
import com.pjieyi.aianswer.model.vo.AppResultCountVO;

import java.util.List;

/**
* @author pengjy
* @description 针对表【user_answer(用户答题记录)】的数据库操作Mapper
* @createDate 2024-12-23 14:31:48
* @Entity com.pjieyi.aianswer.model.entity.UserAnswer
*/
public interface UserAnswerMapper extends BaseMapper<UserAnswer> {

    //热门应用统计
    List<AppAnswerCountVO> doAppAnswerCount();

    //根据应用 id，统计同一个应用内用户答题结果中每个评分结果对应的数量
    List<AppResultCountVO> doAppAnswerResultCount(Long appId);



}




