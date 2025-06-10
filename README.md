# My Greenhouse - Android Application

## 📱 Overview

**My Greenhouse** is a comprehensive Android application designed for cannabis cultivation enthusiasts and professionals. This app provides a complete solution for managing your greenhouse operations, from plant tracking and task scheduling to harvest management and seed banking.

Whether you're a home grower or managing a larger operation, My Greenhouse helps you stay organized, track your plants' progress, and optimize your cultivation process with smart notifications and detailed analytics.

## ✨ Key Features

### 🌱 **Dashboard & Plant Management**
- **Plant Carousel**: Visual overview of all plants in your greenhouse with circular plant cards
- **Plant Profiles**: Detailed tracking including strain name, batch number, growth stage, and progress
- **Plant Types**: Support for both Autoflower and Photoperiod varieties
- **Growth Stages**: Track from Germination → Seedling → Vegetation → Flower → Harvest
- **Clone Management**: Special handling for clone cultivation (Non-rooted → Rooted → Vegetation → Flower)
- **Photo Management**: Take and store progress photos with camera integration

### 📅 **Task Management & Scheduling**
- **Smart Task Alerts**: Automated reminders for watering, feeding, and maintenance
- **Task Types**: Watering, Feeding, Pest Control, Soil/Water Testing, CO₂ Supplementation, Light Cycle Changes
- **Recurring Tasks**: Set up weekly schedules (Monday, Tuesday, etc.)
- **Plant-Specific Tasks**: Associate tasks with individual plants or batches
- **Overdue Tracking**: Visual indicators for overdue and upcoming tasks
- **Push Notifications**: Never miss important cultivation tasks

### 📊 **Quick Stats & Analytics**
- **Live Statistics**: Total plants, active batches, growth stage distribution
- **Growth Stage Charts**: Visual breakdown of plants per growth stage
- **Average Days Tracking**: Monitor typical time spent in each growth stage
- **Strain-Specific Analytics**: Filter statistics by individual strains

### 🏦 **My Dank Bank (Harvest & Seed Management)**
- **Harvest Tracking**: 
  - Log wet and dry weights
  - Track drying and curing processes
  - Quality rating system
  - Harvest timeline and notes
- **Seed Bank Management**:
  - Inventory tracking for multiple strains
  - Seed count and acquisition tracking
  - Support for all seed types (Autoflower Regular/Feminized, Photoperiod Regular/Feminized)
  - Custom strain management
- **Storage Analytics**: Visual charts showing strain distribution and harvest trends

### ⚙️ **Settings & Security**
- **Theme Customization**: Light and Dark mode support
- **Security Options**: PIN/Password and Biometric authentication
- **Data Management**: Export/import capabilities and data backup
- **Photo Management**: Centralized photo viewing and management

## 🛠️ Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3 Design
- **Architecture**: MVVM (Model-View-ViewModel) with Repository pattern
- **Database**: Room Database with SQLite
- **Async Operations**: Kotlin Coroutines and Flow
- **Background Tasks**: WorkManager for notifications and reminders
- **Image Loading**: Coil for efficient image handling
- **Navigation**: Jetpack Navigation Compose
- **Charts**: MPAndroidChart for data visualization
- **Authentication**: Android Keystore for secure credential storage

## 📋 Requirements

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Permissions**: 
  - Camera (for taking plant photos)
  - Storage (for photo management)
  - Notifications (for task reminders)

## 🚀 Installation

### For Users
1. Download the latest APK from the [Releases](../../releases) page
2. Enable "Install from Unknown Sources" in your Android settings
3. Install the APK and grant necessary permissions
4. Launch the app and set up your greenhouse!

### For Developers
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/mygreenhouse.git
   cd mygreenhouse
   ```

2. Open the project in Android Studio (Arctic Fox or later)

3. Build and run:
   ```bash
   ./gradlew assembleDebug
   ```

## 📖 Usage Guide

### Getting Started
1. **First Launch**: Set up authentication (PIN or biometric)
2. **Add Your First Plant**: Use "Add to Greenhouse" to create your first plant entry
3. **Schedule Tasks**: Set up watering and feeding schedules
4. **Track Progress**: Take photos and update growth stages regularly

### Plant Management
- **Adding Plants**: Fill in strain name, batch number, start date, and growing medium
- **Growth Stages**: Update stages as plants progress
- **Photo Timeline**: Build a visual timeline of your plant's growth
- **Harvest**: When ready, move plants to "My Dank Bank" for harvest tracking

### Task Scheduling
- **Create Tasks**: Choose task type, set time, and select repeat days
- **Plant Association**: Link tasks to specific plants or keep them general
- **Completion**: Mark tasks complete directly from the dashboard
- **Notifications**: Receive timely reminders for all scheduled tasks

## 📸 Screenshots

*Coming Soon - Screenshots will be added in the next update*

## 🗺️ Development Roadmap

### ✅ Completed Features
- Core plant and task management
- Dashboard with plant carousel and task alerts
- Comprehensive harvest and seed tracking
- Statistics and analytics with charts
- Light/Dark theme support
- Authentication and security features

### 🔄 In Progress
- Enhanced photo management features
- Data export/import functionality
- Performance optimizations

### 📅 Future Plans
- Cloud backup integration
- AI-powered growth analysis
- Community features and strain sharing
- Advanced automation capabilities

## 🤝 Contributing

We welcome contributions to My Greenhouse! Here's how you can help:

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/AmazingFeature`)
3. **Commit** your changes (`git commit -m 'Add some AmazingFeature'`)
4. **Push** to the branch (`git push origin feature/AmazingFeature`)
5. **Open** a Pull Request

### Bug Reports
Found a bug? Please open an issue with:
- Device information
- Android version
- Steps to reproduce
- Screenshots if applicable

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Android Jetpack** team for excellent development tools
- **Material Design** team for beautiful UI components
- **Kotlin** team for the amazing programming language
- **Cannabis cultivation community** for inspiration and feedback

## 📞 Support

For support, feature requests, or general questions:
- **Create an Issue**: For bugs and feature requests
- **Discussions**: For general questions and community interaction

---

<div align="center">
  
  **Happy Growing! 🌿**
  
  *Cultivate with confidence using My Greenhouse*

</div> 
