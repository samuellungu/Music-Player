import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener{
    // this will be used to update isPaused more synchronously
    private static final Object playSignal = new Object();

    //gui will be updated from this class
    private final MusicPlayerGUI musicPlayerGUI;

    // we will need a way to store our song's details, so we will be creating a song class
    private Song currentSong;
    public Song getCurrentSong() {
        return currentSong;
    }

    private ArrayList<Song> playlist;
    private int currentPlaylistIndex, currentFrame, currentTimeInMilli;

    //this object handles playing of the current music
    private AdvancedPlayer advancedPlayer;
    private boolean isPaused;

    private boolean songFinished;
    private boolean pressedNext, pressedPrev;

    private String songArtist;
    private String songTitle;

    public void setCurrentFrame(int frame){
        currentFrame = frame;
    }

    public int getCurrentFrame(){
        return currentFrame;
    }

    public void setCurrentTimeInMilli(int timeInMilli){
        currentTimeInMilli = timeInMilli;
    }

    public MusicPlayer(MusicPlayerGUI gui) {
        this.musicPlayerGUI = gui;

    }

    public void loadSong(Song song){
        currentSong = song;
        playlist = null;
        
        if(!songFinished){
            stopSong();
        }
        if(currentSong != null){
            currentFrame=0;

            currentTimeInMilli=0;

            musicPlayerGUI.setPlaybackSliderValue(0);

            playCurrentSong();
        }
    }

    public void loadPlaylist(File playlistFile){
        playlist = new ArrayList<>();

        try {
            FileReader reader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String songPath;

            while((songPath = bufferedReader.readLine())!=null){
                Song song = new Song(songPath);
                playlist.add(song);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //after loading the song we should start playing the first one
        if(playlist.size()>0){
            // reset playback slider
            musicPlayerGUI.setPlaybackSliderValue(0);

            //make sure to start from the beginning
            currentFrame = 0;
            currentTimeInMilli = 0;
            currentSong = playlist.get(0);

            // update gui
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);
            playCurrentSong();
        }
    }

    public void pauseSong(){
        if(advancedPlayer != null){
            isPaused = true;
            stopSong();
        }
    }

    public void nextSong(){
        if(playlist==null)return;

        //if this is the last song in the playlist just return
        if(++currentPlaylistIndex>playlist.size()-1)return;

        pressedNext = true;
        currentPlaylistIndex++;
        currentSong = playlist.get(currentPlaylistIndex);

        playCurrentSong();
    }

    void stopSong() {
        if(advancedPlayer !=null){
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;

        }
    }

    public void previousSong(){
        if(playlist==null)return;
        if(--currentPlaylistIndex<0)return;

        pressedPrev = true;
        if(!songFinished){
            stopSong();
        }
        currentPlaylistIndex--;
        currentSong = playlist.get(currentPlaylistIndex);
        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;

        // update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);
        playCurrentSong();
    }

    public void playCurrentSong(){

        if(currentSong== null)return;

        try {
            // read mp3 audio data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            //initialize new advancedPlayer
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            //start music
            startMusicThread();

            // start playback slider thread
            startPlaybackSliderThread();

        } catch (FileNotFoundException | JavaLayerException e) {
            e.printStackTrace();
        }

    }

    private void startMusicThread() {
        new Thread(() -> {
            try {
                if(isPaused){
                    synchronized (playSignal){
                        isPaused=false;
                        playSignal.notifyAll();
                    }
                    //the music should start playing from the currentframe
                    songArtist = currentSong.getSongArtist();
                    songTitle = currentSong.getSongTitle();
                    advancedPlayer.play(currentFrame, Integer.MAX_VALUE);

                }
                else{
                    advancedPlayer.play();
                }

            } catch (JavaLayerException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void startPlaybackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                if(isPaused){
                    try{
                        synchronized (playSignal){
                            playSignal.wait();
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                while(!isPaused && !songFinished && !pressedNext && !pressedPrev){
                    currentTimeInMilli++;

                    int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentSong.getFrameRatePerMilliseconds());

                    // update gui
                    musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        }).start();
    }
    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // this method gets called in the beginning of the song
        System.out.println("Playback Started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // this method gets called when the song finishes or if the player gets closed
        System.out.println("Playback Finished");
        if(isPaused){
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        }else {
            // if the user pressed next or prev we don't need to execute the rest of the code
            if (pressedNext || pressedPrev) return;

            // when the song ends
            songFinished = true;

            if (playlist == null) {
                // update gui
                musicPlayerGUI.enablePlayButtonDisablePauseButton();
            } else {
                // last song in the playlist
                if (currentPlaylistIndex == playlist.size() - 1) {
                    // update gui
                    musicPlayerGUI.enablePlayButtonDisablePauseButton();
                } else {
                    // go to the next song in the playlist
                    nextSong();
                }
            }
        }
    }

}
