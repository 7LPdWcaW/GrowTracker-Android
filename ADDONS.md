# Addons

Grow tracker has a basic implementation for addons via the use of Broadcast Intents.

## Meta data

You should provide the following meta-data tags for your addon application

|key|type|description|
|---|---|---|
|`me.anon.grow.ADDON_NAME`|String|Name of your application addon|
|`me.anon.grow.ADDON_VERSION`|String|Version of your application addon|

## Available broadcasts

Currently the available broadcasts you can listen for include

### `me.anon.grow.ACTION_SAVE_PLANTS`

This broadcast action is called when the plant list json is saved, an image has been saved, or when an image has been deleted.

Data is provided with the broadcast intent via the bundle

|key|type|description|
|---|---|---|
|`me.anon.grow.PLANT_LIST`|String|Full json-encoded array of plants and its data.|
|`me.anon.grow.ENCRYPTED`|Boolean|If this is true, `me.anon.grow.PLANT_LIST` will be encrypted and base64 encoded, images will be encrypted on disk|
|`me.anon.grow.IMAGE_ADDED`|String|Path to image added|
|`me.anon.grow.IMAGE_DELETED`|String|Path to deleted image|

Example receiver:

```xml
<receiver android:name=".CloudSyncBroadcastReceiver" android:exported="true" android:enabled="true">
    <intent-filter>
        <action android:name="me.anon.grow.ACTION_SAVE_PLANTS" />
    </intent-filter>
</receiver>
```

`Note:` you must include the `android:exported="true"` and `android:enabled="true"` parameters.

## Configuring your addon

You can register an activity with the receiver for configuration purposes by using the `me.anon.grow.ADDON_CONFIGURATION` intent-filter category.

`Note:` The `action` for the intent-filter must be the action of the intent-filter of the addon (e.g. `me.anon.grow.ACTION_SAVE_PLANTS`)

```xml
<activity android:name=".ConfigureActivity">
    <intent-filter>
        <action android:name="me.anon.grow.ACTION_SAVE_PLANTS" />
        <category android:name="me.anon.grow.ADDON_CONFIGURATION" />
    </intent-filter>
</activity>
```

## Available intent providers

You can request all the data from the app using the request intent as follows

```
    <action android:name="me.anon.grow.ACTION_REQUEST_PLANTS" />
    <category android:name="android.intent.category.DEFAULT" />
```

The activity will return a bundle with the apps data as a JSON string

|key|type|description|
|---|---|---|
|`me.anon.grow.PLANT_LIST`|String|Full json-encoded array of plants and its data.|
|`me.anon.grow.ENCRYPTED`|Boolean|If this is true, `me.anon.grow.PLANT_LIST` will be encrypted and base64 encoded, images will be encrypted on disk|
