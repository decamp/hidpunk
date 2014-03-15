/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.driver;

import java.util.*;

import bits.hidpunk.*;


/**
 * @author Philip DeCamp
 */
public class DebugDriver extends AbstractHidpunkDriver<HidEventListener> {


    public static List<DebugDriver> findDevices( Long vendorID,
                                                 Long productID,
                                                 long pollMicros, // <= 0 for // async
                                                 boolean broadcastChangesOnly,
                                                 boolean addPrintListener )
                                                 throws HidException
    {
        List<HidDevice> devices = findHidDevices( vendorID, productID );
        List<DebugDriver> ret = new ArrayList<DebugDriver>( devices.size() );
        PrintListener printer = (addPrintListener ? new PrintListener() : null);

        for( HidDevice dev : devices ) {
            DebugDriver driver = new DebugDriver( dev, pollMicros, broadcastChangesOnly );

            if( printer != null )
                driver.addListener( printer );

            ret.add( driver );
        }

        return ret;
    }


    private final long mPollMicros;
    private final boolean mBroadcastChanges;

    
    public DebugDriver( HidDevice device, long pollMicros, boolean broadcastChanges ) {
        super( device, HidEventListener.class );
        mPollMicros = pollMicros;
        mBroadcastChanges = broadcastChanges;
    }


    @Override
    protected void interfaceOpened( HidInterface inter, HidEventListener listener ) throws HidException {
        for( HidElement e : mDevice.flattenElements() ) {
            // /System.out.println(e.getSize());
            HidEventSource source;

            if( mPollMicros <= 0.0 ) {
                source = inter.createAsyncSource( 8, new CloneListener( listener ), e );
            } else {
                source = inter.createPollingSource( mPollMicros, 0, new CloneListener( listener ), e );
            }

            source.start();
        }
    }



    private class CloneListener implements HidEventListener {

        private final HidEventListener mReceiver;
        private boolean mFirst = true;
        private int mLastValue;

        CloneListener( HidEventListener receiver ) {
            mReceiver = receiver;
        }


        public void hidEventSourceClosing( Exception ex ) {
            mReceiver.hidEventSourceClosing( ex );
        }

        public void hidEventsReceived( HidEvent[] events ) {
            if( mBroadcastChanges ) {
                if( events[0].mStale || events[0].mValue == mLastValue && !mFirst )
                    return;

                mLastValue = events[0].mValue;
            }

            mFirst = false;
            mReceiver.hidEventsReceived( new HidEvent[]{ events[0].clone() } );
        }
    }


    private static class PrintListener implements HidEventListener {
        public void hidEventsReceived( HidEvent[] events ) {
            for( HidEvent e : events ) {
                System.out.format( "HIDEvent: ckie: 0x%04X  v: %-8d     payload: %-8d\n",
                                   e.mCookie,
                                   e.mValue,
                                   e.mLongValueSize );
                // System.out.println("HIDEvent: " + e.mCookie + "\t" + e.mValue
                // + "\t" + e.mLongValueSize);
            }
        }

        public void hidEventSourceClosing( Exception ex ) {}
    }

}
