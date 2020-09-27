import org.sapphireforge.program.ParseInput;

import java.io.File;
import java.io.IOException;

public class CLIMain
{
    public static void main(String args[])
    {
        System.out.println("cli test");
        if(args.length == 0)
        {
            try
            {
                Runtime.getRuntime().exec("SapphireExtractGUI.exe", null, new File(System.getProperty("user.dir")));
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            System.exit(0);
        }

        ParseInput.parse(args);
    }
}
