package com.chenhao.aicompanyhelper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chenhao.aicompanyhelper.persist.mapper")
public class AICompanyHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(AICompanyHelperApplication.class, args);
    }
}
