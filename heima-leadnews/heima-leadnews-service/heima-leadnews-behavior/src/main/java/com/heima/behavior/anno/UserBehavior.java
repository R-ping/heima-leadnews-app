package com.heima.behavior.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserBehavior {
    String action() default "UNKNOWN";  // 添加参数，例如操作类型
    String description() default "";    // 添加描述信息
}
