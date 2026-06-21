plugins {
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.spring") version "2.0.21" apply false
    kotlin("plugin.jpa") version "2.0.21" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

subprojects {
    group = "com.hoji"
    version = "0.1.0"

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
        // 발행물에 sources/javadoc jar 동반 (Maven 좌표 관례 충족 — java 컴포넌트에 등록되어
        // 아래 publication의 from(components["java"])가 자동 포함). Kotlin은 표준 javadoc을
        // 생성하지 않아 javadoc jar는 사실상 빈 jar이며, KDoc→HTML(Dokka)은 본 라이브러리 비범위.
        withSourcesJar()
        withJavadocJar()
    }

    // maven-publish 발행 확정: 라이브러리 모듈을 jar로 mavenLocal/원격에 발행한다.
    // from(components["java"])가 jar + sources + javadoc + (io.spring.dependency-management가
    // 보강하는) 의존성/버전을 POM에 기록한다. api(...) 의존성은 compile scope로 노출되어
    // 소비 서비스가 transitive로 확보한다(예: jpa → common-core).
    // 통합 스모크 모듈(hoji-smoke-test)은 검증 전용이라 publication을 등록하지 않는다(미발행 — HOJI-007 R3).
    if (name != "hoji-smoke-test") {
        extensions.configure<PublishingExtension> {
            publications {
                register<MavenPublication>("library") {
                    from(components["java"])
                    // 좌표(groupId/artifactId/version)는 project의 group·name·version에서 자동 도출.
                    pom {
                        name.set(project.name)
                        description.set("도메인 비결합 공통 빌딩블록 Spring Boot 3 스타터 모듈 (${project.name})")
                        url.set("https://github.com/wejsa/hoji-spring-boot-starter")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            }
                        }
                        developers {
                            developer {
                                id.set("hoji")
                                name.set("hoji")
                            }
                        }
                        scm {
                            url.set("https://github.com/wejsa/hoji-spring-boot-starter")
                            connection.set("scm:git:https://github.com/wejsa/hoji-spring-boot-starter.git")
                        }
                    }
                }
            }
        }
    }
}
