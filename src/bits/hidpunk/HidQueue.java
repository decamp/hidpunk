package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public interface HidQueue {

    public void start() throws HidException;
    public void stop() throws HidException;
    
    public void addElement(HidElement el) throws HidException;
    public void removeElement(HidElement el) throws HidException;

}
