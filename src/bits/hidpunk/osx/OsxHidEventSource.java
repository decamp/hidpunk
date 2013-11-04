package bits.hidpunk.osx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import bits.hidpunk.*;
import bits.langx.ref.Refable;



/** 
 * @author Philip DeCamp  
 */
class OsxHidEventSource implements HidEventSource {

    
    static OsxHidEventSource newPollingEventSource
    (OsxHidInterface inter, long interPtr, int queueSize, long pollMicros, HidEventListener callback, HidElement... elements)
    throws HidException 
    {
        List<HidElement> list = new ArrayList<HidElement>(elements.length);
        for(int i = 0; i < elements.length; i++) {
            list.add(elements[i]);
        }

        boolean aborted = true;
        OsxQueue[] queues = null;
        OsxHidEventSource ret = null;
        
        try{
            if(queueSize > 0) {
                queues = OsxQueue.newInstances(interPtr, queueSize, elements);
                ret = new OsxHidEventSource(inter, interPtr, queues, pollMicros, callback, list);
            }else{
                ret = new OsxHidEventSource(inter, interPtr, pollMicros, callback, list);
            }
            
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(queues);
            }
        }
        
        return ret;
    }    
    
    
    static OsxHidEventSource newPollingValueSource
    (OsxHidInterface inter, long interPtr, int queueSize, long pollMicros, HidValueListener callback, HidElement... elements)
    throws HidException 
    {
        List<HidElement> list = new ArrayList<HidElement>(elements.length);
        for(int i = 0; i < elements.length; i++) {
            list.add(elements[i]);
        }
        
        OsxQueue[] queues = null;
        OsxHidEventSource ret = null;
        boolean aborted = true;
        
        try{
            if(queueSize > 0) {
                queues = OsxQueue.newInstances(interPtr, queueSize, elements);
                ret = new OsxHidEventSource(inter, interPtr, queues, pollMicros, callback, list);
            }else{
                ret = new OsxHidEventSource(inter, interPtr, pollMicros, callback, list);
            }
            
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(queues);
            }
        }
        
        return ret;
    }
    
    
    static OsxHidEventSource newAsyncEventSource
    (OsxHidInterface inter, long interPtr, int queueSize, HidEventListener callback, HidElement... elements)
    throws HidException
    {
        List<HidElement> list = new ArrayList<HidElement>(elements.length);
        for(int i = 0; i < elements.length; i++) {
            list.add(elements[i]);
        }

        boolean aborted = true;
        queueSize = Math.max(1, queueSize);
        OsxQueue[] queues = OsxQueue.newInstances(interPtr, queueSize, elements);
        OsxHidEventSource ret = null;
        
        try{
            ret = new OsxHidEventSource(inter, interPtr, queues, callback, list);
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(queues);
            }
        }
        
        return ret;
    }
    
    
    static OsxHidEventSource newAsyncValueSource
    (OsxHidInterface inter, long interPtr, int queueSize, HidValueListener callback, HidElement... elements)
    throws HidException
    {
        List<HidElement> list = new ArrayList<HidElement>(elements.length);
        for(int i = 0; i < elements.length; i++) {
            list.add(elements[i]);
        }

        queueSize = Math.max(1, queueSize);
        OsxQueue[] queues = OsxQueue.newInstances(interPtr, queueSize, elements);
        OsxHidEventSource ret = null;
        boolean aborted = true;
        
        try{
            ret = new OsxHidEventSource(inter, interPtr, queues, callback, list);
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(queues);
            }
        }
        
        return ret;
    }
    
    
    

    
    private final OsxHidInterface mInterface;
    private final OsxQueue[] mQueues;
    private final boolean mIsPolling;
    private final int mElCount;
    private final long mPollMicros;
    private final HidEventListener mEventCallback;
    private final HidValueListener mValueCallback;
    private final List<HidElement> mElements;
    private final HidEvent[] mInEvents;
    private final HidEvent[] mOutEvents;
    private final ByteBuffer mInBuffer;
    private final ByteBuffer mOutBuffer;
    private final CFLoopSource[] mSources;
    private final CFLoopTimer[] mTimers;
    
    
    private boolean mRunning = false;
    private boolean mClosed = false;

    
    
    private OsxHidEventSource
    (OsxHidInterface inter, long interPtr, long pollMicros, HidEventListener callback, List<HidElement> elements)
    throws HidException
    {
        mInterface = inter;
        mQueues = null;
        mIsPolling = true;
        mElCount = elements.size();
        mPollMicros = pollMicros;
        mEventCallback = callback;
        mValueCallback = null;
        mElements = elements;
        
        mInEvents = new HidEvent[mElCount];
        mOutEvents = new HidEvent[mElCount];
        mInBuffer = null;
        mOutBuffer = null;
        
        int[] cookieArr = new int[mElCount];
                
        for(int i = 0; i < mElCount; i++) {
            mInEvents[i] = new HidEvent();
            mInEvents[i].mCookie = elements.get(i).getCookie();
            mInEvents[i].mType = elements.get(i).getType();
            mInEvents[i].mStale = true;
            cookieArr[i] = mInEvents[i].mCookie;
        }
        
        
        mSources = null;
        mTimers = new CFLoopTimer[1];
        EventSourceStruct struct = null;
        boolean aborted = true;
        
        try{
            long structPtr = newPollingEventStruct( interPtr,
                                                    pollMicros,
                                                    mElCount,
                                                    cookieArr,
                                                    mInEvents);
        
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            struct = new EventSourceStruct(structPtr);
            structPtr = getRunLoopTimer(structPtr);
            
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            mTimers[0] = new CFLoopTimer(structPtr, struct);
            struct.deref();
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(struct);
                derefAll(mTimers);
            }
        }
    }
    
    
    private OsxHidEventSource
    (OsxHidInterface inter, long interPtr, OsxQueue[] queues, long pollMicros, HidEventListener callback, List<HidElement> elements)
    throws HidException
    {
        mInterface = inter;
        mQueues = queues;
        mIsPolling = true;
        mElCount = elements.size();
        mPollMicros = pollMicros;
        mEventCallback = callback;
        mValueCallback = null;
        mElements = elements;
        
        mInEvents = new HidEvent[mElCount];
        mOutEvents = new HidEvent[mElCount];
        mInBuffer = null;
        mOutBuffer = null;
        
        int[] cookieArr = new int[mElCount];
        long[] queueArr = new long[mElCount];
                
        for(int i = 0; i < mElCount; i++) {
            mInEvents[i] = new HidEvent();
            mInEvents[i].mCookie = elements.get(i).getCookie();
            mInEvents[i].mType = elements.get(i).getType();
            mInEvents[i].mStale = true;
            cookieArr[i] = mInEvents[i].mCookie;
            queueArr[i] = queues[i].getPointer();
        }
        
        mSources = null;
        mTimers = new CFLoopTimer[1];
        EventSourceStruct struct = null;
        boolean aborted = false;
        
        try{
            long structPtr = newQueuedPollingEventStruct( interPtr,
                                                          queueArr,
                                                          pollMicros,
                                                          mElCount,
                                                          cookieArr,
                                                          mInEvents);
            
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            struct = new EventSourceStruct(structPtr);
            structPtr = getRunLoopTimer(structPtr);
            
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            mTimers[0] = new CFLoopTimer(structPtr, struct);
            
            for(int i = 0; i < mElCount; i++)
                mTimers[0].addResource(mQueues[i]);
            
            struct.deref();
            aborted = false;
            
        }finally{
            if(aborted) {
                derefAll(struct);
                derefAll(mTimers);
            }
        }
    }
    

    private OsxHidEventSource
    (OsxHidInterface inter, long interPtr, long pollMicros, HidValueListener callback, List<HidElement> elements)
    throws HidException
    {
        mInterface = inter;
        mQueues = null;
        mIsPolling = true;
        mElCount = elements.size();
        mPollMicros = pollMicros;
        mEventCallback = null;
        mValueCallback = callback;
        mElements = elements;
        
        mInEvents = null;
        mOutEvents = null;
        
        mInBuffer = ByteBuffer.allocateDirect(mElCount * 4);
        mInBuffer.order(ByteOrder.nativeOrder());
        mOutBuffer = mInBuffer.asReadOnlyBuffer();
        mOutBuffer.order(ByteOrder.nativeOrder());
        
        int[] cookieArr = new int[mElCount];
                
        for(int i = 0; i < mElCount; i++) {
            cookieArr[i] = elements.get(i).getCookie();
        }
                
        mSources = null;
        mTimers = new CFLoopTimer[1];
        EventSourceStruct struct = null;
        boolean aborted = false;
        
        try{
            long structPtr = newPollingValueStruct( interPtr,
                                                    pollMicros,
                                                    mElCount,
                                                    cookieArr,
                                                    mInBuffer);
            
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            struct = new EventSourceStruct(structPtr);
            structPtr = getRunLoopTimer(structPtr);
            
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            mTimers[0] = new CFLoopTimer(structPtr, struct);
            struct.deref();
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(struct);
                derefAll(mTimers);
            }
        }
    }
    
    
    private OsxHidEventSource
    (OsxHidInterface inter, long interPtr, OsxQueue[] queues, long pollMicros, HidValueListener callback, List<HidElement> elements)
    throws HidException
    {
        mInterface = inter;
        mQueues = queues;
        mIsPolling = true;
        mElCount = elements.size();
        mPollMicros = pollMicros;
        mEventCallback = null;
        mValueCallback = callback;
        mElements = elements;
        
        mInEvents = null;
        mOutEvents = null;
        
        mInBuffer = ByteBuffer.allocateDirect(mElCount * 4);
        mInBuffer.order(ByteOrder.nativeOrder());
        mOutBuffer = mInBuffer.asReadOnlyBuffer();
        mOutBuffer.order(ByteOrder.nativeOrder());
        
        int[] cookieArr = new int[mElCount];
        long[] queueArr = new long[mElCount]; 
        
        for(int i = 0; i < mElCount; i++) {
            cookieArr[i] = elements.get(i).getCookie();
            queueArr[i] = queues[i].getPointer();
        }
        
        mSources = null;
        mTimers = new CFLoopTimer[1];
        EventSourceStruct struct = null;
        boolean aborted = false;
        
        try{
            long structPtr = newQueuedPollingValueStruct( interPtr,
                                                          queueArr,
                                                          pollMicros,
                                                          mElCount,
                                                          cookieArr,
                                                          mInBuffer);
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            struct = new EventSourceStruct(structPtr);
            structPtr = getRunLoopTimer(structPtr);
            
            if(structPtr == 0)
                throw new HidException("Failed to create HidEventSource");
            
            mTimers[0] = new CFLoopTimer(structPtr, struct);
            for(int i = 0; i < mElCount; i++)
                mTimers[0].addResource(mQueues[i]);
            
            struct.deref();
            aborted = false;
            
        }finally{
            if(aborted) {
                derefAll(struct);
                derefAll(mTimers);
            }
        }
    }

    
    private OsxHidEventSource
    (OsxHidInterface inter, long interPtr, OsxQueue[] queues, HidEventListener callback, List<HidElement> elements)
    throws HidException
    {
        mInterface = inter;
        mQueues = queues;
        mIsPolling = false;
        mElCount = elements.size();
        mPollMicros = 0;
        mEventCallback = callback;
        mValueCallback = null;
        mElements = elements;
        
        mInEvents = new HidEvent[mElCount];
        mOutEvents = new HidEvent[mElCount];
        mInBuffer = null;
        mOutBuffer = null;
        
        for(int i = 0; i < mElCount; i++) {
            mInEvents[i] = new HidEvent();
            mInEvents[i].mCookie = elements.get(i).getCookie();
            mInEvents[i].mType = elements.get(i).getType();
            mInEvents[i].mStale = true;
        }
        
        mSources = new CFLoopSource[mElCount];
        mTimers = null;
        EventSourceStruct struct = null;
        boolean aborted = true;

        try{
            for(int i = 0; i < mElCount; i++) {
                long structPtr = newAsyncEventStruct( interPtr, 
                                                      mQueues[i].getPointer(),
                                                      i,
                                                      elements.get(i).getCookie(),
                                                      mInEvents[i]);
                
                if(structPtr == 0) 
                    throw new HidException("Failed to allocate create HidEventSource");
                
                struct = new EventSourceStruct(structPtr);
                structPtr = getRunLoopSource(structPtr);
                
                if(structPtr == 0) 
                    throw new HidException("Failed to allocate create HidEventSource");
                
                mSources[i] = new CFLoopSource(structPtr, struct);
                mSources[i].addResource(mQueues[i]);
                struct.deref();
                struct = null;
            }
            
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(mSources);
                derefAll(struct);
            }
        }
    }
    
    
    private OsxHidEventSource
    (OsxHidInterface inter, long interPtr, OsxQueue[] queues, HidValueListener callback, List<HidElement> elements)
    throws HidException
    {
        mInterface = inter;
        mQueues = queues;
        mIsPolling = false;
        mElCount = elements.size();
        mPollMicros = 0;
        mEventCallback = null;
        mValueCallback = callback;
        mElements = elements;
        
        mInEvents = null;
        mOutEvents = null;
        mInBuffer = ByteBuffer.allocateDirect(mElCount * 4);
        mInBuffer.order(ByteOrder.nativeOrder());
        mOutBuffer = mInBuffer.asReadOnlyBuffer();
        mOutBuffer.order(ByteOrder.nativeOrder());
        
        mSources = new CFLoopSource[mElCount];
        mTimers = null;
        EventSourceStruct struct = null;
        boolean aborted = true;
        
        try{
            for(int i = 0; i < mElCount; i++) {
                long structPtr = newAsyncValueStruct( interPtr, 
                                                      mQueues[i].getPointer(),
                                                      i,
                                                      elements.get(i).getCookie(),
                                                      mInBuffer);
                
                if(structPtr == 0) 
                    throw new HidException("Failed to allocate create HidEventSource");
                
                struct = new EventSourceStruct(structPtr);
                structPtr = getRunLoopSource(structPtr);
                
                if(structPtr == 0) 
                    throw new HidException("Failed to allocate create HidEventSource");
                
                mSources[i] = new CFLoopSource(structPtr, struct);
                mSources[i].addResource(mQueues[i]);
                struct.deref();
                struct = null;
            }
            
            aborted = false;
        }finally{
            if(aborted) {
                derefAll(mSources);
                derefAll(struct);
            }
        }
    }
    
    
    
    
    public HidDevice getDevice() {
        return mInterface.getDevice();
    }
    
    public HidInterface getInterface() {
        return mInterface;
    }
    
    public List<HidElement> getElements() {
        return new ArrayList<HidElement>(mElements);
    }

    public boolean hasElement(HidElement el) {
        return mElements.contains(el);
    }
    
    public boolean isAsync() {
        return !mIsPolling;
    }
    
    public boolean isPolling() {
        return mIsPolling;
    }
    
    public boolean isQueued() {
        return mQueues != null;
    }
    
    public long getPollingMicros() {
        return mPollMicros;
    }

    
    
    public void start() throws HidException {
        mInterface.startEventSource(this);
    }
    
    public void stop() throws HidException {
        mInterface.stopEventSource(this);
    }

    public void close() throws HidException {
        mInterface.closeEventSource(this, new HidEventSourceClosedException());
    }
    
    void close(Exception cause) throws HidException {
        mInterface.closeEventSource(this, cause);
    }

    
    
    public synchronized boolean isRunning() {
        return mRunning;        
    }
       
    public synchronized boolean isOpen() {
        return !mClosed;
    }
    
    
    
    synchronized void doStart(CFRunLoop loop) throws HidException {
        if(mRunning)
            return;
        
        if(mClosed) 
            throw new HidInterfaceClosedException();

        if(mQueues != null) {
            for(OsxQueue q: mQueues)
                q.start();
        }
        
        if(mIsPolling) {
            loop.addTimers(mTimers);
        }else{
            loop.addSources(mSources);
        }
        
        mRunning = true;
    }
    
    synchronized void doStop(CFRunLoop loop) throws HidException {
        if(!mRunning || mClosed)
            return;
        
        if(mIsPolling) {
            loop.removeTimers(mTimers);
        }else{
            loop.removeSources(mSources);
        }
        
        if(mQueues != null) {
            for(int i = 0; i < mQueues.length; i++) {
                try{
                    mQueues[i].stop();
                    mQueues[i].clear();
                }catch(Exception ex) {
                    //ex.printStackTrace();
                }
            }
        }
        
        mRunning = false;
    }
    
    synchronized void doClose(CFRunLoop loop, Exception cause) throws HidException {
        synchronized(this) {
            if(mClosed)
                return;
            
            try{
                doStop(loop);
            }catch(Exception ex) {}
            
            derefAll(mTimers);
            derefAll(mSources);
            derefAll(mQueues);

            mRunning = false;
            mClosed = true;
        }
        
        if(mEventCallback != null) {
            mEventCallback.hidEventSourceClosing(cause);
        }else{
            mValueCallback.hidEventSourceClosing(cause);
        }
        
    }
    
    
    
    private static void derefAll(Refable r) {
        if(r == null)
            return;
        
        try{
            r.deref();
        }catch(Exception ex) {ex.printStackTrace();}
    }
        
    private static void derefAll(Refable[] s) {
        if(s == null)
            return;
        
        for(int i = 0; i < s.length; i++) {
            if(s[i] != null) {
                try{
                    s[i].deref();
                }catch(Exception ex) {ex.printStackTrace();}
                s[i] = null;
            }
        }
    }
    
    
    
    private void eventCallback(int code) {
        if(!mRunning)
            return;
        
        try{
            if(code == 0) {
                System.arraycopy(mInEvents, 0, mOutEvents, 0, mInEvents.length);
                mEventCallback.hidEventsReceived(mOutEvents);
            }else{
                try{
                    close(new HidException("Failed to retrieve HID events: "  + code));
                }catch(HidException ex) {
                    ex.printStackTrace();
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            try{
                close(ex);
            }catch(Exception exc) {}
        }
    }
        
    private void valueCallback(int code) {
        if(!mRunning)
            return;
        
        try{
            if(code == 0) {
                mOutBuffer.clear();
                mValueCallback.hidValuesReceived(mOutBuffer);
            }else{
                try{
                    close(new HidException("Failed to retrieve HID values: " + code));
                }catch(Exception ex) {}
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            try{
                close(ex);
            }catch(Exception exc) {}
        }   
    }
        
    private void asyncEventCallback(int code, int idx) {
        if(!mRunning)
            return;
        
        try{
            if(code == 0) {
                if(mElCount == 0) {
                    mOutEvents[0] = mInEvents[0];
                }else{
                    for(int i = 0; i < mElCount; i++) {
                        mOutEvents[i] = mInEvents[i];
                        mOutEvents[i].mStale = i != idx;
                    }
                }
                
                mEventCallback.hidEventsReceived(mOutEvents);
                
            }else{
                try{
                    close(new HidException("Failed to retrieve HID events: " + code));
                }catch(HidException ex) {
                    ex.printStackTrace();
                }
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            try{
                close(ex);
            }catch(Exception exc) {}
        }
    }
            
    private void asyncValueCallback(int code, int idx) {
        if(!mRunning)
            return;
        
        try{
            if(code == 0) {
                mOutBuffer.clear();
                mValueCallback.hidValuesReceived(mOutBuffer);
            }else{
                try{
                    close(new HidException("Failed to retrieve HID values: " + code));
                }catch(Exception ex) {}
            }
        }catch(Exception ex) {
            ex.printStackTrace();
            try{
                close(ex);
            }catch(Exception exc) {}
        }   
    }
    
    
    
    protected native long newPollingEventStruct
    (long interPtr, long pollMicros, int elCount, int[] cookieArr, HidEvent[] eventArr)
    throws HidException;
        
    protected native long newQueuedPollingEventStruct
    (long interPtr, long[] queuePtrs, long pollMicros, int elCount, int[] cookieArr, HidEvent[] eventArr)
    throws HidException;
            
    protected native long newPollingValueStruct
    (long interPtr, long pollMicros, int elCount, int[] cookieArr, ByteBuffer valueBuf)
    throws HidException;
        
    protected native long newQueuedPollingValueStruct
    (long interPtr, long[] queuePtrs, long pollMicros, int elCount, int[] cookieArr, ByteBuffer valueBuf)
    throws HidException;
        
    protected native long newAsyncEventStruct
    (long interPtr, long queuePtr, int elOffset, int cookie, HidEvent event)
    throws HidException;
        
    protected native long newAsyncValueStruct
    (long interPtr, long queuePtr, int elOffset, int cookie, ByteBuffer valueBuf)
    throws HidException;
        
    //Calls CFRetain on the timer before returning.
    protected native long getRunLoopTimer(long structPtr);
        
    //Calls CFRetain on the source before returning.
    protected native long getRunLoopSource(long structPtr);
    
}
