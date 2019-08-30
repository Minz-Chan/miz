package com.miz.utils.distribute.lock;


import vip.xini.v2.server.util.lock.resource.LockSettings;

/**
 * 分布式锁
 * NOTICE:
 *   不适用大量争夺单个锁资源的场景，如抢红包
 *   否则将导致瞬间大量MemCache I/O峰值产生
 */
public class DistributedLock extends SegmentationLock {

	public DistributedLock() {
		super(null);
	}

	public DistributedLock(String segmentation) {
		super(segmentation);
	}

	public String getCodeLine(StackTraceElement stack[]) {
		StackTraceElement parentStack = stack[1];
        return LockSettings.getLockPrefix()
        		+ "@" + "codeLock"
        		+ "@" + parentStack.getClassName() 
        		+ "@" + parentStack.getMethodName()
        		+ "@" + parentStack.getLineNumber();  
        
	}
}
