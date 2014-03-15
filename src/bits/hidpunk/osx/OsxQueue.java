/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.hidpunk.*;


/**
 * @author Philip DeCamp
 */
class OsxQueue extends CStruct {


    static OsxQueue[] create( long interPtr, int queueSize, HidElement... elements ) throws HidException {
        OsxQueue[] ret = new OsxQueue[elements.length];
        boolean aborted = true;

        try {
            for( int i = 0; i < ret.length; i++ ) {
                ret[i] = new OsxQueue( interPtr, queueSize );
                ret[i].addElement( elements[i] );
            }

            aborted = false;

        } finally {
            if( aborted ) {
                for( int i = 0; i < ret.length; i++ ) {
                    if( ret[i] != null ) {
                        try {
                            ret[i].deref();
                        } catch( Exception ex2 ) {}
                    }
                }
            }
        }

        return ret;
    }

    
    OsxQueue( long interPtr, int size ) throws HidException {
        super( allocateQueue( interPtr, size ) );
    }


    
    public synchronized void addElement( HidElement element ) throws HidException {
        if( isValid() ) {
            addElement( getPointer(), element.cookie() );
        }
    }


    public synchronized void start() throws HidException {
        if( !isValid() ) {
            throw new HidException( "Attempted to start disposed queue." );
        }
        startQueue( getPointer() );
    }


    public synchronized void stop() throws HidException {
        if( !isValid() ) {
            return;
        }
        stopQueue( getPointer() );
    }


    public synchronized void clear() throws HidException {
        if( !isValid() ) {
            return;
        }
        clearQueue( getPointer() );
    }

    
    protected void destruct( long ptr ) throws HidException {
        dispose( ptr );
    }

    
    private static native long allocateQueue( long interPtr, int size ) throws HidException;
    private native void startQueue( long ptr ) throws HidException;
    private native void stopQueue( long ptr ) throws HidException;
    private native void clearQueue( long ptr ) throws HidException;
    private native void addElement( long ptr, int cookie ) throws HidException;
    private native void dispose( long ptr ) throws HidException;
}
