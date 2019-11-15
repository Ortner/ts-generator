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

import java.beans.Introspector
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType

/**
 * TypeScript definition generator.
 *
 * Generates the content of a TypeScript definition file (.d.ts) that
 * covers a set of Kotlin and Java classes.
 *
 * This is useful when data classes are serialized to JSON and
 * handled in a JS or TypeScript web frontend.
 *
 * Supports:
 *  * Primitive types, with explicit int
 *  * Kotlin and Java classes
 *  * Data classes
 *  * Enums
 *  * Any type
 *  * Generic classes, without type erasure
 *  * Generic constraints
 *  * Class inheritance
 *  * Abstract classes
 *  * Lists as JS arrays
 *  * Maps as JS objects
 *  * Null safety, even inside composite types
 *  * Java beans
 *  * Mapping types
 *  * Customizing class definitions via transformers
 *  * Parenthesis are placed only when they are needed to disambiguate
 *
 * @constructor
 *
 * @param rootClasses Initial classes to traverse. Enough definitions
 * will be created to cover them and the types of their properties
 * (and so on recursively).
 *
 * @param mappings Allows to map some JVM types with JS/TS types. This
 * can be used e.g. to map LocalDateTime to JS Date.
 *
 * @param classTransformers Special transformers for certain subclasses.
 * They allow to filter out some classes, customize what methods are
 * exported, how they names are generated and what types are generated.
 *
 * @param ignoreSuperclasses Classes and interfaces specified here will
 * not be emitted when they are used as superclasses or implemented
 * interfaces of a class.
 *
 * @param intTypeName Defines the name integer numbers will be emitted as.
 * By default it's number, but can be changed to int if the TypeScript
 * version used supports it or the user wants to be extra explicit.
 */
class TypeScriptGenerator(
    rootClasses: Iterable<KClass<*>>,
    private val mappings: Map<KClass<*>, String> = mapOf(),
    classTransformers: List<ClassTransformer> = listOf(DefaultTransformer(generateFunctions = false)),
    ignoreSuperclasses: Set<KClass<*>> = setOf(),
    private val intTypeName: String = "number",
    private val voidType: VoidType = VoidType.NULL
) {
    private val visitedClasses: MutableSet<KClass<*>> = java.util.HashSet()
    private val generatedDefinitions = mutableListOf<String>()
    private val pipeline = ClassTransformerPipeline(classTransformers)
    private val ignoredSuperclasses = setOf(
        Any::class,
        java.io.Serializable::class,
        Comparable::class
    ).plus(ignoreSuperclasses)

    init {
        rootClasses.forEach { visitClass(it) }
    }

    companion object {
        private val KotlinAnyOrNull = Any::class.createType(nullable = true)

        fun isJavaBeanProperty(kProperty: KProperty<*>, klass: KClass<*>): Boolean {
            val beanInfo = Introspector.getBeanInfo(klass.java)
            return beanInfo.propertyDescriptors
                .any { bean -> bean.name == kProperty.name }
        }
    }

    private fun visitClass(klass: KClass<*>) {
        if (klass !in visitedClasses) {
            visitedClasses.add(klass)

            generatedDefinitions.add(generateDefinition(klass))
        }
    }

    private fun formatClassType(type: KClass<*>): String {
        visitClass(type)
        return type.simpleName!!
    }
	
	private fun formatLambda(kType: KType): String {
		//last argument is return type
		var ret = "("
		val size = kType.arguments.size
		kType.arguments.withIndex().forEach{
			ret += if(it.index == size-1){
				") => "
				//ret+="): "
			} else { // adding parameter name
				val name = it.value.type?.findAnnotation<kotlin.ParameterName>()?.name ?: "par${it.index}"
				"$name: "
			}
			ret+=formatKType(it.value.type!!).formatWithoutParenthesis() //star projections will throw for NOW
			if(it.index < size-2){
				ret+=", "
			}
		}
		println(ret)
		
		
		return ret
	}

    private fun formatKType(kType: KType): TypeScriptType {
        val classifier = kType.classifier
        if (classifier is KClass<*>) {
			if (classifier.toString().startsWith("class kotlin.Function")){
				return TypeScriptType.single(formatLambda(kType), kType.isMarkedNullable, voidType)
			}
            val existingMapping = mappings[classifier]
            if (existingMapping != null) {
                return TypeScriptType.single(mappings[classifier]!!, kType.isMarkedNullable, voidType)
            }
        }

        val classifierTsType = when (classifier) {
            Boolean::class -> "boolean"
            String::class, Char::class -> "string"
            Int::class,
            Long::class,
            Short::class,
            Byte::class -> intTypeName
            Float::class, Double::class -> "number"
            Any::class -> "any"
            Unit::class -> "void"
            else -> {
                @Suppress("IfThenToElvis")
                if (classifier is KClass<*>) {
                    if (classifier.isSubclassOf(Iterable::class)
                        || classifier.javaObjectType.isArray)
                    {
                        // Use native JS array
                        // Parenthesis are needed to disambiguate complex cases,
                        // e.g. (Pair<string|null, int>|null)[]|null
                        val itemType = when (kType.classifier) {
                            // Native Java arrays... unfortunately simple array types like these
                            // are not mapped automatically into kotlin.Array<T> by kotlin-reflect :(
                            IntArray::class -> Int::class.createType(nullable = false)
                            ShortArray::class -> Short::class.createType(nullable = false)
                            ByteArray::class -> Byte::class.createType(nullable = false)
                            CharArray::class -> Char::class.createType(nullable = false)
                            LongArray::class -> Long::class.createType(nullable = false)
                            FloatArray::class -> Float::class.createType(nullable = false)
                            DoubleArray::class -> Double::class.createType(nullable = false)

                            // Class container types (they use generics)
                            else -> kType.arguments.single().type ?: KotlinAnyOrNull
                        }
                        "${formatKType(itemType).formatWithParenthesis()}[]"
                    } else if (classifier.isSubclassOf(Map::class)) {
                        // Use native JS associative object
                        val rawKeyType = kType.arguments[0].type ?: KotlinAnyOrNull
                        val keyType = formatKType(rawKeyType)
                        val valueType = formatKType(kType.arguments[1].type ?: KotlinAnyOrNull)
                        if ((rawKeyType.classifier as? KClass<*>)?.java?.isEnum == true)
                            "{ [key in ${keyType.formatWithoutParenthesis()}]: ${valueType.formatWithoutParenthesis()} }"
                        else
                            "{ [key: ${keyType.formatWithoutParenthesis()}]: ${valueType.formatWithoutParenthesis()} }"
                    } else {
                        // Use class name, with or without template parameters
                        formatClassType(classifier) + if (kType.arguments.isNotEmpty()) {
                            "<" + kType.arguments
                                .map { arg -> formatKType(arg.type ?: KotlinAnyOrNull).formatWithoutParenthesis() }
                                .joinToString(", ") + ">"
                        } else ""
                    }
                } else if (classifier is KTypeParameter) {
                    classifier.name
                } else {
                    "UNKNOWN" // giving up
                }
            }
        }

        return TypeScriptType.single(classifierTsType, kType.isMarkedNullable, voidType)
    }

    private fun generateEnum(klass: KClass<*>): String {
        return "type ${klass.simpleName} = ${klass.java.enumConstants
            .map { constant: Any ->
                constant.toString().toJSString()
            }
            .joinToString(" | ")
        };"
    }

    private fun generateInterface(klass: KClass<*>): String {
        val supertypes = klass.supertypes
            .filterNot { it.classifier in ignoredSuperclasses }
        val extendsString = if (supertypes.isNotEmpty()) {
            " extends " + supertypes
                .map { formatKType(it).formatWithoutParenthesis() }
                .joinToString(", ")
        } else ""

        val templateParameters = if (klass.typeParameters.isNotEmpty()) {
            "<" + klass.typeParameters
                .map { typeParameter ->
                    val bounds = typeParameter.upperBounds
                        .filter { it.classifier != Any::class }
                    typeParameter.name + if (bounds.isNotEmpty()) {
                        " extends " + bounds
                            .map { bound ->
                                formatKType(bound).formatWithoutParenthesis()
                            }
                            .joinToString(" & ")
                    } else {
                        ""
                    }
                }
                .joinToString(", ") + ">"
        } else {
            ""
        }



        return "interface ${klass.simpleName}$templateParameters$extendsString {\n" +
            klass.declaredMemberProperties
                .filter { !isFunctionType(it.returnType.javaType) }
                .filter {
                    it.visibility == KVisibility.PUBLIC || isJavaBeanProperty(it, klass)
                }
                .let { propertyList ->
                    pipeline.transformPropertyList(propertyList, klass)
                }
                .map { property ->
                    val propertyName = pipeline.transformPropertyName(property.name, property, klass)
                    val propertyType = pipeline.transformPropertyType(property.returnType, property, klass)

                    val formattedPropertyType = formatKType(propertyType).formatWithoutParenthesis()
                    "    $propertyName: $formattedPropertyType;\n"
                }
                .joinToString("") +
                //NOW function types are already filtered,
                // optional filter default methods of data class
                //NOW may also use deeper functions?
                pipeline.transformFunctionList(klass.declaredMemberFunctions,klass).joinToString("") {  generateMemberFunction(it) } +
            "}"
    }

    private fun generateMemberFunction(memFun: KCallable<*>): String{
        //Type mapping is provided by formatKType

        //first parameter is always self (only non-static functions are computed)
        val params= if(memFun.parameters.size>1){
            memFun.parameters.subList(1, memFun.parameters.size).joinToString(", ") { par ->
                "${par.name}: ${formatKType(par.type).formatWithoutParenthesis()}"
            }
        }else{""}
        //it.returnType.
        return "    ${memFun.name}(${params}): ${formatKType(memFun.returnType).formatWithoutParenthesis()};\n"
    }

    private fun isFunctionType(javaType: Type): Boolean {
        return javaType is KCallable<*>
            || javaType.typeName.startsWith("kotlin.jvm.functions.")
            || (javaType is ParameterizedType && isFunctionType(javaType.rawType))
    }

    private fun generateDefinition(klass: KClass<*>): String {
        return if (klass.java.isEnum) {
            generateEnum(klass)
        } else {
            generateInterface(klass)
        }
    }

    // Public API:
    val definitionsText: String
        get() = generatedDefinitions.joinToString("\n\n")

    val individualDefinitions: Set<String>
        get() = generatedDefinitions.toSet()
}