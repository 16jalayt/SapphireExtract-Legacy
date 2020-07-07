package org.sapphireforge.audio;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CSSDAT_RCT 
{
	public static void DATextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(0);
		int numfiles = Helpers.readIntLittleEndian(inStream);
		String[] FileList;
		
		
		//css1.dat is sound effects, other numbers are music
		if (Main.inputWithoutExtension.toLowerCase().compareTo("css1") == 0)
		{	//import text file if css1.dat to lookup names
			if (numfiles == 48)
				FileList = Helpers.getTextLines("rct1css1.txt");

			else if (numfiles == 63)
				FileList = Helpers.getTextLines("rct2css1.txt");
				
			else
				FileList = null;
			
			if (numfiles != FileList.length)
			{
				System.out.println("Wrong number of files in index. I can continue with numbers insted of names.");
				if (Helpers.continuePrompt())
				{
					FileList = new String[numfiles];
					for (int i=0; i < numfiles; ++i)
					{
						FileList[i] = Integer.toString(i);
					}
				}
				else
					System.exit(0);
				
			}
			
			
			for (int i = 0; i < numfiles; i++)
			{
				//System.out.println("File " + i + ":");
				int offset = Helpers.readIntLittleEndian(inStream);
				//System.out.println("off:"+offset);
				long tableOffset = inStream.getFilePointer();
				inStream.seek(offset);

				int fileLength = Helpers.readIntLittleEndian(inStream);
			
				
				//copied strait below
				short wavFormat = Helpers.readShortLittleEndian(inStream);
				short numChannels = Helpers.readShortLittleEndian(inStream);
				int samplesPerSecond = Helpers.readIntLittleEndian(inStream);
				int avgBytesPerSecond = Helpers.readIntLittleEndian(inStream);
				short blockAlign = Helpers.readShortLittleEndian(inStream);
				short bitsPerSample = Helpers.readShortLittleEndian(inStream);

				
				byte[] filecut = new byte[fileLength];
				inStream.read(filecut);
				
				
				ByteArrayOutputStream fileoutFormatter = new ByteArrayOutputStream();

				//RIFF
				fileoutFormatter.write(Helpers.hexStringToByteArray("52494646"));
				//file size. probably should add header length?
				fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fileLength + 32).array());
				//fileoutFormatter.write(Helpers.hexStringToByteArray(Integer.toHexString(fileLength)));
				//WAVEfmt 16	16=length of header
				fileoutFormatter.write(Helpers.hexStringToByteArray("57415645666D742010000000"));
				//all of the above values in order
				fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(wavFormat).array());
				fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(numChannels).array());
				fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(samplesPerSecond).array());
				fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(avgBytesPerSecond).array());
				fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(blockAlign).array());
				fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(bitsPerSample).array());
				//data
				fileoutFormatter.write(Helpers.hexStringToByteArray("64617461"));
				//len of wav data
				fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fileLength).array());
				//actual file data
				fileoutFormatter.write(filecut);

				byte[] fileout = fileoutFormatter.toByteArray();
				
				//concat header onto body
				//System.arraycopy(wavHeader, 0, fileout, 0, wavHeader.length);
				//System.arraycopy(filecut, 0, fileout, wavHeader.length, filecut.length);
				
				Output.OutSetup(Main.inputWithoutExtension + Main.separator + FileList[i],".wav");
				Main.outStream.write(fileout);
				Main.outStream.close();
				
				inStream.seek(tableOffset);
			}
			
		}
		else
		{
			inStream.seek(0);
			byte[] fileout = new byte[(int) inStream.length()];
			inStream.read(fileout);
	
			Output.OutSetup(Main.inputWithoutExtension,".wav");
			Main.outStream.write(fileout);
			Main.outStream.close();
		}
	}
}
