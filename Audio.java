/**
 * Created by whitt on 5/16/2018.
 */

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
    static int channels = 2;
    static int frameSize = 4;
    static boolean signed = true;     //Indicates whether the data is signed or unsigned
    static boolean bigEndian = false;  //Indicates whether the audio data is stored in big-endian or little-endian
    static int[] RANGE = new int[] { 40, 80, 120, 180, 300 };
    static int FUZ_FACTOR = 2;
    static Hashtable<double[], int[]> allTheFingerprints = new Hashtable<>();



    public static void main(String[] args){
        Audio.newRecording();

    }

    public static void newRecording() {
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

                    File wavfile = new File("record.wav");

//                    System.out.println("Made it in here");
                    try {
                        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, wavfile);
                        System.out.println("Stopped Recording");
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            };

            thread.start();
//            thread.run();
//            System.out.println("HELLO");
            Thread.sleep(5000);
            line.stop();
            line.close();

        } catch (LineUnavailableException ex) {
            // Handle the error ...
            System.out.println("Line unavailable");
            ex.printStackTrace();
        } catch (InterruptedException ie) {ie.printStackTrace();}

        System.out.println("I made it this far");
        File file = new File("record.wav");
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
//        System.out.println(Arrays.toString(bytes));
        final int totalSize = bytes.length;
        System.out.print("This is the length of the byte array: ");
        System.out.println(totalSize);

        final int chunkSize = 4*1024; //4kB recommended chunk size
        int sampleChunks = totalSize/chunkSize;

        //Use complex numbers for freq domain
        Complex[][] res = new Complex[sampleChunks][];

        for(int i = 0; i < sampleChunks; i++) {
            Complex[] complexArray = new Complex[4*1024];

            for(int j = 0; j < chunkSize; j++) {
                complexArray[i] = new Complex((double) bytes[(i*chunkSize)+j], (double) 0);
            }
//            Perform FFT analysis on the chunk:
            res[i] = Complex.fft(complexArray);
            System.out.println("LOOK HERE");
            System.out.println(res[i]);
        }

        // Now create digital fingerprint
//        createHashPrint(res);
    }

    // find out in which range is frequency
    public static int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq)
            i++;
        return i;
    }

//    public static double[] createHashPrint(Complex[][] result){
//
//
//        double[] hashes = new double[result.length];
//
//
//        for (int i=0; i < result.length; i++) {
//            double[] highscores = new double[] {0, 0, 0, 0};
//            for (int freq=40; freq < 400; freq++) {
//                // Get the magnitude:
//                double mag = Math.log(result[i][freq].abs() + 1); //why is there a log here
//
//                // Find out which range we are in:
//                int index = getIndex(freq);
//
//                // Save the highest magnitude and corresponding frequency:
//                if (mag > highscores[index]) {
//                    highscores[index] = freq;
//                }
//            }
//            hashes[i] = hash(highscores);
//        }
//        return hashes;
//    }

    private static double hash(double[] highscores) {
        double p1 = highscores[0];
        double p2 = highscores[1];
        double p3 = highscores[2];
        double p4 = highscores[3];

        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }

}


