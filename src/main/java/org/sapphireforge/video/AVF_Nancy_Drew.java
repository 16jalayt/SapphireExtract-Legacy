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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
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
        if (idS.compareTo("AVF WayneSikes\0") != 0)
        {
            System.out.println("Unrecognised CIF header: " + idS);
            return;
        }

        short UnknownShort = Helpers.readShortLittleEndian(inStream);
        if (UnknownShort != 512)
            System.out.println("UnknownShort should always be 512?: " + UnknownShort);

        int UnknownInt = Helpers.readIntLittleEndian(inStream);
        if (UnknownInt != 0)
            System.out.println("UnknownShort should always be 0?: " + UnknownShort);

        short numFrames = Helpers.readShortLittleEndian("# of frames: ", inStream);
        short width = Helpers.readShortLittleEndian("width of frames: ", inStream);
        short height = Helpers.readShortLittleEndian("height of frames: ", inStream);

        System.out.println("Extracting " + numFrames + " frames...");

        //seems to be constant 10 42 00 00 00 02
        inStream.skipBytes(6);

        for (int i = 0; i < numFrames; i++)
        {
            //spacing
            if (Main.arg.verbose) System.out.println("");

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
            for (int j = 0; j < frameraw.length; j++)
                frameraw[j] = (byte) (frameraw[j] - j);

            //data now needs to be lzss decompressed
            frameraw = DecompressionManager.decompressLZSS(frameraw);


            //The image tools in java suck

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            //fonvert from 555 to rgb colorspace manually
            int colR;
            int colG;
            int colB;
            List<Integer> out = new ArrayList<Integer>();

            for (int j = 0; j < frameraw.length; j += 2)
            {
                colR = (frameraw[j + 1] & 0x7C) << 1;
                colG = (frameraw[j + 1] & 0x03) << 6 | (frameraw[j] & 0xE0) >> 2;
                colB = (frameraw[j] & 0x1F) << 3;

                colR |= colR >> 5;
                colG |= colG >> 5;
                colB |= colB >> 5;

                out.add(colR);
                out.add(colG);
                out.add(colB);
            }

            //manually map pixels from the rgb array to a buffered image
            Integer[] intArray = new Integer[out.size()];
            intArray = out.toArray(intArray);
            for (int j = 0; j < intArray.length; j += 3)
            {
                int r = intArray[j];
                int g = intArray[j + 1];
                int b = intArray[j + 2];
                int col = (r << 16) | (g << 8) | b;
                img.setRGB(j / 3 % width, j / 3 / width, col);
            }

            File outfile = new File(Main.inputWithoutExtension + Main.separator + frameNumber + ".png");
            outfile.getParentFile().mkdirs();
            ImageIO.write(img, "png", outfile);

            inStream.seek(tableOffset);
        }
        if(numFrames>1)
            makeVid();
    }

    //copy pasted from ffmpegconv file and striped down
    public static void makeVid() throws IOException
    {
        String outputName = Main.inputWithoutExtension + Main.separator + Main.inputWithoutExtension + ".mp4";
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
