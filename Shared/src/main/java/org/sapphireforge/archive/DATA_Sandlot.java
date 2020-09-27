package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class DATA_Sandlot
{
    public static void DATAextract(RandomAccessFile inStream)throws IOException
    {
        inStream.seek(4);
        int numfiles = Helpers.readIntLittleEndian("# files:", inStream);

        long tableOffset = inStream.getFilePointer();

        for(int i=0; i<numfiles; i++)
        {
            inStream.seek(tableOffset);

            //filename is a null termed string. hard to do in java
            //TODO: make into helper?
            StringBuilder builder = new StringBuilder();
            byte c=-1;
            while(c != 0)
            {
                c = inStream.readByte();
                builder.append((char)c);
            }
            String fileName = builder.toString();
            fileName = fileName.substring(0,fileName.length()-1);
            System.out.println(fileName);

            int fileOffset = Helpers.readIntLittleEndian("File offset:", inStream);
            int fileLen = Helpers.readIntLittleEndian("File Len:", inStream);

            tableOffset = inStream.getFilePointer();
            //go to start of file
            inStream.seek(fileOffset);

            byte[] fileout = new byte[fileLen];
            inStream.read(fileout);

            Output.OutSetup(fileName,"");
            ParseInput.outStream.write(fileout);
            ParseInput.outStream.close();
        }
    }
    public static void ZDATAextract(RandomAccessFile inStream)throws IOException
    {
        inStream.seek(0);
        //CONTENTS ARE COMPRSSED. gzip?

        //with full apache
        /*byte[] compresseddata = new byte[(int)inStream.length()];
        inStream.read(compresseddata);
        InputStream in = new ByteArrayInputStream(compresseddata);
        try (GzipCompressorInputStream gzipin = new GzipCompressorInputStream(in)){
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            IOUtils.copy(gzipin, output);
            Output.OutSetup("test.tmp","");
            Main.outStream.write(output.toByteArray());
        }*/

        //java gzip with apache copy
        /*ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] compresseddata = new byte[(int)inStream.length()];
        inStream.read(compresseddata);
        //decompressGzipFile(compresseddata,"test2.tmp");
        try{
            IOUtils.copy(new GZIPInputStream(new ByteArrayInputStream(compresseddata)), out);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        Output.OutSetup("test.tmp","");
        Main.outStream.write(out.toByteArray());*/

        //pure java
        try {
            FileInputStream fis = new FileInputStream(ParseInput.infile);
            GZIPInputStream gis = new GZIPInputStream(fis);
            File fi = new File(ParseInput.inputFull+".tmp");
            FileOutputStream fos = new FileOutputStream(fi);
            byte[] buffer = new byte[1024];
            int len;
            while((len = gis.read(buffer)) != -1){
                fos.write(buffer, 0, len);
            }

            RandomAccessFile newinStream = new RandomAccessFile(fi, "r");
            DATAextract(newinStream);

            //close resources
            fos.close();
            gis.close();
            newinStream.close();
            fi.delete();
        }
        catch (IOException e)
        {
            System.out.println("Could not extract the file: "+ ParseInput.inputWithExtension);
        }


    }

}
