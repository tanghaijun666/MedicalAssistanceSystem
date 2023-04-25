package com.cqupt.mas.utils;

import cn.hutool.core.util.IdUtil;
import com.cqupt.mas.constant.DenoisingType;
import com.cqupt.mas.entity.po.DicomFilePO;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.*;
import org.dcm4che3.imageio.codec.XPEGParser;
import org.dcm4che3.imageio.codec.jpeg.JPEGParser;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.tool.dcm2jpg.Dcm2Jpg;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
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
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author 唐海军
 * @create 2023-01-06 21:59
 */

@Slf4j
public class DenoisingUtil {


    private static final int BUFFER_SIZE = 8162;
    private static final byte[] buf = new byte[BUFFER_SIZE];
    private static final ElementDictionary DICT = ElementDictionary.getStandardElementDictionary();
    private static final int[] TYPE2_TAGS = {
            Tag.ContentDate,
            Tag.ContentTime
    };

    private static final String TEMP_FILE_PATH = getPath() + "/temp";


    private static final double[][] GAUSSIAN_KERNEL = {
            {0.0625, 0.125, 0.0625},
            {0.125, 0.25, 0.125},
            {0.0625, 0.125, 0.0625}
    };

    //均值滤波和中值滤波核的大小
    private static final int FILTER_SIZE = 3;

    static {
        File file = null;
        file = new File(TEMP_FILE_PATH);
        if (!file.exists()) {
            boolean mkdirs = file.mkdirs();
            if (!mkdirs) {
                log.error("未能成功创建目录来存储去噪时的中间文件，程序不能进行去噪操作");
            }
        }
    }

    public static String denoisingTool(DicomFilePO dicomFilePO, Integer type) throws Exception {
//        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        String srcFileName = IdUtil.simpleUUID();
        String tempFilePath = TEMP_FILE_PATH + "/" + srcFileName + ".dcm";
        byteToFile(tempFilePath, dicomFilePO.getContent().getData());
        File file = new File(tempFilePath);
        String destFilePath = TEMP_FILE_PATH + "/" + srcFileName + ".jpg";

        dcm2JpgByDcm4che(file, destFilePath);
        if (file.exists() && !file.delete()) {
            log.warn("去噪中间结果文件未能成功删除，可能会造成硬盘存储空间减少");
        }
        String destFilePath1 = TEMP_FILE_PATH + "/" + srcFileName + "1.jpg";
        if (type.equals(DenoisingType.GAUSSIAN_FILTER)) {
            gaussianFilter(destFilePath, destFilePath1);
        } else if (type.equals(DenoisingType.AVERAGE_FILTER)) {
            averageFilter(destFilePath, destFilePath1);
        } else if (type.equals(DenoisingType.MEDIAN_FILTER)) {
            medianFilter(destFilePath, destFilePath1);
        }

        convertJpg2Dcm(destFilePath1, tempFilePath, dicomFilePO, type);
        File file1 = new File(destFilePath);
        if (file1.exists() && file1.delete()) {
            log.warn("去噪中间结果文件未能成功删除，可能会造成硬盘存储空间减少");
        }
        File file2 = new File(destFilePath1);
        if (file2.exists() && file2.delete()) {
            log.warn("去噪中间结果文件未能成功删除，可能会造成硬盘存储空间减少");
        }
        return tempFilePath;
    }


    public static void gaussianFilter(String srcPath, String destPath) throws IOException {
        BufferedImage input = ImageIO.read(new File(srcPath));
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

        int width = input.getWidth();
        int height = input.getHeight();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                double red = 0;
                double green = 0;
                double blue = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        Color c = new Color(input.getRGB(x + i, y + j));
                        red += c.getRed() * GAUSSIAN_KERNEL[i + 1][j + 1];
                        green += c.getGreen() * GAUSSIAN_KERNEL[i + 1][j + 1];
                        blue += c.getBlue() * GAUSSIAN_KERNEL[i + 1][j + 1];
                    }
                }

                int r = (int) Math.round(red);
                int g = (int) Math.round(green);
                int b = (int) Math.round(blue);
                r = Math.min(r, 255);
                g = Math.min(g, 255);
                b = Math.min(b, 255);

                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        ImageIO.write(output, "jpg", new File(destPath));
    }

    public static void medianFilter(String srcPath, String destPath) throws IOException {
        BufferedImage input = ImageIO.read(new File(srcPath));
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

        int width = input.getWidth();
        int height = input.getHeight();

        for (int y = FILTER_SIZE / 2; y < height - FILTER_SIZE / 2; y++) {
            for (int x = FILTER_SIZE / 2; x < width - FILTER_SIZE / 2; x++) {

                ArrayList<Integer> reds = new ArrayList<>();
                ArrayList<Integer> greens = new ArrayList<>();
                ArrayList<Integer> blues = new ArrayList<>();

                for (int i = -FILTER_SIZE / 2; i <= FILTER_SIZE / 2; i++) {
                    for (int j = -FILTER_SIZE / 2; j <= FILTER_SIZE / 2; j++) {
                        Color c = new Color(input.getRGB(x + i, y + j));
                        reds.add(c.getRed());
                        greens.add(c.getGreen());
                        blues.add(c.getBlue());
                    }
                }

                int r = getMedian(reds);
                int g = getMedian(greens);
                int b = getMedian(blues);

                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        ImageIO.write(output, "jpg", new File(destPath));
    }

    private static int getMedian(ArrayList<Integer> values) {
        Collections.sort(values);
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        } else {
            return (values.get(middle - 1) + values.get(middle)) / 2;
        }
    }

    public static void averageFilter(String srcPath, String destPath) throws IOException {
        BufferedImage input = ImageIO.read(new File(srcPath));
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), input.getType());

        int width = input.getWidth();
        int height = input.getHeight();

        for (int y = FILTER_SIZE / 2; y < height - FILTER_SIZE / 2; y++) {
            for (int x = FILTER_SIZE / 2; x < width - FILTER_SIZE / 2; x++) {

                double red = 0;
                double green = 0;
                double blue = 0;

                for (int i = -FILTER_SIZE / 2; i <= FILTER_SIZE / 2; i++) {
                    for (int j = -FILTER_SIZE / 2; j <= FILTER_SIZE / 2; j++) {
                        Color c = new Color(input.getRGB(x + i, y + j));
                        red += c.getRed();
                        green += c.getGreen();
                        blue += c.getBlue();
                    }
                }

                int r = (int) Math.round(red / (FILTER_SIZE * FILTER_SIZE));
                int g = (int) Math.round(green / (FILTER_SIZE * FILTER_SIZE));
                int b = (int) Math.round(blue / (FILTER_SIZE * FILTER_SIZE));
                r = Math.min(r, 255);
                g = Math.min(g, 255);
                b = Math.min(b, 255);

                output.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }

        ImageIO.write(output, "jpg", new File(destPath));
    }

    public static void dcm2JpgByDcm4che(File dcmFile, String filePath) {

        Dcm2Jpg dcm2Jpg = new Dcm2Jpg();
        dcm2Jpg.initImageWriter("JPEG", null, "com.sun.imageio.plugins.*", null, 1.0);
        File imageFile = new File(filePath);
        try {
            dcm2Jpg.convert(dcmFile, imageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //获取项目所在路径
    public static String getPath() {
        String path = DenoisingUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("dows")) {
            path = path.substring(1);
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            return path.substring(0, path.lastIndexOf("/")).replaceFirst("file:", "");
        }
        return path.replace("target/classes/", "");
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
        }
    }

    /**
     * 将字节流转换成文件
     */
    public static void byteToFile(String filePath, byte[] data) throws Exception {
        if (data != null) {
            File file = new File(filePath);
            if (file.exists() && file.delete()) {
                log.warn("去噪中间结果文件未能成功删除，可能会造成硬盘存储空间减少");
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
