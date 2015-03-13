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
package net.sf.profiler4j.test;

/**
 * Simple test class used for experimenting purposes only. Ignore it.
 * 
 * @author Antonio S. R. Gomes
 */
public class TestSampler2 {

    public static void main(String[] args) {
        while (true) {
            iter_1();
            sleep(100);
        }
    }

    static void iter_1() {
        iter_2();
    }

    static void iter_2() {
        iter_3();
    }

    static void iter_3() {
        iter_4();
    }

    static void iter_4() {
        // empty
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
