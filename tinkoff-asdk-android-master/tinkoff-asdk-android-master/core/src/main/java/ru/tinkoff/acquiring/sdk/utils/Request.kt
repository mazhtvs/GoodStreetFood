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

package ru.tinkoff.acquiring.sdk.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * @author Mariya Chernyadieva
 */
interface Request<R> : Disposable {

    fun execute(onSuccess: (R) -> Unit, onFailure: (Exception) -> Unit)

    suspend fun execute(): R = withContext(Dispatchers.IO) {
        suspendCoroutine { cttn ->
            execute(
                onSuccess = { cttn.resume(it) },
                onFailure = { cttn.resumeWithException(it) }
            )
        }
    }
}
