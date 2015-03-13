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
package net.sf.profiler4j.console;

/**
 * Enumeration for application events
 * 
 * @author Antonio S. R. Gomes
 */
public enum AppEventType {
    /**
     * A remote connection has been stablished.
     */
    CONNECTED(false),
    /**
     * A remote connection has been closed.
     */
    DISCONNECTED(false),
    /**
     * A remote connection will be closed.
     */
    TO_DISCONNECT(false),
    /**
     * Application is trying to exit
     */
    APP_CLOSING(true),
    /**
     * Application is committed to exit
     */
    APP_CLOSED(false),
    /**
     * Memory info updated (this happens whenever the client is connected).
     */
    GOT_MEMORY_INFO(false),
    /**
     * A new snapshot was set active (passed as the argument of the event).
     */
    SNAPSHOT(false),
    /**
     * A new set of profiling rules has been successfully applied in the remote JVM.
     */
    RULES_APPLIED(false);

    private boolean vetoable;

    private AppEventType(boolean vetoable) {
        this.vetoable = vetoable;
    }
    /**
     * @return Returns the vetoable.
     */
    public boolean isVetoable() {
        return this.vetoable;
    }
}
