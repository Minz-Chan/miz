package com.miz.utils.distribute.lock.resource;


import com.danga.MemCached.MemCachedClient;
import vip.xini.v2.server.util.MemCache;

public class MemcachedSyncResource implements SyncResource{

	private MemCachedClient memcachedClient;
	
	public boolean compareAndSwap(String key, Locker value) {
		MemCache.rateLimiter.acquire();
		try {
			return memcachedClient.add(MemCache.key(key), value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public Locker get(String key) {
		MemCache.rateLimiter.acquire();
		try {
			Object value = memcachedClient.get(MemCache.key(key));
			if(value == null) return null;
			if(value instanceof Locker) {
				return (Locker) value;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean delete(String key) {
		MemCache.rateLimiter.acquire();
		try {
			return memcachedClient.delete(MemCache.key(key));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setMemcachedClient(MemCachedClient memcachedClient) {
		this.memcachedClient = memcachedClient;
	}
	
	public MemCachedClient getMemcachedClient() {
		return memcachedClient;
	}
}
