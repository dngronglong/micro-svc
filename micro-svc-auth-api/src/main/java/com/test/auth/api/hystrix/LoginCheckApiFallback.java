package com.test.auth.api.hystrix;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.test.auth.api.LoginCheckApi;

import feign.hystrix.FallbackFactory;

@Component
public class LoginCheckApiFallback implements FallbackFactory<LoginCheckApi> {

    @Override
    public LoginCheckApi create(Throwable arg0) {

        final Map<String, Object> result = new HashMap<String, Object>() {
            private static final long serialVersionUID = 1L;

            {
                put("code", 500);
                put("success", false);
                put("msg", "auth服务不可用");
            }
        };

        return new LoginCheckApi() {
            @Override
            public Object checkLogin() {
                return result;
            }

            @Override
            public Object getPermission(String module, String userEntity) {
                return result;
            }

            @Override
            public Object getUserInfo() {
                return result;
            }

            @Override
            public Object checkPermission(String cookie, String checkUrl) {
                return result;
            }
        };
    }
}
