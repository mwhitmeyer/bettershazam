/**
 * Created by whitt on 5/16/2018.
 */

import sun.misc.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import javax.sound.sampled.*;
import java.io.File;
//import org.math.plot.*;


public class Audio {

    static AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    static float sampleRate = 44100;
    static int sampleSizeInBits = 16;
    static int channels = 2;          //mono
    static int frameSize = 4;
    static boolean signed = true;     //Indicates whether the data is signed or unsigned
    static boolean bigEndian = false;  //Indicates whether the audio data is stored in big-endian or little-endian

    Hashtable blah = new Hashtable();


    public static void main(String[] args){
        // https://www.youtube.com/watch?v=GVtl19L9GxU
        AudioFormat format = new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, sampleRate, bigEndian);
        final TargetDataLine line;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error ...
            System.out.println("Line is not supported");
            return;

        }

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);

            System.out.println("Starting Recording");

            line.start();

            Thread thread = new Thread()
            {
                @Override
                public void run() {
                    AudioInputStream stream = new AudioInputStream(line);

                    File wavfile = new File("audio\\record.wav");
                    try { AudioSystem.write(stream, AudioFileFormat.Type.WAVE, wavfile);}
                    catch (IOException ioe) {ioe.printStackTrace();}
                    System.out.println("Stopped Recording");
                }
            };

            thread.start();
            Thread.sleep(5000);
            line.stop();
            line.close();

        } catch (LineUnavailableException ex) {
            // Handle the error ...
            System.out.println("Line unavailable");
            ex.printStackTrace();
        } catch (InterruptedException ie) {ie.printStackTrace();}

        File file = new File("audio\\record.wav");
        AudioInputStream in = null;
        try {
            in = AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        AudioInputStream din = null;
        AudioFormat baseFormat = in.getFormat();
        AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false);
        din = AudioSystem.getAudioInputStream(decodedFormat, in);
        //byte[] bytes = IOUtils.toByteArray(din);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead = 0;
        int length = (int) sampleRate*5*sampleSizeInBits/8;
        byte[] bytes = new byte[length];
        try {
            nRead = din.read(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (nRead == -1) {
            System.out.println("uh oh");
            return;
        }
        System.out.println(Arrays.toString(bytes));

    }

}


