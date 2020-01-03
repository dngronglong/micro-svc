package com.test.auth.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.test.auth.service.LoginService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * 登录及退出接口
 * @author yuxue
 * @date 2019-08-22
 */
@Api(tags = "登录及退出接口")
@RestController
@RequestMapping("auth")
public class LoginController {
	
	@Autowired
	private LoginService loginService;
	
	
	@ApiOperation(value = "用户名登录", notes = "用户名、密码登录")
	@ApiImplicitParams({
		@ApiImplicitParam(name = "username", dataType = "String", required = true, paramType = "query"),
		@ApiImplicitParam(name = "password", dataType = "String", required = true, paramType = "query")
	})
	@PostMapping("/byLoginName")
	public Object byLoginName(String username, String password) {
		if (StringUtils.isBlank(username)) {
			return "Please input username";
		}
		if (StringUtils.isBlank(password)) {
			return "Please input password";
		}
		return loginService.byLoginName(username, password);
	}
	
	
	@ApiOperation(value = "退出", notes = "退出")
	@GetMapping("/logout")
	public Object systemLogout() {
		SecurityUtils.getSubject().logout();
		return null;
	}
	

}
