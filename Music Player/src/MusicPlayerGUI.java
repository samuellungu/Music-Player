import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MusicPlayerGUI extends JFrame {
    // color configurations
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    private MusicPlayer musicPlayer;

    private JFileChooser jFileChooser;
    private JLabel songArtist, songTitle;
    private JPanel playbackBtns;
    private JSlider playbackSlider;


    public MusicPlayerGUI() {
        super("Music Player");

        // set the width and height
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // launch the app at the center of the screen
        setLocationRelativeTo(null);

        // prevent the app from being resized
        setResizable(false);

        // set layout to null which allows us to control the (x, y) coordinates of our components
        // and also set the height and width
        setLayout(null);

        // change the frame color
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();
        jFileChooser.setCurrentDirectory(new File("D:\\Projects\\Music Player\\src\\assets"));
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));

        addGuiComponents();

    }

    private void addGuiComponents() {
        addToolBar();
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth()-20, 225);
        add(songImage);

        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 285, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        // song artist
        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 315, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        // playback slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                musicPlayer.pauseSong();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();
                int frame = source.getValue();
                musicPlayer.setCurrentFrame(frame);

                musicPlayer.setCurrentTimeInMilli((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));
                musicPlayer.playCurrentSong();
                enablePauseButtonDisablePlayButton();
            }
        });
        
        add(playbackSlider);
        addPlaybackBtns();
    }

    private void addToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        toolBar.setFloatable(false);

        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile !=null) {
                    Song song = new Song(selectedFile.getPath());
                    musicPlayer.loadSong(song);

                    updateSongTitleAndArtist(song);
                    enablePauseButtonDisablePlayButton();
                }

            }
        });
        songMenu.add(loadSong);

        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                new MusicPlaylistDialog(MusicPlayerGUI.this).setVisible(true);
            }
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setFileFilter(new FileNameExtensionFilter("Playlist", "txt"));
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if(result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    //load the music
                    musicPlayer.stopSong();

                    musicPlayer.loadPlaylist(selectedFile);
                }
            }

        });
        playlistMenu.add(loadPlaylist);
        add(toolBar);

    }
    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth()-10, 80);
        playbackBtns.setBackground(null);
        JButton prevButton = new JButton(loadImage("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //musicPlayer.stopSong();
                musicPlayer.previousSong();
            }
        });
        playbackBtns.add(prevButton);
        JButton pauseButton = new JButton(loadImage("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                enablePlayButtonDisablePauseButton();
                musicPlayer.pauseSong();
            }
        });
        playbackBtns.add(pauseButton);

        // play button
        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // toggle off play button and toggle on pause button
                enablePauseButtonDisablePlayButton();

                // play or resume song
                musicPlayer.playCurrentSong();
            }
        });
        playbackBtns.add(playButton);
        // next button
        JButton nextButton = new JButton(loadImage("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go to the next song
                musicPlayer.nextSong();
            }
        });
        playbackBtns.add(nextButton);
        add(playbackBtns);
    }

    private ImageIcon loadImage(String imagePath) {

        try {
            BufferedImage image = ImageIO.read(new File(imagePath));

            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setPlaybackSliderValue(int i) {
    }

    public void updateSongTitleAndArtist(Song song) {
        songArtist.setText(song.getSongArtist());
        songTitle.setText(song.getSongTitle());
    }

    public void updatePlaybackSlider(Song currentSong) {
    }

    public void enablePauseButtonDisablePlayButton() {
        JButton pauseButton = (JButton)playbackBtns.getComponent(1);
        JButton playButton = (JButton)playbackBtns.getComponent(2);
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);

        playButton.setVisible(false);
        playButton.setEnabled(false);
    }
    public void enablePlayButtonDisablePauseButton() {
    }
}
