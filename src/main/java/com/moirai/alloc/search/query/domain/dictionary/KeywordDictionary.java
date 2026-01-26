package com.moirai.alloc.search.query.domain.dictionary;

import com.moirai.alloc.search.query.domain.vocabulary.Tech;
import com.moirai.alloc.search.query.domain.vocabulary.JobRole;
import com.moirai.alloc.search.query.domain.vocabulary.ExperienceDomain;
import com.moirai.alloc.search.query.domain.vocabulary.SeniorityLevel;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

public final class KeywordDictionary {

    private KeywordDictionary() {}

    public static final Map<JobRole, List<String>> JOB_ROLE_KEYWORDS = Map.ofEntries(
            entry(JobRole.BACKEND_ENGINEER, List.of("백엔드", "backend", "서버개발")),
            entry(JobRole.FRONTEND_ENGINEER, List.of("프론트엔드", "frontend", "웹프론트")),
            entry(JobRole.FULLSTACK_ENGINEER, List.of("풀스택", "fullstack")),
            entry(JobRole.INFRA_ENGINEER, List.of("인프라", "infra", "서버", "시스템")),
            entry(JobRole.DEVOPS_ENGINEER, List.of("데브옵스", "devops")),
            entry(JobRole.PLATFORM_ENGINEER, List.of("플랫폼")),
            entry(JobRole.SRE, List.of("sre", "사이트신뢰성")),
            entry(JobRole.DATA_ENGINEER, List.of("데이터엔지니어", "data engineer")),
            entry(JobRole.ML_ENGINEER, List.of("ml엔지니어", "머신러닝")),
            entry(JobRole.AI_ENGINEER, List.of("ai엔지니어", "인공지능")),
            entry(JobRole.CLOUD_ENGINEER, List.of("클라우드")),
            entry(JobRole.MOBILE_ENGINEER, List.of("모바일")),
            entry(JobRole.ANDROID_ENGINEER, List.of("안드로이드", "android")),
            entry(JobRole.IOS_ENGINEER, List.of("ios", "아이폰")),
            entry(JobRole.QA_ENGINEER, List.of("qa", "테스트")),
            entry(JobRole.SECURITY_ENGINEER, List.of("보안", "security")),
            entry(JobRole.PRODUCT_MANAGER, List.of("pm", "프로덕트매니저")),
            entry(JobRole.IT_SUPPORT, List.of("it지원", "it지원부서", "it서포트"))
    );

    public static final Map<Tech, List<String>> TECH_KEYWORDS = Map.ofEntries(
            entry(Tech.JAVA, List.of("java", "자바")),
            entry(Tech.KOTLIN, List.of("kotlin")),
            entry(Tech.PYTHON, List.of("python", "파이썬")),
            entry(Tech.JAVASCRIPT, List.of("javascript", "js")),
            entry(Tech.TYPESCRIPT, List.of("typescript", "ts")),

            entry(Tech.SPRING, List.of("spring", "스프링")),
            entry(Tech.SPRING_BOOT, List.of("spring boot", "springboot", "스프링 부트", "스프링붓트", "스프링부트")),

            entry(Tech.NODEJS, List.of("node", "nodejs", "노드", "노드 제이에스")),
            entry(Tech.NESTJS, List.of("nestjs", "네스트", "네스트 제이에스")),
            entry(Tech.DJANGO, List.of("django", "장고")),
            entry(Tech.FASTAPI, List.of("fastapi", "패스트에이피아이", "패스트API")),

            entry(Tech.REACT, List.of("react", "리액트")),
            entry(Tech.VUE, List.of("vue", "뷰")),
            entry(Tech.ANGULAR, List.of("angular")),

            entry(Tech.MYSQL, List.of("mysql", "마이 에스큐엘")),
            entry(Tech.MARIADB, List.of("mariadb", "마리아디비")),
            entry(Tech.POSTGRESQL, List.of("postgres", "postgresql", "포스트그래")),
            entry(Tech.MONGODB, List.of("mongodb", "몽고디비")),
            entry(Tech.REDIS, List.of("redis", "래디스")),

            entry(Tech.AWS, List.of("aws", "아마존")),
            entry(Tech.AZURE, List.of("azure")),
            entry(Tech.GCP, List.of("gcp")),
            entry(Tech.DOCKER, List.of("docker", "도커")),
            entry(Tech.KUBERNETES, List.of("kubernetes", "k8s", "쿠버네티스")),

            entry(Tech.ELASTICSEARCH, List.of("elasticsearch", "엘라스틱서치")),
            entry(Tech.OPENSEARCH, List.of("opensearch", "오픈서치")),

            entry(Tech.KAFKA, List.of("kafka", "카프카")),
            entry(Tech.SPARK, List.of("spark")),

            entry(Tech.JENKINS, List.of("jenkins", "젠킨스")),
            entry(Tech.GITHUB_ACTIONS, List.of("github actions", "gha", "깃허브 액션"))
    );

    public static final Map<ExperienceDomain, List<String>> EXPERIENCE_DOMAIN_KEYWORDS = Map.ofEntries(
            entry(ExperienceDomain.FINANCE, List.of("금융", "핀테크", "금융it")),
            entry(ExperienceDomain.PUBLIC, List.of("공공", "공공기관")),
            entry(ExperienceDomain.MANUFACTURING, List.of("제조", "공장")),
            entry(ExperienceDomain.RETAIL, List.of("커머스", "이커머스", "쇼핑몰")),
            entry(ExperienceDomain.LOGISTICS, List.of("물류")),
            entry(ExperienceDomain.ERP, List.of("erp")),
            entry(ExperienceDomain.CLOUD_MIGRATION, List.of("클라우드이관", "클라우드마이그레이션")),
            entry(ExperienceDomain.MSA, List.of("msa", "마이크로서비스")),
            entry(ExperienceDomain.DATA_PLATFORM, List.of("데이터플랫폼")),
            entry(ExperienceDomain.SEARCH_PLATFORM, List.of("검색시스템")),
            entry(ExperienceDomain.AI_SERVICE, List.of("ai서비스", "ai플랫폼")),
            entry(ExperienceDomain.SECURITY_PLATFORM, List.of("보안플랫폼"))
    );

    public static final Map<SeniorityLevel, List<String>> SENIORITY_KEYWORDS = Map.ofEntries(
            entry(SeniorityLevel.JUNIOR, List.of("인턴","주니어", "신입", "사원")),
            entry(SeniorityLevel.MIDDLE, List.of("미들", "주임","중급", "대리", "과장")),
            entry(SeniorityLevel.SENIOR, List.of("시니어", "과장", "차장", "부장", "임원"))
    );
}
