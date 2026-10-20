package com.zclei.hitrise

import com.zclei.hitrise.model.AppLanguage

/** Reviewed translations for connection failures and spoken training cues. */
object AuditUiStrings {
    fun get(language: AppLanguage, english: String): String? =
        when (language) {
            AppLanguage.Spanish -> SPANISH[english]
            AppLanguage.German -> GERMAN[english]
            AppLanguage.Korean -> KOREAN[english]
            else -> null
        }

    private val SPANISH =
        mapOf(
            "Bluetooth counting did not start. Check the device connection and retry." to "El recuento Bluetooth no se inició. Comprueba la conexión del dispositivo e inténtalo de nuevo.",
            "Bluetooth counting did not restart after rest. Check the device connection and retry." to "El recuento Bluetooth no se reinició después del descanso. Comprueba la conexión e inténtalo de nuevo.",
            "Bluetooth counting channel is not ready. Keep the device connected and retry." to "El canal de recuento Bluetooth no está listo. Mantén el dispositivo conectado e inténtalo de nuevo.",
            "Bluetooth dropped during training. Reconnecting..." to "Bluetooth se desconectó durante el entrenamiento. Reconectando...",
            "Preparing counting channel..." to "Preparando el canal de recuento...",
            "Let's go. Settle into rhythm first, keep shoulders relaxed, and use light punches for the first 10 seconds." to "¡Vamos! Estabiliza primero el ritmo, relaja los hombros y usa golpes ligeros durante los primeros 10 segundos.",
            "Trigger: round started | AI live coaching" to "Motivo: inicio de ronda | Entrenamiento en directo con IA",
            "Pick up the pace. You are under the target BPM, shorten the punch and recover faster." to "Acelera el ritmo. Estás por debajo del BPM objetivo; acorta el golpe y recupera más rápido.",
            "Trigger: BPM below target" to "Motivo: BPM por debajo del objetivo",
            "Keep your technique consistent. That punch dropped off, lock the wrist and land clean." to "Mantén una técnica constante. Ese golpe perdió potencia; fija la muñeca y golpea con precisión.",
            "Trigger: Relative Power Score below recent average by 28%" to "Motivo: la puntuación de potencia relativa está un 28 % por debajo de la media reciente",
            "Great burst. Hold that state and give me one more short combo." to "Gran ráfaga. Mantén ese estado y haz otro combo corto.",
            "Trigger: power burst detected" to "Motivo: ráfaga de potencia detectada",
            "Nice combo rhythm. Keep the guard returning so the combination does not pull your posture apart." to "Buen ritmo de combo. Recupera la guardia para mantener una postura compacta.",
            "Trigger: combo streak detected" to "Motivo: secuencia de combos detectada",
            "Final 10 seconds. Push hard, hold the rhythm, and keep punches clean." to "Últimos 10 segundos. Aprieta, mantén el ritmo y golpea con precisión.",
            "Trigger: final 10 seconds" to "Motivo: últimos 10 segundos"
        )

    private val GERMAN =
        mapOf(
            "Bluetooth counting did not start. Check the device connection and retry." to "Die Bluetooth-Schlagzählung wurde nicht gestartet. Prüfe die Geräteverbindung und versuche es erneut.",
            "Bluetooth counting did not restart after rest. Check the device connection and retry." to "Die Bluetooth-Schlagzählung wurde nach der Pause nicht neu gestartet. Prüfe die Verbindung und versuche es erneut.",
            "Bluetooth counting channel is not ready. Keep the device connected and retry." to "Der Bluetooth-Zählkanal ist nicht bereit. Lass das Gerät verbunden und versuche es erneut.",
            "Bluetooth dropped during training. Reconnecting..." to "Bluetooth wurde während des Trainings getrennt. Verbindung wird wiederhergestellt...",
            "Preparing counting channel..." to "Zählkanal wird vorbereitet...",
            "Let's go. Settle into rhythm first, keep shoulders relaxed, and use light punches for the first 10 seconds." to "Los geht's. Finde zuerst den Rhythmus, entspanne die Schultern und schlage in den ersten 10 Sekunden leicht.",
            "Trigger: round started | AI live coaching" to "Auslöser: Rundenstart | KI-Live-Coaching",
            "Pick up the pace. You are under the target BPM, shorten the punch and recover faster." to "Erhöhe das Tempo. Du liegst unter den Ziel-BPM; schlage kürzer und ziehe schneller zurück.",
            "Trigger: BPM below target" to "Auslöser: BPM unter dem Ziel",
            "Keep your technique consistent. That punch dropped off, lock the wrist and land clean." to "Halte deine Technik stabil. Dieser Schlag war schwächer; fixiere das Handgelenk und triff sauber.",
            "Trigger: Relative Power Score below recent average by 28%" to "Auslöser: relative Kraftwertung 28 % unter dem letzten Durchschnitt",
            "Great burst. Hold that state and give me one more short combo." to "Starker Burst. Halte diesen Zustand und zeig noch eine kurze Kombination.",
            "Trigger: power burst detected" to "Auslöser: Kraft-Burst erkannt",
            "Nice combo rhythm. Keep the guard returning so the combination does not pull your posture apart." to "Guter Kombinationsrhythmus. Führe die Deckung zurück und halte die Haltung kompakt.",
            "Trigger: combo streak detected" to "Auslöser: Kombinationsserie erkannt",
            "Final 10 seconds. Push hard, hold the rhythm, and keep punches clean." to "Letzte 10 Sekunden. Gib alles, halte den Rhythmus und schlage sauber.",
            "Trigger: final 10 seconds" to "Auslöser: letzte 10 Sekunden"
        )

    private val KOREAN =
        mapOf(
            "Bluetooth counting did not start. Check the device connection and retry." to "Bluetooth 펀치 카운트가 시작되지 않았습니다. 장치 연결을 확인하고 다시 시도하세요.",
            "Bluetooth counting did not restart after rest. Check the device connection and retry." to "휴식 후 Bluetooth 펀치 카운트가 다시 시작되지 않았습니다. 연결을 확인하고 다시 시도하세요.",
            "Bluetooth counting channel is not ready. Keep the device connected and retry." to "Bluetooth 카운트 채널이 준비되지 않았습니다. 장치 연결을 유지하고 다시 시도하세요.",
            "Bluetooth dropped during training. Reconnecting..." to "훈련 중 Bluetooth 연결이 끊어졌습니다. 다시 연결하는 중...",
            "Preparing counting channel..." to "카운트 채널을 준비하는 중...",
            "Let's go. Settle into rhythm first, keep shoulders relaxed, and use light punches for the first 10 seconds." to "시작합니다. 먼저 리듬을 안정시키고 어깨에 힘을 뺀 뒤 처음 10초는 가볍게 치세요.",
            "Trigger: round started | AI live coaching" to "발동 이유: 라운드 시작 | AI 실시간 코칭",
            "Pick up the pace. You are under the target BPM, shorten the punch and recover faster." to "속도를 높이세요. 목표 BPM보다 낮습니다. 펀치를 짧게 하고 더 빠르게 회수하세요.",
            "Trigger: BPM below target" to "발동 이유: BPM이 목표보다 낮음",
            "Keep your technique consistent. That punch dropped off, lock the wrist and land clean." to "동작을 일정하게 유지하세요. 방금 펀치가 약해졌습니다. 손목을 고정하고 정확히 타격하세요.",
            "Trigger: Relative Power Score below recent average by 28%" to "발동 이유: 상대 파워 점수가 최근 평균보다 28% 낮음",
            "Great burst. Hold that state and give me one more short combo." to "좋은 버스트입니다. 이 상태를 유지하고 짧은 콤보를 한 번 더 하세요.",
            "Trigger: power burst detected" to "발동 이유: 파워 버스트 감지",
            "Nice combo rhythm. Keep the guard returning so the combination does not pull your posture apart." to "콤보 리듬이 좋습니다. 가드를 되돌려 자세가 흐트러지지 않게 하세요.",
            "Trigger: combo streak detected" to "발동 이유: 연속 콤보 감지",
            "Final 10 seconds. Push hard, hold the rhythm, and keep punches clean." to "마지막 10초입니다. 끝까지 밀어붙이고 리듬과 정확한 타격을 유지하세요.",
            "Trigger: final 10 seconds" to "발동 이유: 마지막 10초"
        )
}
