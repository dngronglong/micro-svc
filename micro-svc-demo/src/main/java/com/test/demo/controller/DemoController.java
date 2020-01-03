package com.test.demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * 控制层
 * 通过拦截器，或者AOP的方式，处理异常信息
 * Result类，封装返回结果对象 {"code":0,"data":Object,"msg":" 成功/失败"}
 * @author yuxue
 * @date 2019-04-23 15:16:37
 */
@Api(description = "demo")
@RestController
@RequestMapping("/demo")
public class DemoController {

	@ApiOperation(value = "获取网关传递的用户信息", notes = "网关已经添加用户信息参数")
    @ApiImplicitParams({
    	@ApiImplicitParam(name = "userEntity", defaultValue = "{}", dataType = "String", paramType = "query")
    })
    @RequestMapping(value = "getUserInfo1", method = { RequestMethod.GET })
	public Object getUserInfo1(String userEntity) {
    	JSONObject jo = JSONObject.parseObject(userEntity);
    	System.out.println(jo.toJSONString());
		return jo;
	}

	

}

