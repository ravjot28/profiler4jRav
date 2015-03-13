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

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class that represents a processing rule.
 * 
 * @author Antonio S. R. Gomes
 */
public class Rule {

    public enum Action {
        /**
         * Action that tells the bytecode transformer to instrument methods that match the
         * string pattern specified by the rule. In this case, flags will be used to
         * provide more details about the instrumentation to be performed.
         */
        ACCEPT,
        /**
         * Action that tells the transformer to simply skip any matching methods.
         */
        REJECT
    }

    public enum Option {
        /**
         * Option that tells the instrumenter which access modifier should be supported.
         * Supported values:
         * <ul>
         * <li><code>private</code> (default): all methods
         * <li><code>package</code>: only package, protected and public methods
         * <li><code>protected</code>: only protected and public
         * <li><code>public</code>: only public methods
         * </ul>
         */
        ACCESS,
        /**
         * Option that tells the instrumenter to skip methods that follow the JavaBean
         * pattern. Usually this methods do nothing more than read/write local fields.
         * This may increase performance significantly.
         * <ul>
         * <li><code>on</code> (default): instrument property acessors
         * <li><code>off</code>: skip property acessors client)
         * </ul>
         */
        BEANPROPS
    }

    private String pattern;
    private Action action;
    private Map<Option, String> options;

    public Rule(String pattern, Action action, Map<Option, String> options) {
        this.pattern = pattern;
        this.action = action;
        this.options = options;
    }

    public Action getAction() {
        return action;
    }

    /**
     * @return Returns the pattern.
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * Determines whether a boolean option is set.
     * <p>
     * If the option is not set in the current rule then it assumes automatically its
     * default value.
     * 
     * @param option
     * @return status
     * @throws Profiler4JError If a default value is not defined
     */
    public boolean isBooleanOptionSet(Option option, Config c) {
        String v = options.get(option);
        if (v == null) {
            v = c.getDefaultRuleOptions().get(option);
            if (v == null) {
                throw new Profiler4JError("[INTERNAL] No default set for boolean option "
                        + option);
            }
        }
        return Utils.parseBoolean(v);
    }

    /**
     * Gets the value of an option.
     * <p>
     * If the option is not set in the current rule then it assumes automatically its
     * default value.
     * 
     * @param option
     * @return value
     * @throws Profiler4JError If a default value is not defined
     */
    public String getOption(Option option, Config c) {
        String v = options.get(option);
        if (v == null) {
            v = c.getDefaultRuleOptions().get(option);
            if (v == null) {
                throw new Profiler4JError("[INTERNAL] No default set for option "
                        + option);
            }
        }
        return v;
    }

    /**
     * Tests whether a fully qualified method name is compatible with this rule.
     * @param methodFqn
     * @return status
     */
    public boolean matches(String methodFqn) {
        return Utils.getRegex(pattern).matcher(methodFqn).matches();
    }
    @Override
    public String toString() {
        return "Rule[pattern=" + pattern + ", action=" + action + "]";
    }
}
