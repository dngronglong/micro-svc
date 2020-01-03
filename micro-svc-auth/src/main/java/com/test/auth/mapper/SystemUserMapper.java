package com.test.auth.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.alibaba.fastjson.JSONObject;
import com.test.shiro.entity.UserEntity;


/**
 * 
 * @author yuxue
 * @date 2018-08-10
 */
@Mapper
public interface SystemUserMapper {

	UserEntity queryUserByUserId(@Param("userId") Integer userId);

	UserEntity queryUserByLoginName(@Param("loginName") String loginName);

	int deleteByPrimaryKey(Integer id);

	int insert(UserEntity record);

	int insertSelective(UserEntity record);

	int updateByPrimaryKeySelective(UserEntity record);

	int updateByPrimaryKey(UserEntity record);

	UserEntity findByCondition(UserEntity systemUser);

	Integer updatePasswdForReset(UserEntity systemUser);

	List<Map<String, Object>> queryByCondition(UserEntity record);

	List<Map<String, Object>> queryByMap(JSONObject record);

	List<Map<String, Object>> getUserOrgList(@Param("id") Integer id);

	Integer getCount(UserEntity record);

	List<Map<String, Object>> getActiviUser(@Param("date") String date, @Param("ownerId") Integer ownerId);

	List<Map<String, Object>> getAppRate(@Param("ownerId") Integer ownerId);

	UserEntity queryUserByOpenId(@Param("openId") String openId);

	List<Map<String, Object>> queryBindByMap(Map<String, Object> map);

	Integer insertWechatUser(JSONObject userInfoJSON);

	Integer deleteWechatUser(@Param("openId") String openId);

	Integer delBindInfo(Map<String, Object> map);

	Integer updateWechatUser(JSONObject userInfoJSON);

	Integer insertBindRef(Map<String, Object> map);

	List<Map<String, Object>> queryStationsList(Map<String, Object> map);

	Map<String, Object> getStationsInfo(Map<String, Object> map);

	List<Map<String, Object>> queryUserByDataPer(@Param("orgId") Integer orgId);

	List<JSONObject> getUserIdByResource(JSONObject obj);

	Integer queryUserBindAccountCount(@Param("bindAccount") String bindAccount);

	List<UserEntity> queryUsersByUserIds(@Param("map") JSONObject obj);

	Integer judgeWeChatIsBind(@Param("map") JSONObject map);

	List<Map<String, Object>> queryWechatUser(Map<String, Object> map);

}
