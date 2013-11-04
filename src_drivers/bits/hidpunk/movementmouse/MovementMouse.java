package bits.hidpunk.movementmouse;

import java.nio.ByteBuffer;
import java.util.*;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;



/**
 * This is a mouse driver that is more direct than the AWT mouse driver.
 * It does not provide the mouse position on screen, but instead gives you the 
 * movements of the mouse and the button presses.  This can be extremely useful
 * for things like a first person video game, where you want your mouse to control view
 * and to be able to turn around in circles, rather than in Java where the mouse 
 * input ends when the cursor reaches the end of the window.
 * <p>
 * Take note that this driver does not account for application focus.
 * 
 * @author decamp
 */
public class MovementMouse extends AbstractHidpunkDriver<MovementMouseListener> {

    
    public static final long USAGE_PAGE = 0x01;
    public static final long USAGE      = 0x02;

    
    public static HidMatcher newMatcherInstance() throws HidException {
        HidMatcher matcher = HidManager.getManager().newDeviceMatcher();
        matcher.setUsagePage(USAGE_PAGE);
        matcher.setUsage(USAGE);
        return matcher;
    }
    
    
    public static List<MovementMouse> findDevices() throws HidException {
        List<MovementMouse> ret = new ArrayList<MovementMouse>();
        HidMatcher matcher = newMatcherInstance();
        
        for(HidDevice dev: HidManager.getManager().findDevices(matcher)) {
            MovementMouse m = newInstance(dev);
            if(m != null)
                ret.add(m);
        }
        
        return ret;
    }
    
    
    static MovementMouse newInstance(HidDevice dev) {
        HidElement x = null;
        HidElement y = null;
        List<HidElement> buttons = new ArrayList<HidElement>();
        
        for(HidElement el: dev.flattenElements()) {
            switch(el.getUsagePage()) {
            case 0x01:
                switch(el.getUsage()) {
                case 0x30:
                    x = el;
                    break;
                case 0x31:
                    y = el;
                    break;
                }
                break;
            case 0x09:
                buttons.add(el);
                break;
            }
        }

        if(x == null || y == null)
            return null;
        
        return new MovementMouse(dev, x, y, buttons);
    }
    
    

    
    private final HidElement mXEl;
    private final HidElement mYEl;
    private final List<HidElement> mButtonEls;
    
    
    private MovementMouse( HidDevice device, 
                           HidElement x, 
                           HidElement y,
                           List<HidElement> buttons) 
    {
        super(device, MovementMouseListener.class);
        mXEl       = x;
        mYEl       = y;
        mButtonEls = buttons;
    }
    
    
    protected void interfaceOpened(HidInterface inter, MovementMouseListener listener) throws HidException {
        List<HidEventSource> sourceList = new ArrayList<HidEventSource>();
        sourceList.add(inter.newAsyncSource(0, new MouseHandler(listener), mXEl, mYEl));
        
        for(HidElement el: mButtonEls) {
            ButtonHandler t = new ButtonHandler(listener, el.getUsage());
            sourceList.add(inter.newAsyncSource(8, t, el));
        }
        
        for(HidEventSource source: sourceList)
            source.start();
        
        mLogger.fine("Successfully connected to " + mDevice);
    }
    
    
    private class MouseHandler implements HidValueListener {

        private MovementMouseListener mListener;
        
        
        MouseHandler(MovementMouseListener listener) {
            mListener = listener;
        }
        
        
        public void hidValuesReceived(ByteBuffer buf) {
            int vx = buf.getInt();
            int vy = buf.getInt();

            if(vx != 0 || vy != 0) {
                mListener.mouseMoved(System.currentTimeMillis() * 1000L, vx, vy);
            }
        }
        
        public void hidEventSourceClosing(Exception ex) {}

    }
    
    
    private class ButtonHandler implements HidValueListener {
        
        private final MovementMouseListener mListener;
        private final int mButton;
        private boolean mPressed = false;
        
        
        public ButtonHandler(MovementMouseListener listener, int button) {
            mListener = listener;
            mButton = button;
        }

        
        
        public void hidValuesReceived(ByteBuffer buf) {
            boolean pressed = buf.getInt() != 0;
            
            if(pressed == mPressed)
                return;
            
            mPressed = pressed;
            if(mPressed) {
                mListener.mouseButtonPressed(System.currentTimeMillis() * 1000L, mButton);
            }else{
                mListener.mouseButtonReleased(System.currentTimeMillis() * 1000L, mButton);
            }    
        }
        
        public void hidEventSourceClosing(Exception ex) {}
        
    }

}
