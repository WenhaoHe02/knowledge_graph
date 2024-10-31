package load;

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

    private static final Dotenv dotenv = Dotenv.load();
    private static final String API_SECRET_KEY = dotenv.get("LLM_API_SECRET_KEY");//.env文件中

    private  static String json_example =
            """
    {'edges': [{'data': {
            'label': 'label 1',
            'source': 'source 1',
            'target': 'target 1'}},
            {'data': {
            'label': 'label 2',
            'source': 'source 2',
            'target': 'target 2'}}
             ],
    'nodes': [{'data': {'id': '1', 'label': 'label 1'}},
            {'data': { 'id': '2', 'label': 'label 2'}},
            {'data': { 'id': '3', 'label': 'label 3'}}]}
    """;

    private static String __retriever_prompt =
            "You are an AI expert specializing in knowledge graph creation with the goal of capturing relationships based on a given input or request."+
            "Based on the user input in various forms such as paragraph, email, text files, and more."+
            "Your task is to create a knowledge graph based on the input."+
            "Nodes must have a label parameter. where the label is a direct word or phrase from the input."+
            "Edges must also have a label parameter, where the label is a direct word or phrase from the input."+
            "Response only with JSON in a format where we can jsonify in java and feed directly into  cy.add(data)"+
            "you can reference the given example:\n"+
            json_example+
            "Make sure the target and source of edges match an existing node."+
            "Do not include the markdown triple quotes above and below the JSON, jump straight into it with a curly bracket."+
            "Please use Chinese.Please replace source and target with id numbers from nodes."+
            "Please ensure that the relationship you provide is as detailed and comprehensive as possible";


    private static final ClientV4 client = new ClientV4.Builder(API_SECRET_KEY)
            .networkConfig(300, 100, 100, 100, TimeUnit.SECONDS)
            .connectionPool(new okhttp3.ConnectionPool(8, 1, TimeUnit.SECONDS))
            .build();

    public static void main(String[] args) throws Exception {
        System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
        //System.out.println(__retriever_prompt);
        String filepath="text.docx";
        String text=readDoc(filepath);
        getResponse(text);
    }

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
        } catch(Exception e){
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

    private static void getResponse(String text){
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), "你现在扮演信息抽取的角色，要求根据用户输入和AI的回答，正确提取出信息。"));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(),text ));
        messages.add(new ChatMessage(ChatMessageRole.USER.value(),__retriever_prompt ));

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
            String res=extractTextBetweenBackticks(responseBuilder.toString());
            //System.out.println(res);
            saveTojson(res);
        }

    }

}