package test;

/**
 * @author 唐海军
 * @create 2023-01-06 21:46
 */


import org.dcm4che3.data.*;
import org.dcm4che3.util.UIDUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DicomService {

    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static final int[] TYPE2_TAGS = {
            Tag.ContentDate,
            Tag.ContentTime
    };

    public static void convertJpg2Dcm(String srcPath, String destPath) throws Exception {

        // 根据需求加入dicom里的tag信息
        Attributes staticMetadata = new Attributes();

        // set ID
        staticMetadata.setString(Tag.StudyInstanceUID, VR.UI, UIDUtils.createUID());
        staticMetadata.setString(Tag.SeriesInstanceUID, VR.UI, UIDUtils.createUID());
        staticMetadata.setString(Tag.SOPInstanceUID, VR.UI, UIDUtils.createUID());

        // patient info
        setMetadata(staticMetadata, Tag.PatientName, "Test Patient");
        setMetadata(staticMetadata, Tag.PatientID, "Test Patient");
        setMetadata(staticMetadata, Tag.PatientSex, "M");
        setMetadata(staticMetadata, Tag.PatientAge, "50");
        setMetadata(staticMetadata, Tag.PatientBirthDate, "19500101");

        // study info
        setMetadata(staticMetadata, Tag.StudyDate, "20100101");
        setMetadata(staticMetadata, Tag.StudyTime, "123");
        setMetadata(staticMetadata, Tag.StudyDescription, "Study Description");
        setMetadata(staticMetadata, Tag.StudyID, "123");

        // series info
        setMetadata(staticMetadata, Tag.SeriesDate, "20100101");
        setMetadata(staticMetadata, Tag.SeriesTime, "123");

        setMetadata(staticMetadata, Tag.SOPClassUID, UID.SecondaryCaptureImageStorage);
        supplementType2Tags(staticMetadata);


        // convert jpeg files to dicom files
        Path src = Paths.get(srcPath);
        Path dest = Paths.get(destPath);
        Jpg2DcmUtil.convert(src, dest, staticMetadata);
    }

    private static void setMetadata(Attributes metadata, int tag, String value) {
//        if (!metadata.containsValue(tag))
        if (value != null)
            metadata.setString(tag, DICT.vrOf(tag), value);
    }

    private static void supplementType2Tags(Attributes metadata) {
        for (int tag : TYPE2_TAGS)
            if (!metadata.contains(tag))
                metadata.setNull(tag, DICT.vrOf(tag));
    }
}
