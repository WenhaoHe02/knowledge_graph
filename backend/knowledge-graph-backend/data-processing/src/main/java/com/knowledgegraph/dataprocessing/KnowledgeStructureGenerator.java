package com.knowledgegraph.dataprocessing;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;

import io.github.cdimascio.dotenv.Dotenv;
import io.reactivex.Flowable;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KnowledgeStructureGenerator {
    private KnowledgeStructureGenerator() {
        // private constructor to prevent instantiation
    }

    private static final Dotenv dotenv = Dotenv.configure().directory("./data-processing").load();
    private static final String API_SECRET_KEY = dotenv.get("LLM_API_SECRET_KEY");//.env文件中

    private static String json_example =
            """
            {
                'edges': [
                    {
                        'data': {
                            'label': '包含', // 关系类型：包含
                            'source': '1', // 主题的节点ID
                            'target': '2'  // 一级知识点的节点ID
                        }
                    },
                    {
                        'data': {
                            'label': '关联', // 关系类型：关联
                            'source': '2', // 一级知识点的节点ID
                            'target': '3'  // 关联知识点的节点ID
                        }
                    }
                ],
                'nodes': [
                    {
                        'data': {
                            'id': '1',
                            'label': '软件项目管理', // 节点名称
                            'type': '主题',         // 节点类型
                            'description': '关于软件项目管理的总体主题' // 主题描述
                        }
                    },
                    {
                        'data': {
                            'id': '2',
                            'label': '项目计划',   // 节点名称
                            'type': '一级知识点',  // 节点类型
                            'pre_requisite': [],   // 前置知识点，空数组表示无前置要求
                            'post_requisite': ['4'], // 后置知识点
                            'related_points': ['3'],  // 关联知识点
                            'tags': ['重点'],       // 标签
                            'cognitive_dimension': '理解运用', // 认知维度
                            'teaching_objective': '掌握项目计划的基本步骤', // 分类教学目标
                            'content': '项目计划的详细介绍和步骤', // 知识点内容
                            'exercises': [
                                {
                                    'question': '项目计划的基本步骤有哪些？',
                                    'answer': '包括需求分析、时间安排、风险评估等。',
                                    'common_mistakes': '混淆风险评估与需求分析'
                                }
                            ]
                        }
                    },
                    {
                        'data': {
                            'id': '3',
                            'label': '风险管理',  // 节点名称
                            'type': '一级知识点', // 节点类型
                            'pre_requisite': ['2'],  // 前置知识点
                            'post_requisite': [],    // 后置知识点
                            'related_points': ['2'], // 关联知识点
                            'tags': ['难点'],       // 标签
                            'cognitive_dimension': '理解运用', // 认知维度
                            'teaching_objective': '掌握风险管理的原则和方法', // 分类教学目标
                            'content': '风险管理的定义、方法和作用' // 知识点内容
                        }
                    },
                    {
                        'data': {
                            'id': '4',
                            'label': '进度控制',  // 二级知识点的例子
                            'type': '二级知识点',
                            'pre_requisite': ['2'], // 前置知识点
                            'post_requisite': [],   // 后置知识点
                            'related_points': [],   // 无关联知识点
                            'tags': ['重点'],       // 标签
                            'cognitive_dimension': '记忆', // 认知维度
                            'teaching_objective': '理解进度控制的重要性', // 分类教学目标
                            'content': '进度控制的基本概念和管理方法' // 知识点内容
                        }
                    }
                ]
            }
            """;


    private static String __retriever_prompt =
            "You are an AI expert specializing in knowledge graph creation for software project management, capturing relationships and attributes based on a given input." +
                    "Based on user input provided in various forms (e.g., paragraphs, emails, text files), your task is to generate a knowledge graph." +
                    "The knowledge graph should be organized around the following node types with hierarchical and relational structures:" +
                    "\n- '主题' (Theme): highest-level category that may contain multiple knowledge points." +
                    "\n- '一级知识点' (Level-1 Knowledge Point): a major knowledge area under a theme, which may contain further sub-knowledge points." +
                    "\n- '二级知识点' (Level-2 Knowledge Point) and additional nested knowledge levels as required." +

                    "Each node must include relevant attributes such as the following:" +
                    "\n- 前置知识点 (Pre-requisite Knowledge Point): knowledge points that should be learned before this one." +
                    "\n- 后置知识点 (Subsequent Knowledge Point): knowledge points that build on this one." +
                    "\n- 关联知识点 (Related Knowledge Points): other relevant knowledge points for reference." +
                    "\n- 标签 (Tags): indicators such as '难点' (Difficulty), '重点' (Key Point)." +
                    "\n- 认知维度 (Cognitive Dimension): indicates the learning goal, such as '记忆' (Memory), '理解运用' (Understanding and Application), '了解' (Awareness), etc." +
                    "\n- 分类教学目标 (Categorized Teaching Objective): specific teaching goals tied to this knowledge point." +
                    "\n- 知识点详细内容 (Detailed Knowledge Content): a description or explanation of this knowledge point." +
                    "\n- 习题 (Exercises): related practice questions. Each exercise should include attributes like '答案' (Answer) and '易错点' (Common Mistakes)." +

                    "Define relationships (edges) with the following types:" +
                    "\n- 包含 (Contains): used when a theme contains a knowledge point or when a knowledge point includes sub-points." +
                    "\n- 关联 (Related To): used when knowledge points are conceptually or practically related, or when they reference associated exercises." +

                    "Nodes and edges must each include a 'label' parameter, which is a direct word or phrase from the input." +
                    "Only respond with JSON in a format that can be directly parsed in Java and passed to 'cy.add(data)'. Refer to the following example for structure:\n" +
                    json_example +
                    "\nEnsure that the 'target' and 'source' of each edge correspond to an existing node ID." +
                    "Do not include any markdown triple quotes around the JSON, and ensure the output starts with a curly bracket." +
                    "Please provide the output in Chinese, replacing 'source' and 'target' with node IDs." +
                    "Provide detailed and comprehensive relationships in your response.";


    private static final ClientV4 client = new ClientV4.Builder(API_SECRET_KEY)
            .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
            .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
            .build();

    public static Flowable<ChatMessageAccumulator> mapStreamToAccumulator(Flowable<ModelData> flowable) {
        return flowable.map(chunk -> {
            return new ChatMessageAccumulator(chunk.getChoices().get(0).getDelta(), null, chunk.getChoices().get(0), chunk.getUsage(), chunk.getCreated(), chunk.getId());
        });
    }

    public static String readDoc(String path) throws IOException {
        String resullt = "";
        //首先判断文件中的是doc/docx
        try {
            if (path.endsWith(".doc")) {
                InputStream is = new FileInputStream(new File(path));
                WordExtractor re = new WordExtractor(is);
                resullt = re.getText();
                re.close();
            } else if (path.endsWith(".docx")) {
                OPCPackage opcPackage = POIXMLDocument.openPackage(path);
                POIXMLTextExtractor extractor = new XWPFWordExtractor(opcPackage);
                resullt = extractor.getText();
                extractor.close();
            } else {
                System.out.println("此文件不是word文件");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resullt;
    }


    public static String extractTextBetweenBackticks(String input) {
        // 正则表达式匹配三个反引号之间的文本
        Pattern pattern = Pattern.compile("```(.*?)```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // 提取文本并去掉 "json"
            String jsonString = matcher.group(1).trim();
            return jsonString.replace("json", "").trim(); // 去掉 "json" 字符串
        }

        return null; // 如果没有找到，返回null
    }

    private static void saveTojson(String jsonData) {
        // 创建 ObjectMapper
        JSONObject jsonObject = JSONObject.parseObject(jsonData);

        // 指定输出文件路径
        String filePath = "output.json";

        // 写入 JSON 文件
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(jsonObject.toString(SerializerFeature.PrettyFormat)); // 使用 4 个空格进行缩进
            System.out.println("JSON 文件已保存到 " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getResponse(String text) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "你现在扮演信息抽取的角色，要求根据用户输入和AI的回答，正确提取出信息。"));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), text));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(), __retriever_prompt));

        // 函数调用参数构建部分
        List<ChatTool> chatToolList = new ArrayList<>();
        ChatTool chatTool = new ChatTool();
        chatTool.setType(ChatToolType.FUNCTION.value());
        ChatFunctionParameters chatFunctionParameters = new ChatFunctionParameters();
        chatFunctionParameters.setType("object");

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .messages(messages)
                .tools(chatToolList)
                .toolChoice("auto")
                .build();

        ModelApiResponse sseModelApiResp = client.invokeModelApi(chatCompletionRequest);
        StringBuilder responseBuilder = new StringBuilder();
        if (sseModelApiResp.isSuccess()) {
            AtomicBoolean isFirst = new AtomicBoolean(true);
            ChatMessageAccumulator chatMessageAccumulator = mapStreamToAccumulator(sseModelApiResp.getFlowable())
                    .doOnNext(accumulator -> {
                        {
                            if (accumulator.getDelta() != null && accumulator.getDelta().getContent() != null) {
                                responseBuilder.append(accumulator.getDelta().getContent());
                                System.out.print(accumulator.getDelta().getContent());
                            }
                        }
                    })
                    .doOnComplete(System.out::println)
                    .lastElement()
                    .blockingGet();
            String res = extractTextBetweenBackticks(responseBuilder.toString());
            //System.out.println(res);
            saveTojson(res);
        }

    }

}