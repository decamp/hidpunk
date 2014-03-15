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
class CFException extends HidException {

    public CFException() {}

    public CFException( String msg ) {
        super( msg );
    }

}
