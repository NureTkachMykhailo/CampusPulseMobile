# CampusPulse Mobile

Офлайн Android-версія блогу [CampusPulse](https://github.com/NureTkachMykhailo/CampusPulse) — лабораторна робота №3 з дисципліни ВМПтФ. Kotlin, Jetpack Compose (Material 3), Navigation Compose, Room.

## Стек
- Kotlin, Jetpack Compose (Material 3)
- Navigation Compose
- Room (SQLite) через KSP
- SDK 35, minSdk 26

Демо-акаунти після seed: `redaktor@campuspulse.local` / `campus123` (суперкористувач) та `student@campuspulse.local` / `campus123` (звичайний користувач).

## Структура
```
app/src/main/java/com/mtkach/campuspulse/
  CampusPulseApplication.kt
  MainActivity.kt
  data/    # Entities, Daos, AppDatabase, SessionStore, ChronicleRepository, FeedQueryCache
  ui/
    CampusPulseApp.kt      # NavHost + перевірка прав
    screens/                # Login, Feed, Detail, ArticleForm, Categories
    components/             # AccountAvatar
    theme/
```

## Функціональність
- Вхід і ролі (автор / суперкористувач), права на редагування/видалення
- Стрічка з пошуком і фільтром за категорією
- Стаття з коментарями
- Форма створення/редагування статті зі stepper (Мета → Категорія → Текст → Огляд)
- CRUD категорій
- Кеш запитів стрічки (`FeedQueryCache`, TTL 30с)

## Запуск
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.mtkach.campuspulse/.MainActivity
```
