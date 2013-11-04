package bits.hidpunk;

import java.nio.ByteBuffer;

/** 
 * @author Philip DeCamp  
 */
public class HidEvent implements Cloneable {

    public int mType;
    public int mCookie;
    public int mValue;
    public long mTimestampMicros;
    public int mLongValueSize;
    public ByteBuffer mLongValue;
    public boolean mStale;
    
    
    public HidEvent clone() {
        try{
            HidEvent ret = (HidEvent)super.clone();
            
            if(mLongValue != null) {
                ByteBuffer val = mLongValue.duplicate();
                ByteBuffer buf = ByteBuffer.allocate(mLongValue.remaining());
                val.get(buf.array());
                
                ret.mLongValue = buf;
            }
            
            return ret;
            
        }catch(Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
            return null;
        }
    }
    
}
