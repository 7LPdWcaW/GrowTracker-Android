# Read me

Welcome to grow tracker. This app was created to help record data about growing plants in order to monitor the growing conditions to help make the plants grow better, and identify potential issues during the grow process.

[Latest APK: (MD5) fb7153166529a7aa2b072a3c48186d62 v1.2](https://github.com/7LPdWcaW/GrowTracker-Android/raw/master/app/app-release.apk)

# Installation

The app requires no permissions except for external storage (for caching plant data and images) which you can see [here](https://github.com/7LPdWcaW/GrowTracker-Android/blob/develop/app/src/main/AndroidManifest.xml) in order for users to maintain anonymity.

## How to install

1. Follow [this guide](https://gameolith.uservoice.com/knowledgebase/articles/76902-android-4-0-tablets-allowing-app-installs-from) to enable unknown sources
2. Download the APK from [here](https://github.com/7LPdWcaW/GrowTracker-Android/raw/master/app/app-release.apk)
3. Click on downloaded app and install

**For updates, do not uninstall first, you will lose your existing plant data**

[![install](screenshots/install-thumb.png)](screenshots/install.png)
[![plant list](screenshots/1-thumb.png)](screenshots/1.png)
[![discrete plant list](screenshots/1b-thumb.png)](screenshots/1b.png)
[![plant details](screenshots/2-thumb.png)](screenshots/2.png)
[![feeding](screenshots/3-thumb.png)](screenshots/3.png)
[![nutrients](screenshots/4-thumb.png)](screenshots/4.png)
[![actions](screenshots/5-thumb.png)](screenshots/5.png)
[![pictures](screenshots/6-thumb.png)](screenshots/6.png)
[![statistics](screenshots/7-thumb.png)](screenshots/7.png)
[![past actions](screenshots/8-thumb.png)](screenshots/8.png)
[![action filters](screenshots/9-thumb.png)](screenshots/9.png)
[![action options](screenshots/10-thumb.png)](screenshots/10.png)
[![settings](screenshots/11-thumb.png)](screenshots/11.png)

# About the app

The app uses a simple JSON structure to store all the data about the plants that can be found in `/sdcard/Android/data/me.anon.grow/files/plants.json`. All photos taken in the app are stored in `/sdcard/DCIM/GrowTracker/` in the corresponding plant id folder. `NOTE`: Photos will **not** show in any gallery app and will only be accessible through GrowTracker, or other **file** browser apps such as `ESFile Explorer`

The structure is very simple. Note: date timestamps are all unix timestamps from 1/1/1970 in milliseconds. All objects in arrays are in date order, where index 0 is the oldest and index (size - 1) is the newest.

### Plant object

```
{
    "id": <String>,
    "actions": [<Action Object>],
    "images": [<String>],
    "name": "test",
    "strain": "test",
    "clone": <boolean>,
    "stage": <Stage>,
    "medium": <Medium>,
    "plantDate": 1234567890
}
```

### Medium (ENUM)

One of,

`SOIL`, `HYDRO`

### Plant Stage (ENUM)

One of,

`PLANTED`, `GERMINATION`, `VEGETATION`, `FLOWER`, `CURING`, `HARVESTED`

### Action object (feeding)

Temperature measured in ºC

Nutrient object consists of standard percentage of elements in the solution. Ca %, K %, Mg %, N %, P %, S %. usually in the format "1.5:1.0:2.6" for Ca/K/Mg

```
{
    "nutrient": {
        "capc": <Double>,
        "kpc": <Double>,
        "mgpc": <Double>,
        "npc": <Double>,
        "ppc": <Double>,
        "spc": <Double>
    },
    "mlpl": <Double>,
    "ph": <Double>,
    "ppm": <Long>,
    "runoff": <Double>,
    "amount": <Integer>,
    "date": <Long>,
    "type": "Feed",
    "temp": <Integer>
}
```

### Action object (water)

Temperature measured in ºC

Water action is the same as a feeding action, sans the "nutrient" object

```
{
    "ph": <Double>,
    "ppm": <Long>,
    "runoff": <Double>,
    "amount": <Integer>,
    "date": 1431268453111,
    "type": "Water",
    "temp": <Integer>
}
```

### Action object (other)

Action can be one of,

`TRIM`, `TOP`, `FIM`, `LST`, `LOLLIPOP`, `FLUSH`, `FOLIAR_FEED`, `PESTICIDE_APPLICATION`

```
{
    "action": <Action>,
    "date": 1431258118968,
    "type": "Action"
}
```

### Stage change

```
{
    "newStage": <Plant Stage>,
    "date": 1431258118968,
    "type": "StageChange"
}
```

### Note

```
{
    "notes": <String>,
    "date": 1431258118968,
    "type": "Note"
}
```

Image object is a simple string path to an image. Each image is named by the timestamp when it was taken, in milliseconds.

#Encryption

Note that this is **not** a guarantee form of encryption from law enforcement agencies.

Encryption in the app uses basic AES for encryption using the provided passphrase. If the passphrase is less than 128 bits (16 UTF-8 chars), it will be padded with `0x0` bytes. You can view the key generator method [here](https://github.com/7LPdWcaW/GrowTracker-Android/blob/master/app/src/main/java/me/anon/lib/helper/EncryptionHelper.java#L27)

You can decrypt your files using your passphrase either by writing a script that uses AES decryption, or an online tool such as [Online-Domain-Tools](http://aes.online-domain-tools.com/).

#License

Copyright 2011-2015 7LPdWcaW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
