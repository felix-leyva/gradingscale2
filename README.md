# GradingScale2

A multi-platform grading scale calculator built with Kotlin Multiplatform and Compose Multiplatform. GradingScale2 handles non-linear grading systems with precision, allowing educators and students to define custom grading scales and perform exact calculations across all major platforms.

## 🎯 Features

### Core Functionality
- **Non-Linear Grade Calculations**: Support for complex, non-linear grading scales that accurately reflect various educational systems
- **Custom Grade Scale Management**: Create, edit, and manage multiple grading scales with different point systems and grade boundaries
- **Weighted Grade Calculator**: Calculate weighted averages for courses with different component weights (assignments, exams, projects)
- **Import Grade Scales**: Import pre-defined grading scales from various educational systems
- **Real-time Calculations**: Instant grade calculations as you input scores
- **Multi-Scale Support**: Switch between different grading scales seamlessly

### Platform-Specific Features
- **Adaptive UI**: Responsive design that adapts to phones, tablets, and desktop screens
- **Offline-First**: All data stored locally for fast, reliable access
- **Material 3 Design**: Modern UI following the latest Material Design guidelines

## 🏗️ Architecture

### Clean Architecture Implementation

The project follows Clean Architecture principles with clear separation of concerns across three main layers:

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│               (composeApp module)                       │
│    • Compose UI Screens                                 │
│    • ViewModels with Molecule State Management          │
│    • Platform-specific UI implementations               │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                     Domain Layer                        │
│                  (entities module)                      │
│    • Use Cases (Business Logic)                         │
│    • Repository Interfaces                              │
│    • Domain Models                                      │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────┴────────────────────────────────┐
│                      Data Layer                         │
│               (data/* submodules)                       │
│    • Repository Implementations                         │
│    • Local Database (SQLDelight)                        │
│    • Remote API (Ktor)                                  │
│    • Preferences (Multiplatform Settings)               │
└─────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

#### 🎭 Molecule for Reactive State Management
The project uses [CashApp's Molecule](https://github.com/cashapp/molecule) for state management, revolutionizing how UI state is handled by treating it as a Composable function:

```kotlin
interface UIModel<UIState, UICommand> {
    val scope: UIModelScope

    val uiState: StateFlow<UIState>

    @Composable
    fun produceUI(): UIState

    fun sendCommand(command: UICommand)
}
```
**Benefits of Molecule:**
- **Reactive by Design**: UI state recomposes automatically when dependencies change
- **Testable**: State logic can be tested without Android framework dependencies
- **Composable Logic**: Leverage Compose's powerful state management primitives
- **Clear Data Flow**: Unidirectional data flow with events and state


It also separates the UIModel, from the Android Framework Specific UIModel, allowing easier testing and use in different platforms even without the Navigation/Jetpack ComposeUI. 

In case you require to link your UIModels to the Android or Jetpack ComposeUI Navigation/Lifecycle, you can then use a ViewModel and tie its scope into the ViewModel. The functionallity can be easily added via interface implementation with the `by` class delegation.

```kotlin
class CalculatorViewModel(
    calculatorUIModel: CalculatorUIModel,
) : ViewModel(calculatorUIModel.scope),
UIModel<GradeScaleCalculatorUIState, CalculatorUIEvent> by calculatorUIModel

```
This reduces boilerplate and allows to keep this logic out of the platform `:composeApp` module.


#### 🏛️ Adaptive Layout with Persistent Scaffolds

The adaptive layout system uses a unique approach with single per-destination Scaffolds that maintain navigation state across all screens:

```kotlin
@Composable
fun AnimatedContentScope.PersistentScaffold(
    navigationRail: @Composable ScaffoldState.() -> Unit = { DefaultNavigationRail() },
    bottomBar: @Composable ScaffoldState.() -> Unit = { DefaultNavigationBar() },
    content: @Composable ScaffoldState.(PaddingValues) -> Unit,
) {
    val windowSizeClass = calculateWindowSizeClass()
    
    // Automatically adapts between NavigationRail (tablet/desktop) 
    // and BottomNavigation (mobile)
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Mobile: Bottom navigation
            BottomNavigationScaffold(bottomBar, content)
        }
        else -> {
            // Tablet/Desktop: Navigation rail
            NavigationRailScaffold(navigationRail, content)
        }
    }
}
```

**Key Features:**
- **Shared Element Transitions**: Smooth animations between screens using `SharedTransitionScope`
- **State Persistence**: Navigation components maintain their state across screen changes
- **Adaptive Components**: Automatically switches UI components based on screen size
- **Centralized State**: `ScaffoldState` acts as a container for both `AnimatedVisibilityScope` and `SharedTransitionScope`

#### 🔄 Immutability with PersistentList

The project embraces functional programming principles by using immutable data structures throughout:

```kotlin
@Serializable
data class GradeScale(
    val id: String,
    val gradeScaleName: String,
    val totalPoints: Double,
    @Serializable(with = PersistentListSerializer::class)
    val grades: PersistentList<Grade>,
) {
    @Transient
    val sortedGrades = grades.sortedByDescending { it.percentage }.toImmutableList()
}
```

**Why PersistentList?**
- **Thread Safety**: Immutable collections are inherently thread-safe
- **Performance**: Structural sharing reduces memory overhead when creating modified copies
- **Predictability**: Data cannot be accidentally mutated, reducing bugs
- **Compose Integration**: Works seamlessly with Compose's recomposition system
- **Custom Serialization**: Handles WasmJS compatibility issues with a custom serializer

#### 🌊 Reactive Everything with Kotlin Flows

The architecture is fully reactive using Kotlin Coroutines Flow:

```kotlin
interface GradeScaleRepository {
    fun getGradeScaleById(id: String): SharedFlow<GradeScale?>
    fun getGradeScales(): SharedFlow<ImmutableList<GradeScale>>
}

class GradeScaleRepositoryImpl : GradeScaleRepository {
    override fun getGradeScales(): SharedFlow<ImmutableList<GradeScale>> =
        gradeScaleDao.getGradeScales()
            .map { list -> list.toImmutableList() }
            .shareIn(
                scope = scope,
                started = SharingStarted.Lazily,
                replay = 1,
            )
}
```

**Reactive Benefits:**
- **Real-time Updates**: UI automatically updates when data changes
- **Efficient Resource Usage**: `SharingStarted.Lazily` only activates flows when collected
- **Backpressure Handling**: Flow operators handle data stream pressure automatically
- **Cancellation Support**: Proper lifecycle management with structured concurrency

#### ⚡ Functional Error Handling with Arrow

The project uses [Arrow](https://arrow-kt.io/) for functional error handling, avoiding exceptions in favor of explicit error types:

```kotlin
// Using Option for nullable results
class InsertGradeScaleUseCaseImpl : InsertGradeScaleUseCase {
    override suspend operator fun invoke(...): Option<String> = option {
        val currentScales = gradeScaleRepository.getGradeScales().firstOrNull()
        // Arrow's bind() for monadic composition
        gradeScaleRepository.upsertGradeScale(initialGradeScale).bind()
    }
}

// Using Either for operations that can fail
interface RemoteSyncRepository {
    suspend fun countriesAndGrades(): Either<RemoteError, List<CountryGradingScales>>
}

// Clean error handling in UI
when (val result = getRemoteGradeScales()) {
    is Either.Right -> updateUI(result.value)
    is Either.Left -> showError(result.value)
}
```

**Why Arrow?**
- **Type-Safe Errors**: Compile-time guarantee of error handling
- **Composable**: Chain operations without nested try-catch blocks
- **Explicit**: Makes error cases visible in function signatures
- **Functional**: Leverages monadic composition for clean code

#### 🧩 Gradle Convention Plugins

The build system uses a convention plugin approach:

```kotlin
// buildSrc/src/main/kotlin/gs-android-app.gradle.kts
plugins {
    id("com.android.application")
    id("kotlin-android")
    // Common configurations
}

android {
    // Standardized Android app configuration
}

// Applied to apps as:
plugins {
    id("gs-android-app")
}
```

This approach provides:
- **Consistency**: All modules follow the same configuration patterns
- **Maintainability**: Update configurations in one place
- **Type Safety**: Kotlin DSL provides IDE support and compile-time checking
- **Modularity**: Different plugins for different module types

#### 💉 Dependency Injection with Koin

The project uses Koin with a modular, platform-aware structure:

```kotlin
// Feature module
val calculatorModule = module {
    factory { CalculatorViewModel(get(), get()) }
    factory { CalculatorUIModel(get(), get()) }
}

// Platform-specific module
expect val platformModule: Module

// Initialization
fun initKoin() = startKoin {
    modules(
        calculatorModule,
        gradeScaleModule,
        platformModule, // Platform-specific implementations
        dataModule,
    )
}
```

### Module Structure

```
GradingScale2/
├── composeApp/          # UI Layer - Compose Multiplatform app
│   ├── commonMain/      # Shared UI code
│   ├── androidMain/     # Android-specific UI
│   ├── iosMain/         # iOS-specific UI
│   ├── jsMain/          # Web-specific UI
│   └── jvmMain/         # Desktop-specific UI
│
├── entities/            # Domain Layer
│   ├── models/          # Domain models
│   ├── repositories/    # Repository interfaces
│   ├── usecases/        # Business logic
│   └── uimodel/         # UI state models
│
└── data/                # Data Layer
    ├── network/         # Ktor HTTP client
    ├── authFirebase/    # Firebase authentication
    └── persistance/
        ├── db/          # SQLDelight database
        └── sharedprefs/ # Multiplatform Settings
```

## 🛠️ Tech Stack

### Core Technologies
- **[Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)** (2.1.21) - Share code across platforms
- **[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/)** (1.8.1) - Modern declarative UI framework
- **[Material 3 Adaptive](https://m3.material.io/)** - Adaptive design system

### State Management & UI
- **[CashApp Molecule](https://github.com/cashapp/molecule)** - Compose-based state management
- **[Navigation Compose](https://developer.android.com/jetpack/compose/navigation)** - Type-safe navigation

### Networking & Data
- **[Ktor](https://ktor.io/)** - Multiplatform HTTP client
- **[SQLDelight](https://github.com/cashapp/sqldelight)** - Type-safe SQL database
- **[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)** - JSON parsing
- **[Multiplatform Settings](https://github.com/russhwolf/multiplatform-settings)** - Key-value storage

### Dependency Injection
- **[Koin](https://insert-koin.io/)** - Pragmatic lightweight DI framework

### Platform Integration
- **[Firebase](https://firebase.google.com/)** - Authentication & Analytics
  - Android/iOS: GitLive Firebase SDK
  - Web: Firebase JS SDK via CDN
  - Desktop: GitLive Firebase SDK
- **[Conveyor](https://conveyor.hydraulic.dev/)** - Desktop app distribution

### Code Quality
- **[Ktlint](https://pinterest.github.io/ktlint/)** - Kotlin code style enforcement
- **[Arrow](https://arrow-kt.io/)** - Functional programming and error handling
- **Kotlin Coroutines** - Asynchronous programming
- **PersistentList** - Immutable collections from Kotlinx Collections

## 📱 Platform Support

| Platform | Status | Min Version | Notes |
|----------|--------|-------------|-------|
| Android  | ✅ | API 24 (7.0) | Full feature support |
| iOS      | ✅ | iOS 14.0 | Native SwiftUI integration |
| Desktop  | ✅ | JVM 17 | Windows, macOS, Linux |
| Web      | ✅ | Modern browsers | JS/WASM targets |

## 🚀 Getting Started

### Prerequisites
- JDK 17 or higher
- Android Studio (for Android development)
- Xcode 15+ (for iOS development)
- Node.js (for web development)

### Clone the Repository
```bash
git clone https://github.com/yourusername/GradingScale2.git
cd GradingScale2
```

### Build & Run

#### 🤖 Android
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Install on connected device
./gradlew :composeApp:installDebug
```

#### 🍎 iOS
```bash
# Build iOS framework
./build-ios.sh

# Open in Xcode
open iosApp/iosApp.xcodeproj

# Run from Xcode or use:
./gradlew :composeApp:iosSimulatorArm64Test
```

#### 🖥️ Desktop
```bash
# Run desktop application
./gradlew :composeApp:run

# Create distribution
./gradlew :composeApp:packageDistributionForCurrentOS
```

#### 🌐 Web
```bash
# Run development server
./gradlew :composeApp:wasmJsBrowserRun

# Build production bundle
./gradlew :composeApp:wasmJsBrowserProductionWebpack
```

### 🧪 Testing
```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :entities:test
./gradlew :data:network:test

# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

### 🧹 Clean & Rebuild
```bash
# Use the helper script
./clean-and-rebuild.sh

# Or manually
./gradlew clean
./gradlew build
```

## 🧪 Testing Strategy

The project includes comprehensive unit tests focusing on:
- **Use Cases**: Business logic validation
- **ViewModels**: UI state management with Molecule
- **Repositories**: Data layer operations
- **Platform-specific**: Platform-specific functionality

Example test structure:
```kotlin
class GradeScaleListViewModelTest {
    @Test
    fun `should update UI state when grade scale is selected`() = runTest {
        // Given
        val viewModel = GradeScaleListViewModel(...)
        
        // When
        viewModel.sendEvent(SelectGradeScale(gradeScaleId))
        
        // Then
        assertEquals(gradeScaleId, viewModel.uiState.value.selectedId)
    }
}
```

## 📦 Download

The desktop application is available for download on the [Download Section](https://felix-leyva.github.io/gradingscale2/download)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
