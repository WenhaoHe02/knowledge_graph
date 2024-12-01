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

    // 创建对话
    // 创建对话

    public boolean createConversation(int conversationId, String username, String question, String answer) {
        // 使用传入的 conversationId, username, question 和 answer 进行创建
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

    // 获取数据库中所有对话的数量
    public int getTotalConversationsCount() {
        String query = "MATCH (c:Conversation) RETURN COUNT(c) AS totalCount";

        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query);

            if (result.hasNext()) {
                // 获取查询结果并返回总数
                return result.single().get("totalCount").asInt();
            } else {
                return 0; // 如果没有找到任何对话，则返回 0
            }
        }
    }

    // 更新对话的

    // 查询某个用户的所有对话
    public List<ConversationDTO> getUserConversations(String username) {
        List<ConversationDTO> conversations = new ArrayList<>();

        // 更新查询，直接在 Conversation 节点中查询 username 属性
        String query = "MATCH (c:Conversation) WHERE c.username = $username RETURN c";
        try (Session session = Neo4jUtil.getSession()) {
            Result result = session.run(query, Values.parameters("username", username));

            if (!result.hasNext()) {
                System.out.println("No conversations found for user: " + username);
                return conversations;  // 如果没有对话，直接返回空列表
            }

            // 遍历查询结果，格式化每个对话的详细内容
            result.list().forEach(record -> {
                // 获取每个对话节点
                Map<String, Object> conversationData = record.get("c").asNode().asMap();

                // 输出调试信息
                System.out.println("Conversation Data: " + conversationData);

                // 获取 question 和 answer 字段
                String question = (String) conversationData.get("question");
                String answer = (String) conversationData.get("answer");

                System.out.println("Question: " + question + ", Answer: " + answer);

                // 封装到 ConversationDTO 中
                ConversationDTO conversationDTO = new ConversationDTO();
                conversationDTO.setConversationId(((Long) conversationData.get("conversationId")).intValue());

                conversationDTO.setUsername((String) conversationData.get("username"));
                conversationDTO.setQuestion(question);  // 设置 question
                conversationDTO.setAnswer(answer);      // 设置 answer

                // 将对话加入到列表中
                conversations.add(conversationDTO);
            });
        } catch (Exception e) {
            e.printStackTrace();  // 打印异常信息
        }

        return conversations;
    }


}