package com.shiyu.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志注解
 * 用于标注在Controller方法上，自动记录操作日志
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /** 操作类型：CREATE, UPDATE, DELETE, QUERY, LOGIN, LOGOUT, EXPORT, IMPORT, SYNC等 */
    String action() default "";

    /** 操作目标实体名称，如：菜谱, 订单, 用户, 公告等 */
    String target() default "";

    /** 操作描述模板，支持SpEL表达式，如："创建菜谱: #{#request.name}" */
    String detail() default "";
}
