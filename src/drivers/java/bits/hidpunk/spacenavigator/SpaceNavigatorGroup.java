/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.spacenavigator;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;


/**
 * @author Philip DeCamp
 */
public class SpaceNavigatorGroup extends DriverGroup<SpaceNavigatorListener> {

    public static SpaceNavigatorGroup create() throws HidException {
        HidMatcher matcher = SpaceNavigator.createMatcher();
        return new SpaceNavigatorGroup( matcher );
    }

    
    private SpaceNavigatorGroup( HidMatcher matcher ) {
        super( matcher );
    }

    @Override
    protected SpaceNavigator newDriver( HidDevice device ) {
        return new SpaceNavigator( device );
    }
    
}
