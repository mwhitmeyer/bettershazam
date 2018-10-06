/**
 * Created by whitt on 5/16/2018.
 */



import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.sound.sampled.*;
//import javafx.util.Pair;
//import static com.sun.tools.doclets.formats.html.markup.HtmlStyle.details;
//import org.math.plot.*;


public class Audio implements java.io.Serializable{

    static AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    static float sampleRate = 44100;
    static int sampleSizeInBits = 16;
    static int channels = 2;
    static int frameSize = 4;
    static boolean signed = true;     //Indicates whether the data is signed or unsigned
    static boolean bigEndian = false;  //Indicates whether the audio data is stored in big-endian or little-endian
    static int[] RANGE = new int[] { 40, 80, 120, 180, 3000 };
    static int FUZ_FACTOR = 2;
    static HashMap<Long, LinkedList<Pair<String, Integer>>> allTheFingerprints = new HashMap<Long, LinkedList<Pair<String, Integer>>>();

    public static void main(String[] args) throws IOException {

        Pair<Integer, Integer> hi = new Pair<>(1, 1);

        Audio.deserializeDatabase();
//        Audio.fingerprintFullSong("Who_let_the_dogs_out.wav");
//        Audio.fingerprintFullSong("In_My_Feelings.wav");
//        Audio.fingerprintFullSong("Booty_From_A_Distance.wav");
//        Audio.fingerprintFullSong("Shape_Of_You.wav");
//        Audio.serializeDatabase();
        Audio.newRecording();


    }

    public static void deserializeDatabase() {
        try {
            FileInputStream fileIn = new FileInputStream("database.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            allTheFingerprints = (HashMap<Long, LinkedList<Pair<String, Integer>>>) in.readObject();
            in.close();
            fileIn.close();
            if (allTheFingerprints == null) {
                allTheFingerprints = new HashMap<Long, LinkedList<Pair<String, Integer>>>();
            }
            System.out.print("here is allTheFingerprints: ");
            System.out.println(allTheFingerprints);
        } catch (IOException i) {
            i.printStackTrace();
            return;
        } catch (ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }
    }

    public static void serializeDatabase() {
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("database.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(allTheFingerprints);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in database.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
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
//        System.out.println(Arrays.toString(bytes));
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

        long[][] allPoints = new long[result.length][5];

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

//            System.out.println("Here is the hash: ");
//            System.out.println(h);

            HashMap<String, LinkedList<Integer>> PossibleSongs = new HashMap();

            // True: Need to put in database
            if (putInDatabaseOrNot) {
                Audio.putToMap(allTheFingerprints, h, new Pair<String, Integer> (songName, i));

            } else {
                // False: Looking it up in database
                LinkedList<Pair<String, Integer>> listOfPairs = allTheFingerprints.get(h);
                if (listOfPairs == null) {
                    //System.out.println("NOT FOUND IN DATABASE");
                } else {
                    System.out.println("Found a match!!!!");
                    ListIterator<Pair<String,Integer>> listIterator = listOfPairs.listIterator();
                    while(listIterator.hasNext()) {
                        Pair<String, Integer> pair = listIterator.next();
                        String key = pair.getKey();
                        Integer value = pair.getValue();

                        if (PossibleSongs.get(key) == null) {
                            // New possible song
                            LinkedList<Integer> list = new LinkedList<Integer>();
                            list.add(value);
                            PossibleSongs.put(key, list);
                            //Audio.putToMap();  need to put into Possible Songs!!!
                        } else {
                            // Add time interval to existing possible song
                            LinkedList<Integer> list = PossibleSongs.get(key);
                            list.add(value);
                        }
                    }

                    // add logic to check that each sequential case in linked list is chronological

                    Integer max = 0;
                    String mostProbable = "";
                    Iterator it = PossibleSongs.keySet().iterator();
                    while(it.hasNext()) {
                        String songname = it.next().toString();
                        Integer len = PossibleSongs.get(songname).size();
                        if (len > max) {
                            max = len;
                            mostProbable = songname;
                        } else {
                            System.out.println("SOMETHING WENT WRONG");
                        }
                    }

                    System.out.println("Most probable song is: ");
                    System.out.println(mostProbable);

                }
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
        //Need a list of songs, where list index is Song ID, String is songName
        System.out.println("inside fingerprintfullsong with: ");
        System.out.print(wavFile);

        Path path = Paths.get(wavFile);
        String songName = wavFile;
        byte[] data = Files.readAllBytes(path);

//        System.out.println(Arrays.toString(data));

        final int totalSize = data.length;

        System.out.println(totalSize);

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

    public static void putToMap(HashMap<Long, LinkedList<Pair<String, Integer>>> map, long hash, Pair<String, Integer> pair) {
        System.out.print("here is the map: ");
        System.out.println(map);
        LinkedList<Pair<String, Integer>> songs = map.get(hash);
        if (songs == null) {
            songs = new LinkedList<Pair<String, Integer>> ();
            songs.add(pair);
            map.put(hash, songs);
        } else {
            songs.add(pair);
            map.put(hash, songs);
        }

//        System.out.println("Successfully entered: ");
//       // System.out.print(details);
//        System.out.print(" into database with the hash ");
//        System.out.print(hash);

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


