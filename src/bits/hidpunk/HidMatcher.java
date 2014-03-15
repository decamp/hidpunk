/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * Object that will match connected devices based on 
 * some set of properties you provide. For example,
 * after create a fresh matcher m, you may set <br/>
 * <pre></code>m.usagePage( HidConstants.PAGE_GENERIC_DESKTOP_CONTROL );</code></pre>
 * to find all generic desktop devices, or <br/>
 * <pre><code> m.usagePage( HidConstants.PAGE_GENERIC_DESKTOP_CONTROL );
 * m.usage( 0x02 );
 * </code></pre>to find all mice. 
 * 
 * @author Philip DeCamp
 */
public interface HidMatcher {
    public void usagePage( long usagePage );
    public void usage( long usage );
    public void transport( String val );
    public void vendorID( long val );
    public void productID( long val );
    public void versionNumber( long val );
    public void vendorName( String val );
    public void productName( String val );
    public void serialNumber( String val );
    public void locationID( long val );
}
