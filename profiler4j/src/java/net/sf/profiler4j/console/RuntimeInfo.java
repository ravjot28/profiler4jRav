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
import java.util.List;
import java.util.Map;

import net.sf.profiler4j.agent.Server;

/**
 * CLASS_COMMENT
 * 
 * @author Antonio S. R. Gomes
 */
public class RuntimeInfo {

    private String bootClassPath;
    private String classPath;
    private List<String> inputArguments;
    private String libraryPath;
    private String name;
    private String vmName;
    private long startTime;
    private long upTime;
    private Map<String, String> systemProperties;

    public String getBootClassPath() {
        return this.bootClassPath;
    }

    public String getClassPath() {
        return this.classPath;
    }

    public List<String> getInputArguments() {
        return this.inputArguments;
    }

    public String getLibraryPath() {
        return this.libraryPath;
    }

    public String getName() {
        return this.name;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public Map<String, String> getSystemProperties() {
        return this.systemProperties;
    }

    public long getUpTime() {
        return this.upTime;
    }

    public String getVmName() {
        return this.vmName;
    }

    public static RuntimeInfo read(ObjectInputStream in) throws IOException {
        RuntimeInfo ri = new RuntimeInfo();
        ri.bootClassPath = in.readUTF();
        ri.classPath = in.readUTF();
        ri.inputArguments = Server.readStringList(in);
        ri.libraryPath = in.readUTF();
        ri.name = in.readUTF();
        ri.vmName = in.readUTF();
        ri.startTime = in.readLong();
        ri.upTime = in.readLong();
        ri.systemProperties = Server.readStringMap(in);
        return ri;
    }
}
