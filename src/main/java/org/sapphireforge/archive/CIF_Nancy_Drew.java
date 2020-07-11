package org.sapphireforge.archive;

import org.sapphireforge.archive.nancydrew.CIF2_0;
import org.sapphireforge.archive.nancydrew.CIF2_1_0;
import org.sapphireforge.archive.nancydrew.CIF2_1_1;
import org.sapphireforge.archive.nancydrew.CIF3;
import org.sapphireforge.program.DecompressionManager;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import java.io.IOException;
import java.io.RandomAccessFile;

public class CIF_Nancy_Drew 
{//////////TODO: progress bar? faster without print?
	public static void CIFextract(RandomAccessFile inStream) throws IOException
	{//little endian
		inStream.seek(0);
		
		byte[] id = new byte[24];
		inStream.read(id);
		String idS = new String(id);
		
		//Crystal Skull and after
		if(idS.compareTo("CIF FILE HerInteractive\0") != 0 && idS.compareTo("CIF TREE HerInteractive\0") != 0 && idS.compareTo("CIF TREE WayneSikes\0\0\0\0\0") != 0&& idS.compareTo("CIF FILE WayneSikes\0\0\0\0\0") != 0)
		{
			System.out.println("Unrecognised CIF header: " + idS);
			return;
		}
		
		int verMajor = Helpers.readShortLittleEndian(inStream);
		int verMinor = Helpers.readShortLittleEndian(inStream);
		if(Main.arg.verbose )
			System.out.println("Cif version: " + verMajor + "." + verMinor);
		
		if(verMajor == 3 && verMinor == 0)
			CIF3.parsev3(inStream);
		else if(verMajor == 2 && verMinor == 0)
			CIF2_0.parse2_0(inStream);
		else if(verMajor == 2 && verMinor == 1)
			parse2_1(inStream);
		//only change seems to be magic number
		else if(verMajor == 2 && verMinor == 2)
			CIF2_1_1.cif2_1_1(inStream, false);
		//tga now has header already
		else if(verMajor == 2 && verMinor == 3)
			CIF2_1_1.cif2_1_1(inStream, true);
		else
			System.out.println("Version " + verMajor + "." + verMinor + " not supported.");				

	}
	

	//Stay Tuned - White Wolf
	//2.1 changed early on so have to do a check. 2.2 and 2.3 are more consistant
	private static void parse2_1(RandomAccessFile inStream)throws IOException
	{
		
		/*
		 * Chunk types are officially documented as:
		 * PLAIN --  image that IS NOT used as a transparent overlay
	     * DECAL --  image that IS used as a transparent overlay
	     * DATA  --  non-image data such as the Cif Listing file.
		 */
	
		//try to detect game version
		//seek to part of the first header chunk to see if value is zero
		inStream.seek(2115);
		int formatTest = Helpers.readIntLittleEndian(inStream);
		inStream.seek(28);
		
		if(formatTest==0)
		{
			//older game
			inStream.seek(2159);
			int formatTest2 = Helpers.readIntLittleEndian(inStream);
			inStream.seek(28);

			//stay tuned
			if(formatTest2==1)
			{
				CIF2_1_0.cif2_1_0(inStream,true);
			}
			else
				CIF2_1_0.cif2_1_0(inStream,false);
		}


		else
			//newer game
			CIF2_1_1.cif2_1_1(inStream,false);
		
		//spacing for print
		if(Main.arg.verbose) System.out.println();
	}
//External cif files seem different. Garbage data
	/////BROKEN
	private static void parseFile(RandomAccessFile inStream)throws IOException
	{
		inStream.seek(76);
		int fileLength = Helpers.readIntLittleEndian("filelength: ", inStream);

		//still
		inStream.skipBytes(2);

		byte[] fileraw = new byte[fileLength];
		inStream.read(fileraw);

		Output.OutSetup(Main.inputWithoutExtension + Main.separator + "out",".raw");
		Main.outStream.write(fileraw);

		//subtract each bit by position
		//this was a basic attempt at encryption
		for(int j=0; j<fileraw.length;j++)
			fileraw[j]=(byte) (fileraw[j]-j);

		Output.OutSetup(Main.inputWithoutExtension + Main.separator + "out",".shift");
		Main.outStream.write(fileraw);

		fileraw= DecompressionManager.decompressLZSS(fileraw);

		Output.OutSetup(Main.inputWithoutExtension + Main.separator + "out",".lszz");
		Main.outStream.write(fileraw);
		System.exit(0);
	}
	
}
