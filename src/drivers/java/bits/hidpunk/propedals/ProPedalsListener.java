/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.propedals;

/**
 * @author Philip DeCamp
 */
public interface ProPedalsListener {
    public void receivedProPedalEvent( int left, int right, int shift );
}
