package org.sapphireforge.program;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.compressors.lzma.LZMACompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.util.Arrays;
import java.util.zip.ZipEntry;


public class DecompressionManager 
{
	
	public static byte[] decompressLZMAStream(byte[] contentBytes)
	{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            IOUtils.copy(new LZMACompressorInputStream(new ByteArrayInputStream(contentBytes)), out);
        } catch(IOException e){
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
	
	public static boolean unzip() throws FileNotFoundException, IOException
	{
		try
		{
			File inputFile = new File(ParseInput.inputFull);
	        final String OUTPUT_FOLDER = ParseInput.inputPath + ParseInput.inputWithoutExtension;
	 
	        InputStream is = new FileInputStream(inputFile);
			BufferedInputStream buffedInputStream = new BufferedInputStream(is);
	        ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream(buffedInputStream);
	        ZipEntry entry = null;
	        
	        boolean overwrite = false;

	        while ((entry = (ZipArchiveEntry) ais.getNextEntry()) != null) 
	        {
	            if (entry.getName().endsWith("/")) 
	            {
	                File dir = new File(OUTPUT_FOLDER + File.separator + entry.getName());
	                if (!dir.exists()) 
	                {
	                    dir.mkdirs();
	                }
	                continue;
	            }
	 
	            File outFile = new File(OUTPUT_FOLDER + File.separator + entry.getName());
	            
	            
	            if (outFile.isDirectory()) 
	            {
	                outFile.mkdirs();
	                continue;
	            }
	 
	            if (outFile.exists()) 
	            {
	            	if(!overwrite)
	            	{
	            		System.out.println("Output file: " + entry.getName() + " exists. Overwrite? Yes No All");
			        	String usrIn = ParseInput.user.nextLine().toLowerCase();
				        if(usrIn.equals("y"))
				        {
				        	overwrite=false;
				        }
				        else if(usrIn.equals("a"))
				        {
				        	overwrite = true;
				        }
				        else
				        {
				        	continue;
				        }
	            	}
	            	
	                
	            }
	            
	            //make sure file directory is created
	            if (outFile.getParentFile() != null) 
	            {
	            	outFile.getParentFile().mkdirs();
	            }
	            
	            FileOutputStream out = new FileOutputStream(outFile);
	            byte[] buff = new byte[1024];
	            int length = 0;
	            while ((length = ais.read(buff)) > 0) 
	            {
	                out.write(buff, 0, length);
	                out.flush();
	            }
	        }
	        return true;
		}
		catch(ArchiveException e)
		{
			System.out.println("Error extracting: "+e);
			return false;
		}
        
    }
	
	/*static 
	{
		System.loadLibrary("LIBWINPTHREAD-1");
		System.loadLibrary("libstdc++-6");
		System.loadLibrary("libgcc_s_seh-1");
	    System.loadLibrary("lzss");
	 }*/
	
	
	
	/*
	 * Copyright 2008-2013, David Karnok 
	 * The file is part of the Open Imperium Galactica project.
	 * 
	 * The code should be distributed under the LGPL license.
	 * See http://www.gnu.org/licenses/lgpl.html for details.
	 */

	/**
	 * Decompress the given byte array using the LZSS algorithm and
	 * produce the output into the given out array.
	 * @param data the compressed input data
	 * @return
	 */
	public static byte[] decompressLZSS(byte[] data) 
	{
		int src = 0;
		int dst = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		int marker = 0;
		int nextChar = 0xFEE;
		final int windowSize = 4096;
		byte[] slidingWindow = new byte[windowSize];
		Arrays.fill(slidingWindow, (byte)0x20);

		while (src < data.length) 
		{
			marker = data[src++] & 0xFF;
			for (int i = 0; i < 8 && src < data.length; i++) 
			{
				boolean type = (marker & (1 << i)) != 0;
				if (type) 
				{
					byte d = data[src++];
					out.write(d);
					slidingWindow[nextChar] = d;
					nextChar = (nextChar + 1) % windowSize;
				} 
				else 
				{
					int offset = data[src++] & 0xFF;
					int len = data[src++] & 0xFF;
					offset = offset | (len & 0xF0) << 4;
					len = (len & 0x0F) + 3;
					for (int j = 0; j < len; j++) 
					{
						byte d = slidingWindow[(offset + j) % windowSize];
						out.write(d);
						slidingWindow[nextChar] = d;
						nextChar = (nextChar + 1) % windowSize;
					}
				}
			}
		}
		return out.toByteArray();
	}
	
	
	public static byte[] decompressLZSSStream(byte[] in)
	{	
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        byte[] buff = new byte[4096];
        Arrays.fill(buff, (byte)0x20);
        
        int writeIndex = 0xFEE;
        int readIndex = -1;
        int inIndex = 0;
        short flags = -1;
        
        while(inIndex < in.length)
        {
        	flags = in[inIndex];
        	inIndex++;
        	
        	for(int i=0; i<8; i++)
        	{
        		if ((flags & 1) == 1)
        		{
        			out.write(in[inIndex]);
        			buff[writeIndex]=in[inIndex];
        			writeIndex++; 
        			writeIndex %= 4096;
        			inIndex++;
        		}
        		else
        		{
        			readIndex = in[inIndex];
                    inIndex++;
                    readIndex |= (in[inIndex] & 0xF0) << 4;
                    for (int j=0; j<(in[inIndex] & 0x0F)+3; j++) 
                    {
                        out.write(buff[readIndex]);
                        buff[writeIndex] = buff[readIndex];
                        readIndex++; readIndex %= 4096;
                        writeIndex++; writeIndex %= 4096;
                    }
                    inIndex++;
                }
                flags >>= 1;
                if (inIndex >= in.length) break;
        	}
        }
        
        return out.toByteArray();
    }
}