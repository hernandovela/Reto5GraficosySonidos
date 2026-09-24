# Reto 5 · Gráficos y sonidos

Juego Android de tres en raya en Java. Tú juegas con X; Android juega con O.

## Compatibilidad con Android

La aplicación requiere **Android 7.0 (Nougat, API 24) o superior**, tanto en dispositivos físicos como en emuladores. Esta versión mínima está definida por `minSdk = 24` en `app/build.gradle.kts`.

El proyecto usa `compileSdk = 37` y `targetSdk = 37` para compilar y definir el comportamiento de la aplicación; la versión mínima para instalarla sigue siendo Android 7.0.

La ejecución se verificó en el emulador Small Phone con API 37.1; no se ha probado individualmente cada versión compatible.

## Abrir y ejecutar

1. En Android Studio, selecciona **Open** y abre `Reto5GraficosySonidos`.
2. Deja terminar la sincronización de Gradle. Usa el JDK integrado de Android Studio (21 o posterior).
3. Selecciona la configuración `app` y un emulador con Android 7.0 (API 24) o superior.
4. Pulsa **Run ▶**. No necesita Internet ni permisos para jugar.

Se utiliza el SDK 37, Android Gradle Plugin 9.3.2 y Gradle Wrapper 9.5.0 disponibles en el entorno de desarrollo. `local.properties` contiene la ruta local del SDK y no se publica.

## Requisitos del documento

| Requisito | Implementación |
| --- | --- |
| Vista personalizada | `BoardView extends View`, tres constructores, `onDraw(Canvas)` |
| Cuadrícula | Dos líneas verticales y dos horizontales, ancho adaptable |
| Imágenes X/O | PNG originales de 96×96 en `drawable-nodpi`, cargados como `Bitmap` y dibujados con `drawBitmap` |
| Interacción | Coordenadas táctiles a fila/columna; rechazo de casillas ocupadas y toques fuera del tablero |
| Actualización | `invalidate()` después de las jugadas y al reiniciar |
| Sonidos | Dos WAV originales en `res/raw`, reproducidos con `MediaPlayer` |
| Ciclo de vida | Creación en `onResume`, liberación en `onPause` |
| Silenciar | Interruptor persistente de efectos de sonido |
| Reto adicional | `Handler.postDelayed` de 1000 ms; entrada bloqueada durante el turno de Android |

La partida se conserva durante rotaciones y recreación de la actividad; los marcadores y la preferencia de sonido se guardan. Al salir, se cancela la jugada pendiente y se reprograma al volver. Nueva partida cancela cualquier respuesta pendiente. Android prioriza ganar, después bloquear y finalmente una casilla libre aleatoria.

## Verificación

En Windows: `gradlew.bat testDebugUnitTest assembleDebug lintDebug`.

Las pruebas cubren las ocho líneas ganadoras para ambos jugadores, empate, movimientos inválidos, prioridad de la IA, restauración y reinicio. El APK se genera en `app/build/outputs/apk/debug/app-debug.apk`.

Prueba manual: tocar una casilla, comprobar X inmediatamente y O un segundo después; tocar durante la espera; silenciar; reiniciar durante la espera; girar la pantalla; enviar la app al fondo y volver; completar partidas.

## Fuente y recursos

Basado en **Android Application Programming — Challenge: Graphics and Sound**, Frank McCown, Harding University, proporcionado en `Reto5 - graphics and sound.pdf` (CC BY 3.0: https://creativecommons.org/licenses/by/3.0/). Se adapta la propuesta de Eclipse a Android Studio actual. Código y recursos gráficos/sonoros creados para este proyecto; no se requieren descargas de recursos externos. Se usan WAV PCM cortos, formato que el documento admite como alternativa a MP3.
