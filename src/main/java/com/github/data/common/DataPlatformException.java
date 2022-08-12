package com.github.data.common;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/10 14:32
 * @description
 */
public class DataPlatformException extends RuntimeException{
    private static final long serialVersionUID = 1362783929414550762L;

    public DataPlatformException(String message, Throwable cause){
        super(message,cause);
    }

    public DataPlatformException(String message){
        super(message);
    }

    public DataPlatformException(Throwable cause){
        super(cause);
    }

    public DataPlatformException(){
        super();
    }
}
