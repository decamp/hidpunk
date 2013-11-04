package bits.hidpunk.spacenavigator;

/** 
 * @author Philip DeCamp  
 */
public interface SpaceNavigatorListener {
    public void spacePuckMoved(int x, int y, int z, int rx, int ry, int rz);
    public void spaceButtonDown(int id);
    public void spaceButtonUp(int id);
}
