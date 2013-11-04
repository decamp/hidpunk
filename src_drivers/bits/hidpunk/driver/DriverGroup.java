package bits.hidpunk.driver;

import java.util.*;
import java.util.logging.*;
import java.util.concurrent.Executor;

import bits.data.SemiWeakHashSet;
import bits.event.*;
import bits.hidpunk.*;

/** 
 * Instead of connecting to individual devices, you can use a driver group
 * to automatically start/stop and connect to all matching devices as 
 * they're connected.
 * 
 * @author Philip DeCamp  
 */
public abstract class DriverGroup<L> implements EventSource<L> {
    
    protected static final Logger sLog = Logger.getLogger(DriverGroup.class.getName());
    
    
    private static final int UNSTARTED = -1;
    private static final int START_DEFAULT = 0;
    private static final int START_WITH_THREADING_STRATEGY = 1;
    private static final int START_WITH_EXECUTOR = 2;
    private static final int START_PRIVATELY = 3;
    
    
    private final HidMatcher mMatcher;
    private final MatchHandler mMatchHandler;
    private final SemiWeakHashSet<L> mListeners;
    
    private Set<HidpunkDriver<? super L>> mDrivers = new HashSet<HidpunkDriver<? super L>>(); 
    
    private int mStartMethod = UNSTARTED;
    private int mThreading = 0;
    private Executor mExecutor = null;
    private L mPrivateListener = null;
    

    protected DriverGroup(HidMatcher matcher) {
        mMatcher = matcher;
        mMatchHandler = new MatchHandler();
        mListeners = new SemiWeakHashSet<L>();
    }

    
    
    public synchronized void addListener(L listener) {
        mListeners.add(listener);
        
        for(HidpunkDriver<? super L> d: mDrivers) {
            d.addListener(listener);
        }
    }

    
    public synchronized void addListenerWeakly(L listener) {
        mListeners.addWeakly(listener);
        
        for(HidpunkDriver<? super L> d: mDrivers) {
            d.addListenerWeakly(listener);
        }
    }
    
    
    public synchronized void removeListener(L listener) {
        mListeners.remove(listener);
        
        for(HidpunkDriver<? super L> d: mDrivers) {
            d.addListenerWeakly(listener);
        }
    }

    
    
    public synchronized boolean start() throws HidException {
        if(mStartMethod != UNSTARTED)
            return false;
        
        mStartMethod = START_DEFAULT;
        return doStart();
    }
    
    
    public synchronized boolean start(int threading) throws HidException {
        if(mStartMethod != UNSTARTED)
            return false;
        
        mStartMethod = START_WITH_THREADING_STRATEGY;
        mThreading = threading;
        return doStart();
    }
    
    
    public synchronized boolean start(Executor executor) throws HidException {
        if(mStartMethod != UNSTARTED)
            return false;
        
        mStartMethod = START_WITH_EXECUTOR;
        mExecutor = executor;
        return doStart();
    }
    
    
    public synchronized boolean startPrivately(L privateListener) throws HidException {
        if(mStartMethod != UNSTARTED)
            return false;
        
        mStartMethod = START_PRIVATELY;
        mPrivateListener = privateListener;
        return doStart();
    }
    
    
    public synchronized boolean stop() throws HidException {
        if(mStartMethod == UNSTARTED)
            return false;
        
        mStartMethod = UNSTARTED;
        mExecutor = null;
        mPrivateListener = null;
        
        HidManager.getManager().removeMatchListener(mMatchHandler);

        Set<HidpunkDriver<? super L>> drivers = mDrivers;
        mDrivers = new HashSet<HidpunkDriver<? super L>>();
        
        for(HidpunkDriver<?> d: drivers) {
            try{
                d.stop();
            }catch(Exception ex) {
                sLog.warning(String.format("Failed to stop HID Driver [%s]", d));
            }
        }

        return true;
    }
    
    
    
    protected abstract HidpunkDriver<? super L> newDriver(HidDevice device);

    
    
    private synchronized boolean doStart() throws HidException {
        List<HidDevice> devices = HidManager.getManager().addMatchListener(mMatcher, mMatchHandler);
        
        for(HidDevice dev: devices)
            addDevice(dev);
        
        return true;
    }

    
    private synchronized void addDevice(HidDevice device) {
        if(mStartMethod == UNSTARTED)
            return;
        
        sLog.finer("Device of interest found: " + device);
        HidpunkDriver<? super L> driver = null;
        
        try{
            driver = newDriver(device);
        }catch(Exception ex) {
            sLog.warning(String.format("Failed to create driver for device [%s]\n -> %s", device, ex.getMessage()));
            return;
        }

        if(driver == null) {
            sLog.fine(String.format("Failed to create driver for device [%s]", device));
            return;
        }
        
        if(!mDrivers.add(driver)) {
            sLog.severe(String.format("Duplicate driver [%s] provided for device [%s]", driver, device));
            return;
        }
        
        sLog.finer("Adding driver to DriverGroup for device: " + device);
        if(!device.addTerminationListener(new TerminationHandler(driver))) {
            removeDevice(device, driver);
            return;
        }
        
        for(L listener: mListeners) {
            if(mListeners.containsStrongly(listener)) {
                driver.addListener(listener);
            }else{
                driver.addListenerWeakly(listener);
            }
        }
        
        try{
            switch(mStartMethod) {
            case START_WITH_THREADING_STRATEGY:
                driver.start(mThreading);
                break;
                
            case START_WITH_EXECUTOR:
                driver.start(mExecutor);
                break;
                
            case START_PRIVATELY:
                driver.startPrivately(mPrivateListener);
                break;
                
            default:
                driver.start();
            }
        
        }catch(Exception ex) {
            sLog.warning(String.format("Failed to start driver [%s] for device [%s] ", driver, device));
            mDrivers.remove(driver);
            
            try{
                driver.stop();
            }catch(Exception exx) {}
        }
    }
    
    
    private synchronized void removeDevice(HidDevice device, HidpunkDriver<?> driver) {
        if(mDrivers.remove(driver)) {
            sLog.finest(String.format("DriverGroup removing driver [%s]", driver));
        }
    }

    
    
    private final class MatchHandler implements HidMatchListener {
        public void hidFound(HidDevice device) {
            addDevice(device);
        }
    }

    
    
    private final class TerminationHandler implements HidTerminationListener {

        private final HidpunkDriver<?> mDriver;
        
        TerminationHandler(HidpunkDriver<?> driver) {
            mDriver = driver;
        }

        
        public void hidTerminated(HidDevice device) {
            removeDevice(device, mDriver);
        }
        
    }

}
