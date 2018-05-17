/**
 * Created by whitt on 5/16/2018.
 */

import java.util.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

public class AudioSystem {

    Hashtable blah = new Hashtable();

    private AudioFormat getFormat() {
        float sampleRate = 44100;
        int sampleSizeInBits = 16;
        int channels = 1;          //mono
        boolean signed = true;     //Indicates whether the data is signed or unsigned
        boolean bigEndian = true;  //Indicates whether the audio data is stored in big-endian or little-endian order
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    public static void main(String[] args){
        AudioFormat format = getFormat();
        TargetDataLine line;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            // Handle the error ...
            System.out.println("Line is not supported");
            return;

        }
        return;
    }

}


