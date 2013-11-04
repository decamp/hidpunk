package bits.hidpunk.commandpost;

import javax.swing.JFrame;

import bits.hidpunk.*;
import bits.hidpunk.driver.DebugDriver;


/** 
 * @author Philip DeCamp  
 */
public class CommandPostTest {
    
    public static void main(String[] args) {
        try{
            test2();
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public static void test1() throws Exception {
        HidManager man = HidManager.getManager();
        HidMatcher matcher = man.newDeviceMatcher();
        //0xCF01   0x0947

        JFrame frame = new JFrame();
        frame.setVisible(true);
        
        for(DebugDriver driver: DebugDriver.findDevices(0x0947L, 0xCF01L, 100000, true, true)) {
            System.out.println("STARTING");
            driver.start();
        }
                
    }

    
    public static void test2() throws Exception {
        HidManager man = HidManager.getManager();
        HidMatcher matcher = man.newDeviceMatcher();
        matcher.setVendorID(0x0947L);
        matcher.setProductID(0xCF01L);
        
        for(HidDevice d: man.findDevices(matcher)) {
            for(HidElement el: d.flattenElements()) {
                System.out.println(el);
            }
            
            System.out.println("====");
            
            for(HidElement el: d.flattenElements()) {
                if(el.getCookie() == 0x0013 || el.getCookie() == 0x0014)
                    System.out.println(el);
            }
            
            System.out.println("==");
            System.out.println(d.getDescription());
        }
        
        
                
    }
    
}
