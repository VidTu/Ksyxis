<img alt src=https://github.com/VidTu/Ksyxis/raw/main/docs/ksyxis.png>

# Ksyxis

Ускорьте загрузку своего мира, убрав ненужные чанки.

## Язык (Language)

- [English](https://github.com/VidTu/Ksyxis/blob/main/docs/README.md)
- **Русский**

## Скачать

- [Modrinth](https://modrinth.com/mod/ksyxis)
- [CurseForge](https://curseforge.com/minecraft/mc-mods/ksyxis)
- [GitHub Releases](https://github.com/VidTu/Ksyxis/releases)

## Зависимости

- Fabric, Forge, NeoForge, Quilt, Legacy Fabric или Ornithe
- Minecraft (1.8 или новее)
- **Только Forge 1.8-1.14.4**: Любой мод для Mixin на ваш выбор (например,
  [MixinBootstrap](https://modrinth.com/mod/mixinbootstrap),
  [MixinBooter](https://modrinth.com/mod/mixinbooter),
  [UniMixins](https://modrinth.com/mod/unimixins) или любой другой)

## О проекте

В зависимости от вашей версии, Minecraft загружает некоторые
[чанки](https://ru.minecraft.wiki/w/Чанки), когда вы создаёте свой
мир. Иногда, эти чанки всегда прогружены в фоне. В любом случае
независимо от того, замедляют ли эти чанки создание мира один раз
или постоянно висят и лагают в фоне, большинству игроков они не
нужны. Этот мод полностью отключает ненужные чанки в игре.

*Заметка*: Ненужные чанки иногда используются фермами и
технической стороной игры. Если эти чанки нужны вам, вы
всегда можете удалить мод позже, чтобы включить их.

https://github.com/user-attachments/assets/42e65893-6324-46b1-89a4-044eae77802d

## Версии

| Версия        | Эффект         | Заметка                                                                                                                                                    |
|---------------|----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1.21.9+       | Несущественный | Территория 7x7 чанков не будет прогружена вокруг игрока, когда он заходит. Будет удалена искуственная задержка в 500мс при создании мира в одиночной игре. |
| 1.20.5-1.21.8 | Низкий         | Территория 5x5 спавн-чанков не будет постоянно прогружена в фоне.                                                                                          |
| 1.8-1.20.4    | Высокий        | Территория 21x21 спавн-чанков не будет постоянно прогружена в фоне.                                                                                        |

## ЧаВО (FAQ)

### Для игроков

**В**: Мне нужна помощь, у меня есть вопросы, я хочу связаться с разработчиком.  
**А**: Можете зайти на [сервер Discord](https://discord.gg/Q6saSVSuYQ).
(главный разработчик говорит по-русски)

**В**: Где я могу скачать этот мод?  
**А**: На [Modrinth](https://modrinth.com/mod/ksyxis),
[CurseForge](https://curseforge.com/minecraft/mc-mods/ksyxis)
или [GitHub Releases](https://github.com/VidTu/Ksyxis/releases).
Нестабильные версии можно скачать на
[GitHub Actions](https://github.com/VidTu/Ksyxis/actions).
Для них потребуется аккаунт GitHub.

**В**: Какие загрузчики модов поддерживаются?  
**А**: Fabric, Forge, NeoForge, Quilt, Legacy Fabric и Ornithe.

**В**: Какие версии Minecraft поддерживаются?  
**А**: Поддерживаются Minecraft версии с 1.8 и новее.

**В**: Зачем поддерживать столько версий Minecraft?  
**А**: Потому что я могу.

**В**: Нужно ли мне ставить Fabric API или Quilt Standard Libraries?  
**А**: Необязательно.

**В**: Где версии для Fabric, Forge, NeoForge, Quilt, и т.д.?  
**А**: Все в одном файле.

**В**: Этот мод нужно ставить на клиент или на сервер?  
**А**: Этот мод работает на сервере и на клиенте в одиночной игре.
Он ни на что не влияет, когда используется на клиенте в сетевой игре.

**В**: Этот мод достаточно стабилен для использования?  
**А**: Должен быть. Если что-то сломается,
просто удалите его и ваши миры будут в порядке.

**В**: Я нашёл баг.  
**А**: Отправляйте все баги [сюда](https://github.com/VidTu/Ksyxis/issues) (на
английском языке). Если вы не уверены, баг это или нет, вы можете зайти в
[Discord](https://discord.gg/Q6saSVSuYQ). На уязвимости в моде можно
пожаловаться [сюда](https://github.com/VidTu/Ksyxis/security).

**В**: Можно я закину это в свою сборку?  
**А**: Конечно. За упоминание (например, ссылкой на GitHub-страницу мода)
будем премного благодарны, но это необязательно. Монетизация и
распространение модпака разрешены на условиях
[MIT License](https://github.com/VidTu/Ksyxis/blob/main/LICENSE).

**В**: Этот мод ничего не ускоряет.  
**А**: Эффект может быть незаметен не крутых компах. Мод сделан преимущественно
для бюджетных устройств. Тем не менее, для демонстрации есть
[видео](https://www.youtube.com/watch?v=PXWdDoVU1C4).

**В**: Как прогрузить чанки, если спавн-чанки были удалены?  
**А**: Если вам реально нужно прогрузить чанки, загрузите их
через команду `/forceload` в версиях 1.13 или новее. Для старых
версий, поищите моды, которые умеют прогружать чанки.

**В**: Оно говорит *Ksyxis: No Mixin found*.  
**А**: Если вы используете Forge 1.15.2 (или старше), вам надо установить
[MixinBootstrap](https://modrinth.com/mod/mixinbootstrap),
[MixinBooter](https://modrinth.com/mod/mixinbooter),
[UniMixins](https://modrinth.com/mod/unimixins) или любой другой
Mixin-мод на ваш выбор. Если вы используете Forge 1.16
(или новее) или любую версию Fabric/NeoForge/Quilt/Ornithe,
то вам ничего не нужно ставить и это баг.

###### Не забудьте посмотреть [Developer FAQ](https://github.com/VidTu/Ksyxis/blob/main/docs/CONTRIBUTING.md#developer-faq) для частых вопросов по внутреннему фукнционированию мода. (на английском языке)

## Лицензия

Этот мод предоставляется под лицензией MIT License. Посмотрите файл
[LICENSE](https://github.com/VidTu/Ksyxis/blob/main/LICENSE)
для подробностей. (на английском языке)

## Благодарности

В основном этот мод делает [VidTu](https://github.com/VidTu),
но это было бы невозможно, если бы не:

- [Контрибьюторы](https://github.com/VidTu/Ksyxis/graphs/contributors).
- [Blossom](https://github.com/KyoriPowered/blossom) от
  [Kyori](https://github.com/KyoriPowered). (и контрибьюторов)
- [Fabric Loader](https://github.com/FabricMC/fabric-loader) от
  [FabricMC](https://github.com/FabricMC). (и контрибьюторов)
- [NeoForge](https://github.com/neoforged/NeoForge) от
  [NeoForged](https://github.com/neoforged). (и контрибьюторов)
- [Forge](https://github.com/MinecraftForge/MinecraftForge) от
  [Minecraft Forge](https://github.com/MinecraftForge). (и контрибьюторов)
- [Mixin](https://github.com/SpongePowered/Mixin) от
  [SpongePowered](https://github.com/SpongePowered). (и контрибьюторов)
- [Minecraft](https://minecraft.net/) от
  [Mojang](https://mojang.com/).
- Разные CLI инструменты (`cat, curl, grep, jq, kill, mkfifo, mktemp, sed, sh,
  tr, websocat`) и браузер(ы) на основе Chromium, которые используются в
  [upload](https://github.com/VidTu/Ksyxis/blob/main/dev/upload)-скрипте.

Используются [Gradle](https://gradle.org/) и [Java](https://java.com/).

## Разработка

Загляните в [Dev's Corner](https://github.com/VidTu/Ksyxis/blob/main/docs/CONTRIBUTING.md).
(на английском языке)
