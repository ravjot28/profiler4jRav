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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * Class responsible for loading the console application.
 * 
 * @author Antonio S. R. Gomes
 */
public class Bootstrap {

    private static final String MAIN_CLASS = "net.sf.profiler4j.console.ConsoleApp";

    public static void main(String[] args)
        throws IOException, ClassNotFoundException, SecurityException,
        NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
        InvocationTargetException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        URL url = Bootstrap.class.getResource("Bootstrap.class");
        String s = url.toString();
        if (s.startsWith("jar:file:")) {
            System.out.println("URL = " + s);
            s = URLDecoder.decode(s, Charset.defaultCharset().name());
            System.out.println("Decoded URL (charset" + Charset.defaultCharset().name()
                    + ") = " + s);
            int p = s.indexOf("!");
            // Let´s remove the preffix "jar:file:"
            s = s.substring(9, p);
            
            if (s.indexOf(':') == 2) {
                // Probably we are in windows and the string should be something
                // like "/g:/tools/app..." so we must strip the first char out
                s = s.substring(1);
            } else {
              // In the default case the path may be something like "/home/as", so
              // theres nothing to change
            }
                        
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < s.length(); i++) {
                sb.append((s.charAt(i) == '/') ? File.separatorChar : s.charAt(i));
            }
            s = sb.toString();            
            File parentFile = new File(s).getParentFile();
            System.out.println("Detected PROFILER4J_HOME is " + parentFile.getAbsolutePath());
            File libDir = new File(parentFile, "lib");
            File[] libs = libDir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".jar");
                };
            });
            URL[] urls = new URL[libs.length];
            for (int i = 0; i < libs.length; i++) {
                urls[i] = libs[i].toURL();
                System.out.println("urls[" + i + "] = " + urls[i]);
            }
            loader = new URLClassLoader(urls, Bootstrap.class.getClassLoader());
            Thread.currentThread().setContextClassLoader(loader);
        }
        Class cc = loader.loadClass(MAIN_CLASS);
        Method m = cc.getDeclaredMethod("main", new Class[]{String[].class});
        m.invoke(null, new Object[]{null});
    }

}
