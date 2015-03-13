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
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ClassListTableModel extends AbstractTableModel {

    private String pattern;
    private boolean onlyInstrumented;

    private List<Row> rows = new ArrayList<Row>();
    private List<Row> visibleRows = new ArrayList<Row>();

    public ClassListTableModel() {
        setFilters("**", false);
    }

    public void setFilters(String s, boolean onlyInstrumented) {
        pattern = s.trim();
        this.onlyInstrumented = onlyInstrumented;
    }

    /**
     * @param classes The classes to set.
     */
    public void setClasses(Client.ClassInfo[] classes) {
        rows.clear();
        for (Client.ClassInfo c : classes) {
            Row r = new Row();
            r.info = c;
            r.visible = true;
            rows.add(r);
        }
        refresh();
    }

    /**
     * @return Returns the rows.
     */
    public List<Row> getRows(boolean onlyVisible) {
        if (onlyVisible) {
            return Collections.unmodifiableList(visibleRows);
        }
        return Collections.unmodifiableList(rows);
    }

    public Row getRow(int r) {
        return visibleRows.get(r);
    }

    @Override
    public Class<?> getColumnClass(int c) {
        switch (c) {
            case 0 :
                return Boolean.class;
            case 1 :
                return String.class;
            default :
                throw new IllegalArgumentException();
        }
    }

    public int getRowCount() {
        return (visibleRows == null) ? 0 : visibleRows.size();
    }

    @Override
    public String getColumnName(int c) {
        switch (c) {
            case 0 :
                return "Inst?";
            case 1 :
                return "Class";
            default :
                throw new IllegalArgumentException();
        }
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int r, int c) {
        Row row = visibleRows.get(r);
        switch (c) {
            case 0 :
                return row.info.isInstrumented();
            case 1 :
                return row.info.getName();
            default :
                throw new IllegalArgumentException();
        }
    }

    private void refresh() {
        visibleRows.clear();
        for (Row r : rows) {
            if (r.info.getName().startsWith(pattern)) {
                if (!onlyInstrumented || r.info.isInstrumented()) {
                    r.visible = true;
                    visibleRows.add(r);
                }
            } else {
                r.visible = false;
            }
        }
        Collections.sort(visibleRows);
        fireTableDataChanged();
    }

    public void clear() {
        rows.clear();
        visibleRows.clear();
        fireTableDataChanged();
    }
    
    public static class Row implements Comparable {
        Client.ClassInfo info;
        boolean visible;
        public String getName() {
            return this.info.getName();
        }
        public boolean isVisible() {
            return this.visible;
        }
        public int compareTo(Object o) {
            return this.info.getName().compareTo(((Row) o).info.getName());
        }
    }
}
