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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility methods for class manipulation.
 * @author Antonio S. R. Gomes
 */
public class ClassUtil {

    public static final AtomicLong totalWriteTime = new AtomicLong(0);
    public static final AtomicLong totalReadTime = new AtomicLong(0);

    /**
     * Saves the bytes of a given class in the backup directory.
     * @param className
     * @param loader
     * @param classBytes
     * @throws IOException
     */
    public static void saveClassBackup(String className,
                                       ClassLoader loader,
                                       byte[] classBytes) throws IOException {
        long t0 = System.nanoTime();
        try {
            File bkpFile = getClassFile(className, loader);
            bkpFile.getParentFile().mkdirs();
            FileOutputStream fos = new FileOutputStream(bkpFile);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            bos.write(classBytes);
            bos.close();
        } finally {
            long dt = System.nanoTime() - t0;
            totalWriteTime.addAndGet(dt);
        }
    }

    /**
     * Gets the bytes that originally defined a given class.
     * <p>
     * This method tries first to load the class from a backup file created in the
     * filesystem just before that class was enhanced.
     * 
     * @param c class to load
     * @return class bytes or <code>null</code> if class could not be loaded
     * @throws IOException if an I/O occurred
     * 
     * @see #getClassFile(String, ClassLoader)
     */
    public static byte[] loadClassBackup(Class c) throws IOException {
        long t0 = System.nanoTime();
        try {
            File classFile = getClassFile(c);
            if (classFile.exists()) {
                byte[] buffer = new byte[(int) classFile.length()];
                FileInputStream fis = new FileInputStream(classFile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(buffer);
                bis.close();
                return buffer;
            }
            return null;
        } finally {
            long dt = System.nanoTime() - t0;
            totalReadTime.addAndGet(dt);
        }
    }

    /**
     * Gets a file that is uniquely assigned to a given class.
     * @param clazz class
     * @return assigned file (which is always the same given the class name and the
     *         classloader)
     * @see #getClassFile(String, ClassLoader)
     */
    public static File getClassFile(Class clazz) {
        ClassLoader loader = clazz.getClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return getClassFile(clazz.getName(), loader);
    }

    /**
     * Gets a file that is uniquely assigned to a given class name in a given class
     * loader.
     * 
     * @param className class name
     * @param loader class loaded in which the class is defined
     * @return assigned file (which is always the same given the class name and the
     *         classloader)
     */
    public static File getClassFile(String className, ClassLoader loader) {
        String preffix = (loader == null) ? "BOOTCL_" : String.format("CL@%08X_", System
            .identityHashCode(loader));
        return new File(Config.getTempDir() + File.separator + "uninstrumented_classes",
                preffix + className.replace('.', '_'));
    }

}
