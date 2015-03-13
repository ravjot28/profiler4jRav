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
package net.sf.profiler4j.agent;

import static net.sf.profiler4j.agent.Log.print;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.MemoryUsage;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Server extends Thread {

    public static final int CMD_GC = 1;
    public static final int CMD_SNAPSHOT = 2;
    public static final int CMD_RESET_STATS = 3;
    public static final int CMD_DISCONNECT = 4;
    public static final int CMD_APPLY_RULES = 5;
    public static final int CMD_LIST_CLASSES = 6;

    public static final int CMD_GET_RUNTIME_INFO = 7;
    public static final int CMD_GET_MEMORY_INFO = 8;
    public static final int CMD_GET_THREAD_INFO = 9;

    public static final int CMD_SET_THREAD_MONITORING = 10;

    public static final int COMMAND_ACK = 0x00;
    public static final int STATUS_ERROR = 0x01;
    public static final int STATUS_UNKNOWN_CMD = 0x02;

    private Config config;

    public Server(Config config) {
        super("PROFILER4J_REMOTE_AGENT");
        this.config = config;
        setDaemon(true);
        // setPriority(MAX_PRIORITY);
    }

    @Override
    public void run() {
        try {
            while (true) {
                print(0, "Listening on port " + config.getPort() + "...");
                ServerSocket srv = new ServerSocket(config.getPort());
                Socket s = srv.accept();
                print(0, "Serving connection from " + s.getRemoteSocketAddress());
                try {
                    ObjectOutputStream out = new ObjectOutputStream(
                            new BufferedOutputStream(s.getOutputStream()));
                    out.writeUTF(Agent.VERSION);
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                    serveClient(in, out);
                    Thread.sleep(250);
                } catch (SocketException e) {
                    if (e.getMessage().equals("Connection reset")) {
                        print(0, "Connection closed by client");
                    } else {
                        print(0, "Socket I/O error", e);
                    }
                } catch (Exception any) {
                    print(0, "Error during request processing. Closing connection", any);
                }
                try {
                    if (s != null) {
                        s.close();
                    }
                    if (srv != null) {
                        srv.close();
                    }
                } catch (IOException nnn) {
                    // ignore
                }
            }
        } catch (Throwable e) {
            print(0, "Server exception", e);
            if (config.isExitVmOnFailure()) {
                print(0, "Aborting JVM...");
                System.exit(3);
            }
        } finally {
            print(0, "Server exiting");
        }
    }

    private void serveClient(final ObjectInputStream in, final ObjectOutputStream out)
        throws Exception {
        while (true)
            switch (in.readInt()) {
                case CMD_GC :
                    System.gc();
                    out.writeInt(COMMAND_ACK);
                    out.flush();
                    break;
                case CMD_SNAPSHOT :
                    out.writeInt(COMMAND_ACK);
                    ThreadProfiler.createSnapshot(out);
                    out.flush();
                    break;
                case CMD_RESET_STATS :
                    ThreadProfiler.resetStats();
                    out.writeInt(COMMAND_ACK);
                    out.flush();
                    break;
                case CMD_APPLY_RULES :
                    out.writeInt(COMMAND_ACK);
                    String opts = in.readUTF();
                    String rules = in.readUTF();
                    if (config.isWaitConnection() && config.getRules() == null) {
                        config.parseRules(opts, rules);
                        out.writeInt(0);
                    } else {
                        Agent.reloadClasses(opts, rules, new Agent.ReloadCallBack() {
                            public void setMaxValue(int n) throws Exception {
                                out.writeInt(n);
                                out.flush();
                            }
                            public void setValue(int n) throws Exception {
                                if (n % 50 == 0) {
                                    out.writeInt(n);
                                    out.flush();
                                }
                            }
                        });
                    }
                    out.writeInt(-1);
                    out.flush();
                    synchronized (Agent.waitConnectionLock) {
                        Agent.waitConnectionLock.notifyAll();
                    }
                    break;
                case CMD_LIST_CLASSES :
                    out.writeInt(COMMAND_ACK);
                    Class[] classes = Agent.getLoadedClasses(true);
                    out.writeInt(classes.length);
                    synchronized (Agent.modifiedClassNames) {
                        for (int i = 0; i < classes.length; i++) {
                            out.writeUTF(classes[i].getName());
                            out.writeBoolean(Agent.modifiedClassNames.contains(classes[i]
                                .getName()));
                        }
                    }
                    out.flush();
                    break;
                case CMD_GET_RUNTIME_INFO :
                    out.writeInt(COMMAND_ACK);
                    out.writeUTF(Agent.rtbean.getBootClassPath());
                    out.writeUTF(Agent.rtbean.getClassPath());
                    writeStringList(out, Agent.rtbean.getInputArguments());
                    out.writeUTF(Agent.rtbean.getLibraryPath());
                    out.writeUTF(Agent.rtbean.getName());
                    out.writeUTF(Agent.rtbean.getVmName());
                    out.writeLong(Agent.rtbean.getStartTime());
                    out.writeLong(Agent.rtbean.getUptime());
                    writeStringMap(out, Agent.rtbean.getSystemProperties());
                    out.flush();
                    break;
                case CMD_GET_MEMORY_INFO :
                    out.writeInt(COMMAND_ACK);
                    writeMemoryUsage(out, Agent.membean.getHeapMemoryUsage());
                    writeMemoryUsage(out, Agent.membean.getNonHeapMemoryUsage());
                    out.writeInt(Agent.membean.getObjectPendingFinalizationCount());
                    out.flush();
                    break;
                case CMD_GET_THREAD_INFO :
                    out.writeInt(COMMAND_ACK);
                    out.flush();
                    long[] ids = (long[]) in.readUnshared();
                    int maxDepth = in.readInt();
                    ids = (ids == null) ? Agent.threadbean.getAllThreadIds() : ids;
                    out.writeUnshared(makeSerializable(Agent.threadbean
                        .getThreadInfo(ids, maxDepth)));
                    out.flush();
                    break;
                case CMD_SET_THREAD_MONITORING :
                    boolean[] flags = (boolean[]) in.readUnshared();
                    boolean[] support = new boolean[]{
                            Agent.threadbean.isThreadContentionMonitoringSupported(),
                            Agent.threadbean.isThreadCpuTimeSupported()};

                    if (support[0]) {
                        Agent.threadbean.setThreadContentionMonitoringEnabled(flags[0]);
                    }
                    if (support[1]) {
                        Agent.threadbean.setThreadCpuTimeEnabled(flags[1]);
                    }
                    out.writeUnshared(support);
                    out.flush();
                    break;
                case CMD_DISCONNECT :
                    out.writeInt(COMMAND_ACK);
                    out.flush();
                    return;
                default :
                    out.writeInt(STATUS_UNKNOWN_CMD);
            }
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // Utility methods
    // ///////////////////////////////////////////////////////////////////////////////////

    public ThreadInfo[] makeSerializable(java.lang.management.ThreadInfo[] mti) {
        ThreadInfo[] ti = new ThreadInfo[mti.length];
        for (int i = 0; i < ti.length; i++) {
            ti[i] = new ThreadInfo(mti[i]);
        }
        return ti;
    }

    public static void writeStringList(ObjectOutputStream out, List<String> list)
        throws IOException {
        System.out.println("writing " + list.size() + " strings");
        out.writeInt(list.size());
        for (String v : list) {
            out.writeUTF(v);
        }
    }

    public static List<String> readStringList(ObjectInputStream in) throws IOException {
        int n = in.readInt();
        System.out.println("reading " + n + " strings");
        List<String> list = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            list.add(in.readUTF());
        }
        return list;
    }

    public static Map<String, String> readStringMap(ObjectInputStream in)
        throws IOException {
        int n = in.readInt();
        Map<String, String> map = new LinkedHashMap<String, String>(n);
        for (int i = 0; i < n; i++) {
            map.put(in.readUTF(), in.readUTF());
        }
        return map;
    }

    public static void writeStringMap(ObjectOutputStream out, Map<String, String> map)
        throws IOException {
        out.writeInt(map.size());
        for (String v : map.keySet()) {
            out.writeUTF(v);
            out.writeUTF(map.get(v));
        }
    }

    public static void writeMemoryUsage(ObjectOutputStream out, MemoryUsage mu)
        throws IOException {
        out.writeLong(mu.getInit());
        out.writeLong(mu.getUsed());
        out.writeLong(mu.getCommitted());
        out.writeLong(mu.getMax());
    }

    public static MemoryUsage readMemoryUsage(ObjectInputStream in) throws IOException {
        return new MemoryUsage(in.readLong(), in.readLong(), in.readLong(), in.readLong());

    }

}
