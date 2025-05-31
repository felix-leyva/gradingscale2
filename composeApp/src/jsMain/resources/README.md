# Firebase Configuration for JS/Web Platform

## Setup Instructions

1. **Copy the template file:**
   ```bash
   cp firebase-config.js.template firebase-config.js
   ```

2. **Configure your Firebase project:**
   - Open `firebase-config.js` 
   - Replace the placeholder values with your actual Firebase project configuration:
     - `YOUR_API_KEY_HERE` → Your Firebase API key
     - `YOUR_PROJECT_ID` → Your Firebase project ID
     - `YOUR_MESSAGING_SENDER_ID` → Your messaging sender ID
     - `YOUR_APP_ID` → Your web app ID

3. **Get Firebase configuration:**
   - Go to your [Firebase Console](https://console.firebase.google.com/)
   - Select your project
   - Go to Project Settings → General
   - Scroll down to "Your apps" section
   - Select your web app or create a new one
   - Copy the configuration values from the Firebase SDK snippet

## Important Security Notes

- **NEVER commit `firebase-config.js` to the repository!**
- The `firebase-config.js` file is automatically ignored by `.gitignore`
- Always use the template file for sharing with other developers
- Keep your Firebase API keys secure and rotate them regularly

## File Structure

```
jsMain/resources/
├── index.html              # Main HTML file (loads firebase-config.js)
├── firebase-config.js      # Your Firebase config (gitignored)
├── firebase-config.js.template  # Template for other developers
└── README.md              # This file
```

## Development Workflow

1. Each developer should copy the template and configure their own Firebase project
2. For production builds, use environment variables or build-time configuration
3. Consider using different Firebase projects for development, staging, and production