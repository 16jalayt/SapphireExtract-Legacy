package org.sapphireforge.program;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class Helpers 
{
	public static short readShortLittleEndian(RandomAccessFile file) throws IOException 
	{
		return readShortLittleEndian("", file);
	}
	public static short readShortLittleEndian(String message, RandomAccessFile file) throws IOException 
	{
		int a = file.readByte() & 0xFF;
		int b = file.readByte() & 0xFF;
		short res = (short) ((b << 8) | a);
		
		if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + res);
        return res;
    }
	public static short readShortBigEndian(RandomAccessFile file) throws IOException 
	{
		return readShortBigEndian("", file);
	}
	public static short readShortBigEndian(String message, RandomAccessFile file) throws IOException 
	{
		short res = file.readShort();
		if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + res);
		return res;
	}
	
	public static int readIntLittleEndian(RandomAccessFile file) throws IOException 
	{
		return readIntLittleEndian("", file);
	}
	public static int readIntLittleEndian(String message, RandomAccessFile file) throws IOException 
	{
        int a = file.readByte() & 0xFF;
        int b = file.readByte() & 0xFF;
        int c = file.readByte() & 0xFF;
        int d = file.readByte() & 0xFF;
        int res = (d << 24) | (c << 16) | (b << 8) | a;
        
        if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + res);
        return res;
    }
	
	public static int readIntBigEndian(RandomAccessFile file) throws IOException 
	{
		return readIntBigEndian("", file);
	}
	public static int readIntBigEndian(String message, RandomAccessFile file) throws IOException 
	{
		int res = file.readInt();
		if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + res);
		return res;
	}
	
	public static long readLongLittleEndian(RandomAccessFile file) throws IOException 
	{
		return readLongLittleEndian("", file);
	}
	public static long readLongLittleEndian(String message, RandomAccessFile file) throws IOException 
	{
        long a = file.readByte() & 0xFF;
        long b = file.readByte() & 0xFF;
        long c = file.readByte() & 0xFF;
        long d = file.readByte() & 0xFF;
        long e = file.readByte() & 0xFF;
        long f = file.readByte() & 0xFF;
        long g = file.readByte() & 0xFF;
        long h = file.readByte() & 0xFF;
        long res = (h << 56) | (g << 48) | (f << 40) | (e << 32) | (d << 24) | (c << 16) | (b << 8) | a;
        
        if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + res);
        return res;
    }
	
	public static long readLongBigEndian(RandomAccessFile file) throws IOException 
	{
		return readLongBigEndian("", file);
	}
	public static long readLongBigEndian(String message, RandomAccessFile file) throws IOException 
	{
		long res = file.readLong();
		if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + res);
		return res;
	}
	
	public static byte[] readByteArray(int len, RandomAccessFile file) throws IOException 
	{
		return readByteArray(len, "", file);
	}
	public static byte[] readByteArray(int len, String message, RandomAccessFile inStream) throws IOException 
	{
		byte[] bArray = new byte[len];
		inStream.read(bArray);
        
        if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + bArray);
        return bArray;
    }
	
	public static String readByteArrayString(int len, RandomAccessFile file) throws IOException 
	{
		return readByteArrayString(len, "", file);
	}
	public static String readByteArrayString(int len, String message, RandomAccessFile inStream) throws IOException 
	{
		byte[] bArray = new byte[len];
		inStream.read(bArray);
		String bArrayString = new String(bArray).trim();
        
        if (ParseInput.verbose && message.compareTo("") != 0)
			System.out.println(message + bArrayString);
        return bArrayString;
    }
	
	public static String byteArrayToHex(byte[] a) 
	{
		   StringBuilder sb = new StringBuilder(a.length * 2);
		   for(byte b: a)
		      sb.append(String.format("%02x", b));
		   return sb.toString();
		}
	
	//stupid big endian java...
	public static int little2big(byte[ ] b) 
	{
	    return ((b[3]&0xff)<<24)+((b[2]&0xff)<<16)+((b[1]&0xff)<<8)+(b[0]&0xff);
	}
	
	public static byte[] hexStringToByteArray(String s) 
	{
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) 
	    {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	public static String[] getTextLines(String path)
	{
		ArrayList<String> rawLines = new ArrayList<String>();
		
		try
		{
			InputStream in = Helpers.class.getClassLoader().getResourceAsStream(path);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null)
			{
				rawLines.add(line);
			}
			
			
			in.close();
			
		}
		catch (Exception e)
		{
			System.out.println("err:"+e);
		}
		String[] lines = rawLines.toArray(new String[rawLines.size()]);
		return lines;
	}
	
	public static boolean continuePrompt()
	{
		System.out.println("Would you like to Continue or Quit");
		String usrIn = ParseInput.user.nextLine().toLowerCase();
        if(usrIn.equals("c") || usrIn.equals("continue"))
        	return true;
        else if(usrIn.equals("q") || usrIn.equals("quit"))
        	return false;
        else
        	return true;

	}
	
	public static String genmd5(byte[] input)
	{
        try 
        { 
            MessageDigest md = MessageDigest.getInstance("MD5"); 
            byte[] messageDigest = md.digest(input); 
            BigInteger no = new BigInteger(1, messageDigest); 
            String hashtext = no.toString(16); 
            while (hashtext.length() < 32) 
            { 
                hashtext = "0" + hashtext; 
            } 
            return hashtext; 
        }  
  
        catch (NoSuchAlgorithmException e) 
        { 
            throw new RuntimeException(e); 
        } 
	}
	
	public static String byte2Bin(byte b) 
	{
	    return String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
	}
	
	public static short ByteToShort(byte x) 
	{
	    return (short) (x & 0xFF);
	}
	
	public static int unsigned(int val)
	{
		if (val < 0)
			val = val + 256;
		return val;
	}
	//use Short.toUnsignedInt insted
	public static int unsignedShort(short val)
	{
		int result;
		if (val < 0)
			result = val + 65536;
		return val;
	}
}