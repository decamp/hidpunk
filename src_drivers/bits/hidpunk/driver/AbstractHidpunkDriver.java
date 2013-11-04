package bits.hidpunk.driver;

import bits.data.SemiWeakHashSet;
import bits.event.*;
import bits.hidpunk.*;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.logging.Logger;


/** 
 * @author Philip DeCamp  
 */
public abstract class AbstractHidpunkDriver<L> implements HidpunkDriver<L> {

    
    protected static List<HidDevice> findHidDevices(Long vendorID, Long productID) throws HidException {
        HidManager manager = HidManager.getManager();
        HidMatcher matcher = manager.newDeviceMatcher();
        
        if(vendorID != null)
            matcher.setVendorID(vendorID);
        
        if(productID != null)
            matcher.setProductID(productID);
            
        return manager.findDevices(matcher);
    }
    
     
    protected final Logger mLogger = Logger.getLogger(getClass().getName());
    protected final HidDevice mDevice;
    protected final Class<L> mListenerClass; 
    private final SemiWeakHashSet<L> mListeners = new SemiWeakHashSet<L>();

    private EventCaster<L> mCaster = null;
    private HidInterface mInterface = null;
    
    
    protected AbstractHidpunkDriver(HidDevice device, Class<L> listenerClass) {
        mDevice = device;
        mListenerClass = listenerClass;
    }

    
    
    public HidDevice getDevice() {
        return mDevice;
    }
    
    
    
    public synchronized void addListener(L listener) {
        mListeners.add(listener);
        
        if(mCaster != null)
            mCaster.addListener(listener);
    }
 
    
    public synchronized void addListenerWeakly(L listener) {
        mListeners.addWeakly(listener);
        
        if(mCaster != null)
            mCaster.addListenerWeakly(listener);
    }

    
    public synchronized void removeListener(L listener) {
        mListeners.remove(listener);
        
        if(mCaster != null)
            mCaster.removeListener(listener);
    }  

    

    public synchronized boolean start() throws HidException {
        return start(THREADING_AWT);
    }
    
    
    public synchronized boolean start(int threading) throws HidException {
        if(mInterface != null)
            return false;

        EventCaster<L> caster = EventCaster.newInstance( mListenerClass, threading );
        return doStart(caster);
    }
    
    
    public synchronized boolean start(Executor executor) throws HidException {
        if(mInterface != null)
            return false;
        
        EventCaster<L> caster = EventCaster.newInstance( mListenerClass, executor );
        return doStart(caster);
    }

    
    public synchronized boolean startPrivately(L privateListener) throws HidException {
        if(mInterface != null)
            return false;
        
        mInterface = mDevice.openInterface();
        interfaceOpened(mInterface, privateListener);
        
        mLogger.finer(String.format("Driver for device [%s] started for private listener.", mDevice));
        return true;
    }

    
    public synchronized boolean stop() throws HidException {
        HidInterface inter = mInterface;
        if(inter == null)
            return false;
        
        EventCaster<L> caster = mCaster;
        mInterface = null;
        mCaster = null;
        
        inter.close();
        if(caster != null)
            caster.close();
        
        mLogger.finer(String.format("Driver for device [%s] stopped.", mDevice));
        return true;
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getName());
        sb.append("[");
        
        String product = mDevice.getProduct();
        
        if(product != null && product.trim().length() > 0) {
            sb.append(product);
        }else{
            sb.append(String.format("unnammed_device_%04X]", mDevice.getProductID()));
        }

        String serial = mDevice.getSerial();
        
        if(serial != null && serial.trim().length() > 0) {
            sb.append(", Ser:");
            sb.append(serial);
        }else{
            sb.append(String.format(", Loc: 0x%08X", mDevice.getLocationID()));
        }
        
        sb.append("]");
        return sb.toString();
        
    }
    

    /**
     * Called when the device interface is opened.
     * 
     * @param inter Opened interface to device.
     * @param listener The listener that should be used to transmit device events.
     */
    protected abstract void interfaceOpened(HidInterface inter, L listener) throws HidException;

    
    
    private boolean doStart(EventCaster<L> caster) throws HidException {
        mLogger.finest(String.format("Opening interface to HID device [%s]", mDevice));
        
        try{
            mInterface = mDevice.openInterface();
        }catch(HidException ex) {
            mLogger.warning(String.format("Failed to open interface to HID Device [%s]\n -> %s", mDevice, ex.getMessage()));
            throw ex;
        }
            
        mCaster = caster;
        
        for(L listener: mListeners) {
            if(mListeners.containsStrongly(listener)) {
                caster.addListener(listener);
            }else{
                caster.addListenerWeakly(listener);
            }
        }
        
        interfaceOpened( mInterface, mCaster.cast() );
        mLogger.fine(String.format("Connected to HID device [%s]", mDevice));
        return true;
    }

    
}