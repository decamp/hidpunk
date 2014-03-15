/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * @author Philip DeCamp
 */
public class HidEventSourceClosedException extends HidException {

    public HidEventSourceClosedException() {}

    public HidEventSourceClosedException( String msg ) {
        super( msg );
    }

    public HidEventSourceClosedException( Throwable cause ) {
        super( cause );
    }

    public HidEventSourceClosedException( String msg, Throwable cause ) {
        super( msg, cause );
    }

}
