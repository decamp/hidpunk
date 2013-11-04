package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public interface HidElement {

    public HidDevice getDevice();
    
    public int getType();
    public int getUsagePage();
    public int getUsage();
    public int getCookie();
    
    //These elements return longs since HID devices normally returns UInt32 values.
    public long getMin();
    public long getMax();
    public long getScaledMin();
    public long getScaledMax();
    public long getSize();
    
    public boolean isRelative();
    public boolean isWrapping();
    public boolean isNonLinear();
    public boolean hasPreferredState();
    public boolean hasNullState();
    public int getUnit();
    public int getUnitExponent();
    public String getName();
    public int getChildCount();
    public HidElement getChild(int idx);
    public HidElement[] getChildren();
    
}
