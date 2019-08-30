package com.miz.utils.distribute.lock.resource;

public class LockSettings {

	private static SyncResource syncResource;
	
	private static String lockPrefix;

	public static final String TIMEOUT_LOCK_SEGMENTATIO = "TIMEOUT_LOCK_SEGMENTATIO";
	
	public static final long DEFAULT_LOCK_LIFECYCLE = 30 * 1000;

	public static SyncResource getSyncResource() {
		return syncResource;
	}

	public static void setSyncResource(SyncResource syncResource) {
		LockSettings.syncResource = syncResource;
	}

	public static String getLockPrefix() {
		return lockPrefix;
	}

	public static void setLockPrefix(String lockPrefix) {
		LockSettings.lockPrefix = lockPrefix;
	}

}
