/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk.osx;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


/**
 * @author Philip DeCamp
 */
class OsxUtil {
    public static String readCString( ByteBuffer buf, int len ) {
        return readCString( buf, new byte[len], len );
    }

    public static String readCString( ByteBuffer buf, byte[] arr, int len ) {
        buf.get( arr, 0, len );
        int i;

        for( i = 0; i < len; i++ ) {
            if( arr[i] == 0 ) {
                break;
            }
        }

        try {
            return new String( arr, 0, len, "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            return "";
        }
    }
}
