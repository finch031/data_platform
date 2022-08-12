package com.github.data.utils;

// import org.junit.jupiter.api.Test;

/**
 * @author yusheng
 * @version 1.0.0
 * @datetime 2022/8/11 20:22
 * @description
 */
public class UtilsTest {

    // @Test
    public void test01(){
        String pass = "2020@data";

        System.out.println(Utils.md5Hex(pass));

        pass = "2021@data";
        System.out.println(Utils.md5Hex(pass));

        pass = "2022@data";
        System.out.println(Utils.md5Hex(pass));
    }

}
