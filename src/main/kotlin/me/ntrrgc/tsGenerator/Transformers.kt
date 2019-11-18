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
import kotlin.reflect.full.createType

/**
 * This transformer controls which elements of a class are being transformed to typescript.
 */
open class DefaultTransformer(
		/**
		 * If true, the properties of this class will be included.
		 */
		val generateProperties: Boolean = true,
		/**
		 * If true, the member of this class will be included.
		 */
        val generateFunctions: Boolean = false,
		/**
		 * If false, the standardized functions of dataclasses (e.g. componentN(), copy, equals, hashCode etc.) will
		 * be ignored.
		 */
        val generateDataClassFunctions: Boolean = true,
		
		/**
		 * type classifiers starting with any of the strings in this list will be replaced by any
		 */
		val startsWith: List<String> = listOf("kotlin.reflect")

): SkipClassTransformer(){


    override fun transformPropertyList(properties: List<KProperty<*>>, klass: KClass<*>): List<KProperty<*>> {
        if(!generateProperties){
            return listOf()
        }
        return properties
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


/**
 * This transformer suppresses generation of classes
 */
open class SkipClassTransformer(
		/**
		 * type classifiers starting with any of the strings in this list will be replaced by any
		 */
		private val startsWith: List<String> = listOf("kotlin.reflect")
): ClassTransformer{
	
	override fun transformType(type: KType): KType {
		val name = type.classifier.toString()
		startsWith.forEach {
			if(name.startsWith("class $it")){
				return Any::class.createType(nullable = type.isMarkedNullable)
			}
		}
		return type
	}
	
}