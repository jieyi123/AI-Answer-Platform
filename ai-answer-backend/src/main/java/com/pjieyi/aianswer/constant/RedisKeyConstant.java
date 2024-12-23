package com.pjieyi.aianswer.constant;

/**
 * Redis key 常量类
 */
public interface RedisKeyConstant {

    //redis session 前缀
    String SPRING_SESSION_PREFIX = "spring:session:sessions:";

    // session 中保存的属性的前缀
    String SESSION_ATTR_PREFIX="sessionAttr:";
}
