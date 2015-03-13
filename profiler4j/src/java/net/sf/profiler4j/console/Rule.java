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
 * CLASS_COMMENT
 * 
 * @author Antonio S. R. Gomes
 */
public class Rule {

    public enum AccessOption {
        PRIVATE, PACKAGE, PROTECTED, PUBLIC
    }

    public enum Action {
        ACCEPT, REJECT
    }

    private String pattern;
    private Action action;

    public Rule(String pattern, Action action) {
        this.pattern = pattern;
        this.action = action;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

}
