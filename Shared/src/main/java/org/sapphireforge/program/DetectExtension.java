package org.sapphireforge.program;

import org.sapphireforge.archive.*;
import org.sapphireforge.audio.*;
import org.sapphireforge.picture.MPO;
import org.sapphireforge.video.AVF_Nancy_Drew;
import org.sapphireforge.video.FFmpegConv;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/*TODO:
 * show file structure in files
 * verbose option displaying file names
 * gui by default
 * some sort of skip file if not overwrite
 * tbv datavoc2 sound broken
 * print file names as go? ~.3 sec slower on 11.5 sec
 *zip ask overwrite/improved handling
 *fix overwirte prompt invalid choice nullptr exception
 *auto rename
 *command line auto rename
 *allow commandline picture format
 *use junit Assert.assertEquals for sanity checks
 *check file exists before try to parse
 */

/*REF:
 * QuickBMS:
 * short = 2 byte
 * long = 4 byte
 * 
 * Java:
 * short/char = 2 byte
 * int = 4 byte
 * long = 8 byte
 * 
 * word = short
 * dword = int
 */
//TODO:use hash for no magic

public class DetectExtension 
{
	public static void DetectExt(RandomAccessFile inStream) throws IOException
	{
		//extension blacklist for batch looping
		if (Arrays.asList(".mp3",".wav",".bmp",".png",".tga",".dds",".avi",".exe",".dll",".so",".dylib",".app",".wri",".ico",".hlp",".txt",".bat").contains(ParseInput.inputExtension.toLowerCase()))
		{
			return;
		}
		//tgq sort of works, but too slow
		else if (Arrays.asList(".mpg", ".mp2", ".mp4",".tgq", ".flv", ".ogv", ".bik", ".avi", ".wmv", ".mkv").contains(ParseInput.inputExtension.toLowerCase()))
		{
			FFmpegConv.convert();
		}
		else if(ParseInput.inputExtension.toLowerCase().equals(".m4b"))
		{
			M4B_MystIV.M4BExtract(inStream);
			return;
		}
		else if(ParseInput.inputExtension.toLowerCase().equals(".akb"))
		{
			AKB_FFIV.AKBextract(inStream);
			return;
		}
		else if(ParseInput.inputExtension.toLowerCase().equals(".he4"))
		{
			HE4_Humongous.HE4extract(inStream);
			return;
		}
		else if(ParseInput.inputExtension.toLowerCase().equals(".dmu"))
		{
			HE4_Humongous.DMUextract(inStream);
			return;
		}
		
		else if(ParseInput.inputExtension.toLowerCase().equals(".mpo"))
		{
			MPO.MPOextract(inStream);
			return;
		}
		else if(ParseInput.inputExtension.toLowerCase().equals(".ima") || ParseInput.inputExtension.toLowerCase().equals(".img"))
		{
			FAT_Windows.FATextract(inStream);
			return;
		}
		
		else
		{
			//files with 3 byte id check
			ParseInput.inStream.seek(0);
			byte[] format = new byte[3];
			ParseInput.inStream.read(format);
				
			if(new String(format).equals("HIS"))
			{
				ParseInput.inStream.seek(32);
				byte[] test = new byte[3];
				ParseInput.inStream.read(test);
				if(new String(test).equals("Ogg"))
				{
					HIS_Nancy_Drew.HISextractOGG(inStream, true);
				}
				else
				{
					//varient from venice and ? shifted back 2 bytes
					ParseInput.inStream.seek(30);
					test = new byte[3];
					ParseInput.inStream.read(test);
					if(new String(test).equals("Ogg"))
					{
						HIS_Nancy_Drew.HISextractOGG(inStream, false);
					}
					else
						HIS_Nancy_Drew.HISextractWAV(inStream);
				}
				
				
				return;
			}
			
			else if(new String(format).equals("Her"))
			{
				HIS_Nancy_Drew.HISextractWAV(inStream);
				return;
			}
			if(new String(format).equals("CIF"))
			{
				CIF_Nancy_Drew.CIFextract(inStream);
				return;
			}
			if(new String(format).equals("AVF"))
			{
				AVF_Nancy_Drew.AVFextract(inStream);
				return;
			}
			
			if(new String(format).equals("MHW"))
			{
				MHK_Riven.MHKextract(inStream);
				return;
			}
			
			else if(new String(format).equals("TBV"))
			{
				TBV_3D_Ultra.TBVextract(inStream);
				return;
			}
			
			/*
			else if(new String(format).equals("BPU"))
			{
				HPK_Tropico.HPKextract(inStream);
				return;
			}*/
			
			else if(new String(format).equals("GDP"))
			{
				PCK_godot_engine.PCKextract(inStream);
				return;
			}
			
			else if(new String(format).equals("LiO"))
			{
				LUG_Lionhead_Studios.LUGextract(inStream);
				return;
			}
			
			else if(new String(format).equals("ton"))
			{
				PAK_minigolf.DATextract(inStream, 2);
				return;
			}
			
			else if(new String(format).equals("00#"))
			{
				PAK_minigolf.DATextract(inStream, 1);
				return;
			}
			
			else if(new String(format).equals("BIG"))
			{
				BIG_Potter.BIGextract(inStream);
				return;
			}
			
			//BE EF CA FE
			else if(Arrays.equals(format, new byte[]{(byte)0xBE, (byte)0xEF, (byte)0xCA}))
			{
				NPK_Nvidia.NPKextract(inStream);
				return;
			}
			else
			{
				//files with 2 byte id check
				ParseInput.inStream.seek(0);
				format = new byte[2];
				ParseInput.inStream.read(format);

				//AC DC
				if (Arrays.equals(format, new byte[]{(byte) 0xAC, (byte) 0xDC}))
				{
					DATA_Sandlot.DATAextract(inStream);
					return;
				}
				//1f 8b
				else if (Arrays.equals(format, new byte[]{(byte) 0x1f, (byte) 0x8b}))
				{
					DATA_Sandlot.ZDATAextract(inStream);
					return;
				}


				if (ParseInput.inputWithoutExtension.toLowerCase().contains("css") && ParseInput.inputExtension.toLowerCase().compareTo(".dat") == 0)
				{
					CSSDAT_RCT.DATextract(inStream);
					return;
				}
				
			///////files with no header
				ParseInput.inStream.seek(0);
				
				//Gunnar dat file compare to known file lengths
				int header = Helpers.readIntLittleEndian(ParseInput.inStream);
				//		sound			text		interface			scenes
				if(header == 52 || header == 68 || header == 459 || header == 1321)
				{
					DAT_Plan_It_Green.DATextract(inStream);
					return;
				}
				
				if(header == 2)
				{
					SCN_WDWExplorer.SCNextract(inStream);
					return;
				}
				
				System.out.println("File does not match any builtin extractors. Trying generic zip.");
				if (!DecompressionManager.unzip())
				{
					System.out.println("Format not recognised");
					System.exit(404);
				}
				
			}
		}
		
		
	
	}
}
