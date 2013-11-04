package bits.hidpunk.spacenavigator;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;

import java.nio.ByteBuffer;
import java.util.*;


/** 
 * @author Philip DeCamp  
 */
public class SpaceNavigator extends AbstractHidpunkDriver<SpaceNavigatorListener> {

    public static final long VENDOR_ID = 0x046DL;
    public static final long PRODUCT_ID = 0xC626L;

    
    public static HidMatcher newMatcherInstance() throws HidException {
        HidMatcher matcher = HidManager.getManager().newDeviceMatcher();
        matcher.setVendorID(VENDOR_ID);
        matcher.setProductID(PRODUCT_ID);
        return matcher;
    }
    
    
    public static List<SpaceNavigator> findDevices() throws HidException {
        List<SpaceNavigator> ret = new ArrayList<SpaceNavigator>();
        HidMatcher matcher = newMatcherInstance();
        
        for(HidDevice dev: HidManager.getManager().findDevices(matcher)) {
            ret.add(new SpaceNavigator(dev));
        }
        
        return ret;
    }
    

    
    SpaceNavigator(HidDevice device) {
        super(device, SpaceNavigatorListener.class);
        
    }
    
    
    @Override
    protected void interfaceOpened(HidInterface inter, SpaceNavigatorListener listener) throws HidException {
        HidElement[] buttonElements = new HidElement[32];
        HidElement[] puckElements = new HidElement[6];
        Stack<HidElement> stack = new Stack<HidElement>();
        
        for(HidElement el: mDevice.getElements())
            stack.add(el);
        
        while(!stack.isEmpty()) {
            HidElement el = stack.pop();
            
            if(el.getChildCount() > 0) {
                for(HidElement c: el.getChildren()) {
                    stack.push(c);
                }
            }else{
                int page = el.getUsagePage();
                int use = el.getUsage();
                
                switch(page) {
                case 0x01:
                {
                    if(use < 0x30 || use > 0x35)
                        break;
                    
                    puckElements[use - 0x30] = el;
                    break;
                }
                case 0x09:
                {
                    if(use < 0 || use > 31) 
                        break;
                    
                    buttonElements[use] = el;
                    break;
                }}
            }
        }

        List<HidEventSource> sources = new ArrayList<HidEventSource>();
        sources.add(inter.newPollingSource(50000L, 8, new PuckAdapter(listener), puckElements));
        //sources.add(inter.newAsyncSource(1,new PuckAdapter(listener), puckElements)); 
        
        for(int i = 0; i < buttonElements.length; i++) {
            if(buttonElements[i] == null)
                continue;

            sources.add(inter.newAsyncSource(8, new ButtonAdapter(listener, i), buttonElements[i]));
        }
        
        for(HidEventSource s: sources) { 
            s.start();
        }
    }
    
    
    
    
    private final class PuckAdapter implements HidValueListener {

        final SpaceNavigatorListener mCaster;
        private int[] mPuck = new int[6];
        
        PuckAdapter(SpaceNavigatorListener caster) {
            mCaster = caster;
        }
        
        
        public void hidValuesReceived(ByteBuffer buf) {
            boolean send = false;
            
            for(int i = 0; i < mPuck.length; i++) {
                int v = buf.getInt();
                if(v != mPuck[i]) {
                    send = true;
                    mPuck[i] = v;
                }
            }
            
            if(send) {
                mCaster.spacePuckMoved(mPuck[0], mPuck[1], mPuck[2], mPuck[3], mPuck[4], mPuck[5]); 
            }
        }
        
        public void hidEventSourceClosing(Exception ex) {}

    }


    private final class ButtonAdapter implements HidValueListener {

        final SpaceNavigatorListener mCaster;
        final int mId;

        
        public ButtonAdapter(SpaceNavigatorListener caster, int id) {
            mCaster = caster;
            mId = id;
        }
        
        
        public void hidEventSourceClosing(Exception ex) {}

        public void hidValuesReceived(ByteBuffer buf) {
            if(buf.getInt() == 0) {
                mCaster.spaceButtonUp(mId);
            }else{
                mCaster.spaceButtonDown(mId);
            }
        }
        
    }


}
