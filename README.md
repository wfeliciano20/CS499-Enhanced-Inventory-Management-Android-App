# Enhanced Inventory Management Android App

A modern inventory management Android application built with Java. 
This app streamlines inventory tracking, user management, and notifications,
leveraging local and cloud technologies. This is the third enhancement 
for my capstone project at SNHU.

# External Links to Previous Enhancements

- Web API Repository: https://github.com/wfeliciano20/InventoryMangementWebApi
- Web Client Repository: https://github.com/wfeliciano20/InventoryManagementReactWebClient
- Capstone Portfolio: https://wfeliciano20.github.io/CS499_Capstone/

## Features

- **User Management:** Stores user phone numbers locally using SQLite for quick access.
- **Centralized Inventory:** Connects to an Azure-hosted Web API for inventory management, user registration, and login.
- **Real-Time Stock Alerts:** When an item's stock reaches zero, the app sends an SMS reminder to the registered user (requires SMS permission).
- **Secure Authentication:** User registration and authentication are handled via the web API for centralized control.

## Tech Stack

- **Frontend:** Java (Android)
- **Local Database:** SQLite
- **Backend:** Azure-hosted Web API

## Permissions

- **SMS Permission:** Required to send stock alert reminders via SMS when inventory items run out.


## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) installed
- An emulator (or physical Android device)
- Git

### Clone the Repository

```bash
git clone https://github.com/wfeliciano20/CS499-Enhanced-Inventory-Management-Android-App.git
cd CS499-Enhanced-Inventory-Management-Android-App
```

### Open in Android Studio

1. Open Android Studio.
2. Click on **File > Open** and select the cloned project folder.
3. Allow Gradle to sync and dependencies to download.

### Running the App

1. Set up an Android emulator, or connect a physical device.
2. Build and run the application on the emulator/device.
3. On first run, grant SMS permissions when prompted.

### Usage

- **Register/Login:** Use the provided interface to register a new user or log in.
- **Manage Inventory:** Add, update, or remove inventory items.
- **Stock Alerts:** When any item’s stock is updated to zero, you’ll receive an SMS alert as a reminder.


