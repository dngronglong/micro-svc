package com.test.auth.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.test.auth.mapper.InitCacheMapper;
import com.test.common.constant.Constant;
import com.test.redis.RedisUtil;

import lombok.extern.slf4j.Slf4j;


/**
 * InitCache(初始化redis数据)
 *
 * @author zhangxueyu
 * @date 2018年9月26日
 * @discription
 *
 */
@Service
@Slf4j
public class InitCache {

	@Autowired
	InitCacheMapper initCacheMapper;
	
	@Autowired
	RedisUtil redisUtils;

	@PostConstruct
	public void initCache() {
		this.initKeyValue();
		log.info("初始化完成");
	}


	private void initKeyValue() {

		boolean flag = redisUtils.exists(Constant.PARAM_KEY);

		if (flag) {
			redisUtils.remove(Constant.PARAM_KEY);
		}

		List<Map<String, String>> configList = initCacheMapper.queryKeyValueList("'message','wechat','sync'");
		for (Map<String, String> map : configList) {
			redisUtils.hmSet(Constant.PARAM_KEY, map.get("data_key"), map.get("data_value"));
		}

	}

}
