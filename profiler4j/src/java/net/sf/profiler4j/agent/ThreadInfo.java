/*
 * Copyright 2006 Antonio S. R. Gomes
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package net.sf.profiler4j.agent;

import java.io.Serializable;

/**
 * Serializable class equivalent to {@link java.lang.management.ThreadInfo}, which is
 * declated non-serializable.
 * 
 * @author Antonio S. R. Gomes
 */
public class ThreadInfo implements Serializable {

    public ThreadInfo() {
        // default constructor
    }
    
    public ThreadInfo(java.lang.management.ThreadInfo mti) {
        threadName = mti.getThreadName();
        threadId = mti.getThreadId();
        blockedTime = mti.getBlockedTime();
        blockedCount = mti.getBlockedCount();
        waitedTime = mti.getWaitedTime();
        waitedCount = mti.getWaitedCount();
        lockName = mti.getLockName();
        lockOwnerId = mti.getLockOwnerId();
        lockOwnerName = mti.getLockOwnerName();
        inNative = mti.isInNative();
        suspended = mti.isSuspended();
        threadState = mti.getThreadState();
        stackTrace = mti.getStackTrace();
    }

    private static final long serialVersionUID = 1L;

    private String threadName;
    private long threadId;
    private long blockedTime;
    private long blockedCount;
    private long waitedTime;
    private long waitedCount;
    private String lockName;
    private long lockOwnerId;
    private String lockOwnerName;
    private boolean inNative;
    private boolean suspended;
    private Thread.State threadState;
    private StackTraceElement[] stackTrace;

    public long getBlockedCount() {
        return this.blockedCount;
    }
    public void setBlockedCount(long blockedCount) {
        this.blockedCount = blockedCount;
    }
    public long getBlockedTime() {
        return this.blockedTime;
    }
    public void setBlockedTime(long blockedTime) {
        this.blockedTime = blockedTime;
    }
    public boolean isInNative() {
        return this.inNative;
    }
    public void setInNative(boolean inNative) {
        this.inNative = inNative;
    }
    public String getLockName() {
        return this.lockName;
    }
    public void setLockName(String lockName) {
        this.lockName = lockName;
    }
    public long getLockOwnerId() {
        return this.lockOwnerId;
    }
    public void setLockOwnerId(long lockOwnerId) {
        this.lockOwnerId = lockOwnerId;
    }
    public String getLockOwnerName() {
        return this.lockOwnerName;
    }
    public void setLockOwnerName(String lockOwnerName) {
        this.lockOwnerName = lockOwnerName;
    }
    public StackTraceElement[] getStackTrace() {
        return this.stackTrace;
    }
    public void setStackTrace(StackTraceElement[] stackTrace) {
        this.stackTrace = stackTrace;
    }
    public boolean isSuspended() {
        return this.suspended;
    }
    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }
    public long getThreadId() {
        return this.threadId;
    }
    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }
    public String getThreadName() {
        return this.threadName;
    }
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }
    public Thread.State getThreadState() {
        return this.threadState;
    }
    public void setThreadState(Thread.State threadState) {
        this.threadState = threadState;
    }
    public long getWaitedCount() {
        return this.waitedCount;
    }
    public void setWaitedCount(long waitedCount) {
        this.waitedCount = waitedCount;
    }
    public long getWaitedTime() {
        return this.waitedTime;
    }
    public void setWaitedTime(long waitedTime) {
        this.waitedTime = waitedTime;
    }

}
