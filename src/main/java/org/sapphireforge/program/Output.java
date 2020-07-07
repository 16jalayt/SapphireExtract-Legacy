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
		Main.outfile = new File(new String(Main.inputPath + filename + format));
		//System.out.println(new File(new String(Main.inputPath + Main.inputWithoutExtension + Main.separator)).isDirectory()); 

		if (Main.outStream != null)
        	Main.outStream.close();
		
		if (Main.outfile.isDirectory())
		{
			 System.out.println("create dir"); 
			 //redundent to main.outifle
			File dir = new File(Main.inputPath + Main.separator + filename);
            if (!dir.exists()) 
            {
                dir.mkdirs();
            }
		}
		else 
		if (!Main.outfile.getParentFile().exists()) 
		{
		    try
		    {
		    	Main.outfile.getParentFile().mkdirs();
		    } 
		    catch(SecurityException se)
		    {
		    	 System.out.println("Error creating directory"); 
		    	 System.exit(0);
		    }        
		}
	    if (Main.outfile.exists()) 
	    {  
	    	/*if(Main.autoRename)
	    	{
	    		OutSetup(filename + "-" + Main.fileCount, format);
	    	}*/
	        if (Main.arg.overwriteAll) 	
	        {
	        	Main.outStream = new FileOutputStream(Main.outfile);
	        }	        
	        else
	        {
	        	System.out.println("Output file " + Main.outfile.getName() + " exists. Overwrite?");
	        	String usrIn = Main.user.nextLine().toLowerCase();
		        if(usrIn.equals("y"))
		        {
		        	Main.outStream = new FileOutputStream(Main.outfile);
		        }
		        else if(usrIn.equals("a"))
		        {
		        	Main.outStream = new FileOutputStream(Main.outfile);
		        	Main.arg.overwriteAll = true;
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
		        	Main.outStream = null;
		            return;
		        }
	        }   
	    }
	    else
	    {
	    	Main.outStream = new FileOutputStream(Main.outfile);
	    }
	    
		
	}
	
	public static void OutSetup(String format) throws IOException
	{
		OutSetup(Main.inputWithoutExtension, format);
	}
	
}