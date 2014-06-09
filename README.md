### HidPunk Library
HidPunk provides direct access to Human Interface Devices (HID).
This enables Java developers to create custom interfaces and user-space drivers
for a wide range of HIDs - including mice, keyboards and joysticks -
without needing to write compiled code.

Currently, the only implementation of the HidPunk API is written for 
OS X 10.6 or above.

The "src/drivers" contains several example pure-java drivers, including:
- a "deltamouse" driver that provides raw mouse data that can be used for an FPS-type interface
- 3DConnexion SpaceNavigator device driver
- ShuttlePro Jog-Shuttle device driver
- ProPedals pedal device driver

Drivers support detection of device plugins and removals, and asynchronous event passing.


### Build:
1. If the JNI code has been altered, you should first recompile the JNI library.  
Go to the "jni_src" directory and run "make".  Currently, only building from the
OSX platform is supported.  The make file should update the libhidpunk.jnilib 
file in the lib directory. 

2. Go to the project directory and run "ant".


### Runtime:
1. After build, add all jars/libraries in **lib** and **target** directories to your project.

2. The JNILIB libraries must be located somewhere in java.library.path. 
You may set this with the runtime parameter 
"java -Djava.library.path=(location_of_libs) ...".


### Dependencies:
bits_util.jar: Used for event bus and platform specific queries.

---
Author: Philip DeCamp

