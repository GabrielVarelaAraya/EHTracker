# E&H Tracker

A minimalist habit and expense tracker Android app built with Jetpack Compose, Material 3, MVVM, and Room.

## Features

### Habits
- Create and track daily habits with custom icons
- Weekly streak grid with tap-to-complete and haptic feedback
- Set weekly targets (1-7 days/week) per habit
- Completion rate and streak tracking

### Income & Expenses
- Unified transaction list (expenses + income grouped by date with net daily subtotal)
- 7 expense categories (Food, Transport, Shopping, Bills, Health, Fun, Other)
- Custom date picker for backdating entries
- Search with debounced input (300ms)

### Analytics
- Swipeable range selector (Week / Month) with daily trend line chart
- Category breakdown with horizontal bars
- Habit completion ring and weekly summary
- AI-powered insights

### Dashboard
- Weekly scrollable habit grid with swipeable weeks
- Income vs Expenses progress bar
- Real-time balance with savings goal
- Pull-to-refresh on all screens
- FAB for quick-add (habits, expenses, income)

### Settings
- 8 color palettes (Green, Teal, Indigo, Rose, Purple, Coral, Slate, Amber)
- Theme mode: System / Light / Dark
- Currency selector (18 currencies)
- Notification time picker + toggle
- App version info

### UX
- Shimmer loading states
- Swipe-to-delete with confirmation dialog on all list items
- Haptic feedback on long-press delete
- Empty state guidance with emoji icons
- Configurable daily reminder notification
- HorizontalPager tab navigation (swipeable main screens)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Database | Room (KSP) with Flows |
| Navigation | Custom HorizontalPager |
| State | Kotlin Flow / StateFlow |
| DI | Manual (Application-level singletons) |

## Requirements

- Android Studio Ladybug (AGP 9.2.1)
- Kotlin 2.2.10
- Min SDK 24 (Android 7.0)
- Target SDK 36
- Compose BOM 2026.02.01

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/EHTracker.git
   ```

2. Open in Android Studio and sync Gradle.

3. Run on an emulator or device (API 24+).

## Database Schema (v8)

| Table | Description |
|-------|-------------|
| `habits` | Habit definitions (name, icon, target days) |
| `habit_completions` | Per-habit completion dates (composite index on habitId+date) |
| `expenses` | Expense entries (amount, category, note, date) |
| `incomes` | Income entries (amount, note, date) |
| `balance` | User-set total savings |
| `currency` | Selected display currency |
| `budgets` | Monthly spending limits per category |
| `preferences` | Theme, palette, notification settings |

## Project Structure

```
app/src/main/java/com/example/ehtracker/
├── EHTrackerApplication.kt    # Room DB + Repository singletons
├── EHTrackerApp.kt            # HorizontalPager + BottomNav + QuickAddSheet
├── MainActivity.kt            # Entry point (theme loading)
├── NotificationHelper.kt      # Habit reminder notifications
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt     # Room DB (v8, 3 migrations)
│   │   ├── dao/               # 7 DAOs
│   │   └── entity/            # 8 entities
│   ├── model/                 # Domain models + sealed Transaction class
│   └── repository/            # TrackerRepository
└── ui/
    ├── analytics/             # Analytics screen + ViewModel
    ├── components/            # Reusable composables (cards, charts, shimmer)
    ├── dashboard/             # Dashboard screen + ViewModel
    ├── logs/                  # History screen + ViewModel
    ├── navigation/            # Bottom nav bar
    ├── settings/              # Settings screen + ViewModel
    └── theme/                 # Colors (8 palettes), Typography, Theme
```

## License

MIT
