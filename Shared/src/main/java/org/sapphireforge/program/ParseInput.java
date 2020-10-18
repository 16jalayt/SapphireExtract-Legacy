package org.sapphireforge.program;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

//icon from http://www.iconarchive.com/show/flatastic-7-icons-by-custom-icon-design/Extract-object-icon.html

public class ParseInput
{
    public static File infile;
    public static File outfile;
    public static File outdir;
    public static RandomAccessFile inStream;
    public static FileOutputStream outStream;
    public static String inputWithExtension;
    public static String inputWithoutExtension;
    public static String inputExtension;
    public static String inputPath;
    public static String inputFull;
    public static String separator;
    //Usable by programs
    public static Scanner user;


    public static boolean overwriteAll = false;
    public static boolean autoRename = false;
    //TODO: REPLACE WITH LOGGER
    public static boolean verbose = false;
    public static boolean raw = false;

    public static void parseFile(File inputFile)
    {
        user = new Scanner(System.in);
        infile = inputFile;
        System.out.println(inputFile);

        try
        {
            if (!infile.exists())
            {
                System.out.println("File doesnt exist");
                return;
            }

            //TODO: input handle ioexception?
            inputFull = infile.getCanonicalPath();
            separator = System.getProperty("file.separator");
            //inputPath = args[0].substring(0, args[0].lastIndexOf(separator)+1);
            inputPath = infile.getAbsolutePath().substring(0, infile.getAbsolutePath().lastIndexOf(separator) + 1);
            inputExtension = inputFull.substring(inputFull.lastIndexOf("."));
            inputWithExtension = inputFull.substring(inputFull.lastIndexOf(separator) + 1);
            inputWithoutExtension = inputWithExtension.substring(0, inputWithExtension.lastIndexOf("."));

            inStream = new RandomAccessFile(infile, "r");
            DetectExtension.DetectExt(inStream);
            inStream.close();
            if (outStream != null)
                outStream.close();
            System.out.println("Done");
        } catch (IOException e)
        {
            System.out.println("Error: " + e);
            System.exit(-1);
        }
    }


}