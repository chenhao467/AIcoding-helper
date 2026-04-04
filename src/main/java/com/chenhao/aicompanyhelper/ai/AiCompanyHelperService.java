package com.chenhao.aicompanyhelper.ai;


import com.chenhao.aicompanyhelper.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import reactor.core.publisher.Flux;

import java.util.List;

@InputGuardrails({SafeInputGuardrail.class})
public interface AiCompanyHelperService {
    @SystemMessage(fromResource = "system-prompt.txt")
    String chat(String userMessage);
    @SystemMessage(fromResource = "system-prompt.txt")
    Report chatForReport(String userMessage);


    record Report(String name, List<String> suggestionList,String suggester){};

    @SystemMessage(fromResource = "system-prompt.txt")
    Result<String> chatWithRag(String userMessage);

    @SystemMessage(fromResource = "system-prompt.txt")
    Flux<String> chatStream(@MemoryId int memoryId, @UserMessage String userMessage);
}
