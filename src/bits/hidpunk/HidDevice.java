package bits.hidpunk;

/** 
 * Yes, the name HidDevice is redundant.
 * 
 * @author Philip DeCamp  
 */
public interface HidDevice {
    
    public int getUsagePage();
    public int getUsage();
    public String getTransport();
    public String getVendor();
    public int getVendorID();
    public String getProduct();
    public int getProductID();
    public int getVersion();
    public String getSerial();
    public int getLocationID();
    public int getElementCount();
    public HidElement getElement(int idx);
    public HidElement[] getElements();
    public HidElement[] flattenElements();

    /**
     * @param listener Listener to receive termination events.
     * @return false iff device is already terminated.
     */
    public boolean addTerminationListener(HidTerminationListener listener);
    
    /**
     * Like addTerminationListener(), but uses weak references to store listener.
     * 
     * @param listener Listener to receive termination events.
     * @return false iff device is already terminated.
     */
    public boolean addTerminationListenerWeakly(HidTerminationListener listener);

    
    public void removeTerminationListener(HidTerminationListener listener);
    
    
    public HidInterface openInterface() throws HidException;
    
    
    public String getDescription();

}
