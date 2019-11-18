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



package me.ntrrgc.tsGenerator.tests

import com.winterbe.expekt.should
import me.ntrrgc.tsGenerator.ClassTransformer
import me.ntrrgc.tsGenerator.DefaultTransformer
import me.ntrrgc.tsGenerator.TypeScriptGenerator
import me.ntrrgc.tsGenerator.VoidType
import kotlin.reflect.KClass

abstract class BaseTest{
	
	
	fun generateCode(klass: KClass<*>,
					 mappings: Map<KClass<*>, String> = mapOf(),
					 classTransformers: List<ClassTransformer> = listOf(),
					 ignoreSuperclasses: Set<KClass<*>> = setOf(),
					 voidType: VoidType = VoidType.NULL,
					 baseTransformer: ClassTransformer = DefaultTransformer()
	) : Set<TypeScriptDefinition>
	{
		val generator = TypeScriptGenerator(listOf(klass), mappings, classTransformers,
				ignoreSuperclasses, intTypeName = "int", voidType = voidType, baseTransformer = baseTransformer)
		
		val actual = generator.individualDefinitions
				.map(TypeScriptDefinitionFactory::fromCode)
				.toSet()
		return actual
	}
	
	fun generateCodeString(klass: KClass<*>,
								  mappings: Map<KClass<*>, String> = mapOf(),
								  classTransformers: List<ClassTransformer> = listOf(),
								  ignoreSuperclasses: Set<KClass<*>> = setOf(),
								  voidType: VoidType = VoidType.NULL,
								  baseTransformer: ClassTransformer = DefaultTransformer(generateFunctions = true)
	) : String
	{
		return generateCode(klass, mappings, classTransformers, ignoreSuperclasses, voidType, baseTransformer = baseTransformer).joinToString("\n")
	}
	
	/**
	 * Checks if the generated code equals the given string. All leading and trailing whitespace will be removed,
	 * as well as space or tab chars within both strings.
	 */
	fun checkCodeEquals(klass: KClass<*>, code: String, classTransformers: List<ClassTransformer> = listOf()){
		val out = generateCodeString(klass, classTransformers=classTransformers)
		println(out)
		out.clearWS().should.equal(code.clearWS())
	}
	
	
	
	/**
	 * removes spaces, tabs and leading/trailing whitespace. New lines in between are preserved
	 */
	protected fun String.clearWS(): String{
		return this.replace(" ","").replace("\t","").trim()
	}
	
	
}