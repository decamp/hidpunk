package bits.hidpunk.movementmouse;

import bits.hidpunk.*;
import bits.hidpunk.driver.DriverGroup;

public class MovementMouseGroup extends DriverGroup<MovementMouseListener> {
    
    public static MovementMouseGroup newInstance() throws HidException {
        HidMatcher matcher = MovementMouse.newMatcherInstance();
        return new MovementMouseGroup(matcher);
    }    
    
    private MovementMouseGroup(HidMatcher matcher) {
        super(matcher);
    }
    
    protected MovementMouse newDriver(HidDevice device) {
        return MovementMouse.newInstance(device);
    }
    
}
