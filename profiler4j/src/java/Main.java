import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.profiler4j.console.Snapshot;
import net.sf.profiler4j.console.Snapshot.Method;
import net.sf.profiler4j.console.TreeBuilder;

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

/**
 * CLASS_COMMENT
 *
 * @author Antonio S. R. Gomes
 */
public class Main {

    public static void main(String[] args) {
        try {
            FileInputStream fileIn = new FileInputStream(
                    "C:\\Users\\Hammam\\Downloads\\Snapshot.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Map<Integer, Method> map = (Map<Integer, Method>) in.readObject();

            for (Entry<Integer, Method> entry : map.entrySet()) {
                System.out.println(entry.getValue().getClassName() + " "
                        + entry.getValue().getMethodName() + " "
                        + entry.getValue().getHits() + " "
                        + entry.getValue().getNetTime());
                break;
            }

            System.out.println();

            in.close();
            fileIn.close();

            Snapshot snap = new Snapshot();
            snap.setMethods(map);
            TreeBuilder t = new TreeBuilder(snap);

            DefaultMutableTreeNode tree = t.buildTree();

            System.out.println(tree.getRoot().getChildCount());
            System.out.println("Root " + tree.getRoot().toString());
            System.out.println("Children " + tree.getRoot().getChildAt(0).toString());
            System.out.println("Children Children "
                    + tree.getRoot().getChildAt(0).getChildAt(0).toString());

            System.out.println(map.size());
            // System.out.println(map.get(1).getMethodName());

        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }
    }

    public void reduceRecursivePaths(Node n) {
        n.setAdjustedParent(findAdjustedParent(n));

        n.getAdjustedParent().getAdjustedChildren().add(n);
        List<Node> nodes = n.getChildren();
        for (Node c : nodes)
            reduceRecursivePaths(c);
    }

    public Node findAdjustedParent(Node current) {

        Node match1 = null;
        Node match2 = null;
        Node n = current.getParent();

        while (n != null && match2 == null) {
            if (n.getMethod() == current.getMethod()) {
                if (match1 == null) {
                    match1 = n;
                } else
                    match2 = n;
            }
            n = n.getAdjustedParent();
            if (match1 == null || match2 == null) {
                return current.getParent();
            }
            Node n1 = current.getParent();
            Node n2 = match1.getAdjustedParent();
            while (n1 != match1 && n2 != match2) {
                if (n1.getMethod() != n2.getMethod()) {
                    return current.getParent();

                }
                n1 = n1.getAdjustedParent();
                n2 = n2.getAdjustedParent();

                if (n1 != match1 || n2 != match2) {
                    return current.getParent();
                }
            }
        }

        return match1.getAdjustedParent();
    }

    public void minCPD(Method m) {
        m.setMinCPD(99999999);
        Node n = m.getNodes().get(0);
        n = n.getAdjustedParent();
        while (n != null) {
            int dist = CPD(n.getMethod(), m);
            if (dist < m.getMinCPD()) {
                m.setMinCPD(dist);
                m.setCommonParent(n.getMethod());
            }
            n = n.getParent();
        }
    }

    public int CPD(Method p, Method m) {
        List<Node> nodes = m.getNodes();
        int cpd = 0;
        for (Node n : nodes) {
            int dist = Distance(p, n);
            if (dist > cpd)
                cpd = dist;
        }
        return cpd;

    }

    public int Distance(Method m, Node n) {
        int dist = 0;
        while (true) {
            dist = dist + 1;
            if (n == null)
                return 99999999;
            if (n.getMethod() == m)
                return dist;
        }
    }

    public void calculateHeight(Node v) {
        v.setHeight(0);
        List<Node> nodes = v.getAdjustedChildren();
        for (Node c : nodes) {
            calculateHeight(c);
            if ((c.getHeight() + 1) > v.getHeight())
                v.setHeight(c.getHeight() + 1);
        }
        if (v.getHeight() > v.getMethod().getMaxHeight())
            v.getMethod().setMaxHeight(v.getHeight());
    }

    public void CalculateInducedCost(Node n) {
        n.setInduced(n.getCost());
        List<Node> nodes = n.getChildren();
        for (Node c : nodes) {
            CalculateInducedCost(c);
            if (c.getMethod().isSubsumed())
                n.setInduced(n.getInduced() + c.getInduced());
        }
        n.getMethod().setInduced(n.getMethod().getInduced() + n.getInduced());

    }

}
