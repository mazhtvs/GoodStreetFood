package ru.tinkoff.acquiring.sdk.models.enums

import com.google.gson.annotations.SerializedName

/**
 * Признак предмета расчета
 *
 * @author Michael Babayan
 */
enum class PaymentObject105 {

    /**
     * Подакцизный товар
     */
    @SerializedName("excise")
    EXCISE,

    /**
     * Работа
     */
    @SerializedName("job")
    JOB,

    /**
     * Услуга
     */
    @SerializedName("service")
    SERVICE,

    /**
     * Ставка азартной игры
     */
    @SerializedName("gambling_bet")
    GAMBLING_BET,

    /**
     * Выигрыш азартной игры
     */
    @SerializedName("gambling_prize")
    GAMBLING_PRIZE,

    /**
     * Лотерейный билет
     */
    @SerializedName("lottery")
    LOTTERY,

    /**
     * Выигрыш лотереи
     */
    @SerializedName("lottery_prize")
    LOTTERY_PRIZE,

    /**
     * Предоставление результатов интеллектуальной деятельности
     */
    @SerializedName("intellectual_activity")
    INTELLECTUAL_ACTIVITY,

    /**
     * Платеж
     */
    @SerializedName("payment")
    PAYMENT,

    /**
     * Агентское вознаграждение
     */
    @SerializedName("agent_commission")
    AGENT_COMMISSION,

    /**
     * Составной предмет расчета
     */
    @SerializedName("composite")
    COMPOSITE,

    /**
     * Иной предмет расчета
     */
    @SerializedName("another")
    ANOTHER,

    /**
     * Товар
     */
    @SerializedName("commodity")
    COMMODITY,
}
