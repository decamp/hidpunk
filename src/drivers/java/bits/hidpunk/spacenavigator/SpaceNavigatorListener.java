/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.spacenavigator;

/**
 * @author Philip DeCamp
 */
public interface SpaceNavigatorListener {
    public void spacePuckMoved( int x, int y, int z, int rx, int ry, int rz );
    public void spaceButtonDown( int id );
    public void spaceButtonUp( int id );
}
