/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * @author Philip DeCamp
 */
public class HidInterfaceClosedException extends HidException {

    public HidInterfaceClosedException() {}

    public HidInterfaceClosedException( String msg ) {
        super( msg );
    }

    public HidInterfaceClosedException( Throwable cause ) {
        super( cause );
    }

    public HidInterfaceClosedException( String msg, Throwable cause ) {
        super( msg, cause );
    }

}
