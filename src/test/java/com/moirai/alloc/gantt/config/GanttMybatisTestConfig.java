package com.moirai.alloc.gantt.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration
@MapperScan("com.moirai.alloc.gantt.query.mapper")
public class GanttMybatisTestConfig {
}
