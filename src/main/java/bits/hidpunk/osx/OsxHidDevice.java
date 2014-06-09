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
import bits.util.event.EventCaster;


/**
 * @author Philip DeCamp
 */
class OsxHidDevice implements HidDevice {


    static OsxHidDevice create( OsxHidManager manager, IODevice devObj ) throws HidException {
        CFRef hidDict = null;
        CFRef usbDict = null;
        long ptr;

        try {
            ptr = getHidDictionary( devObj.pointer() );
            hidDict = new CFRef( ptr );
            ptr = getUsbDictionary( devObj.pointer() );
            usbDict = new CFRef( ptr );

            ByteBuffer buf = ByteBuffer.allocateDirect( 1056 );
            buf.order( ByteOrder.nativeOrder() );
            queryDeviceInfo( devObj.pointer(), hidDict.pointer(), usbDict.pointer(), buf );

            OsxHidDevice dev = new OsxHidDevice( manager, devObj, buf );
            dev.mElements = OsxHidElement.findElements( dev, hidDict );
            return dev;

        } finally {
            if( hidDict != null ) {
                hidDict.deref();
            }

            if( usbDict != null ) {
                usbDict.deref();
            }
        }
    }
    
    
    private final OsxHidManager mManager;
    private final IODevice mDevObj;

    private OsxHidElement[] mElements = null;

    private final int mUsagePage;
    private final int mUsage;
    private final int mVendorID;
    private final int mProductID;
    private final int mVersion;
    private final int mLocationID;
    private final String mTransport;
    private final String mProduct;
    private final String mVendor;
    private final String mSerial;

    private final HidTerminationListener mCallback;
    private EventCaster<HidTerminationListener> mGroup = null;
    private boolean mTerminated = false;


    private OsxHidDevice( OsxHidManager manager, IODevice devObj, ByteBuffer buf ) throws HidException {
        mManager = manager;
        mDevObj = devObj;
        devObj.ref();

        byte[] arr = new byte[256];
        mTransport = OsxUtil.readCString( buf, arr, 256 );
        mVendorID = buf.getInt();
        mProductID = buf.getInt();
        mVersion = buf.getInt();
        mVendor = OsxUtil.readCString( buf, arr, 256 );
        mProduct = OsxUtil.readCString( buf, arr, 256 );
        mSerial = OsxUtil.readCString( buf, arr, 256 );
        mLocationID = buf.getInt();
        mUsagePage = buf.getInt();
        mUsage = buf.getInt();

        mCallback = new HidTerminationListener() {
            @Override
            public void hidTerminated( HidDevice dev ) {
                processTermination();
            }
        };
    }



    @Override
    public int usagePage() {
        return mUsagePage;
    }

    @Override
    public int usage() {
        return mUsage;
    }

    @Override
    public String transform() {
        return mTransport;
    }

    @Override
    public String vendor() {
        return mVendor;
    }

    @Override
    public int vendorId() {
        return mVendorID;
    }

    @Override
    public String product() {
        return mProduct;
    }

    @Override
    public int productId() {
        return mProductID;
    }

    @Override
    public int version() {
        return mVersion;
    }

    @Override
    public String serial() {
        return mSerial;
    }

    @Override
    public int locationId() {
        return mLocationID;
    }

    @Override
    public int elementCount() {
        return mElements.length;
    }

    @Override
    public OsxHidElement element( int idx ) {
        return mElements[idx];
    }

    @Override
    public OsxHidElement[] elements() {
        OsxHidElement[] ret = new OsxHidElement[mElements.length];
        System.arraycopy( mElements, 0, ret, 0, mElements.length );
        return ret;
    }

    @Override
    public OsxHidElement[] flattenElements() {
        List<OsxHidElement> list = new ArrayList<OsxHidElement>();

        for( int i = 0; i < mElements.length; i++ ) {
            mElements[i].getFlattenedElements( list );
        }

        return list.toArray( new OsxHidElement[list.size()] );
    }



    @Override
    public boolean addTerminationListener( HidTerminationListener listener ) {
        synchronized( mManager ) {
            synchronized( this ) {
                if( mTerminated ) {
                    return false;
                }

                if( mGroup == null ) {
                    mGroup = EventCaster.create( HidTerminationListener.class, EventCaster.THREADING_SYNCHRONOUS );

                    try {
                        mTerminated = mManager.addTerminationListener( mCallback, this );
                        if( mTerminated ) {
                            return false;
                        }

                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }

                mGroup.addListener( listener );
                return true;
            }
        }
    }


    @Override
    public boolean addTerminationListenerWeakly( HidTerminationListener listener ) {
        synchronized( mManager ) {
            synchronized( this ) {
                if( mTerminated ) {
                    return false;
                }

                if( mGroup == null ) {
                    mGroup = EventCaster.create( HidTerminationListener.class, EventCaster.THREADING_SYNCHRONOUS );

                    try {
                        mTerminated = mManager.addTerminationListener( mCallback, this );
                        if( mTerminated ) {
                            return false;
                        }

                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }

                mGroup.addListenerWeakly( listener );
                return true;
            }
        }
    }


    @Override
    public void removeTerminationListener( HidTerminationListener listener ) {
        synchronized( mManager ) {
            synchronized( this ) {
                if( mGroup == null ) {
                    return;
                }

                mGroup.removeListener( listener );

                if( mGroup.listenerCount() == 0 ) {
                    mGroup.close();
                    try {
                        mManager.removeTerminationListener( mCallback );
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    } finally {
                        mGroup = null;
                    }
                }
            }
        }
    }


    @Override
    public OsxHidInterface openInterface() throws HidException {
        long ptr = openInterface( mDevObj.pointer() );
        if( ptr == 0 ) {
            throw new HidException( "Failed to create device interface." );
        }

        return new OsxHidInterface( mManager, this, ptr );
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( "OsxHidDevice[" );

        if( mProduct != null && mProduct.trim().length() > 0 ) {
            sb.append( mProduct );
        } else {
            sb.append( String.format( "unnammed_device_%04X]", mProductID ) );
        }

        if( mSerial != null && mSerial.trim().length() > 0 ) {
            sb.append( ", Ser:" );
            sb.append( mSerial );
        } else {
            sb.append( String.format( ", Loc: 0x%08X", mLocationID ) );
        }

        sb.append( "]" );
        return sb.toString();
    }


    @Override
    public String getDescription() {
        StringBuilder s = new StringBuilder( "OsxHidDevice:" );
        s.append( String.format( "\n  UsagePage: 0x%02X", mUsagePage ) );
        s.append( String.format( "\n  Usage: 0x%02X", mUsage ) );
        s.append( "\n  Transport: " + mTransport );
        s.append( "\n  Vendor: " + mVendor );
        s.append( String.format( "\n  VendorID: 0x%04X", mVendorID ) );
        s.append( "\n  Product: " + mProduct );
        s.append( String.format( "\n  ProductID: 0x%04X", mProductID ) );
        s.append( "\n  Version: " + mVersion );
        s.append( "\n  Serial: " + mSerial );
        s.append( String.format( "\n  LocationID: 0x%08X", mLocationID ) );

        return s.toString();
    }



    long getIOPointer() {
        return mDevObj.pointer();
    }



    private void processTermination() {
        EventCaster<HidTerminationListener> group;
        
        synchronized( this ) {
            if( mTerminated ) {
                return;
            }

            mTerminated = true;
            group = mGroup;
            mGroup = null;
        }

        try {
            mManager.removeTerminationListener( mCallback );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        if( group != null ) {
            group.cast().hidTerminated( this );
        }
    }



    private static native long getHidDictionary( long ptr ) throws HidException;


    private static native long getUsbDictionary( long ptr ) throws HidException;


    private static native void queryDeviceInfo( long ptr, long hidDictPtr, long usbDictPtr, ByteBuffer outBuf ) throws HidException;


    private native long openInterface( long ptr ) throws HidException;

}
