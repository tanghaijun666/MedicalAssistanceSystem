package com.cqupt.mas.utils;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.IdUtil;
import com.cqupt.mas.constant.DenoisingType;
import com.cqupt.mas.entity.po.DicomFilePO;
import org.dcm4che3.data.*;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.dcm2jpg.Dcm2Jpg;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author 唐海军
 * @create 2023-01-06 21:59
 */


public class DenoisingUtil {


    private static final int BUFFER_SIZE = 8162;
    private static byte[] buf = new byte[BUFFER_SIZE];
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static final int[] TYPE2_TAGS = {
            Tag.ContentDate,
            Tag.ContentTime
    };

    public static String denoisingTool(DicomFilePO dicomFilePO, Integer type) throws Exception {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String srcFileName = IdUtil.simpleUUID();
        String tempFilePath = new ClassPathResource("temp").getAbsolutePath() + "/" + srcFileName + ".dcm";
        byteToFile(tempFilePath, dicomFilePO.getContent().getData());
        File file = new File(tempFilePath);
        String destFilePath = new ClassPathResource("temp").getAbsolutePath() + "/" + srcFileName + ".jpg";

        dcm2JpgByDcm4che(file, destFilePath);
        if (file.exists()) {
            file.delete();
        }
        String destFilePath1 = new ClassPathResource("temp").getAbsolutePath() + "/" + srcFileName + "1.jpg";
        if (type.equals(DenoisingType.GAUSSIAN_FILTER)) {
            gaussianFilter(destFilePath, destFilePath1);
        } else if (type.equals(DenoisingType.AVERAGE_FILTER)) {
            averageFilter(destFilePath, destFilePath1);
        } else if (type.equals(DenoisingType.MEDIAN_FILTER)) {
            medianFilter(destFilePath, destFilePath1);
        }

        convertJpg2Dcm(destFilePath1, tempFilePath, dicomFilePO, type);
        File file1 = new File(destFilePath);
        if (file1.exists()) {
            file1.delete();
        }
        File file2 = new File(destFilePath1);
        if (file2.exists()) {
            file2.delete();
        }
        return tempFilePath;
    }

    public static void gaussianFilter(String srcPath, String destPath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = Imgcodecs.imread(srcPath);
        Mat dst = new Mat();
        Imgproc.GaussianBlur(src, dst, new Size(3, 3), 3, 4);
        Imgcodecs.imwrite(destPath, dst);
    }

    public static void medianFilter(String srcPath, String destPath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = Imgcodecs.imread(srcPath);
        Mat dst = new Mat();
        Imgproc.medianBlur(src, dst, 5);
        Imgcodecs.imwrite(destPath, dst);
    }

    public static void averageFilter(String srcPath, String destPath) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat src = Imgcodecs.imread(srcPath);
        Mat dst = new Mat();
        Imgproc.blur(src, dst, new Size(3, 3));
        Imgcodecs.imwrite(destPath, dst);
    }

    public static void dcm2JpgByDcm4che(File dcmFile, String filePath) {

        Dcm2Jpg dcm2Jpg = new Dcm2Jpg();
        dcm2Jpg.initImageWriter("JPEG", null, "com.sun.imageio.plugins.*", null, 1.0);
//            String imagePath = folderFile.getAbsolutePath() + "\\aa.jpg";
        File imageFile = new File(filePath);
        try {
            dcm2Jpg.convert(dcmFile, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
//            return imageFile;

    }

    public static void convert(Path srcFilePath, Path destFilePath, Attributes metaData) throws Exception {
        Attributes fileMetadata = new Attributes();
        fileMetadata.addAll(metaData);
        try (SeekableByteChannel channel = Files.newByteChannel(srcFilePath);
             DicomOutputStream dos = new DicomOutputStream(destFilePath.toFile())) {
            XPEGParser parser = new JPEGParser(channel);
            parser.getAttributes(fileMetadata);
            dos.writeDataset(fileMetadata.createFileMetaInformation(parser.getTransferSyntaxUID()), fileMetadata);
            dos.writeHeader(Tag.PixelData, VR.OB, -1);
            dos.writeHeader(Tag.Item, null, 0);
            copyPixelData(channel, parser.getCodeStreamPosition(), dos);
            dos.writeHeader(Tag.SequenceDelimitationItem, null, 0);
//            System.out.println("converted");
        }
    }

    /**
     * 将字节流转换成文件
     *
     * @param filePath 文件路径加文件名
     * @param data
     * @throws Exception
     */
    public static void byteToFile(String filePath, byte[] data) throws Exception {
        if (data != null) {
            String filepath = filePath;
            File file = new File(filepath);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data, 0, data.length);
            fos.flush();
            fos.close();
        }
    }

    private static void copyPixelData(SeekableByteChannel channel, long position, DicomOutputStream dos, byte... prefix)
            throws IOException {
        long codeStreamSize = channel.size() - position + prefix.length;
        dos.writeHeader(Tag.Item, null, (int) ((codeStreamSize + 1) & ~1));
        dos.write(prefix);
        channel.position(position);
        copy(channel, dos);
        if ((codeStreamSize & 1) != 0)
            dos.write(0);
    }

    private static void copy(ByteChannel in, OutputStream out) throws IOException {
        ByteBuffer bb = ByteBuffer.wrap(buf);
        int read;
        while ((read = in.read(bb)) > 0) {
            out.write(buf, 0, read);
            bb.clear();
        }
    }

    public static void convertJpg2Dcm(String srcPath, String destPath, DicomFilePO dicomFilePO, Integer type) throws Exception {

        // 根据需求加入dicom里的tag信息
        Attributes staticMetadata = new Attributes();

        // set ID
        staticMetadata.setString(Tag.StudyInstanceUID, VR.UI, dicomFilePO.getStudyInstanceUID());
        String seriesInstanceUID = dicomFilePO.getSeriesInstanceUID() + type.toString();
        staticMetadata.setString(Tag.SeriesInstanceUID, VR.UI, seriesInstanceUID);
        staticMetadata.setString(Tag.SOPInstanceUID, VR.UI, dicomFilePO.getSOPInstanceUID());

        // patient info
        setMetadata(staticMetadata, Tag.PatientName, dicomFilePO.getPatientName());
        setMetadata(staticMetadata, Tag.PatientID, dicomFilePO.getPatientId());
        setMetadata(staticMetadata, Tag.PatientSex, dicomFilePO.getPatientSex());
        setMetadata(staticMetadata, Tag.PatientAge, dicomFilePO.getPatientAge());

        // study info
        setMetadata(staticMetadata, Tag.StudyDate, dicomFilePO.getStudyDate());
        setMetadata(staticMetadata, Tag.StudyTime, dicomFilePO.getStudyTime());

        // series info
        setMetadata(staticMetadata, Tag.InstanceNumber, dicomFilePO.getInstanceNumber());
        setMetadata(staticMetadata, Tag.SeriesNumber, dicomFilePO.getSeriesNumber());


        setMetadata(staticMetadata, Tag.AccessionNumber, dicomFilePO.getAccessionNumber());
        setMetadata(staticMetadata, Tag.SOPClassUID, UID.SecondaryCaptureImageStorage);
        supplementType2Tags(staticMetadata);


        // convert jpeg files to dicom files
        Path src = Paths.get(srcPath);
        Path dest = Paths.get(destPath);
        convert(src, dest, staticMetadata);
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
