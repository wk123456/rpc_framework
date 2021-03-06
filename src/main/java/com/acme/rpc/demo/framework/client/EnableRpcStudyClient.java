package com.acme.rpc.demo.framework.client;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: K.
 * @date: 2021/1/20 下午2:42
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RpcStudyClientRegisty.class)
public @interface EnableRpcStudyClient {
    //扫描的包，如果为空，根据启动类所在的包名扫描
    String[] basePackages() default {};
}
