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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.JCheckBox;

public class ClassListPanel extends JPanel implements AppEventListener {

    private JScrollPane classesScrollPane = null;
    private JTable classesTable = null;
    private JPanel bottomPanel = null;
    private JTextField filterTextField = null;
    private ClassListTableModel classListTableModel = null; // @jve:decl-index=0:visual-constraint="600,11"
    private JPanel topPanel = null;
    private JButton refreshButton = null;

    private ConsoleApp app;
    private JButton clearButton = null;
    private JLabel jLabel = null;
    private JButton addAsRuleButton = null;
    private JCheckBox onlyInstrumentedCheckBox = null;

    /**
     * This is the default constructor
     */
    public ClassListPanel(ConsoleApp app) {
        super();
        this.app = app;        
        initialize();
        app.addListener(this);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(582, 200);
        this.add(getClassesScrollPane(), java.awt.BorderLayout.CENTER);
        this.add(getBottomPanel(), java.awt.BorderLayout.SOUTH);
        this.add(getTopPanel(), java.awt.BorderLayout.NORTH);
    }

    /**
     * This method initializes classesScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getClassesScrollPane() {
        if (classesScrollPane == null) {
            classesScrollPane = new JScrollPane();
            classesScrollPane.setViewportView(getClassesTable());
        }
        return classesScrollPane;
    }

    /**
     * This method initializes classesTable
     * 
     * @return javax.swing.JTable
     */
    private JTable getClassesTable() {
        if (classesTable == null) {
            classesTable = new JTable();
            classesTable
                .setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            classesTable.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 12));
            classesTable.setModel(getClassListTableModel());
            classesTable.setRowHeight(20);
            classesTable
                .getSelectionModel()
                    .addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            if (classesTable.getSelectedRowCount() > 0) {
                                addAsRuleButton.setEnabled(true);
                            } else {
                                addAsRuleButton.setEnabled(false);
                            }

                        }
                    });
            TableColumn c = classesTable.getColumnModel().getColumn(0);
            c.setMinWidth(50);
            c.setMaxWidth(50);

            c = classesTable.getColumnModel().getColumn(1);
            c.setMinWidth(300);
            c.setCellRenderer(new DefaultTableCellRenderer() {
                Font f1 = new Font("Tahoma", java.awt.Font.PLAIN, 12);
                Font f2 = new Font("Tahoma", java.awt.Font.BOLD, 12);
                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value,
                                                               boolean isSelected,
                                                               boolean hasFocus,
                                                               int row,
                                                               int column) {
                    super.getTableCellRendererComponent(table,
                                                        value,
                                                        isSelected,
                                                        hasFocus,
                                                        row,
                                                        column);

                    if (isSelected) {
                        setFont(f2);
                    } else {
                        setFont(f1);
                    }
                    if (classListTableModel.getRow(row).info.isInstrumented()) {
                        if (isSelected) {
                            setForeground(Color.YELLOW);
                            setBackground(Color.BLUE);
                        } else {
                            setBackground(Color.decode("#bbffbb"));
                            setForeground(Color.BLACK);
                        }
                    } else {
                        if (isSelected) {
                            setForeground(Color.WHITE);
                            setBackground(Color.BLUE);
                        } else {
                            setBackground(Color.WHITE);
                            setForeground(Color.BLACK);
                        }
                    }
                    setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                    return this;
                }
            });

        }
        return classesTable;
    }

    /**
     * This method initializes bottomPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getBottomPanel() {
        if (bottomPanel == null) {
            GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
            gridBagConstraints12.gridx = 2;
            gridBagConstraints12.insets = new java.awt.Insets(4, 4, 4, 4);
            gridBagConstraints12.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints12.gridy = 0;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 5;
            gridBagConstraints11.insets = new java.awt.Insets(8, 4, 8, 8);
            gridBagConstraints11.gridy = 0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.insets = new java.awt.Insets(8, 4, 8, 4);
            gridBagConstraints3.gridy = 0;
            gridBagConstraints3.gridx = 4;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.insets = new java.awt.Insets(8, 4, 8, 4);
            gridBagConstraints2.gridy = 0;
            gridBagConstraints2.gridx = 3;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridx = 1;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.insets = new java.awt.Insets(4, 4, 4, 4);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(4, 8, 4, 4);
            gridBagConstraints.gridy = 0;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
            gridBagConstraints.gridx = 0;
            jLabel = new JLabel();
            jLabel.setText("Filter: ");
            bottomPanel = new JPanel();
            bottomPanel.setLayout(new GridBagLayout());
            bottomPanel.add(jLabel, gridBagConstraints);
            bottomPanel.add(getFilterTextField(), gridBagConstraints1);
            bottomPanel.add(getClearButton(), gridBagConstraints2);
            bottomPanel.add(getRefreshButton(), gridBagConstraints3);
            bottomPanel.add(getAddAsRuleButton(), gridBagConstraints11);
            bottomPanel.add(getOnlyInstrumentedCheckBox(), gridBagConstraints12);
        }
        return bottomPanel;
    }

    /**
     * This method initializes filterTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getFilterTextField() {
        if (filterTextField == null) {
            filterTextField = new JTextField();
            filterTextField.setColumns(20);
            filterTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        refreshClassList();
                    }
                }
            });
        }
        return filterTextField;
    }

    /**
     * This method initializes classListTableModel
     * 
     * @return net.sf.profiler4j.console.ClassListTableModel
     */
    private ClassListTableModel getClassListTableModel() {
        if (classListTableModel == null) {
            classListTableModel = new ClassListTableModel();
        }
        return classListTableModel;
    }

    public void refreshClassList() {
        try {
            Client.ClassInfo[] classes = app.getClient().listLoadedClasses();
            classListTableModel.setFilters(filterTextField.getText(),
                                           onlyInstrumentedCheckBox.isSelected());
            classListTableModel.setClasses(classes);
        } catch (ClientException e1) {
            e1.printStackTrace();
            JOptionPane.showMessageDialog(ClassListPanel.this,
                                          "Request error",
                                          "Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method initializes topPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getTopPanel() {
        if (topPanel == null) {
            topPanel = new JPanel();
            topPanel.setLayout(new BorderLayout());
        }
        return topPanel;
    }

    /**
     * This method initializes refreshButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRefreshButton() {
        if (refreshButton == null) {
            refreshButton = new JButton();
            refreshButton.setToolTipText("Refresh list");
            refreshButton.setPreferredSize(new java.awt.Dimension(28, 28));
            refreshButton.setIcon(new ImageIcon(getClass()
                .getResource("/net/sf/profiler4j/console/images/refresh.gif")));
            refreshButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    refreshClassList();
                }
            });
        }
        return refreshButton;
    }

    /**
     * This method initializes clearButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getClearButton() {
        if (clearButton == null) {
            clearButton = new JButton();
            clearButton.setIcon(new ImageIcon(getClass()
                .getResource("/net/sf/profiler4j/console/images/removed.gif")));
            clearButton.setPreferredSize(new java.awt.Dimension(28, 28));
            clearButton.setToolTipText("Clear filter");
            clearButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    filterTextField.setText("");
                    refreshClassList();
                }
            });
        }
        return clearButton;
    }

    /**
     * This method initializes addAsRuleButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getAddAsRuleButton() {
        if (addAsRuleButton == null) {
            addAsRuleButton = new JButton();
            addAsRuleButton.setIcon(new ImageIcon(getClass()
                .getResource("/net/sf/profiler4j/console/images/wand.png")));
            addAsRuleButton.setToolTipText("Create rules from classes");
            addAsRuleButton.setEnabled(false);
            addAsRuleButton.setPreferredSize(new java.awt.Dimension(28, 28));
            addAsRuleButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    int ret = JOptionPane
                        .showConfirmDialog(ClassListPanel.this,
                                           "Create rules from selected classes?",
                                           "Question",
                                           JOptionPane.YES_NO_OPTION);
                    if (ret == JOptionPane.OK_OPTION) {
                        int i = 0;
                        for (int r : classesTable.getSelectedRows()) {
                            String n = (String) classListTableModel.getRow(r).info
                                .getName();
                            Rule rule = new Rule(n + ".*(*)", Rule.Action.ACCEPT);
                            app.getProject().getRules().add(i++, rule);
                        }
                        ProjectDialog d = new ProjectDialog(app.getMainFrame(), app);
                        d.edit(app.getProject());
                    }
                }
            });
        }
        return addAsRuleButton;
    }

    /**
     * This method initializes onlyInstrumentedCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getOnlyInstrumentedCheckBox() {
        if (onlyInstrumentedCheckBox == null) {
            onlyInstrumentedCheckBox = new JCheckBox();
            onlyInstrumentedCheckBox.setText("Only Instrumented");
            onlyInstrumentedCheckBox.setSelected(false);
            onlyInstrumentedCheckBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    refreshClassList();
                }
            });
        }
        return onlyInstrumentedCheckBox;
    }

    public boolean receiveEvent(AppEvent ev) {
        if (ev.getType() == AppEventType.CONNECTED) {
            refreshClassList();
        } else if (ev.getType() == AppEventType.DISCONNECTED) {
            classListTableModel.clear();
        } else if (ev.getType() == AppEventType.RULES_APPLIED) {
            refreshClassList();
        }
        return false;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
