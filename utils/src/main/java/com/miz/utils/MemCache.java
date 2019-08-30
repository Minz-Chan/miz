package com.miz.utils;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.google.common.util.concurrent.RateLimiter;
import com.miz.utils.distribute.lock.resource.LockSettings;
import com.miz.utils.distribute.lock.resource.MemcachedSyncResource;
import com.miz.utils.jfinal.P;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.Map;

/**
 * 需要手动下载java_memcached-release_2.6.6.jar导入maven
 *
 * mvn install:install-file -Dfile=java_memcached-release_2.6.6.jar -DgroupId=com.danga -DartifactId=java-memcached  -Dversion=2.6.6 -Dpackaging=jar
 *
 * <dependency>
 *     <groupId>com.danga</groupId>
 *     <artifactId>java-memcached</artifactId>
 *     <version>2.6.6</version>
 * </dependency>
 */
public class MemCache {
	
	private static MemCachedClient mcc;

	public static RateLimiter rateLimiter = RateLimiter.create(1000.0);

	private static String cacheNode = null;
	private static String lockPrefix = null;

	static {
		P.use("application.properties", "utf-8");
		cacheNode = P.get("cacheNode");
		lockPrefix = P.get("prefix");

		// 缓存节点，未设置时默认使用本机节点
		String[] server;
		if (!StringUtils.isEmpty(cacheNode)) {
			server = cacheNode.split(",");
		} else {
			server = new String[]{"127.0.0.1:11211"};
		}

		SockIOPool pool = SockIOPool.getInstance();
		pool.setServers(server);
		pool.setInitConn(5);
		pool.setMinConn(5);
		pool.setMaxIdle(21600000);
		pool.setMaxConn(100);
		pool.setMaintSleep(30);
		pool.setNagle(false);
		pool.setSocketTO(1000);
		pool.setSocketConnectTO(0);
		pool.setFailback(true); // 设置故障恢复，避免重启Memcached时后需要重启tomcat
		pool.initialize();
		mcc = new MemCachedClient();

		MemcachedSyncResource msr = new MemcachedSyncResource();
		msr.setMemcachedClient(mcc);
		LockSettings.setSyncResource(msr);
		LockSettings.setLockPrefix(lockPrefix);
	}

	public static String key(String k) {
		// 用于区分不同的项目，支持在一台服务器上运行多个实例且多个实例共用一个memcached
		return lockPrefix + k;
	}

	public static boolean keyExists(String key) {
		return mcc.keyExists(key(key));
	}

	public static Object get(String key) {
		rateLimiter.acquire();
		return mcc.get(key(key));
	}

	public static Map<String, Object> getMap(String[] keys) {
		rateLimiter.acquire();
		for(int i=0; i<keys.length; i++) {
			keys[i] = key(keys[i]);
		}
		return mcc.getMulti(keys);
	}

	public static Object[] get(String[] keys) {
		rateLimiter.acquire();
		for(int i=0; i<keys.length; i++) {
			keys[i] = key(keys[i]);
		}
		return mcc.getMultiArray(keys);
	}

	public static boolean set(String key, Object value) {
		rateLimiter.acquire();
		key = key(key);
		if(mcc.keyExists(key)) {
			return mcc.replace(key, value);
		}
		return mcc.add(key, value);
	}

	public static boolean set(String key, Object value, Date exp) {
		rateLimiter.acquire();
		key = key(key);
		if(mcc.keyExists(key))
			return mcc.replace(key, value, exp);
		return mcc.add(key, value, exp);
	}

	/**
	 * 提供分布式锁实现
	 */
	public static boolean _set(String key, Object value, Date exp) {
		rateLimiter.acquire();
		key = key(key);
		return mcc.add(key, value, exp);
	}

	/**
	 * 提供分布式锁实现（全局key，与CommonConfig.PROJECT_ID无关）
	 */
	public static boolean _set2(String key, Object value, Date exp) {
		rateLimiter.acquire();
		return mcc.add(key, value, exp);
	}
	
	public static boolean replace(String key, Object value, long exp) {
		rateLimiter.acquire();
		exp -= System.currentTimeMillis();
		return exp >= 1000 && mcc.replace(key(key), value, new Date(exp));
	}

	public static boolean replace(String key, Object value) {
		rateLimiter.acquire();
		return mcc.replace(key(key), value);
	}

	public static boolean remove(String key) {
		rateLimiter.acquire();
		return mcc.delete(key(key));
	}
	public static boolean _remove2(String key) {
		rateLimiter.acquire();
		return mcc.delete(key);
	}

	public static boolean removeAll() {
		return mcc.flushAll();
	}

	public static boolean storeCounter(String key, long value) {
		rateLimiter.acquire();
		key = key(key);
		return mcc.storeCounter(key, value);
	}

	public static long getCounter(String key) {
		rateLimiter.acquire();
		key = key(key);
		return mcc.getCounter(key);
	}

	public static long decr(String key, long value) {
		rateLimiter.acquire();
		key = key(key);
		return mcc.decr(key, value);
	}

}
