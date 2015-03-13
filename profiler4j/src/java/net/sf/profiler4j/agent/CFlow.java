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

/**
 * Thread-local class that contains the recursive depth of a given method.
 * 
 * @author Antonio S. R. Gomes
 */
public class CFlow extends ThreadLocal<CFlow.ThreadLocalMethod> {

    @Override
    protected synchronized ThreadLocalMethod initialValue() {
        return new ThreadLocalMethod();
    }

    /**
     * Holds the recursive depth.
     */
    public static class ThreadLocalMethod {
        private int depth;
        /**
         * Gets the current depth.
         * @return depth
         */
        public int getDepth() {
            return depth;
        }
        public boolean isRecursive() {
            return depth > 1;
        }
        /**
         * Enter the methdod.
         * @return new depth
         */
        public int enter() {
            return ++depth;
        }
        /**
         * Leaver the method
         * @return new depth
         */
        public int leave() {
            return --depth;
        }
    }
}
