package org.sapphireforge.program;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

//icon from http://www.iconarchive.com/show/flatastic-7-icons-by-custom-icon-design/Extract-object-icon.html

public class Main 
{
	public static File infile;
	public static File outfile;
	public static File outdir;
	public static RandomAccessFile inStream;
	public static FileOutputStream outStream;
	public static String inputWithExtension;
	public static String inputWithoutExtension;
	public static String inputExtension;
	public static String inputPath;
	public static String inputFull;
	public static String separator;
	//Usable by programs
	public static Scanner user;
	//public static boolean overwriteAll = false;
	//public static boolean autoRename = false;
	//public static int fileCount = 0;
	//public static boolean verbose = true;
	public static Args arg;
	
	public static void main(String[] args) 
	{		
		user = new Scanner(System.in);
		arg = new Args();
		
		JCommander jc  = JCommander.newBuilder()
		  .addObject(arg)
		  .build();
		
		jc.setProgramName("MultiExtract");
		jc.setAllowParameterOverwriting(true);
		jc.setCaseSensitiveOptions(false);
		try
		{
			jc.parse(args);
		}
		catch(ParameterException e)
		{
			System.out.println();
			jc.usage();
            return;
		}
		
		
		if (arg.help)
		{
			System.out.println();
			jc.usage();
            return;
		}
		
		//BasicConfigurator.configure();
		//Assert.assertEquals(arg.verbose.intValue(), 2);

		/*if (args.length < 1)
		{
			System.out.println("Incorrect usage.\nCorrect usage is (MultiExtract.exe [File name] [flags])\nValid flags are:\n-r Auto rename\n-a Auto overwrite all conflicts");
			return;
		}*/
		///////////////////////Hates utf-8
		if (Files.exists(Paths.get(args[0])) == false)
		{
			System.out.println("Not a valid file");
			return;
		}
		
		/*for(int i=1; i<args.length;i++)
		{
			if(args[i].compareTo("-r") == 0)
			{
				autoRename = true;
				System.out.println("Auto rename is currently broken and will be ignored");
			}
			else if (args[i].compareTo("-a") == 0)
			{
				overwriteAll = true;
			}
			else 
			{
				System.out.println("Invalid command line flag:"+args[i]);
			}
		}*/
		
		try 
		{
			infile = new File(args[0]);
			if(!infile.exists())
			{
				System.out.println("File doesnt exist");
				return;
			}

			inputFull = args[0];
			separator = System.getProperty("file.separator");
			//inputPath = args[0].substring(0, args[0].lastIndexOf(separator)+1);
			inputPath = infile.getAbsolutePath().substring(0, infile.getAbsolutePath().lastIndexOf(separator)+1);
			inputExtension = args[0].substring(args[0].lastIndexOf("."), args[0].length());
			inputWithExtension = args[0].substring(args[0].lastIndexOf(separator) +1, args[0].length());
			inputWithoutExtension = inputWithExtension.substring(0, inputWithExtension.lastIndexOf("."));

			inStream = new RandomAccessFile(infile, "r"); 	        
			DetectExtension.DetectExt(inStream);
	        inStream.close();
	        if (outStream != null)
	        	outStream.close();
	        System.out.println("Done");
	        System.exit(0);
	    } 
		catch (IOException e) 
		{
	    	System.out.println("Error: " + e);
	    	System.exit(-1);
		}
	}
}