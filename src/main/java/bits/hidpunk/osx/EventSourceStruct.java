/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import bits.hidpunk.HidException;


/**
 * @author Philip DeCamp
 */
public class EventSourceStruct extends CStruct {

    public EventSourceStruct( long ptr ) {
        super( ptr );
    }

    @Override
    protected native void destruct( long ptr ) throws HidException;

}
