package com.test.auth.controller;


import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONObject;
import com.test.auth.api.LoginCheckApi;
import com.test.auth.service.CheckService;
import com.test.common.annotation.RetExclude;
import com.test.common.enumtype.FwWebError;
import com.test.common.exception.ResultReturnException;
import com.test.shiro.entity.UserEntity;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;


/**
 * 
 * @author yuxue
 * @date 2019-08-22
 */
@Api(description = "登录及权限校验")
@RestController
@RequestMapping("check")
@Slf4j
public class CheckController implements LoginCheckApi{
	
	@Autowired
	private CheckService checkService;

	
	@RetExclude
	@ApiOperation(value = "登录校验", notes = "从cookie拿到token去校验")
	@Override
	@PostMapping("/checkLogin")
	@RequestMapping(value="/checkLogin", method=RequestMethod.POST)
	public Object checkLogin() {
		return checkService.checkLogin();
	}
	

	@ApiOperation(value = "获取用户信息", notes = "从cookie拿到token去校验")
	@Override
	@RequestMapping(value="/getUserInfo", method=RequestMethod.POST)
	public Object getUserInfo() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		UserEntity user = checkService.getUserInfo(request, null);
		if(null == user || user.getId()<= 0) {
			throw new ResultReturnException(FwWebError.NO_LOGIN);
		}
		return user;
	}
	

	@RetExclude
	@ApiOperation(value = "验证权限", notes = "从cookie拿到token去校验，支持传递token参数， 会先验证登录状态")
	@Override
	@RequestMapping(value="/checkPermission", method=RequestMethod.POST)
	public Object checkPermission(@RequestParam("cookie")String cookie, @RequestParam("checkUrl")String checkUrl) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		// log.info("Cookie==>" + request.getHeader("Cookie"));
		// log.info("param==>" + cookie);
		return checkService.checkPermission(request, cookie, checkUrl);
	}

	
	@ApiOperation(value = "按模块获取权限码", notes = "按服务名称")
	@ApiImplicitParam(name = "module", dataType = "String", required = true, paramType = "query")
	@Override
	@RequestMapping(value="/getPermission", method=RequestMethod.POST)
	public Object getPermission(String module, String userEntity) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();
		UserEntity user = JSONObject.parseObject(userEntity, UserEntity.class);
		log.debug(user.toString());
		return checkService.getPermission(module, request);
	}
	
	
	@ApiOperation(value = "按模块刷新内存权限资源", notes = "按模块刷新内存权限资源")
	@ApiImplicitParam(name = "module", dataType = "String", required = true, paramType = "query")
	@RequestMapping(value="/refreshPermission", method=RequestMethod.POST)
	public Object refreshPermission(String module) {
		return checkService.refreshPermission(module);
	}
	

}
