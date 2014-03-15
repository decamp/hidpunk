/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.deltamouse;

public interface DeltaMouseListener {
    public void mouseMoved( long timeMicros, int dx, int dy );
    public void mouseButtonPressed( long timeMicros, int button );
    public void mouseButtonReleased( long timeMicros, int button );
}
