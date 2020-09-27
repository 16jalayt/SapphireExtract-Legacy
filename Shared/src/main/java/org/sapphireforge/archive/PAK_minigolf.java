package org.sapphireforge.archive;

import org.sapphireforge.program.DecompressionManager;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class PAK_minigolf 
{
	public static void DATextract(RandomAccessFile inStream, int varient) throws IOException
	{
		inStream.seek(0);
		int numfiles;
		
		if (varient == 1)
		{
			//skip header and unknown block
			inStream.seek(18);
			
			numfiles = Helpers.readIntLittleEndian("# files:", inStream);
		}
		else if (varient == 2)
		{
			//skip header
			inStream.seek(18);
			
			numfiles = Helpers.readIntLittleEndian("# files:", inStream);
			//skip end of table pointer
			inStream.skipBytes(4);
		}
		else
		{
			System.out.println("Unknown varient");
			return;
		}
		
		
		
		long tableOffset = inStream.getFilePointer();
		
		for (int i=0; i < numfiles; i++)
		{
			inStream.seek(tableOffset);
			
			int fileOffset = Helpers.readIntLittleEndian(inStream);
			int fileLength = Helpers.readIntLittleEndian(inStream);
			int pathLength = Helpers.readIntLittleEndian(inStream);
			
			
			byte[] fileName = new byte[pathLength];
			inStream.read(fileName);
			String fileNameS = new String(fileName);
			if (varient == 1) {fileNameS = fileNameS.substring(0, fileNameS.lastIndexOf("."));}
			if (ParseInput.arg.verbose) {System.out.println(fileNameS);}
			
			tableOffset = inStream.getFilePointer();
			//go to start of file
			inStream.seek(fileOffset);
			
			byte[] fileout = new byte[fileLength];
			inStream.read(fileout);
			
			/*File currFile = File.createTempFile(fileNameS, null);
			FileOutputStream outStream  = new FileOutputStream(currFile);
			outStream.write(fileout);
			
			DecompressionManager.Decompress(currFile);
			currFile.delete();*/
			byte[] fileDec = DecompressionManager.decompressLZMAStream(fileout);
			Output.OutSetup(fileNameS,"");
			ParseInput.outStream.write(fileDec);
			ParseInput.outStream.close();
		}		
	}
}
