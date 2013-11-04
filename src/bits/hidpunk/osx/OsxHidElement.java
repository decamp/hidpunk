package bits.hidpunk.osx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import bits.hidpunk.*;


/** 
 * @author Philip DeCamp  
 */
public class OsxHidElement implements HidElement {

    
    static OsxHidElement[] findElements(OsxHidDevice device, CFRef dict) throws HidException {
        return findElements(device, dict.getPointer());
    }

    
    private static OsxHidElement[] findElements(OsxHidDevice device, long dictPtr) throws HidException {
        long arrPtr = getElementValue(dictPtr);
        int len = getArrayLength(arrPtr);
        
        if(len < 1)
            return new OsxHidElement[0];
        
        OsxHidElement[] ret = new OsxHidElement[len];
        
        for(int i = 0; i < len; i++) {
            long elPtr = getArrayElement(arrPtr, i); 
            ret[i] = constructElement(device, elPtr);
        }
        
        return ret;
    }
    
    
    private static OsxHidElement constructElement(OsxHidDevice device, long elPtr) throws HidException {
        ByteBuffer buf = ByteBuffer.allocateDirect(360);
        buf.order(ByteOrder.nativeOrder());
        
        boolean hasChildren = queryElementInfo(elPtr, buf);
        OsxHidElement[] children;
        
        if(hasChildren) {
            children = findElements(device, elPtr);
        }else{
            children = new OsxHidElement[0];
        }
        
        return new OsxHidElement(device, buf, children);
    }
    
    
    
    private final OsxHidDevice mDevice;
    private final int mType;
    private final int mUsagePage;
    private final int mUsage;
    private final int mCookie;
    private final long mMin;
    private final long mMax;
    private final long mScaledMin;
    private final long mScaledMax;
    private final long mSize;
    private final boolean mIsRelative;
    private final boolean mIsWrapping;
    private final boolean mIsNonLinear;
    private final boolean mHasPreferredState;
    private final boolean mHasNullState;
    private final int mUnit;
    private final int mUnitExponent;
    private final String mName;
        
    private final OsxHidElement[] mChildren;
    
    
    
    private OsxHidElement(OsxHidDevice device, ByteBuffer buf, OsxHidElement[] children) {
        mDevice = device;
        mChildren = children;
        mType = buf.getInt();
        mUsagePage = buf.getInt();
        mUsage = buf.getInt();
        mCookie = buf.getInt();
        mMin = (buf.getInt() & 0xFFFFFFFFL);
        mMax = (buf.getInt() & 0xFFFFFFFFL);
        mScaledMin = (buf.getInt() & 0xFFFFFFFFL);
        mScaledMax = (buf.getInt() & 0xFFFFFFFFL);
        mSize = (buf.getInt() & 0xFFFFFFFFL);
        mIsRelative = buf.get() != 0;
        mIsWrapping = buf.get() != 0;
        mIsNonLinear = buf.get() != 0;
        mHasPreferredState = buf.get() != 0;
        mHasNullState = buf.get() != 0;
        mUnit = buf.getInt();
        mUnitExponent = buf.getInt();
        mName = OsxUtil.readCString(buf, 256);
    }


    
    public OsxHidDevice getDevice() {
        return mDevice;
    }
    
    public int getType() {
        return mType;
    }
    
    public int getUsagePage() {
        return mUsagePage;
    }
    
    public int getUsage() {
        return mUsage;
    }
    
    public int getCookie() {
        return mCookie;
    }
    
    public long getMin() {
        return mMin;
    }
    
    public long getMax() {
        return mMax;
    }
    
    public long getScaledMin() {
        return mScaledMin;
    }
    
    public long getScaledMax() {
        return mScaledMax;
    }
    
    public long getSize() {
        return mSize;
    }
    
    public boolean isRelative() {
        return mIsRelative;
    }
    
    public boolean isWrapping() {
        return mIsWrapping;
    }
    
    public boolean isNonLinear() {
        return mIsNonLinear;
    }
    
    public boolean hasPreferredState() {
        return mHasPreferredState;
    }
    
    public boolean hasNullState() {
        return mHasNullState;
    }
    
    public int getUnit() {
        return mUnit;
    }
    
    public int getUnitExponent() {
        return mUnitExponent;
    }
    
    public String getName() {
        return mName;
    }
    
    
    
    public int getChildCount() {
        return mChildren.length;
    }
    
    public OsxHidElement getChild(int idx) {
        return mChildren[idx];
    }

    public OsxHidElement[] getChildren() {
        OsxHidElement[] ret = new OsxHidElement[mChildren.length];
        System.arraycopy(mChildren, 0, ret, 0, ret.length);
        return ret;
    }

    
    
    public String toString() {
        StringBuilder s = new StringBuilder("HID Element:");
        s.append(String.format("\n  Type: 0x%02X", mType));
        s.append(String.format("\n  UsagePage: 0x%02X", mUsagePage));
        s.append(String.format("\n  Usage: 0x%02X", mUsage));
        s.append(String.format("\n  Cookie: 0x%08X", mCookie));
        s.append(String.format("\n  Min: %d", mMin));
        s.append(String.format("\n  Max: %d", mMax));
        s.append(String.format("\n  ScaledMin: %d", mScaledMin));
        s.append(String.format("\n  ScaledMax: %d", mScaledMax));
        s.append(String.format("\n  Size: %02X", mSize));
        s.append(String.format("\n  IsRelative: %b", mIsRelative));
        s.append(String.format("\n  IsWrapping: %b", mIsWrapping));
        s.append(String.format("\n  IsNonLinear: %b", mIsNonLinear));
        s.append(String.format("\n  HasPreferredState: %b", mHasPreferredState));
        s.append(String.format("\n  HasNullState: %b", mHasNullState));
        s.append(String.format("\n  Unit: 0x%02X", mUnit));
        s.append(String.format("\n  UnitExponent: 0x%02X", mUnitExponent));
        s.append(String.format("\n  Name: %s", mName));
        s.append(String.format("\n  Children: %d", mChildren.length));
        
        return s.toString();
    }
    

    
    void getFlattenedElements(List<OsxHidElement> list) {
        list.add(this);
        for(int i = 0; i < mChildren.length; i++) {
            mChildren[i].getFlattenedElements(list);
        }
    }
    
    
    
    private static native long getElementValue(long dictPtr) throws HidException;
    private static native int getArrayLength(long arrPtr) throws HidException;
    private static native long getArrayElement(long arrPtr, int pos) throws HidException;
    private static native boolean queryElementInfo(long elPtr, ByteBuffer outBuf) throws HidException;

}
