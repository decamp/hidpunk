/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.shuttlepro;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;


/**
 * @author Philip DeCamp
 */
public class ShuttleProGroup extends DriverGroup<ShuttleProListener> {
    
    public static ShuttleProGroup create() throws HidException {
        HidMatcher matcher = ShuttlePro.createMatcher();
        return new ShuttleProGroup( matcher );
    }


    private ShuttleProGroup( HidMatcher matcher ) {
        super( matcher );
    }

    protected ShuttlePro newDriver( HidDevice device ) {
        return new ShuttlePro( device );
    }

}
