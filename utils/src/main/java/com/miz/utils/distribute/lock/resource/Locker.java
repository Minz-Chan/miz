package com.miz.utils.distribute.lock.resource;

import java.io.Serializable;

public class Locker implements Serializable {

	private String processId;
	
	private Long maxLifecycle;
	
	public Locker() {

	}

	public Locker(String processId, Long maxLifecycle) {
		super();
		this.processId = processId;
		this.maxLifecycle = maxLifecycle;
	}

	public String getProcessId() {
		return processId;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public Long getMaxLifecycle() {
		return maxLifecycle;
	}
	
	public void setMaxLifecycle(Long maxLifecycle) {
		this.maxLifecycle = maxLifecycle;
	}

	public Locker activate() {
		this.maxLifecycle = System.currentTimeMillis() + 20*1000; // 超时释放，避免死锁(NOTICE:此处要求各节点时间基本保持一致)
		return this;
	}
}
