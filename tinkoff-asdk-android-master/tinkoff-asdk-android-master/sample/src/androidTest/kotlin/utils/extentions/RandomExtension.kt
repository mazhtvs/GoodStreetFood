package utils.extentions

import ru.tinkoff.acquiring.sdk.utils.BankIssuer

fun <T> Array<T>.randomExcluding(vararg exclusions: T): T {
    val candidates = this.filterNot { it in exclusions }
    return candidates.random()
}