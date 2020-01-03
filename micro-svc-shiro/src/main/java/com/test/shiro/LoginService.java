package com.test.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.stereotype.Service;

import com.test.common.constant.Constant;
import com.test.shiro.entity.UserEntity;


@Service
public class LoginService {

	public UserEntity getSystemUser() {
		Session session = SecurityUtils.getSubject().getSession();
		UserEntity user = (UserEntity) session.getAttribute(Constant.SSO_USER);
		return user;
	}

}
