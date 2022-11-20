package test;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author 唐海军
 * @create 2022-11-03 13:59
 */


public class Testbythj {

    @Test
    public void fileTest() throws IOException {
        File directory = new File("");//参数为空
        String courseFile = directory.getCanonicalPath() ;
        System.out.println(courseFile);
    }

}
