package com.test.auth.service;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.test.common.model.Result;
import com.test.shiro.entity.UserEntity;

public interface LoginService {
	
	public UserEntity byLoginName(String loginName, String password);
	
	public Object loginByMiniPro(Map<String, String> paramMap);
	
	
	public Object bindUserByMiniPro(Map<String, String> paramMap);
	
	
	public void loginByPublicNo(Map<String, String> paramMap, HttpServletRequest request, HttpServletResponse response) throws IOException ;
	
	
	public Object bindUserAndLogin(String appid, String openId, String loginName, String password, Integer wechatType);

	public void loginWithoutUser(Map<String, String> paramMap, HttpServletRequest request, HttpServletResponse response) throws IOException ;
	
	public Object unbindUser(Integer userId);
	
	public Result judgeWeChatIsBind(Integer userId);
	
	
	
}
