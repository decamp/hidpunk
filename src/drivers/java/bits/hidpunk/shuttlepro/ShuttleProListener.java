/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.shuttlepro;

/**
 * @author Philip DeCamp
 */
public interface ShuttleProListener {
    public void deviceJogChanged( int value, int delta );
    public void deviceShuttleChanged( int value );
    public void deviceButtonPressed( int id );
    public void deviceButtonReleased( int id );
}
