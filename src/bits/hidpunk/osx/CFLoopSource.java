package bits.hidpunk.osx;

import bits.langx.ref.Refable;

/** 
 * @author Philip DeCamp  
 */
public class CFLoopSource extends CFRef {
    
    public CFLoopSource(long ptr) {
        super(ptr, false, null);
    }
    
    public CFLoopSource(long ptr, Refable resource) {
        super(ptr, false, resource);
    }
        
}
