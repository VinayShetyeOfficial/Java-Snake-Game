import java.io.*;
import java.util.*;

/**
 * HighScore manages the high scores for the game, allowing saving and
 * retrieval.
 */
public class HighScore {
    private static final String FILE_PATH = "highscores.dat";
    private List<Score> highScores = new ArrayList<>();

    /**
     * Score represents a player's score, including their name and the date
     * achieved.
     */
    static class Score implements Serializable {
        String playerName;
        int score;
        Date date;

        Score(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
            this.date = new Date();
        }
    }

    /**
     * Saves a new score, maintaining a sorted list of top scores.
     *
     * @param score The score to be saved
     */
    public void saveScore(Score score) {
        highScores.add(score);
        Collections.sort(highScores, (a, b) -> b.score - a.score);
        if (highScores.size() > 10) {
            highScores = highScores.subList(0, 10);
        }
        saveToFile();
    }

    /**
     * Saves the high scores to a file.
     */
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FILE_PATH))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}