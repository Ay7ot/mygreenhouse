---

# 🌿 My Greenhouse

A mobile application to track, manage, and analyze plant growth, tasks, and environmental conditions.

---

## 📋 Project Outline

### 1. Overview

**App Name:** My Greenhouse
**Purpose:** To help users manage plant growth with tools for scheduling, tracking, and analyzing data.

---

### 2. Main Screens & Features

#### 2.1 Dashboard

* **Navigation Menu (Hamburger Menu)**
  Options:

  * My Greenhouse
  * Add to Greenhouse
  * Task
  * Quick Stats
  * My Dank Bank
  * Settings

* **Plant Image Carousel**

  * Displays plants from "Add to Greenhouse"
  * Editable plant cards (Strain Name & Batch Number)

* **Task Alerts**

  * Upcoming tasks
  * Tap to view task details

---

#### 2.2 Add to Greenhouse

* **Form Fields**

  * Strain Name
  * Batch Number
  * Seed or Clone

    * **Seed:** Germination, Seedling, Vegetation, Flower, Drying, Curing
    * **Clone:** Non-rooted, Rooted, Vegetation, Flower, Drying, Curing
  * Autoflower or Photoperiod

    * **Autoflower:** Show "Seed to Harvest"
    * **Photoperiod:** Show "Flower Duration"
  * Start Date (Date Picker)
  * Nutrient Input (multi-entry fields)
  * Soil Type

* **Save Functionality**

  * Data saved to Room Database
  * Plant appears in Dashboard carousel

* **Edit Mode**

  * Modify plant details
  * Upload/delete images
  * View growth progress

---

#### 2.3 Task Scheduling

* **Task Types**

  * Watering
  * Feeding
  * Pest Control
  * Soil Test
  * Water Test
  * CO₂ Supplementation
  * Light Cycle Changes

* **Scheduling**

  * Date & Time Pickers
  * Tasks stored in Room Database

* **Task Management**

  * Edit, delete, reschedule
  * Mark as completed

* **Notifications**

  * Alerts on Dashboard
  * Tap to view task details

---

#### 2.4 Quick Stats

* **Statistics**

  * Total Plants (excluding drying/curing)
  * Drying Count
  * Curing Count
  * Days in growth stage

* **Charts**

  * Bar Chart: Number of plants per growth stage
  * Line Graph: Growth trends over time (MPAndroidChart)

---

#### 2.5 My Dank Bank

* **Harvest Tracking**

  * Total dried & cured weight
  * Manual entry (grams/ounces)

* **Seed Bank Management**

  * Strain Name
  * Batch Number
  * Seed Count
  * Add/Edit/Delete entries

* **Charts**

  * Pie Chart: Proportion of different stored strains
  * Bar Graph: Harvest trends over time

---

#### 2.6 Settings

* Theme selection (Light/Dark)
* Authentication (Password / Biometric)
* **Photo Management**

  * View and delete uploaded images
* **Data Management**

  * Export/import logs
  * Clear all data (confirmation prompt)

---

## ⚙️ 4. Core Functionalities

### 4.1 Data Handling

* Room Database for offline storage
* ViewModel + LiveData for UI updates
* Coroutines for background processing

### 4.2 Task Reminders

* WorkManager for scheduling
* System notifications

### 4.3 Image Management

* Local storage with optional Firebase backup
* Timelapse mode to view growth over time

### 4.4 AI Growth Analysis (Future)

* Track environmental conditions
* Provide growth recommendations
* Detect anomalies (e.g. humidity, light)

---

## 🔁 5. User Flow & Navigation

### Dashboard Navigation

1. User opens app → Sees Dashboard with carousel
2. Selects a plant card → Goes to Edit screen
3. Uses hamburger menu to navigate

### Adding a Plant

1. Clicks "Add to Greenhouse"
2. Fills out form
3. Saves → Plant appears in Dashboard

### Scheduling a Task

1. Goes to Task Screen
2. Selects a task type
3. Chooses Date & Time
4. Task alert appears on Dashboard

### Tracking Growth Progress

1. Opens Quick Stats
2. Views total plants and stages
3. Clicks plant → Views detailed progress

---

## 🛠️ 6. Technical Requirements

* **Language:** Kotlin (Android)
* **Database:** Room Database (SQLite)
* **UI Frameworks:** RecyclerView, ViewPager2, MPAndroidChart
* **Notifications:** WorkManager
* **Storage:** Internal + optional Firebase
* **Authentication:** Biometric / Password

---

## 🚀 7. Future Enhancements

* AI-powered growth recommendations
* Community sharing & comparison
* Cloud-based log/image sync
