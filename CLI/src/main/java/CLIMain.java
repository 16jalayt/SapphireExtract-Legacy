import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.sapphireforge.program.ParseInput;

import java.io.File;
import java.io.IOException;

public class CLIMain
{

    public static void main(String args[])
    {
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

        Args arg = new Args();

        JCommander jc = JCommander.newBuilder()
                .addObject(arg)
                .build();

        jc.setProgramName("MultiExtract");
        jc.setAllowParameterOverwriting(true);
        jc.setCaseSensitiveOptions(false);
        try
        {
            jc.parse(args);
        } catch (ParameterException e)
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
/*
        ///////////////////////Hates utf-8
        if (Files.exists(Paths.get(args[0])) == false)
        {
            System.out.println("Not a valid file");
            return;
        }*/

        ParseInput.overwriteAll = arg.overwriteAll;
        ParseInput.autoRename = arg.autoRename;
        ParseInput.verbose = arg.verbose;
        ParseInput.raw = arg.raw;

        ParseInput.parseFile(new File(args[0]));
    }
}
