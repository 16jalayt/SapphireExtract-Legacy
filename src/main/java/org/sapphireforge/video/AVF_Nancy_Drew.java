package org.sapphireforge.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.sapphireforge.program.DecompressionManager;
import org.sapphireforge.program.Helpers;
import org.sapphireforge.program.Main;
import org.sapphireforge.program.Output;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

public class AVF_Nancy_Drew 
{
	public static void AVFextract(RandomAccessFile inStream) throws IOException
	{
		inStream.seek(0);

		byte[] id = new byte[15];
		inStream.read(id);
		String idS = new String(id);

		//Crystal Skull and after
		if(idS.compareTo("AVF WayneSikes\0") != 0)
		{
			System.out.println("Unrecognised CIF header: " + idS);
			return;
		}

		short UnknownShort = Helpers.readShortLittleEndian(inStream);
		if(UnknownShort !=512)
			System.out.println("UnknownShort should always be 512?: "+UnknownShort);

		int UnknownInt = Helpers.readIntLittleEndian(inStream);
		if(UnknownInt !=0)
			System.out.println("UnknownShort should always be 0?: "+UnknownShort);

		short numFrames = Helpers.readShortLittleEndian("# of frames: ", inStream);
		short width = Helpers.readShortLittleEndian("width of frames: ", inStream);
		short height = Helpers.readShortLittleEndian("height of frames: ", inStream);

		//seems to be constant 10 42 00 00 00 02
		inStream.skipBytes(6);

		for(int i=0; i<numFrames; i++)
		//for(int i=0; i<2; i++)
		{
			//spacing
			if(Main.arg.verbose) System.out.println("");

			short frameNumber = Helpers.readShortLittleEndian("Frame #: ", inStream);
			int frameOffset = Helpers.readIntLittleEndian("Frame offset: ", inStream);
			int frameLength = Helpers.readIntLittleEndian("Frame length: ", inStream);

			//C0 C6 04 00 00 00 00 00 00
			inStream.skipBytes(9);
			//if next short!=i  wrong padding?

			long tableOffset = inStream.getFilePointer();
			inStream.seek(frameOffset);

			//read the file data
			byte[] frameraw = new byte[frameLength];
			inStream.read(frameraw);

			//subtract each bit by position
			//this was a basic attempt at encryption
			for(int j=0; j<frameraw.length;j++)
				frameraw[j]=(byte) (frameraw[j]-j);

			frameraw= DecompressionManager.decompressLZSS(frameraw);

			Output.OutSetup(Main.inputWithoutExtension + Main.separator + frameNumber,".raw");
			Main.outStream.write(frameraw);

			//need to append bmp header
			ByteBuffer buffer = ByteBuffer.allocate(frameraw.length + 54);
			buffer.order(ByteOrder.LITTLE_ENDIAN);
			buffer.put(new byte[] {0x42, 0x4d});
			buffer.putInt(313078);
			buffer.putInt(0);
			buffer.putInt(54);
			buffer.putInt(40);
			buffer.putInt(width);
			buffer.putInt(height);
			buffer.putShort((short) 1);
			buffer.putShort((short) 16);
			buffer.putInt(0);
			buffer.putInt(frameraw.length);
			buffer.putInt(0);
			buffer.putInt(0);
			buffer.putInt(256);
			buffer.putInt(0);
			buffer.put(frameraw);


//The image tools in java suck
			Output.OutSetup(Main.inputWithoutExtension + Main.separator + frameNumber,".bmp");
			Main.outStream.write(buffer.array());

			BufferedImage img = null;
			try {
				img = ImageIO.read(new File(Main.inputWithoutExtension + Main.separator + frameNumber+".bmp"));
			} catch (IOException e) {
				System.out.println(e);;
				System.out.println("Error reading temp file.");
				inStream.seek(tableOffset);
				continue;
			}

			//Main.outStream.close();
			//Main.outfile.delete();

			//AffineTransform at = new AffineTransform();
			//at.concatenate(AffineTransform.getScaleInstance(1, -1));
			//at.concatenate(AffineTransform.getTranslateInstance(0, -height));
			BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			//BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
			Graphics2D g = newImage.createGraphics();
			//g.transform(at);
			g.drawImage(img, 0, 0, null);

			ImageIO.write(newImage, "png", new File(Main.inputWithoutExtension + Main.separator + frameNumber+".png"));
			g.dispose();

			inStream.seek(tableOffset);
		}
		makeVid();
	}

	public static void makeVid() throws IOException
	{
		String outputName = Main.inputWithoutExtension + Main.separator +Main.inputWithoutExtension+".mp4";
		String outputCodec = "libx264";
		String path = (System.getProperty("user.dir") + Main.separator + "ffmpeg" + Main.separator);
		FFmpeg ffmpeg = new FFmpeg(path + "ffmpeg");

		FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(Main.inputWithoutExtension + Main.separator + "%d.png")     // Filename, or a FFmpegProbeResult
				.overrideOutputFiles(Main.arg.overwriteAll) // Override the output if it exists
				.setVerbosity(FFmpegBuilder.Verbosity.ERROR)
				.addOutput(outputName)   // Filename for the destination
				.setVideoCodec(outputCodec)
				.addExtraArgs("-preset", "fast", "-vf", "fps=15")
				.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
				.done();

		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg);

		System.out.println(builder.toString());
		System.out.println("\n\n");

		// Run a one-pass encode
		executor.createJob(builder, new ProgressListener()
		{


			@Override
			public void progress(Progress progress)
			{

				// Print out interesting information about the progress
				System.out.print(String.format(
						"\r status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx  ",
						progress.status,
						progress.frame,
						FFmpegUtils.toTimecode(progress.out_time_ns, TimeUnit.NANOSECONDS),
						progress.fps.doubleValue(),
						progress.speed
				));
			}
		}).run();
	}
}
