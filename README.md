# LocationTracker
A simple app that tracks user location every minute.

Application uses Sticky Foreground service to have more control over periodic interval unlike WorkManager which limits to 15 minutes.
The foreground service will persist even if the application is closed.
