package org.sapphireforge.archive.nancydrew;

import org.im4java.core.ConvertCmd;
import org.im4java.core.GMOperation;
import org.im4java.core.GraphicsMagickCmd;
import org.im4java.core.IMOperation;
import org.sapphireforge.program.DecompressionManager;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CIF2_1_0 
{

	public static void cif2_1_0(RandomAccessFile inStream,boolean is2)throws IOException
	{
		// create command
		GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
		String path = CIF2_1_1.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = URLDecoder.decode(path, "UTF-8");
		decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf(Main.separator)+1);
		cmd.setSearchPath(decodedPath+"GraphicsMagick-1.3.35-Q8"+Main.separator);


		//This value is actually a short not an int
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

				byte[] currFileName;
				if(is2)
					currFileName = new byte[9];
				else
					currFileName = new byte[33];
				inStream.read(currFileName);
				//turn name to string cutting off trailing whitespace
				String name = new String(currFileName).trim();
				System.out.println(name);
				
				short fileIndex = Helpers.readShortLittleEndian("File index: ", inStream);
				
				
				//seek to type descriptor
				long tableOffset = inStream.getFilePointer();
				inStream.seek(tableOffset+56);
				byte fileType = inStream.readByte();
				
				///////NOTE check next byte for offset to tell versions apart
				//NOTe chunk type identifier moved too. now 5e offset. now need to seek 60
				
				if(fileType != 2 && fileType != 3)System.out.println("Unknown type: "+fileType);
				
				//seek back
				inStream.seek(tableOffset);
				
				
				
				if(fileType==2)
				{
					if(Main.arg.verbose) System.out.println("Plain file");
					//padding? 0s
					inStream.skipBytes(8);
					
					//probably need to make int if UnknownZero1 never trips, inconsistant in file though
					//for tga header
					xOrigin = Helpers.readShortLittleEndian("x-origin: ", inStream);
					short UnknownZero1 = Helpers.readShortLittleEndian(inStream);
					
					//probably need to make int if UnknownZero2 never trips, inconsistant in file though
					//for tga header
					yOrigin = Helpers.readShortLittleEndian("y-origin: ", inStream);
					short UnknownZero2 = Helpers.readShortLittleEndian(inStream);
					
					//all zeros
					inStream.skipBytes(16);
					
					//same as above but assuming int
					fileWidth = Helpers.readShortLittleEndian("File width: ", inStream);
					short unknownTwo = Helpers.readShortLittleEndian(inStream);
					fileHeight = Helpers.readShortLittleEndian("File height: ", inStream);
					
					//seems to be constant
					short UnknownShort = Helpers.readShortLittleEndian(inStream);
					
					fileOffset = Helpers.readIntLittleEndian("File offset: ", inStream);
					fileLengthDecompressed = Helpers.readIntLittleEndian("Final file length: ", inStream);
					
					int UnknownInt = Helpers.readIntLittleEndian(inStream);
					
					fileLength = Helpers.readIntLittleEndian("File length in ciff: ", inStream);
					
					//same file type we read before
					byte fileTypeAgain = inStream.readByte();
					
					//value from <100 - ffff
					short UnknownRandom = Helpers.readShortLittleEndian(inStream);				
				}
				
				else if(fileType==3)
				{
					if(Main.arg.verbose) System.out.println("Data file");
					//placeholder 0s for data file
					inStream.skipBytes(39);
					
					//always 2? need to confirm
					byte unknownConstant = inStream.readByte();
					
					fileOffset = Helpers.readIntLittleEndian("File offset: ", inStream);
					fileLengthDecompressed = Helpers.readIntLittleEndian("Final file length: ", inStream);
					
					int UnknownInt = Helpers.readIntLittleEndian(inStream);
					
					fileLength = Helpers.readIntLittleEndian("File length in ciff: ", inStream);
					
					//same file type we read before
					byte fileTypeAgain = inStream.readByte();
					
					//value from <100 - ffff
					short UnknownRandom = Helpers.readShortLittleEndian(inStream);	
				}
				
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
				{
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
					Main.outStream.flush();
					Main.outStream.close();

					// create the operation, add images and operators/options
					GMOperation op = new GMOperation();
					op.addImage(Main.inputWithoutExtension + Main.separator + name+".tga");
					op.transparent("green1");
					op.transparent("red1");
					op.addImage(Main.inputWithoutExtension + Main.separator + name+".png");
					//cmd.createScript("out",op);
					try
					{
						// execute the operation
						cmd.run(op);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}

					Main.outfile.delete();
				}
				//data
				if(fileType==3)
				{
					if(name.compareTo("CIFLIST")==0)
					{
						//hack for CIFLIST to get encoding right
						for(int j=0; j<fileraw.length;j++)
						{
							if(fileraw[j]== 0x00)
								fileraw[j]=0x20;
						}
						Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,"");
						Main.outStream.write(fileraw);
					}
					else
					{//TODO: maybe parse ciflist for extension.
						//not worth doing as most games dont have ciflist. just hardcode cases to name like ciflist itself

						//xsheet 88=X. Not sure how java is getting that. Not hex
						if(fileraw[0]==88)
							Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,".xs1");
						//dat script file
						else if(fileraw[0]==68)
							Output.OutSetup(Main.inputWithoutExtension + Main.separator + name,".iff");
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
