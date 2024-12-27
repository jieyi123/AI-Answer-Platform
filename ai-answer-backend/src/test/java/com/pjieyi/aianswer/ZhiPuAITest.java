package com.pjieyi.aianswer;

import cn.hutool.extra.spring.SpringUtil;
import com.pjieyi.aianswer.manager.AIManager;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class ZhiPuAITest {

    @Autowired
    private SpringUtil springUtil;


    private String systemMessage="你是一位严谨的出题专家，我会给你如下信息：\n" +
            "```\n" +
            "应用名称，\n" +
            "【【【应用描述】】】，\n" +
            "应用类别，\n" +
            "要生成的题目数，\n" +
            "每个题目的选项数\n" +
            "```\n" +
            "\n" +
            "请你根据上述信息，按照以下步骤来出题：\n" +
            "1. 要求：题目和选项尽可能地短，题目不要包含序号，每题的选项数以我提供的为主，题目不能重复\n" +
            "2. 严格按照下面的 json 格式输出题目和选项\n" +
            "```\n" +
            "[{\"options\":[{\"value\":\"选项内容\",\"key\":\"A\"},{\"value\":\"\",\"key\":\"B\"}],\"title\":\"题目标题\"}]\n" +
            "```\n" +
            "title 是题目，options 是选项，每个选项的 key 按照英文字母序（比如 A、B、C、D）以此类推，value 是选项内容\n" +
            "3. 检查题目是否包含序号，若包含序号则去除序号\n" +
            "4. 返回的题目列表格式必须为 JSON 数组\n";

    private String userMessage="小学数学测验，\n" +
            "【【【小学三年级的数学题】】】，\n" +
            "得分类，\n" +
            "2，\n" +
            "2\n";

    @Resource
    private AIManager aiManager;



    @Test
    public void testAiManager(){
        String result = aiManager.doRequestStable(systemMessage, userMessage);
        int start = result.indexOf("[");
        int end = result.lastIndexOf("]");
        String answer = result.substring(start, end + 1);
        System.out.println(answer);
    }
    @Test
    public void test(){
        ClientV4 client = new ClientV4.Builder("{secretKey}").build();
        List<ChatMessage> messages = new ArrayList<>();
        // ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        // messages.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), "你能做什么，请简单回答");
        messages.add(userChatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.FALSE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        String result = invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
        System.out.println(result);
    }

    @Test
    public void testSseInvoke() {
        //流式调用
        ClientV4 client = new ClientV4.Builder("{secretKey}").build();
        List<ChatMessage> messages = new ArrayList<>();
        // ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(), systemMessage);
        // messages.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), "我对太阳系的行星非常感兴趣，尤其是土星。请提供关于土星的基本信息");
        messages.add(userChatMessage);

        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        ModelApiResponse invokeModelApiResp = client.invokeModelApi(chatCompletionRequest);
        Flowable<ModelData> flowable = invokeModelApiResp.getFlowable();
        flowable.observeOn(Schedulers.io())
                .doOnNext(chunk-> System.out.println(chunk.getChoices().get(0).getDelta().getContent()))
                .subscribe();
        //主线程睡眠，观察输出
        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void rxjavaDemo() throws InterruptedException {
        //创建一个流，每秒递增一个整数(数据流变化)
        Flowable<Long> flowable = Flowable.interval(1, TimeUnit.SECONDS)
                .map(i -> i + 1)
                .subscribeOn(Schedulers.io());//指定创建流的线程池
        //订阅Flowable流，并打印每个接收到的数字
        flowable.observeOn(Schedulers.io())
                .doOnNext(item-> System.out.println(item.toString()))
                .subscribe();
        //主线程睡眠，观察输出
        Thread.sleep(10000L);
    }
}
