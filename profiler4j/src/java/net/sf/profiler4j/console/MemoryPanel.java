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

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.data.time.Millisecond;

public class MemoryPanel extends JPanel implements AppEventListener {

    private static final long MEGA = 1024 * 1024;
    
    private AllocDiffPanel allocDiffPanel = null;
    private JLabel jLabel1 = null;
    private JTextField maxNonHeapMemTextField = null;
    private MemoryPlotPanel nonHeapMemoryUsagePanel = null;
    private JPanel infoPanel = null;
    private JTextField maxHeapMemTextField = null;
    private JLabel jLabel = null;
    private MemoryPlotPanel heapMemoryUsagePanel = null;
    private ConsoleApp app;

    /**
     * This is the default constructor
     */
    public MemoryPanel(ConsoleApp app) {
        super();
        this.app = app;
        initialize();
        app.addListener(this);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
        gridBagConstraints12.gridx = 2;
        gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints12.weightx = 1.0;
        gridBagConstraints12.weighty = 1.0;
        gridBagConstraints12.gridy = 1;
        GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
        gridBagConstraints11.gridx = 1;
        gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints11.weightx = 1.0;
        gridBagConstraints11.weighty = 1.0;
        gridBagConstraints11.gridy = 1;
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.gridy = 1;
        GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 0;
        gridBagConstraints1.ipadx = 16;
        gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints1.gridwidth = 3;
        gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
        gridBagConstraints1.gridy = 0;
        this.setLayout(new GridBagLayout());
        this.setSize(541, 332);
        this.add(getInfoPanel(), gridBagConstraints1);
        this.add(getMemoryUsagePanel(), gridBagConstraints);
        this.add(getAllocDiffPanel(), gridBagConstraints11);
        this.add(getNonHeapMemoryUsagePanel(), gridBagConstraints12);
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getInfoPanel() {
        if (infoPanel == null) {
            jLabel1 = new JLabel();
            jLabel1.setText("        Max Non-Heap Memory (MB):");
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            jLabel = new JLabel();
            jLabel.setText("Max Heap Memory (MB):");
            infoPanel = new JPanel();
            infoPanel.setLayout(new FlowLayout());
            infoPanel.add(jLabel, null);
            infoPanel.add(getMaxHeapMemTextField(), null);
            infoPanel.add(jLabel1, null);
            infoPanel.add(getMaxNonHeapMemTextField(), null);
        }
        return infoPanel;
    }

    /**
     * This method initializes maxMemoryTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getMaxHeapMemTextField() {
        if (maxHeapMemTextField == null) {
            maxHeapMemTextField = new JTextField();
            maxHeapMemTextField.setEditable(false);
            maxHeapMemTextField.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD,
                    11));
            maxHeapMemTextField.setColumns(7);
        }
        return maxHeapMemTextField;
    }

    /**
     * This method initializes memoryUsagePanel
     * 
     * @return demo.MemoryUsagePanel
     */
    private MemoryPlotPanel getMemoryUsagePanel() {
        if (heapMemoryUsagePanel == null) {
            heapMemoryUsagePanel = new MemoryPlotPanel("Heap Memory");
        }
        return heapMemoryUsagePanel;
    }

    private void addInfo(MemoryInfo info) {
        Millisecond ms = new Millisecond();
        MemoryUsage hmu = info.getHeapUsage();
        MemoryUsage nhmu = info.getNonHeapUsage();

        heapMemoryUsagePanel.addSample(ms, hmu.getCommitted(), hmu.getUsed());
        maxHeapMemTextField.setText(String.valueOf(hmu.getMax() / MEGA));

        nonHeapMemoryUsagePanel.addSample(ms, nhmu.getCommitted(), nhmu.getUsed());
        maxNonHeapMemTextField.setText(String.valueOf(nhmu.getMax() / MEGA));

        // allocDiffPanel.addSample(ms, info.getLastSecCount());
    }

    public boolean receiveEvent(AppEvent ev) {
        if (ev.getType() == AppEventType.DISCONNECTED) {
            heapMemoryUsagePanel.reset();
            nonHeapMemoryUsagePanel.reset();
        } else if (ev.getType() == AppEventType.GOT_MEMORY_INFO) {
            addInfo((MemoryInfo) ev.getArg());
        }
        return false;
    }

    /**
     * This method initializes allocDiffPanel
     * 
     * @return net.sf.profiler4j.console.AllocDiffPanel
     */
    private AllocDiffPanel getAllocDiffPanel() {
        if (allocDiffPanel == null) {
            allocDiffPanel = new AllocDiffPanel();
            allocDiffPanel.setVisible(false);
        }
        return allocDiffPanel;
    }

    /**
     * This method initializes jTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getMaxNonHeapMemTextField() {
        if (maxNonHeapMemTextField == null) {
            maxNonHeapMemTextField = new JTextField();
            maxNonHeapMemTextField.setEditable(false);
            maxNonHeapMemTextField.setFont(new java.awt.Font("Tahoma",
                    java.awt.Font.BOLD, 11));
            maxNonHeapMemTextField.setColumns(7);
        }
        return maxNonHeapMemTextField;
    }

    /**
     * This method initializes memoryUsagePanel1
     * 
     * @return net.sf.profiler4j.console.MemoryUsagePanel
     */
    private MemoryPlotPanel getNonHeapMemoryUsagePanel() {
        if (nonHeapMemoryUsagePanel == null) {
            nonHeapMemoryUsagePanel = new MemoryPlotPanel("Non-Heap Memory");
        }
        return nonHeapMemoryUsagePanel;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
