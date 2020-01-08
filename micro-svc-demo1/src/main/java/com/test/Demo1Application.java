package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * spring cloud + shiro框架集成
 * 微服务demo项目，集成shiro, 跟micro-svc-auth 配置相同的redis，共享redis会话
 * 依赖micro-svc-shiro模块，通过shiro安全框架获取登录用户信息
 * 
 * @author yuxue
 * @date 2019-11-15
 */
@SpringBootApplication
public class Demo1Application {

	public static void main(String[] args) {
		SpringApplication.run(Demo1Application.class, args);
	}

}
