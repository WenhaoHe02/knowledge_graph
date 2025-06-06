package org.example;

public class KnowledgePoint {
    private String id;
    private String name;
    private String content;
    public KnowledgePoint(){

    }
    public KnowledgePoint(String id, String name, String content) {
        this.id = id;
        this.name = name;
        this.content = content;
    }
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
