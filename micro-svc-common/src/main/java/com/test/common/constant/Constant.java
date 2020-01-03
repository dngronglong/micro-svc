package com.test.common.constant;

/**
 * 系统常量
 * @author yuxue
 * @date 2018-09-07
 */
public class Constant {

	public static final String UTF8 = "UTF-8";

	public static final String JSESSIONID = "JSESSIONID";
	
	public static final String TOKEN = "token";

	public static final String SSO_USER = "fw_sso_user";
	
    /**
	 * 最大允许上传的图片大小 2MB
	 */
	public static long MAX_IMAGE_UPLOAD_SIZE = 1024 * 1024 * 5;
	
	/**
	 * 最大允许上传的视频大小 50MB
	 */
	public static long MAX_VIDEO_UPLOAD_SIZE = 1024 * 1024 * 50;

	
	public static final String DEFAULT_PASSWORD = "111111";
	
	public static String PARAM_KEY = "fw.system.config";
	
	public static final String REDIRECT_URL = "redirect_url";
	
	public static String WECHAT_REQ_URL = "https://api.weixin.qq.com/sns/jscode2session";
	
	public static final String SYSTEM_APP_KEY = "system.app.entity";
	

}
