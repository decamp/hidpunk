package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public class HidEventSourceClosedException extends HidException {
    
    public HidEventSourceClosedException() {}
    
    public HidEventSourceClosedException(String msg) {
        super(msg);
    }

    public HidEventSourceClosedException(Throwable cause) {
        super(cause);
    }
    
    public HidEventSourceClosedException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
