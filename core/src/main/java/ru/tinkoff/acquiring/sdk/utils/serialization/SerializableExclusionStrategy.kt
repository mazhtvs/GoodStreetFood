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

package ru.tinkoff.acquiring.sdk.utils.serialization

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * @author Mariya Chernyadieva
 */
internal class SerializableExclusionStrategy : ExclusionStrategy {

    override fun shouldSkipField(f: FieldAttributes): Boolean {
        return excludedFieldsList.contains(f.name)
    }

    override fun shouldSkipClass(clazz: Class<*>): Boolean {
        return false
    }

    companion object {
        val excludedFieldsList = arrayListOf("serialVersionUID", "CREATOR")
    }
}