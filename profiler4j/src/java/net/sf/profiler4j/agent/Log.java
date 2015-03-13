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

import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {

    private static final String PREFFIX = "[PROFILER4J";
    private static int verbosity = 0;

    public static void setVerbosity(int verbosity) {
        Log.verbosity = verbosity;
    }

    public static void print(int verbosity, Object s) {
        if (verbosity <= Log.verbosity) {
            System.out.println(PREFFIX + ":" + verbosity + "] " + s);
        }
    }
    public static void print(int verbosity, Object s, Throwable t) {
        if (verbosity <= Log.verbosity) {
            System.out.println(PREFFIX + ":" + verbosity + "] " + s);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            for (String l : sw.toString().split("\n+")) {
                System.out.print(PREFFIX + ":" + verbosity + "] " + l);
            }

        }
    }
}
