package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class DAT_Plan_It_Green 
{
	public static void DATextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(0);
		//first addr is number of files not identifier
		int numfiles = Helpers.readIntLittleEndian(inStream);
		System.out.println("Extracting " +  numfiles + " files...");
		
		inStream.seek(32);
		
		long tableOffset = inStream.getFilePointer();
		
		
		for (int i=0; i < numfiles; i++)
		{
			inStream.seek(tableOffset);
			//length of allocated space for name
			int namelength = 512;
			byte[] currFileName = new byte[namelength];
			byte[] newFileName = new byte[namelength];
			
			inStream.read(currFileName);
			
			//c wide string?
			//delete spaces inbetween chars in file names
			for (int j=0; j < currFileName.length; j+=2)
			{
				newFileName[j/2] = currFileName[j];
			}
			
			String newFileString = new String(newFileName);
			int loc = newFileString.indexOf('�');
			String correctFileString = newFileString.substring(0, loc-1);
			System.out.println(correctFileString);
			
			int currFileOff = Helpers.readIntLittleEndian(inStream);
			int currFileLen = Helpers.readIntLittleEndian(inStream);
			tableOffset = inStream.getFilePointer();
			//go to start of file
			inStream.seek(currFileOff);
			
			byte[] fileout = new byte[currFileLen];
			inStream.read(fileout);
			
			System.out.println(new String(correctFileString));
			
			Output.OutSetup(correctFileString,"");
			Main.outStream.write(fileout);
			Main.outStream.close();
		}
		
		

		
		
	}
}
