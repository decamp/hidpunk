package bits.hidpunk;

/** 
 * HidEventListener specifies the methods used to receive HID data from a
 * HidEventSource, and to detect when the HidEvents will no longer be received.
 * 
 * @author Philip DeCamp  
 */
public interface HidEventListener {
    
    /**
     * Callback for incoming HID element events.  Note that HidInterfaces may
     * reuse HidEvent objects for efficiency.
     * <p>
     * THE HIDEVENT OBJECTS MAY NOT BE STORED!!!
     * <p>
     * Instead, data must be pulled from the events by the calling thread, or 
     * not at all.  Furthermore, the ByteBuffer objects within each event
     * are also reused when possible, so do not link to those either.
     * <p>
     * A HID event is provided for every HID element that is managed by the
     * HidEventSource.  The HidEvent objects are ordered to correspond to 
     * the order of HID elements passed to the HidEventSource when created.
     * <p>
     * For HidEventSources that manage multiple elements, the number of 
     * HidEvents held in the <i>events</i> array is always equal to the number 
     * of managed elements.  The order of the events will match the order of the
     * HidElements as given to the HidEventSource at its instantiation.
     * <p>
     * This method will only be called if at least one HID element has produced
     * new data.  The HidEvents that have been updated will have <tt>false</tt>
     * in the <tt>mStale</tt> field.  The HidEvents that have not been updated
     * will have their stale flags set to <tt>true</tt>, and will otherwise be
     * untouched from previous calls.
     * 
     * @param events Array of HID events.
     */
    public void hidEventsReceived(HidEvent[] events);
        
    /**
     * Notifies listener that no more hid events will be recieved from this
     * event source.  There are four reasons why this may occur, each of which
     * causes a different Exception to be passed to the listener:
     * <p>
     * 1. The HidEventSource was closed directly.  In this case, a 
     * HidEventSourceClosedException will be passed to the listener.
     * <p>
     * 2. The device interface was closed. In this case, a 
     * HidInterfaceClosedException exception will be passed to the listener.
     * <p>
     * 3. The device has terminated (ie, been unplugged). In this case, 
     * a HidTerminatedException will be passed to the listener.
     * <p>
     * 4. An unanticipated errorr occurred which forced the interface to close. 
     * In this case, an unspecified Exception object will be passed to the
     * listener that describes the error. 
     * <p>
     * @param ex Exception indicating why HidEventSource is closing.
     */
    public void hidEventSourceClosing(Exception ex);
        
}
