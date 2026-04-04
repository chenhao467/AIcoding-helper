package com.chenhao.aicompanyhelper.controller;

import com.chenhao.aicompanyhelper.ai.AiCompanyHelperService;
import jakarta.annotation.Resource;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/*
*功能：
 作者：chenhao
*日期： 2026/4/2 下午1:22
*/
@RestController
@RequestMapping("/ai")
public class AiController {
    @Resource
    private AiCompanyHelperService aiCompanyHelperService;

    @GetMapping("/chat")
    public Flux<ServerSentEvent<String>> chat(int memoryId,String message){
        return aiCompanyHelperService.chatStream(memoryId,message)
                .map(chunk->ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}
