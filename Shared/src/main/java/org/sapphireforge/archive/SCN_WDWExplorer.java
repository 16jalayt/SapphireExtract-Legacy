package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class SCN_WDWExplorer 
{
	public static void SCNextract(RandomAccessFile inStream) throws IOException
	{
		//Seems to be a compiled script/scene descriptor file with embedded bmp
		
		int bufferlen = 256;
		byte[] buff = new byte[bufferlen];
		RandomAccessFile buffer = new RandomAccessFile(ParseInput.infile, "r");
		
		
		for (int i = 0; i<buffer.length(); i+=(bufferlen/2))
		{
			buffer.seek(i);
			buffer.read(buff);
			String buffs = new String(buff);
			int result = buffs.indexOf("BM6");
			if(result != -1)
			{
				inStream.seek(i + result);
				inStream.seek((i + result)-37);
				
				short nameLength = Helpers.readShortLittleEndian(inStream);
				
				byte[] currFileName = new byte[nameLength];
				inStream.read(currFileName);
				String name = new String(currFileName).trim();
				
				//remove files in /g direcotry
				if (name.charAt(0) != 'G')
				{
					System.out.println(name);
					
					inStream.skipBytes(16);
					int length = Helpers.readIntLittleEndian(inStream);
					
					byte[] fileout = new byte[length];
					inStream.read(fileout);
					Output.OutSetup(name,"");
					ParseInput.outStream.write(fileout);
					
					//skip searcher to end of bmp for speed.
					//For some reason have to subtract exactly 90 or misses every other picture. set to 150 for safe margin.
					i = (int) (inStream.getFilePointer() - (150));
				}	
			}
		}
	}
}
