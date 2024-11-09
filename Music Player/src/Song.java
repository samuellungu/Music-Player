
import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.Mp3File;
import com.mpatric.mp3agic.UnsupportedTagException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;


public class Song {
    private String songTitle;
    private String songArtist;
    private String songLength;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;

    public Song(String filePath){
        this.filePath = filePath;

        try {
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double)mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            songLength = convertToSongLengthFormat();
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // read through the meta data of the audio file
            Tag tag = audioFile.getTag();
            if(tag!=null){
                songTitle = tag.getFirst(FieldKey.TITLE);
                songArtist = tag.getFirst(FieldKey.ARTIST);
            }else{
                songTitle = "N/A";
                songArtist = "N/A";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String convertToSongLengthFormat() {
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getSongLength() {
        return songLength;
    }

    public String getFilePath() {
        return filePath;
    }

    public Mp3File getMp3File() {
        return mp3File;
    }

    public double getFrameRatePerMilliseconds() {
        return frameRatePerMilliseconds;
    }
    public String getSongTitle(){
        System.out.println("This song is entitled "+songTitle);
        return songTitle;
    }

    public String getSongArtist(){
        System.out.println("The Artist is "+songArtist);
        return songArtist;
    }
}
