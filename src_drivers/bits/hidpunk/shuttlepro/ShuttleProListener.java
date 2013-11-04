package bits.hidpunk.shuttlepro;

/** 
 * @author Philip DeCamp  
 */
public interface ShuttleProListener {
    public void deviceJogChanged(int value, int delta);
    public void deviceShuttleChanged(int value);
    public void deviceButtonPressed(int id);
    public void deviceButtonReleased(int id);
}
