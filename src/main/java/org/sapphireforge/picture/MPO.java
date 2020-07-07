package org.sapphireforge.picture;

import org.sapphireforge.program.Main;
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
		
		Output.OutSetup(Main.inputWithoutExtension,".jpg");
		Main.outStream.write(fileout);
		Main.outStream.close();
	}
}
