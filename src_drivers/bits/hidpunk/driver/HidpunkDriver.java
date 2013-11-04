package bits.hidpunk.driver;

import java.util.concurrent.Executor;

import bits.event.*;
import bits.hidpunk.*;

/** 
 * @author Philip DeCamp  
 */
public interface HidpunkDriver<L> extends EventSource<L> {

    public static int THREADING_AWT = EventCaster.THREADING_AWT;
    public static int THREADING_DEDICATED = EventCaster.THREADING_DEDICATED;
    public static int THREADING_SYNCHRONOUS = EventCaster.THREADING_SYNCHRONOUS;
    
    public void addListener(L listener);
    public void addListenerWeakly(L listener);
    public void removeListener(L listener);

    
    /**
     * Starts device using default threading strategy for passing device events.
     * 
     * @return true if successful, false if device already running
     */
    public boolean start() throws HidException;
    
    /**
     * Starts device using specified threading strategy for passing device events.
     * 
     * @param threading One of the threading constants that specifies threading strategy.
     * @return true if successful, false if device already running.
     */
    public boolean start(int threading) throws HidException;

    /**
     * Starts device using specified executor to pass device events.
     * 
     * @param executor Executor to use for message passing.
     * @return true if successful, false if device already running.
     */
    public boolean start(Executor executor) throws HidException;

    /**
     * Starts device with a single listener.  Starting the device with this
     * method will override any calls made to addListener(), addListenerWeakly(),
     * and removeListener() (although those listeners are still stored and
     * may be used if the device is restarted later with a different start
     * method.
     * <p>
     * Calls to the privateListener will be made synchronously, using the same
     * thread that accesses the device through the native drivers.
     * 
     * @param singleListener The one listener that will receive calls from this device.
     * @return true if successful, false if device already running. 
     */
    public boolean startPrivately(L privateListener) throws HidException;

    /**
     * Stops the device, causing it to cease outputing data.  Note that devices
     * some devices may allow the device to be restarted.
     * 
     * @return true iff succesful, false if not running.
     */
    public boolean stop() throws HidException;
    
}
