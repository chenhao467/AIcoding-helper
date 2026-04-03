package com.chenhao.aicodinghelper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.chenhao.aicodinghelper.persist.mapper")
public class AIcodingHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIcodingHelperApplication.class, args);
    }
}
