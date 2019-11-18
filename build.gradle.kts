import java.net.URI

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

group = "me.ntrrgc"
version = "1.1.2"

buildscript {

    repositories {
        mavenCentral()
    }
}

val kotlin_version = "1.3.50"
val spek_version = "1.1.5"
val junit_version = "1.0.2"
val junit5version = "5.2.0"

plugins{
    kotlin("jvm") version "1.3.50"
    id("maven-publish")
}


project.extensions.findByType(PublishingExtension::class.java)?.repositories {
    add(mavenLocal())
}


project.afterEvaluate {
    //println "distDir (info): "+project.info.distDir
    project.extensions.findByType(PublishingExtension::class.java)?.publications {
        register("dist", MavenPublication::class.java){
            from(project.components.getByName("java"))
        }
    }
}

//apply(plugin = "org.junit.platform.gradle.plugin")


/*junitPlatform {
    filters {
        engines {
            include "spek"
        }
    }
}*/

tasks.named("test", Test::class.java) {
    useJUnitPlatform() /*{
        includeEngines("spek")
    }*/
}

tasks.register<Jar>("sourcesJar"){
    dependsOn("classes")
    archiveClassifier.set("sources")
    from((project.property("sourceSets") as SourceSetContainer).named("main").get().allSource)
}

artifacts {
    this.add("implementation",project.tasks.named<Jar>("sourcesJar")){
        builtBy(project.tasks.named<Jar>("sourcesJar"))
    }
}

repositories {
    maven { url = URI("https://dl.bintray.com/jetbrains/spek") }
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin_version")

    testImplementation("com.winterbe:expekt:0.5.0")
    testImplementation("org.jetbrains.spek:spek-api:$spek_version")
    testImplementation("org.jetbrains.spek:spek-junit-platform-engine:$spek_version")
    testImplementation("org.junit.platform:junit-platform-launcher:$junit_version")
    testImplementation("com.google.code.findbugs:jsr305:3.0.1")
    testImplementation(kotlin("test-junit5"))


    //deps.put("junit-jupiter-api", mutableMapOf("group" to "org.junit.jupiter", "name" to "junit-jupiter-api", "version" to junit5version))
    //deps.put("jupiter-engine", mutableMapOf("group" to "org.junit.jupiter", "name" to "junit-jupiter-engine", "version" to junit5version))
    //deps.put("junit-jupiter-engine", mutableMapOf("group" to "org.junit.jupiter", "name" to "junit-jupiter-engine", "version" to junit5version))
    //deps.put("junit-jupiter-params", mutableMapOf("group" to "org.junit.jupiter", "name" to "junit-jupiter-params", "version" to junit5version))
    //deps.put("junit-vintage-engine", mutableMapOf("group" to "org.junit.vintage", "name" to "junit-vintage-engine", "version" to junit5version))
    //deps.put("junit-pioneer", mutableMapOf("group" to "org.junit-pioneer", "name" to "junit-pioneer", "version" to "0.3.0"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit5version")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junit5version")
}
