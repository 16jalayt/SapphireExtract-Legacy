package org.sapphireforge.archive;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.ParseInput;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FAT_Windows
{
    public static void FATextract(RandomAccessFile inStream) throws IOException
    {
        System.out.println("FAT not finished! Will likely not work.");
        
        inStream.seek(0);
        if(ParseInput.verbose) System.out.println("Searching for start of partition...");

        //loop to find start of fat block
        while(true)
        {
            try
            {
                byte testByte = inStream.readByte();
                //System.out.println(Integer.toHexString(testByte));
                if(testByte==(byte)0xEB)
                {
                    if(ParseInput.verbose) System.out.println("Found 0xEB (first marker)");
                    inStream.skipBytes(1);
                    byte testByte2 = inStream.readByte();
                    if(testByte2==(byte)0x90)
                    {
                        if(ParseInput.verbose) System.out.println("Found 0x90 (second marker). Partition start found.");
                        break;
                    }
                }
            }
            catch(EOFException e)
            {
                System.out.println("Encountered end of file while searching for start marker. Is this a fat image?");
                return;
            }
        }

        //start of partition found. Skip back to beginning to follow spec more accurately.
        inStream.seek(inStream.getFilePointer() - 3);

        //x86 assembly to jump to bootcode. Irrelevant to extract
        byte[] BS_jmpBoot = new byte[3];
        inStream.read(BS_jmpBoot);

        byte[] BS_OEMName = new byte[8];
        inStream.read(BS_OEMName);
        if(ParseInput.verbose) System.out.println("Fat image formatted by: " + new String(BS_OEMName));

        //# of bytes per sector. Only valid: 512, 1024, 2048 or 4096
        int BPB_BytsPerSec = Helpers.readIntLittleEndian("Bytes per sector: ", inStream);

        //Number of sectors per allocation unit. Must be a power of 2
        byte BPB_SecPerClus = inStream.readByte();
        if(ParseInput.verbose) System.out.println("Sectors per unit: " +  BPB_SecPerClus);

        
    }
}
