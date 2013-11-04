package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public class HidInterfaceClosedException extends HidException {
    
    public HidInterfaceClosedException() {}
    
    public HidInterfaceClosedException(String msg) {
        super(msg);
    }

    public HidInterfaceClosedException(Throwable cause) {
        super(cause);
    }
    
    public HidInterfaceClosedException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
