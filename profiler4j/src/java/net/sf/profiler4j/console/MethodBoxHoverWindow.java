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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MethodBoxHoverWindow extends HoverWindow {

    private JLabel[] labels;
    private Font boldFont = new Font("Monospaced", Font.BOLD, 12);
    private Font plainFont = new Font("Monospaced", Font.PLAIN, 12);
    private Color bgColor = Color.decode("#ffffaa");

    private JPanel panel;
    public MethodBoxHoverWindow() {
        setLayout(new BorderLayout());
        setAlwaysOnTop(true);
        panel = new JPanel(new GridLayout(7, 1));
        add(panel, "Center");
        panel.setBackground(bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory
            .createLineBorder(Color.BLACK), BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        labels = new JLabel[7];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel();
            labels[i].setFont(plainFont);
            panel.add(labels[i]);
        }
        labels[0].setFont(boldFont);
        labels[4].setFont(boldFont);
        labels[6].setFont(boldFont);
    }

    @Override
    public boolean supportsElement(Object e) {
        return e instanceof MethodView;
    }

    @Override
    public void prepareForElement(Object e) {
        MethodView n = (MethodView) e;
        labels[0].setText("method  : " + n.method.getMethodName());
        labels[1].setText("class   : " + n.method.getClassName());
        labels[2].setText(format("hits    : %d", n.method.getHits()));
        labels[3].setText(format("net     : %.3f ms", n.method.getNetTime()));
        labels[4].setText(format("avg net : %.3f ms", (n.method.getNetTime() / n.method
            .getHits())));
        labels[5].setText(format("self    : %.3f ms", n.method.getSelfTime()));
        labels[6].setText(format("avg self: %.3f ms", (n.method.getSelfTime() / n.method
            .getHits())));
        pack();
    }

}
