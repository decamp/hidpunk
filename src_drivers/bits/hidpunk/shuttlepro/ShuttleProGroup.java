package bits.hidpunk.shuttlepro;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;

/** 
 * @author Philip DeCamp  
 */
public class ShuttleProGroup extends DriverGroup<ShuttleProListener> {

    public static ShuttleProGroup newInstance() throws HidException {
        HidMatcher matcher = ShuttlePro.newMatcherInstance();
        return new ShuttleProGroup(matcher);
    }
    
    private ShuttleProGroup(HidMatcher matcher) {
        super(matcher);
    }
    
    protected ShuttlePro newDriver(HidDevice device) {
        return new ShuttlePro(device);
    }

}
