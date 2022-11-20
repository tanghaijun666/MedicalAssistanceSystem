package test;

/**
 * projectName: xxx
 * fileName: DisplayTag.java
 * packageName: com.xxxx.xxxx.common.util
 * date: 2018-03-26 10:07
 * copyright(c) 2017-2020 xxx公司
 */


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @version: V1.0
 * @author: fendo
 * @className: DisplayTag
 * @packageName: com.xxxx.xxxx.common.util
 * @description: Tag解析
 * @data: 2018-03-26 10:07
 **/
public final class DisplayTag {

    private static Attributes obj=null, object =null;
    private static  DicomInputStream din;
    private static double resultFactorDix;
    private String result = null;
    private Double result2 = null;
    private String nom = null;
    private String nounString = null;
    private int val2 = 0;
    private int valeurReturn;
    private String nounUnit = null;
    private static double resultFacteurDix	= 0;
    private Double valueSpatial = null;
    private String nounUnitRatio = null;
    private   DicomInputStream dis;
    private static final char[] HEX_DIGITS = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'A' , 'B' ,
            'C' , 'D' , 'E' , 'F'
    };
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private static ElementDictionary dict = ElementDictionary.getStandardElementDictionary();


    public DisplayTag(File file ){
        try {
            setObject(loadDicomObject(file) );
        } catch (IOException ex) {
            Logger.getLogger(DisplayTag.class.getName()).log(Level.SEVERE, null, ex);
        }


    }


    /**
     * Put attribut
     * @param obj
     */
    public  void setObject(Attributes obj){
        this.obj = obj;
    }


    /**
     * Giving attribut of metadata
     * @return
     */
    public static Attributes getObject(){
        return obj;

    }

    /**
     *  Read metadata of Dicom 3.0
     * @param f : input file
     * @return Attributes
     * @throws IOException
     */
    public static Attributes loadDicomObject(File f) throws IOException {
        if (f == null){
            return null;
        }else{
            DicomInputStream dis = new DicomInputStream(f);
            //attr.setSpecificCharacterSet("GBK");
            return dis.readDataset(-1, -1);
        }
    }
    public static Attributes loadDicomObject(InputStream inputStream) {
        if (inputStream == null) {
            return null;
        } else {
            DicomInputStream dis = null;
            Attributes attrs = null;
            try {
                dis = new DicomInputStream(inputStream);
                attrs = dis.readDataset(-1, -1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //attr.setSpecificCharacterSet("GBK");
            return attrs;
        }
    }
    public static String getAttribute(Attributes attrs, Integer tag) {
        byte[] bytesex;
        String res = null;
        try {
            bytesex = attrs.getBytes(tag);
            res = new String(bytesex, "gb18030");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        File file = new File("C:\\Users\\彼此\\Desktop\\医疗辅助系统\\image-data-dicom\\LIDC-IDRI-0001\\01-01-2000-30178\\3000566.000000-03192\\1-006.dcm");
        DisplayTag d = new DisplayTag(file);
//        @SuppressWarnings("static-access")
//        Attributes attrs = d.loadDicomObject(file);
        Attributes attrs = d.loadDicomObject(new FileInputStream(file));
        //输出所有属性信息
        System.out.println("所有信息: " + attrs);

        //获取行
        int row = attrs.getInt(Tag.Rows, 1);
        //获取列
        int columns = attrs.getInt(Tag.Columns, 1);
        //窗宽窗位
        float win_center = attrs.getFloat(Tag.WindowCenter, 1);
        float win_width = attrs.getFloat(Tag.WindowWidth, 1);

        System.out.println("" + "row=" + row + ",columns=" + row + ",row*columns = " + row * columns);

        String patientName = attrs.getString(Tag.PatientName, "");
        System.out.println("姓名：" + patientName);


        //描述时间
        String studyData = attrs.getString(Tag.StudyDate, "");
        System.out.println("描述时间：" + studyData);

        byte[] bytename = attrs.getBytes(Tag.PatientName);
        System.out.println("姓名: " + new String(bytename,"gb18030"));
        byte[] bytesex = attrs.getBytes(Tag.PatientSex);
        System.out.println("性别: " + new String(bytesex,"gb18030"));
        byte[] bodyPartExamined=attrs.getBytes(Tag.BodyPartExamined);
        System.out.println(new String(bodyPartExamined));
        byte[] modality = attrs.getBytes(Tag.Modality);
        System.out.println(new String(modality,"gb18030"));
        byte[] patientId = attrs.getBytes(Tag.PatientID);
        System.out.println(new String(patientId,"gb18030"));
        System.out.println("patientId: "+getAttribute(attrs,Tag.PatientID));
        byte[] instance = attrs.getBytes(Tag.InstanceNumber);
        System.out.println(new String(instance,"gb18030"));

    }

}