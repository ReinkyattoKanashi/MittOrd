# MittOrd — ревью читаемости кода

Поверхностное ревью (структура + основные экраны), дата: 2026-08-18, ветка `dev`.
Цель — понять, **в какую сторону улучшать читаемость**, а не список багов.

Всего ~5000 строк Kotlin в 39 файлах. Топ по объёму:

| Файл | Строк |
|---|---|
| `ui/screens/home/FloatingBottomNavigation.kt` | 679 |
| `ui/screens/wordDetail/WordDetailScreen.kt` | 473 |
| `ui/screens/settings/SettingsScreen.kt` | 373 |
| `ui/screens/wordDetail/WordDetailViewModel.kt` | 315 |
| `ui/screens/home/HomeScreen.kt` | 284 |
| `ui/screens/home/HomeViewModel.kt` | 223 |

---

## 0. Что уже хорошо (не ломать)

Чтобы было понятно, от чего отталкиваться:

- **Слои разделены**: `data/` (сеть) — `domain/` (usecase, модели) — `database/` (Room) — `ui/`. DI через Hilt аккуратный, модули маленькие.
- **Context не течёт в ViewModel** — вынесен в `@Singleton`-обёртки (`AppPreferences`, `AvatarRepository`, `WordImageRepository`). Это правильное решение, его надо держать.
- **Одноразовые события через `SharedFlow`** (`HomeEvent`, `WordDetailEvent`) вместо флажков в state — грамотно.
- **UseCase'ы тонкие**, по одной операции, читаются мгновенно.
- **Строки почти везде в ресурсах** (`stringResource`), не хардкод.
- **`util/Modifier.kt`** (lambda-based `height {}`, `size {}`, `paddingLayout {}`) — это осознанная оптимизация анимаций на layout-фазе. Код нетривиальный, но по делу; его надо не убирать, а **задокументировать**.
- Repository — интерфейс + Impl, подмена в тестах возможна.

Проблема не в архитектуре. Проблема в том, что **UI-слой не декомпозирован, и в нём живёт логика**.

---

## 1. Главная проблема: God-Composable

### 1.1 `FloatingBottomNavigation.kt` — 679 строк

Один файл содержит одновременно:

1. Анимационную математику (6 подряд `animateDpAsState`/`animateFloatAsState`)
2. Лейаут плавающей панели
3. **Целую форму добавления слова** (`AddWordContent`)
4. Поле поиска (`StaticSearchField`)
5. **Три bottom sheet'а выбора языка**
6. Layout-хаки-измерители (`MeasureAvailableWidth`, `MeasureAvailableHeight`)
7. Три `@Preview` + фабрику preview-данных

Чтобы понять «как работает добавление слова», нужно прокрутить файл про навбар. Это главный источник ощущения «кошмара».

### 1.2 `WordDetailScreen.kt` — одна функция на ~400 строк

Тело `WordDetailScreen` линейно содержит: поле слова → список переводов → кнопку «добавить перевод» → комментарий → фото → кнопку сохранить → **4 диалога/шита** подряд. Ни одна секция не выделена в функцию.

### 1.3 Симптом-маркер: «прелюдия из stringResource»

В начале `WordDetailScreen` — **25 строк подряд** вида:

```kotlin
val cdBack = stringResource(R.string.cd_back)
val cdDelete = stringResource(R.string.cd_delete)
val screenTitle = stringResource(R.string.screen_edit_word)
val placeholderWord = stringResource(R.string.placeholder_word)
// ... ещё 21 строка
```

То же в `AddWordContent` (7 штук). Это **не причина, а индикатор**: строки поднимают наверх ровно потому, что функция слишком большая и их использование разбросано. Если разбить на компоненты — каждая строка вызывается там, где нужна, и блок исчезает сам.

**Куда двигаться:** разбить оба экрана по вертикальным секциям UI. Ориентир — ни одна `@Composable` длиннее ~60 строк.

```
wordDetail/
  WordDetailScreen.kt        // Scaffold + сборка секций, ~80 строк
  components/
    WordField.kt
    TranslationList.kt       // + AddTranslationButton
    CommentField.kt
    WordPhotoSection.kt
    WordDetailDialogs.kt     // discard + delete
home/
  FloatingBottomNavigation.kt  // только навбар + анимации
  addword/AddWordPanel.kt
  search/NavSearchField.kt
```

---

## 2. Дублирование: одна и та же логика написана 8 раз

### 2.1 Автоопределение языка

Блок «отмени job → обрежь → если длина ≥ N → запусти детект с debounce» повторяется:

- `HomeViewModel`: `onWordChange`, `onTranslationChange`, `onWordLanguageSelected`, `onTranslationLanguageSelected`, `setExternalWord` — **5 раз**
- `WordDetailViewModel`: `onWordChange`, `onWordLanguageSelected`, `onTranslationChange`, `onTranslationLanguageSelected` — **4 раза**

Плюс версии в `WordDetail` ещё и вручную копируют список переводов туда-обратно:

```kotlin
val current = _state.value.translations.toMutableList()
if (index in current.indices) {
    current[index] = current[index].copy(languageCode = code)
    _state.value = _state.value.copy(translations = current)
}
```

— этот паттерн встречается в файле **6 раз**.

**Куда двигаться:**

а) Вынести детект в один компонент (например `LanguageDetector` / `AutoDetectField`), который сам держит `Job`, debounce и правило минимальной длины:

```kotlin
class LanguageDetectionController(
    private val scope: CoroutineScope,
    private val detect: DetectLanguageUseCase
) {
    private var job: Job? = null
    fun request(text: String, onResult: (String?) -> Unit) { /* debounce + cancel */ }
    fun cancel() { job?.cancel() }
}
```

б) Для списка переводов сделать хелпер, чтобы мутация индекса была одной строкой:

```kotlin
private fun updateTranslation(index: Int, transform: (TranslationEntry) -> TranslationEntry) {
    _state.update { s ->
        if (index !in s.translations.indices) s
        else s.copy(translations = s.translations.toMutableList().also { it[index] = transform(it[index]) })
    }
}
```

После этого `onTranslationChange` схлопнется в 4–5 строк.

### 2.2 Резолвинг кода языка в UI

Копипаста, встречающаяся **6 раз** (3 в `FloatingBottomNavigation`, 3 в `WordDetailScreen`):

```kotlin
val code = language?.code?.takeIf { it.isNotEmpty() }
    ?: language?.let { LANG_NAME_TO_BCP47[it.name] }
```

И **в вариантах «перевести» она другая** — с хвостом `?: language.name`:

```kotlin
val code = language.code.takeIf { it.isNotEmpty() }
    ?: LANG_NAME_TO_BCP47[language.name]
    ?: language.name
```

Расхождение поведения спрятано в копипасте — такое замечаешь только при построчном сравнении. Это уже почти баг.

**Куда двигаться:** `fun Language.bcp47(): String` в `domain/`, и UI вызывает только её. Заодно — обёртка `LanguagePickerSheet` не должна дублироваться трижды на экран: сделать один вызов, который управляется одним `sealed`-состоянием.

### 2.3 Три вызова пикера → одно состояние

Сейчас в `WordDetailScreen` три независимые переменные:

```kotlin
var showWordLanguagePicker by remember { mutableStateOf(false) }
var translationPickerIndex by remember { mutableStateOf<Int?>(null) }
var translatePickerIndex by remember { mutableStateOf<Int?>(null) }
```

и три почти одинаковых блока `LanguagePickerSheet(...)` на 20 строк каждый.

**Куда двигаться:**

```kotlin
sealed interface PickerRequest {
    data object WordLanguage : PickerRequest
    data class TranslationLanguage(val index: Int) : PickerRequest
    data class TranslateInto(val index: Int) : PickerRequest
}
var picker by remember { mutableStateOf<PickerRequest?>(null) }
picker?.let { LanguagePickerSheet(request = it, ...) }   // один вызов
```

~60 строк схлопываются в ~15, и разница между тремя режимами становится видимой в одном месте.

---

## 3. Несогласованность подходов внутри проекта

Читаемость страдает не только от объёма, но и от того, что **одна и та же задача решается по-разному в соседних файлах**. Читатель не может выработать ожидание.

| Задача | `HomeViewModel` | `WordDetailViewModel` | `SettingsViewModel` |
|---|---|---|---|
| Состояние экрана | **10 отдельных `StateFlow`** | один `StateFlow<WordDetailState>` | 4 отдельных `StateFlow` |
| Список языков | `StateFlow` | `StateFlow` | **обычная функция**, вызывается из composition |
| Job'ы детекта | 2 поля `Job?` | `MutableMap<Int, Job>` | — |

`HomeScreen` из-за первого пункта начинается с 10 строк `collectAsState()`:

```kotlin
val words by viewModel.filteredWords.collectAsState()
val wordInput by viewModel.wordInput.collectAsState()
val translationInput by viewModel.translationInput.collectAsState()
// ... ещё 7
```

А `SettingsScreen` читает SharedPreferences **прямо из композиции**, на каждой рекомпозиции:

```kotlin
orderedLanguages = viewModel.orderedLanguages(),   // синхронный I/O в composition
```

**Куда двигаться:** выбрать один канон и применить ко всем трём VM. Рекомендую подход `WordDetailViewModel` — один `data class ...UiState` + `StateFlow`. Тогда экран собирает одну строку `val state by vm.state.collectAsState()`, а добавление поля не расширяет сигнатуры.

Похожая несогласованность в UI-компонентах:

- **Два текстовых поля** с пересекающимся назначением: `PrimaryTextField` (на `TextField`, Material) и `WordInputField` (на `BasicTextField`, свой бордер). Неочевидно, какое брать.
- **Два источника цветов**: `MaterialTheme.colorScheme` (используется в 12 файлах, ~90 вызовов) и `Theme.colors` / `MittOrdColors` — где палитра **скопирована 1:1**, но используется ровно в одном файле (`RoundedPrimaryButton.kt`). `MittOrdBrushes` не используется **нигде**. То есть тему надо править в двух местах синхронно, иначе разъедется.

---

## 4. Именование и организация файлов

Файлы не сообщают, что внутри:

| Файл | Что внутри на самом деле |
|---|---|
| `home/components/TranslationFieldButtons.kt` | `LanguageFlagButton` + `TranslateButton` |
| `home/components/BottomNavState.kt` | enum `BottomNavState` + `AddWordState` + `SearchState` |
| `home/HomeScreen.kt` | функция называется `MainScreen`, не `HomeScreen` |
| `settings/LanguagePickerSheet.kt` | пикер, который используется **из трёх экранов**, а лежит в `settings` |
| `settings/Languages.kt` | **пустой файл** (один `package`) |
| `wordDetail/LanguageUtils.kt` | **пустой файл** (один `package`) |

Плюс `FloatingBottomNavigationDefault` — суффикс `Default` ничего не значит, второго варианта нет.

**Куда двигаться:**
- имя файла = имя главного публичного символа;
- общие компоненты (`LanguagePickerSheet`, `WordInputField`, `RoundedPrimaryButton`, `PrimaryTextField`) — в `ui/components/`, а не в `screens/home/components` и `screens/settings`;
- `MainScreen` → `HomeScreen`.

Ещё мелочь, но бьёт по восприятию: **порядок импортов хаотичный**. В `HomeScreen.kt`:

```kotlin
import androidx.compose.material3.Text
import android.app.Activity                              // ← посреди compose
import android.widget.Toast
...
import com.reiny.mittord.domain.util.flagForCode         // ← снова посреди compose
import androidx.compose.ui.Modifier
```

Лечится один раз: Ctrl+Alt+O + настроить порядок в code style, дальше не думать.

---

## 5. Логика, которая утекла в UI

Composable сейчас принимают решения, которые должны быть в VM/domain:

**`HomeScreen`** — состояние навбара живёт в UI (`var state by remember`), а инпуты в VM. В результате переход «сбросить и вернуться в Default» написан **три раза** — в `BackHandler`, в `onMiddleClick` (для Search), в `onMiddleClick` (для AddWord):

```kotlin
keyboardController?.hide()
viewModel.clearInputs()      // где-то clearSearch, где-то оба — легко разъезжается
state = BottomNavState.Default
```

**`WordDetailScreen`** — ручное управление списком `FocusRequester` прямо в теле композиции:

```kotlin
val translationFocusRequesters = remember { mutableListOf<FocusRequester>() }
val neededSize = state.translations.size
while (translationFocusRequesters.size < neededSize) translationFocusRequesters.add(FocusRequester())
while (translationFocusRequesters.size > neededSize) translationFocusRequesters.removeAt(...)
```

Мутация обычного `mutableListOf` на каждой рекомпозиции — работает, но это то, на чём читатель спотыкается и теряет 5 минут. Рядом — `kotlinx.coroutines.delay(...)` полным именем вместо импорта.

**`SettingsScreen`** — декодирование Bitmap из `contentResolver` (I/O + `BitmapFactory`) прямо в лямбде `rememberLauncherForActivityResult`, 20 строк вложенных `try/let/withContext` в composable-файле.

**Куда двигаться:** состояние навбара → в `HomeViewModel` (переходы одним `when` в одном месте); загрузка Bitmap → в `SettingsViewModel`/репозиторий; фокус — через `key`-based подход или отдельный `rememberFocusRequesters(count)`-хелпер.

---

## 6. Проглоченные ошибки

Паттерн `catch (_: Exception) {}` встречается многократно и **молча съедает** ошибки:

```kotlin
try { wordFocusRequester.requestFocus() } catch (_: Exception) {}          // ×3 места
fun saveImage(uri: Uri) { ... try { ... } catch (_: Exception) {} }         // фото не сохранилось — юзер не узнает
fun saveAvatarBitmap(bitmap: Bitmap) { ... catch (_: Exception) {} }        // то же
```

Для читателя это шум: непонятно, «здесь ошибка невозможна» или «здесь ошибка возможна, но нам всё равно».

**Куда двигаться:** там где это защита от гонки (focus) — оставить, но с комментарием-однострочником *почему*. Там где это пользовательская операция (сохранение фото/аватара) — эмитить событие ошибки, как уже сделано для `TranslationFailed`. Механизм уже есть, надо просто применить.

Рядом: `state.imagePath!!` в `WordDetailScreen` — единственный `!!` в UI, стоит убрать через `state.imagePath?.let { }`.

---

## 7. Мёртвый код (можно удалить сегодня, ~150 строк)

| Что | Где |
|---|---|
| `RunAsync` (интерфейс + `Base` + provider в DI) | `core/RunAsync.kt`, 61 строка — **ноль использований** |
| `ui/screens/settings/Languages.kt` | пустой файл |
| `ui/screens/wordDetail/LanguageUtils.kt` | пустой файл |
| `MittOrdBrushes` + `LocalMittOrdBrushes` | не используется нигде |
| `DictionaryRepository.getTranslations()` | не вызывается |
| `dao.getAllObjectsWithTranslations()` | не вызывается |
| `SemanticObjectEntity.isFavorite` | не читается нигде |
| `MittOrdColors.textPrimary` / `textSecondary` | оба дефолтятся в `onPrimary`, не используются |
| `TranslationEntry.id` | читается при загрузке, при сохранении игнорируется |

> **Если понадобится вернуть `MittOrdBrushes`** (градиенты в теме):
> `git checkout 45d81cc -- app/src/main/java/com/reiny/mittord/ui/theme/MittOrdBrushes.kt`
> плюс вернуть две строки в `Theme.kt` (`rememberMittOrdBrushes` + `LocalMittOrdBrushes provides brushes`).
> Учти: в удалённой версии `backgroundGradient` был `surface → surface` (сплошная заливка),
> а `rememberMittOrdBrushes` не вызывал `remember`. При возврате это надо доделать.

Мёртвый код не просто мусор — он **заставляет читателя гадать**, «а вдруг это важно».

---

## 8. Магические числа

Анимационные тайминги разбросаны по `FloatingBottomNavigation`:

```kotlin
enter = fadeIn(tween(280, 150)) + slideInVertically(tween(280, 150)) { it / 3 }
enter = fadeIn(tween(280, 260)) + slideInVertically(tween(280, 260)) { it / 2 }
enter = fadeIn(tween(280, 370)) + slideInVertically(tween(280, 370)) { it / 2 }
enter = fadeIn(tween(250, 460))
delay(420)   // подобрано под ↑, но связь нигде не выражена
```

Тут есть **скрытая связь**: `delay(420)` для фокуса подобран под каскад задержек 150/260/370/460. Изменишь одно — сломается другое, и никто не догадается.

Аналогично «размеры навбара»: `60.dp`, `70.dp`, `35.dp`, `30.dp`, `0.85f`, `+ 10.dp` — вперемешку с логикой.

Объект `NavBarAnimation` уже существует, но используется только для части спеков.

**Куда двигаться:** дособрать `NavBarAnimation` (все каскадные задержки как именованные константы: `TITLE_DELAY`, `WORD_FIELD_DELAY`, ...) и завести `object NavBarDimens`. Это дешёвая правка с большим эффектом на читаемость.

Отдельно: **общих отступов нет вообще** — `16.dp`/`24.dp`/`20.dp`/`8.dp` руками по всем экранам. Стоит завести `Spacing` в теме.

---

## 9. Прочее, что стоит знать

- **Тестов нет вообще** (нет `test/`, нет `androidTest/`). Это влияет на читаемость косвенно: нет исполняемых примеров того, как код должен использоваться.
- **`@Preview PreviewScreen`** вызывает `MainScreen`, у которого дефолт `viewModel = hiltViewModel()` — превью упадёт. Такие «сломанные превью» приучают их игнорировать.
- **Seed-данные в проде**: `HomeViewModel.init { seedIfEmpty() }` наливает 20 норвежских слов в любую пустую базу; `SeedDatabaseUseCase` лежит в `domain/`, не в debug-варианте.
- **4 параллельных источника правды о языках**: `LANGUAGES` (name+flag+code), `BCP47_TO_LANG_NAME`, `LANG_NAME_TO_BCP47`, `BCP47_TO_COUNTRY` + `flagForCode()`. Флаг можно получить двумя разными путями (`Language.flag` и `flagForCode(code)`), и они не обязаны совпадать. Просится единая `LanguageCatalog`.
- **Хардкод строк**: `"Move and scale"` (`AvatarCropDialog.kt:126`), `"L"` как заглушка аватара (`SettingsScreen.kt:314`), `"App Logo"` в `contentDescription`.
- **`AddWordState` — `data class` с 6 лямбдами внутри**. `equals`/стабильность на лямбдах не работает как ожидается → лишние рекомпозиции панели. Обычная практика — разделять `State` (только данные) и `Actions`/`Callbacks` (только лямбды, вынести в отдельный класс или передавать отдельно).
- **`MeasureAvailableWidth`/`MeasureAvailableHeight`** — вызывают колбэк из measure-фазы и пишут в state (→ рекомпозиция). Работает, но это неочевидный трюк; в `HomeScreen` объявление `targetHeight` ещё и спрятано в середине `Box` между UI-элементами. Как минимум нужен комментарий *зачем* (обычно это решается через `BoxWithConstraints`).
- **`updateWordFull`** удаляет все переводы и вставляет заново — id переводов теряются на каждом сохранении.

---

## Приоритеты — с чего начать

Отсортировано по соотношению «эффект на читаемость / затраты»:

### Быстрые (несколько часов, эффект сразу)
1. **Удалить мёртвый код** (раздел 7) — минус ~150 строк и минус вопросы «а это зачем?».
2. **Настроить и применить автоформат импортов** по всему проекту.
3. **Вынести `Language.bcp47()`** — убирает 6 копипаст и одно скрытое расхождение поведения (2.2).
4. **Дособрать `NavBarAnimation` + `NavBarDimens`** — вытащить магические числа и *выразить* связь `delay(420)` с каскадом (раздел 8).

### Средние (день-два, максимальный эффект)
5. **Разбить `FloatingBottomNavigation.kt`** — вынести `AddWordPanel` и `NavSearchField` в свои файлы. Это самый большой единичный выигрыш.
6. **Разбить `WordDetailScreen.kt`** на 5–6 секций-компонентов. Побочно исчезнет «прелюдия из 25 `stringResource`».
7. **Свести пикеры к одному `sealed PickerRequest`** (2.3) — минус ~100 строк на двух экранах.

### Структурные (требуют решения «как правильно», зато задают канон)
8. **Привести все 3 ViewModel к одному канону** — `data class UiState` + один `StateFlow` (раздел 3). После этого `HomeScreen` начинается с одной строки вместо десяти.
9. **Вынести повтор автодетекта в один компонент** (2.1) — минус ~120 строк на двух VM.
10. **Решить судьбу `MittOrdColors`/`Theme.colors`**: либо перевести проект на неё целиком, либо удалить и оставить `MaterialTheme.colorScheme`. Сейчас поддерживаются обе — это ловушка.
11. **Поднять общие компоненты в `ui/components/`** и определиться: `PrimaryTextField` **или** `WordInputField`, не оба.
12. **Заменить `catch (_: Exception) {}`** на события ошибок там, где это пользовательская операция (раздел 6).

---

## Резюме одним абзацем

Архитектурно проект в порядке: слои разделены, DI чистый, Context из ViewModel убран, события через `SharedFlow`. «Кошмар» ощущается на **UI-слое** и сводится к трём вещам: (1) composable-функции не декомпозированы — два файла на 679 и 473 строки делают по 5–7 разных дел; (2) одна и та же логика (автодетект языка, резолвинг кода, вызов пикера, мутация списка переводов) написана заново 3–8 раз, местами с незаметными расхождениями; (3) на одну задачу в проекте живёт по два канона (два text field, две цветовые системы, три способа хранить состояние VM), поэтому у читателя не формируется ожидание. Лечится не переписыванием, а декомпозицией + выбором единого канона; порядок работ — в блоке «Приоритеты» выше.
