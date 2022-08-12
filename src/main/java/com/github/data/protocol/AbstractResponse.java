package com.github.data.protocol;

import com.github.data.protocol.types.Struct;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 18:22
 * @description
 */
public abstract class AbstractResponse extends AbstractRequestResponse{

    public abstract Struct toStruct();

}
