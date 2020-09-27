package org.sapphireforge.program;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Output 
{
	/////////////make another case with path paramater/ actually fix. All option for overwrite
	//format can be empty string
	public static void OutSetup(String filename, String format) throws IOException
	{
		//make not location dependent with sub folder support
		//Main.outdir = new File(new String(System.getProperty("user.dir") + "/bin/ext/" + pathext));  
		
		//doesnt need to be static?
		//Main.outfile = new File(new String(Main.inputPath + "ext" + Main.separator + filename + format));
		ParseInput.outfile = new File(new String(ParseInput.inputPath + filename + format));
		//System.out.println(new File(new String(Main.inputPath + Main.inputWithoutExtension + Main.separator)).isDirectory()); 

		if (ParseInput.outStream != null)
        	ParseInput.outStream.close();
		
		if (ParseInput.outfile.isDirectory())
		{
			 System.out.println("create dir"); 
			 //redundent to main.outifle
			File dir = new File(ParseInput.inputPath + ParseInput.separator + filename);
            if (!dir.exists()) 
            {
                dir.mkdirs();
            }
		}
		else 
		if (!ParseInput.outfile.getParentFile().exists())
		{
		    try
		    {
		    	ParseInput.outfile.getParentFile().mkdirs();
		    } 
		    catch(SecurityException se)
		    {
		    	 System.out.println("Error creating directory"); 
		    	 System.exit(0);
		    }        
		}
	    if (ParseInput.outfile.exists())
	    {  
	    	/*if(Main.autoRename)
	    	{
	    		OutSetup(filename + "-" + Main.fileCount, format);
	    	}*/
	        if (ParseInput.arg.overwriteAll)
	        {
	        	ParseInput.outStream = new FileOutputStream(ParseInput.outfile);
	        }	        
	        else
	        {
	        	System.out.println("Output file " + ParseInput.outfile.getName() + " exists. Overwrite?");
	        	String usrIn = ParseInput.user.nextLine().toLowerCase();
		        if(usrIn.equals("y"))
		        {
		        	ParseInput.outStream = new FileOutputStream(ParseInput.outfile);
		        }
		        else if(usrIn.equals("a"))
		        {
		        	ParseInput.outStream = new FileOutputStream(ParseInput.outfile);
		        	ParseInput.arg.overwriteAll = true;
		        }
		       /* else if(usrIn.equals("r"))
		        {
		        	Main.outStream = new FileOutputStream(Main.outfile);
		        	Main.autoRename = true;
		        }*/
		        else if(usrIn.equals("n"))
		        {
		        	//abort
		        	return;
		        }
		        
		        else
		        {
		        	//gives null pointer excep. probs better solution
		        	ParseInput.outStream = null;
		            return;
		        }
	        }   
	    }
	    else
	    {
	    	ParseInput.outStream = new FileOutputStream(ParseInput.outfile);
	    }
	    
		
	}
	
	public static void OutSetup(String format) throws IOException
	{
		OutSetup(ParseInput.inputWithoutExtension, format);
	}
	
}