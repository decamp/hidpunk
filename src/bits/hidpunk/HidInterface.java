package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public interface HidInterface {
    
    /**
     * @return device to which this interface belongs.
     */
    public HidDevice getDevice(); 
    
    /**
     * Closes the interface, after which the interface object can no longer 
     * retrieve device data.  This call will stop all associated HidEventSources.
     */
    public void close() throws HidException;
    
    /**
     * @return true iff interface is open
     */
    public boolean isOpen();

    /**
     * Retrieves the current data from a single element.
     * 
     * @param el Element that belongs to same device as this interface.
     * @throws IllegalArgumentException if element is from a different interface.
     * @throws HidException if device data cannot be read.
     */
    public HidEvent getElementEvent(HidElement el) throws HidException;
    
    /**
     * Retrieves the current data from a single element.
     * 
     * @param el Element that belongs to same device as this interface.
     * @param out HidEvent in which to store information.
     * @return HidEvent containing current element values.  The return object is only different from "out" param if "out == null".
     * @throws IllegalArgumentException if element does not belong to same device as interface.
     * @throws HidException if device data cannot be read.
     */
    public HidEvent getElementEvent(HidElement el, HidEvent out) throws HidException;
    
    /**
     * Retrieves the current value for a single element
     * 
     * @param el Element that belongs to same device as this interface.
     * @retrn value of that element
     * @throws IllegalArgumentException if the element does not belong to same device as interface.
     * @throws HidException if device element value cannot be retrieve.
     */
    public int getElementValue(HidElement el) throws HidException;

    
    public HidEventSource newPollingSource( long pollMicros, 
                                            int queueSize, 
                                            HidValueListener callback, 
                                            HidElement... elements)
                                            throws HidException;
    
    
    public HidEventSource newPollingSource( long pollMicros, 
                                            int queueSize, 
                                            HidEventListener callback, 
                                            HidElement... elements) 
                                            throws HidException;
    
    
    public HidEventSource newAsyncSource( int queueSize, 
                                          HidValueListener callback, 
                                          HidElement... elements) 
                                          throws HidException;
    
    
    public HidEventSource newAsyncSource( int queueSize, 
                                          HidEventListener callback, 
                                          HidElement... elements) 
                                          throws HidException;
    
}
