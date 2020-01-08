package com.test.shiro;

import java.util.Set;

import org.crazycake.shiro.RedisManager;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;


/**
 * 继承shiro默认的RedisManager
 * 添加database配置，用于指定redis的db，防止默认db冲突
 * @author yuxue
 * @date 2019-01-08
 */
public class DefaultRedisManager extends RedisManager{

	private String host = "127.0.0.1";

	private int port = 6379;

	/**
	 *  0 - never expire
	 */
	private int expire = 0;

	/**
	 * timeout for jedis try to connect to redis server, not expire time! In
	 *  milliseconds
	 */
	private int timeout = 0;

	private String password = "";

	private int database = 0;

	private static JedisPool jedisPool = null;

	public DefaultRedisManager() {

	}

	/**
	 * 初始化方法
	 */
	@Override
	public void init() {
		if (jedisPool == null) {
			if (password != null && !"".equals(password)) {
				jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout, password, database);
			} else if (timeout != 0) {
				jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
			} else {
				jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
			}

		}
	}

	/**
	 * get value from redis
	 * @param key
	 * @return
	 */
	@Override
	public byte[] get(byte[] key) {
		byte[] value = null;
		Jedis jedis = jedisPool.getResource();
		try {
			value = jedis.get(key);
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	/**
	 * set 
	 * @param key
	 * @param value
	 * @return
	 */
	@Override
	public byte[] set(byte[] key, byte[] value) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set(key, value);
			if (this.expire != 0) {
				jedis.expire(key, this.expire);
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	/**
	 * set 
	 * @param key
	 * @param value
	 * @param expire
	 * @return
	 */
	@Override
	public byte[] set(byte[] key, byte[] value, int expire) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.set(key, value);
			if (expire != 0) {
				jedis.expire(key, expire);
			}
		} finally {
			jedisPool.returnResource(jedis);
		}
		return value;
	}

	/**
	 * del
	 * @param key
	 */
	@Override
	public void del(byte[] key) {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.del(key);
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * flush
	 */
	@Override
	public void flushDB() {
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.flushDB();
		} finally {
			jedisPool.returnResource(jedis);
		}
	}

	/**
	 * size
	 */
	@Override
	public Long dbSize() {
		Long dbSize = 0L;
		Jedis jedis = jedisPool.getResource();
		try {
			dbSize = jedis.dbSize();
		} finally {
			jedisPool.returnResource(jedis);
		}
		return dbSize;
	}

	/**
	 * keys
	 * @param regex
	 * @return
	 */
	@Override
	public Set<byte[]> keys(String pattern) {
		Set<byte[]> keys = null;
		Jedis jedis = jedisPool.getResource();
		try {
			keys = jedis.keys(pattern.getBytes());
		} finally {
			jedisPool.returnResource(jedis);
		}
		return keys;
	}

	@Override
	public String getHost() {
		return host;
	}

	@Override
	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public int getPort() {
		return port;
	}

	@Override
	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public int getExpire() {
		return expire;
	}

	@Override
	public void setExpire(int expire) {
		this.expire = expire;
	}

	@Override
	public int getTimeout() {
		return timeout;
	}

	@Override
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	public int getDatabase() {
		return database;
	}

	public void setDatabase(int database) {
		this.database = database;
	}

}