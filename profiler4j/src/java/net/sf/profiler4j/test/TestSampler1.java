/*
 * Copyright 2006 Antonio S. R. Gomes
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package net.sf.profiler4j.test;

import java.util.Locale;

/**
 * Simple test class used for experimenting purposes only. Ignore it.
 * 
 * @author Antonio S. R. Gomes
 */
public class TestSampler1 {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        for (int i = 0; i < 1; i++) {
            Thread t = new Thread() {
                public void run() {
                    TestSampler1 t = new TestSampler1();
                    while (true) {
                        // System.out.println("_----------------------");
                        _iter(t);
                        // for (int i = 0; i < 2; i++) {
                        TestSampler1.sleep(10000000);
                        // }
                    }
                }
            };
            t.start();
            // System.out.println("Started thread #" + i);
        }

    }

    private static void _iter(TestSampler1 t) {
        // t.foo_N1000_S500();
        // t.foo_N10_S10();
        // t.foo_N50_S50();
        // t.foo_N250_S250();
        // t.foo_N150_S50();
        // t.foo_N100_S50();
        // t.foo_N200_S0();
        // t.foo_N200_S50();
        t.foo_1000_A();
        //t.foo_1000_B();
        //t.foo_1000_C();
        t.recursive(3);
    }

    private void foo_1000_A() {
        sleep(1000);
    }

    private void foo_1000_B() {
        sleep(1000);
    }

    private void foo_1000_C() {
        sleep(1000);
    }
    
    public void foo_N10_S10() {
        sleep(10);
    }

    public void foo_N50_S50() {
        sleep(50);
    }

    public void foo_N1000_S500() {
        sleep(500);
        foo_N500_S500();
    }

    public void foo_N500_S500() {
        sleep(500);
    }

    public void foo_N250_S250() {
        sleep(250);
    }

    public void foo_N150_S50() {
        foo_N50_S50();
        foo_N50_S50();
        sleep(50);
    }

    public void foo_N200_S50() {
        foo_N150_S50();
        sleep(50);
    }

    public void foo_N100_S50() {
        foo_N50_S50();
        sleep(50);
    }

    public void foo_N200_S0() {
        foo_N100_S50();
        foo_N100_S50();
    }

    public void recursive(int n) {
        sleep(1000);
        n--;
        if (n > 0) {
            _recursive(n);
        }
    }

    public void _recursive(int n) {
        recursive(n);
    }
    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            // ignore
        }
    }
}
