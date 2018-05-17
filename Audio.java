/**
 * Created by whitt on 5/16/2018.
 */

import java.util.*;
import javax.sound.sampled.*;

public class Audio {

    static float sampleRate = 44100;
    static int sampleSizeInBits = 16;
    static int channels = 1;          //mono
    static boolean signed = true;     //Indicates whether the data is signed or unsigned
    static boolean bigEndian = true;  //Indicates whether the audio data is stored in big-endian or little-endian

    Hashtable blah = new Hashtable();


    public static void main(String[] args){
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
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

            System.out.println("starting Recording");

            line.start();

            Thread stopper = new Thread(new Runnable() {
                @Override
                public void run() {
                    AudioInputStream stream = new AudioInputStream(line);

                    //File wavfile = new File("")
                }
            });

        } catch (LineUnavailableException ex) {
            // Handle the error ...
            System.out.println("Line unavailable");
        }
        return;
    }

}


