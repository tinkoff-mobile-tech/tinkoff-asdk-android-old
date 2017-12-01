# Tinkoff Acquiring SDK for Android

[![Maven Central](https://img.shields.io/maven-central/v/ru.tinkoff.acquiring/ui.svg?maxAge=3600)][search.maven]

![PayFormActivity][img-pay]

Acquiring SDK позволяет интегрировать [Интернет-Эквайринг Tinkoff][acquiring] в мобильные приложения для платформы Android.

Возможности SDK:
- Прием платежей (в том числе рекуррентных)
- Сохранение банковских карт клиента
- Сканирование и распознавание карт с помощью камеры или NFC
- Получение информации о клиенте и сохраненных картах
- Управление сохраненными картами
- Поддержка английского
- Поддержка Android Pay

### Требования
Для работы Tinkoff Acquiring SDK необходим Android версии 4.0 и выше (API level 14).

### Подключение
Для подключения SDK добавьте в [_build.gradle_][build-config] вашего проекта следующую зависимость:
```groovy
compile 'ru.tinkoff.acquiring:ui:$latestVersion'
```
Если вы хотите внедрить сканирование с помощью библиотеки Card-IO, то необходимо добавить в [_build.gradle_][build-config]
```groovy
compile 'ru.tinkoff.acquiring:card-io:$latestVersion'
```


### Подготовка к работе
Для начала работы с SDK вам понадобятся:
* Terminal key
* Пароль
* Public key

Которые выдаются после подключения к [Интернет-Эквайрингу][acquiring].

### Пример работы
Для проведения оплаты необходимо запустить _**PayFormActivity**_. Активити должна быть настроена на обработку конкретного платежа, поэтому для получения интента для ее запуска необходимо вызвать цепочку из методов **PayFormActivity**#_init_, **PayFormStarter**#_prepare_ и **PayFormStarter**#_setCustomerKey_:

```java
PayFormActivity
        .init("TERMINAL_KEY", "PASSWORD", "PUBLIC_KEY") // данные продавца
        .prepare(
               "ORDER-ID",                  // ID заказа в вашей системе
                1000,                       // сумма для оплаты
                "НАЗВАНИЕ ПЛАТЕЖА",         // название платежа, видимое пользователю
                "ОПИСАНИЕ ПЛАТЕЖА",         // описание платежа, видимое пользователю
                "CARD-ID",                  // ID карточки
                "batman@gotham.co",         // E-mail клиента для отправки уведомления об оплате
                false,                      // флаг определяющий является ли платеж рекуррентным [1]
                true                        // флаг использования безопасной клавиатуры [2]
        )
        .setCustomerKey("CUSTOMER_KEY")     // уникальный ID пользователя для сохранения данных его карты
        .startActivityForResult(this, REQUEST_CODE_PAYMENT);

```

Можно передать данные чека на форму, указав парметр [**Receipt**][receipt-javadoc] в метод **PayFormStarter**#_setReceipt_ и кастомизировать форму передав мапу с параметрами в метод **PayFormStarter**#_setData_.
Так же можно указать тему и запустить форму для оплаты уже с привязанных карт (реккурентынй платеж), а также указать модуль для сканирования (свой или **CameraCardIOScanner**)

```java
PayFormActivity
        .init("TERMINAL_KEY", "PASSWORD", "PUBLIC_KEY") // данные продавца
        .prepare(//TODO params)
        .setCustomerKey("CUSTOMER_KEY")     // уникальный ID пользователя для сохранения данных его карты
        .setReceipt(receipt)
        .setData(dataMap)
        .setTheme(themeId)
        .setChargeMode(chargeMode)
        .setCameraCardScanner(new CameraCardIOScanner()))
        .startActivityForResult(this, REQUEST_CODE_PAYMENT);

```

Данные объекты при их наличии будут переданы на сервер с помощью метода [**API Init**][init-documentation], где можно посмотреть детальное описание объекта Receipt

[1] _Рекуррентный платеж_ может производиться для дальнейшего списания средств с сохраненной карты, без ввода ее реквизитов. Эта возможность, например, может использоваться для осуществления платежей по подписке.

[2] _Безопасная клавиатура_ используется вместо системной и обеспечивает дополнительную безопасность ввода, т.к. сторонние клавиатуры на устройстве клиента могут перехватывать данные и отправлять их злоумышленнику.

### Экран привязки карт
Для запуска привязки карт необходимо запустить _**AttachCardFormActivity**_.  Активити должна быть настроена на обработку конкретного платежа, поэтому для получения интента для ее запуска необходимо вызвать цепочку из методов AttachCardFormActivity#init, AttachCardFormActivity#prepare:
```java
AttachCardFormActivity
        .init("TERMINAL_KEY", "PASSWORD", "PUBLIC_KEY") // данные продавца
        .prepare(
                "CUSTOMER_KEY",                         // уникальный ID пользователя для сохранения данных его карты                             
                CheckType.THREE_DS,                     // тип привязки карты
                true,                                   // флаг использования безопасной клавиатуры
                "E-MAIL")                               // e-mail клиента
        .startActivityForResult(this, ATTACH_CARD_REQUEST_CODE);
```

По аналогии с _**PayFormActivity**_, форму привязки карты можно кастомизировать

```java
AttachCardFormActivity
        .init("TERMINAL_KEY", "PASSWORD", "PUBLIC_KEY") // данные продавца
        .prepare("CUSTOMER_KEY", CheckType.THREE_DS, true, "E-MAIL")   
        .setData(data)
        .setTheme(themeId)
        .setCameraCardScanner(new CameraCardIOScanner()))
        .startActivityForResult(this, ATTACH_CARD_REQUEST_CODE);
```

### Темы
для более подробного раздела прочитайте соответвующую тему на wiki страничке.
В приложении есть базовая тема **AcquiringTheme**. Если вы хотите что-то изменить, то отнаследуйтесь от нее и переопределите нужные аттрибуты.

На обоих активити используется одна и та же тема, просто на **AttachCardFormActivity** не используются некоторые аттрибуты.

### Структура
SDK состоит из следующих модулей:

#### Core
Является базовым модулем для работы с Tinkoff Acquiring API. Модуль реализует протокол взаимодействия с сервером и позволяет не осуществлять прямых обращений в API. Не зависит от Android SDK и может использоваться в standalone Java приложениях.

Основной класс модуля - [AcquiringSdk][sdk-class-javadoc] - предоставляет фасад для взаимодействия с Tinkoff Acquiring API. Для работы необходимы ключи и пароль продавца (см. **Подготовка к работе**).

#### UI
Содержит интерфейс, необходимый для приема платежей через мобильное приложение.

Основной класс - [PayFormActivity][payform-class-javadoc] - экран с формой оплаты, который позволяет:
* просматривать информацию о платеже
* вводить или сканировать реквизиты карты для оплаты
* проходить 3DS подтверждение
* управлять списком ранее сохраненных карт

#### Android Pay

[Документация](https://developers.google.com/android-pay/)

Для включения Android Pay необходимо:

Добавить мета информацию в манифест приложения

```xml
    <meta-data
        android:name="com.google.android.gms.wallet.api.enabled"
        android:value="true" />
```

Сконфигурировать необходимые параметры 

```java
    AndroidPayParams androidPayParams = new AndroidPayParams.Builder()
                    .setMerchantName(getString(R.string.merchant_name))
                    .setAddressRequired(false)
                    .setPhoneRequired(false)
                    .setTheme(WalletConstants.THEME_LIGHT)
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build();
```

Передать параметры в PayFormActivity

```java
PayFormActivity
        .init(TERMINAL_KEY, PASSWORD, PUBLIC_KEY) // данные продавца
        .prepare()
        .setAndroidPayParams(params)
        .setCustomerKey(CUSTOMER_KEY)
        .startActivityForResult(this, REQUEST_CODE_PAYMENT);
```

#### Card-IO
Модуль для сканирование карты с помощью камеры с помощью библиотеки Card-IO.

#### Proguard
```
-keep class ru.tinkoff.acquiring.sdk.views.** { *; }
```

#### Sample
Содержит пример интеграции Tinkoff Acquiring SDK и модуля сканирования Card-IO в мобильное приложение по продаже книг.

### Поддержка
- Просьба, по возникающим вопросам обращаться на [card_acquiring@tinkoff.ru][support-email]
- Баги и feature-реквесты можно направлять в раздел [issues][issues]
- [JavaDoc][javadoc]

[search.maven]: http://search.maven.org/#search|ga|1|ru.tinkoff.acquiring.ui
[build-config]: https://developer.android.com/studio/build/index.html
[support-email]: mailto:card_acquiring@tinkoff.ru
[issues]: https://github.com/TinkoffCreditSystems/tinkoff-asdk-android/issues
[acquiring]: https://t.tinkoff.ru/
[payform-class-javadoc]: http://tinkoffcreditsystems.github.io/tinkoff-asdk-android/javadoc/ru/tinkoff/acquiring/sdk/PayFormActivity.html
[sdk-class-javadoc]: http://tinkoffcreditsystems.github.io/tinkoff-asdk-android/javadoc/ru/tinkoff/acquiring/sdk/AcquiringSdk.html
[javadoc]: http://tinkoffcreditsystems.github.io/tinkoff-asdk-android/javadoc/
[img-pay]: http://tinkoffcreditsystems.github.io/tinkoff-asdk-android/res/pay2.png
[receipt-javadoc]: http://tinkoffcreditsystems.github.io/tinkoff-asdk-android/javadoc/ru/tinkoff/acquiring/sdk/Receipt.html
[init-documentation]: https://oplata.tinkoff.ru/landing/develop/documentation/cmp/Init
