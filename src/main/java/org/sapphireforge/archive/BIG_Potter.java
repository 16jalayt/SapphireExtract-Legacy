package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BIG_Potter 
{
	public static void BIGextract(RandomAccessFile inStream) throws IOException
	{
		//BIG ENDIAN FILE
		inStream.seek(0);
		byte[] magic = new byte[4];
		inStream.read(magic);
		String magicS = new String(magic);
		if(magicS.compareTo("BIGF") != 0)
		{
			System.out.println("Not a Harry Potter BIG file. (Only tested with goblet of fire)");
			return;
		}
		
		int contLen = Helpers.readIntBigEndian("Container Length: ",inStream);

		int numFiles = Helpers.readIntBigEndian("Number of files: ",inStream);

		int endFileTable = Helpers.readIntBigEndian(inStream);
		
		for(int i=0; i<numFiles; i++)
		{	
			//offset
			int fileOff = Helpers.readIntBigEndian("File Offset: ",inStream);
			//len
			int fileLen = Helpers.readIntBigEndian("File Length: ",inStream);
			
			//name variable len trailing null
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
			
			long tableOffset = inStream.getFilePointer();
			inStream.seek(fileOff);
			
			byte[] fileout = new byte[fileLen];
			inStream.read(fileout);
			Output.OutSetup(Main.inputWithoutExtension + Main.separator + fileName,"");
			Main.outStream.write(fileout);

			inStream.seek(tableOffset);
		}
		
	}
}
