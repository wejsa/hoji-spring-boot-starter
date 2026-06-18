dependencies {
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework:spring-web")
    // 필터의 jakarta.servlet API (M2) — 런타임은 소비 서비스(서블릿 컨테이너)가 제공
    compileOnly("jakarta.servlet:jakarta.servlet-api")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    // 필터 단위 테스트의 spring-test mock 서블릿 객체용 jakarta.servlet API
    testImplementation("jakarta.servlet:jakarta.servlet-api")
}
