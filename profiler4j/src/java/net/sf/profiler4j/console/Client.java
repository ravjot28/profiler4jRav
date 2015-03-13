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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import net.sf.profiler4j.agent.Agent;
import net.sf.profiler4j.agent.Server;
import net.sf.profiler4j.agent.ThreadInfo;

/**
 * Class the implements the Profiler4j remote client.
 * <p>
 * Notice that if a {@link ClientException} is thrown the connection is automatically
 * closed.
 * 
 * @author Antonio S. R. Gomes
 */
public class Client {

    private Socket s;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientListener listener;

    public Client(ClientListener listener) {
        this.listener = listener;
    }

    public synchronized void connect(String host, int port) throws ClientException {
        if (isConnected()) {
            close();
            throw new ClientException("Client already connected");
        }
        try {
            s = new Socket(host, port);
            out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
            in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
            String serverVersion = in.readUTF();
            if (!serverVersion.equals(Agent.VERSION)) {
                disconnect();
                throw new ClientException(
                        "Version of remote agent is incompatible: console is "
                                + Agent.VERSION + " but agent is " + serverVersion);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public synchronized boolean isConnected() {
        return s != null && s.isConnected() && !s.isInputShutdown();
    }

    public synchronized void disconnect() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_DISCONNECT);
            close();
        } catch (Exception e) {
            handleException(e);
        }
    }

    public synchronized void reset() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_RESET_STATS);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public synchronized RuntimeInfo getRuntimeInfo() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_GET_RUNTIME_INFO);
            return RuntimeInfo.read(in);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public synchronized MemoryInfo getMemoryInfo() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_GET_MEMORY_INFO);
            return MemoryInfo.read(in);
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public synchronized boolean[] setThreadMonitoring(boolean threadMonitoring,
                                                      boolean cpuTimeMonitoring)
        throws ClientException {
        assertConnected();
        try {
            out.writeInt(Server.CMD_SET_THREAD_MONITORING);
            out.writeUnshared(new boolean[]{threadMonitoring, cpuTimeMonitoring});
            out.flush();
            return (boolean[]) in.readUnshared();
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public synchronized ThreadInfo[] getThreadInfo(long[] ids, int maxDepth)
        throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_GET_THREAD_INFO);
            out.writeUnshared(ids);
            out.writeInt(maxDepth);
            out.flush();
            return (ThreadInfo[]) in.readUnshared();
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public synchronized Snapshot getSnapshot() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_SNAPSHOT);

            try {
                FileOutputStream fos = new FileOutputStream(
                        "C:\\Users\\Hammam\\Desktop\\Snapshot.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(Snapshot.read(in).getMethods());
                oos.close();
                fos.close();
                System.out.printf("Serialized HashMap data is saved in hashmap.ser");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            System.out.println("Returning");
            return Snapshot.read(in);
        } catch (Exception e) {
            e.printStackTrace();
            handleException(e);
        }
        return null;
    }

    public void runGc() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_GC);
        } catch (Exception e) {
            handleException(e);
        }
    }

    public synchronized void restoreClasses(ProgressCallback callback)
        throws ClientException {
        applyRules0(callback, "", "*(*):reject");
    }

    public synchronized void applyRules(Project p, ProgressCallback callback)
        throws ClientException {
        StringBuilder defaultRuleOptions = new StringBuilder();
        defaultRuleOptions.append("-beanprops:" + (p.isBeanprops() ? "on" : "off"));
        defaultRuleOptions.append(" -access:" + (p.getAccess().toString().toLowerCase()));
        StringBuilder rules = new StringBuilder();
        for (net.sf.profiler4j.console.Rule r : p.getRules()) {
            rules.append(r.getPattern() + " : " + r.getAction().toString().toLowerCase()
                    + "; ");
        }
        applyRules0(callback, defaultRuleOptions.toString(), rules.toString());
    }

    /**
     * @param callback
     * @param defaultRuleOptions
     * @param rules
     * @throws ClientException
     */
    private void applyRules0(ProgressCallback callback,
                             String defaultRuleOptions,
                             String rules) throws ClientException {
        assertConnected();
        try {
            out.writeInt(Server.CMD_APPLY_RULES);
            out.writeUTF(defaultRuleOptions);
            out.writeUTF(rules);
            out.flush();
            expectOk();

            int n = in.readInt();
            if (callback != null) {
                callback.setMaxValue(n);
            }
            int i;
            while ((i = in.readInt()) != -1) {
                callback.setCurrentValue(i);
            }

        } catch (Exception e) {
            handleException(e);
        }
    }

    public synchronized ClassInfo[] listLoadedClasses() throws ClientException {
        assertConnected();
        try {
            sendAndWaitAck(Server.CMD_LIST_CLASSES);
            int n = in.readInt();
            ClassInfo[] names = new ClassInfo[n];
            for (int i = 0; i < n; i++) {
                names[i] = new ClassInfo(in.readUTF(), in.readBoolean());
            }
            return names;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // Inner classes
    // ///////////////////////////////////////////////////////////////////////////////////

    public static interface ClientListener {
        void updateConnectionState(boolean connected);
    }

    public static interface ProgressCallback {
        void setMaxValue(int amount);
        void setCurrentValue(int value);
    }

    public static class ClassInfo {
        private String name;
        private boolean instrumented;
        ClassInfo(String name, boolean instrumented) {
            this.name = name;
            this.instrumented = instrumented;
        }
        public boolean isInstrumented() {
            return this.instrumented;
        }
        public String getName() {
            return this.name;
        }

    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // Helper methods
    // ///////////////////////////////////////////////////////////////////////////////////

    private void assertConnected() throws ClientException {
        if (!isConnected()) {
            throw new ClientException("Profiler4j client not connected");
        }
    }

    private void sendAndWaitAck(int cmdId) throws IOException, ClientException {
        out.writeInt(cmdId);
        out.flush();
        expectOk();
    }

    private void expectOk() throws ClientException {
        try {
            int status = in.readInt();
            if (status != 0) {
                throw new ClientException("Command Error: code=" + status);
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) throws ClientException {
        close();
        if (e instanceof ClientException) {
            throw (ClientException) e;
        }
        throw new ClientException("I/O Error", e);
    }

    private void close() {
        try {
            s.close();
        } catch (Exception any) {
            // ignore
        }
        s = null;
        in = null;
        out = null;
    }

}
