# TextSerializer

**Compact text serialization library for Paper: MiniMessage, legacy formatting and plain text.**

[![Build](https://github.com/g4vrk/TextSerializer/actions/workflows/build.yml/badge.svg)](https://github.com/g4vrk/TextSerializer/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17+-orange)](.)
[![Paper](https://img.shields.io/badge/Paper-1.16.5+-blue)](.)
[![Version](https://img.shields.io/badge/version-1.1.1-informational)](.)
[![JitPack](https://jitpack.io/v/g4vrk/TextSerializer.svg)](https://jitpack.io/#g4vrk/TextSerializer)
[![License](https://img.shields.io/github/license/g4vrk/TextSerializer)](LICENSE)

---

## Что это

TextSerializer превращает строки из конфигов в Adventure-компоненты и обратно. Один API работает и с MiniMessage, и с `&c`, `§c`, `&#rrggbb`, `&x&r&r&g&g&b&b`, и с их смесью.

Основной API:

* `SerializerRegistry` — точка входа со всеми сериализаторами;
* `universalSerializer()` — MiniMessage, legacy или смесь в одной строке;
* `miniMessageSerializer()` / `legacySerializer()` — строгие варианты;
* `miniMessageConverter()` / `legacyConverter()` — конвертация строки в строку;
* `plainSerializer()` — компонент в обычный текст;
* `TagMapper` — свои теги, одинаково работающие на всех версиях.

> [!NOTE]
> TextSerializer — библиотека, а не самостоятельный плагин.

---

## Модули

| Модуль          | Назначение                                                      |
| --------------- | --------------------------------------------------------------- |
| `common`        | Сериализаторы, конвертеры, кеши                                 |
| `native`        | Paper с нативным MiniMessage (1.19+)                            |
| `legacy`        | Paper без MiniMessage (1.16.5+), parser встроен в JAR           |
| `legacy-engine` | Изолированный MiniMessage для `legacy`, отдельно не подключается |

Выберите один из `native` или `legacy`, `common` подтянется сам.

---

## Подключение через JitPack

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.g4vrk.TextSerializer:native:1.1.1")
}
```

Для старых версий Paper:

```kotlin
dependencies {
    implementation("com.github.g4vrk.TextSerializer:legacy:1.1.1")
}
```

<details>
<summary><b>Gradle Groovy</b></summary>

```groovy
repositories {
    mavenCentral()
    maven { url = uri('https://jitpack.io') }
}

dependencies {
    implementation 'com.github.g4vrk.TextSerializer:native:1.1.1'
}
```

</details>

<details>
<summary><b>Maven</b></summary>

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.g4vrk.TextSerializer</groupId>
        <artifactId>native</artifactId>
        <version>1.1.1</version>
    </dependency>
</dependencies>
```

</details>

---

## Использование

```java
SerializerRegistry registry = NativeSerializerRegistry.INSTANCE;

Component title = registry.universalSerializer().serialize("&6Hello <bold>world</bold>");
```

Для `legacy`:

```java
SerializerRegistry registry = LegacySerializerRegistry.INSTANCE;
```

Остальной код от версии не зависит.

---

## Сериализаторы

| Метод                 | Вход                    | Выход       |
| --------------------- | ----------------------- | ----------- |
| `universalSerializer` | MiniMessage / legacy    | `Component` |
| `miniMessageSerializer` | MiniMessage           | `Component` |
| `legacySerializer`    | legacy                  | `Component` |
| `miniMessageConverter` | legacy                 | MiniMessage |
| `legacyConverter`     | MiniMessage / legacy    | `§`-legacy  |
| `plainSerializer`     | `Component`             | `String`    |

Строка без разметки возвращается как обычный текст, невалидный MiniMessage не бросает исключение, а возвращается текстом.

---

## Свои теги

`TagMapper` отдаёт значение тега строкой. Строка пересекает границу библиотеки как `String`, поэтому один и тот же код работает и с `native`, и с `legacy`, где MiniMessage встроен под другим пакетом.

```java
TagMapper mapper = (name, args) -> switch (name) {
    case "player" -> "<yellow>" + player.getName() + "</yellow>";
    default -> null;
};

Component line = registry.universalSerializer().serialize("&7Hi <player>!", mapper);
```

Переопределите `has(name)`, чтобы стандартные теги вроде `<red>` не попадали в mapper.

Если код уже собран с тем же MiniMessage, можно передать обычные `TagResolver`:

```java
registry.miniMessageSerializer().serialize("<name>", Placeholder.unparsed("name", value));
```

> [!WARNING]
> Значение, вернувшееся из `TagMapper`, разбирается как разметка. Не возвращайте из него чужой ввод без экранирования.

---

## Кеширование

Результаты без тегов-резолверов кешируются через Caffeine отдельно в каждом сериализаторе. Лимит считается по суммарной длине исходных строк, а не по количеству записей, поэтому несколько огромных строк не раздуют память. Обслуживание кеша идёт в вызывающем потоке, общий ForkJoinPool сервера не трогается.

Вызовы с `TagResolver` и `TagMapper` в кеш не попадают.

Все сериализаторы потокобезопасны.

---

## Сборка

Требуется Java 17+.

```bash
./gradlew build
```

Готовые JAR-файлы находятся в:

```text
native/build/libs
legacy/build/libs
```

---

## Лицензия

TextSerializer распространяется под MIT — см. [LICENSE](LICENSE).
