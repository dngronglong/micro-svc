package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;




/**
 * spring cloud + shiro框架集成
 * 微服务demo项目，不集成shiro
 * 通过网关提供的参数，获取用户登录信息
 * 
 * @author yuxue
 * @date 2019-11-15
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
