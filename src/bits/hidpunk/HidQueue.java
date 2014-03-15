/* 
 * Copyright (c) 2012, Massachusetts Institute of Technology
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.hidpunk;

/**
 * @author Philip DeCamp
 */
public interface HidQueue {
    public void start() throws HidException;
    public void stop() throws HidException;
    public void addElement( HidElement el ) throws HidException;
    public void removeElement( HidElement el ) throws HidException;
}
