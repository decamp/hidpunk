package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public class HidException extends Exception {

    public HidException() {}
    
    public HidException(String msg) {
        super(msg);
    }
    
    public HidException(Throwable cause) {
        super(cause);
    }
    
    public HidException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
