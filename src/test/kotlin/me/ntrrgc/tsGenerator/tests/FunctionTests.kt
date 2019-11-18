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

import me.ntrrgc.tsGenerator.ClassTransformer
import me.ntrrgc.tsGenerator.DefaultTransformer
import org.junit.jupiter.api.Test
import kotlin.reflect.KCallable
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FunctionTests: BaseTest() {



    @Test
    public fun testFunction(){
        class TestClassWithBasicFunctions{
            val hans: String =""
            fun noReturnVoidFct(){}
            fun retIntVoidFct(): Int?{return 0}
            fun retIntegerVoidFct(): Int{return 0}
            fun retIntegerStringFct(par: String): Int{return 0}
        }
       checkCodeEquals(TestClassWithBasicFunctions::class, """
            interface TestClassWithBasicFunctions {
                hans: string;
                noReturnVoidFct(): void;
                retIntVoidFct(): int | null;
                retIntegerStringFct(par: string): int;
                retIntegerVoidFct(): int;
            }""")
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
        out = generateCodeString(TestDataClass::class, baseTransformer = DefaultTransformer(
                generateFunctions = true,generateDataClassFunctions = false) )
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
		checkCodeEquals(TestClassWithLambdaParameterFunction::class, """
			interface TestClassWithLambdaParameterFunction {
			    lambda1(listener: (oldVal: int, newVal: int) => void): void;
			    lambda2(listener: (oldVal: any, newVal: int) => void): void;
			    lambda3(listener: (par0: any, par1: int) => void): void;
			}""")
	}
	
	
	@Test
	public fun filterReflectTest(){
		class TestClassWithLambdaKPropertyFunction{
			fun lambda1(listener: (oldVal: KProperty<Int>, newVal: Int) -> Unit) {}
		}
		checkCodeEquals(TestClassWithLambdaKPropertyFunction::class, """
			interface TestClassWithLambdaKPropertyFunction {
			    lambda1(listener: (oldVal: any, newVal: int) => void): void;
			}""")
	}
	
	@Test
	public fun genericFctTest(){
		class TestClass{
			fun <T> generic(listener: T): T = listener
		}
		checkCodeEquals(TestClass::class, """
			interface TestClass {
				generic<T>(listener: T): T;
			}""")
	}
	
	@Test
	public fun renameMethodTest(){
		class TestClassWithBasicFunctions{
			fun retIntegerStringFct(par: String): Int{return 0}
		}
		checkCodeEquals(TestClassWithBasicFunctions::class,"""
            interface TestClassWithBasicFunctions {
                retintegerstringfct(par: string): int;
            }""", classTransformers = listOf(object: ClassTransformer{
			override fun transformFunctionName(name: String, fct: KCallable<*>, klass: KClass<*>): String {
				return name.toLowerCase()
			}
		}))
	}
	@Test
	public fun renameParamTest(){
		class TestClassWithBasicFunctions{
			fun retIntegerStringFct(par: String): Int{return 0}
		}
		checkCodeEquals(TestClassWithBasicFunctions::class,"""
            interface TestClassWithBasicFunctions {
                retIntegerStringFct(PAR: string): int;
            }""", classTransformers = listOf(object: ClassTransformer{
			override fun transformParameterName(name: String, fct: KCallable<*>, klass: KClass<*>): String {
				return name.toUpperCase()
			}
		}))
	}
	@Test
	public fun substTypeTest(){
		class TestClassWithBasicFunctions{
			fun retIntegerStringFct(par: String): Int{return 0}
		}
		checkCodeEquals(TestClassWithBasicFunctions::class,"""
            interface TestClassWithBasicFunctions {
                retIntegerStringFct(par: any): any;
            }""", classTransformers = listOf(object: ClassTransformer{
			override fun transformFctType(type: KType, fct: KCallable<*>, klass: KClass<*>): KType {
				return Any::class.createType()
			}
		}))
	}
	
	
}
