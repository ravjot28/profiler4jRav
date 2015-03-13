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

import javax.swing.SwingUtilities;

/**
 * CLASS_COMMENT
 * 
 * @author Antonio S. R. Gomes
 */
abstract public class LongTask {

    private Throwable error;
    private Object value;
    private LongTaskExecutorDialog dialog;

    /**
     * @param dialog The dialog to set.
     */
    public void setDialog(LongTaskExecutorDialog dialog) {
        this.dialog = dialog;
    }

    /**
     * @param error The error to set.
     */
    public void setError(Throwable error) {
        this.error = error;
    }

    /**
     * @return Returns the error.
     */
    public Throwable getError() {
        return this.error;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    abstract public void executeInBackground() throws Exception;

    public void failed() {

    }

    protected void setTaskProgress(final int percent) {
        //System.err.println(percent);
        if (dialog.getProgressBar().isIndeterminate()) {
            dialog.getProgressBar().setIndeterminate(false);
            dialog.getProgressBar().setMaximum(100);
            dialog.getProgressBar().setStringPainted(true);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.getProgressBar().setValue(percent);
                dialog.getProgressBar().setString(String.valueOf(percent) + "%");
            }
        });
    }
    
    protected void setMessage(final String s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                dialog.setMessage(s);
            }
        });
    }
}
