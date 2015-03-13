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

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import net.sf.profiler4j.console.Rule.Action;

/**
 * Table model for rule editing
 * 
 * @author Antonio S. R. Gomes
 */
public class RuleTableModel extends AbstractTableModel {

    private List<Rule> rules = new ArrayList<Rule>();

    public void insert(int r, Rule rule) {
        if (r == -1) {
            rules.add(rule);
        } else {
            rules.add(r, rule);
        }
        fireTableDataChanged();
    }

    /**
     * @return Returns the rules (do not modify!!!).
     */
    public List<Rule> getRules() {
        return this.rules;
    }
    
    public void insert(Rule rule) {
        rules.add(rule);
        fireTableDataChanged();
    }

    public void remove(int r) {
        rules.remove(r);
        fireTableDataChanged();
    }

    public void moveUp(int r) {
        if (r == 0) {
            return;
        }
        Rule rule = rules.remove(r);
        rules.add(r - 1, rule);
        fireTableDataChanged();
    }

    public void moveDown(int r) {
        if ((r + 1) >= rules.size()) {
            return;
        }
        Rule rule = rules.remove(r);
        rules.add(r + 1, rule);
        fireTableDataChanged();
    }

    public RuleTableModel() {
    }

    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case 0 :
                return String.class;
            case 1 :
                return Action.class;
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String getColumnName(int c) {
        switch (c) {
            case 0 :
                return "Method Pattern";
            case 1 :
                return "Action";
            default :
                throw new IllegalArgumentException();
        }
    }

    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return rules.size();
    }

    public Object getValueAt(int r, int c) {
        Rule rule = rules.get(r);
        switch (c) {
            case 0 :
                return rule.getPattern();
            case 1 :
                return rule.getAction();
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void setValueAt(Object v, int r, int c) {
        Rule rule = rules.get(r);
        switch (c) {
            case 0 :
                rule.setPattern((String) v);
                fireTableCellUpdated(r, c);
                break;
            case 1 :
                rule.setAction((Action) v);
                fireTableCellUpdated(r, c);
                break;
            default :
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return true;
    }
}
