package bits.hidpunk.propedals;

/** 
 * @author Philip DeCamp  
 */
public interface ProPedalsListener {
    public void receivedProPedalEvent(int left, int right, int shift);
}
