package test;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author 唐海军
 * @create 2022-11-03 14:00
 */


public class ImageDemo {

    public static void main(String args[]) {

        getData("C:\\Users\\彼此\\Desktop\\各种文档\\" +
                "医疗辅助系统\\image-data-dicom\\LIDC-IDRI-0002\\" +
                "01-01-2000-98329\\3000522.000000-04919\\1-001.dcm");
    }

    /**
     * 读取dicom文件字节流数据看看
     */
    private static void getData(String filePath) {
        System.out.println("解析文件：" + filePath);
        File file = new File(filePath);
        try {
            FileInputStream is = new FileInputStream(file);
            //跳过128个字节
            is.read(new byte[128]);

            //读取4个字节，要把这四个字节转变成字符串才能看到“DICM”
            byte[] buf = new byte[4];
            is.read(buf);
            String msg_DCM = new String(buf);
            System.out.println("跳过128后面的四个字节,字节1：" + buf[0] + "，字节2：" + buf[1] + "，字节3：" + buf[2] + "，字节4：" + buf[3]);
            System.out.println("跳过128后面的四个字节组成的字符串：" + msg_DCM);
            System.out.println(" ");

            //获取第一个tag的四个字节
            is.read(buf);
            String msg_Tag = new String(buf);
            System.out.println("Tag的四个字节,字节1：" + buf[0] + "，字节2：" + buf[1] + "，字节3：" + buf[2] + "，字节4：" + buf[3]);
            System.out.println("Tag"+msg_Tag);
            System.out.println(" ");

            //获取第一个VR的两个字节
            byte[] buf2 = new byte[2];
            is.read(buf2);
            String msg_VR = new String(buf2);
            System.out.println("VR的两个字节,字节1：" + buf2[0] + "，字节2：" + buf2[1]);
            System.out.println("VR的两个字节组成的字符串：" + msg_VR);
            System.out.println(" ");

            //获取第一个VL的四个字节
            is.read(buf);
            String msg_VL = new String(buf);
            System.out.println("VL的四个字节,字节1：" + buf[0] + "，字节2：" + buf[1] + "，字节3：" + buf[2] + "，字节4：" + buf[3]);
            System.out.println("VL的四个字节组成的字符串：" + msg_VL);
            System.out.println(" ");

            //获取第一个VF的四个字节
            is.read(buf);
            String msg_VF = new String(buf);
            System.out.println("VF的四个字节,字节1：" + buf[0] + "，字节2：" + buf[1] + "，字节3：" + buf[2] + "，字节4：" + buf[3]);
            System.out.println("VF的四个字节组成的字符串：" + msg_VF);
            System.out.println(" ");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
