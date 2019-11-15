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
 * Class transformer pipeline.
 *
 * For each method the return value of the first transformer
 * to return not null is used.
 */
internal class ClassTransformerPipeline(val memberTransformers: List<ClassTransformer>): ClassTransformer {

    override fun transformPropertyList(properties: List<KProperty<*>>, klass: KClass<*>): List<KProperty<*>> {
		return evaluteTillEnd(properties){ t,last ->
			t.transformPropertyList(last,klass)
		}
    }

    override fun transformPropertyName(propertyName: String, property: KProperty<*>, klass: KClass<*>): String {
		return evaluteTillEnd(propertyName){ t,last ->
			t.transformPropertyName(last, property, klass)
		}
    }

    override fun transformPropertyType(type: KType, property: KProperty<*>, klass: KClass<*>): KType {
		var ret= evaluteTillEnd(type){ t,last ->
			t.transformType(last)
		}
		return evaluteTillEnd(ret){ t,last ->
			t.transformPropertyType(last, property, klass)
		}
    }

    override fun transformFunctionList(functions: Iterable<KFunction<*>>, klass: KClass<*>): Iterable<KFunction<*>>{
		return evaluteTillEnd(functions){ t,last ->
			t.transformFunctionList(last,klass)
		}
    }
	
	override fun transformFunctionName(name: String, fct: KCallable<*>, klass: KClass<*>): String {
		return evaluteTillEnd(name){ t,last ->
			t.transformFunctionName(last, fct, klass)
		}
	}
	
	override fun transformParameterName(name: String, fct: KCallable<*>, klass: KClass<*>): String {
		return evaluteTillEnd(name){ t,last ->
			t.transformParameterName(last, fct, klass)
		}
	}
	
	override fun transformFctType(type: KType, fct: KCallable<*>, klass: KClass<*>): KType {
		var ret= evaluteTillEnd(type){ t,last ->
			t.transformType(last)
		}
		return evaluteTillEnd(ret){ t,last ->
			t.transformFctType(last, fct, klass)
		}
	}
	
	override fun transformType(type: KType): KType {
		return evaluteTillEnd(type){ t,last ->
			t.transformType(last)
		}
	}
	
	
	inline fun <T: Any> evaluteTillEnd(value: T, getNext: (transformer: ClassTransformer, last: T)->T): T{
		var ret: T = value
		for(transformer in memberTransformers){
			ret = getNext(transformer, ret)
		}
		return ret
	}
	
	inline fun <T: Any> firstNotNull(value: T, getNext: (transformer: ClassTransformer)->T?): T{
		var ret: T? = null
		for(transformer in memberTransformers){
			ret = getNext(transformer)
			if(ret!=null)
				break
		}
		return ret ?: value
	}

}