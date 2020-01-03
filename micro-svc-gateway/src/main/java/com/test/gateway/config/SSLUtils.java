package com.test.gateway.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Http重定向到Https
 * 配置应用 监听两个端口，一个用于http协议，一个用于https协议
 * 
 * @author yuxue
 * @date 2019-08-22
 */
@Configuration
public class SSLUtils {
	
	@Value("${server.port}")
	private String httpsPort;	//读取yml配置文件参数
	
	@Value("${server.http.port}")
	private String httpPort;	//读取yml配置文件参数
	
    @Bean
    public Connector connector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setSecure(true);	//同时兼容https http
        connector.setPort(Integer.parseInt(httpPort));
        connector.setRedirectPort(Integer.parseInt(httpsPort));
        return connector;
    }

    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory(Connector connector) {
        TomcatServletWebServerFactory webServerFactory = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection securityCollection = new SecurityCollection();
                securityCollection.addPattern("/*");
                securityConstraint.addCollection(securityCollection);
                context.addConstraint(securityConstraint);
            }
        };
        webServerFactory.addAdditionalTomcatConnectors(connector);
        return webServerFactory;
    }
}

