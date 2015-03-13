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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

/**
 * @author Antonio S. R. Gomes
 */
public class HoverManager implements MouseMotionListener, ActionListener {

    private int targetX;
    private int targetY;
    private HoverWindow activeHover;
    private HoverablePanel panel;
    private List<HoverWindow> hovers = new ArrayList<HoverWindow>();
    private Timer timer = new Timer(400, this);

    public void registerHover(HoverWindow h) {
        hovers.add(h);
    }

    public void actionPerformed(ActionEvent e) {
        timer.stop();
        Point p = panel.getLocationOnScreen();
        int screenX = p.x + targetX + 16;
        int screenY = p.y + targetY+ 16;
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension sdim = tk.getScreenSize();
        int correctionX = Math.max(screenX + activeHover.getWidth() - sdim.width + 36, 0);
        int correctionY = Math.max(screenY + activeHover.getHeight() - sdim.height + 42, 0);
        activeHover.setLocation(screenX - correctionX, screenY - correctionY);
        activeHover.setVisible(true);
    }

    public void setContainer(HoverablePanel panel) {
        if (this.panel != null) {
            this.panel.removeMouseMotionListener(this);
        }
        this.panel = panel;
        this.panel.addMouseMotionListener(this);
        if (activeHover != null) {
            activeHover.setVisible(false);
        }
        activeHover = null;
    }

    public void mouseDragged(MouseEvent e) {
        // empty
    }

    public void mouseMoved(MouseEvent e) {
        disableHover();
        Object element = panel.findHoverable(e.getX(), e.getY());
        if (element != null) {
            for (HoverWindow hw : hovers) {
                if (hw.supportsElement(element)) {
                    activeHover = hw;
                    targetX = e.getX();
                    targetY = e.getY();
                    hw.prepareForElement(element);
                    timer.start();
                    break;
                }
            }
        }
    }

    private void disableHover() {
        timer.stop();
        if (activeHover != null && activeHover.isVisible()) {
            activeHover.setVisible(false);
        }
        activeHover = null;
    }

    public void reset() {
        disableHover();
    }

}