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

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Application controller.
 * 
 * @author Antonio S. R. Gomes
 */
public class ConsoleApp {

    private Client client;
    private Project project;
    private MainFrame mainFrame;
    private File lastDir;

    private Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // REMINDER: this method is always invoked from the EDT
            if (client.isConnected()) {
                try {
                    sendEvent(AppEventType.GOT_MEMORY_INFO, client.getMemoryInfo());
                } catch (ClientException re) {
                    re.printStackTrace();
                    error("Could not refresh memory info: " + re.getMessage());
                }
            }
        };
    });

    private List<AppEventListener> listeners = new ArrayList<AppEventListener>();

    private FileFilter projectFilter = new FileFilter() {
        public boolean accept(File file) {
            String filename = file.getName();
            return filename.endsWith(".p4j");
        }
        public String getDescription() {
            return "*.p4j (Profiling Project)";
        }
    };

    private ConsoleApp() {
        project = new Project();
        lastDir = new File(System.getProperty("user.home"));
        client = new Client(new Client.ClientListener() {
            public void updateConnectionState(final boolean connected) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (connected) {
                            sendEvent(AppEventType.CONNECTED);
                        } else {
                            sendEvent(AppEventType.DISCONNECTED);
                        }
                    }
                });
            }
        });
    }

    public void error(String message) {
        showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void error(String message, Throwable t) {
        t.printStackTrace();
        showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void connect() {
        LongTask t = new LongTask() {
            public void executeInBackground() throws Exception {
                setMessage("Establishing connection with remote JVM...");
                client.connect(project.getHostname(), project.getPort());
                setMessage("Activating profiling rules...");
                getClient().applyRules(project, new Client.ProgressCallback() {
                    private int max;
                    public void setMaxValue(int amount) {
                        max = amount;
                    }
                    public void setCurrentValue(int value) {
                        setTaskProgress((value * 100) / max);
                    }
                });
            }
        };
        runTask(t);
        if (t.getError() != null) {
            t.getError().printStackTrace();
        } else {
            sendEvent(AppEventType.CONNECTED);
        }
    }

    public void disconnect() {
        if (!client.isConnected()) {
            return;
        }
        int ret = JOptionPane
            .showConfirmDialog(mainFrame,
                               "Do you want to undo any changes made to classes before\n"
                                       + "you disconnect?\n\n"
                                       + "(This requires some time to complete but leaves\n"
                                       + "the remote JVM running at 100% of the original speed)",
                               "Disconnection",
                               JOptionPane.YES_NO_CANCEL_OPTION);
        if (ret == JOptionPane.CANCEL_OPTION) {
            return;
        }
        final boolean undoChanges = ret == JOptionPane.YES_OPTION;
        LongTask task = new LongTask() {
            public void executeInBackground() throws Exception {
                if (undoChanges) {
                    setMessage("Undoing changes to classes...");
                    client.restoreClasses(new Client.ProgressCallback() {
                        private int max;
                        public void setMaxValue(int amount) {
                            max = amount;
                            setCurrentValue(0);
                        }
                        public void setCurrentValue(int value) {
                            setTaskProgress((value * 100) / max);
                        }
                    });
                }
            };
        };
        runTask(task);

        sendEvent(AppEventType.TO_DISCONNECT);

        try {
            client.disconnect();
        } catch (ClientException e) {
            error("Connection was close with error: (" + e.getMessage() + ")", e);
        }

        sendEvent(AppEventType.DISCONNECTED);
    }

    /**
     * @return <code>true</code> if a new project was created (the user could have been
     *         cancelled)
     */
    public boolean newProject() {
        if (client.isConnected()) {
            int ret = JOptionPane.showConfirmDialog(mainFrame,
                                                    "Proceed and disconnect?",
                                                    "New Profiling Project",
                                                    JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        if (checkUnsavedChanges()) {
            return false;
        }
        if (client.isConnected()) {
            disconnect();
            if (client.isConnected()) {
                return false;
            }
        }
        Project p = new Project();
        ProjectDialog d = new ProjectDialog(mainFrame, this);
        if (d.edit(p)) {
            this.project = p;
            return true;
        }
        return false;
    }

    public void openProject() {

        if (client.isConnected()) {
            int ret = JOptionPane.showConfirmDialog(mainFrame,
                                                    "Proceed and disconnect?",
                                                    "Open Profiling Project",
                                                    JOptionPane.YES_NO_OPTION);
            if (ret == JOptionPane.NO_OPTION) {
                return;
            }
        }

        if (checkUnsavedChanges()) {
            return;
        }

        if (client.isConnected()) {
            disconnect();
            if (client.isConnected()) {
                return;
            }
        }
        JFileChooser fc = new JFileChooser(lastDir);
        fc.addChoosableFileFilter(projectFilter);
        if (fc.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File selFile = fc.getSelectedFile();
            SAXBuilder builder = new SAXBuilder();
            Document doc = null;
            try {
                doc = builder.build(selFile);
            } catch (JDOMException e) {
                error("XML Error: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                error("I/O Error: " + e.getMessage());
                e.printStackTrace();
            }
            if (doc != null) {
                Project p = new Project();
                Element el = doc.getRootElement();
                p.setHostname(el.getChildText("Host"));
                p.setPort(Integer.parseInt(el.getChildText("Port")));
                Element rulesEl = el.getChild("Rules");

                p.setAccess(Rule.AccessOption
                    .valueOf(rulesEl.getAttributeValue("access")));
                p.setBeanprops(Boolean.parseBoolean(rulesEl
                    .getAttributeValue("beanProps")));

                p.getRules().clear();
                for (Iterator i = rulesEl.getChildren("Rule").iterator(); i.hasNext();) {
                    Element r = (Element) i.next();
                    Rule rule = new Rule(r.getText(), Rule.Action.valueOf(r
                        .getAttributeValue("action")));
                    p.getRules().add(rule);
                }
                p.setFile(selFile);
                p.clearChanged();
                this.project = p;
                lastDir = selFile.getParentFile();
            }
        }

    }

    public void close() {
        if (checkUnsavedChanges()) {
            return;
        }
        if (client.isConnected()) {
            disconnect();
            if (client.isConnected()) {
                return;
            }
        }
    }

    /**
     * Saves the current project.
     * 
     * @param saveAs force the user to select a file name even
     * @return <code>true</code> if the user has cancelled (only in the case of save as)
     */
    public boolean saveProject(boolean saveAs) {
        if (project.getFile() == null || saveAs) {
            JFileChooser fc = new JFileChooser(lastDir);

            fc.setDialogTitle("Save Project As");

            fc.addChoosableFileFilter(projectFilter);
            if (fc.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                if (!f.getName().endsWith(".p4j")) {
                    f = new File(f.getAbsolutePath() + ".p4j");
                }
                project.setFile(f);
            } else {
                return true;
            }
        }

        Element rootEl = new Element("Profiler4jProject");
        Document doc = new Document(rootEl);

        rootEl.addContent(new Element("Host").setText(project.getHostname()));
        rootEl.addContent(new Element("Port").setText(String.valueOf(project.getPort())));

        Element rulesEl = new Element("Rules");
        rootEl.addContent(rulesEl);

        rulesEl.setAttribute("access", project.getAccess().name());
        rulesEl.setAttribute("beanProps", String.valueOf(project.isBeanprops()));

        for (Rule rule : project.getRules()) {
            rulesEl.addContent(new Element("Rule")
                .setText(rule.getPattern())
                    .setAttribute("action", rule.getAction().name()));
        }

        try {
            FileWriter fw = new FileWriter(project.getFile());
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, fw);
            fw.close();
            project.clearChanged();
        } catch (IOException e) {
            error("I/O Error: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean editProject() {
        ProjectDialog d = new ProjectDialog(mainFrame, this);
        return d.edit(project);
    }

    public void applyRules() {
        int ret = JOptionPane
            .showConfirmDialog(mainFrame,
                               "Apply profiling rules now? (This may take some time)",
                               "Change Profiling Rules",
                               JOptionPane.OK_CANCEL_OPTION);
        if (ret != JOptionPane.OK_OPTION) {
            return;
        }
        LongTask t = new LongTask() {
            public void executeInBackground() throws Exception {
                setMessage("Activating profiling rules...");
                client.applyRules(project, new Client.ProgressCallback() {
                    private int max;
                    public void setMaxValue(int amount) {
                        max = amount;
                        setTaskProgress(0);
                    }
                    public void setCurrentValue(int value) {
                        setTaskProgress((value * 100) / max);
                    }
                });
            };
        };
        runTask(t);
        if (t.getError() == null) {
            sendEvent(AppEventType.RULES_APPLIED);
        }
    }

    public void takeSnapshot() {
        LongTask t = new LongTask() {
            public void executeInBackground() throws Exception {
                setMessage("Retrieving snapshot...");
                System.out.println("Retrieving snapshot...");
                setValue(client.getSnapshot());
                System.out.println("in takeSnapshot");
            };
        };
        runTask(t);
        if (t.getError() == null) {
            System.out.println("Error in takeSnapshot");
            Snapshot s = (Snapshot) t.getValue();
            sendEvent(AppEventType.SNAPSHOT, s);
        }
    }

    /**
     * 
     * @return <code>true</code> if the user has cancelled
     */
    private boolean checkUnsavedChanges() {
        if (project.isChanged()) {
            int ret = JOptionPane
                .showConfirmDialog(mainFrame,
                                   "Project has unsaved changes? Save before exit?",
                                   "Unsaved Changes",
                                   JOptionPane.YES_NO_CANCEL_OPTION);
            if (ret == JOptionPane.CANCEL_OPTION) {
                return true;
            } else if (ret == JOptionPane.YES_OPTION) {
                return saveProject(false);
            }
        }
        return false;
    }

    public void exit() {

        if (checkUnsavedChanges()) {
            return;
        }

        if (sendEvent(AppEventType.APP_CLOSING)) {
            return;
        }
        if (client.isConnected()) {
            disconnect();
            if (!client.isConnected()) {
                sendEvent(AppEventType.APP_CLOSED);
                System.exit(0);
            }
        } else {
            sendEvent(AppEventType.APP_CLOSED);
            System.exit(0);
        }
    }

    public void runTask(LongTask task) {
        timer.stop();
        try {
            LongTaskExecutorDialog d = new LongTaskExecutorDialog(mainFrame);
            d.runTask(task);
        } finally {
            timer.restart();
        }
    }

    public void addListener(AppEventListener l) {
        listeners.add(l);
    }

    public void removeListener(AppEventListener l) {
        listeners.remove(l);
    }

    private boolean sendEvent(AppEventType evType) {
        return sendEvent(new AppEvent(evType));
    }

    private boolean sendEvent(AppEventType evType, Object arg) {
        return sendEvent(new AppEvent(evType, arg));
    }

    private boolean sendEvent(AppEvent ev) {
        for (AppEventListener l : listeners) {
            boolean vetoed = l.receiveEvent(ev);
            if (vetoed) {
                if (!ev.getType().isVetoable()) {
                    throw new IllegalArgumentException("AppEventType " + ev.getType()
                            + " is not vetoable");
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @return Returns the project.
     */
    public Project getProject() {
        return this.project;
    }

    /**
     * @param project The project to set.
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @return Returns the mainFrame.
     */
    public MainFrame getMainFrame() {
        return this.mainFrame;
    }

    /**
     * @param mainFrame The mainFrame to set.
     */
    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    /**
     * @return Returns the client.
     */
    public Client getClient() {
        return this.client;
    }

    public static void main(String[] args) {
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore
        }
        final ConsoleApp app = new ConsoleApp();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainFrame f = new MainFrame(app);
                app.setMainFrame(f);
                f.pack();
                f.setVisible(true);
            }
        });

    }

}
