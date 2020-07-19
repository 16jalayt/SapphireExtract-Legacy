package org.sapphireforge.audio;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HIS_Nancy_Drew 
{
	public static void HISextractOGG(RandomAccessFile inStream, boolean newer) throws IOException
	{
		inStream.seek(0);
		Output.OutSetup(Main.inputWithoutExtension,".ogg");
		//32 or 30 byte header.
		//varient from venice and ? shifted back 2 bytes
		if(newer)
		{
			inStream.seek(32);
			byte[] ogg = new byte[(int) inStream.length() - 32];
			inStream.read(ogg);
			Main.outStream.write(ogg);
		}
		else
		{
			inStream.seek(30);
			byte[] ogg = new byte[(int) inStream.length() - 30];
			inStream.read(ogg);
			Main.outStream.write(ogg);
		}
		
	}
	
	public static void HISextractWAV(RandomAccessFile inStream) throws IOException
	{		
		//8 byte header
		inStream.seek(8);
		short wavFormat = Helpers.readShortLittleEndian("wavFormat: ", inStream);
		short numChannels = Helpers.readShortLittleEndian("numChannels: ", inStream);
		int samplerate = Helpers.readIntLittleEndian("samplerate: ", inStream);
		int avgBytesPerSecond = Helpers.readIntLittleEndian("avgBytesPerSecond: ", inStream);
		short bitsPerSample = Helpers.readShortLittleEndian("bitsPerSample: ", inStream);
		short blockAlign = Helpers.readShortLittleEndian("blockAlign: ", inStream);
		int fileLength = Helpers.readIntLittleEndian("fileLength: ", inStream);
		
		samplerate = samplerate/numChannels;
		//defined in original file
		//short blockAlign = (short) (bitsPerSample / 8 * numChannels);
        //int avgbytes   = samplerate * blockAlign;
        
        byte[] filecut = new byte[fileLength];
		inStream.read(filecut);
		
		
		ByteArrayOutputStream fileoutFormatter = new ByteArrayOutputStream();
		//RIFF
		fileoutFormatter.write(Helpers.hexStringToByteArray("52494646"));
		//fileoutFormatter.write(new byte[]{(byte)0x52, (byte)0x49, (byte)0x46, (byte)0x46});
		//file size. probably should add header length?
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fileLength + 32).array());
		//fileoutFormatter.write(Helpers.hexStringToByteArray(Integer.toHexString(fileLength)));
		//WAVEfmt 16	16=length of header
		fileoutFormatter.write(Helpers.hexStringToByteArray("57415645666D742010000000"));
		//all of the above values in order
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(wavFormat).array());
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(numChannels).array());
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(samplerate).array());
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(avgBytesPerSecond).array());
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(bitsPerSample).array());
		fileoutFormatter.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(blockAlign).array());
		
		//data
		fileoutFormatter.write(Helpers.hexStringToByteArray("64617461"));
		//len of wav data
		fileoutFormatter.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(fileLength).array());
		//actual file data
		fileoutFormatter.write(filecut);

		byte[] fileout = fileoutFormatter.toByteArray();
		
		Output.OutSetup(".wav");
		//inStream.read(fileout);
		Main.outStream.write(fileout);
	}
}
