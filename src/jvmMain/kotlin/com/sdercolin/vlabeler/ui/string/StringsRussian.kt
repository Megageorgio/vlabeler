@file:Suppress("REDUNDANT_ELSE_IN_WHEN")

package com.sdercolin.vlabeler.ui.string

import com.sdercolin.vlabeler.ui.string.Strings.*

fun Strings.ru(): String? = when (this) {
    AppName -> "vLabeler"
    MenuFile -> "Файл"
    MenuFileNewProject -> "Новый проект..."
    MenuFileOpen -> "Открыть..."
    MenuFileOpenRecent -> "Открыть недавние"
    MenuFileOpenRecentClear -> "Очистить список недавних"
    MenuFileQuickEdit -> "Быстрое редактирование"
    MenuFileSave -> "Сохранить"
    MenuFileSaveAs -> "Сохранить как..."
    MenuFileProjectSetting -> "Настройки проекта..."
    MenuFileImportProject -> "Импортировать проект..."
    MenuFileReloadLabelFile -> "Перезагрузить файл разметки"
    MenuFileReloadLabelFilePickFile -> "Из файла..."
    MenuFileReloadLabelFileDefault -> "Из выходного файла..."
    MenuFileReloadLabelFileDefaultWithoutConfirmation -> "Из выходного файла (без подтверждения)"
    MenuFileExport -> "Экспортировать файл разметки..."
    MenuFileExportOverwrite -> "Экспортировать файл разметки с перезаписью"
    MenuFileExportOverwriteAll -> "Экспортировать все файлы разметки с перезаписью"
    MenuFileInvalidateCaches -> "Очистить кэш"
    MenuFileClose -> "Закрыть"
    MenuEdit -> "Правка"
    MenuEditUndo -> "Отменить"
    MenuEditRedo -> "Повторить"
    MenuEditTools -> "Инструменты"
    MenuEditToolsCursor -> "Курсор"
    MenuEditToolsScissors -> "Ножницы"
    MenuEditToolsPan -> "Панорама"
    MenuEditToolsPlayback -> "Воспроизведение"
    MenuEditRenameEntry -> "Переименовать текущую запись..."
    MenuEditDuplicateEntry -> "Дублировать текущую запись..."
    MenuEditRemoveEntry -> "Удалить текущую запись..."
    MenuEditMoveEntry -> "Переместить текущую запись в..."
    MenuEditToggleDone -> "Отметить текущую запись как готовую / снять отметку"
    MenuEditToggleStar -> "Добавить текущую запись в избранное / убрать"
    MenuEditEditTag -> "Изменить тег текущей записи"
    MenuEditEditEntryExtra -> "Изменить дополнительную информацию текущей записи"
    MenuEditMultipleEditMode -> "Редактировать все связанные записи"
    MenuEditEditModuleExtra -> "Изменить дополнительную информацию текущего подпроекта"
    MenuView -> "Вид"
    MenuViewToggleMarker -> "Показывать линии параметров"
    MenuViewPinEntryList -> "Закрепить список записей"
    MenuViewPinEntryListLocked -> "Зафиксировать разделитель закреплённого списка записей"
    MenuViewToggleProperties -> "Показывать свойства"
    MenuViewToggleToolbox -> "Показывать панель инструментов"
    MenuViewToggleTimescaleBar -> "Показывать шкалу времени"
    MenuViewOpenSampleList -> "Список сэмплов"
    MenuViewVideo -> "Показывать связанное видео"
    MenuViewVideoOff -> "Выкл."
    MenuViewVideoEmbedded -> "Встроенное"
    MenuViewVideoNewWindow -> "В новом окне"
    MenuNavigate -> "Навигация"
    MenuNavigateOpenLocation -> "Открыть расположение"
    MenuNavigateOpenLocationRootDirectory -> "Корневая папка сэмплов"
    MenuNavigateOpenLocationModuleDirectory -> "Папка сэмплов текущего подпроекта"
    MenuNavigateOpenLocationProjectLocation -> "Расположение файла проекта"
    MenuNavigateNextEntry -> "Следующая запись"
    MenuNavigatePreviousEntry -> "Предыдущая запись"
    MenuNavigateNextSample -> "Следующий сэмпл"
    MenuNavigatePreviousSample -> "Предыдущий сэмпл"
    MenuNavigateJumpToEntry -> "Перейти к записи..."
    MenuNavigateNextModule -> "Следующий подпроект"
    MenuNavigatePreviousModule -> "Предыдущий подпроект"
    MenuNavigateJumpToModule -> "Перейти к подпроекту..."
    MenuNavigateScrollFit -> "Прокрутить к текущей записи"
    MenuTools -> "Инструменты"
    MenuToolsBatchEdit -> "Пакетное редактирование"
    MenuToolsBatchEditQuickLaunchManager -> "Настройка слотов..."
    MenuToolsBatchEditQuickLaunch -> "Слот %d: %s"
    MenuToolsBatchEditShowDisabledItems -> "Показывать плагины, недоступные в текущем проекте"
    MenuToolsBatchEditManagePlugins -> "Управление плагинами..."
    MenuToolsPrerender -> "Отрисовать все графики заранее..."
    MenuToolsSyncSample -> "Пересчитать все значения относительно конца сэмпла..."
    MenuToolsRecycleMemory -> "Освободить память"
    MenuToolsFileNameNormalizer -> "Нормализация имён файлов..."
    MenuSettings -> "Настройки"
    MenuSettingsPreferences -> "Параметры..."
    MenuSettingsLabelers -> "Лейблеры..."
    MenuSettingsTemplatePlugins -> "Генераторы шаблонов..."
    MenuSettingsTracking -> "Сбор статистики использования..."
    MenuHelp -> "Справка"
    MenuHelpCheckForUpdates -> "Проверить обновления..."
    MenuHelpOpenLogDirectory -> "Открыть папку с логами"
    MenuHelpIncludeInfoLog -> "Включить подробное логирование"
    MenuHelpOpenHomePage -> "Открыть сайт vLabeler"
    MenuHelpOpenLatestRelease -> "Открыть последний релиз"
    MenuHelpOpenGitHub -> "Открыть страницу на GitHub"
    MenuHelpJoinDiscord -> "Присоединиться к Discord"
    MenuHelpAbout -> "О программе"
    CommonOkay -> "OK"
    CommonApply -> "Применить"
    CommonCancel -> "Отмена"
    CommonYes -> "Да"
    CommonNo -> "Нет"
    CommonWarning -> "Предупреждение"
    CommonError -> "Ошибка"
    CommonDetails -> "Подробнее"
    CommonOthers -> "Другое"
    CommonPrevious -> "Назад"
    CommonNext -> "Далее"
    CommonFinish -> "Готово"
    CommonSelect -> "Выбрать"
    CommonOpen -> "Открыть"
    CommonSave -> "Сохранить"
    CommonReset -> "Сбросить"
    CommonClear -> "Очистить"
    CommonInputErrorPromptNumber -> "Введите число."
    CommonInputErrorPromptInteger -> "Введите целое число."
    CommonInputErrorPromptNumberRange -> "Введите число от %s до %s."
    CommonInputErrorPromptNumberMin -> "Введите число не меньше %s."
    CommonInputErrorPromptNumberMax -> "Введите число не больше %s."
    CommonRootModuleName -> "(Корень)"
    StarterStart -> "Начало"
    StarterNewProject -> "Новый проект..."
    StarterOpen -> "Открыть..."
    StarterQuickEdit -> "Быстрое редактирование"
    StarterRecent -> "Недавние"
    StarterRecentEmpty -> "Недавних проектов нет."
    StarterRecentDeleted -> "Этот файл проекта был удалён."
    StarterNewSampleDirectory -> "Папка сэмплов"
    StarterNewWorkingDirectory -> "Расположение проекта"
    StarterNewProjectTitle -> "Новый проект"
    StarterNewProjectName -> "Название проекта"
    StarterNewProjectNameWarning -> "Файл проекта уже существует. При создании проекта он будет перезаписан."
    StarterNewCacheDirectory -> "Папка кэша"
    StarterNewLabelerCategory -> "Категория"
    StarterNewLabeler -> "Лейблер"
    StarterNewTemplatePlugin -> "Генератор шаблонов"
    StarterNewTemplatePluginNone -> "Нет"
    StarterNewInputFile -> "Входной файл (.%s)"
    StarterNewEncoding -> "Кодировка"
    StarterNewAutoExport -> "Автоэкспорт"
    StarterNewAutoExportHelp ->
        "При сохранении проекта автоматически экспортировать его с перезаписью указанного входного файла " +
            "(если он не указан — файла по умолчанию, заданного лейблером)."
    StarterNewWarningSelfConstructedLabelerWithTemplatePlugin ->
        "Вы пытаетесь использовать генератор шаблонов с лейблером, который управляет несколькими подпроектами. " +
            "Это опасно: все существующие файлы разметки могут быть перезаписаны сгенерированными записями. " +
            "Пожалуйста, ещё раз убедитесь, что настройки соответствуют вашим задачам."
    StarterNewDirectoryPage -> "Настройки папок"
    StarterNewLabelerPage -> "Настройки лейблера"
    StarterNewDataSourcePage -> "Настройки источника данных"
    StarterNewContentType -> "Создать с помощью..."
    StarterNewContentTypeDefault -> "По умолчанию"
    StarterNewContentTypeFile -> "Файла"
    StarterNewContentTypePlugin -> "Генератора шаблонов"
    StarterNewAdvancedSettings -> "Дополнительные настройки"
    SampleListIncludedHeader -> "Сэмплы проекта"
    SampleListIncludedItemEntryCountSingle -> "%d запись"
    SampleListIncludedItemEntryCountPlural -> "Записей: %d"
    SampleListExcludedHeader -> "Другие сэмплы"
    SampleListExcludedPlaceholder -> "В папке сэмплов нет файлов, на которые не ссылается проект."
    SampleListEntryHeader -> "Записи"
    SampleListEntriesPlaceholderUnselected -> "Выберите сэмпл слева, чтобы увидеть связанные с ним записи."
    SampleListEntriesPlaceholderNoEntry -> "С выбранным сэмплом не связано ни одной записи."
    SampleListEntriesPlaceholderNoEntryButton -> "Создать по умолчанию"
    SampleListCreateDefaultForAllButton -> "Создать по умолчанию для всех неиспользуемых"
    SampleListJumpToSelectedEntryButton -> "Перейти к выбранной записи"
    SampleListOpenSampleDirectoryButton -> "Открыть папку сэмплов"
    SampleListCurrentModuleLabel -> "Подпроект: "
    SampleListSampleDirectoryLabel -> "Папка сэмплов: "
    SampleListSampleDirectoryRedirectButton -> "Изменить папку сэмплов"
    PrerendererModuleText -> "Отрисовка подпроектов %d/%d..."
    PrerendererModuleTextFinished -> "Отрисовка подпроектов %d/%d... Готово"
    PrerendererSampleText -> "Отрисовка сэмплов %d/%d..."
    PrerendererSampleTextFinished -> "Отрисовка сэмплов %d/%d... Готово"
    PrerendererChartText -> "Отрисовка графиков %d/%d..."
    PrerendererChartTextFinished -> "Отрисовка графиков %d/%d... Готово"
    EditorRenderStatusLabel -> "%d/%d Отрисовка..."
    ChooseSampleDirectoryDialogTitle -> "Выберите папку сэмплов"
    ChooseWorkingDirectoryDialogTitle -> "Выберите папку для проекта"
    ChooseCacheDirectoryDialogTitle -> "Выберите папку кэша"
    ChooseInputFileDialogTitle -> "Выберите входной файл"
    OpenProjectDialogTitle -> "Открыть проект"
    SaveAsProjectDialogTitle -> "Сохранить проект как"
    ImportDialogTitle -> "Импорт"
    ExportDialogTitle -> "Экспорт"
    SetResolutionDialogDescription -> "Введите разрешение холста (точек на пиксель) для редактора (%d ~ %d)"
    SetEntryPropertyDialogDescription ->
        "Введите значение свойства `%s` этой записи. \n" +
            "Обратите внимание, что введённое значение никак не ограничивается, " +
            "поэтому после этого проверьте корректность данных самостоятельно."
    AskIfSaveBeforeOpenDialogDescription ->
        "Есть несохранённые изменения. Сохранить их перед открытием " +
            "нового проекта?"
    AskIfSaveBeforeExportDialogDescription -> "Есть несохранённые изменения. Сохранить их перед экспортом?"
    AskIfSaveBeforeCloseDialogDescription ->
        "Есть несохранённые изменения. Сохранить их перед закрытием " +
            "текущего проекта?"
    AskIfSaveBeforeExitDialogDescription -> "Есть несохранённые изменения. Сохранить их перед выходом?"
    InputEntryNameDialogDescription -> "Переименовать запись"
    InputEntryNameDuplicateDialogDescription -> "Введите имя новой записи"
    InputEntryNameCutFormerDialogDescription -> "Введите имя первой записи после разрезания"
    InputEntryNameCutLatterDialogDescription -> "Введите имя второй записи после разрезания"
    EditEntryNameDialogExistingError -> "Запись с таким именем уже существует."
    EditEntryExtraDialogDescription -> "Изменить дополнительную информацию текущей записи"
    EditModuleExtraDialogDescription -> "Изменить дополнительную информацию текущего подпроекта"
    MoveEntryDialogDescription -> "Введите новый индекс для записи \"%1\$s\" (%2\$d ~ %3\$d)"
    AskIfRemoveEntryDialogDescription -> "Удаление записи \"%s\"..."
    AskIfRemoveEntryLastDialogDescription ->
        "Удаление записи \"%s\"...\n" +
            "Это единственная запись, которая ссылается на текущий сэмпл.\n" +
            "Если позже понадобится добавить для него запись, откройте меню `Вид` -> `Список сэмплов`."
    AskIfLoadAutoSavedProjectDialogDescription ->
        "Найден автоматически сохранённый файл проекта. Загрузить его? " +
            "Файл будет удалён, если вы откроете или создадите другой проект."
    AskIfRedirectSampleDirectoryDialogDescription ->
        "Папка сэмплов текущего подпроекта (%s) не найдена " +
            "или не содержит нужных сэмплов. " +
            "Указать другую папку?"
    AskIfLabelFileChangeDetectedDialogDescription ->
        "Файл разметки был изменён другой программой. " +
            "Перезагрузить файл, чтобы применить изменения?\n" +
            "Это поведение можно настроить в `Параметры` -> `Автоперезагрузка`."
    PluginDialogTitle -> "vLabeler - Плагин"
    PluginDialogInfoAuthor -> "автор: %s"
    PluginDialogInfoVersion -> "версия: %d"
    PluginDialogInfoContact -> "Связаться с автором"
    PluginDialogDescriptionMin -> "мин.: %s"
    PluginDialogDescriptionMax -> "макс.: %s"
    PluginDialogDescriptionMinMax -> "мин.: %s, макс.: %s"
    PluginDialogExecute -> "Выполнить"
    PluginDialogImportFromSavedParams -> "Загрузить сохранённые параметры по умолчанию"
    PluginDialogImportFromSlot -> "Загрузить из слота %1\$d: %2\$s"
    PluginDialogEmptySlotName -> "(пусто)"
    PluginDialogImportFromFile -> "Импортировать из файла"
    PluginDialogImportSuccess -> "Пресет успешно импортирован."
    PluginDialogImportFailure -> "Не удалось импортировать пресет. Вероятно, он несовместим с текущим элементом."
    PluginDialogExportToSavedParams -> "Сохранить параметры как параметры по умолчанию"
    PluginDialogExportToSlot -> "Сохранить параметры в слот %1\$d: %2\$s"
    PluginDialogExportToFile -> "Экспортировать параметры в файл"
    PluginDialogExportSuccess -> "Пресет успешно экспортирован."
    PluginDialogExportFailure -> "Не удалось экспортировать пресет."
    PluginEntrySelectorTextMatchTypeEquals -> "Равно"
    PluginEntrySelectorTextMatchTypeContains -> "Содержит"
    PluginEntrySelectorTextMatchTypeStartsWith -> "Начинается с"
    PluginEntrySelectorTextMatchTypeEndsWith -> "Заканчивается на"
    PluginEntrySelectorTextMatchTypeRegex -> "Регулярное выражение"
    PluginEntrySelectorNumberMatchTypeEquals -> "="
    PluginEntrySelectorNumberMatchTypeGreaterThan -> ">"
    PluginEntrySelectorNumberMatchTypeGreaterThanOrEquals -> ">="
    PluginEntrySelectorNumberMatchTypeLessThan -> "<"
    PluginEntrySelectorNumberMatchTypeLessThanOrEquals -> "<="
    PluginEntrySelectorPreservedSubjectSample -> "Имя сэмпла (без расширения)"
    PluginEntrySelectorPreservedSubjectName -> "Имя записи"
    PluginEntrySelectorPreservedSubjectTag -> "Тег"
    PluginEntrySelectorPreservedSubjectDone -> "Готово"
    PluginEntrySelectorPreservedSubjectStar -> "В избранном"
    PluginEntrySelectorPreservedSubjectScript -> "Выражение"
    PluginEntrySelectorComparerValue -> "Значение"
    PluginEntrySelectorPreviewSummaryError -> "Некорректный ввод"
    PluginEntrySelectorPreviewSummaryInitializing -> "Инициализация..."
    PluginEntrySelectorPreviewSummary -> "Выбрано %d/%d"
    PluginEntrySelectorPlaceholder -> "Фильтров нет, выбраны все записи."
    PluginEntrySelectorExpressionTitle -> "Выражение"
    PluginEntrySelectorExpressionDescription ->
        "Логическое выражение, объединяющее фильтры, заданные выше.\n" +
            "Доступные элементы: `and`, `or`, `not`, `xor`, `(`, `)`, `#1`, `#2` и т.д."
    EditorSubTitleMultiple -> "редактирование %1\$d записей в сэмпле %2\$s"
    FailedToLoadSampleFileError ->
        "Не удалось загрузить сэмпл.\nВозможно, файл не существует или имеет неподдерживаемый формат."
    PluginRuntimeUnexpectedException ->
        "При выполнении плагина произошла непредвиденная ошибка.\n" +
            "Пожалуйста, свяжитесь с автором и приложите лог ошибок."
    FailedToCreateProjectException ->
        "Не удалось создать проект. " +
            "Проверьте настройки лейблера/плагина и убедитесь, что всё настроено правильно.\n" +
            "Если проблема повторяется, свяжитесь с автором лейблера/плагина и приложите лог ошибок."
    InvalidCreatedProjectException ->
        "Созданный проект некорректен. " +
            "Проверьте настройки лейблера/плагина и убедитесь, что всё настроено правильно.\n" +
            "Если проблема повторяется, свяжитесь с автором лейблера/плагина и приложите лог ошибок."
    FailedToCreateProjectNoSampleException ->
        "Не удалось создать проект: в папке сэмплов, указанной в настройках папок, не найдено ни одного сэмпла."
    InvalidOpenedProjectException ->
        "Не удалось открыть проект, так как он содержит некорректные данные.\n" +
            "Подробности смотрите в логе ошибок."
    ProjectParseException ->
        "Не удалось открыть проект.\n" +
            "Возможно, он повреждён или создан несовместимой версией vLabeler.\n" +
            "Попробуйте создать новый проект и импортировать файл проекта через меню `Файл` -> `Импортировать...`."
    ProjectImportException ->
        "Не удалось импортировать файл.\n" +
            "Возможно, это некорректный файл проекта vLabeler. Подробности смотрите в логе ошибок."
    ProjectUpdateOnSampleException ->
        "Не удалось обновить проект по загруженному сэмплу." +
            "\nПодробности смотрите в логе ошибок."
    InvalidEditedProjectException -> "Некорректный отредактированный проект.\nПодробности смотрите в логе ошибок."
    CustomizableItemLoadingException -> "Не удалось загрузить выбранный пользовательский элемент."
    CustomizableItemRemovingException -> "Не удалось удалить выбранный пользовательский элемент."
    PluginRuntimeExceptionTemplate -> "Ошибка выполнения плагина: %s"
    ProjectConstructorRuntimeExceptionTemplate -> "Ошибка выполнения лейблера при создании проекта: %s"
    QuickProjectBuilderRuntimeExceptionTemplate ->
        "Ошибка выполнения лейблера при создании проекта быстрого редактирования: %s"
    PropertySetterRuntimeExceptionTemplate -> "Ошибка выполнения лейблера при установке свойства: %s"
    PropertySetterUnexpectedRuntimeException ->
        "При установке свойства произошла непредвиденная ошибка.\n" +
            "Пожалуйста, свяжитесь с автором лейблера и приложите лог ошибок."
    VideoComponentInitializationException ->
        "Не удалось инициализировать видеокомпонент. Для этой функции на устройстве должен быть установлен VLC. " +
            "Подробности в разделе `Video integration` файла README."
    VideoFileNotFoundExceptionTemplate ->
        "Не найдено видео с именем \"%s\" и одним из расширений %s."
    LabelerManagerTitle -> "Лейблеры"
    LabelerManagerImportDialogTitle -> "Импорт лейблера"
    TemplatePluginManagerTitle -> "Генераторы шаблонов"
    TemplatePluginManagerImportDialogTitle -> "Импорт генератора шаблонов"
    MacroPluginManagerTitle -> "Плагины пакетного редактирования"
    MacroPluginManagerImportDialogTitle -> "Импорт плагина пакетного редактирования"
    MacroPluginReportDialogTitle -> "Результат пакетного редактирования"
    MacroPluginReportDialogCopy -> "Копировать"
    CustomizableItemManagerRemoveItemConfirm ->
        "Удалить \"%s\"? " +
            "Файл(ы) будут удалены с диска."
    CustomizableItemManagerOpenDirectory -> "Открыть папку"
    CustomizableItemManagerReload -> "Обновить список"
    CustomizableItemManagerLockedDescription -> "Это встроенный элемент, его нельзя удалить."
    PreferencesEditorImport -> "Импорт"
    PreferencesEditorImportDialogTitle -> "Импорт параметров"
    PreferencesEditorImportSuccess -> "Параметры успешно импортированы."
    PreferencesEditorImportFailure -> "Не удалось импортировать выбранный файл параметров."
    PreferencesEditorExport -> "Экспорт"
    PreferencesEditorExportSuccess -> "Параметры успешно экспортированы."
    PreferencesEditorExportFailure -> "Не удалось экспортировать параметры в выбранный файл."
    PreferencesEditorExportDialogTitle -> "Экспорт параметров"
    PreferencesEditorResetPage -> "Сбросить параметры на этой странице"
    PreferencesEditorResetAll -> "Сбросить все параметры"
    PreferencesCharts -> "Графики"
    PreferencesChartsDescription -> "Настройка графиков, отображаемых в редакторе."
    PreferencesChartsCanvas -> "Холст"
    PreferencesChartsCanvasDescription -> "Общие настройки холста, на котором рисуются графики."
    PreferencesChartsCanvasResolution -> "Разрешение холста"
    PreferencesChartsCanvasResolutionDescription ->
        "Количество отсчётов аудио, приходящихся на 1 пиксель.\n" +
            "Чем больше число, тем больший отрезок времени помещается на экране."
    PreferencesChartsCanvasResolutionDefault -> "Разрешение по умолчанию"
    PreferencesChartsCanvasResolutionStep -> "Шаг"
    PreferencesChartsMaxDataChunkSize -> "Максимальный размер блока данных"
    PreferencesChartsMaxDataChunkSizeDescription ->
        "Максимальное число кадров в одном блоке графика.\n" +
            "Чем больше число, тем на меньшее количество частей делятся графики при отрисовке."
    PreferencesChartsWaveform -> "Волна"
    PreferencesChartsWaveformDescription -> "Настройка графика волны."
    PreferencesChartsWaveformResampleDownTo -> "Максимальная частота дискретизации (Гц)"
    PreferencesChartsWaveformResampleDownToDescription ->
        "Аудиофайлы с более высокой частотой дискретизации " +
            "будут передискретизированы до этого значения (сами файлы не изменяются).\n" +
            "0 — отключить передискретизацию."
    PreferencesChartsWaveformNormalize -> "Нормализовать звук"
    PreferencesChartsWaveformNormalizeDescription ->
        "Нормализация требует дополнительного времени при первой загрузке сэмплов.\n" +
            "Сами аудиофайлы при нормализации не изменяются."
    PreferencesChartsWaveformUnitSize -> "Точек на пиксель"
    PreferencesChartsWaveformUnitSizeDescription -> "Увеличьте для более низкого качества изображения."
    PreferencesChartsWaveformIntensityAccuracy -> "Высота изображения (px)"
    PreferencesChartsWaveformYAxisBlankRate -> "Отступ по вертикали (%%)"
    PreferencesChartsWaveformColor -> "Цвет"
    PreferencesChartsWaveformBackgroundColor -> "Цвет фона"
    PreferencesChartsSpectrogram -> "Спектрограмма"
    PreferencesChartsSpectrogramDescription -> "Настройка спектрограммы."
    PreferencesChartsSpectrogramEnabled -> "Показывать спектрограмму"
    PreferencesChartsSpectrogramHeight -> "Высота относительно волны (%%)"
    PreferencesChartsSpectrogramPointDensity -> "Точек на пиксель"
    PreferencesChartsSpectrogramPointDensityDescription -> "Увеличьте для более низкого качества изображения."
    PreferencesChartsSpectrogramHopSize -> "Шаг БПФ"
    PreferencesChartsSpectrogramHopSizeDescription -> "Подстраивается под реальную частоту дискретизации."
    PreferencesChartsSpectrogramWindowSize -> "Размер окна"
    PreferencesChartsSpectrogramWindowSizeDescription -> "Подстраивается под реальную частоту дискретизации."
    PreferencesChartsSpectrogramMelScaleStep -> "Разрешение по частоте (мел)"
    PreferencesChartsSpectrogramMaxFrequency -> "Максимальная отображаемая частота (Гц)"
    PreferencesChartsSpectrogramMinIntensity -> "Минимальная отображаемая интенсивность (дБ)"
    PreferencesChartsSpectrogramMinIntensityInvalid ->
        "Минимальная интенсивность должна быть меньше максимальной."
    PreferencesChartsSpectrogramMaxIntensity -> "Максимальная отображаемая интенсивность (дБ)"
    PreferencesChartsSpectrogramMaxIntensityInvalid ->
        "Максимальная интенсивность должна быть больше минимальной."
    PreferencesChartsSpectrogramWindowType -> "Оконная функция"
    PreferencesChartsSpectrogramColorPalette -> "Цвета"
    PreferencesChartsSpectrogramColorPaletteDescription ->
        "Цветовая палитра спектрограммы. Свои палитры можно добавить в @edit{эту папку}. " +
            "Чтобы изменения применились, откройте окно параметров заново."
    PreferencesChartsSpectrogramUseHighAlphaContrast -> "Высокий контраст прозрачности"
    PreferencesChartsSpectrogramUseHighAlphaContrastDescription ->
        "Влияет только на палитры, содержащие прозрачность."
    PreferencesChartsPower -> "Мощность"
    PreferencesChartsPowerDescription -> "Настройка графика мощности."
    PreferencesChartsPowerEnabled -> "Показывать график мощности"
    PreferencesChartsPowerMergeChannels -> "Объединять каналы"
    PreferencesChartsPowerHeight -> "Высота относительно волны (%%)"
    PreferencesChartsPowerUnitSize -> "Точек на пиксель"
    PreferencesChartsPowerUnitSizeDescription -> "Увеличьте для более низкого качества изображения."
    PreferencesChartsPowerUnitSizeInvalid -> "Размер единицы должен быть не больше размера окна."
    PreferencesChartsPowerWindowSize -> "Размер окна"
    PreferencesChartsPowerWindowSizeInvalid -> "Размер окна должен быть не меньше размера единицы."
    PreferencesChartsPowerMinPower -> "Минимальная отображаемая мощность (дБ)"
    PreferencesChartsPowerMinPowerInvalid -> "Минимальная мощность должна быть меньше максимальной."
    PreferencesChartsPowerMaxPower -> "Максимальная отображаемая мощность (дБ)"
    PreferencesChartsPowerMaxPowerInvalid -> "Максимальная мощность должна быть больше минимальной."
    PreferencesChartsPowerIntensityAccuracy -> "Высота изображения (px)"
    PreferencesChartsPowerColor -> "Цвет"
    PreferencesChartsPowerBackgroundColor -> "Цвет фона"
    PreferencesChartsFundamental -> "F0"
    PreferencesChartsFundamentalDescription -> "Настройка графика основного тона (F0)."
    PreferencesChartsFundamentalEnabled -> "Показывать график F0"
    PreferencesChartsFundamentalHeight -> "Высота относительно волны (%%)"
    PreferencesChartsFundamentalSemitoneResolution -> "Разрешение по полутонам на изображении"
    PreferencesChartsFundamentalMinFundamental -> "Минимальная отображаемая частота (Гц)"
    PreferencesChartsFundamentalMinFundamentalInvalid -> "Минимальная частота должна быть меньше максимальной."
    PreferencesChartsFundamentalMaxFundamental -> "Максимальная отображаемая частота (Гц)"
    PreferencesChartsFundamentalMaxFundamentalInvalid -> "Максимальная частота должна быть больше минимальной."
    PreferencesChartsFundamentalSemitoneSampleNum -> "Количество отсчётов на полутон"
    PreferencesChartsFundamentalMaxHarmonicFrequency -> "Максимальная частота гармоник (Гц)"
    PreferencesChartsFundamentalMaxHarmonicFrequencyInvalid ->
        "Максимальная частота гармоник должна быть больше максимальной частоты."
    PreferencesChartsFundamentalDrawReferenceLine -> "Рисовать опорные линии на нотах «до»"
    PreferencesChartsFundamentalColor -> "Цвет"
    PreferencesChartsFundamentalReferenceLineColor -> "Цвет опорных линий"
    PreferencesChartsFundamentalBackgroundColor -> "Цвет фона"
    PreferencesChartsConversion -> "Поддержка аудиоформатов"
    PreferencesChartsConversionDescription -> "Настройка поддержки аудиоформатов, отличных от wav."
    PreferencesChartsConversionFFmpegPath -> "Путь к исполняемому файлу FFmpeg"
    PreferencesChartsConversionFFmpegPathDescription ->
        "Установите @open{FFmpeg} и укажите путь к исполняемому файлу, " +
            "чтобы файлы конвертировались перед загрузкой. " +
            "Перед использованием в vLabeler убедитесь, что ваша ОС разрешает запуск этого файла. " +
            "Ошибки могут быть связаны с неподписанными исполняемыми файлами или правами доступа."
    PreferencesChartsConversionFFmpegArgs -> "Аргументы FFmpeg (кроме входного и выходного файлов)"
    PreferencesChartsConversionFFmpegUseForWav -> "Конвертировать через FFmpeg и wav-файлы"
    PreferencesKeymap -> "Раскладка"
    PreferencesKeymapDescription -> "Настройка сочетаний клавиш для действий клавиатуры и мыши."
    PreferencesKeymapKeyAction -> "Действия клавиш"
    PreferencesKeymapKeyActionDescription -> "Настройка сочетаний клавиш для действий клавиатуры."
    PreferencesKeymapMouseClickAction -> "Действия по клику мыши"
    PreferencesKeymapMouseClickActionDescription ->
        "Настройка сочетаний для действий по клику мыши.\n" +
            "Действие выполняется, только пока зажаты все клавиши из сочетания."
    PreferencesKeymapMouseScrollAction -> "Действия по прокрутке мыши"
    PreferencesKeymapMouseScrollActionDescription ->
        "Настройка сочетаний для действий по прокрутке колёсика мыши.\n" +
            "Действие выполняется, только пока зажаты все клавиши из сочетания."
    PreferencesKeymapEditDialogTitle -> "Изменение сочетания для:"
    PreferencesKeymapEditDialogDescriptionMouseClick ->
        "Щёлкните левой/правой кнопкой мыши по текстовому полю, удерживая другие " +
            "клавиши, чтобы задать сочетание."
    PreferencesKeymapEditDialogDescriptionMouseScroll ->
        "Прокрутите колёсико мыши над текстовым полем, удерживая другие " +
            "клавиши, чтобы задать сочетание."
    PreferencesKeymapEditDialogConflictingLabel -> "Уже назначено:"
    PreferencesKeymapEditDialogConflictingWarning ->
        "Это сочетание уже назначено другим действиям.\n" +
            "Удалить другие назначения?"
    PreferencesKeymapEditDialogConflictingWarningKeep -> "Оставить"
    PreferencesKeymapEditDialogConflictingWarningRemove -> "Удалить"
    PreferencesView -> "Вид"
    PreferencesViewDescription -> "Настройка внешнего вида"
    PreferencesViewLanguage -> "Язык"
    PreferencesViewFontFamily -> "Шрифт"
    PreferencesViewFontFamilyDescription ->
        "Шрифт (семейство), используемый в приложении.\n" +
            "Кроме имеющихся вариантов, можно добавить свои шрифты в @edit{эту папку}. " +
            "Шрифты ttc пока поддерживаются не полностью. " +
            "Чтобы использовать все начертания ttc-шрифта, заранее сконвертируйте его в файлы ttf/otf.\n" +
            "Чтобы добавленные шрифты появились, откройте окно параметров заново."
    PreferencesViewHideSampleExtension -> "Скрывать расширение файлов сэмплов"
    PreferencesViewAppAccentColor -> "Акцентный цвет приложения (светлый)"
    PreferencesViewAppAccentColorVariant -> "Акцентный цвет приложения (тёмный)"
    PreferencesViewPinnedEntryListPosition -> "Положение закреплённого списка записей"
    PreferencesViewPositionLeft -> "Слева"
    PreferencesViewPositionRight -> "Справа"
    PreferencesViewPositionTop -> "Сверху"
    PreferencesViewPositionBottom -> "Снизу"
    PreferencesViewCornerPositionTopLeft -> "Сверху слева"
    PreferencesViewCornerPositionTopRight -> "Сверху справа"
    PreferencesViewCornerPositionCenterLeft -> "По центру слева"
    PreferencesViewCornerPositionCenterRight -> "По центру справа"
    PreferencesViewCornerPositionBottomLeft -> "Снизу слева"
    PreferencesViewCornerPositionBottomRight -> "Снизу справа"
    PreferencesFontSizeSmall -> "Маленький"
    PreferencesFontSizeMedium -> "Средний"
    PreferencesFontSizeLarge -> "Большой"
    PreferencesFontSizeExtraLarge -> "Очень большой"
    PreferencesEditor -> "Редактор"
    PreferencesEditorDescription -> "Настройка внешнего вида и поведения редактора."
    PreferencesEditorPlayerCursorColor -> "Цвет курсора воспроизведения"
    PreferencesEditorLockedDrag -> "Фиксированное перетаскивание"
    PreferencesEditorLockedDragDescription ->
        "Выберите условие, при котором включается фиксированное перетаскивание при перемещении " +
            "линий параметров.\n" +
            "Когда оно включено, остальные линии параметров сдвигаются вместе с перемещаемой, " +
            "сохраняя своё положение относительно неё."
    PreferencesEditorLockedDragUseLabeler -> "Использовать настройки лейблера"
    PreferencesEditorLockedDragUseStart -> "Фиксированное перетаскивание за начало записи"
    PreferencesEditorLockedDragNever -> "Никогда"
    PreferencesEditorLockedSettingParameterWithCursor -> "Фиксированное перемещение при установке курсором"
    PreferencesEditorLockedSettingParameterWithCursorDescription ->
        "Применять настройку фиксированного перетаскивания и при " +
            "установке параметров действиями «Установить параметр в позицию курсора»"
    PreferencesEditorNotes -> "Пометки"
    PreferencesEditorNotesDescription ->
        "Настройка внешнего вида и поведения редактора для пометок записей " +
            "(избранное, готово, тег, дополнительная информация)."
    PreferencesEditorShowDone -> "Показывать статус «Готово»"
    PreferencesEditorShowStarred -> "Показывать статус «Избранное»"
    PreferencesEditorShowTag -> "Показывать теги"
    PreferencesEditorShowExtra -> "Показывать «Изменить дополнительную информацию»"
    PreferencesEditorShowExtraDescription ->
        "Даже если включено, кнопка не показывается, если лейблер не определяет дополнительную информацию."
    PreferencesEditorPostEditAction -> "Действия после редактирования"
    PreferencesEditorPostEditActionDescription -> "Действия, выполняемые после редактирования записи."
    PreferencesEditorPostEditActionDone -> "Отмечать отредактированные записи как «Готово»"
    PreferencesEditorPostEditActionNext -> "Переходить к следующей записи после редактирования"
    PreferencesEditorPostEditActionEnabled -> "Включено"
    PreferencesEditorPostEditActionTrigger -> "Параметр(ы), запускающие действие"
    PreferencesEditorPostEditActionTriggerUseLabeler -> "Использовать настройки лейблера"
    PreferencesEditorPostEditActionTriggerUseStart -> "Начало записи"
    PreferencesEditorPostEditActionTriggerUseEnd -> "Конец записи"
    PreferencesEditorPostEditActionTriggerUseAny -> "Любой параметр"
    PreferencesEditorPostEditActionUseDragging -> "Запускать при перетаскивании"
    PreferencesEditorPostEditActionUseDraggingDescription ->
        "Выполнять действие после перетаскивания линии (линий) параметров."
    PreferencesEditorPostEditActionUseCursorSet -> "Запускать при «Установить параметр в позицию курсора»"
    PreferencesEditorPostEditActionUseCursorSetDescription ->
        "Выполнять действие после установки параметра(ов) действиями «Установить параметр в позицию курсора»."
    PreferencesEditorScissors -> "Ножницы"
    PreferencesEditorScissorsDescription -> "Настройка внешнего вида и поведения инструмента «Ножницы»."
    PreferencesEditorScissorsUseOnScreenScissors -> "Вводить имена записей прямо в редакторе"
    PreferencesEditorScissorsUseOnScreenScissorsDescription ->
        "Если включено, после щелчка ножницами в редакторе появляется поле для ввода имени новой записи. " +
            "Подтвердить разрезание можно клавишей Enter или отведя курсор от места щелчка. " +
            "Отменить — клавишей Esc. Доступно только в режиме редактирования связанных записей."
    PreferencesEditorScissorsScissorsSubmitThreshold -> "Порог подтверждения действия (DP)"
    PreferencesEditorScissorsScissorsSubmitThresholdDescription ->
        "Если после щелчка ножницами отвести курсор от места щелчка дальше этого расстояния, " +
            "разрезание будет подтверждено."
    PreferencesEditorScissorsColor -> "Цвет"
    PreferencesEditorScissorsActionTargetNone -> "Нет"
    PreferencesEditorScissorsActionTargetFormer -> "Первая запись"
    PreferencesEditorScissorsActionTargetLatter -> "Вторая запись"
    PreferencesEditorScissorsActionGoTo -> "Переходить к записи после разрезания"
    PreferencesEditorScissorsActionAskForName -> "Переименовывать запись после разрезания"
    PreferencesEditorScissorsActionPlay -> "Воспроизводить звук при разрезании"
    PreferencesEditorAutoScroll -> "Автопрокрутка"
    PreferencesEditorAutoScrollDescription ->
        "Когда редактор автоматически прокручивается, чтобы показать " +
            "текущую запись."
    PreferencesEditorAutoScrollOnLoadedNewSample -> "При переходе к другому сэмплу"
    PreferencesEditorAutoScrollOnJumpedToEntry -> "При переходе к другой записи по номеру"
    PreferencesEditorAutoScrollOnSwitchedInMultipleEditMode ->
        "При переходе к другой записи в режиме редактирования связанных записей"
    PreferencesEditorAutoScrollOnSwitched -> "При переходе к другой записи"
    PreferencesEditorContinuousLabelNames -> "Имена меток (непрерывная разметка)"
    PreferencesEditorContinuousLabelNamesDescription ->
        "Настройка отображения имён записей в редакторе при использовании непрерывного лейблера."
    PreferencesEditorContinuousLabelNamesColor -> "Цвет"
    PreferencesEditorContinuousLabelNamesBackgroundColor -> "Цвет фона"
    PreferencesEditorContinuousLabelNamesEditableBackgroundColor -> "Цвет фона (при редактировании)"
    PreferencesEditorContinuousLabelNamesSize -> "Размер"
    PreferencesEditorContinuousLabelNamesPosition -> "Положение"
    PreferencesEditorBorderHighlight -> "Подсветка границ"
    PreferencesEditorBorderHighlightDescription ->
        "Настройка подсветки границ в режиме редактирования связанных записей."
    PreferencesEditorHighlightCurrentEntryBorder -> "Подсвечивать границы текущей записи"
    PreferencesEditorHighlightCurrentEntryBorderDescription ->
        "В режиме редактирования связанных записей подсвечивать границы записи, выбранной в списке записей."
    PreferencesEditorHighlightCursorPositionEntryBorder -> "Подсвечивать границы записи под курсором"
    PreferencesEditorHighlightCursorPositionEntryBorderDescription ->
        "В режиме редактирования связанных записей подсвечивать границы записи, на которой находится курсор."
    PreferencesEditorHighlightEntryBorderEnabled -> "Включено"
    PreferencesEditorHighlightEntryBorderColor -> "Цвет"
    PreferencesEditorHighlightEntryBorderWidth -> "Толщина"
    PreferencesPlayback -> "Воспроизведение"
    PreferencesPlaybackDescription -> "Настройка воспроизведения звука."
    PreferencesPlaybackPlayOnDragging -> "Предпрослушивание"
    PreferencesPlaybackPlayOnDraggingDescription ->
        "При перетаскивании линий параметров с клавишами «Предпрослушивания» (см. раскладку) " +
            "воспроизводится фрагмент звука рядом с курсором."
    PreferencesPlaybackPlayOnDraggingEnabled -> "Включено"
    PreferencesPlaybackPlayOnDraggingRangeRadiusMillis -> "Радиус (мс)"
    PreferencesPlaybackPlayOnDraggingEventQueueSize -> "Хранить событий перетаскивания"
    PreferencesAutoSave -> "Автосохранение"
    PreferencesAutoSaveDescription -> "Настройка автосохранения проекта."
    PreferencesAutoSaveTarget -> "Куда сохранять автоматически"
    PreferencesAutoSaveTargetNone -> "Не сохранять автоматически"
    PreferencesAutoSaveTargetProject -> "Перезаписывать файл проекта"
    PreferencesAutoSaveTargetRecord -> "Сохранять во временный файл"
    PreferencesAutoSaveIntervalSec -> "Интервал (с)"
    PreferencesAutoReload -> "Автоперезагрузка"
    PreferencesAutoReloadDescription -> "Настройка автоматической перезагрузки файлов разметки при их изменении."
    PreferencesAutoReloadBehavior -> "Поведение"
    PreferencesAutoReloadBehaviorDisabled -> "Выключено"
    PreferencesAutoReloadBehaviorAskWithDetails -> "Спрашивать с подробностями"
    PreferencesAutoReloadBehaviorAsk -> "Спрашивать Да/Нет"
    PreferencesAutoReloadBehaviorAuto -> "Автоматически"
    PreferencesHistory -> "История правок"
    PreferencesHistoryDescription -> "Настройка истории правок (отмена/повтор)."
    PreferencesHistoryMaxSize -> "Максимальный размер истории"
    PreferencesHistorySquashIndex -> "Объединять смену записи"
    PreferencesHistorySquashIndexDescription ->
        "Если включено, переходы между записями не сохраняются в историю " +
            "до следующего изменения содержимого."
    PreferencesMisc -> "Прочее"
    PreferencesMiscDescription -> "Прочие настройки. Обратите внимание: некоторые из них экспериментальные."
    PreferencesMiscUpdateChannel -> "Канал обновлений"
    PreferencesMiscUpdateChannelDescription ->
        "Канал, используемый при автоматической проверке обновлений и в `Справка` -> `Проверить обновления...`."
    UpdateChannelStable -> "Стабильный"
    UpdateChannelPreview -> "Предварительный"
    PreferencesMiscUseCustomFileDialog -> "Использовать встроенные диалоги выбора файлов вместо системных"
    PreferencesMiscUseCustomFileDialogDescription ->
        "Если включено, диалоги выбора файлов заменяются встроенными. " +
            "Это может пригодиться, если ваше окружение рабочего стола не поддерживает " +
            "системные диалоги. Возможно, потребуется перезапустить приложение."
    PreferencesMiscDangerZone -> "Опасная зона"
    PreferencesMiscClearRecord -> "Очистить историю использования приложения"
    PreferencesMiscClearRecordDescription ->
        "Удалить все данные об использовании приложения, не относящиеся к параметрам: " +
            "размеры окон, пропущенные версии обновлений, сохранённые слоты плагинов и т.д."
    PreferencesMiscClearRecordButton -> "Очистить"
    PreferencesMiscClearRecordConfirmation ->
        "Очистить всю историю использования приложения?\n" +
            "Это действие нельзя отменить.\n" +
            "Сразу после очистки приложение закроется без сохранения открытого проекта."
    PreferencesMiscClearAppData -> "Удалить все данные приложения"
    PreferencesMiscClearAppDataDescription ->
        "Удалить все данные приложения: параметры, историю использования, установленные плагины и т.д."
    PreferencesMiscClearAppDataButton -> "Удалить все данные"
    PreferencesMiscClearAppDataConfirmation ->
        "Удалить все данные приложения?\n" +
            "Это действие нельзя отменить: с устройства будут удалены все данные vLabeler, " +
            "кроме файлов, которые вы создали сами, например файлов проектов.\n" +
            "Сразу после удаления приложение закроется без сохранения открытого проекта."
    ActionToggleSamplePlayback -> "Воспроизвести/остановить текущий сэмпл"
    ActionToggleEntryPlayback -> "Воспроизвести/остановить текущую запись"
    ActionToggleScreenRangePlayback -> "Воспроизвести/остановить видимый на экране фрагмент"
    ActionToggleVideoPopupEmbedded -> "Показать/скрыть видео (встроенное)"
    ActionToggleVideoPopupNewWindow -> "Показать/скрыть видео (в новом окне)"
    ActionIncreaseResolution -> "Отдалить"
    ActionDecreaseResolution -> "Приблизить"
    ActionInputResolution -> "Ввести разрешение холста"
    ActionCancelDialog -> "Закрыть диалог"
    ActionScissorsCut -> "Разрезать в позиции курсора"
    ActionSetValue1 ->
        "Установить параметр 1 в позицию курсора (в режиме нескольких записей — левую границу записи под курсором)"
    ActionSetValue2 ->
        "Установить параметр 2 в позицию курсора (в режиме нескольких записей — правую границу записи под курсором)"
    ActionSetValue3 -> "Установить параметр 3 в позицию курсора"
    ActionSetValue4 -> "Установить параметр 4 в позицию курсора"
    ActionSetValue5 -> "Установить параметр 5 в позицию курсора"
    ActionSetValue6 -> "Установить параметр 6 в позицию курсора"
    ActionSetValue7 -> "Установить параметр 7 в позицию курсора"
    ActionSetValue8 -> "Установить параметр 8 в позицию курсора"
    ActionSetValue9 -> "Установить параметр 9 в позицию курсора"
    ActionSetValue10 -> "Установить параметр 10 в позицию курсора"
    ActionSetProperty1 -> "Ввести значение свойства 1"
    ActionSetProperty2 -> "Ввести значение свойства 2"
    ActionSetProperty3 -> "Ввести значение свойства 3"
    ActionSetProperty4 -> "Ввести значение свойства 4"
    ActionSetProperty5 -> "Ввести значение свойства 5"
    ActionSetProperty6 -> "Ввести значение свойства 6"
    ActionSetProperty7 -> "Ввести значение свойства 7"
    ActionSetProperty8 -> "Ввести значение свойства 8"
    ActionSetProperty9 -> "Ввести значение свойства 9"
    ActionSetProperty10 -> "Ввести значение свойства 10"
    ActionQuickLaunch1 -> "Запустить плагин из слота 1"
    ActionQuickLaunch2 -> "Запустить плагин из слота 2"
    ActionQuickLaunch3 -> "Запустить плагин из слота 3"
    ActionQuickLaunch4 -> "Запустить плагин из слота 4"
    ActionQuickLaunch5 -> "Запустить плагин из слота 5"
    ActionQuickLaunch6 -> "Запустить плагин из слота 6"
    ActionQuickLaunch7 -> "Запустить плагин из слота 7"
    ActionQuickLaunch8 -> "Запустить плагин из слота 8"
    ActionMoveParameter -> "Перетащить линию параметра"
    ActionMoveParameterWithPlaybackPreview -> "Перетащить линию параметра с предпрослушиванием"
    ActionMoveParameterIgnoringConstraints -> "Перетащить линию параметра без ограничений"
    ActionMoveParameterInvertingLocked ->
        "Перетащить линию параметра с обратной настройкой фиксированного перетаскивания"
    ActionPlayAudioSection -> "Воспроизвести фрагмент, по которому щёлкнули"
    ActionPlayAudioUntilEnd -> "Воспроизвести от места щелчка до конца"
    ActionPlayAudioUntilScreenEnd -> "Воспроизвести от места щелчка до края экрана"
    ActionPlayAudioFromStart -> "Воспроизвести от начала до места щелчка"
    ActionPlayAudioFromScreenStart -> "Воспроизвести от края экрана до места щелчка"
    ActionPlayAudioRange -> "Воспроизвести выделенный перетаскиванием диапазон"
    ActionPlayAudioRangeRepeat -> "Воспроизводить выделенный перетаскиванием диапазон по кругу"
    ActionScrollCanvasLeft -> "Прокрутить холст влево"
    ActionScrollCanvasRight -> "Прокрутить холст вправо"
    ActionZoomInCanvas -> "Приблизить"
    ActionZoomOutCanvas -> "Отдалить"
    ActionGoToNextEntry -> "Следующая запись"
    ActionGoToPreviousEntry -> "Предыдущая запись"
    ActionGoToNextSample -> "Следующий сэмпл"
    ActionGoToPreviousSample -> "Предыдущий сэмпл"
    CheckForUpdatesAlreadyUpdated -> "У вас уже установлена последняя версия vLabeler."
    CheckForUpdatesFailure -> "Не удалось получить информацию о последней версии."
    UpdaterDialogSummaryDetailsLink -> "Подробнее"
    UpdaterDialogTitle -> "vLabeler - Обновление"
    UpdaterDialogCurrentVersionLabel -> "Текущая версия: %s"
    UpdaterDialogLatestVersionLabel -> "Последняя версия: %1\$s (%2\$s)"
    UpdaterDialogStartDownloadButton -> "Скачать"
    UpdaterDialogIgnoreButton -> "Пропустить эту версию"
    UpdaterDialogDownloadPositionLabel -> "Папка загрузки: "
    UpdaterDialogChangeDownloadPositionButton -> "Изменить"
    UpdaterDialogChooseDownloadPositionDialogTitle -> "Выберите папку загрузки"
    AboutDialogTitle -> "vLabeler - О программе"
    AboutDialogCopyInfo -> "Копировать информацию"
    AboutDialogShowLicenses -> "Показать лицензии"
    LicenseDialogTitle -> "vLabeler - Лицензии"
    LicenseDialogLicenses -> "Лицензии open source компонентов vLabeler"
    LoadProjectErrorLabelerNotFound ->
        "На этом устройстве не найден нужный лейблер `%1\$s` (версия %2\$s). " +
            "Установите его вручную перед открытием проекта."
    LoadProjectWarningLabelerCreated -> "Из файла проекта установлен новый лейблер `%s`."
    LoadProjectWarningLabelerUpdated -> "Лейблер `%s` обновлён из файла проекта до версии `%s`."
    LoadProjectWarningCacheDirReset ->
        "Не удалось найти или создать папку кэша, указанную в файле проекта. " +
            "Будет использована папка кэша по умолчанию."
    FilterStarred -> "Только избранные записи"
    FilterUnstarred -> "Только записи не в избранном"
    FilterStarIgnored -> "Без фильтра по избранному"
    FilterDone -> "Только готовые записи"
    FilterUndone -> "Только неготовые записи"
    FilterDoneIgnored -> "Без фильтра по готовности"
    FilterLink -> "Применять фильтры к навигации по проекту"
    FilterLinked -> "Фильтры применяются к навигации по проекту"
    FilterAdvancedInUse -> "Расширенные фильтры"
    FilterDisabledDueToAdvancedInUse -> "Недоступно, так как применены расширенные фильтры"
    ColorPickerDialogTitle -> "vLabeler - Выбор цвета"
    QuickLaunchManagerDialogTitle -> "Слоты плагинов"
    QuickLaunchManagerDialogDescription ->
        "В слоты можно назначить часто используемые плагины пакетного редактирования вместе с их параметрами " +
            "для быстрого доступа. Параметры в слотах независимы друг от друга " +
            "и не влияют на параметры, сохраняемые при обычном использовании."
    QuickLaunchManagerDialogHeaderTitle -> "Слот"
    QuickLaunchManagerDialogHeaderPlugin -> "Плагин"
    QuickLaunchManagerDialogHeaderForceAskParams -> "Всегда спрашивать параметры"
    QuickLaunchManagerDialogItemTitle -> "Слот %d"
    QuickLaunchManagerDialogOpenKeymap -> "Открыть раскладку"
    TrackingSettingsDialogTitle -> "Сбор статистики использования"
    TrackingSettingsDialogDescription ->
        "Здесь можно включить или выключить сбор анонимной статистики использования, например событий " +
            "`Запуск приложения` и `Использование плагина`. Собранные данные помогают улучшать vLabeler " +
            "и не содержат никакой конкретной информации о ваших проектах, данных или личной информации. " +
            "Пожалуйста, включите сбор статистики, если считаете это допустимым. " +
            "Подробнее — по кнопке `Подробнее`."
    TrackingSettingsDialogFirstTimeAlert ->
        "Это окно показано, потому что вы впервые используете версию vLabeler со сбором статистики. " +
            "Открыть его и изменить настройки можно в любой момент через меню " +
            "`Настройки` -> `Сбор статистики использования...`"
    TrackingSettingsDialogEnabled -> "Включено"
    TrackingSettingsDialogTrackingIdLabel -> "ID для статистики:"
    ProjectSettingDialogTitle -> "Настройки проекта"
    ProjectSettingOutputFileLabel -> "Выходной файл"
    ProjectSettingOutputFileHelperText ->
        "Файл для действия `Экспорт`.\nЕсли он не задан, `Экспорт с перезаписью` недоступен."
    ProjectSettingOutputFileDisabledPlaceholder -> "Отключено текущим лейблером"
    ProjectSettingOutputFileSelectorDialogTitle -> "Выберите выходной файл"
    ProjectSettingAutoExportHelperText ->
        "При сохранении проекта автоматически экспортировать все подпроекты в их выходные файлы.\n" +
            "Работает, только если `Выходной файл` задан правильно\n" +
            "или зафиксирован лейблером."
    ImportEntriesDialogTitle -> "Импорт проекта"
    ImportEntriesDialogItemSummaryTitle -> "Записей: %d"
    ImportEntriesDialogItemTargetLabel -> "Куда"
    ImportEntriesDialogItemIncompatible -> "Несовместимо с текущим проектом"
    ImportEntriesDialogReplaceContent -> "Удалить текущие записи"
    ImportEntriesDialogReplaceContentDisabledDescription ->
        "Текущий проект не поддерживает добавление записей, " +
            "поэтому перед импортом все текущие записи будут удалены."
    EntrySampleSyncerModuleText -> "Обработка подпроектов %d/%d..."
    EntrySampleSyncerModuleTextFinished -> "Обработка подпроектов %d/%d... Готово"
    EntrySampleSyncerSampleText -> "Обработка сэмплов %d/%d..."
    EntrySampleSyncerSampleTextFinished -> "Обработка сэмплов %d/%d... Готово"
    FFmpegConverterException ->
        "Не удалось загрузить сэмпл. Чтобы загружать форматы, отличные от wav, " +
            "установите FFmpeg и укажите путь к нему в `Параметры` -> `Графики` -> `Поддержка аудиоформатов`. " +
            "Если FFmpeg уже установлен и настроен, подробности смотрите в логе."
    AppRunningOnCompatibilityModeWarning ->
        "vLabeler работает в режиме совместимости Rosetta 2.\n" +
            "Для лучшей производительности попробуйте сборку для Apple Silicon (~mac-arm64.dmg)."
    EntryFilterSetterDialogTitle -> "Настройки фильтра"
    EntryFilterSetterDialogModeBasic -> "Простой"
    EntryFilterSetterDialogModeAdvanced -> "Расширенный"
    EntryFilterSetterDialogHeaderAny -> "Любой текст содержит"
    EntryFilterSetterDialogHeaderName -> "Имя записи содержит"
    EntryFilterSetterDialogHeaderSample -> "Имя сэмпла содержит"
    EntryFilterSetterDialogHeaderTag -> "Тег содержит"
    EntryFilterSetterDialogHeaderStar -> "В избранном"
    EntryFilterSetterDialogHeaderDone -> "Готово"
    FileNameNormalizerDialogTitle -> "vLabeler - Нормализация имён файлов"
    FileNameNormalizerTitle -> "Нормализация имён файлов"
    FileNameNormalizerDescription ->
        "Этот инструмент конвертирует имена файлов из кодировки NFD (обычно используется в macOS) в " +
            "NFC (обычно используется в Windows). " +
            "Выберите папку, чтобы рекурсивно сконвертировать имена всех файлов в ней, " +
            "или выберите файл, чтобы сконвертировать его содержимое.\n" +
            "Если проект на основе этих сэмплов уже создан, сконвертируйте и имена сэмплов, " +
            "и содержимое файла проекта."
    FileNameNormalizerHandleFolderButton -> "Выбрать папку"
    FileNameNormalizerHandleFileContentButton -> "Выбрать файл"
    FileNameNormalizerHandleFolderSuccess -> "Просмотрено файлов: %d, сконвертировано: %d."
    FileNameNormalizerHandleFileSuccess -> "Содержимое файла успешно сконвертировано."
    FileNameNormalizerHandleFileNoChange -> "Содержимое файла не требует конвертации."
    ReloadLabelDialogTitle -> "Перезагрузка файла разметки"
    ReloadLabelDialogModuleNameTemplate -> "Подпроект: %s"
    ReloadLabelDialogShowUnchanged -> "Показывать неизменённые"
    ReloadLabelDialogNotice ->
        "Сравнение рассчитано по настройкам текущего лейблера, " +
            "поэтому может показывать не все мелкие различия. После перезагрузки проверьте содержимое, " +
            "чтобы убедиться, что всё верно."
    ReloadLabelDialogNoDiff -> "Различий не найдено."
    ReloadLabelDialogInheritNotes -> "Сохранить пометки"
    ReloadLabelDialogInheritNotesDescription ->
        "Если включено, пометки старых записей будут скопированы в новые для всех совпавших пар записей."
    EditorContextActionOpenRenameEntryDialog -> "Переименовать запись..."
    EditorContextActionOpenDuplicateEntryDialog -> "Дублировать запись..."
    EditorContextActionOpenRemoveEntryDialog -> "Удалить запись..."
    EditorContextActionOpenMoveEntryDialog -> "Переместить запись в..."
    EditorContextActionCopyEntryName -> "Копировать имя записи"
    EditorContextActionFilterByEntryName -> "Фильтровать по имени записи"
    EditorContextActionCopySampleName -> "Копировать имя сэмпла"
    EditorContextActionFilterBySampleName -> "Фильтровать по имени сэмпла"
    EditorContextActionFilterByTag -> "Фильтровать по тегу"
    EditorContextActionFilterStarred -> "Показать избранные записи"
    EditorContextActionFilterUnstarred -> "Показать записи не в избранном"
    EditorContextActionFilterDone -> "Показать готовые записи"
    EditorContextActionFilterUndone -> "Показать неготовые записи"
    MenuFileModuleManagement -> "Управление подпроектами (экспериментально)"
    MenuFileModuleManagementAdd -> "Добавить подпроект..."
    MenuFileModuleManagementRename -> "Переименовать текущий подпроект..."
    MenuFileModuleManagementRemove -> "Удалить текущий подпроект..."
    MenuFileModuleManagementDuplicate -> "Дублировать текущий подпроект..."
    ModuleOperationAddDefaultName -> "Добавить подпроект"
    ModuleOperationRenameDefaultName -> "Переименовать текущий подпроект"
    ModuleOperationRemoveDefaultName -> "Удалить текущий подпроект"
    ModuleOperationDuplicateDefaultName -> "Дублировать текущий подпроект"
    MenuFileReloadLabelFileAllModules -> "Из выходных файлов всех подпроектов..."
    MenuFileReloadLabelFileAllModulesWithoutConfirmation ->
        "Из выходных файлов всех подпроектов (без подтверждения)"
    AskIfRemoveEntriesDialogDescription -> "Удаление записей: %d..."
    EditEntriesTagDialogDescription -> "Задать тег для выбранных записей (%d). Оставьте пустым, чтобы очистить теги."
    PreferencesEditorEntryNamePresets -> "Шаблоны названий записей"
    PreferencesEditorEntryNamePresetsDescription ->
        "Готовые названия записей, которые можно быстро подставить в диалогах ввода названия, например при " +
            "переименовании или разрезании записи."
    PreferencesStringListEmptyPlaceholder -> "Нет элементов"
    PreferencesStringListNewItemPlaceholder -> "Новый элемент..."
    PreferencesStringListImport -> "Импорт из файла"
    PreferencesStringListExport -> "Экспорт в файл"
    PreferencesStringListImportDialogTitle -> "Импорт из файла"
    PreferencesStringListExportDialogTitle -> "Экспорт в файл"
    PreferencesStringListImportSuccess -> "Элементы успешно импортированы."
    PreferencesStringListImportFailure -> "Не удалось импортировать выбранный файл."
    PreferencesStringListExportSuccess -> "Элементы успешно экспортированы."
    PreferencesStringListExportFailure -> "Не удалось экспортировать в выбранный файл."
    PreferencesEditorClickToJumpToEntry -> "Переход к записи по клику"
    PreferencesEditorClickToJumpToEntryDescription ->
        "В режиме редактирования нескольких записей клик по записи на холсте делает её текущей"
    PreferencesEditorCascadedDrag -> "Каскадное перетаскивание"
    PreferencesEditorCascadedDragDescription ->
        "Включает каскадное перетаскивание границ в параллельных подпроектах.\n" +
            "Если выбрано «Каскадное перетаскивание», совпадающие границы во всех параллельных подпроектах " +
            "будут двигаться вместе."
    PreferencesEditorCascadedDragDisabled -> "Перетаскивать только одну границу"
    PreferencesEditorCascadedDragEnabled -> "Каскадное перетаскивание"
    PreferencesAutoSavePermanentBackupMaxCount -> "Максимальное число резервных копий"
    PreferencesAutoSavePermanentBackupMaxCountDescription ->
        "Резервные копии создаются при автоматическом или ручном сохранении файла проекта. " +
            "При превышении лимита самые старые копии удаляются."
    ActionSetCurrentEntryLeft ->
        "Установить левую границу текущей записи в позицию курсора (режим нескольких записей)"
    ActionSetCurrentEntryRight ->
        "Установить правую границу текущей записи в позицию курсора (режим нескольких записей)"
    ActionMoveParameterInvertingCascaded ->
        "Перетащить линию параметра с обратной настройкой каскадного перетаскивания"
    EditorContextActionSetEntriesDone -> "Отметить записи как готовые (%d)"
    EditorContextActionSetEntriesUndone -> "Снять отметку «Готово» с записей (%d)"
    EditorContextActionSetEntriesStarred -> "Отметить записи звёздочкой (%d)"
    EditorContextActionSetEntriesUnstarred -> "Снять звёздочку с записей (%d)"
    EditorContextActionEditEntriesTag -> "Задать тег для записей (%d)..."
    EditorContextActionRemoveEntries -> "Удалить записи (%d)..."
    else -> null
}
