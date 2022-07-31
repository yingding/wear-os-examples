# About this project

this project is a folk from the [`WatchFaceKotlin`](https://github.com/android/wear-os-samples/tree/main/WatchFaceKotlin) from the android [`wear-os-samples` repository](https://github.com/android/wear-os-samples/)

This project codes are updated and modified by the author of this project.

## Changelog
* Updated `Renderer.CanvasRenderer2` and deprecated `Renderer.CanvasRenderer`
* Updated `Compose-Wear` lib to Version `1.0.0-rc02`
* Added comments to enforce better understanding of source codes

## Steps to build in Android Studio

* Because a watch face only contains a service, that is, there is no Activity, you first need to turn
off the launch setting that opens an Activity on your device.

* To do that (and once the project is open) go to Run -> Edit Configurations. Select the **app**
module and the **General** tab. In the Launch Options, change **Launch:** to **Nothing**. This will
allow you to successfully install the watch face on the Wear device.

* When installed, you will need to select the watch face in the watch face picker, i.e., the watch
face will not launch on its own like a regular app.


## Drawable no-dpi resource
* https://stackoverflow.com/questions/34156957/what-is-the-difference-between-anydpi-and-nodpi/34370735#34370735

# Reference:
* Create WatchFace Service for Android Studio: https://developer.android.com/training/wearables/watch-faces/service#create-project
* Change ComplicationDrawable Style: https://developer.android.com/reference/kotlin/androidx/wear/watchface/complications/rendering/ComplicationDrawable

## Kotlin Function, add Names to Call Arguments in Kotlin
left click to highlight the function -> right mouse click -> Show context action -> Add names to call arguments

or

use ⌥ + Enter (macOS), or Alt+Enter (Windows/Linux) to open the "Show context action" -> Add names to call arguments

Reference:
* https://www.jetbrains.com/idea/guide/tips/adding-call-args/

## Kotlin rem function
get the modulo, do not use the mod function which is deprecated

## Minimum Libs to create a watchface in AS Studio 4.x Kotlin
The following libs are the minimum lib you might need to make a kotlin watchface in wear os 3 without WatchFace configuration
```kotlin
    def ktx_core_version = '1.8.0'
    def material_version = "1.6.1"
    def wear_watchface_version = "1.1.0"

    // Ktx core
    implementation "androidx.core:core-ktx:$ktx_core_version"
    // Material components: Theme.MaterialComponents.DayNight
    implementation "com.google.android.material:material:$material_version"
    // WatchFace Service, Renderer.CanvasRenderer2
    implementation "androidx.wear.watchface:watchface:$wear_watchface_version"
    // Complication Style and ComplicationDrawable
    implementation "androidx.wear.watchface:watchface-complications-rendering:$wear_watchface_version"
```




