# WasmJS Storage Module

This module provides a shared implementation for browser localStorage persistence in WasmJS and JS targets.

## Features

- **Unified Storage API**: Single interface for localStorage operations
- **Persistent KStore Factory**: Easy creation of persistent KStore instances
- **Storage Utilities**: Helper functions for storage management
- **Type-safe Serialization**: Built-in JSON serialization with error handling
- **Logging Support**: Optional logging for debugging storage operations

## Usage

### Creating a Persistent Store

```kotlin
import de.felixlf.gradingscale2.storage.PersistentStoreFactory

val myStore = PersistentStoreFactory.create(
    default = MyData(),
    serializer = MyData.serializer(),
    storageKey = "my_data_key"
)
```

### Storage Configuration

```kotlin
import de.felixlf.gradingscale2.storage.StorageConfig

val myStore = PersistentStoreFactory.create(
    default = MyData(),
    serializer = MyData.serializer(),
    storageKey = "my_data_key",
    config = StorageConfig(
        enableLogging = true,      // Enable/disable console logging
        clearOnError = true,       // Clear corrupted data on error
        json = customJson          // Custom JSON configuration
    )
)
```

### Storage Utilities

```kotlin
import de.felixlf.gradingscale2.storage.StorageUtils

// Get storage information
val storageInfo = StorageUtils.getStorageInfo()
StorageUtils.printStorageInfo()

// Clear specific keys
StorageUtils.clearKeys("key1", "key2")

// Clear all app storage
StorageUtils.clearAllAppStorage()

// Export/Import data
val backup = StorageUtils.exportAllData()
StorageUtils.importData(backup)
```

## Storage Keys

Pre-defined storage keys are available in `StorageUtils.Keys`:
- `GRADE_SCALES`: Grade scales data
- `WEIGHTED_GRADES`: Weighted grades data
- `PREFERENCES`: Application preferences

## Platform Support

- **WasmJS**: ✅ Full localStorage support
- **JS**: ✅ Full localStorage support
- **Android/iOS/JVM**: ❌ Throws UnsupportedOperationException

## Error Handling

The storage codec automatically handles:
- Corrupted data (clears and returns default)
- Serialization errors (logs and returns null)
- Missing data (returns null for default handling)

## Logging

Console output uses emoji indicators:
- ✅ Success operations
- ❌ Error conditions
- 🗑️ Deletion operations
- ℹ️ Information messages
- 🔧 Recovery operations
- 📊 Statistics