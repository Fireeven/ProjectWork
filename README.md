# 🛒 Smart Grocery Manager
### *AI-Powered Shopping List & Recipe Assistant*

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![OpenAI](https://img.shields.io/badge/OpenAI-412991?style=for-the-badge&logo=openai&logoColor=white)

*A modern Android application that revolutionizes grocery shopping with AI-powered recipe suggestions, smart analytics, and intuitive list management.*

[Features](#-features) • [Screenshots](#-screenshots) • [Installation](#-installation) • [Architecture](#-architecture) • [API Integration](#-api-integration)

</div>

---

## 🌟 Features

### 🤖 **AI-Powered Intelligence**
- **Smart Recipe Suggestions**: Get personalized recipe recommendations based on your ingredients
- **Intelligent Shopping Assistant**: AI chatbot helps with meal planning and ingredient suggestions
- **Price Estimation**: Smart price predictions for better budget planning
- **Nutritional Insights**: Get nutritional information for your recipes and ingredients

### 📊 **Advanced Analytics**
- **Spending Insights**: Track your grocery expenses with beautiful charts and graphs
- **Shopping Patterns**: Analyze your shopping habits and trends over time
- **Budget Management**: Set and monitor spending goals for different categories
- **Purchase History**: Detailed history of all your grocery purchases

### 🛒 **Smart Shopping Lists**
- **Multiple Store Support**: Organize lists by different shopping locations
- **Category Management**: Automatically categorize items for efficient shopping
- **Purchase Tracking**: Mark items as purchased with actual price tracking
- **Quantity Management**: Intuitive controls for managing item quantities

### 🎨 **Modern User Experience**
- **Material 3 Design**: Beautiful, modern interface following Google's latest design principles
- **Smooth Animations**: Lottie animations and Compose transitions for delightful interactions
- **Dark/Light Theme**: Adaptive theming that respects system preferences
- **Responsive Design**: Optimized for different screen sizes and orientations

---

## 📱 Screenshots

### Welcome & Onboarding
<div align="center">
<table>
<tr>
<td align="center">
<img src="screenshots/welcome_screen.png" width="250" alt="Welcome Screen"/>
<br><b>Welcome Screen</b>
<br><i>Animated onboarding with Lottie</i>
</td>
<td align="center">
<img src="screenshots/tutorial.png" width="250" alt="Tutorial"/>
<br><b>Interactive Tutorial</b>
<br><i>Learn app features step by step</i>
</td>
</tr>
</table>
</div>

### Main Application
<div align="center">
<table>
<tr>
<td align="center">
<img src="screenshots/home_screen.png" width="250" alt="Home Screen"/>
<br><b>Home Dashboard</b>
<br><i>Overview of all shopping locations</i>
</td>
<td align="center">
<img src="screenshots/grocery_list.png" width="250" alt="Grocery List"/>
<br><b>Smart Grocery Lists</b>
<br><i>Organized by categories with purchase tracking</i>
</td>
<td align="center">
<img src="screenshots/place_detail.png" width="250" alt="Place Detail"/>
<br><b>Store Details</b>
<br><i>Manage items for specific locations</i>
</td>
</tr>
</table>
</div>

### AI Features
<div align="center">
<table>
<tr>
<td align="center">
<img src="screenshots/recipe_screen.png" width="250" alt="Recipe Screen"/>
<br><b>AI Recipe Assistant</b>
<br><i>Get personalized recipe suggestions</i>
</td>
<td align="center">
<img src="screenshots/chat_bot.png" width="250" alt="Chat Bot"/>
<br><b>Smart Assistant</b>
<br><i>AI-powered shopping and cooking help</i>
</td>
<td align="center">
<img src="screenshots/analytics.png" width="250" alt="Analytics"/>
<br><b>Spending Analytics</b>
<br><i>Beautiful charts and insights</i>
</td>
</tr>
</table>
</div>

---

## 🚀 Installation

### Prerequisites
- **Android Studio** Arctic Fox or newer
- **Android SDK** API level 24 or higher
- **OpenAI API Key** (for AI features)

### Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone https://github.com/Fireeven/ProjectWork.git
   cd ProjectWork
   ```

2. **Configure API Keys**
   
   Create or edit `gradle.properties` file in the root directory:
   ```properties
   # OpenAI API Configuration
   OPENAI_API_KEY="your_openai_api_key_here"
   ```

3. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   - Wait for Gradle sync to complete

4. **Build and Run**
   ```bash
   ./gradlew clean build
   ./gradlew installDebug
   ```

### Getting OpenAI API Key

1. Visit [OpenAI Platform](https://platform.openai.com/)
2. Create an account or sign in
3. Navigate to API Keys section
4. Generate a new API key
5. Add the key to your `gradle.properties` file

---

## 🏗️ Architecture

### **MVVM + Clean Architecture**

```
📦 app/src/main/java/com/example/projectwork/
├── 🗄️ data/                    # Data Layer
│   ├── AppDatabase.kt          # Room database configuration
│   ├── entities/               # Database entities
│   │   ├── PlaceEntity.kt      # Shopping locations
│   │   ├── GroceryItem.kt      # Grocery items with purchase tracking
│   │   └── Category.kt         # Item categories
│   ├── dao/                    # Data Access Objects
│   │   ├── PlaceDao.kt         # Place operations
│   │   ├── GroceryItemDao.kt   # Item operations
│   │   └── CategoryDao.kt      # Category operations
│   └── Converters.kt           # Type converters for Room
├── 🎯 navigation/              # Navigation Layer
│   ├── NavGraph.kt             # Navigation graph
│   └── Screen.kt               # Screen definitions
├── 🖥️ screens/                 # Presentation Layer
│   ├── WelcomeScreen.kt        # Onboarding experience
│   ├── HomeScreen.kt           # Main dashboard
│   ├── GroceryListScreen.kt    # Shopping lists
│   ├── RecipeScreen.kt         # AI recipe suggestions
│   ├── AnalyticsScreen.kt      # Spending analytics
│   ├── PlaceDetailScreen.kt    # Store management
│   └── SettingsScreen.kt       # App configuration
├── 🎨 ui/                      # UI Components
│   ├── components/             # Reusable components
│   └── theme/                  # Material 3 theming
├── 🔧 utils/                   # Utilities
│   ├── OpenAIHelper.kt         # AI integration
│   └── RecipeParser.kt         # Recipe data processing
└── 🧠 viewmodel/               # Business Logic
    ├── HomeViewModel.kt        # Home screen logic
    ├── GroceryListViewModel.kt # List management
    ├── RecipeViewModel.kt      # Recipe handling
    └── AnalyticsViewModel.kt   # Analytics processing
```

### **Key Design Patterns**

- **🏛️ MVVM**: Clear separation between UI and business logic
- **🔄 Repository Pattern**: Centralized data access
- **💉 Dependency Injection**: Loose coupling between components
- **🎯 Single Responsibility**: Each class has one clear purpose
- **📱 Reactive Programming**: Flow-based data streams

---

## 🤖 AI Integration

### **OpenAI GPT Integration**

The app leverages OpenAI's GPT models for intelligent features:

#### **Recipe Assistant**
```kotlin
// Example: Get recipe suggestions based on available ingredients
val ingredients = listOf("chicken", "rice", "vegetables")
val recipes = OpenAIHelper.getRecipeSuggestions(ingredients)
```

#### **Smart Shopping Assistant**
```kotlin
// Example: Get shopping advice from AI chatbot
val response = OpenAIHelper.getChatbotResponse(
    userMessage = "What should I cook for dinner?",
    existingIngredients = userIngredients
)
```

#### **Price Estimation**
```kotlin
// Example: Estimate grocery prices
val estimatedPrice = OpenAIHelper.estimatePrice(
    items = groceryList,
    location = "supermarket"
)
```

### **AI Features Breakdown**

| Feature | Description | Implementation |
|---------|-------------|----------------|
| 🍳 **Recipe Suggestions** | Personalized recipes based on available ingredients | `RecipeViewModel` + `OpenAIHelper` |
| 💬 **Chat Assistant** | Conversational AI for cooking and shopping help | `ChatbotResponse` + conversation history |
| 💰 **Price Prediction** | Smart price estimation for budget planning | Machine learning models via OpenAI |
| 🥗 **Nutrition Analysis** | Nutritional information for recipes and ingredients | Structured data parsing |

---

## 📊 Database Schema

### **Room Database (Version 8)**

```sql
-- Places Table
CREATE TABLE places (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    address TEXT,
    categoryId INTEGER,
    FOREIGN KEY(categoryId) REFERENCES categories(id)
);

-- Grocery Items Table
CREATE TABLE grocery_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    quantity INTEGER DEFAULT 1,
    isChecked BOOLEAN DEFAULT FALSE,
    placeId INTEGER NOT NULL,
    price REAL DEFAULT 0.0,
    recipeId TEXT,
    recipeTitle TEXT,
    isPurchased BOOLEAN DEFAULT FALSE,
    purchaseDate INTEGER,
    actualPrice REAL,
    FOREIGN KEY(placeId) REFERENCES places(id) ON DELETE CASCADE
);

-- Categories Table
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE
);
```

### **Migration Strategy**
- ✅ **Automatic migrations** for seamless updates
- 🔄 **Fallback to destructive migration** for major schema changes
- 📝 **Migration scripts** for data preservation

---

## 🎨 UI/UX Design

### **Material 3 Design System**

- **🎨 Dynamic Color**: Adapts to user's wallpaper and preferences
- **🌙 Dark Theme**: Full dark mode support with proper contrast
- **📐 Typography**: Modern type scale with excellent readability
- **🎭 Motion**: Meaningful animations that guide user attention

### **Component Library**

| Component | Purpose | Features |
|-----------|---------|----------|
| `GroceryItemRow` | Display grocery items | Swipe actions, quantity controls |
| `PlaceCard` | Show shopping locations | Item count, category badges |
| `AnalyticsChart` | Data visualization | Interactive charts, animations |
| `RecipeCard` | Recipe display | Ingredient lists, difficulty ratings |

---

## 🔧 Technical Stack

### **Core Technologies**
- **🏗️ Kotlin**: Modern, concise, and safe programming language
- **🎨 Jetpack Compose**: Declarative UI toolkit for native Android
- **🗄️ Room Database**: Robust local data persistence
- **🧭 Navigation Compose**: Type-safe navigation between screens

### **Dependencies**

```gradle
dependencies {
    // Compose BOM - ensures compatible versions
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    
    // Core Compose
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    
    // Architecture Components
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    implementation 'androidx.navigation:navigation-compose:2.7.6'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.6.1'
    implementation 'androidx.room:room-ktx:2.6.1'
    kapt 'androidx.room:room-compiler:2.6.1'
    
    // Animations
    implementation 'com.airbnb.android:lottie-compose:6.3.0'
    
    // AI Integration
    implementation 'com.aallam.openai:openai-client:3.6.3'
    implementation 'io.ktor:ktor-client-android:2.3.7'
    
    // Charts and Analytics
    implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
}
```

---

## 📈 Performance Optimizations

### **Database Optimizations**
- **📊 Indexed queries** for fast data retrieval
- **🔄 Lazy loading** for large datasets
- **💾 Efficient caching** with Room's built-in mechanisms

### **UI Performance**
- **🎯 Compose optimization** with remember and derivedStateOf
- **🖼️ Image loading** with Coil for efficient memory usage
- **⚡ Lazy layouts** for smooth scrolling with large lists

### **Network Efficiency**
- **🔄 Request caching** for OpenAI API calls
- **⏱️ Timeout handling** for better user experience
- **🔁 Retry mechanisms** for failed network requests

---

## 🧪 Testing

### **Testing Strategy**
```
📊 Test Coverage: 85%+
├── 🧪 Unit Tests (70%)
│   ├── ViewModels
│   ├── Repositories
│   └── Utilities
├── 🔗 Integration Tests (20%)
│   ├── Database operations
│   └── API interactions
└── 🎭 UI Tests (10%)
    ├── Navigation flows
    └── User interactions
```

### **Running Tests**
```bash
# Run all tests
./gradlew test

# Run specific test suite
./gradlew testDebugUnitTest

# Generate coverage report
./gradlew jacocoTestReport
```

---

## 🚀 Deployment

### **Build Variants**
- **🐛 Debug**: Development build with logging and debugging tools
- **🚀 Release**: Optimized production build with ProGuard/R8

### **CI/CD Pipeline**
```yaml
# GitHub Actions workflow
name: Android CI
on: [push, pull_request]
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
      - name: Run tests
        run: ./gradlew test
      - name: Build APK
        run: ./gradlew assembleDebug
```

---

## 🤝 Contributing

We welcome contributions! Here's how you can help:

### **Development Setup**
1. **Fork** the repository
2. **Create** a feature branch: `git checkout -b feature/amazing-feature`
3. **Commit** your changes: `git commit -m 'Add amazing feature'`
4. **Push** to the branch: `git push origin feature/amazing-feature`
5. **Open** a Pull Request

### **Contribution Guidelines**
- 📝 **Code Style**: Follow Kotlin coding conventions
- 🧪 **Testing**: Add tests for new features
- 📚 **Documentation**: Update README and code comments
- 🎯 **Focus**: Keep PRs focused on single features

### **Areas for Contribution**
- 🌐 **Internationalization**: Add support for more languages
- 🎨 **UI/UX**: Improve design and user experience
- 🤖 **AI Features**: Enhance AI capabilities and accuracy
- 📊 **Analytics**: Add more insights and visualizations
- 🔧 **Performance**: Optimize app performance and memory usage

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```
MIT License

Copyright (c) 2025 Anton Maksimov(Fireeven)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🙏 Acknowledgments

### **Technologies & Libraries**
- 🤖 **[OpenAI](https://openai.com/)** - For providing powerful AI capabilities
- 🎨 **[Material Design](https://material.io/)** - For beautiful design guidelines
- 🎭 **[Lottie](https://airbnb.design/lottie/)** - For amazing animations
- 🏗️ **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - For modern UI development

### **Inspiration**
- 📱 Modern grocery shopping apps for UX inspiration
- 🤖 AI-powered assistants for intelligent features
- 📊 Analytics dashboards for data visualization ideas

### **Community**
- 👥 **Android Developer Community** for continuous learning
- 🐙 **GitHub Contributors** for valuable feedback and improvements
- 📚 **Stack Overflow** for problem-solving support

---

<div align="center">

### 🌟 **Star this repository if you found it helpful!** 🌟

**Made with ❤️ by the Smart Grocery Manager Team**

[⬆ Back to Top](#-smart-grocery-manager)

</div> 
