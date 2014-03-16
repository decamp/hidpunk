/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import java.util.*;

import bits.util.ref.*;


/**
 * @author Philip DeCamp
 */
class CFRef extends AbstractRefable {

    private final boolean mReleaseAtFinalize;
    private long mPtr;

    /**
     * Set for managing Java dependencies. For safety, it is necessary for C
     * resources to be disposed before referenced Java objects are garbage
     * collected.
     */
    private final List<Refable> mResourceList = new ArrayList<Refable>();


    public CFRef( long ptr ) {
        this( ptr, false, null );
    }


    public CFRef( long ptr, boolean releaseAtFinalize ) {
        this( ptr, releaseAtFinalize, null );
    }


    public CFRef( long ptr, Refable resource ) {
        this( ptr, false, resource );
    }


    public CFRef( long ptr, boolean releaseAtFinalize, Refable resource ) {
        mPtr = ptr;
        mReleaseAtFinalize = releaseAtFinalize;
        if( mPtr != 0 && resource != null ) {
            addResource( resource );
        }
    }



    public synchronized long pointer() {
        return mPtr;
    }


    public synchronized boolean addResource( Refable resource ) {
        if( mPtr == 0 || !mResourceList.add( resource ) ) {
            return false;
        }

        resource.ref();
        return true;
    }


    public synchronized boolean removeResource( Refable resource ) {
        if( mPtr == 0 || !mResourceList.remove( resource ) ) {
            return false;
        }

        resource.deref();
        return true;
    }


    
    @Override
    protected void freeObject() {
        long ptr;

        synchronized( this ) {
            ptr = mPtr;
            mPtr = 0;
        }

        if( ptr == 0 ) {
            return;
        }


        System.out.flush();

        try {
            release( ptr );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }

        for( Refable r : mResourceList ) {
            try {
                r.deref();
            } catch( Exception ex ) {}
        }

        mResourceList.clear();
    }


    @Override
    protected void finalize() throws Throwable {
        try {
            if( mPtr != 0 && mReleaseAtFinalize ) {
                release( mPtr );
                mPtr = 0;
            }
        } finally {
            super.finalize();
        }
    }



    private static native void retain( long ptr );

    private static native void release( long ptr );

}
