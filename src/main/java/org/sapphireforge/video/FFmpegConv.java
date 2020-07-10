package org.sapphireforge.video;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFmpegUtils;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.builder.FFmpegBuilder.Verbosity;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import org.sapphireforge.program.Main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FFmpegConv 
{
	public static void convert() throws IOException
	{
		//Config
		//TODO if input same as output make converted directory
		File dir = new File(Main.inputPath + "output");
		dir.mkdirs();
		String outputName = Main.inputPath + "output" + Main.separator + Main.inputWithoutExtension + ".mp4";
		String inputName = Main.inputFull;
		
		String outputCodec = "libx265";
		
		//libx265 cant process flv
		//should repeat to get into x265
		if(Main.inputExtension.equals(".flv"))
			outputCodec = "libx264";

		String path = (System.getProperty("user.dir") + Main.separator + "ffmpeg" + Main.separator);
		//determine win or lin. shouldnt be needed. below works without extension
		//System.getProperty("os.name");
		FFmpeg ffmpeg = new FFmpeg(path + "ffmpeg");
		FFprobe ffprobe = new FFprobe(path + "ffprobe");
		
		
		//note ffmpeg wont edit if multi is closed first. cant change deps or override
		/*String[] command;
			final Process childProcess = new ProcessBuilder(command).start();

		Thread closeChildThread = new Thread() {
    		public void run() {
        		childProcess.destroy();
    		}
		};

		Runtime.getRuntime().addShutdownHook(closeChildThread); */
		
		
		//check if just doing a transcode
		/*if (Main.inputFull.equals(Main.inputPath + outputName))
		{			
			File directory = new File(Main.inputPath + "output");
		    if (! directory.exists()){
		        directory.mkdir();
		    }
			outputName = Main.inputPath + "output" + Main.separator + Main.inputWithoutExtension +  Main.inputExtension;
		}
		*/
		final FFmpegProbeResult probeResult = ffprobe.probe(inputName);

		FFmpegFormat format = probeResult.getFormat();
		System.out.format("%nFile: '%s' ; Format: '%s' ; Duration: %.3fs", 
			format.filename, 
			format.format_long_name,
			format.duration
		);

		FFmpegStream stream = probeResult.getStreams().get(0);
		System.out.format("%nCodec: '%s' ; Width: %dpx ; Height: %dpx\n\n",
			stream.codec_long_name,
			stream.width,
			stream.height
		);

		
		
		//notes. will overwrite output - TODO implement  exist check before
		FFmpegBuilder builder = new FFmpegBuilder()
		  .setInput(probeResult)     // Filename, or a FFmpegProbeResult
		  .overrideOutputFiles(Main.arg.overwriteAll) // Override the output if it exists
		  .setVerbosity(Verbosity.ERROR)
		  .addOutput(outputName)   // Filename for the destination
		  .setAudioCodec("aac")        // using the aac codec

		  //nvenc makes huge files, but gpu accelerated. h265 smallest file. "gpu only good for on the fly"
		  .setVideoCodec(outputCodec)     //default: libx264 or libx265 or nvenc_hevc, rec: libx265 
		  .addExtraArgs("-preset", "fast", "-map", "0:v", "-map", "0:a?", "-c:s?", "copy")

		  .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL) // Allow FFmpeg to use experimental specs
		  
		  .done();

		//builder.readAtNativeFrameRate();
		FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
		
		System.out.println(builder.toString());
		System.out.println("\n\n");
		
		// Run a one-pass encode
		executor.createJob(builder, new ProgressListener()
		{

			// Using the FFmpegProbeResult determine the duration of the input
			final double duration_ns = probeResult.getFormat().duration * TimeUnit.SECONDS.toNanos(1);

			@Override
			public void progress(Progress progress)
			{
				double percentage = progress.out_time_ns / duration_ns;

				// Print out interesting information about the progress
				System.out.print(String.format(
					"\r[%.0f%%] status:%s frame:%d time:%s ms fps:%.0f speed:%.2fx  ",
					percentage * 100,
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
