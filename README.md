# SportTracker

**SportTracker** is a fitness-tracking Android application that allows users to log, track, and analyze their physical activities. The app combines **manual workout entry**, **GPS-based tracking**, and **activity recognition** powered by sensor data and machine learning.  

---

## 🚀 Features

- **User Profile Management**  
  - Create and update profile with name, email, phone, gender, class year, and major.  
  - Capture or select profile photo using **Camera** or **Gallery**.  
  - Persistent storage with **SharedPreferences**.  

- **Tabbed Navigation (MVVM-based)**  
  - **Start Tab**: Choose input mode (Manual, GPS, Automatic) and activity type.  
  - **History Tab**: View past workouts, formatted with distance, duration, and activity type.  
  - **Settings Tab**: Configure preferences such as unit system (Metric/Imperial) and profile access.  

- **Workout Logging**  
  - Manual entry of duration, distance, calories, heart rate, and comments.  
  - GPS tracking with live path visualization on **Google Maps**, showing distance, climb, speed, and calories.  
  - Automatic activity recognition using accelerometer and a **Weka-based classifier**.  

- **History & Visualization**  
  - Stores workouts in a **Room Database** with Repository + ViewModel pattern.  
  - Map replay of saved workouts with route polylines and start/end markers.  
  - Unit conversion (km ↔ miles, meters ↔ feet) depending on user preference.  

- **Foreground Tracking Service**  
  - Background location and accelerometer tracking.  
  - Foreground notification ensures reliability during GPS logging.  

---

## 🛠 Tech Stack

- **Language**: Kotlin (+ Java for classifier integration)  
- **Architecture**: MVVM (Model–View–ViewModel)  
- **UI**: Android Views, XML layouts, Fragments, ViewPager2 + TabLayout  
- **Database**: Room (DAO, Repository, LiveData, TypeConverters for complex data like GPS paths)  
- **Maps & Location**: Google Maps SDK, FusedLocationProviderClient  
- **Sensors**: Android Sensor API (Accelerometer)  
- **Machine Learning**: Weka-based activity classification  
- **Persistence**: SharedPreferences (profile, settings)  

---

## 📂 Project Structure

```
app/
├── MainActivity.kt           # Entry point, tab navigation
├── Profile.kt                # User profile screen
├── Start.kt                  # Start tab with input/activity selection
├── Manual_input.kt           # Manual workout entry
├── Map_display.kt            # Live GPS tracking
├── MapDisplayActivity.kt     # View saved routes
├── History.kt                # Workout history
├── Settings.kt               # App settings
├── TrackingService.kt        # Foreground GPS + sensor service
├── data/
│   ├── InputEntryDao.kt      # DAO for Room
│   ├── InputEntryDatabase.kt # Room DB setup
│   ├── InputEntryRepository.kt # Repository layer
│   ├── InputEntryViewModel.kt  # ViewModel + Factory
├── Util.kt                   # Utilities (permissions, unit conversion, image handling)
├── MyDialog.kt               # Reusable dialogs
├── WekaClassifier.java       # ML classifier for activity recognition
```

---

## ▶️ Usage

1. Launch the app.  
2. Create or edit your **profile** in the Settings tab.  
3. Start a workout in one of three modes:  
   - **Manual** → enter data fields.  
   - **GPS** → track location and display route.  
   - **Automatic** → detect activity type (e.g., walking, running, biking).  
4. View past entries in the **History tab**.  
5. Tap an entry to see details or a replay on Google Maps.  

---

## 🎯 Technical Highlights

- Strong experience with **Android development** (Kotlin, Java).  
- Applied **MVVM architecture** with Repository + Room database.  
- Implemented **sensor-based tracking** (accelerometer, GPS).  
- Integrated **Google Maps SDK** for visualization.  
- Applied **machine learning (Weka)** for activity recognition.  
- Managed **permissions and persistence** effectively.  

---
