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
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;

import javassist.ByteArrayClassPath;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;

public class BytecodeTransformer {

    public static final AtomicLong totalTransformTime = new AtomicLong(0);

    private static String THREAD_PROFILER_CN = ThreadProfiler.class.getName();
    private static Pattern getterRegex = Pattern.compile("^get[A-Z].*$");
    private static Pattern getterBoolRegex = Pattern.compile("^is[A-Z].*$");
    private static Pattern setterRegex = Pattern.compile("^set[A-Z].*$");

    private ClassPool classPool;
    private ClassLoader loader;
    private byte[] classBytes;
    private Config config;

    volatile static boolean enabled = false;

    public BytecodeTransformer(String className,
                               ClassLoader loader,
                               byte[] classBytes,
                               Config config) {
        this.classBytes = classBytes;
        classPool = new ClassPool();
        classPool.appendSystemPath();
        classPool.insertClassPath(new ByteArrayClassPath(className, classBytes));
        if (loader != null) {
            classPool.appendClassPath(new LoaderClassPath(loader));
        }
        this.loader = loader;
        this.config = config;
    }

    /**
     * Transforms a class.
     * 
     * @param className
     * @return byte array with the new definition for the intrumented class
     * 
     * @throws NotFoundException
     * @throws CannotCompileException
     * @throws IOException
     */
    public byte[] transform(String className)
        throws NotFoundException, CannotCompileException, IOException {
        long t0 = System.nanoTime();
        try {
            if (!ClassUtil.getClassFile(className, loader).exists()) {
                ClassUtil.saveClassBackup(className, loader, classBytes);
            }
            List<Rule> rules = config.getRules();
            if (!enabled || rules == null) {
                return null;
            }
            boolean modified = false;
            CtClass cc = classPool.get(className);
            if (Modifier.isInterface(cc.getModifiers())) {
                return null;
            }
            for (CtMethod cm : cc.getDeclaredMethods()) {
                if (Modifier.isAbstract(cm.getModifiers())
                        || Modifier.isNative(cm.getModifiers())
                        || cm.getName().startsWith("access$")) {
                    continue;
                }
                String[] names = makeName(cm);
                Rule selectedRule = null;
                for (Rule rule : rules) {
                    if (rule.matches(names[0])) {
                        selectedRule = rule;
                        break;
                    }
                }
                if (selectedRule != null) {
                    if (selectedRule.getAction() == Rule.Action.ACCEPT) {
                        if (!selectedRule.isBooleanOptionSet(Rule.Option.BEANPROPS,
                                                             config)
                                && isGetterSetter(cm)) {
                            continue;
                        }
                        boolean packageAccess = Modifier.isPackage(cm.getModifiers());
                        boolean protectedAccess = Modifier.isProtected(cm.getModifiers());
                        boolean publicAccess = Modifier.isPublic(cm.getModifiers());

                        String access = selectedRule
                            .getOption(Rule.Option.ACCESS, config);
                        boolean accept = "private".equals(access)
                                || ("package".equals(access) && (packageAccess
                                        || protectedAccess || publicAccess))
                                || ("protected".equals(access)
                                        && (protectedAccess || publicAccess) || ("public"
                                    .equals(access) && publicAccess));

                        if (accept) {
                            if (!modified) {
                                Log.print(1, "Transforming class " + className);
                                modified = true;
                            }
                            transformMethod(cm, names);
                        }
                    }
                }
            }
            if (modified) {
                synchronized (Agent.modifiedClassNames) {
                    Agent.modifiedClassCount++;
                    if (Agent.modifiedClassNames.contains(cc.getName())) {
                        Log.print(0, "Found duplicated class name " + cc.getName()
                                + " in " + loader);
                    } else {
                        Agent.modifiedClassNames.add(cc.getName());
                    }
                }
                if (config.isDumpClasses()) {
                    String dir = makeClassBackupDir();
                    Log.print(1, "Written modified class to " + dir);
                    cc.writeFile(dir);
                }
                byte[] b = cc.toBytecode();
                return b;
            }
            return null;
        } finally {
            long dt = System.nanoTime() - t0;
            totalTransformTime.addAndGet(dt);
        }
    }

    private String makeClassBackupDir() {
        String dir;
        String preffix = "cls_backup" + File.separator;
        if (loader == null) {
            preffix += "BOOT_CL";
        } else {
            preffix += String.format("CL@%x", System.identityHashCode(loader));
        }
        dir = config.getDumpDir().getAbsolutePath() + File.separator + preffix;
        return dir;
    }

    private void transformMethod(CtMethod cm, String[] names)
        throws NotFoundException, CannotCompileException {
        String s = getAccessModifierChar(cm);
        Log.print(2, "   instrumenting " + s + names[1]);
        int globalMethodId = ThreadProfiler.newMethod(names[0]);
        cm.insertAfter("{" + THREAD_PROFILER_CN + "#exitMethod(" + globalMethodId + ");"
                + "}", true);
        cm.insertBefore("{" + THREAD_PROFILER_CN + "#enterMethod(" + globalMethodId + ");"
                + "}");
    }

    private String[] makeName(CtMethod cm) throws NotFoundException {
        StringBuilder sbName = new StringBuilder();
        StringBuilder sbMethod = new StringBuilder();
        sbName.append(cm.getDeclaringClass().getName());
        sbName.append(".");
        sbMethod.append(cm.getName());
        sbMethod.append("(");
        boolean comma = false;
        for (CtClass pt : cm.getParameterTypes()) {
            if (comma) {
                sbMethod.append(",");
            }
            comma = true;
            sbMethod.append(pt.getName());
        }
        sbMethod.append(")");
        sbName.append(sbMethod);
        return new String[]{sbName.toString(), sbMethod.toString()};
    }

    private String getAccessModifierChar(CtMethod cm) {
        String s;
        if (Modifier.isPublic(cm.getModifiers())) {
            s = "+";
        } else if (Modifier.isPrivate(cm.getModifiers())) {
            s = "-";
        } else if (Modifier.isProtected(cm.getModifiers())) {
            s = "~";
        } else {
            s = " ";
        }
        return s;
    }

    public static boolean isGetterSetter(CtMethod cm) throws NotFoundException {

        if (!Modifier.isPublic(cm.getModifiers()) || Modifier.isStatic(cm.getModifiers())
                || Modifier.isAbstract(cm.getModifiers())
                || Modifier.isNative(cm.getModifiers())
                || Modifier.isSynchronized(cm.getModifiers())) {
            return false;
        }

        String name = cm.getName();

        if (getterRegex.matcher(name).matches()
                || getterBoolRegex.matcher(name).matches()) {
            return cm.getParameterTypes().length == 0
                    && cm.getReturnType() != CtClass.voidType;
        }

        if (setterRegex.matcher(name).matches()) {
            return cm.getParameterTypes().length == 1
                    && cm.getReturnType() == CtClass.voidType;
        }

        return false;

    }
}
