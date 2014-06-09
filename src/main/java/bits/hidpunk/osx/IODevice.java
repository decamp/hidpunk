/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

/**
 * @author Philip DeCamp
 */
class IODevice extends IOObject {

    public IODevice( long ioref ) {
        super( ioref, true );
    }

}
