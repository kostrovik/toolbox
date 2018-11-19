package com.github.kostrovik.toolbox.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * project: toolbox
 * author:  kostrovik
 * date:    2018-11-16
 * github:  https://github.com/kostrovik/toolbox
 */
public class ZipUtils {
    private static final int BUFFER_SIZE = 4096;

    public void extract(ZipInputStream source, Path destination) throws IOException {
        File destDir = new File(destination.toUri());
        if (!destDir.exists()) {
            throw new NotDirectoryException(destination.toString());
        }

        ZipEntry entry = source.getNextEntry();
        while (entry != null) {
            String filePath = destination.toString() + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                extractFile(source, filePath);
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            source.closeEntry();
            entry = source.getNextEntry();
        }
        source.close();
    }

    private void extractFile(ZipInputStream zipInputStream, String filePath) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] bytesIn = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = zipInputStream.read(bytesIn)) != -1) {
                bos.write(bytesIn, 0, read);
            }
        }
    }
}