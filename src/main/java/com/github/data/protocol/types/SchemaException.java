package com.github.data.protocol.types;

import com.github.data.common.DataPlatformException;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 16:36
 * @description
 */
public class SchemaException extends DataPlatformException {
    private static final long serialVersionUID = -8742021213990052695L;

    public SchemaException(String message){
        super(message);
    }
}
