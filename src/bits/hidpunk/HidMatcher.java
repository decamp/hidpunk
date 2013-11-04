package bits.hidpunk;

/** 
 * @author Philip DeCamp  
 */
public interface HidMatcher {

    public void setUsagePage(long usagePage);
    public void setUsage(long usage);
    public void setTransport(String val);
    public void setVendorID(long val);
    public void setProductID(long val);
    public void setVersionNumber(long val);
    public void setVendorName(String val);
    public void setProductName(String val);
    public void setSerialNumber(String val);
    public void setLocationID(long val);

}
