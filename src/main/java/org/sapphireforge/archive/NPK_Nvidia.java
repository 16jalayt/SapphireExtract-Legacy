package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NPK_Nvidia 
{
	public static void NPKextract(RandomAccessFile inStream) throws IOException
	{
		//table ends 55a0
		
		System.out.println("THIS FORMAT IS INCOMPLETE");	
		
		//9c55 first offset
		inStream.seek(4);
		
		long numfiles = Helpers.readLongLittleEndian("Number of files: ",inStream);
		
		for (int i=0; i< numfiles; i++)
		{
			
		}
	}
}
