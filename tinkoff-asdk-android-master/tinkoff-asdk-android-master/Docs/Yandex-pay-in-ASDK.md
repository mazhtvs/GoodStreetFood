# 1. Visual

Yandex pay button имеет несколько параметров для кастомизации в базовой реализации : 
`locale`,`yandexpay_color_scheme `,`yandexpay_corner_radius `. В реализации ASDK `locale` и `yandexpay_color_scheme` берутся из системы.

`yandexpay_corner_radius` - может быть переопределен в теме приложения, через параметр `acqYandexPayButtonCornerRadius`.

Рекомендованный минимальный размер кнопки - `android:minWidth="120dp"` & `android:minHeight="40dp"` 

# 2. Fragment

Рассмотрим метод создания фрагмента.

```

/**
 * Создает обертку для элемента yandex-pay-button для выбора средства оплаты
 *
 * @param activity               контекст для дальнешей навигации платежного флоу из Activity
 * @param yandexPayData          параметры, для настройки yandex-pay библиотеки, полученные от бэка
 * @param options                настройки платежной сессии
 * @param yandexPayRequestCode   код для получения результата, по завершению работы экрана Acquiring SDK
 * @param isProd                 выбор окружения для яндекса YandexPayEnvironment.Prod или YandexPayEnvironment.Sandbox
 * @param enableLogging          включение логгирования событий YandexPay
 * @param themeId                идентификатор темы приложения, параметры которого будет использованы для
 *                               отображение yandex-pay-button
 * @param onYandexErrorCallback  дополнительный метод для возможности обработки ошибки от яндекса на
 *                               стороне клиентского приложения
 * @param onYandexCancelCallback дополнительный метод для возможности обработки отмены
 */
fun TinkoffAcquiring.createYandexPayButtonFragment(
    activity: FragmentActivity,
    yandexPayData: YandexPayData,
    options: PaymentOptions,
    yandexPayRequestCode: Int,
    isProd: Boolean = false,
    enableLogging: Boolean = false,
    themeId: Int? = null,
    onYandexErrorCallback: AcqYandexPayErrorCallback? = null
): YandexButtonFragment 
```
Данный метод рекомедуется для создания кнопки, вариант использования можно посмотреть в sample, `PayableActivity#createYandexButtonFragment`
Параметр `options` можно сетить в рантайме. Обратите внимание, что повторная оплата для одного и того же `OrderId` вернет ошибку.

Предусмотрены ситуации, когда `YandexButtonFragment` может быть восстановлен из `bundle`, после этого необходимо вызвать метод `TinkoffAcquiring.addYandexResultListener` на фрагменте, для корректной работы. 
Рассмотрим метод привязки листнера.

```
/**
 * Создает слушатель, который обрабатывает результат флоу yandex-pay
 *
 * @param activity                контекст для дальнешей навигации платежного флоу из Activity
 * @param fragment                экземляр фрагмента - обертки над яндексом
 * @param yandexPayRequestCode    код для получения результата, по завершению работы экрана Acquiring SDK
 * @param onYandexErrorCallback   дополнительный метод для возможности обработки ошибки от яндекса на
 *                                стороне клиентского приложения
 * @param onYandexCancelCallback  дополнительный метод для возможности обработки отмены
 */
fun TinkoffAcquiring.addYandexResultListener(
    fragment: YandexButtonFragment,
    activity: FragmentActivity,
    yandexPayRequestCode: Int,
    onYandexErrorCallback: AcqYandexPayErrorCallback? = null
)
```

# 3. Процесс оплаты.

После выбора карты, библиотека запустит процесс оплаты самостоятельно. Процесс не остановится при сворачивания экрана. 
В процессе оплаты будет показана шторка со статусом оплаты(в процессе\успех\ошибка), кастомизировать этот экран нельзя.

# 4. Combi-init YandexPay.

Так же вы можете использовать YandexPay в варианте, где запрос init реализован на вашем бекенде, для этого вам необходимо переопределить аргумент `onYandexSuccessCallback` в методе `TinkoffAcquiring#createYandexPayButtonFragment`, для внедрения своей логики. Затем открыть экран яндекс-флоу я помощью метода `TinkoffAcquiring#openYandexPaymentScreen` и передать туда необходимые параметры.

Пример работы combi-init можно посмотреть в классе `DetailsActivity`

 
При интеграции YandexPay с Combi-init, обратите внимание параметрам в запросе init, вы должны сформировать поле DATA и добавить туда данные самостоятельно. Пример формирования можно посмотреть в исходниках  - `InitRequest#putDataIfNonNull`


<img width="625" alt="image" src="https://user-images.githubusercontent.com/37448471/218110938-1acefc75-9fb2-43a5-802f-c7da6304ceb3.png">
 

 