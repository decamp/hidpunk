/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.util.ref.Refable;


/**
 * @author Philip DeCamp
 */
public class CFLoopTimer extends CFRef {

    public CFLoopTimer( long ptr ) {
        super( ptr, false, null );
    }

    public CFLoopTimer( long ptr, Refable resource ) {
        super( ptr, false, resource );
    }

}
