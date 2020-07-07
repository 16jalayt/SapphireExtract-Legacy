package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PCK_godot_engine 
{
	public static void PCKextract(RandomAccessFile inStream) throws IOException
	{
		boolean skipmd5 = false;
		
		inStream.seek(4);
		int revVersion = Helpers.readIntLittleEndian(inStream);
		int revMajor = Helpers.readIntLittleEndian(inStream);
		int revMinor = Helpers.readIntLittleEndian(inStream);
		int revRevison = Helpers.readIntLittleEndian(inStream);
		System.out.println("Engine version detected: " + revVersion + "." + revMajor + "." + revMinor);
		
		//zero padded
		inStream.skipBytes(64);
		
		int numfiles = Helpers.readIntLittleEndian(inStream);
		//System.out.println("# files:" +  numfiles);
		
		
		System.out.println();
		long tableOffset = inStream.getFilePointer();
		
		for (int i=0; i < numfiles; i++)
		{
			inStream.seek(tableOffset);
			
			int nameLength = Helpers.readIntLittleEndian(inStream);
			//System.out.println("namelen:" +  nameLength);
			
			byte[] fileName = new byte[nameLength];
			inStream.read(fileName);
			String name = new String(fileName).trim();
			name = name.substring(6);
			name = name.replace("/", Main.separator);
			if(name.contains(".import\\"))
				name = name.substring(0, name.indexOf('-'));
			//System.out.println("file: " +  name);
			
			long fileOffset = Helpers.readLongLittleEndian(inStream);
			//System.out.println("off: " +  fileOffset);
			
			long fileSize = Helpers.readLongLittleEndian(inStream);
			//System.out.println("size: " +  fileSize);
			
			byte[] filemd5 = new byte[16];
			inStream.read(filemd5);
			String filemd5S = Helpers.byteArrayToHex(filemd5);
			
			
			tableOffset = inStream.getFilePointer();
			inStream.seek(fileOffset);
			
			byte[] fileout = new byte[(int) fileSize];
			inStream.read(fileout);
			
			if(!skipmd5)
			{
				String md5gen = Helpers.genmd5(fileout);
				if(md5gen.compareTo(filemd5S) != 0)
				{
					System.out.println("\nmd5 Conflict");
					System.out.println("md5 file: " +  filemd5S);
					System.out.println("md5 gen: " + md5gen);
					System.out.println("\nA to skip md5 or anything else to continue.");
					String skipinput = Main.user.nextLine();
					if(skipinput.toLowerCase().equals("a"))
			        {
			        	Main.outStream = new FileOutputStream(Main.outfile);
			        }
					
				}
			}
			
				
			Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,"");
			Main.outStream.write(fileout);
			
			
			System.out.println();
		}		
	}
}
