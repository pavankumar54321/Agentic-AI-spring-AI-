package com.medagent.agent;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.client.ChatClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MultilingualTranslationAgent {
    private static final Logger log = LoggerFactory.getLogger(MultilingualTranslationAgent.class);

    private final ChatClient chatClient;

    public MultilingualTranslationAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Tool(description = "Translates text to a specific language with medical and cultural context. Use this tool if you need to actively translate medical terms or instructions into the user's requested language.")
    public String translateText(String textToTranslate, String targetLanguage) {
        log.info("Translating text to {}: {}", targetLanguage, textToTranslate);

        String prompt = "You are a professional medical translator. Translate the following text into " + targetLanguage + ":\n" +
                "\"" + textToTranslate + "\"\n" +
                "Ensure that complex medical terminology is translated into simple, culturally appropriate terms that a layperson can easily understand. " +
                "Output ONLY the translated text in the native script.";

        try {
            return chatClient.prompt(prompt).call().content();
        } catch (Exception e) {
            log.error("Failed to translate text", e);
            return "Translation Error. Please proceed in English or simple terminology.";
        }
    }
    
    @Tool(description = "Provides cultural context and confirms language formatting for translations. Call this tool to get rules before formatting the final 7-section response in a foreign language.")
    public String getTranslationGuidelines(String targetLanguage) {
        return "Translation Guidelines for " + targetLanguage + ":\n" +
               "1. Use simple, understandable terminology for low-literacy users.\n" +
               "2. Maintain the strict 7-section formatting, but translate the section headers into " + targetLanguage + ".\n" +
               "3. Use the native script.";
    }
}
