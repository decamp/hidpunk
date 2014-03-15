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
class IOKitException extends HidException {

    public IOKitException() {}

    public IOKitException( String msg ) {
        super( msg );
    }

}
