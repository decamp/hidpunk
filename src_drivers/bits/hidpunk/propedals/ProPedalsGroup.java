/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.propedals;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;


/**
 * @author Philip DeCamp
 */
public class ProPedalsGroup extends DriverGroup<ProPedalsListener> {

    public static ProPedalsGroup create() throws HidException {
        HidMatcher matcher = ProPedals.createMatcher();
        return new ProPedalsGroup( matcher );
    }

    
    private ProPedalsGroup( HidMatcher matcher ) {
        super( matcher );
    }

    protected ProPedals newDriver( HidDevice device ) {
        return new ProPedals( device );
    }

}
