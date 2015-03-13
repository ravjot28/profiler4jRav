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

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.profiler4j.agent.Agent;

public class AboutDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JLabel titleLabel = null;
    private JLabel versionLabel = null;
    /**
     * This is the default constructor
     */
    public AboutDialog() {
        super();
        initialize();
    }

    public AboutDialog(JFrame f) {
        super(f);
        initialize();
        setLocationRelativeTo(getOwner());
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setSize(423, 200);
        this.setResizable(false);
        this
            .setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        this.setTitle("About Profiler4j " + Agent.VERSION);
        this.setContentPane(getJContentPane());
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            versionLabel = new JLabel();
            versionLabel
                .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            versionLabel.setFont(new java.awt.Font("Bitstream Vera Sans",
                    java.awt.Font.PLAIN, 14));
            versionLabel.setOpaque(true);
            versionLabel.setText("");
            titleLabel = new JLabel();
            titleLabel.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD,
                    24));
            titleLabel
                .setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
            titleLabel
                .setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            titleLabel.setIcon(new ImageIcon(getClass().getResource("/net/sf/profiler4j/console/images/profiler4j.png")));
            titleLabel.setText("");
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(titleLabel, BorderLayout.CENTER);
            jContentPane.add(versionLabel, BorderLayout.SOUTH);
        }
        return jContentPane;
    }

} //  @jve:decl-index=0:visual-constraint="10,10"
