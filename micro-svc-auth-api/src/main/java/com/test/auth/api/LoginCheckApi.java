package com.test.auth.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.test.auth.api.hystrix.LoginCheckApiFallback;
import com.test.auth.constant.Canstant;


/**
 * 用户登录验证接口
 * 检验登录状态
 * 获取用户数据
 * 获取权限等接口
 * @author yuxue
 * @date 2019-08-08
 */
@FeignClient(value = Canstant.MICRO_AUTH, fallbackFactory = LoginCheckApiFallback.class)
@RequestMapping("/check")
public interface LoginCheckApi {
	
	/**
	 * 根据Token，判断是否登录
	 * @return
	 */
	@RequestMapping(value="/checkLogin", method=RequestMethod.POST)
	public Object checkLogin();
	
	@RequestMapping(value="/getUserInfo", method=RequestMethod.POST)
	public Object getUserInfo();
	
	/**
	 * 根据请求url，判断是否有权限
	 * 需要先判断是否登录
	 * 未配置权限码的接口，默认放行；后续在数据库添加控制字段
	 * 这里的参数名称，不能叫token，碰到过冲突的场景
	 * @return
	 */
	@RequestMapping(value="/checkPermission", method=RequestMethod.POST)
	public Object checkPermission(@RequestParam("cookie")String cookie, @RequestParam("checkUrl")String checkUrl);
	
	
	@RequestMapping(value="/getPermission", method=RequestMethod.POST)
	public Object getPermission(@RequestParam("module")String module, @RequestParam("userEntity")String userEntity);
	
}
