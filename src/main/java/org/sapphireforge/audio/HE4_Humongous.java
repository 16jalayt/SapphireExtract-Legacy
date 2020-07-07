package org.sapphireforge.audio;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HE4_Humongous 
{
	public static void HE4extract(RandomAccessFile inStream) throws IOException
	{
		//default wav settings
		int samplesPerSecond = 11025;
		
		//skip header: SONG
		inStream.seek(4);
		
		//Everything should be little endian except fileLen?
		int fileLen = Helpers.readIntBigEndian(inStream);
		//chunk type should be SGHD
		int magic = Helpers.readIntBigEndian(inStream);
		if(magic!=1397180484)
		{
			System.out.println("Invalid chunk type: "+ magic);
			return;
		}
		//unknown. 737 in ff1 and 40 in sf1
		inStream.skipBytes(4);
		
		int numFiles = Helpers.readIntLittleEndian(inStream);
		if (Main.arg.verbose) {System.out.println("Extracting " + numFiles + " files.");}
		
		int id = Helpers.readIntLittleEndian("song id: ",inStream);
		inStream.seek(inStream.getFilePointer() - 4);
		
		boolean offset = false;
		if (id == 0)
		{
			//offset past padding
			inStream.seek(47);
			offset = true;
		}
		
		for (int i = 0; i < numFiles; i++)
		{
			id = 0;
			if (offset)
			{
				inStream.skipBytes(1);
				//Assert.assertEquals("Unknown chunk.", 352321536, inStream.readInt());
				inStream.readInt();
				//no idea. always 352321536?
				inStream.skipBytes(4);

			}
			
			id = Helpers.readIntLittleEndian("song id: ",inStream);
			int pos = Helpers.readIntLittleEndian("song pos: ", inStream);
			int len = Helpers.readIntLittleEndian("song len: ", inStream);
			
			if (Main.arg.verbose) {System.out.println();}
			
			long tableOffset = inStream.getFilePointer();
			//skip sdat header stuff
			inStream.seek(pos + 40);
			
			byte[] filecut = new byte[len];
			inStream.read(filecut);
			
			
			ByteArrayOutputStream fileoutFormatter = new ByteArrayOutputStream();

			//RIFF
			fileoutFormatter.write(Helpers.hexStringToByteArray("52494646"));
			//file size. probably should add header length?
			fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(len + 36).array());
			//fileoutFormatter.write(Helpers.hexStringToByteArray(Integer.toHexString(fileLength)));
			//WAVEfmt 16	16=length of header
			fileoutFormatter.write(Helpers.hexStringToByteArray("57415645666D742010000000"));

			fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1).array());
			fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1).array());
			fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(samplesPerSecond).array());
			fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(samplesPerSecond).array());
			fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 2).array());
			fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 8).array());
			//data
			fileoutFormatter.write(Helpers.hexStringToByteArray("64617461"));
			//len of wav data
			fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(len).array());
			//actual file data
			fileoutFormatter.write(filecut);
			
			byte[] fileout = fileoutFormatter.toByteArray();
			
			Output.OutSetup(Main.inputWithoutExtension + Main.separator + Main.inputWithoutExtension + " " + id,".wav");
			Main.outStream.write(fileout);
			
			inStream.seek(tableOffset);
			if (!offset)
				//alphabet?
				inStream.skipBytes(13);
		}
	}
	
	//music from early games
	public static void DMUextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(4);
		int len = Helpers.readIntBigEndian(inStream) - 40;
		inStream.seek(40);
		byte[] filecut = new byte[len];
		inStream.read(filecut);
		
		
		ByteArrayOutputStream fileoutFormatter = new ByteArrayOutputStream();

		//RIFF
		fileoutFormatter.write(Helpers.hexStringToByteArray("52494646"));
		//file size. probably should add header length?
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(len + 36).array());
		//fileoutFormatter.write(Helpers.hexStringToByteArray(Integer.toHexString(fileLength)));
		//WAVEfmt 16	16=length of header
		fileoutFormatter.write(Helpers.hexStringToByteArray("57415645666D742010000000"));

		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1).array());
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1).array());
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(11000).array());
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(11000).array());
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 2).array());
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 16).array());
		//data
		fileoutFormatter.write(Helpers.hexStringToByteArray("64617461"));
		//len of wav data
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(len).array());
		//actual file data
		fileoutFormatter.write(filecut);
		
		byte[] fileout = fileoutFormatter.toByteArray();
		
		Output.OutSetup(Main.inputWithoutExtension,".wav");
		Main.outStream.write(fileout);
	}
}
