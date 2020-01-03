package com.test.auth.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.test.auth.entity.ComResourcesEntity;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface ComResourcesMapper {
	
    int deleteByPrimaryKey(Integer id);

    int insert(ComResourcesEntity record);

    int insertSelective(ComResourcesEntity record);

    ComResourcesEntity selectByPrimaryKey(Integer id);

    List<ComResourcesEntity> selectByCondition(Map<String, Object> map);

    int updateByPrimaryKeySelective(ComResourcesEntity record);

    int updateByPrimaryKey(ComResourcesEntity record);
    
}