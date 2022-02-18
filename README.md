# README #

A player+ app with controls that rely on hardware/software sensors and user location.
Included in this repo is an APK you can download directly here :

### Features ###

This app currently only currently works on portrait mode. Using it with other orientations may
lead to unwanted behavior.

* Tilt forward to _lower_ media volume
* Tilt backward to _increase_ media volume
* Tilt to the right to _seek backwards_
* Tilt to the left to _seek forwards_
* Shake device to pause playback
* Turn on location (and accept permissions); and move around to reset media.

### Tech ###

Android 4.1
Gradle 4.1.0 + Kotlin ext 1.4.21

Google's Exoplayer to play media

Android [GAME ROTATION VECTOR sensor](https://source.android.com/devices/sensors/sensor-types#game_rotation_vector)
and a modified verion of [this project](https://github.com/kplatfoot/android-rotation-sensor-sample)
to measure device pitch and azimuth

[Seismic](https://github.com/square/seismic) to handle a *shake* gesture

Google Play Services' [FusedLocationClient](https://developer.android.com/training/location/request-updates#updates) to subscribe to location updates



**Thank you and have a nice day :)**

