![FastNFitness Logo](https://lh3.googleusercontent.com/KKJw0HA9fD2g9mZMhzzeretD4Tvkr7-wPVzl7WMTTXiiqO6ikS5SqR5X9E8i2HPrNQ=w300)
# Welcome to the FastNFitness wiki!

https://play.google.com/store/apps/details?id=com.easyfitness

## Track your progress (power lifting and fitness) quickly and easily

Now you can track your progress easily and quickly!
You can create as many machines as you want with the name you want!
It's your choice!

Graphics will show your progress and your body evolution. 

Don't leave the app to get your music; a music player is integrated into the app!

Easy!


## Development

For development, [Android Studio](https://developer.android.com/studio/) with [gradle](https://gradle.org/) is used. If you want to develop and contribute, the easiest way is to use Android Studio as well.

1. Fork the project on GitHub.
2. Clone it within Android Studio or a Git Bash:
    1. From the Welcome Screen: Check out project from Version Control -> Git
    1. Or from within Android Studio: File -> New -> Project from version control -> Git
    2. Log in to GitHub... -> Create API Token -> Enter your username and password from your GitHub account
    3. Add the URL of your forked project in „Git Repository URL“
    4. Clone
3. Click **No** on the next dialog: `Would you like to create an Android Studio project for the sources you have checked out to [...]`
4. Then open an existing Android Studio project:
    1. From the Welcome Screen: Open an existing Android Studio project
    1. Or from within Android Studio: File -> Open
    2. Search for the path, where you cloned this project
5. Do you want to add the following file to Git? (Project.xml) -> **No**
6. Then in the Build window: Add Google Maven repository
7. File -> Sync Project with Gradle Files


## Change Log

### 0.17.2 : April 5, 2019

- Enhancement : Permissions have been made optional
- Enhancement : Cardio fragment removed

### 0.17.1 : March 27, 2019

- Bug : Fixed frozen edit in creation profile

### 0.17 : March 17, 2019

- Enhancement : Optimized start-up and update UI
- Enhancement : Added fat, muscles and water and BMI to weight tracking
- Enhancement : Added date to record list
- Enhancement : Added gender to profile for BMI calculation
- Bug : Fixed vibration at the end of countdown when screen off
- Bug : Other small bug fixes

### 0.16.2 : January 16, 2019
- Enhancement : Possibility to copy existing records
- Enhancement : Main list displaying last date history 
- Enhancement : Refactoring for better performances
- Enhancement : Added filtering in exercise page (Thanks to @geniusupgrader)

### 0.16.1 : November 14, 2018
- Bug : Fixed crash on Cardio page
- Bug : Fixed title on Workout page

### 0.16 : November 8, 2018
- Enhancement : Merge of Cardio and Bodybuilding UI
- Enhancement : UI improvement
- Enhancement : Records visible on Exercise page
- Bug : Removed Exercise list order case sensitive
- Bug : Fixed crash on graph when less that 5 reps

### 0.15.2 : September 19, 2018
- Bug : Fixed intro

### 0.15.1 : September 13, 2018
- Bug : Fixed float issue for profile size
- Bug : Fixed wrong image rotation
- Bug : Fixed CVS export for body measures

### 0.15 - August 8, 2018
- Enhancement : Profile photo in Menu
- Enhancement : New profile fragment
- Enhancement : Possibility to remove a photo
- Enhancement : Small UI updates

### 0.14.6 - June 15, 2018

- Enhancement : Added German Version (Thanks to @EGUltraTM)

### 0.14.5 - June 11, 2018

- Enhancement : Possibility to set a picture for the profile 
- Enhancement : Crop tool for pictures 
- Enhancement : New Dialog box and Toast, Init optimization

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
