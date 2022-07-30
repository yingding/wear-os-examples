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




