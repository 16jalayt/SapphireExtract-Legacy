package org.sapphireforge.picture;

import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MPO 
{
	public static void MPOextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(0);
		
		byte[] fileout = new byte[(int) inStream.length()];
		inStream.read(fileout);
		
		Output.OutSetup(ParseInput.inputWithoutExtension,".jpg");
		ParseInput.outStream.write(fileout);
		ParseInput.outStream.close();
	}
}
