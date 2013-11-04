package bits.hidpunk.osx;

import bits.hidpunk.HidException;

/** 
 * @author Philip DeCamp  
 */
class IOIter extends IOObject {

    public IOIter(long ptr) {
        super(ptr);
    }
    
    public IOIter(long ptr, boolean releaseAtFinalize) {
        super(ptr, releaseAtFinalize);
    }

    
    
    public synchronized long next() throws HidException {
        return next(getPointer());
    }
    
    
    
    private static native long next(long pointer);
    
}
