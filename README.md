# GrowTracker

Welcome to grow tracker. This app was created to help record data about growing plants in order to monitor the growing conditions to help make the plants grow better, and identify potential issues during the grow process.

[Latest APK: (MD5) 9dbd3d137cbf062d3d06b1918942fc10 v2.5.2](https://github.com/7LPdWcaW/GrowTracker-Android/releases/download/v2.5.2/v2.5.2-production.apk)

[Latest APK (Discrete): (MD5) 078a0326b61fd065cb7168535de85767 v2.5.2](https://github.com/7LPdWcaW/GrowTracker-Android/releases/download/v2.5.2/v2.5.2-discrete.apk)

[Get it on F-Droid with automatic updates](https://f-droid.org/packages/me.anon.grow/)

You can follow development, post questions, or grow logs in the [Subreddit](https://reddit.com/r/growutils)

# Installation

The app requires no permissions except for external storage (for caching plant data and images) which you can see [here](https://github.com/7LPdWcaW/GrowTracker-Android/blob/develop/app/src/main/AndroidManifest.xml) in order for users to maintain anonymity, and a minimum Android version of `4.2` and above

# Addons

On documentation on creating addons, please see [ADDONS.md](ADDONS.md)

## How to install

1. Follow [this guide](https://gameolith.uservoice.com/knowledgebase/articles/76902-android-4-0-tablets-allowing-app-installs-from) to enable unknown sources
2. Download the APK from [here](https://github.com/7LPdWcaW/GrowTracker-Android/releases)
3. Click on downloaded app and install

# Updating

You can either elect to update manually, or get notified on releases by installing the [Update plugin](https://github.com/7LPdWcaW/GrowUpdater-Android/releases)

**For updates, do not uninstall first, you will lose your existing plant data**

# Screenshots

[![install](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/install-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/install.png)
[![plant list](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/1-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/1.png)
[![discrete plant list](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/1b-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/1b.png)
[![discrete plant list](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/1c-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/1c.png)
[![discrete plant list](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/1d-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/1d.png)
[![discrete plant list](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/1e-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/1e.png)
[![plant details](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/2-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/2.png)
[![feeding](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/3-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/3.png)
[![nutrients](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/4-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/4.png)
[![nutrients](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/4b-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/4b.png)
[![actions](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/5-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/5.png)
[![pictures](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/6-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/6.png)
[![statistics](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/7-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/7.png)
[![past actions](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/8-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/8.png)
[![action filters](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/9-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/9.png)
[![action options](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/10-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/10.png)
[![settings](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/11-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/11.png)
[![measurements](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/12-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/12.png)
[![schedules](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/13-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/13.png)
[![schedule details](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/14-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/14.png)
[![schedule date](fastlane/metadata/android/en-GB/images/phoneScreenshotsThumbs/15-thumb.png)](fastlane/metadata/android/en-GB/images/phoneScreenshots/15.png)

# About the app

The app uses a simple JSON structure to store all the data about the plants that can be found in `/sdcard/Android/data/me.anon.grow/files/plants.json`. All photos taken in the app are stored in `/sdcard/DCIM/GrowTracker/` in the corresponding plant id folder. `NOTE`: Photos will **not** show in any gallery app and will only be accessible through GrowTracker, or other **file** browser apps such as `ESFile Explorer`

The structure is very simple. Note: date timestamps are all unix timestamps from 1/1/1970 in milliseconds. All objects in arrays are in date order, where index 0 is the oldest and index (size - 1) is the newest.

### Plant object

- Plant date in milliseconds

```
{
    "id": <String>,
    "actions": [<Action Object>],
    "images": [<String>],
    "name": "test",
    "strain": "test",
    "clone": <boolean>,
    "medium": <Medium>,
    "mediumDetails": <String>,
    "plantDate": 1234567890
}
```

### Medium (ENUM)

One of,

`SOIL`, `HYDRO`, `COCO`, `AERO`

### Plant Stage (ENUM)

One of,

`PLANTED`, `GERMINATION`, `SEEDLING`, `CUTTING`, `VEGETATION`, `FLOWER`, `DRYING`, `CURING`, `HARVESTED`

### Action object (feeding)

Temperature measured in ºC


### Action object (water)

- Temperature measured in ºC
- Amount measured in ml
- Date is milliseconds

Water action for waterings

```
{
    "additives": [<Additive>],
    "ph": <Double>,
    "ppm": <Long>,
    "runoff": <Double>,
    "amount": <Double>,
    "date": 1431268453111,
    "type": "Water",
    "temp": <Double>
}
```

### Additive object - used for nutrients

- Amount is measured in ml

```
{
    "description": <String>,
    "amount": <Double>
}
```

### Action object (other)

Action can be one of,

`FIM`, `FLUSH`, `FOLIAR_FEED`, `LST`, `LOLLIPOP`, `PESTICIDE_APPLICATION`, `TOP`, `TRANSPLANTED`, `TRIM`

- Date in milliseconds

```
{
    "action": <Action>,
    "date": 1431258118968,
    "type": "Action"
}
```

### Stage change

- Date in milliseconds

```
{
    "newStage": <Plant Stage>,
    "date": 1431258118968,
    "type": "StageChange"
}
```

### Note

- Date in milliseconds

```
{
    "notes": <String>,
    "date": 1431258118968,
    "type": "Note"
}
```

Image object is a simple string path to an image. Each image is named by the timestamp when it was taken, in milliseconds.

# Encryption

Note that this is **not** a guarantee form of encryption from law enforcement agencies.

Encryption in the app uses basic AES for encryption using the provided passphrase. If the passphrase is less than 128 bits (16 UTF-8 chars), it will be padded with `0x0` bytes. You can view the key generator method [here](https://github.com/7LPdWcaW/GrowTracker-Android/blob/master/app/src/main/java/me/anon/lib/helper/EncryptionHelper.java#L27)

You can decrypt your files using your passphrase either by writing a script that uses AES decryption, or an online tool such as [Online-Domain-Tools](http://aes.online-domain-tools.com/).

# License

Copyright 2014-2019 7LPdWcaW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
