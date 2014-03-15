/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/** 
 * Yes, the name HidDevice is redundant.
 * 
 * @author Philip DeCamp  
 */
public interface HidDevice {
    
    public int usagePage();
    public int usage();
    public String transform();
    public String vendor();
    public int vendorId();
    public String product();
    public int productId();
    public int version();
    public String serial();
    public int locationId();
    public int elementCount();
    public HidElement element( int idx );
    public HidElement[] elements();
    public HidElement[] flattenElements();

    /**
     * @param listener Listener to receive termination events.
     * @return false iff device is already terminated.
     */
    public boolean addTerminationListener( HidTerminationListener listener );
    
    /**
     * Like addTerminationListener(), but uses weak references to store listener.
     * 
     * @param listener Listener to receive termination events.
     * @return false iff device is already terminated.
     */
    public boolean addTerminationListenerWeakly( HidTerminationListener listener );

    
    public void removeTerminationListener( HidTerminationListener listener );
    
    
    public HidInterface openInterface() throws HidException;
    
    
    public String getDescription();

}
