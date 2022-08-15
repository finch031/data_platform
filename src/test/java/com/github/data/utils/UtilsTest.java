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
        String pass = "2026@data";

        System.out.println(Utils.md5Hex(pass));

        pass = "2027@data";
        System.out.println(Utils.md5Hex(pass));

        pass = "2028@data";
        System.out.println(Utils.md5Hex(pass));
    }

    public static void main(String[] args){
        UtilsTest test = new UtilsTest();
        test.test01();
    }

}
