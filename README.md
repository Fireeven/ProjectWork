# Grocery List App

A modern Android application built with Jetpack Compose for managing grocery lists and shopping locations.

## Features

- 🛒 Create and manage multiple grocery lists
- 📍 Add and organize shopping locations
- ✏️ Edit items and quantities with intuitive controls
- 🔄 Sort items by name, quantity, or category
- 🎨 Beautiful Material 3 design with animations
- 📱 Modern Jetpack Compose UI
- 💾 Local storage with Room database

## Screens

1. **Welcome Screen**
   - Animated introduction with Lottie animation
   - Smooth transitions to main app

2. **Home Screen**
   - List of shopping locations
   - Add new locations
   - Quick access to grocery lists

3. **Place Detail Screen**
   - View and manage items for a specific location
   - Add, edit, and delete items
   - Sort items by different criteria

4. **Edit Screens**
   - Add/Edit items with quantity controls
   - Manage item details
   - Intuitive plus/minus buttons for quantity

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **Animations**: Lottie, Compose Animations

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Run the app on an emulator or physical device

## Dependencies

The project uses the following major dependencies:

```gradle
// Compose
implementation(platform(libs.compose.bom))
implementation(libs.compose.ui)
implementation(libs.compose.material3)

// Room Database
implementation(libs.room.runtime)
implementation(libs.room.ktx)
ksp(libs.room.compiler)

// Navigation
implementation(libs.navigation.compose)

// Lottie Animation
implementation("com.airbnb.android:lottie-compose:6.3.0")
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/projectwork/
│   │   │       ├── data/           # Database and repositories
│   │   │       ├── di/             # Dependency injection
│   │   │       ├── navigation/     # Navigation components
│   │   │       ├── screens/        # UI screens
│   │   │       ├── ui/             # UI components
│   │   │       └── utils/          # Utility classes
│   │   └── assets/                 # Lottie animations
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Material Design 3 for the beautiful UI components
- Lottie for the amazing animations
- Jetpack Compose for the modern UI framework
