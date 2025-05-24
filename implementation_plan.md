# My Greenhouse - Implementation Plan

This document tracks the development progress of the "My Greenhouse" Android application.

## 1. Core Modules & Features

Based on `greenhouse.md`:

*   **Dashboard:**
    *   [X] Navigation Menu (Hamburger)
    *   [X] Plant Image Carousel (editable cards: Strain Name & Batch Number)
    *   [X] Task Alerts (upcoming tasks, tap to view details)
    *   [X] Enhanced empty states for plants and tasks
    *   [X] Skeleton loaders for plants and tasks
    *   [X] UI Polish for active states (plant cards, task alerts)
    *   [X] Plant cards with circular images for visual consistency
*   **Add to Greenhouse:**
    *   [X] Form (Strain Name, Batch Number, Seed/Clone, Autoflower/Photoperiod, Start Date, Nutrients, Soil Type)
    *   [X] Growth Stage Logic (Seed: Germination, Seedling, Vegetation, Flower, Drying, Curing; Clone: Non-rooted, Rooted, Vegetation, Flower, Drying, Curing)
    *   [X] Autoflower/Photoperiod specific fields (Seed to Harvest / Flower Duration)
    *   [X] Save to Room Database
    *   [X] Plant appears in Dashboard carousel
    *   [X] Edit Mode (modify details, upload/delete images, view progress)
*   **Task Scheduling:**
    *   [X] Task Types (Watering, Feeding, Pest Control, Soil Test, Water Test, CO₂ Supplementation, Light Cycle Changes)
    *   [X] Scheduling (Date & Time Pickers)
    *   [X] Save tasks to Room Database
    *   [X] Task Management (Edit, delete, reschedule, mark complete)
    *   [X] Notifications (Dashboard alerts, tap to view details)
    *   [X] Enhanced Task List Screen UI (active and empty states)
    *   [X] Skeleton loader for Task List Screen
    *   [X] Enhanced Task Selection Screen UI
    *   [X] Task-Plant associations (assign tasks to specific plants)
    *   [X] Plant selection dropdown in task creation/editing
    *   [X] Display associated plant names in task alerts and list
*   **Quick Stats:**
    *   [X] Statistics (Total Plants, Drying Count, Curing Count, Days in growth stage)
    *   [X] Charts (Bar: Plants per stage, Line: Growth trends - MPAndroidChart)
*   **My Dank Bank:**
    *   [X] Harvest Tracking (Total dried & cured weight - grams/ounces)
    *   [X] Seed Bank Management (Strain Name, Batch Number, Seed Count, Add/Edit/Delete)
    *   [X] Charts (Pie: Stored strains proportion, Bar: Harvest trends)
*   **Settings:**
    *   [X] Theme selection (Light/Dark)
    *   [X] Authentication (Password / Biometric)
    *   [>] Photo Management (View/delete uploaded images)
    *   [>] Data Management (Export/import logs, Clear all data with confirmation)

## 2. Core Functionalities (Technical)

*   [X] Data Handling: Room Database, ViewModel + LiveData/StateFlow, Coroutines
*   [X] Task Reminders: WorkManager for scheduling, System notifications
*   [X] Image Management: Local storage, (Optional Firebase backup), Timelapse view

## 3. Technical Stack (Confirmed/Planned)

*   **Language:** Kotlin ✓
*   **UI:** Jetpack Compose (Material 3) ✓
*   **Database:** Room ✓
*   **UI Components:** RecyclerView (or Compose equivalent LazyLists), ViewPager2 (or Compose equivalent), MPAndroidChart (or Compose alternative)
*   **Background Tasks & Notifications:** WorkManager
*   **Storage:** Internal Storage (Optional Firebase later)
*   **Architecture:** MVVM (or MVI) with LiveData/Kotlin Flows ✓
*   **Authentication:** Biometric / Password (Android Keystore)

## 4. Development Milestones & Tasks

(Adapted from `greenhouse_proposal.md`)

### Milestone 1: UI/UX Setup & Base Architecture (Week 1 Focus)

*   [X] **Branding & Theme:**
    *   [X] Define color palette (refine from default `Color.kt`)
    *   [X] Define typography (refine from default `Type.kt`)
    *   [X] Implement Light/Dark theme switching mechanism (Settings screen later, but foundation now)
*   [X] **Base Project Structure:**
    *   [X] Set up MVVM/MVI architecture (create base classes/interfaces if needed).
    *   [X] Set up Room Database:
        *   [X] Define initial Entities (e.g., Plant, Task)
        *   [X] Define DAOs
        *   [X] Create Database class
        *   [X] Set up Dependency Injection (e.g., Hilt) for database and view models.
*   [X] **Navigation:**
    *   [X] Implement basic navigation graph (Jetpack Compose Navigation).
    *   [X] Create placeholder screens for main modules.
    *   [X] Implement main navigation structure (e.g., Bottom Navigation or Hamburger Menu for Dashboard).
*   [X] **Initial Dashboard UI:**
    *   [X] Basic layout for Dashboard screen.
    *   [X] Placeholder for Plant Carousel.
    *   [X] Placeholder for Task Alerts.

### Milestone 2: Dashboard + Add Plant Flow

*   [X] **Plant Entity & DAO (Refine/Implement):**
    *   [X] Finalize all fields for Plant entity.
    *   [X] Implement all necessary DAO methods for Plant (CRUD).
*   [X] **"Add to Greenhouse" Screen:**
    *   [X] Create UI for the form.
    *   [X] Implement input fields and pickers (Date, Seed/Clone, etc.).
    *   [X] Implement DatePickerDialog for Start Date.
    *   [X] Implement logic for conditional fields (Autoflower/Photoperiod, Seed/Clone stages).
    *   [X] ViewModel for "Add to Greenhouse" screen.
    *   [X] Integrate Image Picker.
    *   [X] Save functionality (ViewModel interacts with Repository/DAO).
*   [X] **Dashboard - Plant Carousel:**
    *   [X] Implement Composable for Plant Card.
    *   [X] Fetch plants from database and display in a `LazyRow` or similar.
    *   [X] Clicking a card navigates to "Edit Plant".
*   [X] **"Edit Plant" Mode (Basic):**
    *   [X] Screen to display plant details (mirrors Add Plant screen).
    *   [X] Allow modification of existing plant details.
    *   [X] Save changes to database.
    *   [X] Implement DatePickerDialog for Start Date.
*   [X] **Image Handling (Basic - Local):**
    *   [X] Functionality to pick an image from gallery for a plant (Add & Edit screens).
    *   [X] Store image URI/path with the plant entity.
    *   [X] Display plant image in carousel, add screen, and edit screen.

### Milestone 3: Task Management + Notifications

*   [X] **Task Entity & DAO (Refine/Implement):**
    *   [X] Finalize all fields for Task entity.
    *   [X] Implement all necessary DAO methods for Task (CRUD, fetching upcoming).
*   [X] **"Task Scheduling" Screen:**
    *   [X] UI for selecting task type, date, time, and details.
    *   [X] ViewModel for Task Scheduling.
    *   [X] Save tasks to database.
*   [X] **Task Management UI:**
    *   [X] Display list of tasks.
    *   [X] Allow editing, deleting, rescheduling tasks.
    *   [X] Mark tasks as complete.
*   [X] **Dashboard - Task Alerts:**
    *   [X] Fetch upcoming/pending tasks.
    *   [X] Display alerts on Dashboard.
    *   [X] Tapping an alert navigates to task details/scheduling screen.
*   [X] **WorkManager for Notifications:**
    *   [X] Schedule background worker to check for due tasks. (Now: Schedule individual workers per task)
    *   [X] Implement system notifications for task reminders.

### Milestone 4: Quick Stats & My Dank Bank

*   [X] **"Quick Stats" Screen:**
    *   [X] UI for displaying statistics.
    *   [X] Logic to calculate: Total Plants, Drying Count, Curing Count, Days in growth stage.
    *   [X] ViewModel for Quick Stats.
*   [X] **Charts - Quick Stats:**
    *   [X] Integrate MPAndroidChart or Compose charting library.
    *   [X] Bar Chart: Number of plants per growth stage.
    *   [X] Line Graph: Growth trends over time (requires data logging over time - consider how to store this).
*   [X] **"My Dank Bank" Screen - Harvest Tracking:**
    *   [X] UI for logging dried & cured weight. (AddHarvestScreen created)
    *   [X] ViewModel and database updates for harvest data.
    *   [X] `HarvestListItem.kt` created for harvest list display.
    *   [X] `HarvestTrackingContent` implemented with `LazyColumn`.
    *   [X] Dialogs for entering/updating Dry and Cured weights for harvests.
    *   [X] Statistics for harvests (Total Harvested, Drying, Curing, Completed) display and update reactively.
*   [X] **"My Dank Bank" Screen - Seed Bank Management:**
    *   [X] UI for managing seed inventory (Strain Name, Batch, Count).
    *   [X] `AddSeedScreen.kt` created for adding new seeds.
    *   [X] `SeedListItem.kt` created for seed list display.
    *   [X] `SeedBankContent` implemented with `LazyColumn`.
    *   [X] CRUD operations for seed bank entries (via ViewModel).
    *   [X] Statistics for seeds (Total Seeds, Unique Strains) display and update reactively.
*   [>] **Charts - My Dank Bank:**
    *   [X] Pie Chart: Proportion of different stored strains.
    *   [X] Bar Graph: Harvest trends over time.
*   [>] **Next Steps for Dank Bank:**
    *   [X] Implement "Edit Harvest" screen and navigation.
    *   [X] Implement "Edit Seed" screen and navigation.
    *   [X] Implement Detail View screens for Harvests and Seeds.
    *   [X] Add filtering and sorting capabilities to harvest and seed lists.
    *   [X] Implement search functionality for harvests and seeds.
    *   [X] Add loading states and skeleton loaders to the Dank Bank section.
    *   [X] Refine chart implementation and ensure proper layout and scrolling.
    *   [X] Ensure correct loading state behavior with skeleton loaders for lists.

### Milestone 5 (Optional/Later): Settings, Advanced Features & Testing

*   [X] **Settings Screen:**
    *   [X] UI for all settings options.
    *   [X] Theme selection (Light/Dark) - connect to implementation from Milestone 1.
*   [X] **Authentication:**
    *   [X] Implement Password/PIN setup.
    *   [X] Implement Biometric authentication.
*   [x] **Photo Management (Advanced):**
    *   [x] Dedicated screen to view all plant photos.
    *   [x] Delete photos.
*   [x] **Data Management:**
    *   [x] Export logs/data (e.g., as CSV or JSON).
    *   [x] Import logs/data.
    *   [x] "Clear all data" functionality with confirmation.
*   [ ] **Image Management - Timelapse:**
    *   [ ] Logic to create a timelapse from a plant's images.
*   [ ] **Firebase Integration (Optional):**
    *   [ ] Cloud backup for logs/images.
*   [ ] **Comprehensive Testing:**
    *   [ ] Unit tests for ViewModels and Repositories.
    *   [ ] UI tests for critical user flows.
*   [ ] **AI Growth Analysis (Future Scope)**

### Milestone 6: UI/UX Polish & Advanced Empty/Loading States (New)

*   [X] **Dashboard Enhancements:**
    *   [X] Refine overall layout and visual hierarchy.
    *   [X] Implement polished empty state UI for plant carousel.
    *   [X] Implement polished empty state UI for task alerts.
    *   [X] Implement skeleton loader for plant carousel.
    *   [X] Implement skeleton loader for task alerts.
    *   [X] Fix empty state flicker during data loading.
    *   [X] Improve plant cards with circular images matching skeleton loader.
    *   [X] Show plant names in task alerts instead of generic text.
*   [X] **Task Screen Enhancements:**
    *   [X] Refine UI for task type selection (`TaskScreen.kt`).
    *   [X] Refine UI for task list (`TaskListScreen.kt`).
    *   [X] Implement polished empty state UI for task list.
    *   [X] Implement skeleton loader for task list.
    *   [X] Fix empty state flicker during data loading.
    *   [X] Update task screens to show associated plant names.
*   [X] **Task-Plant Association:**
    *   [X] Add plant selection dropdown in task creation
    *   [X] Add plant name display in task list and alerts
    *   [X] Cache plant data for efficient lookups
    *   [X] Create consistent UI for displaying associated plants
*   [X] **General UI Consistency:**
    *   [X] Ensure consistent styling, typography, and spacing across updated screens.
    *   [X] Remove sample data initialization to rely on true empty/loading states.
    *   [X] Make card elevation consistent (flat design) across the app.

## Client Corrections

*   **Add Plant Screen:**
    *   [X] Seed Types: Autoflower Regular, Autoflower Feminized, Photoperiod Regular, Photoperiod Feminized.
    *   [X] "Save" prompt: "Would you like to add another Plant?" (Yes/No). Yes: fresh Add Plant screen. No: return to Dashboard.
*   **Edit Plant Screen and Add Plant Screen:**
    *   [X] Input for Quantity (below Batch Number).
    *   [X] Add Plant Gender: Male, Female, Unknown.
    *   [X] Seed to Harvest: Visible countdown to harvest day.
    *   [X] Drying and Curing: Start Date picker with a visible counter (days drying/curing).
    *   [ ] Image Selection: Allow taking a picture with the camera (in addition to gallery).
    *   [ ] Change "Soil Type" to "Grow Medium".
    *   [ ] Add to "Grow Medium": Deep Water Culture, Nutrient Film Technique, Wick System, Ebb and Flow, Drip System.
    *   [ ] Change "Hydroponic" to "Hydroponic: Other".
*   **Dank Bank, Add Plant, Edit Plant:**
    *   [ ] Replace "Unique Strain" with "Custom Strain" (checkbox near Strain Name).

## Progress Tracking

*   **To Do:** [ ]
*   **In Progress:** [>]
*   **Done:** [X]

*(We will update the checkboxes as we complete tasks)* 