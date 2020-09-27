package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class TBV_3D_Ultra 
{
	public static void TBVextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(0);
		System.out.println("Warning... only 3d ultra games are tested, others may work.");
		System.out.println("Warning... Some files may error with an EOF exception. This appears to be Dynamix's fault.");
		
		//header
		inStream.skipBytes(9);
		//unknown. Seems to always stay same. Version?
		inStream.skipBytes(2);
		//# of files (4)
		int numfiles = Helpers.readIntLittleEndian(inStream);
		System.out.println("Extracting " +  numfiles + " files...");
		//always null?
		inStream.skipBytes(2);
		//guy's name
		inStream.skipBytes(24);
		
		//End of entry in file table
		long tableOffset = inStream.getFilePointer();
		
		
		for (int i = 0; i < numfiles; i++)
		{
			//go back to look up table
			inStream.seek(tableOffset);
			//print unknown chunk for exam. chunk type? Dont think so
			//System.out.println(Helpers.readIntLittleEndian(inStream));
			inStream.skipBytes(4);
			
			//Offset of current file in container
			int fileOffset = Helpers.readIntLittleEndian(inStream);
			tableOffset = inStream.getFilePointer();
			
			//go to start of file
			inStream.seek(fileOffset);
			//name of current output file
			byte[] currFileName = new byte[24];
			inStream.read(currFileName);
			//turn name to string cutting off trailing whitespace
			String name = new String(currFileName).trim();
			//System.out.println(new String(name));
			int length = Helpers.readIntLittleEndian(inStream);
			//System.out.println("lenth:"+ length);
			
			byte[] fileout = new byte[length];
			inStream.read(fileout);
			Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + name,"");
			ParseInput.outStream.write(fileout);
			
		}

	}
}
