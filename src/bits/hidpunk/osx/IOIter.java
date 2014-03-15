/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.hidpunk.HidException;


/**
 * @author Philip DeCamp
 */
class IOIter extends IOObject {

    public IOIter( long ptr ) {
        super( ptr );
    }

    public IOIter( long ptr, boolean releaseAtFinalize ) {
        super( ptr, releaseAtFinalize );
    }


    public synchronized long next() throws HidException {
        return next( pointer() );
    }


    private static native long next( long pointer );

}
