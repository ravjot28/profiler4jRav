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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.profiler4j.console.Snapshot.Method;

/**
 * CLASS_COMMENT
 * 
 * @author Antonio S. R. Gomes
 */
public class CallGraphPanel extends HoverablePanel {

    private Snapshot snapshot;
    // [ix][iy]
    private MethodView[][] matrix = new MethodView[256][512];
    private List<MethodView> rootNodes;
    private MethodView selectedNode;
    private Map<Method, MethodView> nodes = new HashMap<Method, MethodView>();
    static double maxTime;
    private int[] iyMax = new int[128];
    private int ixMax = 1;

    //
    // Visual attributes
    //
    private Stroke selStroke = new BasicStroke(3f);
    private Stroke linkStroke = new BasicStroke(.15f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_BEVEL);
    private Stroke selLinkStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_BEVEL);
    private Stroke defaultStroke = new BasicStroke(.25f);
    private Color disabledBgColor = Color.decode("#eeeedd");
    private Color disabledFgColor = Color.decode("#ccccbb");

    //
    // Geometry attributes
    //
    private boolean geometryOk;

    static int offsetX = 16;
    static int offsetY = 28;
    static int spacingX = 64;
    static int spacingY = 20;
    static int methodWidth = 150;
    static int methodHeight = 32;
    static int r = 10;

    private List<LinkView> links = new ArrayList<LinkView>();
    private List<LinkView> selectedLinks = new ArrayList<LinkView>();

    private HoverManager hoverManager = new HoverManager();

    private Comparator<MethodView> byNetTimeComparator = new Comparator<MethodView>() {
        public int compare(MethodView o1, MethodView o2) {
            return (int) -Math.signum(o1.method.getNetTime() - o2.method.getNetTime());
        }
    };

    private MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            MethodView n = findNode(e.getX(), e.getY());
            if (n != selectedNode) {
                geometryOk = false;
                selectedNode = n;
                repaint();
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            hoverManager.reset();
        }
    };
    private boolean antialiased = true;

    // //////////////////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////////////////

    public CallGraphPanel() {
        setBackground(Color.decode("#ffffee"));
        Locale.setDefault(Locale.US);
        hoverManager.setContainer(this);
        hoverManager.registerHover(new MethodBoxHoverWindow());
        hoverManager.registerHover(new LinkHoverWindow());
        addMouseListener(mouseAdapter);
    }

    @Override
    public Object findHoverable(int x, int y) {
        MethodView n = findNode(x, y);
        if (n != null) {
            return n;
        }
        for (LinkView lnk : selectedLinks) {
            if (lnk.contains(x, y)) {
                return lnk;
            }
        }
        return null;
    }

    private MethodView findNode(int x, int y) {
        Rectangle r = new Rectangle();
        for (MethodView n : nodes.values()) {
            if (n.visible) {
                r.x = n.x;
                r.y = n.y;
                r.width = n.w;
                r.height = n.h;
                if (r.contains(x, y)) {
                    return n;
                }
            }
        }
        return null;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
        if (snapshot == null) {
            nodes.clear();
            setPreferredSize(new Dimension(320, 300));
            repaint();
            revalidate();
        } else {
            refresh();
        }
    }

    @Override
    public void paint(Graphics g_) {
        super.paint(g_);
        Graphics2D g = (Graphics2D) g_;
        g.setStroke(defaultStroke);
        if (antialiased) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        }
        drawMethods(g);
    }

    // //////////////////////////////////////////////////////////////////////////
    // Helper methods
    // //////////////////////////////////////////////////////////////////////////

    private void drawMethods(Graphics2D g) {
        if (!geometryOk) {
            computeGeometry();
            geometryOk = true;
        }
        FontMetrics fm = getFontMetrics(getFont());
        //
        // Draw unselected links
        //
        g.setColor(Color.BLACK);
        g.setStroke(linkStroke);
        for (LinkView l : links) {
            l.paint(g);
        }
        //
        // Draw unselected methods
        //
        for (MethodView n : nodes.values()) {
            if (n == selectedNode || !n.visible) {
                continue;
            }
            g.setColor((n.visible) ? n.getColor() : disabledBgColor);
            g.fillRoundRect(n.x, n.y, n.w, n.h, r, r);
            g.setColor((n.visible) ? Color.BLACK : disabledFgColor);
            g.drawRoundRect(n.x, n.y, n.w, n.h, r, r);
            g.drawString(n.method.getMethodName(), n.x + 5, n.y + fm.getHeight());
            g.drawString(makeDetailText(n), n.x + 5, n.y + fm.getHeight() * 2);
        }

        MethodView n = this.selectedNode;
        if (n != null) {
            //
            // Draw selected method
            //
            g.setColor(n.getColor());
            g.setStroke(selStroke);
            g.fillRoundRect(n.x, n.y, n.w, n.h, r, r);
            g.setColor(Color.BLUE);
            g.drawRoundRect(n.x, n.y, n.w, n.h, r, r);
            g.setColor(Color.BLACK);
            g.drawString(n.method.getMethodName(), n.x + 5, n.y + fm.getHeight());
            g.drawString(makeDetailText(n), n.x + 5, n.y + fm.getHeight() * 2);
            g.setStroke(defaultStroke);
            //
            // Draw selected links
            //
            g.setColor(Color.BLUE);
            g.setStroke(selLinkStroke);
            g.setColor(Color.BLUE);
            g.setStroke(selLinkStroke);
            for (LinkView l : selectedLinks) {
                l.paint(g);
            }
        }
    }

    /**
     * @param n
     * @return formatted detail
     */
    private String makeDetailText(MethodView n) {
        return String.format("N:%.1f AN:%.3f H:%d", n.method.getNetTime(), n.method
            .getNetTime()
                / n.method.getHits(), n.method.getHits());
    }

    private void computeGeometry() {
        //
        // Compute method boxes
        //
        for (MethodView n : nodes.values()) {
            n.x = n.ix * (methodWidth + spacingX) + offsetX;
            n.y = n.iy * (methodHeight + spacingY) + offsetY;
            n.w = methodWidth;
            n.h = methodHeight;
        }
        links.clear();
        selectedLinks.clear();
        //
        // Compute links segments
        //
        for (MethodView src : nodes.values()) {
            if (!src.visible) {
                continue;
            }
            for (MethodView dst : src.children) {
                if (!dst.visible) {
                    continue;
                }
                LinkView link = new LinkView(src, dst);
                if (selectedNode == src || selectedNode == dst) {
                    selectedLinks.add(link);
                } else {
                    links.add(link);
                }
            }
        }
    }

    private void refresh() {
        geometryOk = false;
        selectedNode = null;
        createNodes();
        layNodes(rootNodes, 0, 0);
        applyNCut(40);
    }

    private void layNodes(List<MethodView> nodeList, int level, int y) {
        for (int i = 0; i < iyMax.length; i++) {
            iyMax[i] = 0;
        }
        ixMax = 1;
        Collections.sort(nodeList, byNetTimeComparator);

        // System.out.println("-------");
        for (MethodView rootNode : nodeList) {
            rootNode.ix = level;
            rootNode.iy = iyMax[0]++;
            rootNode.visible = true;
            // dumpNode(rootNode);
            matrix[rootNode.ix][rootNode.iy] = rootNode;
            lay(1, rootNode.children);
        }
        level++;

        int ymax_ = -1;
        for (int i = 0; i < iyMax.length; i++) {
            ymax_ = Math.max(iyMax[i], ymax_);
        }

        //
        // Sort columns
        //

        List<MethodView> colNodes = new ArrayList<MethodView>();
        for (int ix = 0; ix < ixMax; ix++) {
            colNodes.clear();
            for (int iy = 0; iy < iyMax[ix]; iy++) {
                colNodes.add(matrix[ix][iy]);
            }
            Collections.sort(colNodes, byNetTimeComparator);
            int iy = 0;
            // System.out.format("Column #%d\n", ix);
            for (MethodView n : colNodes) {
                matrix[ix][iy] = n;
                n.iy = iy;
                // System.out.format(" cell #%d (time %.0f ms)\n", iy,
                // n.method.getNetTime());
                iy++;
            }
        }

        // Random rnd = new Random();
        // long[] bestSeed = new long[ixMax];
        // long seed = 0;
        // for (int ix = 1; ix < ixMax; ix++) {
        // double minCost = Double.MAX_VALUE;
        // for (int k = 0; k < 100; k++) {
        // double cost = 0;
        // seed = System.nanoTime();
        // rnd.setSeed(seed);
        // for (int iy = 0; iy < iyMax[ix] / 2; iy++) {
        // int nextInt = rnd.nextInt(iyMax[ix] / 2);
        // swapY(ix, iy, nextInt);
        // }
        // for (int iy = 0; iy < iyMax[ix-1] / 2; iy++) {
        // MethodView n = matrix[ix-1][iy];
        // for (MethodView cn : matrix[ix-1][iy].children) {
        // int d = Math.abs(n.iy - cn.iy);
        // cost += d * d * d * n.method.getNetTime()
        // * (1 + iy);
        // }
        // }
        // if (cost < minCost) {
        // minCost = cost;
        // bestSeed[ix] = seed;
        // System.out.println("cost = " + cost);
        // }
        // }
        //
        // }
        //
        // for (int ix = 1; ix < ixMax; ix++) {
        // rnd.setSeed(bestSeed[ix]);
        // for (int iy = 0; iy < iyMax[ix] / 2; iy++) {
        // swapY(ix, iy, rnd.nextInt(iyMax[ix] / 2));
        // }
        // }

        setPreferredSize(new Dimension(ixMax * (methodWidth + spacingX) + offsetX, ymax_
                * (methodHeight + spacingY) + offsetY));
        repaint();
        revalidate();
    }

    private void swapY(int x, int y1, int y2) {
        MethodView aux = matrix[x][y1];
        matrix[x][y1] = matrix[x][y2];
        matrix[x][y2] = aux;
        int yAux = matrix[x][y1].iy;
        matrix[x][y1].iy = matrix[x][y2].iy;
        matrix[x][y2].iy = yAux;
    }

    @SuppressWarnings("unused")
    private void dumpNode(MethodView rootNode) {
        System.out.format("%23s: ix=%2d iy=%2d\n",
                          rootNode.method.getMethodName(),
                          rootNode.ix,
                          rootNode.iy);
    }

    private void lay(int ix, List<MethodView> nodes) {
        for (MethodView n : nodes) {
            if (n.visible) {
                continue;
            }
            n.ix = ix;
            n.iy = iyMax[ix]++;
            n.visible = true;
            // dumpNode(n);
            matrix[n.ix][n.iy] = n;
            lay(ix + 1, n.children);
        }
        ixMax = Math.max(ixMax, ix);
    }

    public void applyNCut(int n) {
        // System.out.println("NCUT(" + n + ")");
        List<MethodView> aux = new ArrayList<MethodView>(nodes.values());
        Collections.sort(aux, byNetTimeComparator);
        int ncut = n;
        for (MethodView node : aux) {
            ncut--;
            node.visible = ncut >= 0;
            // dumpNode(node);
        }
        if (selectedNode != null && !selectedNode.visible) {
            selectedNode = null;
        }
        geometryOk = false;
    }

    private void createNodes() {
        maxTime = -1;
        nodes.clear();
        if (snapshot == null) {
            return;
        }
        for (Method m : snapshot.getMethods().values()) {
            MethodView n = new MethodView();
            n.method = m;
            maxTime = Math.max(maxTime, m.getNetTime());
            nodes.put(m, n);
        }
        for (MethodView node : nodes.values()) {
            for (Method m : node.method.getChildrenTimes().keySet()) {
                node.children.add(nodes.get(m));
            }
        }
        findRootNodes();
    }

    private void findRootNodes() {
        for (MethodView node : nodes.values()) {
            for (Method childMethod : node.method.getChildrenTimes().keySet()) {
                if (childMethod != node.method) {
                    nodes.get(childMethod).root = false;
                }
            }
        }
        rootNodes = new ArrayList<MethodView>();
        for (MethodView node : nodes.values()) {
            if (node.root) {
                rootNodes.add(node);
                // System.out.println("root " + node.method.getName());
            }
        }
    }

    private static int k = 70;

}

class MethodView {

    Method method;
    List<MethodView> children = new ArrayList<MethodView>();

    int x;
    int y;
    int w;
    int h;
    Color c;

    int ix;
    int iy;

    boolean visible;
    boolean root = true;

    Color getColor() {
        if (c == null) {
            int k = (int) ((method.getNetTime() / CallGraphPanel.maxTime) * 192);
            k = (k < 0) ? 0 : ((k > 255) ? 255 : k);
            c = new Color(255, 255 - k, 255 - k);
        }
        return c;
    }

    @Override
    public boolean equals(Object obj) {
        return method.getId() == ((MethodView) obj).method.getId();
    }

    @Override
    public int hashCode() {
        return method.getId();
    }
}

class LinkView {

    private static double tolerance = 4;

    protected Point2D.Double[] n;
    protected GeneralPath path = new GeneralPath();
    private MethodView src;
    private MethodView dst;

    public LinkView(MethodView src, MethodView dst) {
        this.src = src;
        this.dst = dst;
        updateShape();
    }

    public MethodView getDst() {
        return this.dst;
    }

    public MethodView getSrc() {
        return this.src;
    }

    private double k = 10;

    public void updateShape() {
        path.reset();

        if (src.x >= dst.x) {
            if (src.y == dst.y) {
                k = CallGraphPanel.spacingX * .3;
                n = new Point2D.Double[6];
                n[0] = new Point2D.Double(src.x + CallGraphPanel.methodWidth, src.y
                        + CallGraphPanel.methodHeight / 2);
                n[5] = new Point2D.Double(dst.x, dst.y + CallGraphPanel.methodHeight / 2);
                n[1] = new Point2D.Double(n[0].x + k, n[0].y);
                n[2] = new Point2D.Double(n[0].x + k, n[0].y - k * 2);
                n[3] = new Point2D.Double(n[5].x - k, n[5].y - k * 2);
                n[4] = new Point2D.Double(n[5].x - k, n[5].y);
            } else if (src.y < dst.y) {
                k = CallGraphPanel.spacingX * .3;
                n = new Point2D.Double[6];
                n[0] = new Point2D.Double(src.x + CallGraphPanel.methodWidth, src.y
                        + CallGraphPanel.methodHeight / 2);
                n[5] = new Point2D.Double(dst.x, dst.y + CallGraphPanel.methodHeight / 2);
                n[1] = new Point2D.Double(n[0].x + k, n[0].y);
                n[2] = new Point2D.Double(n[0].x + k, n[0].y + k * 2);
                n[3] = new Point2D.Double(n[5].x - k, n[5].y - k * 2);
                n[4] = new Point2D.Double(n[5].x - k, n[5].y);
            } else {
                k = CallGraphPanel.spacingX * .3;
                n = new Point2D.Double[6];
                n[0] = new Point2D.Double(src.x + CallGraphPanel.methodWidth, src.y
                        + CallGraphPanel.methodHeight / 2);
                n[5] = new Point2D.Double(dst.x, dst.y + CallGraphPanel.methodHeight / 2);
                n[1] = new Point2D.Double(n[0].x + k, n[0].y);
                n[2] = new Point2D.Double(n[0].x + k, n[0].y - k * 2);
                n[3] = new Point2D.Double(n[5].x - k, n[5].y + k * 2);
                n[4] = new Point2D.Double(n[5].x - k, n[5].y);
            }
        } else {
            n = new Point2D.Double[4];
            n[0] = new Point2D.Double(src.x + CallGraphPanel.methodWidth, src.y
                    + CallGraphPanel.methodHeight / 2);
            n[3] = new Point2D.Double(dst.x, dst.y + CallGraphPanel.methodHeight / 2);
            k = CallGraphPanel.spacingX * .3;
            n[1] = new Point2D.Double(n[0].x + k, n[0].y);
            n[2] = new Point2D.Double(n[3].x - k, n[3].y);
        }

        Point2D.Double cp1;
        Point2D.Double cp2;
        Point2D.Double p;
        path.moveTo((float) n[0].x, (float) n[0].y);
        int i = 1;
        for (i = 1; i < (n.length - 1); i++) {
            cp1 = n[i];
            cp2 = n[i];
            p = midPoint(n[i], n[i + 1]);
            path.curveTo((float) cp1.x,
                         (float) cp1.y,
                         (float) cp2.x,
                         (float) cp2.y,
                         (float) p.x,
                         (float) p.y);
        }
        path.lineTo((float) n[i].x, (float) n[i].y);
    }

    public boolean contains(int x, int y) {
        if (n != null) {
            Line2D seg = new Line2D.Double();
            for (int i = 0; i < (n.length - 1); i++) {
                seg.setLine(n[i].x, n[i].y, n[i + 1].x, n[i + 1].y);
                if (seg.ptSegDist(x, y) <= tolerance) {
                    return true;
                }

            }
        }
        return false;
    }

    public void paint(Graphics2D g) {
        g.draw(path);
    }

    private static Point2D.Double midPoint(Point2D.Double p1, Point2D.Double p2) {
        return new Point2D.Double((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
    }

}
