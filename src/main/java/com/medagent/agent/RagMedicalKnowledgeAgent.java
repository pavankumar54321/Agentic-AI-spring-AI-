package com.medagent.agent;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.retriever.EmbeddingStoreRetriever;
import dev.langchain4j.retriever.Retriever;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RagMedicalKnowledgeAgent {
    private static final Logger log = LoggerFactory.getLogger(RagMedicalKnowledgeAgent.class);

    private Retriever<TextSegment> retriever;

    @PostConstruct
    public void init() {
        log.info("Initializing RAG Medical Knowledge Agent...");
        try {
            // Load the document
            Path documentPath = new ClassPathResource("first_aid_manual.txt").getFile().toPath();
            Document document = FileSystemDocumentLoader.loadDocument(documentPath, new TextDocumentParser());

            // Initialize the local embedding model and in-memory store
            EmbeddingModel embeddingModel = new AllMiniLmL6V2QuantizedEmbeddingModel();
            InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

            // Ingest the document into the store
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();
            ingestor.ingest(document);

            // Configure the retriever
            retriever = EmbeddingStoreRetriever.from(embeddingStore, embeddingModel, 2, 0.6);
            log.info("RAG Initialization complete.");
        } catch (Exception e) {
            log.error("Failed to initialize RAG agent", e);
        }
    }

    @Tool("Retrieves verified medical guidelines and first aid protocols. Use this tool whenever you need exact steps for medical interventions like CPR, treating severe bleeding, heart attacks, etc.")
    public String retrieveMedicalProtocol(String query) {
        log.info("RAG Query: {}", query);
        if (retriever == null) {
            return "Knowledge base unavailable. Rely on baseline medical knowledge but advise immediate professional help.";
        }

        List<TextSegment> relevantSegments = retriever.findRelevant(query);
        if (relevantSegments.isEmpty()) {
            return "No specific protocol found in the verified manual for: " + query;
        }

        return "VERIFIED MEDICAL PROTOCOL: " + 
               relevantSegments.stream()
                   .map(TextSegment::text)
                   .collect(Collectors.joining("\n---\n"));
    }
}
