package com.test.common.model;

import java.util.HashMap;

import com.test.common.enumtype.FwWebError;

/**
 * 返回值封装模型类
 * @author yuxue
 * @date 2018-09-07
 */
public class Result extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    private static final Integer SUCCESS_CODE = 200;
    private static final String SUCCESS_INFO = "Success!";

    public Result() {
        put("code", SUCCESS_CODE);
        put("msg", SUCCESS_INFO);
        put("success", true);
    }

    public Result(Object obj) {
        put("code", SUCCESS_CODE);
        put("msg", SUCCESS_INFO);
        put("obj", obj);
        put("success", true);
    }

    public static Result ok() {
        return new Result();
    }

    public static Result ok(Object obj) {
        return new Result(obj);
    }

    public static Result error() {
        return error(FwWebError.COMMON_ERROR);
    }

    public static Result error(String msg) {
        Result result = error(FwWebError.COMMON_ERROR);
        result.put("msg", msg);
        return result;
    }

    public static Result error(String msg, int code) {
        Result result = error(FwWebError.COMMON_ERROR);
        result.put("msg", msg);
        result.put("code", code);
        return result;
    }

    public static Result error(FwWebError fwWebError) {
        Result result = new Result();
        result.put("code", fwWebError.code);
        result.put("msg", fwWebError.msg);
        result.put("success", false);
        return result;
    }

    public static Result error(int code, String msg) {
        Result result = new Result();
        result.put("code", code);
        result.put("msg", msg);
        result.put("success", false);
        return result;
    }


    @Override
    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
