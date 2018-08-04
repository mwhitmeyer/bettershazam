/**
 * Created by whitt on 5/16/2018.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    static int[] RANGE = new int[] { 40, 80, 120, 180, 3000 };
    static int FUZ_FACTOR = 2;
    static Map<Long, LinkedList<String>> allTheFingerprints = new HashMap<>();

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
        int length = (int) sampleRate*5*sampleSizeInBits/8; // why multiply by 5 here?
        byte[] bytes = new byte[length];
        try {
            nRead = din.read(bytes, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (nRead == -1) {
//            System.out.println("uh oh");
            return;
        }
        System.out.println(Arrays.toString(bytes));
        final int totalSize = bytes.length;
//        System.out.print("This is the length of the byte array: ");
//        System.out.println(totalSize);

        final int chunkSize = 4*1024; //4kB recommended chunk size
        int sampleChunks = totalSize/chunkSize;

        //Use complex numbers for freq domain
        Complex[][] res = new Complex[sampleChunks][];

        for(int i = 0; i < sampleChunks; i++) {
            Complex[] complexArray = new Complex[chunkSize];
//            System.out.println();

            for(int j = 0; j < chunkSize; j++) {
                complexArray[j] = new Complex(bytes[(i*chunkSize)+j], (double) 0);
//                System.out.println("Print bytes array index");
//                System.out.println((i*chunkSize)+j);
//                System.out.println(complexArray[i].toString());

            }
//            System.out.println(complexArray.toString());

            //nth root of unity
            int len = complexArray.length;
            double real = Math.cos(2*Math.PI / len);
            double imag = Math.sin(2*Math.PI / len);
            Complex nru = new Complex(real, imag);


            //Perform FFT analysis on the chunk:
            res[i] = Complex.fft(complexArray, nru);
//            System.out.println("LOOK HERE");
//            System.out.println(res[i]);
        }

        // Now create digital fingerprint
        getSignature(res, false, "");
    }

    // find out in which range is frequency
    public static int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq) {
            i++;
        }
        return i;
    }

    public static void getSignature(Complex[][] result, boolean putInDatabaseOrNot, String songName){
        // within each interval, identify the frequency with the highest magnitude
        // forms a signature hash for this chunk of the song
        //result is song chunk/interval
        System.out.println("Inside getSignature, here is result length");
        System.out.println(result.length);

        long[][] allPoints = new long[result.length][4];

        for (int i=0; i < result.length; i++) {

            double[][] highscores = new double[result.length][400-40];

            for (int freq=40; freq < 400; freq++) {
                // Get the magnitude:
                double mag = Math.log(result[i][freq].abs() + 1); //why is there a log here
//                System.out.println("Mag is: ");
//                System.out.println(mag);

                // Find out which range we are in:
                int index = getIndex(freq);

                // Save the highest magnitude and corresponding frequency:
                if (mag > highscores[i][index]) {
                    highscores[i][index] = mag;
                    try {
                        allPoints[i][index] = freq;
                    } catch (ArrayIndexOutOfBoundsException ex) {
                        System.out.println("Array Index Out of Bounds Exception");

                        System.out.println("Freq is: ");
                        System.out.println(freq);

                        System.out.println("Index is: ");
                        System.out.println(index);
                    }
                }
            }

            // hash
            long h = hashThis(
                    allPoints[i][0],
                    allPoints[i][1],
                    allPoints[i][2],
                    allPoints[i][3]
            );

            System.out.println("Here is the hash: ");
            System.out.println(h);

            // True: Need to put in database
            if (putInDatabaseOrNot) {
                Audio.putToMap(allTheFingerprints, h, songName + Integer.toString(i));

            } else {
                // False: Looking it up in database

            }

        }
//        return everyHighscore;
    }

    public static long hashThis(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }

    public static void fingerprintFullSong(String wavFile) throws IOException {
        Path path = Paths.get(wavFile);
        String songName = wavFile;
        byte[] data = Files.readAllBytes(path);

        final int totalSize = data.length;

        final int chunkSize = 4*1024; //4kB recommended chunk size
        int sampleChunks = totalSize/chunkSize;

        //Use complex numbers for freq domain
        Complex[][] res = new Complex[sampleChunks][];

        for(int i = 0; i < sampleChunks; i++) {
            Complex[] complexArray = new Complex[4*1024];

            for(int j = 0; j < chunkSize; j++) {
                complexArray[j] = new Complex((double) data[(i*chunkSize)+j], (double) 0);
            }

            //nth root of unity
            int len = complexArray.length;
            double real = Math.cos(2*Math.PI / len);
            double imag = Math.sin(2*Math.PI / len);
            Complex nru = new Complex(real, imag);

            //Perform FFT analysis on the chunk:
            res[i] = Complex.fft(complexArray, nru);
        }

        // call getSignature
        getSignature(res, true, songName);

    }

    public static void putToMap(Map map, long hash, String details) {

//        LinkedList<String> songs = (LinkedList<String>) map.get(freq);
        if (songs == null) {
            songs = new LinkedList<String> ();
            songs.add(details);
            map.put(freq, songs);
        } else {
            songs.add(details);
            map.put(freq, songs);
        }

    }



//    @Override
//    public int hashCode() {
//        return hash()
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


