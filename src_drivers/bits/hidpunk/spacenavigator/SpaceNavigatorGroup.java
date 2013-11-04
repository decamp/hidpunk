package bits.hidpunk.spacenavigator;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;

/** 
 * @author Philip DeCamp  
 */
public class SpaceNavigatorGroup extends DriverGroup<SpaceNavigatorListener> {

    
    public static SpaceNavigatorGroup newInstance() throws HidException {
        HidMatcher matcher = SpaceNavigator.newMatcherInstance();
        return new SpaceNavigatorGroup(matcher);   
    }
    
    
    private SpaceNavigatorGroup(HidMatcher matcher) {
        super(matcher);
    }

    
    @Override
    protected SpaceNavigator newDriver(HidDevice device) {
        return new SpaceNavigator(device);
    }
    
}
