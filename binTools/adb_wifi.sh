#! /bin/bash

echo "Starting ADB"
adb tcpip 5555
adb connect "`adb shell ifconfig wlan0 | grep 'inet addr' | cut -d: -f2 | awk '{print $1}'`:5555"
