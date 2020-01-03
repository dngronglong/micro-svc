package com.test.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域通配
 * 支持 @CrossOrigin 注解局部声明
 * @author yuxue
 * @date 2018-09-07
 */
@Configuration
public class CorsConfig {

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				/** * 至少需要addMapping *** */
			registry
	            .addMapping("/**")
	            .allowedOrigins("*")
	            .allowedMethods("PUT", "DELETE", "GET", "POST", "OPTIONS", "HEAD")
	            .allowedHeaders(
	            	"Content-Type", 
	            	"X-Requested-With", 
	            	"accept", 
	            	"Authorization", 
	            	"Origin", 
	            	"Access-Control-Request-Method", 
	            	"Access-Control-Request-Headers")
	            .allowCredentials(true)//是否带上cookie
	            .maxAge(3600)
	            .exposedHeaders(
	                "access-control-allow-headers",
	                "access-control-allow-methods",
	                "access-control-allow-origin",
	                "access-Control-allow-credentials",
	                "access-control-max-age",
	                "X-Frame-Options");
			}
		};
	}
	

	private CorsConfiguration buildConfig() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
		corsConfiguration.addAllowedOrigin("*");
		corsConfiguration.addAllowedHeader("*");
		corsConfiguration.addAllowedMethod("*");
		corsConfiguration.setAllowCredentials(true);
		return corsConfiguration;
	}

	/**
	 * 拦截器跨域配置
	 *
	 * @return
	 */
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", buildConfig());
		return new CorsFilter(source);
	}
}
