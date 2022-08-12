package com.github.data.protocol;

import com.github.data.common.DataPlatformException;
import com.github.data.protocol.request.HeartBeatRequest;
import com.github.data.protocol.request.LoginRequest;
import com.github.data.protocol.request.MessageRequest;
import com.github.data.protocol.types.Struct;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 14:50
 * @description
 */
public abstract class AbstractRequest extends AbstractRequestResponse{

    public AbstractRequest() {
    }

    public abstract Struct toStruct();

    public static AbstractRequest parseRequest(ApiKeys apiKeys,Struct struct){

        switch (apiKeys){
            case LOGIN:
                return new LoginRequest(struct);
            case HEART_BEAT:
                return new HeartBeatRequest(struct);
            case MESSAGE:
                return new MessageRequest(struct);

            default:
                throw new DataPlatformException("Unsupported Request.");
        }
    }

}
