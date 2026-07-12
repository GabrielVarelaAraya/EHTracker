# E&H Tracker

A minimalist **habit and expense tracker** Android app built with Jetpack Compose, Material 3, MVVM, and Room.

## Features

### Habits
- Create habits with custom emoji icons
- **Numeric habits** (e.g. hours slept, cups of water, kg lost) — tap to open a bottom sheet and enter a custom value; shows current value inside the circle; defaults to `0.0`
- **Boolean habits** (completed / not completed) — tap to toggle
- Weekly streak grid with swipeable weeks (infinite horizontal pager)
- Set weekly targets (1–7 days/week) per habit
- Completion rate and streak tracking
- Edit existing habits (name, icon, target, numeric toggle, unit)

### Income & Expenses
- Unified transaction list (expenses + income grouped by date with net daily subtotal)
- 7 expense categories: Food, Transport, Shopping, Bills, Health, Fun, Other
- Custom date picker for backdating entries
- Search with debounced input (300ms)
- Swipe-to-delete with confirmation dialog
- Edit existing transactions

### Analytics
- Swipeable range selector (Week / Month) with daily trend line chart
- Category breakdown with horizontal bars
- Habit completion ring and weekly summary
- Income vs Expenses overview
- AI-powered insights

### Dashboard
- Weekly scrollable habit grid with swipeable weeks
- Income vs Expenses progress bar
- Real-time balance with savings goal
- Pull-to-refresh on all screens
- Calendar month grid with daily completion rate and net amount
- Edit savings and currency from dashboard

### Settings
- 8 color palettes: Green, Teal, Indigo, Rose, Purple, Coral, Slate, Amber
- Theme mode: System / Light / Dark
- Currency selector (18 currencies)
- Notification time picker + toggle for daily habit reminders
- App version info

### Navigation
- 4-tab bottom navigation bar: Home, Insights, History, **Add**
- The **Add** button opens a quick-add bottom sheet with tabs for Expense, Income, and Habit
- HorizontalPager for swipeable main screens
- Settings accessible from dashboard header

### UX
- Shimmer loading states
- Haptic feedback on long-press interactions
- Empty state guidance with emoji icons
- Configurable daily reminder notification
- Snackbar messages for actions and errors

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM |
| Database | Room (KSP) with Flows |
| Navigation | Custom HorizontalPager + ModalBottomSheet |
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

## Database Schema (v9)

| Table | Description |
|-------|-------------|
| `habits` | Habit definitions (name, icon, target days, is_numeric, unit) |
| `habit_completions` | Per-habit completion dates + optional numeric value |
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
│   │   ├── AppDatabase.kt     # Room DB (v9, 4 migrations)
│   │   ├── dao/               # 7 DAOs
│   │   └── entity/            # 8 entities
│   ├── model/                 # Domain models + sealed Transaction class
│   └── repository/            # TrackerRepository
└── ui/
    ├── analytics/             # Analytics screen + ViewModel
    ├── calendar/              # Calendar month grid + ViewModel
    ├── components/            # Reusable composables (cards, charts, shimmer, habit row)
    ├── dashboard/             # Dashboard screen + ViewModel
    ├── logs/                  # History screen + ViewModel
    ├── navigation/            # Bottom nav bar
    ├── settings/              # Settings screen + ViewModel
    └── theme/                 # Colors (8 palettes), Typography, Theme
```

## License

MIT
