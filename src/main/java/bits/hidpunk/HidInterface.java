/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * @author Philip DeCamp
 */
public interface HidInterface {

    /**
     * @return device to which this interface belongs.
     */
    public HidDevice device();
    
    
    /**
     * Closes the interface, after which the interface object can no longer
     * retrieve device data. This call will stop all associated HidEventSources.
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
    public HidEvent eventFor( HidElement el ) throws HidException;

    /**
     * Retrieves the current data from a single element.
     * 
     * @param el   Element that belongs to same device as this interface.
     * @param out  HidEvent in which to store information. May be null.
     * @return HidEvent containing current element values. The return object is
     *         only different from "out" param if "out == null".
     * @throws IllegalArgumentException if element does not belong to same device as interface.
     * @throws HidException if device data cannot be read.
     */
    public HidEvent eventFor( HidElement el, HidEvent out ) throws HidException;

    /**
     * Retrieves the current value for a single element
     * 
     * @param el  Element that belongs to same device as this interface.
     * @return value of that element
     * @throws IllegalArgumentException if the element does not belong to same device as interface.
     * @throws HidException if device element value cannot be retrieve.
     */
    public int valueOf( HidElement el ) throws HidException;

    
    public HidEventSource createPollingSource( long pollMicros,
                                               int queueSize,
                                               HidValueListener callback,
                                               HidElement... elements )
                                               throws HidException;


    public HidEventSource createPollingSource( long pollMicros,
                                               int queueSize,
                                               HidEventListener callback,
                                               HidElement... elements )
                                               throws HidException;


    public HidEventSource createAsyncSource( int queueSize,
                                             HidValueListener callback,
                                             HidElement... elements )
                                             throws HidException;
    

    public HidEventSource createAsyncSource( int queueSize,
                                             HidEventListener callback,
                                             HidElement... elements )
                                             throws HidException;
    
}
