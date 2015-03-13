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

import static java.lang.String.format;
import static net.sf.profiler4j.agent.Log.print;
import static net.sf.profiler4j.agent.ThreadProfiler.globalLock;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;

/**
 * Class responsible for the intialization of the agent.
 * 
 * @author Antonio S. R. Gomes
 */
public class Agent {

    public static final String VERSION = "1.0-beta2 (build 27)";
    private static Transformer t;
    public static Instrumentation inst;

    public static final Object waitConnectionLock = new Object();
    public static Server server;
    private static Config config;

    public static RuntimeMXBean rtbean = ManagementFactory.getRuntimeMXBean();
    public static List<GarbageCollectorMXBean> gcbeans = ManagementFactory
        .getGarbageCollectorMXBeans();
    public static MemoryMXBean membean = ManagementFactory.getMemoryMXBean();
    public static ThreadMXBean threadbean = ManagementFactory.getThreadMXBean();

    /**
     * Set with the name of all currently transformed classes. Notice that his set should
     * be used only for informative purposes as it may be imprecise.
     * <p>
     * It is interesting to see that the effective number of modified classes may be
     * greater than <tt>modifiedClasses.size()</tt>. This happens when more than one
     * class loader loads a class with same name.
     */
    public static final Set<String> modifiedClassNames = new HashSet<String>();
    public static int modifiedClassCount;

    /**
     * Premain method called before the target application is initialized.
     * 
     * @param args command line argument passed via <code>-javaagent</code>
     * @param inst instance of the instrumentation service
     */
    public static void premain(String args, Instrumentation inst) {
        ClassPool.doPruning = false;
        try {
            config = new Config(args);
            if (config.isEnabled()) {

                Log.setVerbosity(config.getVerbosity());

                print(0, "+---------------------------------------+");
                print(0, "| Profiler4j " + String.format("%-27s", VERSION) + "|");
                print(0, "| Copyright 2006 Antonio S. R. Gomes    |");
                print(0, "| See LICENSE-2.0.txt for more details  |");
                print(0, "+---------------------------------------+");

                server = new Server(config);
                server.start();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // ignore
                }

                t = new Transformer(config);

                BytecodeTransformer.enabled = true;
                inst.addTransformer(t);

                Agent.inst = inst;

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        BytecodeTransformer.enabled = false;
                        if (config.isSaveSnapshotOnExit()) {
                            SnapshotUtil.saveSnapshot();
                        }
                        print(0, "Profiler stopped");
                        config.requestTempDirCleanup();
                    }
                });
                if (config.isWaitConnection()) {
                    print(0, "JVM waiting connection from Profiler4j Console...");
                    synchronized (waitConnectionLock) {
                        waitConnectionLock.wait();
                    }
                }
            }
        } catch (Throwable any) {
            Log.print(0, "UNEXPECTED ERROR", any);
            System.exit(1);
        }
    }

    static Class[] getLoadedClasses(boolean skipNonEnhanceable) {
        if (!skipNonEnhanceable) {
            return inst.getAllLoadedClasses();
        }
        List<Class> list = new ArrayList<Class>();
        for (Class c : inst.getAllLoadedClasses()) {
            String cn = c.getName();
            if (!Transformer.rejectByDefault(cn) && !c.isInterface()) {
                list.add(c);
            }
        }
        return (Class[]) list.toArray(new Class[0]);
    }

    /**
     * Reloads all classes and applies a new set of instrumentation rules.
     * 
     * @param optionsAsStr new default rule options given as a string
     * @param rulesAsStr new rule list given as a string
     * 
     * @throws Exception
     */
    static void reloadClasses(String optionsAsStr,
                              String rulesAsStr,
                              ReloadCallBack callback) throws Exception {
        synchronized (globalLock) {
            ClassUtil.totalWriteTime.set(0);
            ClassUtil.totalReadTime.set(0);
            BytecodeTransformer.totalTransformTime.set(0);
            long totalRedefineTime = 0;

            long t0 = System.currentTimeMillis();
            Class[] classes = getLoadedClasses(true);
            print(0, "--> Starting class redefinition (max " + classes.length + ")...");
            ThreadProfiler.startSessionConfig();
            config.parseRules(optionsAsStr, rulesAsStr);
            if (callback != null)
                callback.setMaxValue(classes.length);
            int nProbed = 0;
            int nRedefined = 0;
            List<Class> errors = new ArrayList<Class>();
            synchronized (modifiedClassNames) {
                modifiedClassNames.clear();
                modifiedClassCount = 0;
            }
            for (Class c : classes) {
                if (mustRedefineClass(c)) {
                    byte[] bytes = ClassUtil.loadClassBackup(c);
                    if (bytes != null) {
                        long rt0 = System.nanoTime();
                        try {
                            print(0, "Redefining " + c.getName());
                            ClassDefinition cd = new ClassDefinition(c, bytes);
                            inst.redefineClasses(new ClassDefinition[]{cd});
                            nRedefined++;
                        } catch (Throwable e) {
                            print(0, "Could not redefine class " + c.getName(), e);
                            errors.add(c);
                        } finally {
                            long rt = System.nanoTime() - rt0;
                            totalRedefineTime += rt;
                        }
                    }
                }
                nProbed++;
                if (callback != null)
                    callback.setValue(nProbed);
            }
            ThreadProfiler.endSessionConfig();
            synchronized (modifiedClassNames) {
                long t1 = System.currentTimeMillis();
                print(0, format("--> Redefined %d classes in %.1f seconds",
                                nRedefined,
                                (t1 - t0) / 1000d));
                print(0, "    Probed classes : " + nProbed);
                print(0, "    Redefinition errors: " + errors.size());
                for (Class c : errors) {
                    print(0, "       - " + c);
                }
                print(0, "    Transformed classes: " + modifiedClassCount);
                print(0, "    Unique names of transformed classes: "
                        + modifiedClassNames.size());
            }
            print(1, "    ClassUtil.totalWriteTime(ms) = "
                    + ClassUtil.totalWriteTime.get() / 1000000);
            print(1, "    ClassUtil.totalReadTime(ms) = " + ClassUtil.totalReadTime.get()
                    / 1000000);
            print(1, "    BytecodeTransformed.totalTransformTime(ms) = "
                    + BytecodeTransformer.totalTransformTime.get() / 1000000);
            print(1, "    Agent.totalRedefineTime(ms) = " + totalRedefineTime / 1000000);
        }

    }

    /**
     * Checks whether a class must be redefined.
     * <p>
     * The rationale for this method is that not all classes must go through all
     * redefinition steps (load bytes, redefine and transform). If they are neither
     * affected by the currently active set of rules (so they are uninstrumented) or the
     * new set of rules (they won't be instrumented) they can be simply skipped. The gains
     * provided by this verification are directly proportional to the level of
     * restrictions imposed by our rules (current and new).
     * <p>
     * In simple terms, a class must reloaded if at least one of the following conditions
     * hold:
     * <ol>
     * <li>The class was transformed/redefined in the last session. This means that we
     * need to undo the bytecode instrumentation.
     * <li>The class will be redefined in the current session. This means that the class
     * must be instrumented anyway.
     * </ol>
     * 
     * This method depends on the fact that whenever the method
     * {@link Config#parseRules(String, String)} is called the method
     * {@link ThreadProfiler#startSessionConfig()} is called as well. Otherwise the premisse
     * that {@link Config#getLastRules()} is valid vanishes.
     * 
     * @param c class to check
     * @return <code>true</code> if the class must be redefined
     */
    private static boolean mustRedefineClass(Class c) {
        if (config.getLastRules() != null) {
            for (Rule rule : config.getLastRules()) {
                if (ruleMatchesClass(rule, c)) {
                    if (rule.getAction() == Rule.Action.ACCEPT) {
                        return true;
                    }
                    break;
                }
            }
        }
        if (config.getRules() != null) {
            for (Rule rule : config.getRules()) {
                if (ruleMatchesClass(rule, c)) {
                    if (rule.getAction() == Rule.Action.ACCEPT) {
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    private static boolean ruleMatchesClass(Rule r, Class c) {
        String s = r.getPattern();
        int p1 = s.indexOf('(');
        int p2 = s.indexOf(')');
        if (p1 > 0 && p2 > p1) {
            s = s.substring(0, p1);
            return Utils.getRegex(s).matcher(c.getName()).matches();
        }
        throw new Profiler4JError("Invalid rule pattern '" + s + "'");
    }

    public static interface ReloadCallBack {
        void setMaxValue(int n) throws Exception;
        void setValue(int n) throws Exception;
    }

}
