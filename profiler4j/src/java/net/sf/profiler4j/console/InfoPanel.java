package net.sf.profiler4j.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class InfoPanel extends JPanel {

    private String title = "Title";
    private String description = "Description";

    private JLabel jLabel = null;

    /**
     * This is the default constructor
     */
    public InfoPanel() {
        super();
        initialize();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateInfo();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateInfo();
    }

    private void updateInfo() {
        jLabel.setText("<html><b>" + title + "</b><br>" + description);
    }

    /**
     * This method initializes this
     */
    private void initialize() {
        jLabel = new JLabel();
        jLabel
            .setText("<html><b>Some title</b><br>Some description about this window... feel free to write anithing you want");
        jLabel
            .setBorder(javax.swing.BorderFactory
                .createCompoundBorder(javax.swing.BorderFactory
                                          .createLineBorder(java.awt.SystemColor.controlShadow,
                                                            1),
                                      javax.swing.BorderFactory.createEmptyBorder(8,
                                                                                  8,
                                                                                  8,
                                                                                  8)));
        jLabel.setMinimumSize(new java.awt.Dimension(69, 45));
        jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        jLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        this.setLayout(new BorderLayout());
        this.setSize(230, 46);
        this.add(jLabel, java.awt.BorderLayout.CENTER);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (!isOpaque()) {
            return;
        }
        Color control = new Color(150, 150, 255);
        int width = getWidth();
        int height = getHeight();

        Graphics2D g2 = (Graphics2D) g;

        Paint storedPaint = g2.getPaint();
        g2.setPaint(new GradientPaint(0, 0, Color.white, width, height, control));
        g2.fillRect(0, 0, width, height);
        g2.setPaint(storedPaint);
    }

} // @jve:decl-index=0:visual-constraint="10,10"
