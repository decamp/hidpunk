/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import bits.hidpunk.*;

/**
 * @author Philip DeCamp
 */
class OsxHidInterface implements HidInterface {

    private final OsxHidDevice mDevice;
    private final ByteBuffer mEventBuf;
    private final long mPtr;

    private final List<OsxHidEventSource> mSourceList = new LinkedList<OsxHidEventSource>();
    private final CFRunLoop mLoop;
    private final HidTerminationListener mTerminationCallback;

    private boolean mClosed = false;
    private boolean mDeviceTerminated = false;
    private Thread mHook = null;


    OsxHidInterface( OsxHidManager manager, OsxHidDevice device, long ptr ) throws HidException {
        mDevice = device;
        mEventBuf = ByteBuffer.allocateDirect( 28 );
        mEventBuf.order( ByteOrder.nativeOrder() );
        mPtr = ptr;
        mClosed = ptr == 0;
        mTerminationCallback = new HidTerminationListener() {
            @Override
            public void hidTerminated( HidDevice device ) {
                try {
                    close( new HidTerminatedException(), true );
                } catch( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        };

        if( mClosed ) {
            mLoop = null;
            return;
        }

        boolean aborted = true;
        CFRunLoop loop = null;
        CFNotificationSource noteSource = null;
        NotificationHandler handler = null;

        try {
            loop = new CFRunLoop( "HID Interface Thread" );
            noteSource = CFNotificationSource.create( manager.masterPort() );
            handler = NotificationHandler.createTerminationHandler( mTerminationCallback, device );

            if( handler.armRemoval( noteSource ) ) {
                throw new HidTerminatedException( "Device has been terminated." );
            }

            noteSource.addResource( handler );
            handler.deref();
            handler = null;

            loop.addSources( noteSource );
            noteSource.deref();
            noteSource = null;
            aborted = false;

        } finally {
            if( aborted ) {
                if( handler != null ) {
                    try {
                        handler.deref();
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }

                if( noteSource != null ) {
                    try {
                        noteSource.deref();
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }

                if( loop != null ) {
                    try {
                        loop.kill( false );
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        mLoop = loop;
        mHook = new ShutdownHook();
        Runtime.getRuntime().addShutdownHook( mHook );
        mLoop.start();
    }



    @Override
    public OsxHidDevice device() {
        return mDevice;
    }

    @Override
    public void close() throws HidException {
        close( new HidInterfaceClosedException(), false );
    }

    @Override
    public synchronized boolean isOpen() {
        return !mClosed;
    }

    @Override
    public synchronized HidEvent eventFor( HidElement el ) throws HidException {
        return eventFor( el, null );
    }

    @Override
    public synchronized HidEvent eventFor( HidElement el, HidEvent out ) throws HidException {
        if( mClosed ) {
            throw new HidInterfaceClosedException();
        }

        if( el.device() != mDevice ) {
            throw new IllegalArgumentException( "Element does not belong to same device as interface." );
        }
        if( out == null ) {
            out = new HidEvent();
        }

        getElementEvent( mPtr, el.cookie(), out );
        if( out.mLongValueSize > 0 ) {
            out.mLongValue.clear().limit( out.mLongValueSize );
        }

        return out;
    }

    @Override
    public synchronized int valueOf( HidElement el ) throws HidException {
        if( mClosed ) {
            throw new HidInterfaceClosedException();
        }
        if( el.device() != mDevice ) {
            throw new IllegalArgumentException( "Element does not belong to same device as interface." );
        }
        return getElementValue( mPtr, el.cookie() );
    }



    @Override
    public synchronized HidEventSource createPollingSource( long pollMicros, 
                                                         int queueSize, 
                                                         HidValueListener callback, 
                                                         HidElement... elements )
                                                         throws HidException
    {
        if( mClosed ) {
            throw new HidInterfaceClosedException();
        }

        if( elements == null || elements.length == 0 ) {
            throw new NullPointerException( "elements" );
        }

        if( callback == null ) {
            throw new NullPointerException( "callback" );
        }

        for( int i = 0; i < elements.length; i++ ) {
            if( elements[i].device() != mDevice ) {
                throw new IllegalArgumentException( "Element does not belong to same device as interface." );
            }
        }

        pollMicros = Math.max( 0, pollMicros );
        OsxHidEventSource ret = OsxHidEventSource.createPollingValueSource( this,
                                                                            mPtr,
                                                                            queueSize,
                                                                            pollMicros,
                                                                            callback,
                                                                            elements );
        mSourceList.add( ret );
        return ret;

    }

    @Override
    public synchronized HidEventSource createPollingSource
            ( long pollMicros, int queueSize, HidEventListener callback, HidElement... elements )
                    throws HidException
    {
        if( mClosed ) {
            throw new HidException();
        }

        if( elements == null || elements.length == 0 ) {
            throw new NullPointerException( "elements" );
        }

        if( callback == null ) {
            throw new NullPointerException( "callback" );
        }

        for( int i = 0; i < elements.length; i++ ) {
            if( elements[i].device() != mDevice ) {
                throw new IllegalArgumentException( "Element does not belong to same device as interface." );
            }
        }

        pollMicros = Math.max( 0, pollMicros );
        OsxHidEventSource ret = OsxHidEventSource.createPollingEventSource( this,
                                                                            mPtr,
                                                                            queueSize,
                                                                            pollMicros,
                                                                            callback,
                                                                            elements );
        mSourceList.add( ret );
        return ret;
    }

    @Override
    public synchronized HidEventSource createAsyncSource
            ( int queueSize, HidValueListener callback, HidElement... elements )
                    throws HidException
    {
        if( mClosed ) {
            throw new HidException();
        }

        if( elements == null || elements.length == 0 ) {
            throw new NullPointerException( "elements" );
        }

        if( callback == null ) {
            throw new NullPointerException( "callback" );
        }

        for( int i = 0; i < elements.length; i++ ) {
            if( elements[i].device() != mDevice ) {
                throw new IllegalArgumentException( "Element does not belong to same device as interface." );
            }
        }

        OsxHidEventSource ret = OsxHidEventSource.createAsyncValueSource( this, mPtr, queueSize, callback, elements );
        mSourceList.add( ret );
        return ret;
    }

    @Override
    public synchronized HidEventSource createAsyncSource
            ( int queueSize, HidEventListener callback, HidElement... elements )
                    throws HidException
    {
        if( mClosed ) {
            throw new HidException();
        }

        if( elements == null || elements.length == 0 ) {
            throw new NullPointerException( "elements" );
        }

        if( callback == null ) {
            throw new NullPointerException( "callback" );
        }

        for( int i = 0; i < elements.length; i++ ) {
            if( elements[i].device() != mDevice ) {
                throw new IllegalArgumentException( "Element does not belong to same device as interface." );
            }
        }

        OsxHidEventSource ret = OsxHidEventSource.createAsyncEventSource( this, mPtr, queueSize, callback, elements );
        mSourceList.add( ret );
        return ret;
    }



    synchronized void startEventSource( OsxHidEventSource source ) throws HidException {
        if( mDeviceTerminated ) {
            throw new HidTerminatedException();
        }

        if( mClosed ) {
            throw new HidInterfaceClosedException();
        }

        source.doStart( mLoop );
    }

    synchronized void stopEventSource( OsxHidEventSource source ) throws HidException {
        if( mSourceList.isEmpty() ) {
            return;
        }

        source.doStop( mLoop );
    }

    synchronized void closeEventSource( OsxHidEventSource source, Exception cause ) throws HidException {
        if( !mSourceList.remove( source ) ) {
            return;
        }

        source.doClose( mLoop, cause );
    }



    void close( Exception cause, boolean deviceTerminated ) throws HidException {
        List<OsxHidEventSource> list;

        synchronized( this ) {
            mDeviceTerminated |= deviceTerminated;

            if( mClosed ) {
                return;
            }

            mClosed = true;
            list = new ArrayList<OsxHidEventSource>( mSourceList );
            mSourceList.clear();
        }

        try {
            mLoop.kill( true );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        for( OsxHidEventSource source : list ) {
            try {
                source.doClose( mLoop, cause );
            } catch( Exception ex ) {}
        }

        if( mHook != null && !mHook.equals( Thread.currentThread() ) ) {
            try {
                Runtime.getRuntime().removeShutdownHook( mHook );
            } catch( Exception ex ) {}
            mHook = null;
        }

        if( !deviceTerminated ) {
            try {
                close( mPtr );
            } catch( Exception ex ) {}
        }

        release( mPtr );
    }



    private native void close( long ptr ) throws IOKitException;

    private native void release( long ptr ) throws IOKitException;

    private native int getElementValue( long ptr, int cookie ) throws HidException;

    private native void getElementEvent( long ptr, int cookie, HidEvent out ) throws HidException;



    private class ShutdownHook extends Thread {
        @Override
        public void run() {
            try {
                close( new RuntimeException( "JVM Shutting Down" ), true );
            } catch( HidException ex ) {
                ex.printStackTrace();
            }
        }
    }

}
