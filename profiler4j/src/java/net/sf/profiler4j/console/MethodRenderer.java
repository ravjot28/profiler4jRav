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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class MethodRenderer extends DefaultTreeCellRenderer {

    private static final String IMAGE_BASE = "/net/sf/profiler4j/console/images/";

    private Font rootFont = new Font("Tahoma", Font.BOLD, 11);
    private Color rootColor = Color.decode("#000000");
    private Icon rootIcon = loadIcon("medal_gold_1.png");

    private Font level1Font = new Font("Tahoma", Font.BOLD, 11);
    private Color level1Color = Color.decode("#FF0000");
    private Icon level1Icon = loadIcon("tag_red.png");

    private Font level2Font = new Font("Tahoma", Font.PLAIN, 11);
    private Color level2Color = Color.decode("#DD2222");
    private Icon level2Icon = loadIcon("tag_yellow.png");

    private Font level3Font = new Font("Tahoma", Font.PLAIN, 11);
    private Color level3Color = Color.decode("#006600");
    private Icon level3Icon = loadIcon("tag_green.png");

    private Font selfFont = new Font("Tahoma", Font.PLAIN, 11);
    private Color selfColor = Color.decode("#000000");
    private Icon selfIcon = loadIcon("time.png");

    private Icon realRootIcon = loadIcon("clock.png");

    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value,
                                                  boolean sel,
                                                  boolean expanded,
                                                  boolean leaf,
                                                  int row,
                                                  boolean hasFocus) {
        super.getTreeCellRendererComponent(tree,
                                           value,
                                           sel,
                                           expanded,
                                           leaf,
                                           row,
                                           hasFocus);        
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        TreeBuilder.NodeInfo info = (TreeBuilder.NodeInfo) node.getUserObject();
        if (info != null) {
            boolean child = info.getType() == TreeBuilder.Type.CHILD;
            boolean self = info.getType() == TreeBuilder.Type.SELF;
            boolean root = info.getType() == TreeBuilder.Type.ROOT;
            if (root) {
                setForeground(rootColor);
                setFont(rootFont);
                setIcon(rootIcon);
            } else if (child) {
                if (info.getPercentFromRoot() >= 30) {
                    setForeground(level1Color);
                    setFont(level1Font);
                    setIcon(level1Icon);
                } else if (info.getPercentFromRoot() >= 15) {
                    setForeground(level2Color);
                    setFont(level2Font);
                    setIcon(level2Icon);
                } else {
                    setForeground(level3Color);
                    setFont(level3Font);
                    setIcon(level3Icon);
                }
            } else if (self) {
                setForeground(selfColor);
                setFont(selfFont);
                setIcon(selfIcon);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            setIcon(realRootIcon);
        }

        if (sel) {
            setForeground(Color.WHITE);
        }

        //setBackground(Color.decode("#ffffee"));
        setOpaque(false);
        return this;
    }

    private static ImageIcon loadIcon(String name) {
        return new ImageIcon(MethodRenderer.class
            .getResource(IMAGE_BASE + name));
    }

}
