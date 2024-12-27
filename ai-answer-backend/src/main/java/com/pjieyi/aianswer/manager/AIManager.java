package com.pjieyi.aianswer.manager;

import com.pjieyi.aianswer.common.ErrorCode;
import com.pjieyi.aianswer.exception.BusinessException;
import com.zhipu.oapi.ClientV4;
import com.zhipu.oapi.Constants;
import com.zhipu.oapi.service.v4.model.*;
import io.reactivex.Flowable;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Component
public class AIManager {


    @Resource
    private ClientV4 clientV4;

    // 稳定的随机数
    private static final float STABLE_TEMPERATURE = 0.05f;

    //不稳定的随机数
    private static final float UNSTABLE_TEMPERATURE = 0.95f;



    /**
     * 不稳定流式调用
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @return 返回
     */
    public String doRequestStreamUnstable(String systemMessage,String userMessage){
        return doRequest(systemMessage,userMessage,Boolean.TRUE,UNSTABLE_TEMPERATURE);
    }

    /**
     * 不稳定调用(非流式)
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @return 返回
     */
    public String doRequestUnstable(String systemMessage,String userMessage){
        return doRequest(systemMessage,userMessage,Boolean.FALSE,UNSTABLE_TEMPERATURE);
    }

    /**
     * 稳定流式调用
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @return 返回
     */
    public String doRequestStreamStable(String systemMessage,String userMessage){
        return doRequest(systemMessage,userMessage,Boolean.TRUE,STABLE_TEMPERATURE);
    }

    /**
     * 稳定调用(非流式)
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @return 返回
     */
    public String doRequestStable(String systemMessage,String userMessage){
        return doRequest(systemMessage,userMessage,Boolean.FALSE,STABLE_TEMPERATURE);
    }

    /**
     * 默认调用
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @param temperature 稳定性
     * @return 回复
     */
    public String doRequest(String systemMessage,String userMessage,Float temperature){
        return doRequest(systemMessage,userMessage,Boolean.FALSE,temperature);
    }


    /**
     * 简化消息传递 流式调用
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @param temperature 稳定性
     * @return 流式返回
     */
    public Flowable<ModelData> doRequestStream(String systemMessage,String userMessage,Float temperature){
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),systemMessage);
        messages.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);
        return doRequestStream(messages,temperature);
    }

    /**
     * 通用流式调用请求
     * @param messages    消息列表
     * @param temperature 稳定性
     * @return 回复
     */
    public Flowable<ModelData> doRequestStream(List<ChatMessage> messages, Float temperature){
        //构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(Boolean.TRUE)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try{
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getFlowable();
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }
    }

    /**
     * 简化消息传递
     * @param systemMessage 系统消息
     * @param userMessage 用户消息
     * @param stream 流式调用
     * @param temperature 稳定性
     * @return 回复
     */
    public String doRequest(String systemMessage,String userMessage,Boolean stream,Float temperature){
        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage systemChatMessage = new ChatMessage(ChatMessageRole.SYSTEM.value(),systemMessage);
        messages.add(systemChatMessage);
        ChatMessage userChatMessage = new ChatMessage(ChatMessageRole.USER.value(), userMessage);
        messages.add(userChatMessage);
        return doRequest(messages,stream,temperature);
    }

    /**
     * 通用请求
     * @param messages 消息列表
     * @param stream 流式调用
     * @param temperature 稳定性
     * @return 回复
     */
    public String doRequest(List<ChatMessage> messages,Boolean stream,Float temperature){
        //构建请求
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model(Constants.ModelChatGLM4)
                .stream(stream)
                .temperature(temperature)
                .invokeMethod(Constants.invokeMethod)
                .messages(messages)
                .build();
        try{
            ModelApiResponse invokeModelApiResp = clientV4.invokeModelApi(chatCompletionRequest);
            return invokeModelApiResp.getData().getChoices().get(0).getMessage().getContent().toString();
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,e.getMessage());
        }
    }
}
