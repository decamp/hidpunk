#!/bin/bash

cd `dirname "$0"`
cd ..

names[0]=bits.hidpunk.osx.CFRef
names[1]=bits.hidpunk.osx.CFRunLoop
names[2]=bits.hidpunk.osx.CFNotificationSource
names[3]=bits.hidpunk.osx.IOIter
names[4]=bits.hidpunk.osx.IOObject
names[5]=bits.hidpunk.osx.NotificationHandler
names[6]=bits.hidpunk.osx.EventSourceStruct
names[7]=bits.hidpunk.osx.OsxQueue
names[8]=bits.hidpunk.osx.OsxUtil
names[9]=bits.hidpunk.osx.OsxHidDevice
names[10]=bits.hidpunk.osx.OsxHidElement
names[11]=bits.hidpunk.osx.OsxHidEventSource
names[12]=bits.hidpunk.osx.OsxHidInterface
names[13]=bits.hidpunk.osx.OsxHidManager
names[14]=bits.hidpunk.osx.OsxHidMatcher


for i in {0..14}
do
# Generate header
  javah -classpath scratch/main/java ${names[i]}
# Remove package cruft at beginning of name and move into place.
  f0=$(echo ${names[i]}|sed 's/\./_/g').h
  f1=src/main/c/osx/$(echo ${names[i]}|sed 's/.*\.//g').h
# Replace invalid <jni.h> with <JavaVM/jni.h> and move into place.
  cat ${f0} | sed s_^\#include\ \<jni\.h\>_\#include\ \<JavaVM\\/jni.h\>_ > ${f1}
  rm ${f0}
done

delNames[0]=bits.hidpunk.osx.CFRunLoop.Action
delNames[1]=bits.hidpunk.osx.CFRunLoop.ActionEvent
delNames[2]=bits.hidpunk.osx.CFRunLoop.LoopThread
delNames[3]=bits.hidpunk.osx.OsxHidInterface.ShutdownHook
for i in {0..3}
do
  f0=$(echo ${delNames[i]}|sed 's/\./_/g').h
  rm ${f0}
done

