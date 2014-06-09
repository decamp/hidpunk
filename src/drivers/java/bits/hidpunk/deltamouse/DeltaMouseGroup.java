/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.deltamouse;

import bits.hidpunk.*;
import bits.hidpunk.driver.DriverGroup;

/**
 * @author Philip DeCamp
 */
public class DeltaMouseGroup extends DriverGroup<DeltaMouseListener> {
    
    public static DeltaMouseGroup create() throws HidException {
        HidMatcher matcher = DeltaMouse.createMatcher();
        return new DeltaMouseGroup( matcher );
    }
    
    
    protected DeltaMouse newDriver( HidDevice device ) {
        return DeltaMouse.create( device );
    }
    
    private DeltaMouseGroup( HidMatcher matcher ) {
        super( matcher );
    }

}
