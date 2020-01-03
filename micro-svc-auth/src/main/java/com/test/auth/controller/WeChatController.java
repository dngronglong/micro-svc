package com.test.auth.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.test.auth.service.LoginService;
import com.test.common.model.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 微信相关接口
 * @author yuxue
 * @date 2019-09-17
 */
@Controller
@RequestMapping("wechat")
@Api(description = "微信登录")
public class WeChatController {

	@Autowired
	private LoginService loginService;

	
	@ResponseBody
	@PostMapping("loginByMiniPro")
	@ApiOperation(value = "小程序登录", notes = "小程序登录")
	public Object loginByMiniPro(@RequestParam String appid, @RequestParam String code, @RequestParam String encryptedData, @RequestParam String iv) {

		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("js_code", code);
		paramMap.put("encryptedData", encryptedData);
		paramMap.put("iv", iv);
		paramMap.put("appid", appid);

		return loginService.loginByMiniPro(paramMap);
	}


	@ResponseBody
	@PostMapping("bindUserByMiniPro")
	@ApiOperation(value = "小程序账号绑定", notes = "小程序账号绑定")
	public Object bindUserByMiniPro(@RequestParam String appid, @RequestParam String code, @RequestParam String loginName, @RequestParam String loginPasswd) {
		if (StringUtils.isEmpty(appid)) {
			return Result.error("appid为空");
		}
		if (StringUtils.isEmpty(loginName)) {
			return Result.error("登陆账号为空");
		}
		if (StringUtils.isEmpty(loginPasswd)) {
			return Result.error("登录密码为空");
		}
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("appid", appid);
		paramMap.put("code", code);
		paramMap.put("loginName", loginName);
		paramMap.put("loginPasswd", loginPasswd);

		return loginService.bindUserByMiniPro(paramMap);
	}

	
	@GetMapping("loginByPublicNo")
	@ApiOperation(value = "公众号登录", notes = "公众号登录， 需要先绑定内部用户名密码; 登录成功，后端进行页面跳转，需要前端传递跳转页面链接")
	public void loginByPublicNo(@RequestParam String code, @RequestParam String redirectorUrl, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// 获取token
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("code", code);
		String success_url = "/error.jsp";
		String city = "";
		if (!StringUtils.isEmpty(redirectorUrl)) {
			String str[] = redirectorUrl.split(";");
			success_url = str[0];
			city = str.length > 1 ? str[1] : "china";
		}
		paramMap.put("success_url", success_url);
		paramMap.put("city", city);
		loginService.loginByPublicNo(paramMap, request, response);
	}

	
	
	@ResponseBody
	@PostMapping("bindByPublicNo")
	@ApiOperation(value = "公众号账号绑定", notes = "公众号账号绑定")
	public Object bindByPublicNo(HttpServletRequest request, @RequestParam String appid, @RequestParam String loginName, @RequestParam String password) {
		String openId = request.getSession().getAttribute("openId").toString();
		if (openId == null) {
			return Result.error("openid不能为空！");
		}
		if (StringUtils.isEmpty(appid)) {
			return Result.error("appid为空");
		}
		if (StringUtils.isEmpty(openId)) {
			return Result.error("需获取微信授权！");
		}
		if (StringUtils.isEmpty(loginName)) {
			return Result.error("账号名为空");
		}
		if (StringUtils.isEmpty(password)) {
			return Result.error("密码为空");
		}
		return loginService.bindUserAndLogin(appid, openId.toString(), loginName, password, 1);
	}

	
	
	/**
	 * 微信临时用户登录 公众号
	 * 生成临时账户
	 * @param code 微信登录状态校验code
	 * @param successUrl 登录成功之后重定向的页面路径
	 * @return
	 */
	@GetMapping("loginWithoutUser")
	@ApiOperation(value = "微信临时用户登录 公众号", notes = "宣城微信公众号登录，生成临时账户， 登录成功之后重定向的页面路径")
	public void loginWithoutUser(@RequestParam String code, @RequestParam String successUrl, HttpServletRequest request, HttpServletResponse response) throws IOException  {
		Map<String, String> paramMap = new HashMap<>();
		paramMap.put("code", code);

		String register_url = "/error.jsp";
		String success_url = "/error.jsp";
		if (!StringUtils.isEmpty(successUrl)) {
			success_url = successUrl;
		}
		paramMap.put("register_url", register_url);
		paramMap.put("success_url", success_url);
		loginService.loginWithoutUser(paramMap, request, response);
	}
	

	@GetMapping("unbindUser")
	@ApiOperation(value = "微信号解绑", notes = "微信号解绑")
	@ResponseBody
	public Object unbindUser(@RequestParam Integer userId) {
		if (Objects.isNull(userId)) {
			return Result.error("userId不能为空");
		}

		return Result.ok(loginService.unbindUser(userId));
	}


	@ResponseBody
	@ApiOperation(value = "微信是否绑定", notes = "微信是否绑定")
	@GetMapping("judgeWeChatIsBind")
	public Object judgeWeChatIsBind(@RequestParam Integer userId) {
		return loginService.judgeWeChatIsBind(userId);
	}

}
