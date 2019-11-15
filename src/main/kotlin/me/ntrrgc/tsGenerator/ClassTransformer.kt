/*
 * Copyright 2017 Alicia Boya García
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

/**
 * A TypeScript generator class transformer.
 *
 * Allows to customize how class properties are transformed from
 * Kotlin to TypeScript.
 */
interface ClassTransformer {

    /**
     * Generates a list with the properties to include in the
     * definition.
     *
     * The returned list will be filtered further in the next stages of the pipeline
     *
     * @param properties Property list from previous stage in the pipeline,
     * by default the public, non-function properties are chosen.
     * @param klass Class the properties come from.
     */
    fun transformPropertyList(properties: List<KProperty<*>>, klass: KClass<*>): List<KProperty<*>> {
        return properties
    }

    /**
     * Returns the property name that will be included in the
     * definition.
     *
     * @param propertyName Property name generated in previous
     * transformers in the pipeline, by default the original property
     * name.
     * @param property The actual property of the class.
     * @param klass Class the property comes from.
     */
    fun transformPropertyName(propertyName: String, property: KProperty<*>, klass: KClass<*>): String {
        return propertyName
    }

    /**
     * Returns the property type that will be processed and included in the definition.
	 * It includes transformType in the first stage of the pipeline.
     *
     * @param type Type coming from previous stages of the pipeline,
     * by default the actual type of the property.
     * @param property The actual property of the class.
     * @param klass Class the property comes from.
     */
    fun transformPropertyType(type: KType, property: KProperty<*>, klass: KClass<*>): KType {
        return type
    }


    /**
     * Generates a list with the member functions to include in the
     * definition.
     *
	 * The returned list will be filtered further in the next stages of the pipeline
     *
     * @param functions member functions list from previous stage in the pipeline,
     * by default the public member functions are chosen.
     * @param klass Class the properties come from.
     */
    fun transformFunctionList(functions: Iterable<KFunction<*>>, klass: KClass<*>): Iterable<KFunction<*>> {
        return functions
    }
	
	/**
	 * Returns the function name that will be included in the
	 * definition.
	 *
	 * @param name the name of the function.
	 * @param fct the function
	 * @param klass Class the function comes from.
	 */
	fun transformFunctionName(name: String, fct: KCallable<*>, klass: KClass<*>): String {
		return name
	}
	
	
	/**
	 * Returns the name of a parameter of a function that will be included in the
	 * definition.
	 *
	 * @param name the name of the function.
	 * @param fct the function
	 * @param klass Class the function comes from.
	 */
	fun transformParameterName(name: String, fct: KCallable<*>, klass: KClass<*>): String {
		return name
	}
	
	/**
	 * Returns the property type that will be processed and included in the definition.
	 * It includes transformType in the first stage of the pipeline.
	 *
	 * @param name the name of the function.
	 * @param fct the function
	 * by default the actual type of the property.
	 */
	fun transformFctType(type: KType, fct: KCallable<*>, klass: KClass<*>): KType {
		return type
	}
	
	
	/**
	 * Returns the type that will be processed and included in the definition.
	 * In the pipeline, it is also applied as first stage in transformFctType and transformPropertyType
	 *
	 * @param name the name of the function.
	 * @param fct the function
	 * by default the actual type of the property.
	 */
	fun transformType(type: KType): KType {
		return type
	}

}