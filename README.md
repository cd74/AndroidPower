Android power monitoring application. Based on BatteryStatsHelper(com.android.internal.os.BatteryStatsHelper), PowerUsageSummary(/packagea/apps/Settinga/src/com/android/settings/fuelgauge).  

To use internal API, add Android API21 classfile (android21.jar) to build lib, and run "adb shell pm grant com.example.t5 android.permission.BATTERY_STATS".