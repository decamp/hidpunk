/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

import java.util.*;

import bits.hidpunk.osx.*;
import bits.util.platform.OperatingSystem;


/**
 * @author Philip DeCamp
 */
public abstract class HidManager {

    private static boolean sInit = false;
    private static HidManager sManager = null;


    private static synchronized void init() throws HidException {
        if( sInit ) {
            return;
        }
        sInit = true;
        if( OperatingSystem.local() != OperatingSystem.OSX ) {
            throw new HidException( "Only OSX is supported at this time." );
        }

        try {
            System.loadLibrary( "hidpunk" );
        } catch( SecurityException ex ) {
            throw new HidException( "Not allowed to load library.", ex );
        } catch( UnsatisfiedLinkError ex ) {
            throw new HidException( "Library not found.", ex );
        } catch( Throwable ex ) {
            throw new HidException( "Unknown exception.", ex );
        }

        sManager = new OsxHidManager();
    }


    public static synchronized HidManager getManager() throws HidException {
        if( sManager != null ) {
            return sManager;
        }
        if( !sInit ) {
            init();
        }
        if( sManager != null ) {
            return sManager;
        }

        throw new HidException( "HidManager could not be instantiated." );
    }

    public abstract HidMatcher createMatcher() throws HidException;

    public abstract List<HidDevice> findDevices( HidMatcher matcher ) throws HidException;

    public abstract List<HidDevice> addMatchListener( HidMatcher matcher, HidMatchListener listener ) throws HidException;

    public abstract void removeMatchListener( HidMatchListener listener ) throws HidException;


    @Deprecated
    public abstract HidMatcher newDeviceMatcher() throws HidException;

}
