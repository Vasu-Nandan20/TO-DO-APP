# TaskFlow AI 🚀

TaskFlow AI is a production-level Android productivity application that leverages the power of Google's Gemini AI to transform natural language input into structured, actionable tasks. Designed with a premium user experience and offline-first reliability in mind.

## ✨ Features

- **🤖 Magic Add (AI Integration)**: Create tasks using natural language. Just type "Meeting with team tomorrow at 5 PM" and let Gemini handle the rest.
- **⚡ Smart Caching**: AI responses are cached locally using Room to minimize API calls, reduce latency, and ensure a fast, "unlimited" feel.
- **🛡️ Offline Resilience**: Seamless fallback to rule-based parsing when the internet is unavailable or the API limit is reached.
- **📅 Advanced Calendar**: Interactive monthly and weekly views to manage your deadlines effectively.
- **📊 Productivity Analytics**: Track your progress with weekly completion charts and personal streaks.
- **🔔 Intelligent Reminders**: Exact-time reminders and daily summaries powered by WorkManager.
- **🎨 Premium UI/UX**: Built with Jetpack Compose and Material 3, featuring dark/light mode support and smooth animations.
- **🔒 Local-First Privacy**: Your data stays on your device, ensuring maximum privacy and speed.

## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture principles
- **UI Framework**: Jetpack Compose (Material 3)
- **Dependency Injection**: Dagger Hilt
- **Database**: Room (Offline storage & AI Cache)
- **Concurrency**: Coroutines + Flow
- **Background Tasks**: WorkManager
- **AI Engine**: Google Gemini 1.5 Flash
- **Navigation**: Jetpack Compose Navigation

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala or newer
- A Google Gemini API Key from [Google AI Studio](https://aistudio.google.com/app/apikey)

### Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/TaskFlowAI.git
   ```
2. Open the project in Android Studio.
3. Open `local.properties` in the root directory.
4. Add your API key:
   ```properties
   GEMINI_API_KEY=your_actual_api_key_here
   ```
5. Build and run the app!

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Developed with ❤️ by [Your Name/Handle]
