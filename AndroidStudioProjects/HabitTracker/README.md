# Daily Habit Tracker 🌷

A simple Android app designed to help users build consistent daily habits and track their progress over time.

## ✨ Features

- Create and manage daily habits.
- Track habit completion.
- View habit history using a calendar.
- Save habit data locally so it remains available after reopening the app.
- Connect to a Java backend to load habit data.
- Maintain up to 90 days of habit history.
- A soft lavender-themed user interface.

## 📱 App Screenshots

### 1. Home Screen
<img width="368" height="822" alt="home-screen" src="https://github.com/user-attachments/assets/e0f27b63-0f9c-4b75-bd90-2e835c2eae39" />




### 2. Add a Habit
<img width="362" height="827" alt="add-habit" src="https://github.com/user-attachments/assets/6153d472-692f-4954-874b-18195eee2954" />




### 3. Habit Added
<img width="372" height="828" alt="habit-added" src="https://github.com/user-attachments/assets/542c617a-3d74-4ec3-8743-d5a40a3076b5" />



### 4. Habit History
<img width="362" height="829" alt="habit-history" src="https://github.com/user-attachments/assets/8c361cd1-d023-4cfa-ba68-126eac786ea1" />



## 🛠️ Tech Stack

**Android**
- Kotlin
- Android Jetpack Compose
- SharedPreferences for local data persistence

**Backend**
- Java
- Spring Boot
- Spring Data JPA
- H2 Database
- REST APIs

## 🏗️ Project Structure

- `app/` — Android application
- `gradle/` — Gradle wrapper and build configuration

The backend is maintained separately in the repository's `main` branch.

## 🚀 Running the Android App

1. Install Android Studio.
2. Clone this repository.
3. Open the Android project located at `AndroidStudioProjects/HabitTracker/`.
4. Allow Gradle to sync.
5. Run the app on an Android emulator or compatible device.

## 🔌 Backend Setup

The backend is a separate Java Spring Boot application maintained on the `main` branch of this repository.

**Configuration**
- Server port: `8080`
- Database: H2 file-based database
- Database location: `./data/habitdb`
- ORM: Spring Data JPA

**Run locally**
1. Check out the `main` branch.
2. Open the backend project in your IDE or terminal.
3. Ensure a compatible Java and Maven environment is available.
4. Start the Spring Boot application.
5. The Android emulator can reach the development machine's backend at `http://10.0.2.2:8080`.

This configuration is intended for local development. Review network binding and database security before deploying the backend publicly.

## 🎯 Project Goal

This project was built to practise Android development, backend integration, data persistence, and building a complete application.

## 👩‍💻 Developer

**Akhila Narkenabilli**

- GitHub: [Akhila-git10](https://github.com/Akhila-git10)
- Project Repository: [Daily Habit Tracker](https://github.com/Akhila-git10/habit-tracker)
