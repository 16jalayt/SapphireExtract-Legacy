package org.sapphireforge.audio;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class LUG_Lionhead_Studios 
{
	public static void LUGextract(RandomAccessFile inStream) throws IOException
	{
		//LiOnHeAdLHAudioBankCompData  f1 pack 
		//LiOnHeAdLHFileSegmentBankInfo  f1,mov pack 
		//LiOnHeAdLHAudioBankMetaData  mov pack
		
		inStream.seek(0);
		byte[] header = new byte[32];
		inStream.read(header);
		//turn name to string cutting off trailing whitespace
		String headerS = new String(header).trim();
		
		if (headerS.compareTo("LiOnHeAdLHFileSegmentBankInfo")==0)
		{
			long tablePos;
			long footerPos;
			inStream.skipBytes(8);
			//unknown consistant value
			int unknown520 = Helpers.readIntLittleEndian(inStream);
			if (unknown520 != 520) 
				System.out.println("Should be 520: " + unknown520);
			//useless description of file
			inStream.skipBytes(520);
			
			byte[] FirstChunk = new byte[15];
			inStream.read(FirstChunk);
			//turn name to string cutting off trailing whitespace
			String FirstChunkS = new String(FirstChunk);
			if (FirstChunkS.compareTo("LHAudioWaveData") != 0) 
				System.out.println("Unknown Chunk: " + FirstChunkS);
			
			//padded
			inStream.skipBytes(17);
			long footerOffset = (long) Helpers.readIntLittleEndian(inStream);
			tablePos = inStream.getFilePointer();
			
			inStream.seek(footerOffset + inStream.getFilePointer());
			byte[] IndexChunk = new byte[22];
			inStream.read(IndexChunk);
			String IndexChunkS = new String(IndexChunk);
			if (IndexChunkS.compareTo("LHAudioBankSampleTable") != 0) 
				System.out.println("Unknown Chunk: " + IndexChunkS);
			//padded
			inStream.skipBytes(10);
			int unknown = Helpers.readIntLittleEndian(inStream);
			short numFiles = Helpers.readShortLittleEndian(inStream);
			//repeated for some reason?
			short numFiles2 = Helpers.readShortLittleEndian(inStream);
			footerPos = inStream.getFilePointer();
			
			for (int i = 0; i < numFiles; i++)
			{
				inStream.seek(footerPos);
				//should be long enough
				byte[] currFileName = new byte[255];
				inStream.read(currFileName);
				String name = new String(currFileName).trim();
				inStream.skipBytes(13);
				//cut off file path to just file name
				String fileName = name.substring(name.lastIndexOf(Main.separator) +1, name.length());
				System.out.println("file: " + fileName);
				int fileLength = Helpers.readIntLittleEndian(inStream);
				inStream.skipBytes(380);
				footerPos = inStream.getFilePointer();
				//4f num entries?
				//path begining of chunk
				//len directly before wav
				inStream.seek(tablePos);
				byte[] fileout = new byte[fileLength];
				inStream.read(fileout);
				Output.OutSetup("ext" + Main.separator + fileName,"");
				Main.outStream.write(fileout);
			}
			
			
			
		}
		else
		{
			System.out.println("Unrecognised varient: "+ headerS);
		}
	}
}
