package org.sapphireforge.archive;

import org.sapphireforge.archive.nancydrew.CIF2_0;
import org.sapphireforge.archive.nancydrew.CIF2_1_0;
import org.sapphireforge.archive.nancydrew.CIF2_1_1;
import org.sapphireforge.archive.nancydrew.CIF3;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;

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
		if(idS.compareTo("CIF FILE HerInteractive\0") != 0 && idS.compareTo("CIF TREE WayneSikes\0\0\0\0\0") != 0)
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
		else
			System.out.println("Version " + verMajor + "." + verMinor + " not supported.");				

	}
	
	
	
	//Stay Tuned - White Wolf
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
		int formatTest = Helpers.readIntLittleEndian("Format Test: ", inStream);
		inStream.seek(28);
		
		if(formatTest==0)
			//older game
			CIF2_1_0.cif2_1_0(inStream);
		else
			//newer game
			CIF2_1_1.cif2_1_1(inStream);
		
		//spacing for print
		if(Main.arg.verbose) System.out.println();
	}
	
	
}
