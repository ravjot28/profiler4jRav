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

/**
 * Class that represents an application event.
 *
 * @author Antonio S. R. Gomes
 */
public class AppEvent {

    private AppEventType type;
    private Object arg;
    
    public AppEvent(AppEventType type) {
        this.type = type;
    }
    
    public AppEvent(AppEventType type, Object arg) {
        this.type = type;
        this.arg = arg;
    }    
    /**
     * @return Returns the type.
     */
    public AppEventType getType() {
        return this.type;
    }
    /**
     * @return Returns the arg.
     */
    public Object getArg() {
        return this.arg;
    }
}
