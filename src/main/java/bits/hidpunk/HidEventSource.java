/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

import java.util.List;

/** 
 * @author Philip DeCamp  
 */
public interface HidEventSource {

    public HidDevice device();
    public HidInterface hidInterface();
    public List<HidElement> elements();
    public boolean hasElement( HidElement el );
    public boolean isAsync();
    public boolean isPolling();
    public boolean isQueued();
    public long pollingMicros();
    
    public void start() throws HidException;
    public void stop() throws HidException;
    public void close() throws HidException;
    
    public boolean isOpen();
    public boolean isRunning();
    
}
