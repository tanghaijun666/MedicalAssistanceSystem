package com.cqupt.mas.utils;


import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomEncodingOptions;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class DisplayTagUtil {

    private static Attributes obj = null, object = null;
    private DicomEncodingOptions encOpts = DicomEncodingOptions.DEFAULT;
    private static ElementDictionary dict = ElementDictionary.getStandardElementDictionary();


    public DisplayTagUtil(File file) throws IOException {
        setObject(loadDicomObject(file));
    }


    /**
     * Put attribut
     *
     * @param obj
     */
    public void setObject(Attributes obj) {
        this.obj = obj;
    }


    /**
     * Giving attribut of metadata
     *
     * @return
     */
    public static Attributes getObject() {
        return obj;

    }

    /**
     * Read metadata of Dicom 3.0
     *
     * @param f : input file
     * @return Attributes
     * @throws IOException
     */
    public static Attributes loadDicomObject(File f) throws IOException {
        if (f == null) {
            return null;
        } else {
            DicomInputStream dis = null;
            Attributes attrs = null;
            dis = new DicomInputStream(f);
            attrs = dis.readDataset(-1, -1);
            //attr.setSpecificCharacterSet("GBK");
            return attrs;
        }
    }

    public static Attributes loadDicomObject(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        } else {
            DicomInputStream dis = null;
            Attributes attrs = null;
            dis = new DicomInputStream(inputStream);
            attrs = dis.readDataset(-1, -1);
            //attr.setSpecificCharacterSet("GBK");
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