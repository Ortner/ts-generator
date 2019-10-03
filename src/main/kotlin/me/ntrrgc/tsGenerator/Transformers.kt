/*
 * Copyright 2019 Jan Ortner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package me.ntrrgc.tsGenerator

import kotlin.reflect.*


open class DefaultTransformer(
        private val generateProperties: Boolean = true,
        private val generateFunctions: Boolean = false,
        private val generateDataClassFunctions: Boolean = true
): ClassTransformer{


    override fun transformPropertyList(properties: List<KProperty<*>>, klass: KClass<*>): List<KProperty<*>> {
        //NOW filter according to current code
        if(!generateProperties){
            return listOf()
        }
        return properties
    }

    override fun transformPropertyName(propertyName: String, property: KProperty<*>, klass: KClass<*>): String {
        return propertyName
    }

    override fun transformPropertyType(type: KType, property: KProperty<*>, klass: KClass<*>): KType {
        return type
    }


    override fun transformFunctionList(functions: Iterable<KFunction<*>>, klass: KClass<*>): Iterable<KFunction<*>> {
        if(!generateFunctions){
            return listOf()
        }
        return functions.filter { // visibility
            it.visibility?.equals(KVisibility.PUBLIC) ?: false
        }.filter { // data class functions
            generateDataClassFunctions || (!it.name.startsWith("component") && when (it.name){
                "copy","equals","hashCode","toString" -> false
                else -> true
            })
        }
    }

}