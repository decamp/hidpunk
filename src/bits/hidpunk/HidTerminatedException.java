/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * @author Philip DeCamp
 */
public class HidTerminatedException extends HidException {

    public HidTerminatedException() {}

    public HidTerminatedException( String msg ) {
        super( msg );
    }

}
