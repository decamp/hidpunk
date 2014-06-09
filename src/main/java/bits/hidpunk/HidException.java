/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * @author Philip DeCamp
 */
public class HidException extends Exception {

    public HidException() {}

    public HidException( String msg ) {
        super( msg );
    }

    public HidException( Throwable cause ) {
        super( cause );
    }

    public HidException( String msg, Throwable cause ) {
        super( msg, cause );
    }

}
