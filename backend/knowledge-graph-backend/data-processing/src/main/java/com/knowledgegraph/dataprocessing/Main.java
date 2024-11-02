package com.knowledgegraph.dataprocessing;
public class Main {
    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        //System.out.println(__retriever_prompt);
        String filepath="text.docx";
        String text = KnowledgeStructureGenerator.readDoc(filepath);
        KnowledgeStructureGenerator.getResponse(text);
    }
}