# TaskFlow 📝
### Organize Your Life, One Task at a Time.

TaskFlow is a robust, feature-rich Android task management application built with a focus on professional UI/UX and efficient performance. Designed for the modern user, it provides a seamless experience for tracking daily responsibilities, scheduling reminders, and maintaining a high level of productivity through organized task prioritization.

---

## 🚀 Core Features

### 📅 Advanced Task Management
- **Versatile Task Types**: Support for both **Daily Recurrence** and **One-Time Events**.
- **Precise Reminders**: Integrated notification system using `AlarmManager` and `BroadcastReceivers` to ensure you never miss a deadline.
- **Dynamic Prioritization**: Three-tier priority system (High 🔴, Medium 🟠, Low 🟢) with bold visual coding for immediate focus.

### 🎨 Personalization & UI
- **Adaptive Dark Mode**: A custom-engineered, system-wide dark theme toggle that persists across app restarts.
- **Customizable Aesthetics**: Choose from a vibrant color palette for each task card to visually categorize your work.
- **Material 3 Design**: Utilizing the latest Material Design components, including `MaterialCardView`, `FloatingActionButton`, and `TextInputLayout`.
- **Branded Splash Screen**: A professional entry experience featuring the TaskFlow logo and branding.

### ⚡ Seamless User Experience
- **Interactive Gestures**: Swipe-to-delete functionality for rapid list management.
- **Long-Press Editing**: Intuitive access to task modification via long-press on any list item.
- **Smart Sorting**: Automatic list organization prioritizing incomplete tasks and urgent priorities.
- **Data Persistence**: Lightweight local storage using `SharedPreferences` and `JSON` serialization—no heavy database required.

---

## 🛠️ Technical Stack

- **Language**: Java
- **UI Framework**: Android XML with Material Components
- **Architecture**: Model-View-Adapter (MVA) pattern
- **List Handling**: `RecyclerView` with optimized `TaskAdapter`
- **Background Services**: `AlarmManager` for scheduling and `NotificationManager` for alerts
- **Storage**: `SharedPreferences` with `org.json` for object serialization

---

## 📂 Project Architecture

- **`MainActivity.java`**: The core controller managing the UI state, theme application, and dialog orchestrations.
- **`TaskAdapter.java`**: Custom RecyclerView adapter handling complex data binding, dual-sorting logic, and theme-responsive styling.
- **`ReminderScheduler.java`**: Utility class for precise alarm management and scheduling.
- **`SplashActivity.java`**: Handles the initial branding experience and smooth transition to the main interface.
- **`Task.java`**: The comprehensive data model supporting multiple attributes and backwards compatibility.

---

## 💻 Getting Started

### Prerequisites
- Android Studio Iguana (or newer)
- Android SDK Level 24 (Nougat) or higher
- A physical device or emulator running Android 7.0+

### Installation
1. Clone the repository to your local environment:
   ```bash
   git clone https://github.com/KABU-DEVS/TaskFlow.git
   ```
2. Open the project in **Android Studio**.
3. Allow Gradle to sync and download dependencies.
4. Build and Run (**Shift + F10**) on your target device.

---

## 👥 Collaboration & Credits

TaskFlow is a collaborative effort by the **KABU-DEVS** team.
- **Core Task Logic & Persistence**
- **Reminder Scheduling & Notification Systems**
- **UI Design & Theme Engineering**

---

## 📄 License
This project is developed as part of an Android Development Assignment and is intended for educational and productivity purposes.

---
*Built with ❤️ by the KABU-DEVS Team*
