// 통합 스모크 검증 전용 모듈 — 3개 스타터를 한 컨텍스트에 조립해 auto-config 동시 기동을 확인한다.
// 라이브러리가 아니므로 발행하지 않는다(루트 build.gradle.kts의 발행 분기에서 hoji-smoke-test 제외).
dependencies {
    // 3개 스타터를 테스트 클래스패스로 조립 — 소비 서비스가 함께 끌어 쓰는 형태를 모사
    testImplementation(project(":hoji-common-core"))
    testImplementation(project(":hoji-common-jpa"))
    testImplementation(project(":hoji-common-security"))
    // 서블릿 웹 컨텍스트(@SpringBootTest MOCK) — core @ConditionalOnWebApplication / security @EnableWebSecurity 활성화에 필요
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    // jpa auto-config(@EnableJpaAuditing) 기동용 인메모리 DataSource — 단일 후보가 되어 SequenceRepository 빈도 등록
    testRuntimeOnly("com.h2database:h2")
    // spring-boot-starter-test / kotlin-test-junit5 는 루트 subprojects 블록에서 상속
}
