package bits.hidpunk;

/** 
 * HidRemovalListener is used to received HID removal notifications.
 * 
 * @author Philip DeCamp  
 */
public interface HidTerminationListener {
    
    /**
     * @param device A HidDevice that is no longer accessible.
     */
    public void hidTerminated(HidDevice device);

}
