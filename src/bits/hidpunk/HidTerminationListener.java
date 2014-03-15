/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * HidRemovalListener is used to received HID removal notifications.
 * 
 * @author Philip DeCamp
 */
public interface HidTerminationListener {

    /**
     * @param device A HidDevice that is no longer accessible.
     */
    public void hidTerminated( HidDevice device );

}
