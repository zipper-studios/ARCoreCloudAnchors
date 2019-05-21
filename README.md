# ARCoreCloudAnchors

<img src="https://i1.wp.com/androidcommunity.com/wp-content/uploads/2018/05/cloud-anchors.png?resize=696%2C383&ssl=1" height="250" width="500">

This app is based on Augmented Reality technology and uses ARCore Cloud Anchors to create multiplayer or collaborative AR experiences that Android and IOS users can share. A step-by-step guide to create this app is available on our article on [Medium](https://medium.com/p/16929723f693/edit "AR technology for Android - Part 4 : AR Cloud Anchors"), where you will learn how to host and resolve a Cloud Anchor as Android developer. 

## Running 
To use Cloud Anchors, you'll need to add an API Key to your app for authentication with the ARCore Cloud Anchor Service. Follow [Steps 1 and 2](https://developers.google.com/ar/develop/java/cloud-anchors/quickstart-android#add_an_api_key) from these instructions to get an API Key.

Include the API key in your *AndroidManifest.xml* file as follows:
```
<meta-data
        android:name="com.google.android.ar.API_KEY"
        android:value="<YOUR API KEY HERE>" />
```
                
                
For sharing the Cloud Anchor IDs between different devices, we use the [Firebase Realtime Database](https://firebase.google.com/docs/database). You need to set up a Firebase Realtime Database with your Google account to use with this app. This is easy with the Firebase Assistant in Android Studio. See more details about how to connect your app with Firebase on [Medium- Step 4](https://medium.com/p/16929723f693/edit "AR technology for Android - Part 4 : AR Cloud Anchors").

## Requirements
- A supported ARCore device, connected via a USB cable to your development machine (and also connected to the Internet via WiFi).
- ARCore 1.8 or later.
- A development machine with Android Studio (v3.0 or later).

## License
This project is licensed under the Apache License 2.0 - see the [LICENSE](https://github.com/zipper-studios/ARCoreCloudAnchors/blob/master/LICENSE) file for details

