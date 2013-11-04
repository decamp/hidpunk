package bits.hidpunk.osx;

import bits.hidpunk.HidException;

/** 
 * @author Philip DeCamp  
 */
class IOKitException extends HidException {

    public IOKitException() {}
    
    public IOKitException(String msg) {
        super(msg);
    }

}
