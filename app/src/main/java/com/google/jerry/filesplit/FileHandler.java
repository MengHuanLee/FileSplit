package com.google.jerry.filesplit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Jerry on 3/21/18.
 */

public class FileHandler {
    public static long chunkSize = (long)(1.4 * 1024);

    public static void split(String filename) throws IOException
    {
        // open the file
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

        // get the file length
        File f = new File(filename);
        long fileSize = f.length();

        // loop for each full chunk
        int subfile;
        for (subfile = 0; subfile < fileSize / chunkSize; subfile++)
        {
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));

            // write the right amount of bytes
            for (int currentByte = 0; currentByte < chunkSize; currentByte++)
            {
                // load one byte from the input file and write it to the output file
                out.write(in.read());
            }

            // close the file
            out.close();
        }

        // loop for the last chunk (which may be smaller than the chunk size)
        if (fileSize != chunkSize * (subfile - 1))
        {
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));

            // write the rest of the file
            int b;
            while ((b = in.read()) != -1)
                out.write(b);

            // close the file
            out.close();
        }

        // close the file
        in.close();
    }
}
