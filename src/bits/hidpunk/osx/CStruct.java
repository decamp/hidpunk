/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.hidpunk.HidException;
import bits.langx.ref.AbstractRefable;


/**
 * @author Philip DeCamp
 */
abstract class CStruct extends AbstractRefable {

    private long mPointer;
    
    protected CStruct( long pointer ) {
        mPointer = pointer;
    }


    public synchronized long getPointer() {
        return mPointer;
    }

    public synchronized boolean isValid() {
        return mPointer != 0;
    }
    

    @Override
    protected void freeObject() {
        long ptr;

        synchronized( this ) {
            ptr = mPointer;
            mPointer = 0;
        }
        
        if( ptr == 0 ) {
            return;
        }
        
        try {
            destruct( ptr );
        } catch( HidException ex ) {
            ex.printStackTrace();
        }
    }

    protected abstract void destruct( long ptr ) throws HidException;

}
