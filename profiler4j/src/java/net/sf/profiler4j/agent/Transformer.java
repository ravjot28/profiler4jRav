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

import static net.sf.profiler4j.agent.Log.print;
import static net.sf.profiler4j.agent.ThreadProfiler.globalLock;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Class file transformer.
 * 
 * @author Antonio S. R. Gomes
 */
public class Transformer implements ClassFileTransformer {

    private Config config;

    public Transformer(Config config) {
        this.config = config;
    }

    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] currentBytes) throws IllegalClassFormatException {

        className = className.replace('/', '.');
        // !"java.lang.Object".equals(className) &&
        if (rejectByDefault(className)) {
            return null;
        }

        try {

            BytecodeTransformer bt = new BytecodeTransformer(className, loader,
                    currentBytes, config);

            byte[] newBytes = null;

            synchronized (globalLock) {
                newBytes = bt.transform(className);
            }

            if (newBytes != null) {
                return newBytes;
            }

        } catch (Throwable t) {
            print(0, "Could not transform class " + className, t);
            if (config.isExitVmOnFailure()) {
                System.exit(1);
            }
        }

        return null;
    }

    /**
     * Checks whether a class should be rejected by default. This is required to avoid
     * dependency problems and other kinds of situation in which the class should never be
     * redefined (such ash <code>org.apache.tomcat.jni.OS</code>).
     * 
     * @param className name of the class to check
     * @return <code>true</code> if the class should be rejected, <code>false</code>
     *         otherwise
     */
    public static boolean rejectByDefault(String className) {
        return className == null
                || !className.startsWith("net.sf.profiler4j.test.")
                && (className.startsWith("java.") || className.startsWith("javax.")
                        || className.startsWith("sun.") || className.startsWith("[")
                        || className.startsWith("com.sun.")
                        || className.startsWith("org.xml.")
                        || className.startsWith("org.w3c.")
                        || className.startsWith("org.jaxen.")
                        || className.startsWith("javassist.")
                        || className.startsWith("org.dom4j.")
                        || className.startsWith("org.apache.xerces.")
                        || className.startsWith("org.apache.xalan.")
                        || className.startsWith("org.apache.xpath.")
                        || className.startsWith("org.apache.xml.")
                        || className.startsWith("org.apache.tomcat.jni.")
                        || className.startsWith("net.sf.profiler4j.")
                        || className.startsWith("$Proxy") || className
                    .contains("ByCGLIB$$"));
    }

}
