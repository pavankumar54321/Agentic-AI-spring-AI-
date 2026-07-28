package com.medagent.agent;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RagMedicalKnowledgeAgent {
    private static final Logger log = LoggerFactory.getLogger(RagMedicalKnowledgeAgent.class);

    private final SimpleVectorStore vectorStore;

    @Autowired
    public RagMedicalKnowledgeAgent(EmbeddingModel embeddingModel) {
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
    }

    @PostConstruct
    public void init() {
        log.info("Initializing RAG Medical Knowledge Agent...");
        try {
            Resource resource = new ClassPathResource("first_aid_manual.txt");
            TextReader textReader = new TextReader(resource);
            List<Document> documents = textReader.get();
            
            TokenTextSplitter textSplitter = new TokenTextSplitter();
            List<Document> splitDocuments = textSplitter.apply(documents);
            
            vectorStore.add(splitDocuments);
            log.info("RAG Initialization complete.");
        } catch (Exception e) {
            log.error("Failed to initialize RAG agent", e);
        }
    }

    @Tool(description = "Retrieves verified medical guidelines and first aid protocols. Use this tool whenever you need exact steps for medical interventions like CPR, treating severe bleeding, heart attacks, etc.")
    public String retrieveMedicalProtocol(String query) {
        log.info("RAG Query: {}", query);
        if (vectorStore == null) {
            return "Knowledge base unavailable. Rely on baseline medical knowledge but advise immediate professional help.";
        }

        List<Document> relevantDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(2).similarityThreshold(0.6).build()
        );
        
        if (relevantDocuments.isEmpty()) {
            return "No specific protocol found in the verified manual for: " + query;
        }

        return "VERIFIED MEDICAL PROTOCOL: " + 
               relevantDocuments.stream()
                   .map(Document::getText)
                   .collect(Collectors.joining("\n---\n"));
    }
}
