package org.sapphireforge.audio;

import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class AKB_FFIV 
{
	public static void AKBextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(204);
		Output.OutSetup(".ogg");
		
		byte[] ogg = new byte[(int) (inStream.length() - inStream.getFilePointer())];
		inStream.read(ogg);
		Main.outStream.write(ogg);
	}
}
