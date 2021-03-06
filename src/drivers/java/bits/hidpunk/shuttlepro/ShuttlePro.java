/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.shuttlepro;

import java.nio.ByteBuffer;
import java.util.*;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;

/**
 * @author Philip DeCamp
 */
public class ShuttlePro extends AbstractHidpunkDriver<ShuttleProListener> {

    public static final long VENDOR_ID = 0x0B33L;
    public static final long PRODUCT_ID = 0x0030L;


    public static HidMatcher createMatcher() throws HidException {
        HidMatcher matcher = HidManager.getManager().createMatcher();
        matcher.vendorID( VENDOR_ID );
        matcher.productID( PRODUCT_ID );
        return matcher;
    }

    public static List<ShuttlePro> findDevices() throws HidException {
        List<ShuttlePro> ret = new ArrayList<ShuttlePro>();
        HidMatcher matcher = createMatcher();
        for( HidDevice dev : HidManager.getManager().findDevices( matcher ) ) {
            ret.add( new ShuttlePro( dev ) );
        }

        return ret;
    }


    private final HidElement mJogElement;
    private final HidElement mShuttleElement;
    private final List<HidElement> mButtonElements;


    ShuttlePro( HidDevice device ) {
        super( device, ShuttleProListener.class );

        HidElement jogElement = null;
        HidElement shuttleElement = null;
        mButtonElements = new ArrayList<HidElement>();
        HidElement[] elements = device.flattenElements();

        for( HidElement el : elements ) {
            if( el.usagePage() == 0x09 ) {
                mButtonElements.add( el );
            } else if( el.usagePage() == 0x01 ) {
                if( el.usage() == 0x37 ) {
                    jogElement = el;
                } else if( el.usage() == 0x38 ) {
                    shuttleElement = el;
                }
            }
        }

        mJogElement = jogElement;
        mShuttleElement = shuttleElement;
    }



    protected void interfaceOpened( HidInterface inter, ShuttleProListener listener ) throws HidException {
        List<HidEventSource> sourceList = new ArrayList<HidEventSource>( 20 );

        if( mJogElement != null && mShuttleElement != null ) {
            JogHandler t = new JogHandler( listener,
                                           inter.valueOf( mJogElement ),
                                           inter.valueOf( mShuttleElement ) );
            HidEventSource source = inter.createPollingSource( 40000, 0, t, mJogElement, mShuttleElement );
            sourceList.add( source );
        }

        for( HidElement el : mButtonElements ) {
            ButtonHandler t = new ButtonHandler( listener, el.usage() );
            HidEventSource source = inter.createAsyncSource( 8, t, el );
            sourceList.add( source );
        }

        for( HidEventSource source : sourceList )
            source.start();

        mLogger.fine( "Successfully connected to " + mDevice );
    }

    
    private class JogHandler implements HidValueListener {

        private final ShuttleProListener mListener;
        private int mJogValue;
        private int mShuttleValue;


        public JogHandler( ShuttleProListener listener, int initJogValue, int initShuttleValue ) {
            mListener = listener;
            mJogValue = initJogValue;
            mShuttleValue = initShuttleValue;
        }



        public void hidValuesReceived( ByteBuffer buf ) {
            int value = buf.getInt();

            if( value != mJogValue ) {
                int delta = (value + 256 - mJogValue) % 256;
                if( delta > 127 )
                    delta -= 256;

                mJogValue = value;
                mListener.deviceJogChanged( mJogValue, delta );
            }

            value = buf.getInt();
            if( value != mShuttleValue ) {
                mShuttleValue = value;
                mListener.deviceShuttleChanged( value );
            }
        }

        public void hidEventSourceClosing( Exception ex ) {}

    }


    private class ButtonHandler implements HidValueListener {

        private final ShuttleProListener mListener;
        private final int mButton;
        private boolean mPressed = false;


        public ButtonHandler( ShuttleProListener listener, int button ) {
            mListener = listener;
            mButton = button;
        }



        public void hidValuesReceived( ByteBuffer buf ) {
            boolean pressed = buf.getInt() != 0;

            if( pressed == mPressed )
                return;

            mPressed = pressed;
            if( mPressed ) {
                mListener.deviceButtonPressed( mButton );
            } else {
                mListener.deviceButtonReleased( mButton );
            }
        }

        public void hidEventSourceClosing( Exception ex ) {}

    }

}
