package com.knowledgegraph.application.repository;

import com.knowledgegraph.application.model.ConversationDTO;
import com.knowledgegraph.application.util.Neo4jUtil;
import org.neo4j.driver.Session;
import org.neo4j.driver.Result;
import org.neo4j.driver.Values;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConversationRepository {



    public boolean createConversation(int conversationId, String username, String question, String answer) {
        String query = "CREATE (c:Conversation {conversationId: $conversationId, username: $username, question: $question, answer: $answer})";
        try (Session session = Neo4jUtil.getSession()) {
            session.run(query, Values.parameters(
                    "conversationId", conversationId,
                    "username", username,
                    "question", question,
                    "answer", answer
            ));
        }
        System.out.println("Conversation created: id = " + conversationId + ", username = " + username);
        return true;
    }

    public int getTotalConversationsCount() {
        String query = "MATCH (c:Conversation) RETURN COUNT(c) AS totalCount";

        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query);

            if (result.hasNext()) {
                return result.single().get("totalCount").asInt();
            } else {
                return 0;
            }
        }
    }

    public List<ConversationDTO> getUserConversations(String username) {
        List<ConversationDTO> conversations = new ArrayList<>();

        String query = "MATCH (c:Conversation) WHERE c.username = $username RETURN c";
        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query, Values.parameters("username", username));

            if (!result.hasNext()) {
                System.out.println("No conversations found for user: " + username);
                return conversations;
            }

            result.list().forEach(record -> {
                Map<String, Object> conversationData = record.get("c").asNode().asMap();

                System.out.println("Conversation Data: " + conversationData);

                String question = (String) conversationData.get("question");
                String answer = (String) conversationData.get("answer");

                System.out.println("Question: " + question + ", Answer: " + answer);

                // 封装到 ConversationDTO 中
                ConversationDTO conversationDTO = new ConversationDTO();
                conversationDTO.setConversationId(((Long) conversationData.get("conversationId")).intValue());

                conversationDTO.setUsername((String) conversationData.get("username"));
                conversationDTO.setQuestion(question);
                conversationDTO.setAnswer(answer);

                conversations.add(conversationDTO);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conversations;
    }


}