dependencies {
    // @RestControllerAdvice, ResponseEntity
    api("org.springframework:spring-web")
    // Pageable / Sort
    api("org.springframework.data:spring-data-commons")
    // zip4j 2.x (M5: 1.x → 2.x)
    api("net.lingala.zip4j:zip4j:2.11.5")
    // auto-configuration 애너테이션 (런타임 의존 아님)
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
