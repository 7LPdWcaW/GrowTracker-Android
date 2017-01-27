# Addons

Grow tracker has a basic implementation for addons via the use of Broadcast Intents.

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

You can register an activity with the receiver for configuration purposes by using the `me.anon.grow.ADDON_CONFIGURATION` intent-filter action.

`Note:` The `category` for the intent-filter must be the action of the intent-filter of the addon (e.g. `me.anon.grow.ACTION_SAVE_PLANTS`)

```xml
<activity android:name=".ConfigureActivity">
    <intent-filter>
        <action android:name="me.anon.grow.ADDON_CONFIGURATION" />
        <category android:name="me.anon.grow.ACTION_SAVE_PLANTS" />
    </intent-filter>
</activity>
```
