package ru.tinkoff.acquiring.sample.example.utils

import android.content.Context
import android.widget.Toast
import ru.tinkoff.acquiring.sample.utils.SessionParams

/**
 * @author s.y.biryukov
 */

/**
 * Функция для получения данных тестового терминала.
 * Для проверки работоспособности кода на своем терминале необходимо подставить значения, полученные
 * в личном кабинете Тинькофф Кассы
 */
fun getTerminalInfo() : TerminalData {
    val find = SessionParams.getDefaultTerminals().find { it.description == "СБП 3" }!!
    return TerminalData(
        terminalKey = find.terminalKey,
        terminalPublicKey = find.publicKey,
        password = find.password.orEmpty()
    )
}

/**
 * Вспомогательный класс для группировки параметров терминала. Данные параметры необходимо получить
 * в личном кабинете Тинькофф Кассы
 *
 * @param password Пароль, необходимый для генерации токена для подписания запроса.
 * Не рекомендуется передавать на клиент и хранить в приложении.
 * Пример алгоритма генерации токена [ru.tinkoff.acquiring.sdk.utils.SampleAcquiringTokenGenerator]
 */
data class TerminalData(
    val terminalKey: String,
    val terminalPublicKey: String,
    val password: String,
)

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
