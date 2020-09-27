package org.sapphireforge.archive.nancydrew;

import org.im4java.core.GMOperation;
import org.im4java.core.GraphicsMagickCmd;
import org.sapphireforge.program.DecompressionManager;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CIF2_0 
{
	//Just secrets can kill?
		public static void parse2_0(RandomAccessFile inStream)throws IOException
		{
			// create command
			GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
			String path = CIF2_1_1.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			decodedPath = decodedPath.substring(0, decodedPath.lastIndexOf(ParseInput.separator)+1);
			cmd.setSearchPath(decodedPath+"GraphicsMagick-1.3.35-Q8"+ ParseInput.separator);


			//This value is actually a short not an int
			short numFiles = Helpers.readShortLittleEndian("Number of files: ", inStream);

			//Pseudorandom padding? 2kb
			inStream.skipBytes(2048);
			for (int i = 0; i < numFiles; i++)
			//for (int i = 0; i < 2; i++)
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
				if(ParseInput.arg.verbose) System.out.println();

				byte[] currFileName = new byte[9];
				inStream.read(currFileName);
				//turn name to string cutting off trailing whitespace
				String name = new String(currFileName).trim();
				System.out.println(name);

				short fileIndex = Helpers.readShortLittleEndian("File index: ", inStream);


				//seek to type descriptor
				long tableOffset = inStream.getFilePointer();
				inStream.seek(tableOffset+24);
				byte fileType = inStream.readByte();

				if(fileType != 2 && fileType != 3)System.out.println("Unknown type: "+fileType);

				//seek back
				inStream.seek(tableOffset);



				if(fileType==2)
				{
					if(ParseInput.arg.verbose) System.out.println("Plain file");


					fileWidth = Helpers.readShortLittleEndian("File width: ", inStream);
					short unknownTwo = Helpers.readShortLittleEndian(inStream);
					fileHeight = Helpers.readShortLittleEndian("File height: ", inStream);

					//seems to be constant
					byte UnknownByte = inStream.readByte();
					byte UnknownByte2 = inStream.readByte();

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
					if(ParseInput.arg.verbose) System.out.println("Data file");
					//placeholder 0s for data file
					inStream.skipBytes(7);

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

				fileraw= DecompressionManager.decompressLZSS(fileraw);

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

					Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + name,".tga");
					ParseInput.outStream.write(buffer.array());
					ParseInput.outStream.flush();
					ParseInput.outStream.close();

					// create the operation, add images and operators/options
					GMOperation op = new GMOperation();
					op.addImage(ParseInput.inputWithoutExtension + ParseInput.separator + name+".tga");
					op.transparent("green1");
					op.transparent("red1");
					op.addImage(ParseInput.inputWithoutExtension + ParseInput.separator + name+".png");
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

					ParseInput.outfile.delete();
				}
				//data
				if(fileType==3)
				{
					if(name.compareTo("CIFLIST")==0)
					{
						Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + name,"");
						ParseInput.outStream.write(fileraw);
					}
					else
					{//TODO: maybe parse ciflist for extension.
						//not worth doing as most games dont have ciflist. just hardcode cases to name like ciflist itself

						//xsheet 88=X. Not sure how java is getting that. Not hex
						if(fileraw[0]==88)
							Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + name,".xs1");
							//dat script file
						else if(fileraw[0]==68)
							Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + name,".iff");
						else
						{
							Output.OutSetup(ParseInput.inputWithoutExtension + ParseInput.separator + name,".unk");
							System.out.println("Unknown extension for file: "+name);
						}
						ParseInput.outStream.write(fileraw);
					}
				}

				inStream.seek(tableOffset);
			}
		}
}
