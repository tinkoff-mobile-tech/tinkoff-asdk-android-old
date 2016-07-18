# Tinkoff Acquiring SDK for Android

[![Maven Central](https://img.shields.io/maven-central/v/ru.tinkoff.acquiring/ui.svg?maxAge=2592000)][search.maven]

Acquiring SDK позволяет интегрировать функционал Интернет-Эквайринга в мобильные приложения для платформы Android.

Поддерживаемый функционал:
- Прием платежей (в том числе рекуррентных)
- Сохранение банковских карт клиента
- Сканирование карт и распознавание карт с помощью камеры
- Сканирование карт с помощью NFC
- Получение информации о клиенте и сохраненных картах
- Управление сохраненными картами

![PayFormActivity][img-pay]

### Требования
Для работы Tinkoff Acquiring SDK требуется Android версии 4.0 и выше (API level 14)

### Подключение
Для подключения SDK добавьте в [_build.gradle_][build-config] вашего проекта следующую зависимость:
```groovy
compile 'ru.tinkoff.acquiring:ui:1.0.1'
```

Если вам нужна только обертка над REST API без UI компонентов, то _вместо_ указанной
выше зависимости включите в _build.gradle_:
```groovy
compile 'ru.tinkoff.acquiring:core:1.0.1'
```

### Подготовка к работе
Для начала работы с SDK вам понадобятся 2 ключа: _Terminal key_, _Public key_ и пароль. _Где получить_?

### Пример работы
Для проведения оплаты необходимо запустить _**PayFormActivity**_. Активити должна быть
настроена на обработку конкретного платежа, поэтому для получения интента для ее запуска необходимо
вызвать цепочку из методов **PayFormActivity**#_init_, **PayFormActivity**#_prepare_ и **PayFormActivity**#_setCustomerKey_

```java
PayFormActivity
        .init("TERMINAL_KEY", "PASSWORD", "PUBLIC_KEY") // данные продавца
        .prepare(
               "ORDER-ID",                  // ID заказа в вашей системе
                1000,                       // сумма для оплаты
                "НАЗВАНИЕ ПЛАТЕЖА",         // название платежа, видимое пользователю
                "ОПИСАНИЕ ПЛАТЕЖА",         // описание платежа, видимое пользователю
                "CARD-ID",                  // ID картчки
                "batman@gotham.co",         // E-mail клиента для отправки уведомления об оплате
                false,                      // флаг определяющий регулярность платежа [1]
                true                        // флаг использования безопасной клавиатуры [2]
        )
        .setCustomerKey("CUSTOMER_KEY")     // уникальный ID пользователя для сохранения данных его карты
        .startActivityForResult(this, REQUEST_CODE_PAYMENT);

```

[1] _Регулярный платеж_ производится периодически. _false_ в примере указывает на то, что данный
платеж является _разовым_

[2] _Безопасная клавиатура_ используется вместо системной и гарантирует приватность ввода, т.к.
 сторонние клавиатуры на устройстве клиента могут быть небезопасны для ввода конфиденциальных данных (например, данных карты)

### Структура
SDK состоит из трех модулей: _core_, _UI_ и _sample_.

#### Core
Модуль _core_ является базовой оберткой над Tinkoff Acquiring REST API, позволяющей не делать прямые
вызовы к REST API и реализующий функции шифрования и кэширования данных. Модуль не зависит от Android SDK и может
использоваться в standalone Java проектах.

##### AcquirinkSdk
Данный класс предоставляет интерфейс для работы с Tinkoff Acquiring REST API. Для работы с ним
требуются ключи и пароль продавца (см. **Подготовка к работе**). Для подробной информайии о каждом методе
обращайтесь к javadoc.

#### UI
Модуль _UI_ содержит компоненты (главным образом, **PayFormActivity**) для удобной работы с модулем _core_ в Android.

##### PayFormActivity
Экран оплаты. Позволяет посмотреть данные платежа, ввести данные карты для оплаты, проходить в
случае необходимости 3DS, управлять ранее сохраненными картами.

#### Sample
Модуль _sample_ содержит полнценный пример работы с Tinkoff Acquiring SDK.

#### Поддержка
- Для оперативной поддержки по всем вопросам использования Tinkoff Acquiring SDK вы можете писать
на [card_acquiring@tinkoff.ru][support-email].
- Для оповещения о найденных багах используйте раздел [issues][issues].

[search.maven]: http://search.maven.org/#search|ga|1|ru.tinkoff.acquiring.ui
[build-config]: https://developer.android.com/studio/build/index.html
[support-email]: mailto:card_acquiring@tinkoff.ru
[issues]: https://github.com/TinkoffCreditSystems/tinkoff-asdk-android/issues
[img-pay]: http://TODO