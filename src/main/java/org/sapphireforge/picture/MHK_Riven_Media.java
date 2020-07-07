package org.sapphireforge.picture;

import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.RandomAccessFile;

public class MHK_Riven_Media 
{
	//thanks to https://github.com/buildist/Riven-imageData-Extractor
	private static final int[] BPP = new int[]{1, 4, 8, 16, 24};
	
	public static byte[] MHK_BMP_Convert(RandomAccessFile inStream, int fileLength, int fileOffset) throws IOException, InvalidObjectException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BufferedImage outputImage;
		
		
		if (Main.arg.verbose) {System.out.println();}
		byte[] bitmap = {0};
		
		//only supposed to use last 10 bytes
		int width = Helpers.readShortBigEndian("Width:", inStream)  & 0x3ff;
		int height = Helpers.readShortBigEndian("Height:", inStream)  & 0x3ff;
		//bpr has to be even
		int bytesPerRow = Helpers.readShortBigEndian("Bytes per row:", inStream) & 0x3fe;
		short isCompressed = Helpers.readShortBigEndian(inStream);
		//always 03 04
		int unknown = Helpers.readShortBigEndian(inStream);
		
		short bitsPerColor = inStream.readByte();
		if(bitsPerColor != 24) {System.out.println("Unknown bits per color " + bitsPerColor); return bitmap;}
		
		int colorCount = inStream.readByte() + 1;
		if(colorCount == 0)
			colorCount = 256;
		if(colorCount != 256) {System.out.println("Nonstandard color count = " + colorCount);}
		Color[] colors = new Color[colorCount];
		for (int i = 0; i < colorCount; i++) 
		{
	          int b = Helpers.unsigned(inStream.readByte());
	          int g = Helpers.unsigned(inStream.readByte());
	          int r = Helpers.unsigned(inStream.readByte());
	          //System.out.println(b + ":" + g + ":" + r);
	          colors[i] = new Color(r, g, b);
	    }
		
		//wierd bitmasking. 3 values stored in compression value
		int bpp = BPP[isCompressed & 0b111];
        int secondaryCompression = (isCompressed & 0b11110000) >> 4;
        int primaryCompression = (isCompressed & 0b111100000000) >> 8;
        
        if(bpp != 8){System.out.println("Unrecognised color depth + " + bpp); return bitmap;}
        if(secondaryCompression != 0){System.out.println("Unrecognised secondary compression + " + secondaryCompression); return bitmap;}
        if((primaryCompression != 4) && (primaryCompression != 0)){System.out.println("Unrecognised primary compression + " + primaryCompression); return bitmap;}
		
        if (primaryCompression == 0) 
        {System.out.println("uncompressed/////////////////////////////////////");
        	outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
            	for (int x = 0; x < bytesPerRow; x++) 
            	{
            		int colorIndex = Helpers.unsigned(inStream.readByte());
            		Color color = colors[colorIndex];
            		if (x < width) outputImage.setRGB(x, y, color.getRGB());
            	}
            }
            ImageIO.write(outputImage, "png", outputStream);
            outputStream.flush();
            bitmap = new byte[outputStream.size()];
            bitmap = outputStream.toByteArray();
            return bitmap;
        }
        else 
        {
        	//unknown
        	inStream.readInt();
        	byte[] raw = new byte[(int) (fileOffset + fileLength - inStream.getFilePointer())];
        	System.out.println(raw.length + ":" + fileOffset + ":" + fileLength + ":" + inStream.getFilePointer());
        	inStream.read(raw);
        	int[] image = new int[bytesPerRow * height];
        	int p = 0;
            int q = 0;
        	
            //raw.length
            while (p < raw.length) 
            {
            	int cmd = Helpers.unsigned(raw[p]);
            	p++;
            	
            	
            	
            	if (cmd == 0) 
            	{
            		//End of stream
            		break;
            	} 
            	else if (cmd <= 0x3f) 
            	{
            		
            		// Output n pixel duplets, where n is the command value itself. Pixel data comes
                    // immediately after the command as 2*n bytes representing direct indices in the 8-bit
                    // color table.
            		for (int i = 0; i < cmd; i++) 
            		{
            			image[q] = Helpers.unsigned(raw[p]);
            			image[q + 1] = Helpers.unsigned(raw[p + 1]);
            			p += 2;
            			q += 2;
            		}
            	}
            	else if (cmd <= 0x7f) 
            	{
                    // Repeat last 2 pixels n times, where n = command_value & 0x3F.
                    int pixel1 = image[q - 2];
                    int pixel2 = image[q - 1];
                    for (int i = 0; i < (cmd & 0x3f); i++) 
                    {
                      image[q] = pixel1;
                      image[q + 1] = pixel2;
                      q += 2;
                    }
                  } 
            	else if (cmd <= 0xbf) 
            	{
                    // Repeat last 4 pixels n times, where n = command_value & 0x3F.
                    int pixel1 = image[q - 4];
                    int pixel2 = image[q - 3];
                    int pixel3 = image[q - 2];
                    int pixel4 = image[q - 1];
                    for (int i = 0; i < (cmd & 0x3f); i++) 
                    {
                      image[q] = pixel1;
                      image[q + 1] = pixel2;
                      image[q + 2] = pixel3;
                      image[q + 3] = pixel4;
                      q += 4;
                    }
            	} 
            	else
            	{
            		// Begin of a subcommand stream. This is like the main command stream, but contains
                    // another set of commands which are somewhat more specific and a bit more complex.
                    // This command says that command_value & 0x3F subcommands will follow.
                    int subCount = cmd & 0x3f;
                    for (int i = 0; i < subCount; i++) 
                    {
                      int sub = Helpers.unsigned(raw[p]);
                      //System.out.println(p + ":" + cmd + ":" + sub + ":" + subCount);
                      p++;
                      if (sub >= 0x01 && sub <= 0x0f) 
                      {
                        // 0000mmmm
                        // Repeat duplet at relative position -m, where m is given in duplets. So if m=1,
                        // repeat the last duplet.
                        int offset = -(sub & 0b00001111) * 2;
                        image[q] = image[q + offset];
                        image[q + 1] = image[q + offset + 1];
                        q += 2;
                      } 
                      else if (sub == 0x10) {
                        // Repeat last duplet, but change second pixel to p.
                        image[q] = image[q - 2];
                        image[q + 1] = Helpers.unsigned(raw[p]);
                        p++;
                        q += 2;
                      } 
                      else if (sub >= 0x11 && sub <= 0x1f) 
                      {
                        // 0001mmmm
                        // Output the first pixel of last duplet, then pixel at relative position -m. m is
                        // given in pixels. (relative to the second pixel!)
                        int offset = -(sub & 0b00001111) + 1;
                        image[q] = image[q - 2];
                        image[q + 1] = image[q + offset];
                        q += 2;
                      } 
                      else if (sub >= 0x20 && sub <= 0x2f) 
                      {
                        // 0010xxxx
                        // Repeat last duplet, but add x to second pixel.
                        image[q] = image[q - 2];
                        image[q + 1] = image[q - 1] + (sub & 0b00001111);
                        q += 2;
                      } 
                      else if (sub >= 0x30 && sub <= 0x3f) 
                      {
                        // 0011xxxx
                        // Repeat last duplet, but subtract x from second pixel.
                        image[q] = image[q - 2];
                        image[q + 1] = image[q - 1] - (sub & 0b00001111);
                        q += 2;
                      } 
                      else if (sub == 0x40)
                      {
                        // Repeat last duplet, but change first pixel to p.
                        image[q] = Helpers.unsigned(raw[p]);
                        image[q + 1] = image[q - 1];
                        p++;
                        q += 2;
                      } 
                      else if (sub >= 0x41 && sub <= 0x4f) 
                      {
                        // 0100mmmm
                        // Output pixel at relative position -m, then second pixel of last duplet.
                        int offset = -(sub & 0b00001111);
                        image[q] = image[q + offset];
                        image[q + 1] = image[q - 1];
                        q += 2;
                      } 
                      else if (sub == 0x50) 
                      {
                        // Output two absolute pixel values, p1 and p2.
                        image[q] = Helpers.unsigned(raw[p]);
                        image[q + 1] = Helpers.unsigned(raw[p + 1]);
                        p += 2;
                        q += 2;
                      } 
                      else if (sub >= 0x51 && sub <= 0x57)
                      {
                        // 01010mmm p
                        // Output pixel at relative position -m, then absolute pixel value p.
                        int offset = -(sub & 0b00000111);
                        image[q] = image[q + offset];
                        image[q + 1] = Helpers.unsigned(raw[p]);
                        p++;
                        q += 2;
                      } 
                      else if (sub >= 0x59 && sub <= 0x5f) 
                      {
                        // 01011mmm p
                        // Output absolute pixel value p, then pixel at relative position -m.
                        // (relative to the second pixel!)
                        int offset = -(sub & 0b00000111) + 1;
                        image[q] = Helpers.unsigned(raw[p]);
                        image[q + 1] = image[q + offset];
                        p++;
                        q += 2;
                      } 
                      else if (sub >= 0x60 && sub <= 0x6f) 
                      {
                        // 0110xxxx p
                        // Output absolute pixel value p, then (second pixel of last duplet) + x.
                        image[q] = Helpers.unsigned(raw[p]);
                        image[q + 1] = image[q - 1] + (sub & 0b00001111);
                        p++;
                        q += 2;
                      } 
                      else if (sub >= 0x70 && sub <= 0x7f) 
                      {
                        // 0111xxxx p
                        // Output absolute pixel value p, then (second pixel of last duplet) - x.
                        image[q] = Helpers.unsigned(raw[p]);
                        image[q + 1] = image[q - 1] - (sub & 0b00001111);
                        p++;
                        q += 2;
                      } 
                      else if (sub >= 0x80 && sub <= 0x8f) 
                      {
                        // 1000xxxx
                        // Repeat last duplet adding x to the first pixel.
                        image[q] = image[q - 2] + (sub & 0b00001111);
                        image[q + 1] = image[q - 1];
                        q += 2;
                      } 
                      else if (sub >= 0x90 && sub <= 0x9f)
                      {
                        // 1001xxxx p
                        // Output (first pixel of last duplet) + x, then absolute pixel value p.
                        image[q] = image[q - 2] + (sub & 0b00001111);
                        image[q + 1] = Helpers.unsigned(raw[p]);
                        p++;
                        q += 2;
                      } 
                      else if (sub == 0xa0) 
                      {
                        // 0xa0 xxxxyyyy
                        // Repeat last duplet, adding x to the first pixel and y to the second.
                        int x = (Helpers.unsigned(raw[p]) & 0b11110000) >> 4;
                        int y = Helpers.unsigned(raw[p]) & 0b00001111;
                        image[q] = image[q - 2] + x;
                        image[q + 1] = image[q - 1] + y;
                        p++;
                        q += 2;
                      } 
                      else if (sub == 0xb0) 
                      {
                        // 0xb0 xxxxyyyy
                        // Repeat last duplet, adding x to the first pixel and subtracting y to the
                        // second.
                        int x = (Helpers.unsigned(raw[p]) & 0b11110000) >> 4;
                        int y = Helpers.unsigned(raw[p]) & 0b00001111;
                        image[q] = image[q - 2] + x;
                        image[q + 1] = image[q - 1] - y;
                        p++;
                        q += 2;
                      }
                      else if (sub >= 0xc0 && sub <= 0xcf) 
                      {
                        // 1100xxxx
                        // Repeat last duplet subtracting x from first pixel.
                        image[q] = image[q - 2] - (sub & 0b00001111);
                        image[q + 1] = image[q - 1];
                        q += 2;
                      } 
                      else if (sub >= 0xd0 && sub <= 0xdf) 
                      {
                        // 1101xxxx p
                        // Output (first pixel of last duplet) - x, then absolute pixel value p.
                        image[q] = image[q - 2] - (sub & 0b00001111);
                        image[q + 1] = Helpers.unsigned(raw[p]);
                        p++;
                        q += 2;
                      } 
                      else if (sub == 0xe0) 
                      {
                        // 0xe0 xxxxyyyy
                        // Repeat last duplet, subtracting x from first pixel and adding y to second.
                        int x = (Helpers.unsigned(raw[p]) & 0b11110000) >> 4;
                        int y = Helpers.unsigned(raw[p]) & 0b00001111;
                        image[q] = image[q - 2] - x;
                        image[q + 1] = image[q - 1] + y;
                        p++;
                        q += 2;
                      } 
                      else if (sub == 0xf0 || sub == 0xff) 
                      {
                        // 0xfx xxxxyyyy
                        // Repeat last duplet, subtracting x from first pixel and y from second.
                        int x = ((sub & 0b00001111) << 4) | ((Helpers.unsigned(raw[p]) & 0b11110000) >> 4);
                        int y = Helpers.unsigned(raw[p]) & 0b00001111;
                        image[q] = image[q - 2] - x;
                        image[q + 1] = image[q - 1] - y;
                        p++;
                        q += 2;
                      }
                      else if ((sub & 0b10100000) == 0b10100000 && sub != 0xfc) 
                      {
                        // 1x1xxxmm mmmmmmmm
                        // Repeat n duplets from relative position -m (given in pixels, not duplets). If r
                        // is 0, another byte follows and the last pixel is set to that value. n and r come
                        // from the table on the right.
                        int n, r;
                        if (sub >= 0xa4 && sub <= 0xa7) 
                        {
                          n = 2;
                          r = 0;
                        } 
                        else if (sub >= 0xa8 && sub <= 0xab)
                        {
                          n = 2;
                          r = 1;
                        } 
                        else if (sub >= 0xac && sub <= 0xaf) 
                        {
                          n = 3;
                          r = 0;
                        } 
                        else if (sub >= 0xb4 && sub <= 0xb7)
                        {
                          n = 3;
                          r = 1;
                        } 
                        else if (sub >= 0xb8 && sub <= 0xbb) 
                        {
                          n = 4;
                          r = 0;
                        } 
                        else if (sub >= 0xbc && sub <= 0xbf) 
                        {
                          n = 4;
                          r = 1;
                        } 
                        else if (sub >= 0xe4 && sub <= 0xe7) 
                        {
                          n = 5;
                          r = 0;
                        } 
                        else if (sub >= 0xe8 && sub <= 0xeb) 
                        {
                          n = 5;
                          r = 1;
                        } 
                        else if (sub >= 0xec && sub <= 0xef) 
                        {
                          n = 6;
                          r = 0;
                        }
                        else if (sub >= 0xf4 && sub <= 0xf7) 
                        {
                          n = 6;
                          r = 1;
                        } 
                        else if (sub >= 0xf8 && sub <= 0xfb) 
                        {
                          n = 7;
                          r = 0;
                        } 
                        else 
                        {
                          throw new RuntimeException("subcommand: " + sub);
                        }

                        int offset = -(Helpers.unsigned(raw[p]) | ((sub & 0b00000011) << 8));
                        p++;
                        for (int j = 0; j < n; j++) 
                        {
                          image[q + 2 * j] = image[q + offset + 2 * j];
                          image[q + 2 * j + 1] = image[q + offset + 2 * j + 1];
                        }
                        q += 2 * n;
                        if (r == 0) 
                        {
                          image[q - 1] = Helpers.unsigned(raw[p]);
                          p++;
                        }
                      } 
                      else if (sub == 0xfc) 
                      {
                        // 0xfc nnnnnrmm mmmmmmmm (p)
                        // Repeat n+2 duplets from relative position -m (given in pixels, not duplets). If
                        // r is 0, another byte p follows and the last pixel is set to absolute value p.
                        int n = (Helpers.unsigned(raw[p]) & 0b11111000) >> 3;
                        int r = (Helpers.unsigned(raw[p]) & 0b00000100) >> 2;
                        int offset = -(Helpers.unsigned(raw[p + 1]) | ((Helpers.unsigned(raw[p]) & 0b00000011) << 8));

                        for (int j = 0; j < n + 2; j++) 
                        {
                          image[q + 2 * j] = image[q + offset + 2 * j];
                          image[q + 2 * j + 1] = image[q + offset + 2 * j + 1];
                        }
                        p += 2;
                        q += 2 * n + 4;
                        if (r == 0) 
                        {
                          image[q - 1] = Helpers.unsigned(raw[p]);
                          p++;
                        }
                      } 
                      else 
                      {
                        throw new RuntimeException("subcommand: " + sub);
                      }
                    }
                  }
            	}
            outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int i = 0;
            for (int y = 0; y < height; y++) {
            	for (int x = 0; x < bytesPerRow; x++) 
            	{
            		int colorIndex = image[i];
                    if (colorIndex < 0) 
                      colorIndex = 255;
                    Color color = colors[colorIndex & 0xff];
                    if (x < width) outputImage.setRGB(x, y, color.getRGB());
                    i++;
            	}
            }
            ImageIO.write(outputImage, "png", outputStream);
            outputStream.flush();
            bitmap = new byte[outputStream.size()];
            bitmap = outputStream.toByteArray();
            return bitmap;
            
        }
	}
}
