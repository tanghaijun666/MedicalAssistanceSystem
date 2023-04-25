package com.cqupt.mas.utils;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public final class DisplayTagUtil {

    private static Attributes obj = null;


    public DisplayTagUtil(File file) throws IOException {
        setObject(loadDicomObject(file));
    }


    /**
     * Put attribut
     */
    public void setObject(Attributes obj) {
        DisplayTagUtil.obj = obj;
    }


    /**
     * Giving attribut of metadata
     */
    public static Attributes getObject() {
        return obj;

    }

    /**
     * Read metadata of Dicom 3.0
     *
     */
    public static Attributes loadDicomObject(File f) throws IOException {
        if (f == null) {
            return null;
        } else {
           try( DicomInputStream dis = new DicomInputStream(f)){
               return dis.readDataset(-1, -1);
           }
        }
    }

    public static Attributes loadDicomObject(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        } else {
            DicomInputStream dis ;
            Attributes attrs;
            dis = new DicomInputStream(inputStream);
            attrs = dis.readDataset(-1, -1);
            return attrs;
        }
    }

    public static String getAttribute(Attributes attrs, Integer tag) {
        byte[] bytesex;
        String res = null;
        try {
            bytesex = attrs.getBytes(tag) != null ? attrs.getBytes(tag) : new byte[0];
            res = new String(bytesex, "gb18030");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }


}