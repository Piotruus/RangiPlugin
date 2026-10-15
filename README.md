# RangiPlugin

Plugin Paper/Spigot dla Minecraft 1.21.4, Java 17+. Komenda `/rangi` otwiera menu z VIP, MVIP, ELITA i czwartą rangą `?`. Kliknięcie otwiera opis skonfigurowanych bonusów; przycisk powrotu wraca do menu.

## Budowanie
1. Zainstaluj JDK 17+ oraz Maven.
2. W katalogu projektu uruchom: `mvn package`
3. Plik wynikowy: `target/RangiPlugin-1.0.0.jar`
4. Wrzuć go do `plugins/` i uruchom ponownie serwer.

## Konfiguracja
Edytuj `plugins/RangiPlugin/config.yml`: `menu.slots`, `menu.items` oraz `ranks.*.description`. Opisy bonusów w konfiguracji są przykładowe — zastąp je rzeczywistymi korzyściami na swoim serwerze. Puste pole `command` jest na przyszłość; plugin nie sprzedaje ani nie nadaje rang automatycznie.

Komenda admina: `/rangi reload` (uprawnienie `rangi.admin`, domyślnie OP).
