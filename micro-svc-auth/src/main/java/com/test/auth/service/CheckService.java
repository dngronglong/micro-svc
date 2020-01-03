package com.test.auth.service;


import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.test.shiro.entity.UserEntity;

public interface CheckService {
	
	public Boolean checkLogin();
	
	public UserEntity getUserInfo(HttpServletRequest request, String cookie);
	
	public Object checkPermission(HttpServletRequest request, String cookie, String checkUrl);
	
	public Set<String> getPermission(String module, HttpServletRequest request);
	
	public Object refreshPermission(String module);
	
}
