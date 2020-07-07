package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;

import java.io.IOException;
import java.io.RandomAccessFile;

public class M4B_MystIV 
{
	public static void M4BExtract(RandomAccessFile inStream) throws IOException
	{
		System.out.println("THIS FORMAT IS INCOMPLETE");
		inStream.seek(0);
		
		int idLen = Helpers.readIntLittleEndian("ID Length: ", inStream);
		byte[] chunkHeader = new byte[idLen];
		inStream.read(chunkHeader);
		String secondHeaderS = new String(chunkHeader);
		if(secondHeaderS.compareTo("UBI_BF_SIG\0") != 0)
		{
			System.out.println("Unknown Header");
			return;
		}
		
		inStream.skipBytes(8);
		byte[] numRootFolder = new byte[1];
		inStream.read(numRootFolder);
		System.out.println("Num Root Folders: " + numRootFolder[0]);
		
		for(int i=0; i<numRootFolder[0]; i++)
		{
			int folderNameLen = Helpers.readIntLittleEndian("Length of folder: ", inStream);
			
			byte[] folderName = new byte[folderNameLen];
			inStream.read(folderName);
			String folderNameS = new String(folderName);
			System.out.println("Named: " + folderNameS);
			
			byte[] numSubFolders = new byte[1];
			inStream.read(numSubFolders);
			System.out.println("Num Sub Folders: " + numSubFolders[0]);
		}
		
		M4BFolder(inStream, numRootFolder[0]);
	}
	
	//TODO: keep track of extraction path
	public static void M4BFolder(RandomAccessFile inStream,int numberFolders) throws IOException
	{
		for(int i=0; i<numberFolders; i++)
		{
			int folderNameLen = Helpers.readIntLittleEndian("Length of folder: ", inStream);
			
			byte[] folderName = new byte[folderNameLen];
			inStream.read(folderName);
			String folderNameS = new String(folderName);
			System.out.println("Named: " + folderNameS);
			
			byte[] numSubFolders = new byte[1];
			inStream.read(numSubFolders);
			System.out.println("Num Sub Folders: " + numSubFolders[0]);
			//itter sub folders
			for(int j=0; j<numSubFolders[0]; j++)
			{
				
			}
			
			int numFiles = Helpers.readIntLittleEndian("Number of files: ", inStream);
			//ext files
			for(int j=0; j<numFiles; j++)
			{
				
			}
		}
	}
}
