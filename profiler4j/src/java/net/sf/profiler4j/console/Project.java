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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO that represents a profiler project.
 * 
 * @author Antonio S. R. Gomes
 */
public class Project {

    private boolean changed;
    
    private File file;
    private String name = "Sample Profiling Project";
    private String hostname = "localhost";
    private int port = 7890;
    private boolean beanprops = true;
    private Rule.AccessOption access = Rule.AccessOption.PRIVATE;

    private List<Rule> rules = new ArrayList<Rule>();

    public Project() {
        rules.add(new Rule("org.apache.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("org.jboss.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("net.sf.jasperreports.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("bsh.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("EDU.oswego.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("org.eclipse.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("org.hsqldb.*(*)", Rule.Action.REJECT));
        rules.add(new Rule("*(*)", Rule.Action.ACCEPT));
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        changed = true;
        this.hostname = hostname;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        changed = true;
        this.port = port;
    }

    public Rule.AccessOption getAccess() {
        return this.access;
    }
    public void setAccess(Rule.AccessOption access) {
        changed = true;
        this.access = access;
    }
    public boolean isBeanprops() {
        return this.beanprops;
    }
    public void setBeanprops(boolean beanprops) {
        this.beanprops = beanprops;
    }
    public List<Rule> getRules() {
        return this.rules;
    }
    public void setRules(List<Rule> rules) {
        changed = true;
        this.rules = rules;
    }
    public File getFile() {
        return this.file;
    }
    public void setFile(File file) {
        changed = true;
        this.file = file;
    }

    /**
     * @return Returns the changed.
     */
    public boolean isChanged() {
        return this.changed;
    }
    
    public void clearChanged() {
        changed = false;
    }
}
