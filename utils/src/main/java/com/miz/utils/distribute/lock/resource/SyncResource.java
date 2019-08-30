package com.miz.utils.distribute.lock.resource;

public interface SyncResource {

	public boolean compareAndSwap(String key, Locker value);
	
	public Locker get(String key);
	
	public boolean delete(String key);
}
