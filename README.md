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
- 🤖 AI assistance with OpenAI integration for intelligent suggestions

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

5. **API Test Screen**
   - Test OpenAI integration
   - Generate sample responses
   - Visualize AI capabilities

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Database**: Room
- **Navigation**: Navigation Compose
- **Animations**: Lottie, Compose Animations
- **AI Integration**: OpenAI API

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync the project with Gradle files
4. Add your OpenAI API key to `gradle.properties` file
5. Run the app on an emulator or physical device

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

// OpenAI API
implementation("com.aallam.openai:openai-client:3.6.3")
implementation("io.ktor:ktor-client-android:2.3.7")
```

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/projectwork/
│   │   │       ├── data/           # Database, entities and repositories
│   │   │       ├── navigation/     # Navigation components
│   │   │       ├── screens/        # UI screens
│   │   │       ├── ui/             # UI components and themes
│   │   │       ├── utils/          # Utility classes and helpers
│   │   │       └── viewmodel/      # ViewModels for screens
│   │   ├── assets/                 # Lottie animations
│   │   └── res/                    # Resources and layouts
```

## Key Components

### Data Models
- **PlaceEntity**: Represents shopping locations
- **GroceryItem**: Represents items in grocery lists
- **Category**: Categorizes grocery items

### Database
- Room database with migrations support
- Type converters for complex data types
- DAOs for database operations

### UI Components
- Custom composable components for consistent UI
- Material 3 design implementation
- Responsive layouts for different screen sizes

## API Integration

The app uses the OpenAI API for intelligent suggestions. To use this feature:

1. Obtain an API key from OpenAI
2. Add it to the `gradle.properties` file:
   ```
   OPENAI_API_KEY="YOUR_API_KEY_HERE"
   ```
3. The OpenAIHelper class manages API requests and responses

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
- OpenAI for the intelligent API integration 