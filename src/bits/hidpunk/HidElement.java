/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public interface HidElement {

    public HidDevice device();
    
    public int type();
    public int usagePage();
    public int usage();
    public int cookie();
    
    //These elements return longs because HID devices normally returns UInt32 values.
    public long min();
    public long max();
    public long scaledMin();
    public long scaledMax();
    public long size();
    
    public boolean isRelative();
    public boolean isWrapping();
    public boolean isNonLinear();
    public boolean hasPreferredState();
    public boolean hasNullState();
    
    public int unit();
    public int unitExponent();
    
    public String name();
    public int childCount();
    public HidElement child( int idx );
    public HidElement[] children();
}
