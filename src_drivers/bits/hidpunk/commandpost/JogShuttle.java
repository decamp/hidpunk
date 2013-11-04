package bits.hidpunk.commandpost;

import bits.hidpunk.*;

import java.util.*;

/** 
 * Hidpunk driver for a JogShuttle controllers.
 * 
 * @author Philip DeCamp  
 */
public class JogShuttle {

    static final int USAGE_WHEEL = 0x38;  //Jog
    static final int USAGE_DIAL = 0x37;   //Shuttle
    static final int USAGE_SLIDER = 0x36; //Fader
    static final int USAGE_X = 0x30;      //Joystick X
    static final int USAGE_Y = 0x31;      //Joystick Y
    
    
    
    
    
    public static List<JogShuttle> findDevices() throws HidException {
        HidManager man = HidManager.getManager();
        HidMatcher matcher = man.newDeviceMatcher();
        return findDevices(matcher);
    }
    
    public static List<JogShuttle> findDevices(Long vendorID, Long productID) throws HidException {
        HidManager man = HidManager.getManager();
        HidMatcher matcher = man.newDeviceMatcher();
        if(vendorID != null)
            matcher.setVendorID(vendorID);
        
        if(productID != null)
            matcher.setProductID(productID);
        
        return findDevices(matcher);
    }
    
    public static List<JogShuttle> findDevices(HidMatcher matcher) throws HidException {
        List<HidDevice> devices = HidManager.getManager().findDevices(matcher);
        List<JogShuttle> ret = new ArrayList<JogShuttle>();
        
        for(HidDevice dev: devices) {
            HidElement[] elements = dev.flattenElements();
            HidElement jogEl = null;
            HidElement shuttleEl = null;
            HidElement xEl = null;
            HidElement yEl = null;
            
            List<HidElement> buttons = new ArrayList<HidElement>();
            List<HidElement> faders = new ArrayList<HidElement>();
            
            
            for(HidElement e: elements) {
                if(e.getUsagePage() == 0x01) {
                    switch(e.getUsage()) {
                    case USAGE_WHEEL:
                        jogEl = e;
                        break;
                    case USAGE_DIAL:
                        shuttleEl = e;
                        break;
                    case USAGE_SLIDER:
                        faders.add(e);
                        break;
                    case USAGE_X:
                        xEl = e;
                        break;
                    case USAGE_Y:
                        yEl = e;
                    }
                }else if(e.getUsagePage() == 0x09) {
                    buttons.add(e);
                }
            }
            
            if(jogEl == null || shuttleEl == null)
                continue;
            
            
            //ret.add(new JogShuttle(jogEl, shuttleEl, fader, xEl, yEl, buttons));
        }
        
        return ret;
    }
    
}
