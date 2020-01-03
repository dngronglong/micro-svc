package com.test.auth.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * t_base_resources
 * @author 
 */
@Data
@NoArgsConstructor
public class ComResourcesEntity implements Serializable {
    private Integer id;
    
    
    /**
     * 子系统模块
     * 对应微服务的服务名称，全小写
     */
    private String module;

    /**
     * 包名.类名
     */
    private String className;

    /**
     * 类名
     */
    private String simpleName;

    /**
     * 方法名称
     */
    private String methodName;

    /**
     * 资源权限控制
     */
    private String permissions;

    /**
     * 请求类型 get post等
     */
    private String requestType;

    /**
     * 请求路径
     */
    private String requestUrl;

    /**
     * 接口描述
     */
    private String apiDesc;

    /**
     * 接口备注
     */
    private String apiRemark;
    
    /**
     * 该请求是否校验登录 0校验 1不校验
     */
    private Short loginCheck;
    
    /**
     * 该请求是否校验权限 0校验 1不校验
     */
    private Short permissionCheck;

    /**
     * 记录创建时间
     */
    private Date createTime;

    /**
     * 最后修改时间
     */
    private Date editTime;

    /**
     * 删除标记位 0未删除 1删除
     */
    private Short delFlag;

    private static final long serialVersionUID = 1L;

}