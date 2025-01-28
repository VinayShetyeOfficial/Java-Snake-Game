import java.io.*;
import java.util.*;

public class HighScore {
    private static final String FILE_PATH = "highscores.dat";
    private List<Score> highScores = new ArrayList<>();

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

    public void saveScore(Score score) {
        highScores.add(score);
        Collections.sort(highScores, (a, b) -> b.score - a.score);
        if (highScores.size() > 10) {
            highScores = highScores.subList(0, 10);
        }
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FILE_PATH))) {
            oos.writeObject(highScores);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}