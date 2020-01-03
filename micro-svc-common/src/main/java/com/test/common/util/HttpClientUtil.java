package com.test.common.util;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.test.common.constant.Constant;

/**
 * http|https请求工具类
 *
 * @author MC.Chen
 * @date 2017-08-04 11:11:59
 */
public class HttpClientUtil {

	private static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

	private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.66 Safari/537.36";
	private static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	;

	static {
		// 将最大连接数增加到200
		cm.setMaxTotal(200);
		// 将每个路由基础的连接增加到50
		cm.setDefaultMaxPerRoute(50);

		// 将目标主机的最大连接数增加到50
		// HttpHost localhost = new HttpHost("zx.ums86.com", 8893);
		// cm.setMaxPerRoute(new HttpRoute(localhost), 50);
	}

	public static CloseableHttpClient getHttpClient(boolean isSSL) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		RequestConfig config = RequestConfig.custom().setConnectTimeout(60000).setSocketTimeout(60000).build();
		if (isSSL) {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				// 信任所有
				@Override
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

			return HttpClientBuilder.create().setDefaultRequestConfig(config).setConnectionManager(cm).setSSLSocketFactory(sslsf).build();
		} else {
			return HttpClientBuilder.create().setDefaultRequestConfig(config).setConnectionManager(cm).build();
		}

	}

	public static void release() {
		if (cm != null) {
			cm.shutdown();
		}
	}

	/**
	 * 获取 get 请求 图片数据
	 *
	 * @param url
	 * @return
	 */
	public static byte[] doGet(String url) {
		return doGet(url, false);
	}

	/**
	 * 获取 get 请求 图片数据
	 *
	 * @param url
	 * @param isSSL 是否支付SSL协议
	 * @return
	 */
	public static byte[] doGet(String url, boolean isSSL) {
		if (url == null || "".equals(url)) {
			return null;
		}
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse response = getHttpClient(isSSL).execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();

				return null;
			}
			HttpEntity entity = response.getEntity();
			byte[] imgArray = null;
			if (entity != null) {
				imgArray = EntityUtils.toByteArray(entity);
			}
			EntityUtils.consume(entity);
			response.close();
			return imgArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String doGet(String url, Map<String, String> paramsMap, Map<String, String> headers) {
		return doGet(url, paramsMap, "UTF-8", false, headers);
	}

	public static String doGet(String url, Map<String, String> paramsMap) {
		return doGet(url, paramsMap, "UTF-8", false);
	}

	public static String doGet(String url, Map<String, String> paramsMap, String charset) {
		return doGet(url, paramsMap, charset, false);
	}

	/**
	 * 获取 get 请求 内容
	 *
	 * @param url     请求地址
	 * @param params  参数
	 * @param charset 编码格式
	 * @return 返回内容
	 */
	public static String doGet(String url, Map<String, String> paramsMap, String charset, boolean isSSL) {
		return doGet(url, paramsMap, charset, false, null);
	}

	public static String doGet(String url, Map<String, String> paramsMap, String charset, boolean isSSL, Map<String, String> headers) {
		if (url == null || "".equals(url)) {
			return null;
		}
		logger.info("request url --> {}", url);
		
		try {
			StringBuilder urlBuilder = new StringBuilder(200);
			urlBuilder.append(url);
			if (paramsMap != null && !paramsMap.isEmpty()) {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>(paramsMap.size());
				for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
					String value = entry.getValue();
					if (value != null) {
						pairs.add(new BasicNameValuePair(entry.getKey(), value));
					}
				}
				urlBuilder.append("?");
				urlBuilder.append(EntityUtils.toString(new UrlEncodedFormEntity(pairs, charset)));

			}
			HttpGet httpGet = new HttpGet(urlBuilder.toString());
			httpGet.setHeader("User-Agent", USER_AGENT);
			httpGet.setHeader("Referer", url);
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpGet.setHeader(entry.getKey(), entry.getValue());
				}
			}
			CloseableHttpResponse response = getHttpClient(isSSL).execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != 200) {
				httpGet.abort();
				return null;
			}
			HttpEntity entity = response.getEntity();
			String result = null;
			if (entity != null) {
				result = EntityUtils.toString(entity, charset);
			}
			
			logger.info("request result : {}", result);
			
			EntityUtils.consume(entity);
			response.close();
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * POST 请求
	 *
	 * @param url     请求地址
	 * @param content 请求内容[JSON]
	 * @return String
	 */
	public static String post(String url, String content) {
		return request(url, null, content);
	}

	/**
	 * POST 请求
	 *
	 * @param url 请求地址
	 * @param map 请求参数
	 * @return String
	 */
	public static String post(String url, Map<String, String> map, String content) {

		return post(url, map, content, null);
	}

	public static String post(String url, Map<String, String> map, String content, Map<String, String> headers) {

		List<NameValuePair> values = new ArrayList<NameValuePair>();
		if (null != map && !map.isEmpty()) {
			for (Entry<String, String> en : map.entrySet()) {
				values.add(new BasicNameValuePair(en.getKey(), en.getValue()));
			}
		}
		if (values.size() == 0) {
			values = null;
		}
		return request(url, values, content, headers);
	}

	private static String request(String url, List<NameValuePair> values, String content, Map<String, String> headers) {
		try {
			RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
			HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

			logger.info("request url --> {}", url);

			long beginTime = System.currentTimeMillis();

			HttpPost post = new HttpPost(url);

			if (null != values && values.size() > 0 ) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(values, Constant.UTF8);
				post.setEntity(entity);
			}
			if (StringUtils.isNotEmpty(content)) {
				logger.info("request content : {}", content);
				StringEntity entity = new StringEntity(content, Constant.UTF8);
				post.setEntity(entity);
				post.addHeader("Content-Type", "application/json;charset=UTF-8");
			}

			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					post.setHeader(entry.getKey(), entry.getValue());
				}
			}

			post.setConfig(config);
			HttpResponse response = httpClient.execute(post);

			HttpEntity httpEntity = response.getEntity();

			String result = EntityUtils.toString(httpEntity);

			logger.info("request result : {}", result);

			long endTime = System.currentTimeMillis();

			logger.info("response time --> {}ms", (endTime - beginTime));

			EntityUtils.consume(httpEntity);

			return result;
		} catch (Exception e) {
			logger.error("request error:", e);
		}

		return "";
	}

	/**
	 * POST请求
	 *
	 * @param url
	 * @param param
	 * @return
	 */
	private static String request(String url, List<NameValuePair> values, String content) {
		return request(url, values, null, null);
	}

	public static String put(String url, Map<String, String> map, Map<String, String> headers) {
		List<NameValuePair> values = new ArrayList<NameValuePair>();
		for (Entry<String, String> en : map.entrySet()) {
			values.add(new BasicNameValuePair(en.getKey(), en.getValue()));
		}
		return put(url, values, null, headers);
	}

	public static String put(String url, List<NameValuePair> values, String content, Map<String, String> headers) {
		try {
			RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
			HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();

			logger.info("request url --> {}", url);

			long beginTime = System.currentTimeMillis();

			HttpPut httpput = new HttpPut(url);

			if (null != values) {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(values, Constant.UTF8);
				httpput.setEntity(entity);
			} else if (StringUtils.isNotEmpty(content)) {
				StringEntity entity = new StringEntity(content, Constant.UTF8);
				httpput.setEntity(entity);
			}

			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					httpput.setHeader(entry.getKey(), entry.getValue());
				}
			}

			httpput.setConfig(config);
			HttpResponse response = httpClient.execute(httpput);

			HttpEntity httpEntity = response.getEntity();

			String result = EntityUtils.toString(httpEntity);

			logger.info("request result : {}", result);

			long endTime = System.currentTimeMillis();

			logger.info("response time --> {}ms", (endTime - beginTime));

			EntityUtils.consume(httpEntity);

			return result;
		} catch (Exception e) {
			logger.error("request error:", e);
		}

		return "";
	}




}
