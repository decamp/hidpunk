/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.deltamouse;

import java.nio.ByteBuffer;
import java.util.*;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;

/**
 * A mouse driver that provides changes in mouse position ("deltas") 
 * instead of an absolute position, like the AWT mouse system.
 * This is essential for things like a first person interfaces, where you
 * need to control a camera with a mouse and don't care about a screen cursor.
 * This is not possible with the AWT mouse driver because the mouse position is 
 * always limited to the boundaries of the screen.
 * <p>
 * NB this driver does not account for application focus.
 * <p>
 * TODO: Add support for window focus listening.
 * 
 * @author decamp
 */
public class DeltaMouse extends AbstractHidpunkDriver<DeltaMouseListener> {
    
    public static final long USAGE_PAGE = 0x01;
    public static final long USAGE      = 0x02;


    public static HidMatcher createMatcher() throws HidException {
        HidMatcher matcher = HidManager.getManager().createMatcher();
        matcher.usagePage( USAGE_PAGE );
        matcher.usage( USAGE );
        return matcher;
    }
    

    public static List<DeltaMouse> findDevices() throws HidException {
        List<DeltaMouse> ret = new ArrayList<DeltaMouse>();
        HidMatcher matcher = createMatcher();

        for( HidDevice dev : HidManager.getManager().findDevices( matcher ) ) {
            DeltaMouse m = create( dev );
            if( m != null )
                ret.add( m );
        }

        return ret;
    }


    static DeltaMouse create( HidDevice dev ) {
        HidElement x = null;
        HidElement y = null;
        List<HidElement> buttons = new ArrayList<HidElement>();

        for( HidElement el : dev.flattenElements() ) {
            switch( el.usagePage() ) {
            case 0x01:
                switch( el.usage() ) {
                case 0x30:
                    x = el;
                    break;
                case 0x31:
                    y = el;
                    break;
                }
                break;
            case 0x09:
                buttons.add( el );
                break;
            }
        }

        if( x == null || y == null )
            return null;

        return new DeltaMouse( dev, x, y, buttons );
    }



    private final HidElement mXEl;
    private final HidElement mYEl;
    private final List<HidElement> mButtonEls;


    private DeltaMouse( HidDevice device,
                        HidElement x,
                        HidElement y,
                        List<HidElement> buttons )
    {
        super( device, DeltaMouseListener.class );
        mXEl = x;
        mYEl = y;
        mButtonEls = buttons;
    }


    protected void interfaceOpened( HidInterface inter, DeltaMouseListener listener ) throws HidException {
        List<HidEventSource> sourceList = new ArrayList<HidEventSource>();
        sourceList.add( inter.createAsyncSource( 0, new MouseHandler( listener ), mXEl, mYEl ) );

        for( HidElement el : mButtonEls ) {
            ButtonHandler t = new ButtonHandler( listener, el.usage() );
            sourceList.add( inter.createAsyncSource( 8, t, el ) );
        }

        for( HidEventSource source : sourceList )
            source.start();

        mLogger.fine( "Successfully connected to " + mDevice );
    }


    private class MouseHandler implements HidValueListener {

        private DeltaMouseListener mListener;


        MouseHandler( DeltaMouseListener listener ) {
            mListener = listener;
        }


        public void hidValuesReceived( ByteBuffer buf ) {
            int vx = buf.getInt();
            int vy = buf.getInt();

            if( vx != 0 || vy != 0 ) {
                mListener.mouseMoved( System.currentTimeMillis() * 1000L, vx, vy );
            }
        }

        public void hidEventSourceClosing( Exception ex ) {}

    }


    private class ButtonHandler implements HidValueListener {
        
        private final DeltaMouseListener mListener;
        private final int mButton;
        private boolean mPressed = false;
        
        public ButtonHandler( DeltaMouseListener listener, int button ) {
            mListener = listener;
            mButton = button;
        }
        
        
        public void hidValuesReceived( ByteBuffer buf ) {
            boolean pressed = buf.getInt() != 0;

            if( pressed == mPressed )
                return;

            mPressed = pressed;
            if( mPressed ) {
                mListener.mouseButtonPressed( System.currentTimeMillis() * 1000L, mButton );
            } else {
                mListener.mouseButtonReleased( System.currentTimeMillis() * 1000L, mButton );
            }
        }
        
        public void hidEventSourceClosing( Exception ex ) {}
        
    }
}
