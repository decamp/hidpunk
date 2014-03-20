package bits.hidpunk.deltamouse;

import java.util.List;

import bits.hidpunk.*;
import bits.hidpunk.driver.*;
import bits.util.event.EventCaster;

/**
 * @author Philip DeCamp
 */
public class DeltaMouseTest implements DeltaMouseListener {

    
    public static void main( String[] args ) throws Exception {
//        new DeltaMouseTest();
        
        HidMatcher matcher = DeltaMouse.createMatcher();
        List<DebugDriver> devs = DebugDriver.findDevices( matcher, 100000L, true, true );
        if( devs.isEmpty() ) {
            System.out.println( "No mice found. ");
            return;
        }
        
        devs.get( 0 ).start( HidpunkDriver.THREADING_SYNCHRONOUS );
        
        while( true ) {
            Thread.sleep( 1000L );
        }
    }
    
    
    
    
    
    private final DeltaMouseGroup mGroup;
    
    
    private DeltaMouseTest() throws Exception {
        mGroup = DeltaMouseGroup.create();
        mGroup.addListener( this );
        mGroup.start( EventCaster.THREADING_SYNCHRONOUS );
    }


    
    @Override
    public void mouseMoved( long timeMicros, int dx, int dy ) {
        System.out.println( "mouseMoved: " + dx + "\t" + dy );
    }


    @Override
    public void mouseButtonPressed( long timeMicros, int button ) {
        System.out.println( "mousePressed: " + button );
    }

    
    @Override
    public void mouseButtonReleased( long timeMicros, int button ) {
        System.out.println( "mouseReleased: " + button );
    }
        

}