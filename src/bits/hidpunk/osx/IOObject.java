/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.langx.ref.AbstractRefable;


/**
 * @author Philip DeCamp
 */
class IOObject extends AbstractRefable {

    private final boolean mReleaseAtFinalize;
    private long mPtr;


    public IOObject( long ptr ) {
        mPtr = ptr;
        mReleaseAtFinalize = false;
    }


    public IOObject( long ptr, boolean releaseAtFinalize ) {
        mPtr = ptr;
        mReleaseAtFinalize = releaseAtFinalize;
    }



    public synchronized long pointer() {
        return mPtr;
    }


    protected synchronized void freeObject() {
        long ptr = mPtr;
        mPtr = 0;

        if( ptr == 0 )
            return;

        try {
            release( ptr );
        } catch( Exception ex ) {}
    }


    @Override
    protected void finalize() throws Throwable {
        try {
            freeObject();
        } finally {
            super.finalize();
        }
    }

    
    private static native void retain( long ptr );

    private static native void release( long ptr );

}
