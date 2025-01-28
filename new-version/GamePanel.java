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
 * This class extends JPanel and implements ActionListener,
 * and is responsible for displaying the game board and handling game logic.
 */
public class GamePanel extends JPanel implements ActionListener {

  // Constants for setting the dimensions and properties of the game
  static final int SCREEN_WIDTH = 600; // Width of the game screen
  static final int SCREEN_HEIGHT = 600; // Height of the game screen
  static final int UNIT_SIZE = 25; // Size of objects in the game (snake and apple)
  static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / (UNIT_SIZE * UNIT_SIZE); // Number of grid cells
  static final int DELAY = 100; // Speed of the game (milliseconds per move)

  // Snake properties
  final int x[] = new int[GAME_UNITS]; // x-coordinates for the snake's body parts
  final int y[] = new int[GAME_UNITS]; // y-coordinates for the snake's body parts
  int bodyParts = 6; // Initial number of body parts for the snake

  // Apple properties
  int appleEaten; // Number of apples eaten (default value: 0)
  int appleX; // x-coordinate for the apple
  int appleY; // y-coordinate for the apple
  boolean running = false; // Flag to indicate if the game is running

  Timer timer;
  Random random;

  // Direction constants
  private static final int UP = 0;
  private static final int DOWN = 1;
  private static final int LEFT = 2;
  private static final int RIGHT = 3;

  // Current direction of the snake
  private int direction = RIGHT; // Initialize with the default starting direction

  // Game State Enumeration
  enum GameState {
    PLAYING,
    PAUSED,
    GAME_OVER
  }

  private GameState gameState = GameState.PLAYING; // Initial game state

  // Flag to determine if the tongue should be visible
  private boolean tongueVisible = false;

  // Class representing a star for Game Over screen
  class Star {
    int x;
    int y;
    int speed;
    int size;
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

  // Declare the SoundManager instance
  private SoundManager soundManager;

  /**
   * Constructor for the GamePanel class.
   * Initializes the game board and adds a KeyListener for handling user input.
   */
  GamePanel() {
    random = new Random();
    this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
    this.setBackground(Color.black);
    this.setFocusable(true);
    this.addKeyListener(new MyKeyAdapter());

    // Initialize the SoundManager
    soundManager = new SoundManager();

    startGame();
  }

  /**
   * Method to start the game.
   * Creates a new apple and sets the running flag to true.
   */
  public void startGame() {
    // Stop the existing timer if it's running
    if (timer != null) {
      timer.stop();
    }

    // Reset game variables
    bodyParts = 6;
    appleEaten = 0;
    direction = RIGHT;
    gameState = GameState.PLAYING;
    running = true;

    // Initialize snake position (starting at the center and extending to the left)
    int startX = SCREEN_WIDTH / 2;
    int startY = SCREEN_HEIGHT / 2;
    for (int i = 0; i < bodyParts; i++) {
      x[i] = startX - UNIT_SIZE * i; // Extend to the left
      y[i] = startY;
    }

    newApple(); // Create a new Apple when game starts.

    // Create a new timer with the correct delay
    timer = new Timer(DELAY, this);
    timer.start(); // Starts the game.
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    draw(g);
  }

  /**
   * Method to draw all game elements.
   */
  public void draw(Graphics g) {
    if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
      // Create fantasy background with gradient
      Graphics2D g2d = (Graphics2D) g;
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      // Create magical background gradient
      GradientPaint backgroundGradient = new GradientPaint(
          0, 0, new Color(48, 25, 52),
          SCREEN_WIDTH, SCREEN_HEIGHT, new Color(95, 41, 99));
      g2d.setPaint(backgroundGradient);
      g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

      // Draw mystical grid lines
      g2d.setStroke(new BasicStroke(1));
      g2d.setColor(new Color(255, 255, 255, 15));
      for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
        g2d.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT);
        g2d.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE);
      }

      // Draw magical particles (random sparkles)
      for (int i = 0; i < 20; i++) {
        int sparkleX = random.nextInt(SCREEN_WIDTH);
        int sparkleY = random.nextInt(SCREEN_HEIGHT);
        g2d.setColor(new Color(255, 255, 255, random.nextInt(100)));
        g2d.fillOval(sparkleX, sparkleY, 2, 2);
      }

      // Draw magical apple with glow effect
      RadialGradientPaint appleGlow = new RadialGradientPaint(
          appleX + UNIT_SIZE / 2, appleY + UNIT_SIZE / 2, UNIT_SIZE,
          new float[] { 0.0f, 1.0f },
          new Color[] { new Color(255, 50, 50, 100), new Color(255, 50, 50, 0) });
      g2d.setPaint(appleGlow);
      g2d.fillOval(appleX - 5, appleY - 5, UNIT_SIZE + 10, UNIT_SIZE + 10);

      // Draw crystal-like apple
      GradientPaint appleGradient = new GradientPaint(
          appleX, appleY, new Color(255, 50, 50),
          appleX + UNIT_SIZE, appleY + UNIT_SIZE, new Color(200, 20, 20));
      g2d.setPaint(appleGradient);
      g2d.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

      // Add shine to apple
      g2d.setColor(new Color(255, 255, 255, 100));
      g2d.fillOval(appleX + 5, appleY + 5, 4, 4);

      // Draw snake with magical effect
      drawSnake(g2d);

      // Draw score panel with crystal effect
      GradientPaint scorePanelGradient = new GradientPaint(
          SCREEN_WIDTH - 160, 0, new Color(0, 0, 0, 180),
          SCREEN_WIDTH - 30, 60, new Color(20, 20, 20, 180));
      g2d.setPaint(scorePanelGradient);
      int scorePanelWidth = 130;
      int scorePanelHeight = 50;
      int scorePanelX = SCREEN_WIDTH - 140;
      int scorePanelY = 12;
      g2d.fillRoundRect(scorePanelX, scorePanelY, scorePanelWidth, scorePanelHeight, 15, 15);

      // Center score text
      g2d.setFont(new Font("Arial", Font.BOLD, 40));
      String scoreText = String.valueOf(appleEaten);
      FontMetrics metrics = getFontMetrics(g2d.getFont());
      int scoreWidth = metrics.stringWidth(scoreText);
      int scoreX = scorePanelX + (scorePanelWidth - scoreWidth) / 2;

      // Calculate perfect vertical center
      int textAscent = metrics.getAscent();
      int textY = scorePanelY + (scorePanelHeight + textAscent) / 2 - 2; // Adjusted for visual center

      // Score text with magical glow
      g2d.setColor(new Color(255, 255, 255, 50));
      g2d.drawString(scoreText, scoreX, textY);
      g2d.setColor(Color.WHITE);
      g2d.drawString(scoreText, scoreX, textY - 2);

      // Optionally, indicate paused state
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
   * Method to create a new apple at a random position.
   */
  public void newApple() {
    appleX = random.nextInt((int) (SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE;
    appleY = random.nextInt((int) (SCREEN_HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

    // Ensure apple does not spawn on the snake's body
    for (int i = 0; i < bodyParts; i++) {
      if (x[i] == appleX && y[i] == appleY) {
        newApple(); // Recursive call until a free spot is found
        break;
      }
    }
  }

  /**
   * Method responsible for moving the snake.
   * Implements screen wrapping instead of collision with borders.
   */
  public void move() {
    for (int i = bodyParts; i > 0; i--) {
      x[i] = x[i - 1];
      y[i] = y[i - 1];
    }

    switch (direction) {
      case UP:
        y[0] = y[0] - UNIT_SIZE;
        break;
      case DOWN:
        y[0] = y[0] + UNIT_SIZE;
        break;
      case LEFT:
        x[0] = x[0] - UNIT_SIZE;
        break;
      case RIGHT:
        x[0] = x[0] + UNIT_SIZE;
        break;
    }

    // Implement screen wrapping
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
   * Method to check if the snake has eaten an apple.
   */
  public void checkApple() {
    if ((x[0] == appleX) && (y[0] == appleY)) {
      bodyParts++;
      appleEaten++;
      newApple();

      // Play the eating sound
      soundManager.playSound("/asset/food.wav"); // Corrected path
    }
  }

  /**
   * Method to check for collisions with the snake itself.
   * Game over occurs only if the snake collides with its own body.
   */
  public void checkCollisions() {
    // Check if the snake's head collides with its body
    for (int i = bodyParts - 1; i > 0; i--) { // Corrected loop bounds
      if ((x[0] == x[i]) && (y[0] == y[i])) {
        gameState = GameState.GAME_OVER;
        soundManager.playSound("/asset/gameover.wav"); // Corrected path
        break;
      }
    }

    if (gameState == GameState.GAME_OVER) {
      running = false;
      timer.stop();
      initializeGameOverStars(); // Initialize stars when game is over
      repaint(); // Ensure the screen is repainted to show the Game Over screen
    }
  }

  /**
   * Method to initialize stars for Game Over screen.
   */
  private void initializeGameOverStars() {
    gameOverStars = new ArrayList<>();
    int numberOfStars = 50; // Number of stars to display

    for (int i = 0; i < numberOfStars; i++) {
      int xPos = random.nextInt(SCREEN_WIDTH);
      int yPos = random.nextInt(SCREEN_HEIGHT);
      int speed = random.nextInt(5) + 3; // Increased speed range for faster animation
      int size = random.nextInt(3) + 2; // Size between 2 and 4
      Color color = new Color(255, 255, 255, random.nextInt(150));
      gameOverStars.add(new Star(xPos, yPos, speed, size, color));
    }
  }

  /**
   * Method to draw the game over screen.
   */
  public void drawGameOver(Graphics g) {
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // Fade background
    g2d.setColor(new Color(0, 0, 0, 200));
    g2d.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

    // Draw moving stars
    if (gameOverStars != null) {
      for (Star star : gameOverStars) {
        g2d.setColor(star.color);
        g2d.fillOval(star.x, star.y, star.size, star.size);
      }
    }

    // Update stars' positions
    updateGameOverStars();

    // Game Over text with magical effect
    String gameOverText = "Game Over";
    g2d.setFont(new Font("Arial", Font.BOLD, 70));
    FontMetrics metrics = getFontMetrics(g2d.getFont());

    // Text glow effect
    for (int i = 5; i > 0; i--) {
      g2d.setColor(new Color(200, 50, 50, 50 / i));
      g2d.drawString(gameOverText,
          (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2 + i,
          SCREEN_HEIGHT / 2 + i);
    }

    // Main text
    g2d.setColor(new Color(255, 50, 50));
    g2d.drawString(gameOverText,
        (SCREEN_WIDTH - metrics.stringWidth(gameOverText)) / 2,
        SCREEN_HEIGHT / 2);

    // Score display with crystal effect
    String scoreText = "Score: " + appleEaten;
    g2d.setFont(new Font("Arial", Font.BOLD, 40));
    metrics = getFontMetrics(g2d.getFont());

    // Score text glow
    int scoreY = SCREEN_HEIGHT / 2 + 50; // Position below game over text

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

    // Add "Press Space to Start" text
    String restartText = "Press Space to Start";
    g2d.setFont(new Font("Arial", Font.BOLD, 25));
    metrics = getFontMetrics(g2d.getFont());
    int restartY = SCREEN_HEIGHT / 2 + 90; // Position below score text

    g2d.setColor(new Color(255, 255, 255, 150));
    g2d.drawString(restartText,
        (SCREEN_WIDTH - metrics.stringWidth(restartText)) / 2,
        restartY);
  }

  /**
   * Method to update positions of Game Over stars.
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
      // Check if the snake is about to eat an apple within the next two units
      tongueVisible = isAboutToEat(x[0], y[0], direction, 2); // Change the proximity here

      move(); // Move the snake
      checkApple(); // Check if the snake has consumed an apple
      checkCollisions(); // Check if the snake has collided with itself
    }
    // Game Over animations are handled in drawGameOver via updateGameOverStars()
    repaint(); // Repaint the screen
  }

  /**
   * Method to draw the snake and its features.
   */
  private void drawSnake(Graphics2D g2d) {
    for (int i = 0; i < bodyParts; i++) {
      if (i == 0) { // Snake head
        // Enhanced Glowing head effect with 3D shading
        RadialGradientPaint headGlow = new RadialGradientPaint(
            x[i] + UNIT_SIZE / 2, y[i] + UNIT_SIZE / 2, UNIT_SIZE / 2,
            new float[] { 0.0f, 1.0f },
            new Color[] { new Color(100, 255, 100, 150), new Color(100, 255, 100, 0) });
        g2d.setPaint(headGlow);
        g2d.fillOval(x[i], y[i], UNIT_SIZE, UNIT_SIZE);

        // Enhanced Crystal-like head with smoother gradients
        GradientPaint headGradient = new GradientPaint(
            x[i], y[i], new Color(0, 200, 100),
            x[i] + UNIT_SIZE, y[i] + UNIT_SIZE, new Color(0, 150, 80));
        g2d.setPaint(headGradient);
        RoundRectangle2D.Double head = new RoundRectangle2D.Double(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 20, 20);
        g2d.fill(head);

        // Add detailed eyes to the snake head
        g2d.setColor(new Color(0, 0, 0, 180)); // Darker black for eyes
        int eyeWidth = UNIT_SIZE / 4;
        int eyeHeight = UNIT_SIZE / 4;
        int eyeOffsetX = UNIT_SIZE / 5;
        int eyeOffsetY = UNIT_SIZE / 4;

        // Draw black part of the eyes
        Ellipse2D.Double leftEye = new Ellipse2D.Double(x[i] + eyeOffsetX, y[i] + eyeOffsetY, eyeWidth, eyeHeight);
        Ellipse2D.Double rightEye = new Ellipse2D.Double(x[i] + UNIT_SIZE - eyeOffsetX - eyeWidth, y[i] + eyeOffsetY,
            eyeWidth, eyeHeight);
        g2d.fill(leftEye);
        g2d.fill(rightEye);

        // Add white highlight to the eyes for a lifelike appearance
        g2d.setColor(Color.WHITE);
        int highlightSize = eyeWidth / 3;
        Ellipse2D.Double leftEyeHighlight = new Ellipse2D.Double(x[i] + eyeOffsetX + highlightSize / 2,
            y[i] + eyeOffsetY + highlightSize / 2, highlightSize, highlightSize);
        Ellipse2D.Double rightEyeHighlight = new Ellipse2D.Double(
            x[i] + UNIT_SIZE - eyeOffsetX - eyeWidth + highlightSize / 2, y[i] + eyeOffsetY + highlightSize / 2,
            highlightSize, highlightSize);
        g2d.fill(leftEyeHighlight);
        g2d.fill(rightEyeHighlight);

        // Draw tongue based on the direction of movement and proximity to apple
        if (tongueVisible) {
          drawTongue(g2d, x[i], y[i], direction);
        }

        // Optional: Add a subtle outline around the head for better separation
        g2d.setColor(new Color(0, 150, 80, 150)); // Semi-transparent outline
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(head);

      } else {
        // Enhanced Magical body segments with smooth gradients and subtle textures
        float alpha = 1.0f - ((float) i / bodyParts) * 0.3f;
        GradientPaint bodyGradient = new GradientPaint(
            x[i], y[i], new Color(0, 180, 100, (int) (255 * alpha)),
            x[i] + UNIT_SIZE, y[i] + UNIT_SIZE, new Color(0, 130, 80, (int) (255 * alpha)));
        g2d.setPaint(bodyGradient);

        // Draw body segment with smoother edges
        RoundRectangle2D.Double bodySegment = new RoundRectangle2D.Double(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
        g2d.fill(bodySegment);

        // Optional: Add a subtle inner shadow for depth
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2d.setColor(Color.BLACK);
        g2d.fill(bodySegment);
        g2d.setComposite(originalComposite);
      }
    }
  }

  /**
   * Method to draw the tongue based on the current direction of movement.
   *
   * @param g2d   Graphics2D object for drawing
   * @param headX X-coordinate of the snake's head
   * @param headY Y-coordinate of the snake's head
   * @param dir   Current direction of the snake
   */
  private void drawTongue(Graphics2D g2d, int headX, int headY, int dir) {
    g2d.setColor(new Color(255, 0, 0, 180)); // Slightly transparent red for realism
    int tongueLength = 15; // Increased length for better visibility
    int tongueWidth = 4; // Increased width for a more substantial look

    switch (dir) {
      case UP:
        g2d.fillRoundRect(headX + UNIT_SIZE / 2 - tongueWidth / 2, headY - tongueLength,
            tongueWidth, tongueLength, 2, 2);
        break;
      case DOWN:
        g2d.fillRoundRect(headX + UNIT_SIZE / 2 - tongueWidth / 2, headY + UNIT_SIZE,
            tongueWidth, tongueLength, 2, 2);
        break;
      case LEFT:
        g2d.fillRoundRect(headX - tongueLength, headY + UNIT_SIZE / 2 - tongueWidth / 2,
            tongueLength, tongueWidth, 2, 2);
        break;
      case RIGHT:
        g2d.fillRoundRect(headX + UNIT_SIZE, headY + UNIT_SIZE / 2 - tongueWidth / 2,
            tongueLength, tongueWidth, 2, 2);
        break;
      default:
        break;
    }
  }

  /**
   * Method to determine if the snake is about to eat the apple.
   * The tongue will extend only when the apple is directly in front of the
   * snake's head within the specified proximity.
   *
   * @param headX     X-coordinate of the snake's head
   * @param headY     Y-coordinate of the snake's head
   * @param dir       Current direction of the snake
   * @param proximity Number of units ahead to check (e.g., 1 for one unit ahead)
   * @return True if the apple is within the specified proximity in front;
   *         otherwise, false
   */
  private boolean isAboutToEat(int headX, int headY, int dir, int proximity) {
    for (int step = 1; step <= proximity; step++) {
      int nextHeadX = headX;
      int nextHeadY = headY;

      switch (dir) {
        case UP:
          nextHeadY = headY - UNIT_SIZE * step;
          break;
        case DOWN:
          nextHeadY = headY + UNIT_SIZE * step;
          break;
        case LEFT:
          nextHeadX = headX - UNIT_SIZE * step;
          break;
        case RIGHT:
          nextHeadX = headX + UNIT_SIZE * step;
          break;
      }

      // Handle screen wrapping for next head position
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
   * Inner class to handle key events for controlling the snake.
   */
  public class MyKeyAdapter extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
          if (direction != DOWN) {
            direction = UP;
          }
          break;
        case KeyEvent.VK_DOWN:
          if (direction != UP) {
            direction = DOWN;
          }
          break;
        case KeyEvent.VK_LEFT:
          if (direction != RIGHT) {
            direction = LEFT;
          }
          break;
        case KeyEvent.VK_RIGHT:
          if (direction != LEFT) {
            direction = RIGHT;
          }
          break;
        // Add pause functionality
        case KeyEvent.VK_P:
          togglePause();
          break;
        // Restart the game when space is pressed during Game Over
        case KeyEvent.VK_SPACE:
          if (gameState == GameState.GAME_OVER) {
            startGame();
          }
          break;
      }
      System.out.println("Direction updated: " + direction);
    }
  }

  /**
   * Method to toggle the game's pause state.
   */
  private void togglePause() {
    if (gameState == GameState.PLAYING) {
      gameState = GameState.PAUSED;
    } else if (gameState == GameState.PAUSED) {
      gameState = GameState.PLAYING;
    }
    // Do not allow pausing during Game Over
  }

}
