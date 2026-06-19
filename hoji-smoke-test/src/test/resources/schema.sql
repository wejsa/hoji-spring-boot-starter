-- 통합 스모크 행위 검증용 시퀀스 — Spring Boot 임베디드 DB 초기화가 기동 시 자동 실행
-- (spring.sql.init.mode=embedded 기본). StarterBehaviorSmokeTest가 이 시퀀스로
-- OracleSequenceRepository의 트랜잭션 프록시 + 채번 SQL 왕복을 실제 호출해 회귀 고정한다.
CREATE SEQUENCE IF NOT EXISTS HOJI_SMOKE_SEQ START WITH 1 INCREMENT BY 1;
