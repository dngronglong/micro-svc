package com.test.auth.service.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.test.auth.mapper.LoginMapper;
import com.test.auth.mapper.SystemUserMapper;
import com.test.auth.service.LoginService;
import com.test.common.constant.Constant;
import com.test.common.enumtype.FwWebError;
import com.test.common.exception.ResultReturnException;
import com.test.common.model.Result;
import com.test.common.util.AesCbcUtil;
import com.test.common.util.DateUtil;
import com.test.common.util.HttpClientUtil;
import com.test.common.util.MD5Util;
import com.test.common.util.UUIDUtil;
import com.test.redis.RedisUtil;
import com.test.shiro.entity.UserEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Service
public class LoginServiceImpl implements LoginService{
	
	
	@Autowired
	private RedisUtil redisUtils;
	
	@Autowired
	private LoginMapper loginMapper;
	
	@Autowired
	private SystemUserMapper systemUserMapper;

	@Override
	public UserEntity byLoginName(String loginName, String password) {
		
		UserEntity userEntity = loginMapper.queryUserByLoginName(loginName);
		if (null == userEntity || userEntity.getId() <= 0) {
			throw new ResultReturnException(FwWebError.WRONG_ACCOUNT_OR_PSW);
		}
		// 先退出， 再登录
		SecurityUtils.getSubject().logout();
		
		return this.doLogin(userEntity, password);
	}
	
	
	/**
	 * 用户登录，公共方法
	 * @param UserEntity 登录查询到的用户
	 * @param password 用户输入的密码，可以为空
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public UserEntity doLogin(UserEntity userEntity, String password) {
		if (null == userEntity || userEntity.getId() <= 0) {
			throw new ResultReturnException(FwWebError.WRONG_ACCOUNT_OR_PSW);
		}
		
		UserEntity vo = new UserEntity();
		vo.setId(userEntity.getId());
		// 锁定半个小时后, 重新登录自动解锁
		if (DateUtil.getDatePoor(new Date(), userEntity.getUpdateTime()) >= 30) {
			vo.setUserStatus((short) 1);
			vo.setFailCount((short) 0);
			if (loginMapper.updateByPrimaryKeySelective(vo) > 0) {
				userEntity.setUserStatus((short) 1);
				userEntity.setFailCount((short) 0);
			}
		}
		if (userEntity.getUserStatus() == 2) {
			throw new ResultReturnException("您的账号已被停用");
		}
		if (userEntity.getUserStatus() == 3) {
			throw new ResultReturnException("账号已被锁定，请半小时后重新登录，或者联系管理员处理");
		}

		if (userEntity.getFailCount() > 5) {
			vo.setUserStatus((short) 3);
			loginMapper.updateByPrimaryKeySelective(vo);
			throw new ResultReturnException("密码连续错误5次，账号已被锁定，请半小时后重新登录，或者联系管理员处理");
		}

		if (!StringUtils.isEmpty(password)) {
			String passwd = this.getPassword(userEntity.getSalt(), password);
			if (!userEntity.getLoginPasswd().equals(passwd)) {
				// 更新failcount + 1
				vo.setFailCount((short) (userEntity.getFailCount() + 1));
				loginMapper.updateByPrimaryKeySelective(vo);
				throw new ResultReturnException("用户名或者密码有误，错误次数：" + vo.getFailCount());
			}
			userEntity.setLoginPasswd(passwd);
		}
		
		// 设置用户部门信息
//		List<JSONObject> list = null;
//		Set<Integer> deptSet = Sets.newHashSet();
//		Map<String, Object> params = Maps.newHashMap();
//		params.put("ownerId", userEntity.getOwnerId());
//		if ("admin".equals(userEntity.getLoginName())) {
//			list = loginMapper.queryOrgByAdmin(params);	// 按照ID排序
//		} else {
//			params.put("userId", userEntity.getId());
//			list = loginMapper.queryOrgByUser(params);	// 按照ID排序
//		}
//		if(null != list && list.size() > 0) {
//			list.stream().forEach(n->{
//				deptSet.add(n.getInteger("orgid"));
//			});
//
//			JSONObject jo = list.get(0);
//			userEntity.setOrgId(jo.getInteger("orgid"));
//			userEntity.setOrgName(jo.getString("orgname"));
//		} else {
//			userEntity.setOrgId(0);
//			userEntity.setOrgName("");
//		}
//		userEntity.setDeptIds(deptSet);

		// 设置微信名
//		params = Maps.newHashMap();
//		params.put("userId", userEntity.getId());
//		List<JSONObject> li = loginMapper.queryWechatUser(params);
//		Optional<JSONObject> findFirst = li.stream().findFirst();
//		userEntity.setNickName(findFirst.isPresent() ? findFirst.get().getString("nick_name") : "");
		
		// 获取角色id
		userEntity.setRoleId(0);
		if (!userEntity.getLoginName().equals("admin")) {
			Set<Integer> role = loginMapper.queryRoleByUser(userEntity.getId(), userEntity.getOwnerId());
			if (null != role && role.size() > 0) {
				userEntity.setRoleIds(role);
				userEntity.setRoleId(role.stream().findFirst().get());
			}
		}

		// 设置岗位级别
//		Map<String, Object> param = Maps.newHashMap();
//		param.put("id", userEntity.getStationsId());
//		Map<String, Object> map = loginMapper.getStationsInfo(param);
//		if (map.size() > 0) {
//			userEntity.setStationsLevel(Integer.valueOf(map.get("level").toString()));
//		}
//		if (userEntity.getLoginName().equals("admin")) {
//			userEntity.setStationsLevel(1);
//		}
		
		// 获取用户权限，放到redis缓存
		Set<Integer> permsSet = Sets.newHashSet();
		if ("admin".equals(userEntity.getLoginName())) {
			permsSet = loginMapper.queryPermissionByAdmin();
		} else {
			permsSet = loginMapper.queryPermissionByUser(userEntity.getId());	// 加载所有权限
		}
		userEntity.setPermissionId(permsSet);
		
		this.shiroLogin(userEntity);
		
		vo.setFailCount((short) 0);	// 登录成功，更新failcount重置为0、 最后登录时间
		loginMapper.updateByPrimaryKeySelective(vo);

		userEntity.setLoginPasswd(null);
		return userEntity;
	}

	/**
	 * 组装token，登录，将用户信息缓存到redis
	 * @param user
	 */
	private void shiroLogin(UserEntity user) {
		user.setSalt(null);
		// 组装token
		UsernamePasswordToken token = new UsernamePasswordToken(user.getLoginName(), user.getLoginPasswd().toCharArray());
		Session session = SecurityUtils.getSubject().getSession();
		user.setToken(session.getId().toString());
		session.setAttribute(Constant.SSO_USER, user);
		// shiro登陆验证
		SecurityUtils.getSubject().login(token);
	}
	
	
	public String getPassword(String salt, String password) {
		// 用户登录密码的key
//		String input = salt + MD5Util.encrypt32(password) + "73a526076a830e445905d596157729bf";
		String input = salt + MD5Util.encrypt32(password);
		return MD5Util.encrypt32(input);
	}

	public static void main(String[] args) {
//		LoginServiceImpl service=new LoginServiceImpl();
//		service.getPassword("admin","123456");
		String pw="admin"+MD5Util.encrypt32("123456");
		String p=MD5Util.encrypt32(pw);
		System.out.println(p);
	}
	
	
	private JSONObject getWechatSession(String appid, String code) {
		String wechatkey = redisUtils.hmGet(Constant.PARAM_KEY, "wechatkey#".concat(appid)).toString();
		String wechatsercet = redisUtils.hmGet(Constant.PARAM_KEY, "wechatsercet#".concat(appid)).toString();
		Map<String, String> paramMap = new HashMap<String, String>();

		String grantType = "authorization_code";
		//String wechatRequest = "https://api.weixin.qq.com/sns/jscode2session";

		// 授权（必填）
		paramMap.put("appid", wechatkey);
		paramMap.put("secret", wechatsercet);
		paramMap.put("js_code", code);
		paramMap.put("grant_type", grantType);

		// 发送请求
		String sr = HttpClientUtil.post(Constant.WECHAT_REQ_URL, paramMap, null);

		// 解析相应内容（转换成json对象）
		return JSONObject.parseObject(sr);
	}

	// 需要调用统一认证的redis sys setting数据
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Object loginByMiniPro(Map<String, String> paramMap) {
		// 解析相应内容（转换成json对象）
		JSONObject json = this.getWechatSession(paramMap.get("appid"), paramMap.get("js_code"));
		// 获取会话密钥（session_key）
		String sessionKey = json.getString("session_key");
		// 微信登录异常，，返回异常信息 --验证是否微信登录
		if (StringUtils.isEmpty(sessionKey)) {
			return Result.error("微信登录异常");
		}
		// 用户的唯一标识（openid）
		String openId = json.getString("openid");

		// 微信登录成功，验证是否已经绑定设施系统账号
		// 根据openId查询用户信息，调用shiro登陆，返回session
		UserEntity user = systemUserMapper.queryUserByOpenId(openId);

		// 更新用户微信信息
		try {
			String result = AesCbcUtil.decrypt(paramMap.get("encryptedData"), sessionKey, paramMap.get("iv"), "UTF-8");
			if (!StringUtils.isEmpty(result)) {
				JSONObject userInfoJSON = JSONObject.parseObject(result);
				if (null == user || user.getId() <= 0) {
					systemUserMapper.insertWechatUser(userInfoJSON);
				} else {
					// 更新用户最新的微信个人信息
					systemUserMapper.updateWechatUser(userInfoJSON);
				}
			}
		} catch (Exception e) {
		}
		if (null == user || user.getId() <= 0) {
			return Result.error("未绑定系统账号");
		}
		return this.doLogin(user, null);
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Object bindUserByMiniPro(Map<String, String> paramMap) {
		String appid = paramMap.get("appid");
		String code = paramMap.get("code");
		String loginName = paramMap.get("loginName");
		String loginPasswd = paramMap.get("loginPasswd");

		// 解析相应内容（转换成json对象）
		JSONObject json = this.getWechatSession(appid, code);
		// 获取会话密钥（session_key）
		String sessionKey = json.getString("session_key");
		// 微信登录异常，，返回异常信息 --验证是否微信登录
		if (StringUtils.isEmpty(sessionKey)) {
			return Result.error("微信登录异常");
		}

		// 用户的唯一标识（openid）
		String openId = json.getString("openid");

		return this.bindUserAndLogin(appid, openId, loginName, loginPasswd, 0);
	}


	@Override
	public void loginByPublicNo(Map<String, String> paramMap, HttpServletRequest request, HttpServletResponse response) throws IOException {
		paramMap.put("register_url", redisUtils.hmGet(Constant.PARAM_KEY, "wechat.bind").toString());
		// 获取对应城市公众号的appid secret
		String city = paramMap.get("city").toString();

		// 获取微信登录token
		paramMap.put("appid", redisUtils.hmGet(Constant.PARAM_KEY, city + ".wechat.public.appid").toString());
		paramMap.put("secret", redisUtils.hmGet(Constant.PARAM_KEY, city + ".wechat.public.secret").toString());
		paramMap.put("grant_type", "authorization_code");
		String result = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/oauth2/access_token", paramMap);
		JSONObject jsonObject = JSONObject.parseObject(result);
		String openId = jsonObject.getString("openid");
		if (StringUtils.isEmpty(openId)) {
			response.sendRedirect(paramMap.get("register_url"));
		}

		UserEntity systemUser = systemUserMapper.queryUserByOpenId(openId);
		if (systemUser == null) {
			// 发送请求，获取微信用户信息
			Map<String, String> paramsMap = new HashMap<>();
			paramsMap.put("access_token", jsonObject.getString("access_token"));
			paramsMap.put("openid", openId);

			String str = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/userinfo", paramsMap);
			jsonObject = JSONObject.parseObject(str);

			systemUserMapper.deleteWechatUser(openId);
			// 保存公众号微信用户信息
			jsonObject.put("gender", jsonObject.getShort("sex"));
			jsonObject.put("avatarUrl", jsonObject.getString("headimgurl"));
			jsonObject.put("nickName", jsonObject.getString("nickname"));
			jsonObject.put("openId", openId);
			systemUserMapper.insertWechatUser(jsonObject);

			// 将openid放入session缓存，绑定的时候需要用到
			request.getSession().setAttribute("openId", openId);
			// 未绑定用户，由前端负责跳转到绑定页面
			// return ResponseResult.failed("当前微信未绑定系统用户");
			response.sendRedirect(paramMap.get("register_url") + "?" + Constant.REDIRECT_URL + "=" + paramMap.get("success_url"));
		} else {
			this.doLogin(systemUser, null);
			Session session = SecurityUtils.getSubject().getSession();

			String url = paramMap.get("success_url").toString();
			if (url.indexOf("?") > -1) {
				url = url + "&" + Constant.TOKEN + "=" + session.getId();
				// 特殊处理 测试发现微信截掉了部分参数
				url = url.replace("~", "&");
			} else {
				url = url + "?" + Constant.TOKEN + "=" + session.getId();
			}
			// response.addHeader("Cookie", Constant.JSESSIONID.concat("=").concat(session.getId().toString()));
			response.sendRedirect(url);
		}
	}

	@Override
	public Object bindUserAndLogin(String appid, String openId, String loginName, String password, Integer wechatType) {

		JSONObject app = JSONObject.parseObject(redisUtils.hmGet(Constant.SYSTEM_APP_KEY, appid).toString());
		Integer systemId = app.getInteger("id"); // 系统id

		JSONObject params = new JSONObject();
		params.put("openId", openId);
		int result = systemUserMapper.judgeWeChatIsBind(params);
		if (result > 0) {
			return Result.error("该微信号已经绑定账号! ");
		}

		UserEntity record = new UserEntity();
		record.setLoginName(loginName);
		UserEntity systemUser = systemUserMapper.findByCondition(record);
		if (systemUser == null || systemUser.getId() <= 0) {
			return Result.error("账号不存在！");
		}

		// 校验密码是否正确
		String passwd = this.getPassword(systemUser.getSalt(), password);
		if (!systemUser.getLoginPasswd().equals(passwd)) {
			return Result.error("用户名或者密码有误");
		}

		Map<String, Object> paramMap = Maps.newHashMap();
		paramMap.put("userId", systemUser.getId());
		paramMap.put("userType", wechatType);
		// 根据UserId查询已经绑定的公众号微信
		List<Map<String, Object>> wechat = systemUserMapper.queryBindByMap(paramMap);
		if (null != wechat && wechat.size() > 0) {
			return Result.error("当前用户已经绑定微信号！");
		}

		// 写入绑定关系
		Map<String, Object> param = new HashMap<>();
		param.put("userId", systemUser.getId());
		param.put("openId", openId);
		param.put("systemId", systemId);
		// 0小程序 1 公众号
		param.put("userType", wechatType);

		systemUserMapper.insertBindRef(param);

		// 登录当前用户
		return this.doLogin(systemUser, null);
	}

	
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void loginWithoutUser(Map<String, String> paramMap, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String openId = "";
		JSONObject jsonObject = null;
		try {
			// 获取微信登录token
			paramMap.put("appid", redisUtils.hmGet(Constant.PARAM_KEY, "china.wechat.public.appid").toString());
			paramMap.put("secret", redisUtils.hmGet(Constant.PARAM_KEY, "china.wechat.public.secret").toString());
			paramMap.put("grant_type", "authorization_code");
			String result = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/oauth2/access_token", paramMap);
			jsonObject = JSONObject.parseObject(result);
			openId = jsonObject.getString("openid");

			// 发送请求，获取微信用户信息
			Map<String, String> paramsMap = new HashMap<>();
			paramsMap.put("access_token", jsonObject.getString("access_token"));
			paramsMap.put("openid", openId);

			String str = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/userinfo", paramsMap);
			jsonObject = JSONObject.parseObject(str);
			// 公众号微信用户信息
			jsonObject.put("gender", jsonObject.getShort("sex"));
			jsonObject.put("avatarUrl", jsonObject.getString("headimgurl"));
			jsonObject.put("nickName", jsonObject.getString("nickname"));
			jsonObject.put("openId", openId);
		} catch (Exception e) {
			response.sendRedirect("/error.jsp");
		}

		UserEntity systemUser = systemUserMapper.queryUserByOpenId(openId);
		if (systemUser == null) {

			// 记录微信用户个人 信息
			systemUserMapper.insertWechatUser(jsonObject);

			// 未绑定用户，生成临时账号
			// user表添加临时用户, 返回用户ID
			systemUser = new UserEntity();
			String u= "U" + System.currentTimeMillis();
			systemUser.setUserName(u);
			systemUser.setLoginName(u);
			systemUser.setUserType((short) 1); // 1 公众临时用户
			systemUser.setSalt(UUIDUtil.getUUID());
			String loginPasswd = this.getPassword(systemUser.getSalt(), Constant.DEFAULT_PASSWORD);
			systemUser.setLoginPasswd(loginPasswd);

			String success_url = paramMap.get("success_url");

			String[] urls = success_url.split(";");
			if (urls.length > 1) {
				systemUser.setOwnerId(Integer.valueOf(redisUtils.hmGet(Constant.PARAM_KEY, "ownerid." + urls[1]).toString()));
			} else {

				systemUser.setOwnerId(14); // 宣城业主
			}

			// 写入数据，返回userId
			if (systemUserMapper.insertSelective(systemUser) <= 0) {
				response.sendRedirect("/error.jsp");
			}
			// t_user_userbind表添加临时用户
			Map<String, Object> param = new HashMap<>();
			param.put("userId", systemUser.getId());
			param.put("openId", openId);
			param.put("userType", 1); // 0内部用户 1公众用户
			param.put("userStatus", 1);
			param.put("systemId", 0); // 临时微信用户，不设置归属系统

			if (systemUserMapper.insertBindRef(param) <= 0) {
				response.sendRedirect("/error.jsp");
			}
		} else {
			// 更新微信用户个人信息
			systemUserMapper.updateWechatUser(jsonObject);
		}

		systemUser = systemUserMapper.queryUserByOpenId(openId);
		this.doLogin(systemUser, null);
		Session session = SecurityUtils.getSubject().getSession();
		
		// response.addHeader("Cookie", Constant.JSESSIONID.concat("=").concat(session.getId().toString()));
		
		response.sendRedirect(paramMap.get("success_url").split(";")[0] + "?" + Constant.TOKEN + "=" + session.getId());
	}


	@Override
	@Transactional(rollbackFor = Exception.class)
	public Object unbindUser(Integer userId) {
		Map<String, Object> param = Maps.newHashMap();
		param.put("userId", userId);
		return systemUserMapper.delBindInfo(param);
	}


	@Override
	public Result judgeWeChatIsBind(Integer userId) {
		JSONObject param = new JSONObject();
		param.put("userId", userId);
		param.put("systemId", 0);
		int result = systemUserMapper.judgeWeChatIsBind(param);
		if (result <= 0) {
			return Result.error("该账号未绑定微信");
		}
		return Result.ok();
	}
	
	
	
}
