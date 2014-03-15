/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

import java.nio.*;

/**
 * HidValueListener specifies the methods used to received HID element values
 * from a HidEventSource. Unlike the <i>HidEventListener</i> interface, this
 * interface does not receive full information about each element, but collects
 * only the value (the <i>HidEvent.mValue</i> field). This is more efficient
 * than using HidEventListener, but does not provide timing information or any
 * HID element "long value" (the <i>HidEvent.mLongValue</i>).
 * 
 * @author Philip DeCamp
 */
public interface HidValueListener {

    /**
     * Callback for incoming HID element values. Note that HidEventSources may
     * reuse ByteBuffers for efficiency:
     * <p>
     * THE BYTE_BUFFER MAY NOT BE STORED!!!
     * <p>
     * Instead, data must be pulled from the ByteBuffer when this method is
     * called or not at all.
     * <p>
     * The number of integer values held in the ByteBuffer may be inferred by
     * the ByteBuffer's limit. For HidEventSources that query multiple elements,
     * the ByteBuffer will always contain a value for each element, whether or
     * not the element has received new data. The order of the values is
     * determined by the order of HidElement objects used to create the
     * HidEventSource.
     * <p>
     * This method does not provide any means of determining which elements have
     * or have not produced new values. If this information is required, you
     * must either create HidEventSources for each element, or use a
     * HidEventListener, which provides a "stale" flag for each element that
     * differentiates new values from old. This method will not be called unless
     * at least one element has been given a new value.
     * 
     * @param buf A read-only ByteBuffer containing a series of INT values.
     */
    public void hidValuesReceived( ByteBuffer buf );

    /**
     * Notifies listener that no more hid events will be recieved from this
     * event source. There are four reasons why this may occur, each of which
     * causes a different Exception to be passed to the listener:
     * <p>
     * 1. The HidEventSource was closed directly. In this case, a
     * HidEventSourceClosedException will be passed to the listener.
     * <p>
     * 2. The device interface was closed. In this case, a
     * HidInterfaceClosedException exception will be passed to the listener.
     * <p>
     * 3. The device has terminated (ie, been unplugged). In this case, a
     * HidTerminatedException will be passed to the listener.
     * <p>
     * 3. An unanticipated errorr occurred which forced the interface to close.
     * In this case, an unspecified Exception object will be passed to the
     * listener that describes the error.
     * 
     * @param ex  Exception indicating why HidEventSource is closing.
     */
    public void hidEventSourceClosing( Exception ex );

}
