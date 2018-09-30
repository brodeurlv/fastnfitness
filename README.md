![FastnFitness Logo](https://lh3.googleusercontent.com/KKJw0HA9fD2g9mZMhzzeretD4Tvkr7-wPVzl7WMTTXiiqO6ikS5SqR5X9E8i2HPrNQ=w300)
# Welcome to FastnFitness wiki !

https://play.google.com/store/apps/details?id=com.easyfitness

## Track your progress (Power lifting and fitness) easily and quickly

Take the control of your app by becoming one of its developers:
https://github.com/brodeurlv/fastnfitness

Now you can track you progress easily and quickly!
You can create as many machine as you want with the name you want ! It's your choice !

Many graphics will show your progress and your body evolution. 

Don't leave the app to get your music. A music player is integrated with the app !

Easy !



## Development

For Development [Android Studio](https://developer.android.com/studio/) with [gradle](https://gradle.org/) is used. If you want to develop and contribute, the easiest way is to use Android Studio as well.

1. First fork this project on Github.
2. Then clone it within Android Studio:
    1. From the Welcome Screen: Check out project from Version Contrlo -> Git
    1. Or from within Android Studio: File -> New -> Project from version control -> Git
    2. Log in to Github... -> Create API Token -> Enter your username and password from your Github account
    3. Git Repository URL: https://github.com/geniusupgrader/Timeupgrader.git
    4. Clone
3. Click **No** on the next Dialog: „Would you like to create an Android Studio project for the sources you have checked out to [...]“
4. Then open an existing Android Studio project:
    1. From the Welcome Screen: Open an existing Android Studio project
    1. Or from within Android Studio: File -> Open
    2. Search for the path, where you cloned this project
5. Do you want to add the following file to Git? (Project.xml) -> **No**
6. Then in the Build window: Add Google Maven repository and sync project
7. In the Android File Browser, go to: fastnfitness -> app -> build.gradle and comment out the „signingConfigs“ section and signingConfig instructions inside buildTypes:

```
signingConfigs {
    release {
        storeFile file("C:/Dev/fastnfitness_keystore/keystore")
        storePassword "5d1f5s2f"
        keyAlias "brodeur"
        keyPassword "5d1f5s2f"
    }
    debug {
        storeFile file("C:/Dev/fastnfitness_keystore/keystore")
        storePassword "5d1f5s2f"
        keyAlias "brodeur"
        keyPassword "5d1f5s2f"
    }
}

signingConfig signingConfigs.release

signingConfig signingConfigs.debug
```

8. File -> Sync Project with Gradle Files




## Change Log

### 0.15.2 : September 19, 2018
- Bug : Fixed intro

### 0.15.1 : September 13, 2018
- Bug : Fixed float issue for profile size
- Bug : Fixed wrong image rotation
- Bug : Fixed CVS export for bodymeasures

### 0.15 - August 8, 2018
- Enhancement : Profile photo in Menu
- Enhancement : New profile fragment
- Enhancement : Possibility to remove a photo
- Enhancement : Small UI updates

### 0.14.6 - June 15, 2018

- Enhancement : Added German Version (Thanks to EGUltraTM)

### 0.14.5 - June 11, 2018

- Enhancement : Possibility to set a picture for the profile 
- Enhancement : Crop tool for pictures 
- Enhancement : New Dialogbox and Toast, Init optimization

### 0.14.4

- Bug : Fixed issue on Time dialog display on Android 8+ 
- Bug : Fixed issue on Camera dialog display on Android 8+ 
- Enhancement : Better exercise refresh 
- Enhancement : Automatically close intro after profile creation 
- Enhancement : MP3 player hidden by default

### 0.14.3

- Bug : Fixed sum of weight during rest time 
- Bug : Fixed some crash on app resume 
- Enhancement : Added min and max value for exercises 
- Enhancement : Performance enhancement 
- Enhancement : Rest countdown vibrates 2 sec before the end 
- Enhancement : Added cross in table to delete Weight and Body measures 
- Enhancement : Exercise list with icons and details 
- Enhancement : Current exercise with icons


