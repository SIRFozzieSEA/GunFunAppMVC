# GunFunApp

### Purpose

The purpose of the application was a way for me to keep track of my guns.  The app can track basic information about each gun, how many bullets  fired through it, when it was last cleaned, when it was last carried, etc.  You can log your daily carry, cleanings, shots, and provide basic reporting on those metrics. There is also a basic quiz feature built in as well... in order to use this feature, you would need to click on `Rebuild Questions` on the main navigation then you should be able to click on `Quiz` to launch a quiz session.

### Requirements

This was designed to work with Java 17.x, your mileage may vary using anything other than that.  The application is backed with an embedded H2 database which gets stored locally on your PC.

### Getting Started

To get the project running for the first time (build tables, app folders, sample images/pdfs, etc.) do the following:

- Open the `application.properties` file in the IDE.
- Edit the key `GUNFUN_APP_FOLDER` and set it to the path you'd like your application assets (data, images, pdfs, backups) to go into.
- Edit the key `DELETE_MASTER_PASSWORD` and set it to whatever password you want to use as your delete confirm password.
- Edit the key `server.port` and set it to whatever port you'd like the service to run on.
- Edit the key `spring.datasource.password` and set it to whatever password you want to secure your database with.
- Edit the key `spring.jpa.hibernate.ddl-auto` and set it to `create`. 
- Save the `application.properties` file.
- Launch the `com.codef.gunfunmvc.App` class from the IDE.
- Let the application load completely, and keep the application **running** for all steps below this.
- Edit the key `spring.jpa.hibernate.ddl-auto` value to `update`.
- Edit the key `spring.datasource.initialization-mode` value to `always`.
- Save the file.  If the application is still running, the devtools should reload the context automatically.
- Database should be rebuilt now and will show up in the path. Next we'll revert to default values for normal operation.
- Edit the key `spring.jpa.hibernate.ddl-auto` value to `none`.
- Edit the key `spring.datasource.initialization-mode` value to `never`.
- Save the file.  If the application is still running, the devtools should reload the context automatically.
- Click to launch the app.  Default URL is `http://localhost:9002/`
- The first run should trigger the rebuild of the assets folder and the start page should show four (4) firearms registered.
- Comment out the `UtilsBuildConvert.checkDataFoldersBuilt(getGunFunAppLocation());` (line 106) in `AppController.java` to disable this rebuild logic.
- That's it!  Have fun!


### 'To Do' List
- A way to customize template questions
- Break out data backup -- data, images, manuals -- SQL for rebuilding database from scratch?
- Sorting on some pages
- Preference page????
- Limit history to so many months
- Fix backup independence