import java.io.InputStream;
import javax.sound.sampled.*;

/**
 * SoundManager handles the playback of sound effects for the game.
 */
public class SoundManager {
    private static final String EAT_SOUND = "/asset/food.wav";
    private static final String GAME_OVER_SOUND = "/asset/gameover.wav";

    /**
     * Plays a sound from the specified file path.
     *
     * @param soundFilePath Path to the sound file
     */
    public void playSound(String soundFilePath) {
        try {
            // Load the sound file as a resource
            InputStream audioSrc = getClass().getResourceAsStream(soundFilePath);
            if (audioSrc == null) {
                System.err.println("Sound file not found: " + soundFilePath);
                return;
            }
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioSrc);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}