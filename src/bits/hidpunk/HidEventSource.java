package bits.hidpunk;

import java.util.List;

/** 
 * @author Philip DeCamp  
 */
public interface HidEventSource {

    public HidDevice getDevice();
    public HidInterface getInterface();
    public List<HidElement> getElements();
    public boolean hasElement(HidElement el);
    public boolean isAsync();
    public boolean isPolling();
    public boolean isQueued();
    public long getPollingMicros();
    
    public void start() throws HidException;
    public void stop() throws HidException;
    public void close() throws HidException;
    
    public boolean isOpen();
    public boolean isRunning();
    
}
