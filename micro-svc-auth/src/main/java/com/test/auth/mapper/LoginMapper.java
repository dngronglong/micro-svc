package com.test.auth.mapper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.alibaba.fastjson.JSONObject;
import com.test.shiro.entity.UserEntity;

@Mapper
public interface LoginMapper {

	int updateByPrimaryKeySelective(UserEntity record);

	int updateByPrimaryKey(UserEntity record);
	
	Map<String, Object> getStationsInfo(Map<String, Object> param);
	
	UserEntity queryUserByLoginName(@Param("loginName") String loginName);
	
	Set<Integer> queryPermissionByAdmin();
	
	Set<Integer> queryPermissionByUser(Integer userId);
	
	Set<Integer> queryRoleByUser(@Param("userId")Integer userId, @Param("ownerId")Integer ownerId);
	
	List<JSONObject> queryWechatUser(Map<String, Object> params);
	
	List<JSONObject> queryOrgByAdmin(Map<String, Object> params);
	
	List<JSONObject> queryOrgByUser(Map<String, Object> params);
	
}
