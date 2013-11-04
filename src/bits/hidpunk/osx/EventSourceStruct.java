package bits.hidpunk.osx;

import bits.hidpunk.HidException;

/** 
 * @author Philip DeCamp  
 */
public class EventSourceStruct extends CStruct {

    public EventSourceStruct(long ptr) {
        super(ptr);
    }

    @Override
    protected native void destruct(long ptr) throws HidException;
    
}
