import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Map;

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
                    "/Users/Rav/Desktop/Snapshot1.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Map<Integer, Method> map = (Map<Integer, Method>) in.readObject();

            in.close();
            fileIn.close();
            
            Snapshot snap = new Snapshot();
            snap.setMethods(map);
            TreeBuilder t = new TreeBuilder(snap);
            
            DefaultMutableTreeNode tree= t.buildTree();
            
            System.out.println(tree.getRoot().getChildCount());
            

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

}
