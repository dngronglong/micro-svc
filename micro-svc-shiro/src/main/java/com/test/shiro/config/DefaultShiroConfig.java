package com.test.shiro.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.test.shiro.DefaultRealm;
import com.test.shiro.DefaultRedisManager;
import com.test.shiro.filter.DefaultUserFilter;



/***
 * 内部使用的shiro配置
 * 统一一个redis共享会话，，要求redis的链接参数一样
 * @author yuxue
 * @date 2019-08-15
 */
@Configuration
@ConfigurationProperties(prefix = "spring") // application.yml中的spring下的属性
@ConditionalOnMissingBean(ShiroFilterFactoryBean.class)
public class DefaultShiroConfig {

	private Map<String, String> redis = new HashMap<>();

	public Map<String, String> getRedis() {
		return redis;
	}

	public void setRedis(Map<String, String> redis) {
		this.redis = redis;
	}

	@Bean
	public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {

		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

		// Shiro的核心安全接口,这个属性是必须的
		shiroFilterFactoryBean.setSecurityManager(securityManager);

		// 没有登陆的用户只能访问登陆页面
		shiroFilterFactoryBean.setLoginUrl("./login");

		// 登录成功后要跳转的链接
		shiroFilterFactoryBean.setSuccessUrl("./index");

		// 未授权界面; ----这个配置了没卵用，具体原因想深入了解的可以自行百度
		// shiroFilterFactoryBean.setUnauthorizedUrl("/auth/403");

		// 自定义拦截器
		Map<String, Filter> filtersMap = new LinkedHashMap<String, Filter>();
		// filtersMap.put("loginCheck", new LoginCheckFilter());
		filtersMap.put("userFilter", new DefaultUserFilter());
		shiroFilterFactoryBean.setFilters(filtersMap);

		// 权限控制map
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
		filterChainDefinitionMap.put("/static/**", "anon");
		filterChainDefinitionMap.put("/css/**", "anon");
		filterChainDefinitionMap.put("/js/**", "anon");
		filterChainDefinitionMap.put("/img/**", "anon");
		filterChainDefinitionMap.put("/login", "anon");
		filterChainDefinitionMap.put("/auth/doLogin", "anon");
		filterChainDefinitionMap.put("/auth/logout", "anon");

		filterChainDefinitionMap.put("/**", "anon");
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}

	@Bean
	public SecurityManager securityManager() {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		// 设置realm
		securityManager.setRealm(defaultRealm());
		// 自定义缓存实现 使用redis
		securityManager.setCacheManager(cacheManager());
		// 自定义session管理 使用redis
		securityManager.setSessionManager(sessionManager());
		return securityManager;
	}

	/**
	 * 自定义身份认证realm;
	 * @return
	 */
	@Bean
	public DefaultRealm defaultRealm() {
		DefaultRealm myRealm = new DefaultRealm();
		return myRealm;
	}

	/**
	 * cacheManager 缓存 redis实现
	 * 使用的是shiro-redis开源插件
	 * 自定义了RedisCacheManager   为了支持database
	 * @return
	 */
	public RedisCacheManager cacheManager() {
		RedisCacheManager redisCacheManager = new RedisCacheManager();
		redisCacheManager.setRedisManager(redisManager());
		return redisCacheManager;
	}

	/**
	 * 配置shiro redisManager
	 * 使用的是shiro-redis开源插件
	 * 自定义了RedisManager  为了支持database
	 * @return
	 */
	public RedisManager redisManager() {
		DefaultRedisManager redisManager = new DefaultRedisManager();
		redisManager.setHost(redis.get("host"));
		redisManager.setPort(Integer.parseInt(redis.get("port")));
		redisManager.setExpire(Integer.parseInt(redis.get("expire")));// 配置缓存过期时间
		redisManager.setTimeout(Integer.parseInt(redis.get("timeout")));
		redisManager.setPassword(redis.get("password"));
		redisManager.setDatabase(Integer.parseInt(redis.get("database")));
		return redisManager;
	}

	/**
	 * Session Manager
	 * 使用的是shiro-redis开源插件
	 */
	@Bean
	public DefaultWebSessionManager sessionManager() {
		DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
		sessionManager.setSessionDAO(redisSessionDAO());
		return sessionManager;
	}

	/**
	 * RedisSessionDAO shiro sessionDao层的实现 通过redis
	 * 使用的是shiro-redis开源插件
	 */
	@Bean
	public RedisSessionDAO redisSessionDAO() {
		RedisSessionDAO redisSessionDAO = new RedisSessionDAO();
		redisSessionDAO.setRedisManager(redisManager());
		return redisSessionDAO;
	}


	/***
	 * 授权所用配置
	 * @return
	 */
	@Bean
	public DefaultAdvisorAutoProxyCreator getDefaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		return defaultAdvisorAutoProxyCreator;
	}

	/***
	 * 使授权注解起作用
	 * 如不想配置可以在pom文件中加入
	 * <dependency>
	 *<groupId>org.springframework.boot</groupId>
	 *<artifactId>spring-boot-starter-aop</artifactId>
	 *</dependency>
	 * @param securityManager
	 * @return
	 */
	/*@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
		authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
		return authorizationAttributeSourceAdvisor;
	}*/

	/**
	 * Shiro生命周期处理器
	 */
	@Bean
	public static LifecycleBeanPostProcessor getLifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

}