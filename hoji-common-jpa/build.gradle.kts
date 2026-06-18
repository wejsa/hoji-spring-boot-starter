plugins {
    // @Entity / @MappedSuperclass 등에 no-arg/all-open 적용 (버전은 루트에서 관리)
    kotlin("plugin.jpa")
}

dependencies {
    // core의 Base64 유틸 재사용
    api(project(":hoji-common-core"))
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    // OracleSequenceMaxValueIncrementer (표준 javax.sql.DataSource 의존)
    api("org.springframework:spring-jdbc")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
}
