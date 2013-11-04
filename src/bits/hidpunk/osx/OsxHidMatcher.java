package bits.hidpunk.osx;

import bits.hidpunk.HidMatcher;

/** 
 * @author Philip DeCamp  
 */
public class OsxHidMatcher implements HidMatcher {

    private static final int KEY_USAGE_PAGE      = 1;
    private static final int KEY_USAGE           = 2;
    private static final int KEY_TRANSPORT       = 3;
    private static final int KEY_VENDOR_ID       = 4;
    private static final int KEY_PRODUCT_ID      = 5;
    private static final int KEY_VERSION_NUM     = 6;
    private static final int KEY_VENDOR_NAME     = 7;
    private static final int KEY_PRODUCT_NAME    = 8;
    private static final int KEY_SERIAL_NUM      = 9;
    private static final int KEY_LOCATION_ID     = 10;
    
    private CFRef mRef;
        
    
    OsxHidMatcher() throws IOKitException {
        long ptr = create();
        
        if(ptr == 0)
            throw new IOKitException("Failed to create OsxHidMatcher.");
        
        mRef = new CFRef(ptr, true);
    }
    
    
    
    long getPointer() {
        return mRef.getPointer();
    }
    
    
    
    public void setUsagePage(long usagePage) {
        trySetLongValue(KEY_USAGE_PAGE, usagePage);
    }
    
    public void setUsage(long usage) {
        trySetLongValue(KEY_USAGE, usage);
    }
    
    public void setTransport(String val) {
        trySetStringValue(KEY_TRANSPORT, val);
    }
    
    public void setVendorID(long val) {
        trySetLongValue(KEY_VENDOR_ID, val);
    }
    
    public void setProductID(long val) {
        trySetLongValue(KEY_PRODUCT_ID, val);
    }
    
    public void setVersionNumber(long val) {
        trySetLongValue(KEY_VERSION_NUM, val);
    }
    
    public void setVendorName(String val) {
        trySetStringValue(KEY_VENDOR_NAME, val);
    }
    
    public void setProductName(String val) {
        trySetStringValue(KEY_PRODUCT_NAME, val);
    }
    
    public void setSerialNumber(String val) {
        trySetStringValue(KEY_SERIAL_NUM, val);
    }
    
    public void setLocationID(long val) {
        trySetLongValue(KEY_LOCATION_ID, val);
    }

    

    private void trySetLongValue(int key, long value) {
        long ptr = mRef.getPointer();
        
        try{
            setLongValue(ptr, key, value);
        }catch(IOKitException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    private void trySetStringValue(int key, String value) {
        if(value == null)
            return;
        
        long ptr = mRef.getPointer();
        
        try{
            setStringValue(ptr, key, value);
        }catch(IOKitException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    

    
    private static native long create();

    private static native void setLongValue(long ptr, int key, long value) throws IOKitException;
    
    private static native void setStringValue(long ptr, int key, String value) throws IOKitException;

}
