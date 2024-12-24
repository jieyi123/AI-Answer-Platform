package com.pjieyi.aianswer.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pjieyi.aianswer.common.ErrorCode;
import com.pjieyi.aianswer.common.ReviewRequest;
import com.pjieyi.aianswer.constant.CommonConstant;
import com.pjieyi.aianswer.exception.BusinessException;
import com.pjieyi.aianswer.exception.ThrowUtils;
import com.pjieyi.aianswer.mapper.AppMapper;
import com.pjieyi.aianswer.model.dto.app.AppQueryRequest;
import com.pjieyi.aianswer.model.entity.App;
import com.pjieyi.aianswer.model.entity.User;
import com.pjieyi.aianswer.model.enums.AppTypeEnum;
import com.pjieyi.aianswer.model.enums.ReviewStatusEnum;
import com.pjieyi.aianswer.model.enums.ScoringStrategyEnum;
import com.pjieyi.aianswer.model.vo.AppVO;
import com.pjieyi.aianswer.model.vo.UserVO;
import com.pjieyi.aianswer.service.AppService;
import com.pjieyi.aianswer.service.UserService;
import com.pjieyi.aianswer.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用服务实现
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param app
     * @param add 对创建的数据进行校验
     */
    @Override
    public void validApp(App app, boolean add) {
        String appName = app.getAppName();
        String appDesc = app.getAppDesc();
        String appIcon = app.getAppIcon();
        Integer appType = app.getAppType();
        Integer scoringStrategy = app.getScoringStrategy();
        Integer reviewStatus = app.getReviewStatus();
        // 创建数据时，参数不能为空

        ThrowUtils.throwIf(StringUtils.isBlank(appName), ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(appDesc), ErrorCode.PARAMS_ERROR, "应用描述不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(appIcon), ErrorCode.PARAMS_ERROR, "应用图片不能为空");
        ThrowUtils.throwIf(appType == null, ErrorCode.PARAMS_ERROR, "应用类型不能为空");
        ThrowUtils.throwIf(scoringStrategy == null, ErrorCode.PARAMS_ERROR, "评分类型不能为空");

        // 修改数据时，有参数则校验
        if (StringUtils.isNotBlank(appName)) {
            ThrowUtils.throwIf(appName.length() > 80, ErrorCode.PARAMS_ERROR, "应用名称过长");
        }
        AppTypeEnum enumByValue = AppTypeEnum.getEnumByValue(appType);
        ThrowUtils.throwIf(enumByValue == null, ErrorCode.PARAMS_ERROR, "应用类型不存在");
        ScoringStrategyEnum scoringStrategyEnum = ScoringStrategyEnum.getEnumByValue(scoringStrategy);
        ThrowUtils.throwIf(scoringStrategyEnum == null, ErrorCode.PARAMS_ERROR, "评分类型不存在");
        if (reviewStatus!=null){
            ReviewStatusEnum review = ReviewStatusEnum.getEnumByValue(reviewStatus);
            ThrowUtils.throwIf(review==null,ErrorCode.PARAMS_ERROR,"审核状态非法");
        }
    }

    /**
     * 获取查询条件
     *
     * @param appQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<App> getQueryWrapper(AppQueryRequest appQueryRequest) {
        QueryWrapper<App> queryWrapper = new QueryWrapper<>();
        if (appQueryRequest==null){
            return queryWrapper;
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String appDesc = appQueryRequest.getAppDesc();
        Integer appType = appQueryRequest.getAppType();
        String searchText = appQueryRequest.getSearchText();
        Integer scoringStrategy = appQueryRequest.getScoringStrategy();
        Integer reviewStatus = appQueryRequest.getReviewStatus();
        String reviewMessage = appQueryRequest.getReviewMessage();
        Long reviewerId = appQueryRequest.getReviewerId();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("appName", searchText).or().like("appDesc", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(appName), "appName", appName);
        queryWrapper.like(StringUtils.isNotBlank(appDesc), "appDesc", appDesc);
        queryWrapper.like(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(scoringStrategy), "scoringStrategy", scoringStrategy);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appType), "appType", appType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取应用封装
     *
     * @param app
     * @param request
     * @return
     */
    @Override
    public AppVO getAppVO(App app, HttpServletRequest request) {
        // 对象转封装类
        AppVO appVO = AppVO.objToVo(app);
        // region 可选
        // 1. 关联查询用户信息
        Long userId = app.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appVO.setUser(userVO);
        return appVO;
    }

    /**
     * 分页获取应用封装
     *
     * @param appPage
     * @param request
     * @return
     */
    @Override
    public Page<AppVO> getAppVOPage(Page<App> appPage, HttpServletRequest request) {
        List<App> appList = appPage.getRecords();
        Page<AppVO> appVOPage = new Page<>(appPage.getCurrent(), appPage.getSize(), appPage.getTotal());
        if (CollUtil.isEmpty(appList)) {
            return appVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppVO> appVOList = appList.stream().map(AppVO::objToVo).collect(Collectors.toList());

        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 填充信息
        appVOList.forEach(appVO -> {
            Long userId = appVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appVO.setUser(userService.getUserVO(user));
        });
        // endregion
        appVOPage.setRecords(appVOList);
        return appVOPage;
    }

    /**
     * 应用审核
     *
     * @param reviewRequest
     * @param request
     * @return
     */
    @Override
    public Boolean reviewApp(ReviewRequest reviewRequest, HttpServletRequest request) {
        Long id = reviewRequest.getId();
        Integer reviewStatus = reviewRequest.getReviewStatus();
        String reviewMessage = reviewRequest.getReviewMessage();
        //参数校验
        if (id==null || reviewStatus==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(StringUtils.isBlank(reviewMessage), ErrorCode.PARAMS_ERROR);

        ThrowUtils.throwIf(ReviewStatusEnum.getEnumByValue(reviewStatus)==null,ErrorCode.PARAMS_ERROR,"审核状态不存在");
        App oldApp = this.getById(id);
        if (oldApp==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"应用不存在");
        }
        App app=new App();
        app.setId(oldApp.getId());
        app.setReviewStatus(reviewStatus);
        app.setReviewTime(new Date());
        app.setReviewerId(userService.getLoginUser(request).getId());
        app.setReviewMessage(reviewMessage);
        return this.updateById(app);
    }

}
