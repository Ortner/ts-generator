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

import kotlin.test.Test





class TestClassWithCompanion{
	fun lambda1(listener: (oldVal: Int, newVal: Int) -> Unit) {}
	fun lambda2(listener: (oldVal: Any, newVal: Int) -> Unit) {}
	fun lambda3(listener: (Any, Int) -> Unit) {}
	companion object{
		fun companionFunction():String = ""
	}
}
class CompanionTests: BaseTest(){
	
	@Test
	fun generateCompanionTest(){
		checkCodeEquals(TestClassWithCompanion.Companion::class, """
			interface TestClassWithCompanion${'$'}Companion {
				companionFunction(): string;
			}""")
	}
	
}
