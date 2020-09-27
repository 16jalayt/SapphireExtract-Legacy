package org.sapphireforge.archive;

import org.sapphireforge.picture.MHK_Riven_Media;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;

public class MHK_Riven 
{
	public static void MHKextract(RandomAccessFile inStream) throws IOException
	{////////////////big endian
		inStream.seek(0);
		
		inStream.skipBytes(4);//MHWK signature
		int size = Helpers.readIntBigEndian("Archive size:", inStream);
		inStream.skipBytes(4);//RSRC signature
		short version = Helpers.readShortBigEndian("Version:", inStream);
		inStream.skipBytes(2);//compaction? 0?
		int archiveLength = Helpers.readIntBigEndian("Archive length:", inStream);
		int typeTableOffset = Helpers.readIntBigEndian("Type table ofset:", inStream);
		short tableOffset = Helpers.readShortBigEndian("Table offset:", inStream);
		short tableSize = Helpers.readShortBigEndian("Table size:", inStream);
		if (ParseInput.arg.verbose) {System.out.println();}
		
		inStream.seek(typeTableOffset);
		
		short nameOffset = Helpers.readShortBigEndian("Name offset:", inStream);
		short resType = Helpers.readShortBigEndian("Resource type?:", inStream);
		
		//tmp
		String fileTypeS = "";
		
		
		for (int i = 0; i < resType; i++)
		{///////////////multiple resorce types will overwrite
			if (ParseInput.arg.verbose) {System.out.println("\n");}
			//type of chunk
			byte[] fileType = new byte[4];
			inStream.read(fileType);
			fileTypeS = new String(fileType);
			if (ParseInput.arg.verbose) {System.out.println(fileTypeS);}
			
			short resTableOff = Helpers.readShortBigEndian("Resource table offset:", inStream);
			short nameTableOff = Helpers.readShortBigEndian("Name table offset:", inStream);
			
			//file types are very game specific. need to implement for each game
			if (!fileTypeS.equals("tBMP")) {System.out.println("Resource node of type: " + fileTypeS + " is not implemented"); continue;}
			
			//long pointer = inStream.getFilePointer();
			inStream.seek(resTableOff + typeTableOffset);
			//if (Main.verbose) {System.out.println(inStream.getFilePointer());}
			
			short resCount = Helpers.readShortBigEndian("Res count:", inStream);
			short[] idArray = new short[resCount];
			short[] indexArray = new short[resCount];
			HashMap<Short, Short> indexMap = new HashMap<Short, Short>();
			for (int j = 0; j < resCount; j++)
			{
				idArray[j] = Helpers.readShortBigEndian(inStream);
				indexArray[j] = Helpers.readShortBigEndian(inStream);
				indexMap.put(indexArray[j], idArray[j]);
			}
			if (ParseInput.arg.verbose) {System.out.println(Arrays.toString(idArray));}
			if (ParseInput.arg.verbose) {System.out.println(Arrays.toString(indexArray));}
			
			inStream.seek(nameTableOff + typeTableOffset);
			short nameCount = Helpers.readShortBigEndian("Name count:", inStream);
			System.out.println();
			
			for (int j = 0; j < nameCount; j++) 
			{
				int typeNameTableOff = Helpers.readShortBigEndian("Type name table offset:", inStream);
				//ids not in order
				short id = Helpers.readShortBigEndian("id:", inStream);
				
				long tablepos = inStream.getFilePointer();
				inStream.seek(resTableOff + nameOffset + typeNameTableOff);
				System.out.println("Located at:" + inStream.getFilePointer());
				//System.out.println(indexMap.get(id));
				 byte ch = -1;
			     String name = "";
			     while(ch != 0) 
			     {
			       ch = inStream.readByte();
			       name += Character.toString((char) ch);
			     }
			     System.out.println(name);
				
				System.out.println();
				inStream.seek(tablepos);
			}
			
			inStream.seek(tableOffset + typeTableOffset);
			int fileTableCount = Helpers.readIntBigEndian("File table count:", inStream);
			
			int[] fileOff = new int[fileTableCount];
			int[] fileLength = new int[fileTableCount];
			
			for (int j = 0; j < fileTableCount; j++)
			{
				if (ParseInput.arg.verbose) {System.out.println();}
				fileOff[j] = Helpers.readIntBigEndian("File offset:", inStream);
				int fileLen = Short.toUnsignedInt(Helpers.readShortBigEndian(inStream));
				
				//the wierd 3 byte might be needed on older games
				//int fileLen = Helpers.unsignedShort(Helpers.readShortBigEndian(inStream));
				//byte fileLen2 = inStream.readByte();
				//fileLength[j] = fileLen | (fileLen2 << 16);
				fileLength[j] = fileLen;
				if (ParseInput.arg.verbose) {System.out.println("File length: " + fileLength[j]);}
				
				//fileLen2, byte flag, short unk?
				inStream.skipBytes(4);
			}
	
			for (int j = 0; j < fileTableCount; j++)
			{
				//do proper names
				Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + String.valueOf(idArray[j]),".png");
				//byte[] fileContents = new byte[fileLength[j]];
				inStream.seek(fileOff[j]);
				//inStream.read(fileContents);
				
				//hack to just seek to location insted of passing data, so i can keep using ras without temp file
				//note might use bytebuffer.position or seekablebytechannel
				try
				{
					ParseInput.outStream.write(MHK_Riven_Media.MHK_BMP_Convert(inStream, fileLength[j], fileOff[j]));
				}
				catch(InvalidObjectException e)
				{
					System.out.println("invalid");
					continue;
				}
				//Main.outStream.close();
				///////
				//return;
			}
		}
		
	}
}
