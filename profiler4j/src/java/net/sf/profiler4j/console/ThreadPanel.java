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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;

import java.awt.FlowLayout;
import net.sf.profiler4j.agent.ThreadInfo;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * CLASS_COMMENT
 * 
 * @author Antonio S. R. Gomes
 */
public class ThreadPanel extends JPanel implements AppEventListener {

    private ConsoleApp app;
    private JPanel topPanel = null;
    private JPanel viewPanel = null;
    private JButton refreshButton = null;
    private JScrollPane jScrollPane = null;
    private JTextPane text = null;

    /**
     * Do not use this constructor.
     * 
     */
    public ThreadPanel() {
        super();
        initialize();
    }

    /**
     * This is the default constructor
     */
    public ThreadPanel(ConsoleApp app) {
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
        this.setSize(300, 200);
        this.add(getTopPanel(), java.awt.BorderLayout.NORTH);
        this.add(getViewPanel(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes topPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getTopPanel() {
        if (topPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
            topPanel = new JPanel();
            topPanel.setLayout(flowLayout);
            topPanel.add(getRefreshButton(), null);
        }
        return topPanel;
    }

    /**
     * This method initializes viewPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getViewPanel() {
        if (viewPanel == null) {
            viewPanel = new JPanel();
            viewPanel.setLayout(new BorderLayout());
            viewPanel.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return viewPanel;
    }

    /**
     * This method initializes refreshButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRefreshButton() {
        if (refreshButton == null) {
            refreshButton = new JButton();
            refreshButton.setIcon(new ImageIcon(getClass()
                .getResource("/net/sf/profiler4j/console/images/refresh.gif")));
            refreshButton.setToolTipText("Refresh info");
            refreshButton.setEnabled(false);
            refreshButton.setPreferredSize(new java.awt.Dimension(32, 32));
            refreshButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    refreshThreads();
                }
            });
        }
        return refreshButton;
    }
    /**
     * This method initializes jScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
            jScrollPane.setViewportView(getText());
        }
        return jScrollPane;
    }

    /**
     * This method initializes textArea
     * 
     * @return javax.swing.JTextArea
     */
    private JTextPane getText() {
        if (text == null) {
            text = new JTextPane();
            text.setEditable(false);
            text.setContentType("text/html");
            text.setEditable(false);
            text.setBackground(java.awt.SystemColor.info);
        }
        return text;
    }

    public boolean receiveEvent(AppEvent ev) {
        if (ev.getType() == AppEventType.CONNECTED) {
            refreshButton.setEnabled(true);
            try {
                boolean[] capabilities = app.getClient().setThreadMonitoring(true, true);
                System.out.println("Supports thread contention monitoring? "
                        + capabilities[0]);
                System.out.println("Supports thread cpu time monitoring? "
                        + capabilities[1]);
            } catch (ClientException e) {
                app.error("Could not set thread monitoring options", e);
            }

        } else if (ev.getType() == AppEventType.TO_DISCONNECT) {
            refreshButton.setEnabled(false);
            try {
                app.getClient().setThreadMonitoring(false, false);
            } catch (ClientException e) {
                app.error("Could not reset thread monitoring options", e);
            }
            text.setText("");
        }
        return false;
    }

    private void refreshThreads() {
        StringBuilder out = new StringBuilder();
        text.setText("");
        out
            .append("<html><head><style type=\"text/css\">"
                    + "th {font-style: plain; border-style: none; border-width: thin; padding: 5px; marging: 0px;}"
                    + "td {border-style: solid; border-width: thin; padding: 5px; marging: 0px; background-color: #ffffee}"
                    + "</style><body>");
        try {
            for (ThreadInfo ti : app.getClient().getThreadInfo(null, Integer.MAX_VALUE)) {

                out.append("<table width=100%><tr>");
                out.append("<th width=240>Thread name</th>");
                out.append("<th>Thread id</th>");
                out.append("<th>State</th>");
                out.append("<th>Blocked time (ms)</th>");
                out.append("<th>Blocked count</th>");
                out.append("<th>Waited time (ms)</th>");
                out.append("</tr>");

                out.append("<tr>");
                out.append("<td><b>" + ti.getThreadName() + "</b></td>");
                out.append("<td>" + ti.getThreadId() + "</td>");
                out.append("<td>" + ti.getThreadState() + "</td>");
                out.append("<td>" + ti.getBlockedTime() + "</td>");
                out.append("<td>" + ti.getBlockedCount() + "</td>");
                out.append("<td>" + ti.getWaitedTime() + "</td>");
                out.append("</tr>");

                // out.append("Lock name : " + ti.getLockName() + "\n");
                // out.append("Lock owner : " + ti.getLockOwnerName() + "\n");
                // out.append("Is in native : " + ti.isInNative() + "\n");
                // out.append("Is supended : " + ti.isSuspended() + "\n");

                out.append("<tr><td colspan=6>");
                out.append("Stack trace:<pre>");
                for (StackTraceElement ste : ti.getStackTrace()) {
                    out.append("    " + ste.getClassName()
                            + "."
                            + ste.getMethodName()
                            + ":"
                            + (ste.isNativeMethod() ? "(Native Method)" : ((ste
                                .getLineNumber() < 0) ? "(Source Unavailable)" : String
                                .valueOf(ste.getLineNumber()))) + "\n");
                }
                out.append("</pre></td></tr></table><br>");
            }
        } catch (ClientException e1) {
            e1.printStackTrace();
            app.error("Could not get thread info");
        }
        out.append("</body></html>");
        int caret = text.getCaretPosition();
        text.setText(out.toString());
        text.setCaretPosition(Math.min(caret, out.length()));
    }

}
