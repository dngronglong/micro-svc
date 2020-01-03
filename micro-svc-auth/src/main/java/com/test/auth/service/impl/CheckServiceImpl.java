package com.test.auth.service.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.crazycake.shiro.RedisManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.crazycake.shiro.SerializeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.test.auth.entity.ComResourcesEntity;
import com.test.auth.mapper.ComResourcesMapper;
import com.test.auth.service.CheckService;
import com.test.common.constant.Constant;
import com.test.common.model.Result;
import com.test.shiro.entity.UserEntity;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CheckServiceImpl implements CheckService {

	// 不校验登录的资源--在application-url配置文件处理
	// --后期再考虑从数据库配置，如果从数据库添加，处理起来比较消耗性能

	
	/**
	 * 举例：
	 * "brain-facililty": {"post/brain-facililty/repairmain": 410, "get/brain-facililty/repairmain/faultrecord": 423 ....}
	 */
	// 需要检验权限的资源
	public static ConcurrentHashMap<String, HashMap<String, Integer>> resMap = new ConcurrentHashMap<String, HashMap<String, Integer>>();

	// 不校验权限的资源
	public static ConcurrentHashMap<String, HashMap<String, Integer>> unCheckResMap = new ConcurrentHashMap<String, HashMap<String, Integer>>();

	@Value("${test.pass-without-res:false}")
	private Boolean isPass;

	@Autowired
	private ComResourcesMapper comResourcesMapper;
	

	@Autowired
	private RedisSessionDAO redisSessionDAO;

	/**
	 * 跟随应用启动后执行
	 * 将系统资源（校验登录、校验权限的所有资源），转存到redis
	 * -- 资源数据较少，不写入redis，，直接放内存了
	 */
	@PostConstruct
	public void initResToRedis() {
		Map<String, Object> map = Maps.newHashMap();
		map.put("permissionCheck", 0);	// 需要校验权限的资源
		this.toCache(map);
		return ;
	}

	public Object freshResToRedis(String module) {
		if(resMap.contains(module)) {
			resMap.remove(module);
		}
		Map<String, Object> map = Maps.newHashMap();
		map.put("permissionCheck", 0);
		map.put("module", module);
		this.toCache(map);
		return 1;
	}

	public void toCache(Map<String, Object> map) {
		List<ComResourcesEntity> res = comResourcesMapper.selectByCondition(map);
		if(null == res || res.size() <=0 ) {
			return;
		}
		for (ComResourcesEntity entity : res) {
			String hashKey = entity.getRequestType().concat("/").concat(entity.getModule()).concat(entity.getRequestUrl());
			hashKey = hashKey.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("//", "/");
			HashMap<String, Integer> value = null;
			if(resMap.containsKey(entity.getModule())) {
				value = resMap.get(entity.getModule());
			} else {
				value = new HashMap<>();
			}
			value.put(hashKey.toLowerCase(), entity.getId());
			resMap.put(entity.getModule(), value);
		}
	}


	@PostConstruct
	public void initUnCheckResToRedis() {
		Map<String, Object> map = Maps.newHashMap();
		map.put("permissionCheck", 1);	// 不需要校验权限的资源
		this.uncheckToCache(map);
		return ;
	}

	public void freshUnCheckResToRedis(String module) {
		Map<String, Object> map = Maps.newHashMap();
		map.put("permissionCheck", 1);	// 不需要校验权限的资源
		map.put("module", module);
		this.uncheckToCache(map);
		return ;
	}

	public void uncheckToCache(Map<String, Object> map) {
		List<ComResourcesEntity> res = comResourcesMapper.selectByCondition(map);
		if(null == res || res.size() <=0 ) {
			return;
		}
		for (ComResourcesEntity entity : res) {
			String hashKey = entity.getRequestType().concat("/").concat(entity.getModule()).concat(entity.getRequestUrl());
			hashKey = hashKey.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("//", "/");
			HashMap<String, Integer> value = null;
			if(unCheckResMap.containsKey(entity.getModule())) {
				value = unCheckResMap.get(entity.getModule());
			} else {
				value = new HashMap<>();
			}
			value.put(hashKey.toLowerCase(), entity.getId());
			unCheckResMap.put(entity.getModule(), value);
		}
	}



	@Override
	public Boolean checkLogin() {
		Session session = SecurityUtils.getSubject().getSession();
		UserEntity user = (UserEntity) session.getAttribute(Constant.SSO_USER);
		if(null == user || user.getId() <=0) {
			return false;
		}
		return true;
	}

	@Override
	public UserEntity getUserInfo(HttpServletRequest request, String cookie) {

		// 获取登录用户信息，判断是否登录 -- 默认使用request 获取cookie方式
		Session session = SecurityUtils.getSubject().getSession();
		UserEntity user = (UserEntity) session.getAttribute(Constant.SSO_USER);
		if(null == user || user.getId() <=0) {

			String token = request.getParameter("token");
			if(StringUtils.isEmpty(token) && !StringUtils.isEmpty(cookie)) {
				token = cookie;
			}
			// System.out.println("getUserInfo==>" + token);
			if(!StringUtils.isEmpty(token) && !token.contains("JSESSIONID") && !"undefined".equals(token)) {
				// log.info("默认从cookie获取用登录信息失败，尝试根据token获取==>" + token);
				RedisManager redisManager = redisSessionDAO.getRedisManager();
				Session s = (Session)SerializeUtils.deserialize(redisManager.get(this.getByteKey(redisSessionDAO.getKeyPrefix(), token)));
				if(null != s && null != s.getAttribute(Constant.SSO_USER)) {
					user = (UserEntity) s.getAttribute(Constant.SSO_USER);
				}
				if(null == user || user.getId() <=0) {
					return null;
				}
			} else {
				return null;
			}
		}
		user.setLoginPasswd(null);
		return user;
	}

	/**
	 * RedisSessionDAO里面的方法
	 * @param keyPrefix
	 * @param sessionId
	 * @return
	 */
	private byte[] getByteKey(String keyPrefix, Serializable sessionId){
		String preKey = keyPrefix + sessionId;
		return preKey.getBytes();
	}


	/**
	 * 权限判断接口：先查询到资源对应的id，然后根据用户权限判断
	 * 
	 */
	@Override
	public Result checkPermission(HttpServletRequest request, String cookie, String checkUrl) {
		UserEntity user = this.getUserInfo(request, cookie);
		if(null == user || user.getId() <=0) {
			return Result.error("未登录", 401);
		}

		// 获取用户功能权限ID集合
		Set<Integer> permissionSet = user.getPermissionId();
		// 减少放到请求中的属性
		user.setPermission(null);	
		user.setPermissionId(null);	

		// 获取微服务名称
		String[] str = checkUrl.split("/");
		String module = str[1];
		
		// 判断是否是免校验资源
		if(this.getIdByUrl(unCheckResMap.get(module), checkUrl) > 0) {
			return Result.ok(JSONObject.toJSONString(user));
		}

		// 用户完全没有权限, 且请求资源不是开放资源
		if(null == permissionSet || permissionSet.size() <= 0) {
			log.info("当前用户未分配权限:" + user.getLoginName());
			return Result.error("无权限", 401);
		}

		// 获取系统指定模块资源
		Integer resId = this.getIdByUrl(resMap.get(module), checkUrl);
		
		// 系统没有配置该权限，或者请求路径不存在
		if(resId <= 0 && isPass) {
			// log.info("系统没有配置该资源对应的权限, 但是配置放行：" + uri);
			return Result.ok(JSONObject.toJSONString(user));
		}
		
		// 系统配置了权限
		if(permissionSet.contains(resId)) {
			return Result.ok(JSONObject.toJSONString(user));
		}

		return Result.error("无权限", 401);
	}
	
	public Integer getIdByUrl(HashMap<String, Integer> value, String url) {
		Integer result = 0;
		if(null != value && value.size() > 0) {
			Set<Integer> resultSet = Sets.newHashSet();
			
			if(value.containsKey(url)) {
				result = value.get(url);
			} else {
				// 遍历，匹配，处理@PathVariable注解的请求
				value.entrySet().forEach(entry -> {
					String key1 = entry.getKey();
					if(key1.contains("{")) {
						AntPathMatcher matcher = new AntPathMatcher();
						if(matcher.match(key1, url)) {
							resultSet.add(entry.getValue());
						}
					}
				});
			}
			if(resultSet.size() > 0) {
				result = resultSet.stream().findFirst().get();
			}
		}
		return result;
	}


	/**
	 * 按模块，获取用户的权限码数据
	 * 按模块查询到所有的资源，然后从用户redis会话，获取permissionIdSet
	 * 遍历，将用户权限码Set返回
	 */
	@Override
	public Set<String> getPermission(String module, HttpServletRequest request) {
		Map<String, Object> map = Maps.newHashMap();
		map.put("module", module);
		List<ComResourcesEntity> res = comResourcesMapper.selectByCondition(map);
		if(null == res || res.size() <=0 ) {
			return null;
		}
		UserEntity user = this.getUserInfo(request, null);
		if(null == user) {
			return null;
		}

		Set<Integer> permissionId = user.getPermissionId();
		Set<String> set = Sets.newHashSet();
		res.forEach(n->{
			if(permissionId.contains(n.getId())) {
				set.add(n.getPermissions());
			}
		});
		set.remove("");
		return set;
	}

	/**
	 * 刷新下权限
	 */
	@Override
	public Object refreshPermission(String module) {
		this.freshResToRedis(module);
		this.freshUnCheckResToRedis(module);
		return resMap.get(module);
	}
	
	


}
