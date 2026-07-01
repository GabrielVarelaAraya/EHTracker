# E&H Tracker

A minimalist habit and expense tracker Android app built with Jetpack Compose, MVVM architecture, and Room database.

## Features

### Habits
- Create and track daily habits with custom icons
- Weekly streak grid (7-day view) with tap-to-complete
- Set weekly targets (1-7 days/week)
- Completion rate tracking per habit
- Edit habit name, icon, and target days

### Income & Expenses
- Log expenses with 7 categories (Food, Transport, Shopping, Bills, Health, Fun, Other)
- Track income separately
- Custom date picker for backdating entries
- Edit/delete expenses and income
- Search expenses by note or category

### Analytics
- Date range filter (Week / Month / Quarter)
- Spending trend line chart
- Category breakdown with horizontal bars
- Habit completion ring and weekly summary
- Dynamic AI insights

### Balance Dashboard
- Income vs Expenses visualization with progress bar
- Real-time balance calculation
- Currency selector (17 currencies: USD, EUR, CRC, MXN, GBP, JPY, BRL, COP, PEN, ARS, CLP, GTQ, HNL, NIO, PAB, DOP, CUP)

### UX Polish
- Shimmer loading states on all screens
- Dark / Light mode (monochromatic palette with muted green accent)
- Delete confirmation dialogs
- Empty state guidance messages
- Habit reminder notifications

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Database | Room (KSP) |
| Navigation | Navigation Compose |
| State | Kotlin Flow / Coroutines |
| DI | Manual (Application-level singletons) |

## Requirements

- Android Studio Ladybug (AGP 9.2.1)
- Kotlin 2.2.10
- Min SDK 24 (Android 7.0)
- Target SDK 36

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/EHTracker.git
   ```

2. Open in Android Studio and sync Gradle.

3. Run on an emulator or device (API 24+).

## Database Schema

| Table | Description |
|-------|-------------|
| `habits` | Habit definitions (name, icon, target days) |
| `habit_completions` | Per-habit completion dates |
| `expenses` | Expense entries (amount, category, note, date) |
| `incomes` | Income entries (amount, note, date) |
| `balance` | User-set total savings |
| `currency` | Selected display currency |
| `budgets` | Monthly spending limits per category |

## Project Structure

```
app/src/main/java/com/example/ehtracker/
├── EHTrackerApplication.kt    # Room DB + Repository singletons
├── EHTrackerApp.kt            # NavHost + Scaffold + QuickAddSheet
├── MainActivity.kt            # Entry point
├── NotificationHelper.kt      # Habit reminder notifications
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt     # Room DB (v5)
│   │   ├── dao/               # 6 DAOs
│   │   └── entity/            # 7 entities
│   ├── model/                 # Domain models
│   └── repository/            # TrackerRepository
└── ui/
    ├── analytics/             # Analytics screen + ViewModel
    ├── components/            # Reusable composables
    ├── dashboard/             # Dashboard screen + ViewModel
    ├── logs/                  # History screen + ViewModel
    ├── navigation/            # Bottom nav bar
    └── theme/                 # Colors, Typography, Theme
```

## License

MIT
