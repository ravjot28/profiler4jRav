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
package net.sf.profiler4j.console.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

/**
 * Utility class that implements a simple JComponent validator.
 *
 * @author Antonio S. R. Gomes
 */
public class Validator {

    private Component parent;
    private List<Rule> validations = new ArrayList<Rule>();
    private Map<JComponent, ValidationError> errors = new LinkedHashMap<JComponent, ValidationError>();

    private Map<JComponent, Color> restoreColors = new LinkedHashMap<JComponent, Color>();
    private Map<JComponent, String> restoreTooltips = new LinkedHashMap<JComponent, String>();

    public Validator(Component parent) {
        this.parent = parent;
    }

    /**
     * Performs the validation of all registered fields.
     * @param focusFirstError
     * @return <code>true</code> if all fields are valid.
     */
    public boolean validate(boolean focusFirstError) {

        restore();
        errors.clear();
        for (Rule rule : validations) {
            rule.validate();
        }
        if (errors.isEmpty()) {
            return true;
        }
        for (ValidationError ve : errors.values()) {
            hightlight(ve.getComponent(), ve.getReason());
        }
        if (focusFirstError && !errors.isEmpty()) {
            errors.keySet().iterator().next().requestFocus();
        }
        return false;
    }

    private MouseListener ml = new MouseAdapter() {
        Color old = null;
        int initialDelay;

        @Override
        public void mouseEntered(MouseEvent e) {
            JComponent c = (JComponent) e.getComponent();
            if (errors.containsKey(c)) {
                old = UIManager.getColor("ToolTip.background");
                initialDelay = ToolTipManager.sharedInstance().getInitialDelay();
                UIManager.put("ToolTip.background", Color.YELLOW);
                ToolTipManager.sharedInstance().setInitialDelay(10);
            }
        }
        @Override
        public void mouseExited(MouseEvent e) {
            if (old != null) {
                UIManager.put("ToolTip.background", old);
                old = null;
                ToolTipManager.sharedInstance().setInitialDelay(initialDelay);
            }
        }
    };

    private void hightlight(JComponent c, String s) {
        if (!restoreColors.containsKey(c)) {
            restoreColors.put(c, c.getBackground());
            c.setBackground(Color.decode("#ffaaaa"));
            restoreTooltips.put(c, c.getToolTipText());
            c.setToolTipText(s);
            c.addMouseListener(ml);
        }
    }
    private void restore() {
        for (JComponent c : restoreColors.keySet()) {
            c.setBackground(restoreColors.get(c));
            c.setToolTipText(restoreTooltips.get(c));
            c.removeMouseListener(ml);
        }
        restoreColors.clear();
        restoreTooltips.clear();
    }

    private void saveError(JComponent c, String message) {
        if (!errors.containsKey(c)) {
            errors.put(c, new ValidationError(c, message));
        }
    }

    private static String norm(String s) {
        return (s == null) ? "" : s;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Inner classes
    // //////////////////////////////////////////////////////////////////////////

    abstract interface Rule {
        void validate();
    }

    // //////////////////////////////////////////////////////////////////////////
    // Factory Methos
    // //////////////////////////////////////////////////////////////////////////

    public void newNonEmpty(final JTextComponent c) {
        validations.add(new Rule() {
            public void validate() {
                if (isEmpty(c.getText())) {
                    saveError(c, "Value cannot be empty");
                }
            }
        });
    }

    public void newNonEmpty(final JComboBox c) {
        validations.add(new Rule() {
            public void validate() {
                if (c.getSelectedIndex() == -1) {
                    saveError(c, "Value cannot be empty");
                }
            }
        });
    }

    public void newInteger(final JTextComponent c, final int min, final int max) {
        validations.add(new Rule() {
            public void validate() {
                String s = c.getText();
                if (isEmpty(s)) {
                    return;
                }
                int v;
                try {
                    v = Integer.parseInt(s);
                    if (v < min || v > max) {
                        saveError(c, "Value must in the range [" + min + ";" + max + "]");
                    }
                } catch (NumberFormatException nfe) {
                    saveError(c, "Value must be a valid integer");
                }

            }
        });
    }

    public void newEmail(final JTextComponent c) {
        validations.add(new Rule() {
            public void validate() {
                String s = c.getText();
                if (isEmpty(s)) {
                    return;
                }
                if (!Pattern.matches("^[a-zA-Z0-9\\+\\.\\_\\-]+"
                        + "@[a-zA-Z0-9\\.\\-\\_]+$", s)) {
                    saveError(c, "Value must be an e-mail");
                }
            }
        });
    }

    public void newSameText(final JTextComponent c1, final JTextComponent c2) {
        validations.add(new Rule() {
            public void validate() {
                String s1 = norm(c1.getText());
                String s2 = norm(c2.getText());
                if (!s1.equals(s2)) {
                    saveError(c2, "Value must be the same as before");
                }
            }
        });
    }

    public void newId(final JTextComponent c) {
        validations.add(new Rule() {
            public void validate() {
                String s = c.getText();
                if (isEmpty(s)) {
                    return;
                }
                if (!Pattern.matches("^[a-zA-Z0-9\\_]+[a-zA-Z0-9\\_]*$", s)) {
                    saveError(c, "Value must be an identifier");
                }
            }
        });
    }

    public void newLengthRange(final JTextComponent c,
                               final int minLengh,
                               final int maxLengh) {
        validations.add(new Rule() {
            public void validate() {
                String s = c.getText();
                if (isEmpty(s)) {
                    return;
                }
                if (s.length() < minLengh || s.length() > maxLengh) {
                    saveError(c, "Text length is out of range (min=" + minLengh
                            + "; max=" + maxLengh + ")");
                }
            }
        });
    }

    public void newRegex(final JTextComponent c, final String regex) {
        validations.add(new Rule() {
            public void validate() {
                String s = c.getText();
                if (isEmpty(s)) {
                    return;
                }
                if (!Pattern.matches(regex, s)) {
                    saveError(c, "Value is not valid: " + regex);
                }
            }
        });
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

}
