import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * GamePanel is responsible for rendering the game board and handling game
 * logic.
 * It extends JPanel and implements ActionListener for game updates.
 */
public class GamePanel extends JPanel implements ActionListener {

  // Constants for game dimensions and properties
  static final int SCREEN_WIDTH = 600;
  static final int SCREEN_HEIGHT = 600;
  static final int UNIT_SIZE = 25;
  static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
  static final int DELAY = 100;

  // Snake properties
  final int x[] = new int[GAME_UNITS];
  final int y[] = new int[GAME_UNITS];
  int bodyParts = 6;

  // Apple properties
  int appleEaten;
  int appleX;
  int appleY;
  boolean running = false;

  Timer timer;
  Random random;

  // Direction constants
  private static final int UP = 0;
  private static final int DOWN = 1;
  private static final int LEFT = 2;
  private static final int RIGHT = 3;

  // Current direction of the snake
  private int direction = RIGHT;

  // Game state management
  enum GameState {
    PLAYING,
    PAUSED,
    GAME_OVER
  }

  private GameState gameState = GameState.PLAYING;

  // Flag for tongue visibility
  private boolean tongueVisible = false;

  // Class representing a star for Game Over screen
  class Star {
    int x, y, speed, size;
    Color color;

    Star(int x, int y, int speed, int size, Color color) {
      this.x = x;
      this.y = y;
      this.speed = speed;
      this.size = size;
      this.color = color;
    }

    void move() {
      x -= speed;
      if (x < -size) {
        x = SCREEN_WIDTH;
        y = random.nextInt(SCREEN_HEIGHT);
      }
    }
  }

  // List to hold stars for Game Over screen
  List<Star> gameOverStars;

  // Sound manager for handling game sounds
  private SoundManager soundManager;

  /**
   * Initializes the game panel, setting up the game board and input handling.
   */
  GamePanel() {
    random = new Random();
    this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
    this.setBackground(Color.black);
    this.setFocusable(true);
    this.addKeyListener(new MyKeyAdapter());

    soundManager = new SoundManager();
    startGame();
  }

  /**
   * Starts the game by initializing game variables and starting the timer.
   */
  public void startGame() {
    if (timer != null) {
      timer.stop();
    }

    bodyParts = 6;
    appleEaten = 0;
    direction = RIGHT;
    gameState = GameState.PLAYING;
    running = true;

    int startX = SCREEN_WIDTH / 2;
    int startY = SCREEN_HEIGHT / 2;
    for (int i = 0; i < bodyParts; i++) {
      x[i] = startX - UNIT_SIZE * i;
      y[i] = startY;
    }

    newApple();
    timer = new Timer(DELAY, this);
    timer.start();
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    draw(g);
  }

  /**
   * Draws all game elements on the screen.
   */
  public void draw(Graphics g) {
    if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      GradientPaint backgroundGradient = new GradientPaint(
          0, 0, new Color(48, 25, 52),
          SCREEN_WIDTH, SCREEN_HEIGHT, new Color(95, 41, 99));
      g2d.setPaint(backgroundGradient);
      g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

      g2d.setStroke(new BasicStroke(1));
      g2d.setColor(new Color(255, 255, 255, 15));
      for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
        g2d.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
        g2d.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
      }

      for (int i = 0; i < 20; i++) {
        int sparkleX = random.nextInt(SCREEN_WIDTH);
        int sparkleY = random.nextInt(SCREEN_HEIGHT);
        g2d.setColor(new Color(255, 255, 255, random.nextInt(100)));
        g2d.fillOval(sparkleX, sparkleY, 2, 2);
      }

      RadialGradientPaint appleGlow = new RadialGradientPaint(
          appleX + UNIT_SIZE / 2, appleY + UNIT_SIZE / 2, UNIT_SIZE,
          new float[] { 0.0f, 1.0f },
          new Color[] { new Color(255, 50, 50, 100), new Color(255, 50, 50, 0) });
      g2d.setPaint(appleGlow);
      g2d.fillOval(appleX - 5, appleY - 5, UNIT_SIZE + 10, UNIT_SIZE + 10);

      GradientPaint appleGradient = new GradientPaint(
          appleX, appleY, new Color(255, 50, 50),
          appleX + UNIT_SIZE, appleY + UNIT_SIZE, new Color(200, 20, 20));
      g2d.setPaint(appleGradient);
      g2d.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

      g2d.setColor(new Color(255, 255, 255, 100));
      g2d.fillOval(appleX + 5, appleY + 5, 4, 4);

      drawSnake(g2d);

      GradientPaint scorePanelGradient = new GradientPaint(
          SCREEN_WIDTH - 160, 0, new Color(0, 0, 0, 180),
          SCREEN_WIDTH - 30, 60, new Color(20, 20, 20, 180));
      g2d.setPaint(scorePanelGradient);
      int scorePanelWidth = 130;
      int scorePanelHeight = 50;
      int scorePanelX = SCREEN_WIDTH - 140;
      int scorePanelY = 12;
      g2d.fillRoundRect(scorePanelX, scorePanelY, scorePanelWidth, scorePanelHeight, 15, 15);

      g2d.setFont(new Font("Arial", Font.BOLD, 40));
      String scoreText = String.valueOf(appleEaten);
      FontMetrics metrics = getFontMetrics(g2d.getFont());
      int scoreWidth = metrics.stringWidth(scoreText);
      int scoreX = scorePanelX + (scorePanelWidth - scoreWidth) / 2;

      int textAscent = metrics.getAscent();
      int textY = scorePanelY + (scorePanelHeight + textAscent) / 2 - 2;

      g2d.setColor(new Color(255, 255, 255, 50));
      g2d.drawString(scoreText, scoreX, textY);
      g2d.setColor(Color.WHITE);
      g2d.drawString(scoreText, scoreX, textY - 2);

      if (gameState == GameState.PAUSED) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        String pauseText = "Paused";
        FontMetrics pauseMetrics = getFontMetrics(g2d.getFont());
        int pauseX = (SCREEN_WIDTH - pauseMetrics.stringWidth(pauseText)) / 2;
        int pauseY = SCREEN_HEIGHT / 2;
        g2d.setColor(Color.WHITE);
        g2d.drawString(pauseText, pauseX, pauseY);
      }

    } else if (gameState == GameState.GAME_OVER) {
      drawGameOver(g);
    }
  }

  /**
   * Creates a new apple at a random position on the board.
   */
  public void newApple() {
    appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
    appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

    for (int i = 0; i < bodyParts; i++) {
      if (x[i] == appleX && y[i] == appleY) {
        newApple();
        break;
      }
    }
  }

  /**
   * Moves the snake and handles screen wrapping.
   */
  public void move() {
    for (int i = bodyParts; i > 0; i--) {
      x[i] = x[i - 1];
      y[i] = y[i - 1];
    }

    switch (direction) {
      case UP -> y[0] -= UNIT_SIZE;
      case DOWN -> y[0] += UNIT_SIZE;
      case LEFT -> x[0] -= UNIT_SIZE;
      case RIGHT -> x[0] += UNIT_SIZE;
    }

    if (x[0] < 0) {
      x[0] = SCREEN_WIDTH - UNIT_SIZE;
    } else if (x[0] >= SCREEN_WIDTH) {
      x[0] = 0;
    }

    if (y[0] < 0) {
      y[0] = SCREEN_HEIGHT - UNIT_SIZE;
    } else if (y[0] >= SCREEN_HEIGHT) {
      y[0] = 0;
    }
  }

  /**
   * Checks if the snake has eaten an apple and updates the game state.
   */
  public void checkApple() {
    if ((x[0] == appleX) && (y[0] == appleY)) {
      bodyParts++;
      appleEaten++;
      newApple();
      soundManager.playSound("/asset/food.wav");
    }
  }

  /**
   * Checks for collisions with the snake itself.
   */
  public void checkCollisions() {
    for (int i = bodyParts - 1; i > 0; i--) {
      if ((x[0] == x[i]) && (y[0] == y[i])) {
        gameState = GameState.GAME_OVER;
        soundManager.playSound("/asset/gameover.wav");
        break;
      }
    }

    if (gameState == GameState.GAME_OVER) {
      running = false;
      timer.stop();
      initializeGameOverStars();
      repaint();
    }
  }

  /**
   * Initializes stars for the Game Over screen.
   */
  private void initializeGameOverStars() {
    gameOverStars = new ArrayList<>();
    int numberOfStars = 50;

    for (int i = 0; i < numberOfStars; i++) {
      int xPos = random.nextInt(SCREEN_WIDTH);
      int yPos = random.nextInt(SCREEN_HEIGHT);
      int speed = random.nextInt(5) + 3;
      int size = random.nextInt(3) + 2;
      Color color = new Color(255, 255, 255, random.nextInt(150));
      gameOverStars.add(new Star(xPos, yPos, speed, size, color));
    }
  }

  /**
   * Draws the Game Over screen with animations.
   */
  public void drawGameOver(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.setColor(new Color(0, 0, 0, 200));
    g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    if (gameOverStars != null) {
      for (Star star : gameOverStars) {
        g2d.setColor(star.color);
        g2d.fillOval(star.x, star.y, star.size, star.size);
      }
    }

    updateGameOverStars();

    String gameOverText = "Game Over";
    g2d.setFont(new Font("Arial", Font.BOLD, 70));
    FontMetrics metrics = getFontMetrics(g2d.getFont());

    for (int i = 5; i > 0; i--) {
      g2d.setColor(new Color(200, 50, 50, 50 / i));
      g2d.drawString(gameOverText,
          (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2 + i,
          SCREEN_HEIGHT / 2 + i);
    }

    g2d.setColor(new Color(255, 50, 50));
    g2d.drawString(gameOverText,
        (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2,
        SCREEN_HEIGHT / 2);

    String scoreText = "Score: " + appleEaten;
    g2d.setFont(new Font("Arial", Font.BOLD, 40));
    metrics = getFontMetrics(g2d.getFont());

    int scoreY = SCREEN_HEIGHT / 2 + 50;

    for (int i = 3; i > 0; i--) {
      g2d.setColor(new Color(200, 200, 200, 50 / i));
      g2d.drawString(scoreText,
          (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2 + i,
          scoreY + i);
    }

    g2d.setColor(Color.WHITE);
    g2d.drawString(scoreText,
        (SCREEN_WIDTH - metrics.stringWidth(scoreText)) / 2,
        scoreY);

    String restartText = "Press Space to Start";
    g2d.setFont(new Font("Arial", Font.BOLD, 25));
    metrics = getFontMetrics(g2d.getFont());
    int restartY = SCREEN_HEIGHT / 2 + 90;

    g2d.setColor(new Color(255, 255, 255, 150));
    g2d.drawString(restartText,
        (SCREEN_WIDTH - metrics.stringWidth(restartText)) / 2,
        restartY);
  }

  /**
   * Updates positions of Game Over stars.
   */
  private void updateGameOverStars() {
    if (gameOverStars != null) {
      for (Star star : gameOverStars) {
        star.move();
      }
    }
  }

  /**
   * Event listener method called by the Timer.
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (gameState == GameState.PLAYING) {
      tongueVisible = isAboutToEat(x[0], y[0], direction, 2);
      move();
      checkApple();
      checkCollisions();
    }
    repaint();
  }

  /**
   * Draws the snake and its features.
   */
  private void drawSnake(Graphics2D g2d) {
    for (int i = 0; i < bodyParts; i++) {
      if (i == 0) {
        RadialGradientPaint headGlow = new RadialGradientPaint(
            x[i] + UNIT_SIZE / 2, y[i] + UNIT_SIZE / 2, UNIT_SIZE / 2,
            new float[] { 0.0f, 1.0f },
            new Color[] { new Color(100, 255, 100, 150), new Color(100, 255, 100, 0) });
        g2d.setPaint(headGlow);
        g2d.fillOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);

        GradientPaint headGradient = new GradientPaint(
            x[i], y[i], new Color(0, 200, 100),
            x[i] + UNIT_SIZE, y[i] + UNIT_SIZE, new Color(0, 150, 80));
        g2d.setPaint(headGradient);
        RoundRectangle2D.Double head = new RoundRectangle2D.Double(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 20, 20);
        g2d.fill(head);

        g2d.setColor(new Color(0, 0, 0, 180));
        int eyeWidth = UNIT_SIZE / 4;
        int eyeHeight = UNIT_SIZE / 4;
        int eyeOffsetX = UNIT_SIZE / 5;
        int eyeOffsetY = UNIT_SIZE / 4;

        Ellipse2D.Double leftEye = new Ellipse2D.Double(x[i] + eyeOffsetX, y[i] + eyeOffsetY, eyeWidth, eyeHeight);
        Ellipse2D.Double rightEye = new Ellipse2D.Double(x[i] + UNIT_SIZE - eyeOffsetX - eyeWidth, y[i] + eyeOffsetY,
            eyeWidth, eyeHeight);
        g2d.fill(leftEye);
        g2d.fill(rightEye);

        g2d.setColor(Color.WHITE);
        int highlightSize = eyeWidth / 3;
        Ellipse2D.Double leftEyeHighlight = new Ellipse2D.Double(x[i] + eyeOffsetX + highlightSize / 2,
            y[i] + eyeOffsetY + highlightSize / 2, highlightSize, highlightSize);
        Ellipse2D.Double rightEyeHighlight = new Ellipse2D.Double(
            x[i] + UNIT_SIZE - eyeOffsetX - eyeWidth + highlightSize / 2, y[i] + eyeOffsetY + highlightSize / 2,
            highlightSize, highlightSize);
        g2d.fill(leftEyeHighlight);
        g2d.fill(rightEyeHighlight);

        if (tongueVisible) {
          drawTongue(g2d, x[i], y[i], direction);
        }

        g2d.setColor(new Color(0, 150, 80, 150));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(head);

      } else {
        float alpha = 1.0f - ((float) i / bodyParts) * 0.3f;
        GradientPaint bodyGradient = new GradientPaint(
            x[i], y[i], new Color(0, 180, 100, (int) (255 * alpha)),
            x[i] + UNIT_SIZE, y[i] + UNIT_SIZE, new Color(0, 130, 80, (int) (255 * alpha)));
        g2d.setPaint(bodyGradient);

        RoundRectangle2D.Double bodySegment = new RoundRectangle2D.Double(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
        g2d.fill(bodySegment);

        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2d.setColor(Color.BLACK);
        g2d.fill(bodySegment);
        g2d.setComposite(originalComposite);
      }
    }
  }

  /**
   * Draws the tongue based on the current direction of movement.
   */
  private void drawTongue(Graphics2D g2d, int headX, int headY, int dir) {
    g2d.setColor(new Color(255, 0, 0, 180));
    int tongueLength = 15;
    int tongueWidth = 4;

    switch (dir) {
      case UP -> g2d.fillRoundRect(headX + UNIT_SIZE / 2 - tongueWidth / 2, headY - tongueLength,
          tongueWidth, tongueLength, 2, 2);
      case DOWN -> g2d.fillRoundRect(headX + UNIT_SIZE / 2 - tongueWidth / 2, headY + UNIT_SIZE,
          tongueWidth, tongueLength, 2, 2);
      case LEFT -> g2d.fillRoundRect(headX - tongueLength, headY + UNIT_SIZE / 2 - tongueWidth / 2,
          tongueLength, tongueWidth, 2, 2);
      case RIGHT -> g2d.fillRoundRect(headX + UNIT_SIZE, headY + UNIT_SIZE / 2 - tongueWidth / 2,
          tongueLength, tongueWidth, 2, 2);
    }
  }

  /**
   * Determines if the snake is about to eat the apple.
   */
  private boolean isAboutToEat(int headX, int headY, int dir, int proximity) {
    for (int step = 1; step <= proximity; step++) {
      int nextHeadX = headX;
      int nextHeadY = headY;

      switch (dir) {
        case UP -> nextHeadY = headY - UNIT_SIZE * step;
        case DOWN -> nextHeadY = headY + UNIT_SIZE * step;
        case LEFT -> nextHeadX = headX - UNIT_SIZE * step;
        case RIGHT -> nextHeadX = headX + UNIT_SIZE * step;
      }

      if (nextHeadX < 0) {
        nextHeadX = SCREEN_WIDTH - UNIT_SIZE;
      } else if (nextHeadX >= SCREEN_WIDTH) {
        nextHeadX = 0;
      }

      if (nextHeadY < 0) {
        nextHeadY = SCREEN_HEIGHT - UNIT_SIZE;
      } else if (nextHeadY >= SCREEN_HEIGHT) {
        nextHeadY = 0;
      }

      if ((appleX == nextHeadX) && (appleY == nextHeadY)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Handles key events for controlling the snake.
   */
  public class MyKeyAdapter extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_UP -> {
          if (direction != DOWN) {
            direction = UP;
          }
        }
        case KeyEvent.VK_DOWN -> {
          if (direction != UP) {
            direction = DOWN;
          }
        }
        case KeyEvent.VK_LEFT -> {
          if (direction != RIGHT) {
            direction = LEFT;
          }
        }
        case KeyEvent.VK_RIGHT -> {
          if (direction != LEFT) {
            direction = RIGHT;
          }
        }
        case KeyEvent.VK_P -> togglePause();
        case KeyEvent.VK_SPACE -> {
          if (gameState == GameState.GAME_OVER) {
            startGame();
          }
        }
      }
      System.out.println("Direction updated: " + direction);
    }
  }

  /**
   * Toggles the game's pause state.
   */
  private void togglePause() {
    if (gameState == GameState.PLAYING) {
      gameState = GameState.PAUSED;
    } else if (gameState == GameState.PAUSED) {
      gameState = GameState.PLAYING;
    }
  }
}
