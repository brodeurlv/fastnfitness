## Change Log

### 0.20.6.2: April 11, 2024
- Enhancement: Updated libs to increase stability

### 0.20.6.1: April 09, 2024
- Bug: Crash when activating countdown (#275)

### 0.20.6: April 01, 2024
- Enhancement: New tracking of body photos (#268)
- Enhancement: Added OpenScale weight import (#272)
- Bug: Fixed crop image (#273)
- Bug: Fixed min/max for free records (#271)

### 0.20.5.1: February 22, 2022
- Bug: Fixed start issue (#252)
- Bug: Fixed Dutch translation

### 0.20.5: February 13, 2022
- Enhancement: Changes csv column names. Please refer to github WIKI documentation for details (#49, #209)
- Enhancement: Added program and program templates to csv export (#228)
- Enhancement: Made csv import more flexible
- Enhancement: Added feature to calculate BMR and daily calories based on activity level (#248, Thank you @a124557)
- Bug: Moved program template value to record value to avoid discrepancies in case of program update
- Bug: Fixed small UI issues (#244)

### 0.20.4: December 07, 2021
- Enhancement: Weight all in one insertion (#230, Thank you @chaptergy)
- Enhancement: Added turkish translation (Thank you @Gokhungoktas)
- Bug: Fixed crash on Body Measure import (#240)
- Bug: Fixed pending record time (#232, Thank you @hhpmmd)
- Bug: Fixed import files being unselectable (#224)

### 0.20.3: September 09, 2021
- Bug: Fixed bug introduced on 0.20.2 on muscles list (#220, Thank you @MatthewRFennell).
  Muscles created on 0.20.2 exclusively might need to be manually corrected by user.


### 0.20.2: August 20, 2021
- Enhancement: Added countdown in program view
- Enhancement: Added program update request(#141)
- Enhancement: Added Glutes to muscles (#212, Thank you @MatthewRFennell)
- Enhancement: Added "Not done" exercise in program history
- Enhancement: Added success and failed buttons in program record editor
- Enhancement: Added confirmation on program closing
- Enhancement: Updated Italian, German, French and Norvegian translations
- Enhancement: Finalized migration to API30
- Bug: Fixed CSV export


### 0.19.7: May 02, 2021
- Enhancement: Audible beeps at the end of rest timer #159 (Thank you @alextsakpinis)
- Enhancement: Automatic exports for backup purpose #113 (Thank you @alextsakpinis)
- Enhancement: Consider an option to track height in "Body Track" section #186

### 0.19.6: April 18, 2021
- Enhancement: Fixed German translation (Thank you @lucca-ruhland)
- Enhancement: Added Ramdom play for music player (#13 - Thank you @alextsakpinis)
- Bug: Fixed access grant for music player (#194 - Thank you @alextsakpinis)
- Bug: Fixed small UI bug
- Bug: Fixed profile birthday update (#189)

### 0.19.5: January 24, 2021
- Enhancement: Add 1 Max Rep in graph (#185 - Thank you @realGWM)
- Enhancement: Updated graph function - view rep count (#182)
- Enhancement: Added Russian language (#187 - Thank you @zayn1991)
- Bug: Unable to import after fresh install (#180)
- Bug: Fix wrong names for exported files (#183)

### 0.19.4: September 28, 2020\n
- Enhancement:  Add Italian language (#179 - Thank you @mimo84)
- Bug: Incomplete program name in history (#171)
- Bug: BMI wrong when weight is not in kg (#174)

### 0.19.3: August 3, 2020
- Enhancement : Update auto clear mechanism for Exercise (#154) (Thank you @ironjan)
- Enhancement : Added max length graphic for isometric exercises
- Bug : Fixed timer not vibrating (#145) (Thank you @sdriv3r)
- Bug : Fixed crash on program launch (#166) (Thank you @ConstanHin)
- Bug : Fixed exercise delete behavior (#162)
- Bug : Fixed conversion issue on LB weight

### 0.19.2: July 15, 2020
- Enhancement : Update the zoom feature for body tracking graph (#158)
- Enhancement : Several UI enhancement (#147, #155) (Thank you @ironjan)
- Bug : Fixed Body measure in kg/lbs/st while it should be in cm/in (#160)
- Bug : Fixed spanish translation (#151) (Thank you @J053Fabi0)

### 0.19.1: July 10, 2020
- Enhancement : Updated Exercise page UI (#130, #131, #132)
- Enhancement : Added units for body measures (#102)
- Bug : Fixed photo import from gallery on Android
- Bug : Fixed program behavior when changing profile
- Bug : Fixed crash after deleting a program that is in progress (#142) (Thank you @sdriv3r)
- Bug : Fixed units not taking the values from settings (#140) (Thank you again @sdriv3r)
- Bug : Fixed brazilian translation (#139) (Thank you again @rffontenelle)
- Bug : Fixed CVS import crash
- Bug : Fixed Bar Graph not at zero
- Bug : Fixed Program template showing in graphs

### 0.19.0.1: June 20, 2020
- Bug : Fixed Brazilian translation (#135) (Thanks @rffontenelle)

### 0.19.0: June 16, 2020
- Enhancement : New program feature (#82) (Thanks @senpl for the mockup)
- Enhancement : Possibility to edit records (#66)
- Enhancement : Several UI enhancements
- Enhancement : Several architectural updates
- Bug : Fixed light/dark mode crash when mode is different from system mode

### 0.18.11: April 04, 2020
- Enhancement : Possibility to add new body part
- Enhancement : Clarified unit for size (#118)
- Enhancement : Renamed bodybuilding category to strength (#117)
- Enhancement : Some icons update

### 0.18.10: March 02, 2020
- Bug: Fixed crash on start!!

### 0.18.9: February 27, 2020
- Enhancement : DarkMode (#96)
- Enhancement : Added dutch translation (Thanks Rishabh)

### 0.18.8: February 22, 2020
- Enhancement : Added Miles unit to distance #104
- Enhancement : Added possibility to set custom times #106
- Enhancement : Better visibility in intro
- Bug : Fixed Brazilian trapezius translation (Thanks Rafael) #105
- Bug: Fixed german typo (Thanks Matthias #100)

### 0.18.7: August 10, 2019
- Enhancement : Added muscles (#81)
- Bug : Fixed behavior on muscle checklist (#95)

### 0.18.6: August 10, 2019
- Enhancement : Added Czech Translation (#90) (Thanks @venous)
- Bug : Fixed behavior on muscle checklist (#92)

### 0.18.5: August 3, 2019
- Bug : Fixed issue on Weight fragment adding unexpected value when changing date
- Enhancement : Added graph in weight fragment
- Bug : Fixed issue not showing the keyboard on Workout fragment (#86)
- Enhancement : Added seconds to Duration for cardio records (#83)

### 0.18.4: July 22, 2019
- Enhancement : Removed Google play services for FDroid compatibility (#84)

### 0.18.3: July 19, 2019
- Enhancement : Removed Firebase
### 0.18.2: July 09, 2019
- Bug : CSV export fixes (#74, #75, #76, #77)
- Enhancement : Spanish traduction (Thanks to @sguinetti)

### 0.18.1 : June 25, 2019
- Enhancement : Add Brazilian Portuguese translation (Thanks to @rffontenelle)
- Bug : Fixed impossible crash.

### 0.18 : June 12, 2019
- Enhancement : Added Static exercise type (#61)
- Bug : Fixed spinner scroll issues
- Bug : Fixed other UI issues

### 0.17.3 : May 12, 2019
- Enhancement : Added FFMI index (#62)
- Enhancement : Project refactoring & migration to androidX (Thank to @TacoTheDank)
- Bug : Fixed crash after app reset (#65)

### 0.17.2 : April 5, 2019
- Enhancement : Permissions have been made optional
- Enhancement : Cardio fragment removed

### 0.17.1 : March 27, 2019
- Bug : Fixed bug where app would freeze while editing the creation profile

### 0.17 : March 17, 2019
- Enhancement : Optimized start-up and updated UI
- Enhancement : Added fat, muscle, water, and BMI to weight tracking
- Enhancement : Added date to record list
- Enhancement : Added gender to profile for BMI calculation
- Bug : Fixed vibration at the end of countdown when the screen is off
- Bug : Other small bug fixes

### 0.16.2 : January 16, 2019
- Enhancement : It's now possible to copy existing records
- Enhancement : Main list now displays last date history
- Enhancement : Refactoring for better performance
- Enhancement : Added filtering in exercise page (thanks to @geniusupgrader)

### 0.16.1 : November 14, 2018
- Bug : Fixed crash on Cardio page
- Bug : Fixed title on Workout page

### 0.16 : November 8, 2018
- Enhancement : Merged Cardio and Bodybuilding UI
- Enhancement : UI improvement
- Enhancement : Records are now visible on Exercise page
- Bug : Removed case sensitivity for the Exercise list's order
- Bug : Fixed crash on graph when less than 5 reps

### 0.15.3 : October 8, 2018
- Enhancement : Ability to add an exercise from the Exercise page (thanks to @geniusupgrader)
- Enhancement : Ability to set exercise as Favorite in the list
- Enhancement : Ability to edit exercise from main screen by clicking on the exercise picture
- Enhancement : Renaming of terms

### 0.15.2 : September 19, 2018
- Bug : Fixed intro

### 0.15.1 : September 13, 2018
- Bug : Fixed float issue for profile size
- Bug : Fixed wrong image rotation
- Bug : Fixed CVS export for body measures

### 0.15 - August 8, 2018
- Enhancement : Profile photo in Menu
- Enhancement : New profile fragment
- Enhancement : Ability to remove photo
- Enhancement : Small UI updates

### 0.14.6 - June 15, 2018
- Enhancement : Added German language support (Thanks to @EGUltraTM)

### 0.14.5 - June 11, 2018
- Enhancement : Ability to set a picture for the profile
- Enhancement : Crop tool for pictures
- Enhancement : New Dialog box and Toast, Init optimization

### 0.14.4 - May 19, 2018
- Bug : Fixed issue with Time dialog display on Android 8+
- Bug : Fixed issue with Camera dialog display on Android 8+
- Enhancement : Better exercise refresh
- Enhancement : Automatically close intro after profile creation
- Enhancement : MP3 player hidden by default

### 0.14.3 - May 15, 2018
- Bug : Fixed sum of weight during rest time
- Bug : Fixed some crash on app resume
- Enhancement : Added min and max value for exercises
- Enhancement : Performance enhancement
- Enhancement : Rest countdown vibrates 2 sec before the end
- Enhancement : Added cross in table to delete Weight and Body measurements
- Enhancement : Exercise list with icons and details
- Enhancement : Current exercise with icons

### 0.14.2 : February 18, 2018
- Bug : Fixed profile initialization

### 0.14.1 : February 11, 2018
- Bug : Fixed crash on initialization

###	0.14 : February 6, 2018
- Enhancement : App introduction for new users
- Enhancement : Added summary to the rest dialog box
- Enhancement : Ability to define default weight Unit
- Enhancement : Weight on exercise can now be a float
- Bug : Fixed issue with Body weight graph date being one day off
- Bug : Minor UI fixes and fixed typo
- Bug : Fixed crash on machine photo click

###	0.13.2 : October 14, 2017
- Bug : Fixed issue on photo association for machines
- Enhancement : Removed internet permission

###	0.13.1 : 08/10/2017
- Enhancement : Added fill color to graph
- Bug : Fixed typo

###	0.13 : September 17, 2017
- Enhancement : Added Body measurement feature
- Enhancement : Rest time option after adding record
- Enhancement : Change audio time when clicking on the progress bar
- Enhancement : Date modified to follow local settings
- Enhancement : Simplified UI
- Enhancement : New app icon
- Bug : Fix crash when no profile is entered

### 0.12 : September 25, 2016
- Enhancement : Open-sourced on GitHub!
- Enhancement : New tab display
- Enhancement : Added time to records
- Enhancement : Added "all" button to graph page
- Enhancement : UI Enhancement
- Bug : Fixed graph fragment that was blocked on SUM filter
- Bug : Fixed MP3 player reappearing
- Bug : Fixed crash with switching profiles

### 0.11 : November 7, 2015
- Enhancement : Ability to get machine image from gallery
- Enhancement : Ability to hide the player bar
- Enhancement : Ability to rename profile
- Enhancement : Language corrections
- Enhancement : Database optimization

### 0.10 : September 26, 2015
- Enhancement : Changed name
- Enhancement : Ability to associate a photo to a machine
- Enhancement : Add table for profile weight
- Enhancement : Zoom button for graph
- Enhancement : MP3 player UI and more file formats supported
- Bug : Fixed renaming a machine not being possible

### 0.9 : August 3, 2015
- Bug : Fixed crash with 0.8 update
- Bug : Fixed cardio date picker not working
- Enhancement : MP3 player listview is more readable

### 0.8 : August 1, 2015
- Enhancement : Rework machine page
- Enhancement : CVS profile weight export
- Enhancement : Graphics automatic zoom
- Enhancement : Music player icon modification
- Enhancement : Application icon modification
- Bug : Fixed empty profile not being allowed
- Bug : Fixed crash when player stopped

### 0.7 : June 7, 2015
- Enhancement : Added functions to weightlifting graph
- Enhancement : Pause music when headphones are unplugged
- Enhancement : New icons for navigation drawer
- Bug : Fixed crash at the end of the playlist

### 0.6 : March 15, 2015
- Enhancement : Added music player
- Enhancement : Added chronometer
- Enhancement : Ability to rename a machine
- Enhancement : Added unit selection
- Enhancement : Some UI enhancements
- Enhancement : Added date selection in profile view
- Bug : Fixed wrong values in Graph view
- Bug : Fixed view overlap on restore

### 0.5 : December 28, 2014
- Enhancement : Application Navigation modified
- Enhancement : Import/Export CSV (Beta)
- Enhancement : Keep selected machine in History and Graph
- Performance : Limit to 10 records in Records panel

### 0.4 : December 10, 2014
- Enhancement : Removed API7 support
- Enhancement : Filter machine by profile
- Bug : Fix crash when entering empty weight
- Bug : Fix dates on profile weight graph

### 0.3 : November 26, 2014
- Enhancement : Update of Cardio fragment
- Bug : Fix crash when restoring app

### 0.2 : November 22, 2014
- Enhancement : Added profile management
- Bug : Fix few bugs

### 0.1 : October 20, 2014
- Beta version
