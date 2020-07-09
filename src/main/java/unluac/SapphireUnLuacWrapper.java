package unluac;

import unluac.decompile.Decompiler;
import unluac.decompile.Output;
import unluac.decompile.OutputProvider;
import unluac.parse.BHeader;
import unluac.parse.LFunction;

import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class SapphireUnLuacWrapper
{
    /*public static void main(String infile, String outfile) throws IOException
    {
        LFunction lmain = null;
        try {
            lmain = file_to_function(fn, config);
        } catch(IOException e) {
            error(e.getMessage(), false);
        }
        Decompiler d = new Decompiler(lmain);
        d.decompile();
        d.print();
    }

    private static LFunction file_to_function(String fn, Configuration config) throws IOException
    {
        RandomAccessFile file = new RandomAccessFile(fn, "r");
        ByteBuffer buffer = ByteBuffer.allocate((int) file.length());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int len = (int) file.length();
        FileChannel in = file.getChannel();
        while(len > 0) len -= in.read(buffer);
        buffer.rewind();
        BHeader header = new BHeader(buffer, config);
        return header.main;
    }

    public static void decompile(String in, String out) throws IOException
    {
        LFunction lmain = file_to_function(in, new Configuration());
        Decompiler d = new Decompiler(lmain);
        d.decompile();
        final PrintStream pout = new PrintStream(out);
        d.print(new Output(new OutputProvider() {

            @Override
            public void print(String s) {
                pout.print(s);
            }

            @Override
            public void print(byte b) {
                pout.print(b);
            }

            @Override
            public void println() {
                pout.println();
            }

        }));
        pout.flush();
        pout.close();
    }*/
}
