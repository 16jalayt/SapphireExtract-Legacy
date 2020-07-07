package org.sapphireforge.archive.nancydrew;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import unluac.decompile.Decompiler;
import unluac.parse.LFunction;

//using the unluac program
/*Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.*/

public class CIF3 
{
	//Crystal Skull - Sea of Darkness
		public static void parsev3(RandomAccessFile inStream)throws IOException
		{
			byte[] chunkHeader = new byte[24];
			inStream.read(chunkHeader);
			String secondHeaderS = new String(chunkHeader);
			//should only be for Ntdl.cif
			if(secondHeaderS.compareTo("CIF FILE HerInteractive\0") != 0)
			{
				System.out.println("Processing orphaned cif tree");
				parseChunk(inStream, Main.inputWithoutExtension, 0, 1,0x40);
			}
			
			//skip to last int of file
			inStream.seek(inStream.length() - 4);
			int nameTableLen = Helpers.readIntLittleEndian("Name Table Length: ", inStream);
			//go back name table length
			long nameTableStart = inStream.length() - 4 - nameTableLen;
			inStream.seek(nameTableStart);
			System.out.println("Name table starts at: "+ nameTableStart);
			
			int nameTableCount = Helpers.readIntLittleEndian("Name Table Count: ", inStream);
			String[] nameTable = new String[nameTableCount];
			//System.out.println("\n");
			long returnSpot = inStream.getFilePointer();
			
			//check for venice. Seek to end of normal name to see if 0, if not then longer name
			int nameLength = 0x40;
			inStream.seek(returnSpot+0x45);
			byte[] test = new byte[1];
			inStream.read(test);
			if(test[0]==0)
				nameLength = 0x21;
			inStream.seek(returnSpot);
			
			for(int i=0; i<nameTableCount;i++)
			{
				System.out.println();
				inStream.seek(returnSpot);
				//name 0x40 long except 18 and 19 0x21
				byte[] fileNameByte = new byte[nameLength];
				inStream.read(fileNameByte);
				//convert byte array to string and remove whitespace
				String fileName = new String(fileNameByte).trim();
				
				int filePointer = Helpers.readIntLittleEndian(inStream);
				System.out.println("File located at: "+ filePointer);
				returnSpot = inStream.getFilePointer();
				parseChunk(inStream, fileName, filePointer, nameTableCount,nameLength);
			}
			
			System.out.println();
			 //and negative offset that value for start on name table. get int file count then names
			//start direct with name after 0x40 int with start of header
			//ciffile herinteractive is artificial file header for each chunk.
		}
		
		private static boolean venice = false;
		private static boolean untested = true;
		private static void parseChunk(RandomAccessFile inStream, String fileName, int filePointer, int nameTableCount, int nameLength)throws IOException
		{
			inStream.seek(filePointer);
			inStream.skipBytes(24);
			
			//sanity check for currently unused value. version again?
			if (Helpers.readIntLittleEndian(inStream) != 3) 
				System.out.println("Unknown value 1 Please report game this occured in");
			
			int fileType = Helpers.readIntLittleEndian(inStream);
			String fileExt = ".unknown";
			//png
			if(fileType == 2)
			{
				//For png there are x and y size. everything else 0. This is presumably an engine convenience
				fileExt = ".png";
				
				//sanity check for currently unused value. varies from 1 to 600+, some same, 512 and 256
				/*if (Helpers.readIntLittleEndian("Unknown value 2 decal? should be 1:", inStream) != 1) 
					System.out.println("Unknown value 2. Please report game this occured in");*/
				//inStream.skipBytes(8);
			}
			//first venice file or malloy test
			else if (fileType == 3 && untested)
			{
				long begining = inStream.getFilePointer();
				inStream.skipBytes(16);
				
				if(inStream.readInt() == 1145132097 &&  nameLength == 0x21)
				{
					venice=true;
					fileExt = ".data";
				}
				//maloy
				else
					fileExt = ".luac";
				
				untested = false;
				inStream.seek(begining);
			}
			//lua
			else if (fileType == 3 && !venice)
			{
				fileExt = ".luac";
			}
			//venice
			else if (fileType == 3 && venice)
			{
				fileExt = ".data";
			}
			
			//xsheet
			else if (fileType == 6)
			{
				fileExt = ".xsheet";
			}
			else 
			{
				fileExt = ".unknown";
				System.out.println("Unknown file type " + fileType + ". Please report game this occured in.");
				if (Main.arg.verbose) {System.out.println("This occured at addr: " + (inStream.getFilePointer() - 32));}
			}
			inStream.skipBytes(12);
			
			System.out.println(fileName + fileExt);
			System.out.println("File offset: " + filePointer);
			
			int fileSize = Helpers.readIntLittleEndian("File size: ", inStream);
			
			byte[] fileout = new byte[fileSize];
			inStream.read(fileout);
			
			//don't create dir for orphan
			if(nameTableCount == 1)
				Output.OutSetup(fileName, fileExt);
			else
				Output.OutSetup(Main.inputWithoutExtension + Main.separator + fileName, fileExt);
			Main.outStream.write(fileout);
			Main.outStream.close();
			
			//files are written. Below is post process for specific files
			if(fileExt.equals(".luac"))
			{
				if(Main.arg.raw)
					return;

				/*String arguments = "cmd /C java -jar unluac_2015_06_13.jar \"" + Main.outfile.getPath() + "\"" + " > \"" + Main.outfile.getPath().replace("luac", "lua") + "\"";
				Process ps=Runtime.getRuntime().exec(arguments);
		        try 
		        {
					ps.waitFor();
				} 
		        catch (InterruptedException e) 
		        {
					e.printStackTrace();
				}
		        InputStream in = ps.getInputStream();
		        InputStream err = ps.getErrorStream();
		        
		        ByteArrayOutputStream result = new ByteArrayOutputStream();
		        byte[] buffer = new byte[1024];
		        int length;
		        while ((length = err.read(buffer)) != -1) {
		            result.write(buffer, 0, length);
		        }
		        //check if empty for error
		        if(!result.toString().equals(""))
		        	System.out.println(result.toString(StandardCharsets.UTF_8.name()));
		        
		        
		        ByteArrayOutputStream result2 = new ByteArrayOutputStream();
		        byte[] buffer2 = new byte[1024];
		        int length2;
		        while ((length2 = in.read(buffer2)) != -1) {
		            result2.write(buffer2, 0, length2);
		        }
		        //succesful should be blank
		        if(!result2.toString().equals(""))
		        	System.out.println(result2.toString(StandardCharsets.UTF_8.name()));
		        
		        
		        
		        if(!Main.outfile.delete()) 
		        { 
		            System.out.println("Error deleteing temp lauq file"); 
		        } */
			}
			
			return;
		}
}