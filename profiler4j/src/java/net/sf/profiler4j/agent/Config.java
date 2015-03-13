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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.profiler4j.agent.Agent.ReloadCallBack;
import net.sf.profiler4j.agent.Rule.Option;

/**
 * Agent configuration.
 * 
 * @author Antonio S. R. Gomes
 */
public class Config {

    private Map<Option, String> defaultRuleOptions = new HashMap<Option, String>();
    private Map<Option, String> lastDefaultRuleOptions = new HashMap<Option, String>();
    private List<Rule> rules;
    private List<Rule> lastRules;
    private boolean traceAllocations = false;
    private int verbosity = 1;
    private boolean enabled = true;
    private int port = 7890;
    private boolean exitVmOnFailure = true;
    private boolean waitConnection = true;
    private boolean saveSnapshotOnExit;
    private static File tempDir;
    private boolean dumpClasses = false;
    private String password = null;
    private File dumpDir;

    private int sessionVersion;
    
    // //////////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////////

    /**
     * @return Returns the sessionVersion.
     */
    public int getSessionVersion() {
        return this.sessionVersion;
    }
    
    public Config(String agentArgs) {
        defaultRuleOptions = Utils.parseOptions("-access:public -beanprops:on");
        try {
            File sysTmpDir = new File(System.getProperty("java.io.tmpdir"));
            File seed = File.createTempFile("profiler4j_", "", sysTmpDir);
            tempDir = new File(seed.getAbsolutePath() + ".tmp");
            tempDir.mkdir();
            seed.delete();
        } catch (IOException e) {
            throw new Profiler4JError("Could not create temporary dir", e);
        }
        dumpDir = new File(tempDir, "instrumented_classes");
        if (agentArgs == null) {
            return;
        }
        for (String arg : agentArgs.split(",")) {
            String key;
            String value = null;
            int p = arg.indexOf('=');
            if (p == -1) {
                key = arg;
            } else {
                key = arg.substring(0, p);
                if ((p + 1) < arg.length()) {
                    value = arg.substring(p + 1, arg.length());
                }
            }
            if ("waitconn".equals(key)) {
                waitConnection = Boolean.parseBoolean(value);
            } else if ("verbosity".equals(key)) {
                verbosity = Integer.parseInt(value);
            } else if ("port".equals(key)) {
                port = Integer.parseInt(value);
            } else if ("enabled".equals(key)) {
                enabled = Boolean.parseBoolean(value);
            } else if ("password".equals(key)) {
                password = value;
            } else {
                throw new Profiler4JError("Invalid agent option '" + key + "'");
            }
        }

    }

    // //////////////////////////////////////////////////////////////////////////
    // Public methods
    // //////////////////////////////////////////////////////////////////////////

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }
    
    /**
     * Defines the new set of instrumentation rules.
     * <p>
     * Notice that these rules will only take effect after the classes are redefined. For
     * the sake of consistency, this method should only be called by
     * 
     * @param optionsAsStr
     * @param rulesAsStr
     */

    public void parseRules(String optionsAsStr, String rulesAsStr) {
        
        lastDefaultRuleOptions = defaultRuleOptions;
        defaultRuleOptions = Utils.parseOptions(optionsAsStr);
        
        lastRules = rules;
        rules = Utils.parseRules(rulesAsStr);
        
        sessionVersion++;
    }
    
    /**
     * @return Returns the tempDir.
     */
    public static File getTempDir() {
        return tempDir;
    }

    public void requestTempDirCleanup() {
        requestDeleteOnExit(tempDir);
        tempDir.delete();
    }

    private void requestDeleteOnExit(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                requestDeleteOnExit(f);
            }
            f.delete();
        }
    }

    /**
     * @return Returns the defaultRuleOptions.
     */
    public Map<Option, String> getDefaultRuleOptions() {
        return this.defaultRuleOptions;
    }

    /**
     * @return Returns the dumpClasses.
     */
    public boolean isDumpClasses() {
        return this.dumpClasses;
    }

    /**
     * @return Returns the dumpDir.
     */
    public File getDumpDir() {
        return this.dumpDir;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * @return Returns the exitVmOnFailure.
     */
    public boolean isExitVmOnFailure() {
        return this.exitVmOnFailure;
    }

    /**
     * @return Returns the port.
     */
    public int getPort() {
        return this.port;
    }

    /**
     * @return Returns the rules.
     */
    public List<Rule> getRules() {
        return this.rules;
    }

    public List<Rule> getLastRules() {
        return this.lastRules;
    }

    /**
     * @return Returns the saveSnapshotOnExit.
     */
    public boolean isSaveSnapshotOnExit() {
        return this.saveSnapshotOnExit;
    }

    /**
     * @return Returns the traceAllocations.
     */
    public boolean isTraceAllocations() {
        return this.traceAllocations;
    }

    /**
     * @return Returns the verbosity.
     */
    public int getVerbosity() {
        return this.verbosity;
    }

    /**
     * @return Returns the waitConnection.
     */
    public boolean isWaitConnection() {
        return this.waitConnection;
    }

}
