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
package net.sf.profiler4j.console;

import static java.lang.String.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.profiler4j.agent.SnapshotUtil;
import net.sf.profiler4j.console.Snapshot.Method;

/**
 * Class responsible for creating a tree that represents all call contexts from root methods.
 * 
 * @author Antonio S. R. Gomes
 */
public class TreeBuilder {

    private static final boolean DEBUG = false;

    private static DecimalFormat millisecFormat = new DecimalFormat("0.000",
            new DecimalFormatSymbols(Locale.US));

    private int maxRootCount = 30;
    private double minChildPercent = 0.05;
    private double minimumTotalTime = 1;
    private DefaultMutableTreeNode root;
    private Snapshot snapshot;
    private Map<Integer, Type> methodTypes;
    private List<Method> rootMethods;

    private Stack<Method> stack = new Stack<Method>();

    // //////////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////////

    public TreeBuilder(Snapshot snapshot) {
        Locale.setDefault(Locale.US);
        this.snapshot = snapshot;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Public methods
    // //////////////////////////////////////////////////////////////////////////

    public void setMaxRootCount(int maxRootMethodCount) {
        this.maxRootCount = maxRootMethodCount;
    }

    public void setMinChildPercent(double minChildPercent) {
        this.minChildPercent = minChildPercent;
    }

    public DefaultMutableTreeNode buildTree() {
        findRootMethods();
        root = new DefaultMutableTreeNode();
        int ncut = maxRootCount;
        Collections.sort(rootMethods, SnapshotUtil.byNetTimeComparator);
        for (Method g : rootMethods) {
            createNodes(root, g.getNetTime(), 1.0, null, g);
            ncut--;
            if (ncut == 0) {
                break;
            }
        }
        return root;
    }

    private void findRootMethods() {
        methodTypes = new HashMap<Integer, Type>(snapshot.getMethods().size());
        rootMethods = new ArrayList<Method>();
        for (Method method : snapshot.getMethods().values()) {
            for (Method childMethod : method.getChildrenTimes().keySet()) {
                methodTypes.put(childMethod.getId(), Type.CHILD);
            }
        }
        for (Method method : snapshot.getMethods().values()) {
            if (!methodTypes.containsKey(method.getId())) {
                rootMethods.add(method);
                methodTypes.put(method.getId(), Type.ROOT);
            }
        }
    }

    // /////////////////////////////////////////////////////////////////////////
    // Helper methods
    // /////////////////////////////////////////////////////////////////////////

    private void createNodes(DefaultMutableTreeNode parentNode,
                             double rootTotalTime,
                             double contribFactor,
                             Method parentMethod,
                             Method method) {

        if (parentMethod == null) {
            System.out.println();
        }
        String preffix = null;
        if (DEBUG) {
            preffix = "\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|\t|"
                .substring(0, stack.size() * 2);
            System.out.format("%s-  net=%.1fms method=%s\n", preffix, method.getNetTime(), method
                .getName());
            preffix += "  ";
        }
        if (stack.contains(method)) {
            if (DEBUG) {
                System.out.println(preffix + "RECURSIVE!");
            }
            return;
        }

        stack.push(method);

        if (method.getNetTime() < minimumTotalTime) {
            if (DEBUG) {
                System.out.println(preffix + "RETURNED (not enough time)");
            }
            return;
        }

        String text;
        double minPercent = minChildPercent;
        Type type = methodTypes.get(method.getId());

        if (parentMethod != null) {
            double childTime = parentMethod.getChildTime(method);
            double percent = childTime / parentMethod.getNetTime();
            double k = contribFactor;
            contribFactor *= percent;

            if (DEBUG) {
                System.out.format("%s childTime=%.1fms\n", preffix, childTime);
                System.out.format("%s parent.netTime=%.1fms\n", preffix, parentMethod.getNetTime());
                System.out.format("%s percent=%.2f%%\n", preffix, percent * 100);
            }
            if (contribFactor < minPercent) {
                return;
            }
            text = format("[%2.0f%%; %6.0fms] %s",
                          contribFactor * 100,
                          k * childTime,
                          formatMethod(method));

        } else {
            // type == Type.ROOT
            text = format("[%6.0fms] %s", method.getNetTime(), method.getName());
        }
        if (DEBUG) {
            System.out.println(preffix + text);
        }

        NodeInfo nodeInfo = new NodeInfo(text, method, contribFactor, type);
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(nodeInfo);
        parentNode.add(node);

        List<Method> children = new ArrayList<Method>(method.getChildrenTimes().keySet());
        Collections.sort(children, new ChildTimeComparator(method));
        for (Method child : children) {
            createNodes(node, rootTotalTime, contribFactor, method, child);
        }

        stack.pop();
        return;
    }

    private class ChildTimeComparator implements Comparator<Method> {
        private Method parent;

        ChildTimeComparator(Method parent) {
            this.parent = parent;
        }

        public int compare(Method m1, Method m2) {
            return (int) Math.signum(parent.getChildTime(m1) - parent.getChildTime(m2));
        }

    }

    private String formatMethod(Method method) {
        int p0 = method.getName().indexOf("#") + 1;
        int p1 = method.getName().indexOf("(");
        return method.getName().substring(p0, p1) + "()";
    }

    // //////////////////////////////////////////////////////////////////////////
    // Inner classes
    // //////////////////////////////////////////////////////////////////////////

    public enum Type {
        ROOT, CHILD, SELF
    }

    /**
     * Convenience class that holds the node user data.
     */
    public static class NodeInfo {

        private String text;
        private Method methodInfo;
        private double percentFromRoot;
        private Type type;

        public NodeInfo(String text, Method methodInfo, double percentFromRoot, Type type) {
            this.text = text;
            this.methodInfo = methodInfo;
            this.percentFromRoot = percentFromRoot;
            this.type = type;
        }

        public Method getMethodInfo() {
            return methodInfo;
        }

        public String getText() {
            return text;
        }

        public double getPercentFromRoot() {
            return percentFromRoot;
        }

        /**
         * @return Returns the type.
         */
        public Type getType() {
            return this.type;
        }

        @Override
        public String toString() {
            return text;
        }
    }
}
