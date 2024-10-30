/*
 * Copyright © 2020 Tinkoff Bank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package ru.tinkoff.acquiring.sdk.models.enums

import com.google.gson.annotations.SerializedName

/**
 * Признак предмета расчета
 *
 * @author Michael Babayan
 */
enum class PaymentObject12 {

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

    /**
     * Выплата
     */
    @SerializedName("contribution")
    CONTRIBUTION,

    /**
     * Имущественное право
     */
    @SerializedName("property_rights")
    PROPERTY_RIGHTS,

    /**
     * Внереализационный доход
     */
    @SerializedName("unrealization")
    UNREALIZATION,

    /**
     * Иные платежи и взносы
     */
    @SerializedName("tax_reduction")
    TAX_REDUCTION,

    /**
     * Торговый сбор
     */
    @SerializedName("trade_fee")
    TRADE_FEE,

    /**
     * Курортный сбор
     */
    @SerializedName("resort_tax")
    RESORT_TAX,

    /**
     * Залог
     */
    @SerializedName("pledge")
    PLEDGE,

    /**
     * Расход
     */
    @SerializedName("income_decrease")
    INCOME_DECREASE,

    /**
     * Взносы на ОПС ИП без платежей
     */
    @SerializedName("ie_pension_insurance_without_payments")
    IE_PENSION_INSURANCE_WITHOUT_PAYMENTS,

    /**
     * Взносы на ОПС с платежами
     */
    @SerializedName("ie_pension_insurance_with_payments")
    IE_PENSION_INSURANCE_WITH_PAYMENTS,

    /**
     * Взносы на ОМС ИП без платежей
     */
    @SerializedName("ie_medical_insurance_without_payments")
    IE_MEDICAL_INSURANCE_WITHOUT_PAYMENTS,

    /**
     * Взносы на ОМС с платежами
     */
    @SerializedName("ie_medical_insurance_with_payments")
    IE_MEDICAL_INSURANCE_WITH_PAYMENTS,

    /**
     * Взносы на ОСС
     */
    @SerializedName("social_insurance")
    SOCIAL_INSURANCE,

    /**
     * Платеж казино
     */
    @SerializedName("casino_chips")
    CASINO_CHIPS,

    /**
     * Выдача ДС
     */
    @SerializedName("agent_payment")
    AGENT_PAYMENT,

    /**
     * АТНМ
     */
    @SerializedName("excisable_goods_without_marking_code")
    EXCISABLE_GOODS_WITHOUT_MARKING_CODE,

    /**
     * АТМ
     */
    @SerializedName("excisable_goods_with_marking_code")
    EXCISABLE_GOODS_WITH_MARKING_CODE,

    /**
     * ТНМ
     */
    @SerializedName("goods_without_marking_code")
    GOODS_WITHOUT_MARKING_CODE,

    /**
     * ТМ
     */
    @SerializedName("goods_with_marking_code")
    GOODS_WITH_MARKING_CODE
}
