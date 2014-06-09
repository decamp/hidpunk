/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.propedals;

import java.nio.ByteBuffer;
import java.util.*;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;



/**
 * @author Philip DeCamp
 */
public class ProPedals extends AbstractHidpunkDriver<ProPedalsListener> {

    public static final long VENDOR_ID  = 0x068EL;
    public static final long PRODUCT_ID = 0x00F2L;


    public static HidMatcher createMatcher() throws HidException {
        HidMatcher matcher = HidManager.getManager().createMatcher();
        matcher.vendorID( VENDOR_ID );
        matcher.productID( PRODUCT_ID );
        return matcher;
    }


    public static List<ProPedals> findDevices() throws HidException {
        List<ProPedals> ret = new ArrayList<ProPedals>();
        HidMatcher matcher = createMatcher();

        for( HidDevice dev : HidManager.getManager().findDevices( matcher ) ) {
            ret.add( new ProPedals( dev ) );
        }

        return ret;
    }
    
    

    ProPedals( HidDevice device ) {
        super( device, ProPedalsListener.class );
    }


    protected void interfaceOpened( HidInterface inter, ProPedalsListener listener ) throws HidException {
        HidElement parent = mDevice.element( 0 ).child( 0 );
        HidEventSource source = inter.createPollingSource( 40000, 0, new PedalHandler( listener ), parent.children() );
        source.start();
    }

    private class PedalHandler implements HidValueListener {

        private final ProPedalsListener mCaster;
        private int mLeft = -1;
        private int mRight = -1;
        private int mShift = -1;

        PedalHandler( ProPedalsListener caster ) {
            mCaster = caster;
        }


        public void hidValuesReceived( ByteBuffer buf ) {
            int left = buf.getInt();
            int right = buf.getInt();
            int shift = buf.getInt();

            if( left == mLeft && right == mRight && shift == mShift )
                return;

            mLeft = left;
            mRight = right;
            mShift = shift;
            mCaster.receivedProPedalEvent( left, right, shift );
        }

        public void hidEventSourceClosing( Exception ex ) {}
    }

}
