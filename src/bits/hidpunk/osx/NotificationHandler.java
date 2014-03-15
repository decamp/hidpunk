/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import java.util.*;

import bits.hidpunk.*;


/**
 * @author Philip DeCamp
 */
class NotificationHandler extends CStruct {

    private static final int CODE_MATCHED = 1;
    private static final int CODE_TERMINATED = 2;
    private static final int CODE_ERROR = 3;


    public static NotificationHandler createMatchHandler( OsxHidManager manager, 
                                                          HidMatchListener listener )
                                                          throws HidException
    {
        long structPtr = 0;
        boolean abort = true;

        try {
            structPtr = allocateStruct();
            if( structPtr == 0 ) {
                throw new IOKitException( "Failed to allocate NotificationHandler struct" );
            }

            NotificationHandler ret = new NotificationHandler( structPtr, manager, listener );
            abort = false;
            return ret;

        } finally {
            if( abort && structPtr != 0 ) {
                try {
                    freeStruct( structPtr );
                } catch( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        }
    }


    public static NotificationHandler createTerminationHandler( HidTerminationListener listener, 
                                                                OsxHidDevice device )
                                                                throws HidException
    {
        long structPtr = 0;
        boolean abort = true;

        try {
            structPtr = allocateStruct();
            if( structPtr == 0 ) {
                throw new IOKitException( "Failed to allocate NotificationHandler struct" );
            }

            NotificationHandler ret = new NotificationHandler( structPtr, listener, device );
            abort = false;
            return ret;

        } finally {
            if( abort && structPtr != 0 ) {
                try {
                    freeStruct( structPtr );
                } catch( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        }
    }



    private final OsxHidManager mManager;
    private final HidMatchListener mFoundListener;
    private final HidTerminationListener mRemovedListener;
    private final OsxHidDevice mDevice;

    private boolean mArmed = false;


    private NotificationHandler( long structPtr, 
                                 OsxHidManager manager, 
                                 HidMatchListener listener ) 
                                 throws HidException
    {
        super( structPtr );
        mManager = manager;
        mFoundListener = listener;
        mRemovedListener = null;
        mDevice = null;

        initStruct( structPtr, this );
    }

    private NotificationHandler( long structPtr, 
                                 HidTerminationListener listener, 
                                 OsxHidDevice device ) 
                                 throws HidException
    {
        super( structPtr );
        mManager = null;
        mFoundListener = null;
        mRemovedListener = listener;
        mDevice = device;

        initStruct( structPtr, this );
    }



    synchronized List<IODevice> armFound( CFNotificationSource source, OsxHidMatcher matcher ) throws HidException {
        if( !isValid() ) {
            throw new IllegalStateException( "Attempted to use disposed NotificationHandler." );
        }

        if( mArmed ) {
            throw new IllegalStateException( "NotificationHandler already armed." );
        }

        if( mFoundListener == null ) {
            throw new IllegalStateException( "Attempted to arm Removal NotificationHandler for Found events." );
        }

        mArmed = true;

        long iterPtr = addFoundNotification( getPointer(), source.portPointer(), matcher.pointer() );
        if( iterPtr == 0 ) {
            throw new HidException( "Failed to add matching notification." );
        }

        IOIter iter = new IOIter( iterPtr, false );
        List<IODevice> ret = new ArrayList<IODevice>();

        for( long ptr = iter.next(); ptr != 0; ptr = iter.next() ) {
            ret.add( new IODevice( ptr ) );
        }

        return ret;
    }

    synchronized boolean armRemoval( CFNotificationSource source ) throws HidException {
        if( !isValid() ) {
            throw new IllegalStateException( "Attempted to use disposed NotificationHandler." );
        }

        if( mArmed ) {
            throw new IllegalStateException( "NotificationHandler already armed." );
        }

        if( mRemovedListener == null || mDevice == null ) {
            throw new IllegalStateException( "Attempted to arm Found NotificationHandler for Removed events." );
        }

        mArmed = true;
        return addRemovedNotification( getPointer(), source.portPointer(), mDevice.getIOPointer() );
    }


    @Override
    protected void destruct( long ptr ) throws HidException {
        freeStruct( ptr );
    }


    private void callback( int code, int device ) {
        switch( code ) {
        case CODE_MATCHED:
            processMatch( device );
            break;

        case CODE_TERMINATED:
            processTermination( device );
            break;

        case CODE_ERROR:
            processError( device );
            break;
        }
    }

    private void processMatch( int device ) {
        if( mFoundListener == null && mManager != null ) {
            return;
        }

        IODevice iodev = null;

        try {
            iodev = new IODevice( device );
            OsxHidDevice dev = OsxHidDevice.create( mManager, iodev );
            mFoundListener.hidFound( dev );

        } catch( Exception ex ) {
            ex.printStackTrace();

            if( iodev != null ) {
                try {
                    iodev.deref();
                } catch( Exception ex2 ) {}
            }
        }
    }

    private void processTermination( int device ) {
        if( mRemovedListener != null ) {
            mRemovedListener.hidTerminated( mDevice );
        }
    }

    private void processError( int errCode ) {
        new HidException( "Notification callback received error: " + errCode ).printStackTrace();
    }
    

    private static native long allocateStruct() throws HidException;
    private static native void initStruct( long ptr, NotificationHandler self ) throws HidException, LinkageError;
    private static native void freeStruct( long ptr ) throws HidException;
    private static native long addFoundNotification( long structPtr, long portPtr, long dictPtr ) throws HidException;
    private static native boolean addRemovedNotification( long structPtr, long portPtr, long devID ) throws HidException;
}
