/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.hidpunk.HidException;


/**
 * This class has a weird referencing inversion thing going, so be careful.
 * ref() and deref() calls on this object are actually given to the member
 * CFLoopSource object, mSource. When mSource is freed, it will call deref() on
 * a special object, "SuperRef", which will actually dereference the
 * OsxNotificationPort. What this accomplishes is that, as long as anything owns
 * a reference to either the OsxNotificationPort or the CFLoopSource, they will
 * both be valid. However, the CFLoopSource is then guaranteed to be free itself
 * before the notification port, ensuring that a run loop thread is not
 * accessing the notification port while a different thread disposes the port.
 * 
 * @author Philip DeCamp
 */
class CFNotificationSource extends CFLoopSource {

    public static CFNotificationSource create( long masterPort ) throws HidException {
        long portPtr = 0;
        long sourcePtr = 0;
        boolean aborted = true;

        try {
            portPtr = createPort( masterPort );
            if( portPtr == 0 ) {
                throw new IOKitException( "Failed to create notification port." );
            }
            
            sourcePtr = getLoopSource( portPtr );
            if( sourcePtr == 0 ) {
                throw new IOKitException( "Failed to retreive RunLoopSource for NotificationPort." );
            }

            CFNotificationSource port = new CFNotificationSource( portPtr, sourcePtr );
            aborted = false;
            return port;

        } finally {
            if( aborted ) {
                if( sourcePtr != 0 ) {
                    try {
                        new CFRef( sourcePtr ).deref();
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }

                if( portPtr != 0 ) {
                    try {
                        destroyPort( portPtr );
                    } catch( Exception ex ) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    
    private final long mPortPtr;


    private CFNotificationSource( long portPtr, long sourcePtr ) throws HidException {
        super( sourcePtr );
        mPortPtr = portPtr;
    }



    public long portPointer() {
        return mPortPtr;
    }


    @Override
    protected void freeObject() {
        try {
            super.freeObject();
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        try {
            destroyPort( mPortPtr );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    

    private static native long createPort( long masterPort ) throws HidException;

    
    private static native void destroyPort( long portPtr ) throws HidException;

    /**
     * Calls CFRetain on pointer before returning.
     */
    private static native long getLoopSource( long portPtr ) throws HidException;

}
