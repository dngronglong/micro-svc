package com.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;


/**
 * 认证授权模块，负责校验用户登录状态及是否有接口访问权限
 * 通过请求url、请求方式、微服务名称 三者确定一个权限的唯一性
 * 使用shiro安全框架的认证及会话管理功能，没有使用其授权功能
 * 
 * @author yuxue
 * @date 2019-11-15
 */
@SpringBootApplication
@EnableTransactionManagement // 开启启注解事务管理
public class AuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

}
