THE HIDPUNK LIBRARY:
HidPunk is used for accessing Human Interface Devices (HID). The API provided
allows developers to create custom interfaces / user-space drivers for a wide
range of HID devices, including mice, keyboards and joysticks, without
having to write any compiled code.

Currently, the only implementation of the HidPunk API is written for 
OSX 10.6 or above.


BUILDING:
1. If the JNI code has been altered, you should first recompile the JNI library.  
Go to the "jni_src" directory and run "make".  Currently, only building from the
OSX platform is supported.  The make file should update the libhidpunk.jnilib 
file in the lib directory. 

2. Go to the project directory and run "ant".  


RUNTIME:
1. The HIDPUNK JNI libraries should be located somewhere in java.library.path. 
You may set this with the runtime parameter 
"java -Djava.library.path=<location_of_libs> <whatever else>".


DEPENDENCIES:
cogmac_langx.jar: Used for platform specific queries.  Found in the cogmac
commons repository.

cogmac_event.jar: Used for the event casting system.  Found in the cogmac 
commons repository.


MAINTAINED BY:
Philip DeCamp