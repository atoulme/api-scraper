import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.30"
    id("org.mikeneck.graalvm-native-image") version "v1.4.0"
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

repositories {
    mavenCentral()
}

apply(plugin ="com.github.johnrengelman.shadow")

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation(platform("org.apache.camel:camel-bom:3.13.0"))
    implementation("org.apache.camel:camel-core")
    implementation("org.apache.camel:camel-atom")
    implementation("org.apache.camel:camel-infinispan-embedded")
    implementation("org.apache.camel:camel-splunk-hec")
    implementation("org.apache.tuweni:tuweni-config:2.2.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("ch.qos.logback:logback-core:1.2.11")
    implementation("org.infinispan:infinispan-core:13.0.8.Final")
    implementation("org.infinispan:infinispan-commons:13.0.8.Final")
    implementation("org.infinispan:infinispan-cachestore-rocksdb:13.0.8.Final")
}

tasks{
    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes(Pair("Main-Class", "com.toulme.scraper.MainKt"))
        }
    }
}

nativeImage {
    dependsOn("shadowJar")
    mainClass ="com.toulme.scraper.MainKt" // Deprecated, use `buildType.executable.main` as follows instead.
    buildType { build ->
        build.executable(main = "com.toulme.scraper.MainKt")
    }
    classpath = tasks.get("shadowJar").outputs.files
    executableName = "api-scraper"
    outputDirectory = file("$buildDir/executable")
    arguments(
            "--no-fallback",
            "--allow-incomplete-classpath",
            "-H:IncludeResources=META-INF/services/*.*",
            "-H:+UseServiceLoaderFeature",
            "--enable-url-protocols=https"
    )
}

repositories {
    mavenCentral()
}

tasks.withType<Wrapper> {
  gradleVersion = "7.3.1"
  distributionType = Wrapper.DistributionType.BIN
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "11"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
