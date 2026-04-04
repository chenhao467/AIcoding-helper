package com.chenhao.aicompanyhelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/*
*功能：加载rag
 作者：chenhao
*日期： 2026/4/1 下午8:01
*/
@Configuration
public class RagConfig {
    @Resource
    private EmbeddingModel qwenEmbeddingModel;
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;
    @Bean
    public ContentRetriever contentRetriever(){
        //1.加载文档
        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src/main/resources/docs");
        //2.文档切割，每个文档按照段落进行分隔，最大1000个字符，每次最多重叠200个字符（重叠是为了使切片上下文联系，防止因分片内容断联）
        DocumentByParagraphSplitter documentByParagraphSplitter = new DocumentByParagraphSplitter(1000, 200);
        //3.自定义文档加载器，把文档转换成向量并保存到向量数据库中。
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentByParagraphSplitter)
                // 为了提高文档质量，为每个切割后的文档切片 TextSegement 添加文档名称作为元信息
                .textSegmentTransformer(textSegment ->
                        TextSegment.from(textSegment.metadata().getString("file_name") +
                                "\n" + textSegment.text(), textSegment.metadata()))
                //使用的向量模型
                .embeddingModel(qwenEmbeddingModel)
                //保存到向量存储里
                .embeddingStore(embeddingStore)
                .build();
        //加载文档
        ingestor.ingest(documents);
        //4.自定义内容加载器
        EmbeddingStoreContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(5)//最多5条结果
                .minScore(0.75) //过滤掉分数小于0.75的结果
                .build();
        return  contentRetriever;
    }
}
