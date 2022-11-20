package test;

import java.io.File;
import java.util.Scanner;

/**
 * @author 唐海军
 * @create 2022-11-08 9:10
 */


public class FileTest {
    public static int fileNum = 0;
    public static int directoryNum = 0;

    public static void show(File file) {
        File[] files = file.listFiles();
        if (null != files) {
            for (int i = 0; i < files.length; i++) {
                String result = files[i].isFile()? "一个文件": "一个目录";
                System.out.println(files[i] + "\t" + result);
                if ("一个目录".equals(result)) {
                    FileTest.directoryNum++;
                    show(files[i]);
                } else {
                    FileTest.fileNum++;
                }
            }
        }
    }

    public static void main(String[] args) {
//
//        System.out.println("请输入你想要查询的文件路径：");
//        Scanner sc = new Scanner(System.in);
//        // 如果文件路径名有空格会发生异常，需要使用 nextLine 去获取输入的内容
//        String pathName = sc.nextLine();
        String pathName ="D:/dicomfile";
        System.out.println(pathName);
        File file = new File(pathName);
        System.out.println("当前路径的文件是否存在：" + file.exists());
        System.out.println("文件名称：" + file.getName());
        System.out.println("文件的长度：" + file.length());
        System.out.println("文件最后一次修改的时间：" + file.lastModified());
        System.out.println("文件的绝对路径：" + file.getAbsolutePath());
        System.out.println("=============================");
        // 循环遍历当前目录下所有的文件，包括子目录中的所有文件
        show(file);
        System.out.println("=============================");
        System.out.println("总计：文件数量：" + FileTest.fileNum + "; 目录数量：" + FileTest.directoryNum);
        String[] split = file.getAbsolutePath().split( File.pathSeparator);
        for(String s:split){
            System.out.println(": "+s);
        }

        System.out.println(file.getAbsolutePath().indexOf(File.separatorChar));

        File file1 = new File("D:\\dicomfile\\LIDC-IDRI-0002\\20000101\\1.3.6.1.4.1.14519.5.2.1.6279.6001.490157381160200744295382098329\\1.3.6.1.4.1.14519.5.2.1.6279.6001.619372068417051974713149104919\\1.3.6.1.4.1.14519.5.2.1.6279.6001.306789031543980493707890947898.dcm");
        Integer sopstart=file1.getAbsolutePath().length()-68-1-64;
        System.out.println(file1.getAbsolutePath().substring(sopstart,sopstart+64) );
    }
}