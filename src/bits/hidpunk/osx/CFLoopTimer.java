package bits.hidpunk.osx;

import bits.langx.ref.Refable;

/** 
 * @author Philip DeCamp  
 */
public class CFLoopTimer extends CFRef {
    
    public CFLoopTimer(long ptr) {
        super(ptr, false, null);
    }
    
    public CFLoopTimer(long ptr, Refable resource) {
        super(ptr, false, resource);
    }

}
