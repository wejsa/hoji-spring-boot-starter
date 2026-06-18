plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.spring") version "2.0.21" apply false
    kotlin("plugin.jpa") version "2.0.21" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    group = "com.hoji"
    version = "0.1.0-SNAPSHOT"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    repositories { mavenCentral() }

    // Spring Boot BOM으로 버전 일괄 관리 (boot 플러그인은 미적용 — 라이브러리 jar 배포)
    the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
        imports { mavenBom("org.springframework.boot:spring-boot-dependencies:3.4.1") }
    }

    dependencies {
        "implementation"(kotlin("stdlib-jdk8"))
        "implementation"("org.jetbrains.kotlin:kotlin-reflect")
        "annotationProcessor"("org.springframework.boot:spring-boot-configuration-processor")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testImplementation"("org.jetbrains.kotlin:kotlin-test-junit5")
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
    tasks.withType<Test> { useJUnitPlatform() }

    extensions.configure<JavaPluginExtension> {
        toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
        withSourcesJar()
    }
}
