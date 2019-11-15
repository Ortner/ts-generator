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
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FunctionTests {

    val fctTrans: MutableList<ClassTransformer> = TypeScriptGenerator.defaultTransformers().toMutableList()
	init{
		fctTrans.removeIf {
			it is DefaultTransformer
		}
		fctTrans.add(0,DefaultTransformer(generateFunctions = true))
	}

    fun generateCode(klass: KClass<*>,
                            mappings: Map<KClass<*>, String> = mapOf(),
                            classTransformers: List<ClassTransformer> = fctTrans,
                            ignoreSuperclasses: Set<KClass<*>> = setOf(),
                            voidType: VoidType = VoidType.NULL
    ) : Set<TypeScriptDefinition>
    {
        val generator = TypeScriptGenerator(listOf(klass), mappings, classTransformers,
                ignoreSuperclasses, intTypeName = "int", voidType = voidType)

        val actual = generator.individualDefinitions
                .map(TypeScriptDefinitionFactory::fromCode)
                .toSet()
        return actual
    }

    inline fun generateCodeString(klass: KClass<*>,
                           mappings: Map<KClass<*>, String> = mapOf(),
                           classTransformers: List<ClassTransformer> = fctTrans,
                           ignoreSuperclasses: Set<KClass<*>> = setOf(),
                           voidType: VoidType = VoidType.NULL
    ) : String
    {
        return generateCode(klass, mappings, classTransformers, ignoreSuperclasses, voidType).joinToString("\n")
    }

    fun assertGeneratedCode(klass: KClass<*>,
                            expectedOutput: Set<String>,
                            mappings: Map<KClass<*>, String> = mapOf(),
                            classTransformers: List<ClassTransformer> = listOf(),
                            ignoreSuperclasses: Set<KClass<*>> = setOf(),
                            voidType: VoidType = VoidType.NULL)
    {
        val generator = TypeScriptGenerator(listOf(klass), mappings, classTransformers,
                ignoreSuperclasses, intTypeName = "int", voidType = voidType)

        val expected = expectedOutput
                .map(TypeScriptDefinitionFactory::fromCode)
                .toSet()
        val actual = generator.individualDefinitions
                .map(TypeScriptDefinitionFactory::fromCode)
                .toSet()

        actual.should.equal(expected)
    }


    @Test
    public fun testFunction(){
        class TestClassWithBasicFunctions{
            val hans: String =""
            fun noReturnVoidFct(){}
            fun retIntVoidFct(): Int?{return 0}
            fun retIntegerVoidFct(): Int{return 0}
            fun retIntegerStringFct(par: String): Int{return 0}
        }
        val out = generateCodeString(TestClassWithBasicFunctions::class)
        println(out)
        out.clearWS().should.equal("""
            interface TestClassWithBasicFunctions {
                hans: string;
                noReturnVoidFct(): void;
                retIntVoidFct(): int | null;
                retIntegerStringFct(par: string): int;
                retIntegerVoidFct(): int;
            }""".clearWS())
    }


    @Test
    public fun testPublicOnly(){
        open class TestVisibilityFunctions{
            val hans: String =""
            fun noReturnVoidFct(){}
            private fun privateFct(){}
            protected fun protectedFct(){}
        }
        val out = generateCodeString(TestVisibilityFunctions::class)
        println(out)
        assertFalse(out.contains("private"))
        assertFalse(out.contains("protected"))
    }

    @Test
    public fun testDataClass(){
        data class TestDataClass(val value: String =""){}
        var out = generateCodeString(TestDataClass::class)
        println("data enabled "+out)
        assertTrue(out.contains("component"))
        assertTrue(out.contains("copy"))
        assertTrue(out.contains("equals"))
        assertTrue(out.contains("hashCode"))
        assertTrue(out.contains("toString"))

        out = generateCodeString(TestDataClass::class, classTransformers = listOf(DefaultTransformer(
                generateFunctions = true,generateDataClassFunctions = false)) )
        println("data disabled "+out)
        assertFalse(out.contains("component"))
        assertFalse(out.contains("copy"))
        assertFalse(out.contains("equals"))
        assertFalse(out.contains("hashCode"))
        assertFalse(out.contains("toString"))

    }
	
	@Test
	public fun testLambda(){
		class TestClassWithLambdaParameterFunction{
			fun lambda1(listener: (oldVal: Int, newVal: Int) -> Unit) {}
			fun lambda2(listener: (oldVal: Any, newVal: Int) -> Unit) {}
			fun lambda3(listener: (Any, Int) -> Unit) {}
		}
		val out = generateCodeString(TestClassWithLambdaParameterFunction::class)
		println(out)
		out.clearWS().should.equal("""
			interface TestClassWithLambdaParameterFunction {
			    lambda1(listener: (oldVal: int, newVal: int) => void): void;
			    lambda2(listener: (oldVal: any, newVal: int) => void): void;
			    lambda3(listener: (par0: any, par1: int) => void): void;
			}""".clearWS())
	}
	
	
	@Test
	public fun filterReflectTest(){
		class TestClassWithLambdaKPropertyFunction{
			fun lambda1(listener: (oldVal: KProperty<Int>, newVal: Int) -> Unit) {}
		}
		val out = generateCodeString(TestClassWithLambdaKPropertyFunction::class)
		println(out)
		out.clearWS().should.equal("""
			interface TestClassWithLambdaKPropertyFunction {
			    lambda1(listener: (oldVal: any, newVal: int) => void): void;
			}""".clearWS())
	}
	
	//NOW test naming and type subst
	
	/**
	 * removes spaces, tabs and leading/trailing whitespace. New lines in between are preserved
	 */
	private fun String.clearWS(): String{
		return this.replace(" ","").replace("\t","").trim()
	}
	
}
