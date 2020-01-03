package com.test.auth.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * InitCacheMapper
 *
 * @author zhangxueyu
 * @date 2018年9月26日
 * @discription
 *
 */
@Mapper
public interface InitCacheMapper {

	List<Map<String, String>> queryKeyValueList(@Param("dataType") String dataType);
}
