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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.management.MemoryUsage;

import net.sf.profiler4j.agent.Server;

/**
 * Each instance of <code>MemoryInfo</code> represents a snapshot of the current memory
 * usage, including heap and non-heap.
 *
 * @author Antonio S. R. Gomes
 */
public class MemoryInfo implements Serializable {

    private MemoryUsage heapUsage;
    private MemoryUsage nonHeapUsage;
    private int objectPendingFinalizationCount;

    public MemoryUsage getHeapUsage() {
        return this.heapUsage;
    }
    public void setHeapUsage(MemoryUsage heapUsage) {
        this.heapUsage = heapUsage;
    }
    public MemoryUsage getNonHeapUsage() {
        return this.nonHeapUsage;
    }
    public int getObjectPendingFinalizationCount() {
        return this.objectPendingFinalizationCount;
    }

    public static MemoryInfo read(ObjectInputStream in) throws IOException {
        MemoryInfo mi = new MemoryInfo();
        mi.heapUsage = Server.readMemoryUsage(in);
        mi.nonHeapUsage = Server.readMemoryUsage(in);
        mi.objectPendingFinalizationCount = in.readInt();
        return mi;
    }
}
