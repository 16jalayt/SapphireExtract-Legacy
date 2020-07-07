package org.sapphireforge.archive.nancydrew;

import org.sapphireforge.program.DecompressionManager;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CIF2_1_1 
{
//algorithm developled on ghost dogs
	public static void cif2_1_1(RandomAccessFile inStream)throws IOException
	{
		//This value is actually a short not an int
		//games seem to have 0000 after but external seems to have a value
		short numFiles = Helpers.readShortLittleEndian("Number of files: ", inStream);
		inStream.skipBytes(2);
		
		//Pseudorandom padding? 2kb
		inStream.skipBytes(2048);
		
		for (int i = 0; i < numFiles; i++)
		//for (int i = 0; i < 5; i++)
		{
			//forward decs for needed variables
			int fileOffset = -1;
			int fileLengthDecompressed = -1;
			int fileLength = -1;
			
			short xOrigin = -1;
			short yOrigin = -1;
			short fileWidth = -1;
			short fileHeight = -1;
			
			//spacing for print
			if(Main.arg.verbose) System.out.println();
			
			byte[] currFileName = new byte[33];
			inStream.read(currFileName);
			//turn name to string cutting off trailing whitespace
			String name = new String(currFileName).trim();
			if(Main.arg.verbose) System.out.println(name);
			
			short fileIndex = Helpers.readShortLittleEndian("File index: ", inStream);
			
			//seek to type descriptor
			long tableOffset = inStream.getFilePointer();
			inStream.seek(tableOffset+58);
			byte fileType = inStream.readByte();
			
			///////NOTE check next byte for offset to tell versions apart
			//NOTe chunk type identifier moved too. now 5e offset. now need to seek 60
			
			if(fileType != 2 && fileType != 3)System.out.println("Unknown type: "+fileType);
			
			//seek back
			inStream.seek(tableOffset);
			
			
			
			if(fileType==2)
			{
				if(Main.arg.verbose) System.out.println("Plain file");
				
				fileOffset = Helpers.readIntLittleEndian("File offset: ", inStream);
				//padding? 0s and unknown
				inStream.skipBytes(10);
				
				//probably need to make int if UnknownZero1 never trips, inconsistant in file though
				//for tga header
				xOrigin = Helpers.readShortLittleEndian("x-origin: ", inStream);
				short UnknownZero1 = Helpers.readShortLittleEndian(inStream);
				if(UnknownZero1 !=0)
					System.out.println("UnknownInt should always be 0?: "+UnknownZero1);
				
				
				//probably need to make int if UnknownZero2 never trips, inconsistant in file though
				//for tga header
				yOrigin = Helpers.readShortLittleEndian("y-origin: ", inStream);
				short UnknownZero2 = Helpers.readShortLittleEndian(inStream);
				if(UnknownZero2 !=0)
					System.out.println("UnknownInt should always be 0?: "+UnknownZero2);
				
				//all zeros
				inStream.skipBytes(16);
				
				//same as above but assuming int
				fileWidth = Helpers.readShortLittleEndian("File width: ", inStream);
				short unknownTwo = Helpers.readShortLittleEndian(inStream);
				fileHeight = Helpers.readShortLittleEndian("File height: ", inStream);
				
				//seems to be constant
				short UnknownShort = Helpers.readShortLittleEndian(inStream);
				if(UnknownShort !=528)
					System.out.println("UnknownShort should always be 528?: "+UnknownShort);
				
				
				fileLengthDecompressed = Helpers.readIntLittleEndian("Final file length: ", inStream);
				
				int UnknownInt = Helpers.readIntLittleEndian(inStream);
				if(UnknownInt !=0)
					System.out.println("UnknownInt should always be 0?: "+UnknownInt);
				
				fileLength = Helpers.readIntLittleEndian("File length in ciff: ", inStream);
				
				//same file type we read before
				byte fileTypeAgain = inStream.readByte();			
			}
			
			else if(fileType==3)
			{
				if(Main.arg.verbose) System.out.println("Data file");
				
				fileOffset = Helpers.readIntLittleEndian("File offset: ", inStream);
				
				//placeholder 0s for data file
				inStream.skipBytes(50);				
				
				//fileLengthDecompressed = Helpers.readIntLittleEndian("Final file length: ", inStream);
				fileLength = Helpers.readIntLittleEndian("File length in ciff: ", inStream);
				
				//same file type we read before
				byte fileTypeAgain = inStream.readByte();
			}
			/////////////////////////////////////////////////
			//temp ignore broken
			else
			{
				System.out.println("File of type "+fileType+ " detected: " +name);
				System.out.println("This type of file is either a dummy file or points to an external file.");
				inStream.skipBytes(59);
				continue;
			}
			//////////////////////////////////////////////
			
			tableOffset = inStream.getFilePointer();
			inStream.seek(fileOffset);
			
			//read the file data
			byte[] fileraw = new byte[fileLength];
			inStream.read(fileraw);
			
			//subtract each bit by position
			//this was a basic attempt at encryption
			for(int j=0; j<fileraw.length;j++)
				fileraw[j]=(byte) (fileraw[j]-j);
			
			fileraw=DecompressionManager.decompressLZSS(fileraw);
				
			//plain
			if(fileType==2)
			{///////////////TODO: convert to png
				//need to append tga header
				ByteBuffer buffer = ByteBuffer.allocate(fileraw.length + 18);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				buffer.put(new byte[] {0x00, 0x00, 0x02,
		                   0x00, 0x00, 0x00, 0x00, 0x00});
				buffer.putShort(xOrigin);
				buffer.putShort(yOrigin);
				buffer.putShort(fileWidth);
				buffer.putShort(fileHeight);
				buffer.put(new byte[] {0x0F, 0x20});
				buffer.put(fileraw);
				
				Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,".tga");
				Main.outStream.write(buffer.array());
			}
			//data
			if(fileType==3)
			{
				if(name.compareTo("CIFLIST")==0)
				{
					System.out.println("CIFLIST shouldnt be detected!!!!!!!!!!!!! ");
				}
				else
				{//TODO: maybe parse ciflist for extension.
					//not worth doing as most games dont have ciflist. just hardcode cases to name like ciflist itself
					
					//xsheet 88=X. Not sure how java is getting that. Not hex
					if(fileraw[0]==88)
						Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,".xs1");
					//dat script file
					else if(fileraw[0]==68)
						Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,".dat");
					else
					{
						Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,".unk");
						System.out.println("Unknown extension for file: "+name);
					}
					Main.outStream.write(fileraw);
				}
			}

			inStream.seek(tableOffset);
		}
	}
	
}
