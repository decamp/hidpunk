/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * Interface for receiving notifications when a device is found.
 * 
 * @author Philip DeCamp
 */
public interface HidMatchListener {
    public void hidFound( HidDevice device );
}