package com.miz.utils.distribute.lock;


import com.miz.utils.distribute.lock.resource.LockSettings;
import com.miz.utils.distribute.lock.resource.Locker;
import com.miz.utils.distribute.lock.resource.SyncResource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;


/**
 * 分段锁
 */
public class SegmentationLock implements Lock {

    protected String segmentation;

    protected long maxLifecycle;

    private Locker selfLock;

    private SyncResource sync = LockSettings.getSyncResource();

    protected SegmentationLock(String segmentation) {
        this(segmentation, System.currentTimeMillis() + LockSettings.DEFAULT_LOCK_LIFECYCLE);
    }

    protected SegmentationLock(String segmentation, long maxLifecycle) {
        if (maxLifecycle <= System.currentTimeMillis()) {
            throw new IllegalArgumentException("max lifecycle must great than now");
        }
        this.segmentation = LockSettings.getLockPrefix() + "@" + segmentation;
        this.selfLock = new Locker(getLockOwner(), maxLifecycle);
        this.maxLifecycle = maxLifecycle;
    }

    public boolean tryLock() {
        Locker ownerLock = sync.get(segmentation);
        if (ownerLock == null) {
            return sync.compareAndSwap(segmentation, selfLock.activate());
        }

        // 额外校验机制，防止其他客户端没有释放锁，防止死锁机制
        // lock is timeout
        if (isTimeout(ownerLock)) {
            SegmentationLock timeoutLock = new SegmentationLock(LockSettings.TIMEOUT_LOCK_SEGMENTATIO + "@" + segmentation);
            try {
                // 加锁 防止多次释放
                if (timeoutLock.tryLock()) {
                    // release this lock
                    sync.delete(segmentation);
                    // tryLock again
                    return sync.compareAndSwap(segmentation, selfLock.activate());
                }
                return false;
            } finally {
                // release timeoutLock.lock
                timeoutLock.unlock();
            }
        }

        return false;
//        return reentryable(ownerLock, selfLock);
    }

    public boolean tryLock(long timeout) throws InterruptedException {
        long first = System.currentTimeMillis();
        if (timeout <= first) {
            throw new IllegalArgumentException("timeout must great than now");
        }

        for (; ; ) {
            if (!tryLock() && !Thread.interrupted()) {
                long last = System.currentTimeMillis();
                timeout -= last - first;
                if (timeout <= 0) {
                    return false;
                }
                continue;
            }

            return true;
        }
    }

    @Override
    public void lock() {
        for (; ; ) {
            if (tryLock() && !Thread.interrupted()) {
                break;
            }
        }
    }



    @Override
    public void unlock() {
        Locker ownerLock = sync.get(segmentation);
        if (ownerLock == null) return;
        // 删除前确认锁是自己持有的
        if (reentryable(ownerLock, selfLock)) {
            // 删除前确认锁没有过期
            if (!isTimeout(ownerLock)) {
                sync.delete(segmentation);
            }
        }
    }

    /**
     * 是否过期
     */
    private boolean isTimeout(Locker locker) {
        return System.currentTimeMillis() > locker.getMaxLifecycle();
    }

    /**
     * 是否可重入
     */
    private boolean reentryable(Locker ownerLock, Locker selfLock) {
        return ownerLock.getProcessId().equals(selfLock.getProcessId());
    }

    private String getLockOwner() {
        // 分布式应用其他节点上从Memcached同一key取出的对象为不同的对象，因此通过实例的hashcode可均分是否同一jvm所拥有
        return this.toString();
    }

    @Deprecated
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Deprecated
    @Override
    public Condition newCondition() {
        return null;
    }

    @Deprecated
    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

}
