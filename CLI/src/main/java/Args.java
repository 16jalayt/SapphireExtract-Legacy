import com.beust.jcommander.Parameter;

public class Args 
{
	//collect parameters without -, ie the filename
  @Parameter(required = true)
  String file = new String();

  @Parameter(names = { "-a", "-o", "--overwrite" }, description = "Overwrite all files")
  public boolean overwriteAll = false;

  @Parameter(names = {"-r", "--rename"}, description = "Auto rename existing files")
  public boolean autoRename = false;

  @Parameter(names = { "-v", "--verbose" }, description = "Print out extra information")
  public boolean verbose = false;
  
  @Parameter(names = { "-c", "--raw" }, description = "Skip decompalation. Dump compiled script")
  public boolean raw = false;
  
  @Parameter(names = { "-h", "--help"}, help = true)
  public boolean help;
  
  //do output path
  
  //can do , required = true
  //or , order = 0 to set priority in usage dialog
  //or , hidden = true
  
  //ffmpeg
  
  //output format string - issues with h264 on other formats. explicit check?
  //high quality flag
  //force h264
  //encoder string
  //nvidia or cpu flag
}