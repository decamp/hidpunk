package bits.hidpunk.propedals;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;

/** 
 * @author Philip DeCamp  
 */
public class ProPedalsGroup extends DriverGroup<ProPedalsListener> {
    
    public static ProPedalsGroup newInstance() throws HidException {
        HidMatcher matcher = ProPedals.newMatcherInstance();
        return new ProPedalsGroup(matcher);
    }
    
    private ProPedalsGroup(HidMatcher matcher) {
        super(matcher);
    }
    
    protected ProPedals newDriver(HidDevice device) {
        return new ProPedals(device);
    }

}
