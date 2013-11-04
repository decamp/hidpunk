package bits.hidpunk.osx;

import java.util.*;

import bits.hidpunk.*;


/** 
 * @author Philip DeCamp  
 */
public class OsxHidManager extends HidManager {

    private static boolean sInit = false;
    
    private final long mMasterPort;
    private final CFNotificationSource mNoteSource;
    private final CFRunLoop mNoteLoop;
    
    private final Map<HidMatchListener, NotificationHandler> mMatchMap = 
            new HashMap<HidMatchListener, NotificationHandler>();
        
    private final Map<HidTerminationListener, NotificationHandler> mTerminationMap = 
            new HashMap<HidTerminationListener, NotificationHandler>();
    
        
    public OsxHidManager() throws HidException {
        synchronized(OsxHidManager.class) {
            if(!sInit)
                initLibrary();
    
            mMasterPort = getIOMasterPort();
            if(mMasterPort == 0)
                throw new HidException("Failed to retrieve IO Master Port");
    
            mNoteSource = CFNotificationSource.create(mMasterPort);
            mNoteLoop = new CFRunLoop("HID Notification Thread");
            mNoteLoop.addSources(mNoteSource);
            mNoteLoop.start();
            sInit = true;
        }
    }

    
    
    
    public HidMatcher newDeviceMatcher() throws HidException {
        return new OsxHidMatcher();
    }
    
    
    public List<HidDevice> findDevices(HidMatcher matcher) throws HidException {
        if(matcher == null || !(matcher instanceof OsxHidMatcher))
            throw new IllegalArgumentException("Invalid HidMatcher");
    
        IOIter iter = null;
        long ptr = ((OsxHidMatcher)matcher).getPointer();
        List<HidDevice> list = new ArrayList<HidDevice>();
        
        iter = new IOIter(getDeviceIterator(mMasterPort, ptr), true);
        
        try{
            while(true) {
                long devPtr = iter.next();
                if(devPtr == 0)
                    break;
                
                IODevice obj = new IODevice(devPtr);
                
                try{
                    OsxHidDevice dev = OsxHidDevice.constructDevice(this, obj);
                    list.add(dev);
                }catch(Exception ex) {
                    ex.printStackTrace();
                }finally{
                    obj.deref();
                }
            }
            
        }finally{
            if(iter != null)
                iter.deref();
        }
        
        return list;
    }

    
    public synchronized List<HidDevice> addMatchListener( HidMatcher matcher, 
                                                          HidMatchListener listener ) 
                                                          throws HidException 
    {        
        if(matcher == null || !(matcher instanceof OsxHidMatcher))
            throw new IllegalArgumentException("Invalid HidMatcher");
        
        if(listener == null)
            throw new NullPointerException("NULL listener");
        
        removeMatchListener(listener);
        
        boolean aborted = true;
        NotificationHandler handler = null;
        List<IODevice> matchList = null;
        List<HidDevice> ret = null;
        
        try{
            handler = NotificationHandler.createMatchHandler(this, listener);
            mNoteLoop.stop(true);
            matchList = handler.armFound(mNoteSource, (OsxHidMatcher)matcher);
            
            ret = new ArrayList<HidDevice>(matchList.size());
            ListIterator<IODevice> iter = matchList.listIterator();
            
            while(iter.hasNext()) {
                IODevice obj = iter.next();
                
                try{
                    OsxHidDevice dev = OsxHidDevice.constructDevice(this, obj);
                    ret.add(dev);
                }catch(Exception ex) {
                    ex.printStackTrace();
                }finally{
                    iter.remove();
                    obj.deref();
                }
            }
            
            mMatchMap.put(listener, handler);
            aborted = false;
            return ret;
            
        }catch(HidException ex) {
            ex.printStackTrace();
            throw ex;
            
        }finally{
            if(aborted) {
                if(matchList != null) {
                    for(IOObject obj: matchList) {
                        try{
                            obj.deref();
                        }catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            
                if(handler != null) {
                    try{
                        handler.deref();
                    }catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
            mNoteLoop.start();
        }
    }
    
    
    public synchronized void removeMatchListener(HidMatchListener listener) throws HidException { 
        if(listener == null)
            return;
  
        NotificationHandler handler = mMatchMap.remove(listener);
        if(handler == null)
            return;
        
        try{
            mNoteLoop.stop(true);
            handler.deref();
        }finally{
            mNoteLoop.start();
        }
    }
    
    

    
    synchronized boolean addTerminationListener( HidTerminationListener listener, 
                                                 OsxHidDevice device ) 
                                                 throws HidException 
    {        
        if(listener == null)
            throw new NullPointerException("NULL listener");
        
        removeTerminationListener(listener);
        
        boolean aborted = true;
        NotificationHandler handler = null;
        
        try{
            handler = NotificationHandler.createTerminationHandler(listener, device);
            mNoteLoop.stop(true);
            boolean terminated = handler.armRemoval(mNoteSource);
            
            if(terminated)
                return true;
            
            mTerminationMap.put(listener, handler);
            aborted = false;
            return false;
            
        }finally{
            if(aborted) {
                if(handler != null) {
                    try{
                        handler.deref();
                    }catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            
            mNoteLoop.start();
        }   
    }    

    
    synchronized void removeTerminationListener(HidTerminationListener listener) throws HidException {
        if(listener == null)
            return;

        NotificationHandler handler = mTerminationMap.remove(listener);
        if(handler == null)
            return;
        
        try{
            mNoteLoop.stop(true);
            handler.deref();
        }finally{
            mNoteLoop.start();
        }
    }
    
    
    long getMasterPort() {
        return mMasterPort;
    }
    
    
    
    private static native void initLibrary() throws HidException;
        
    private native long getIOMasterPort() throws HidException;
    
    private native long getDeviceIterator(long masterPort, long matcherPtr) throws HidException;
        
}
