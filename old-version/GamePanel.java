import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Random;
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
  static final int GAME_UNITS = (SCREEN_WIDTH * SCREEN_HEIGHT) / UNIT_SIZE; // Number of objects that can fit on the screen
  static final int DELAY = 100; // Speed of the game

  // Snake properties
  final int x[] = new int[GAME_UNITS]; // x-coordinates for the snake's body parts
  final int y[] = new int[GAME_UNITS]; // y-coordinates for the snake's body parts
  int bodyParts = 6; // Initial number of body parts for the snake

  // Apple properties
  int appleEaten; // Number of apples eaten (default value: 0)
  int appleX; // x-coordinate for the apple
  int appleY; // y-coordinate for the apple
  char direction = 'R'; // Initial movement direction for the snake (RIGHT)
  boolean running = false; // Flag to disable auto movement of the snake when the game starts

  Timer timer;
  Random random;

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
    startGame();
  }

  /**
   * Method to start the game.
   * Creates a new apple and sets the running flag to true.
   */
  public void startGame() {
    newApple(); // Call to create new Apple when game starts.
    running = true; // To Start the game, we set running = true;
    timer = new Timer(DELAY, this); // 'this' because we are using ActionListener interface.
    timer.start(); // Starts the game.
  }

  public void paintComponent(Graphics g) {
    super.paintComponent(g); // Calls the super class (JPanel) paintComponent method passing Graphics object.
    draw(g); // Calls draw method passing the Graphics object.
  }

  public void draw(Graphics g) {
    // Drawing matrix on screen
    if (running) {
      // The code below is responsible for drawing grid lines on the screen.
      // Comment this section out if grid lines are not required.
      for (int i = 0; i < SCREEN_HEIGHT / UNIT_SIZE; i++) {
        g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, SCREEN_HEIGHT); // Grid lines for y-axis;   
        g.drawLine(0, i * UNIT_SIZE, SCREEN_WIDTH, i * UNIT_SIZE); // Grid lines for x-axis;   
      }
      //---------x---------x-----------x-------------x----------

      g.setColor(Color.red);
      g.fillOval(appleX, appleY, UNIT_SIZE, UNIT_SIZE);

      // The code below is responsible for drawing the snake on the screen
      for (int i = 0; i < bodyParts; i++) {
        if (i == 0) {
          g.setColor(Color.green);
          g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
        } else {
          g.setColor(new Color(45, 180, 0));
          // Uncomment the line below if a multicolored snake body is required. 
          //g.setColor(new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255),random.nextInt(255)));
          g.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);

        }
      }
      // The code below is responsible for displaying the player's score on the screen
      g.setColor(Color.red);
      g.setFont(new Font("Ink Free", Font.BOLD, 40));
      FontMetrics metrics = getFontMetrics(g.getFont());
      g.drawString("Score: " + appleEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: " + appleEaten)) / 2, g.getFont().getSize());
    } else {
      gameOver(g);
    }
  }

  // The code below is responsible for populating the game with apples.
  public void newApple() {
    appleX = random.nextInt((int)(SCREEN_WIDTH / UNIT_SIZE)) * UNIT_SIZE; // Setting random position for Apple along x-axis; (range 0-600);
    appleY = random.nextInt((int)(SCREEN_HEIGHT) / UNIT_SIZE) * UNIT_SIZE; // Setting random position for Apple along y-axis; (range 0-600);
  }

  // The code below is responsible for moving the snake
  public void move() {
    // Moves the body of the snake in the direction specified by the "direction" variable
    for (int i = bodyParts; i > 0; i--) {
      x[i] = x[i - 1];
      y[i] = y[i - 1];
    }
    switch (direction) {
    case 'U':
      y[0] = y[0] - UNIT_SIZE;
      break;
    case 'D':
      y[0] = y[0] + UNIT_SIZE;
      break;
    case 'L':
      x[0] = x[0] - UNIT_SIZE;
      break;
    case 'R':
      x[0] = x[0] + UNIT_SIZE;
      break;
    }
  }

  public void checkApple() {
    // Check if the snake's head is on the apple's position
    if ((x[0] == appleX) && (y[0] == appleY)) {
      bodyParts++;
      appleEaten++;
      newApple();
    }
  }

  public void checkCollisions() {
    // checks if the snake's head collides with its body
    for (int i = bodyParts; i > 0; i--) {
      if ((x[0] == x[i]) && (y[0] == y[i])) {
        running = false;
      }
    }

    // checks if the snake's head touches the Left border
    if (x[0] < 0) {
      running = false;
    }

    // checks if the snake's head touches the Right border
    if (x[0] > SCREEN_HEIGHT) {
      running = false;
    }
    // checks if the snake's head touches the Top border
    if (y[0] < 0) {
      running = false;
    }
    // checks if the snake's head touches the Bottom border
    if (y[0] > SCREEN_HEIGHT) {
      running = false;
    }

    if (!running) {
      timer.stop();
    }
  }

  public void gameOver(Graphics g) {
    // Draws the Score on the screen
    g.setColor(Color.red);
    g.setFont(new Font("Ink Free", Font.BOLD, 40));
    FontMetrics metrics1 = getFontMetrics(g.getFont());
    g.drawString("Score: " + appleEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + appleEaten)) / 2, g.getFont().getSize());

    // Draws the "Game Over" text on the screen
    g.setColor(Color.red);
    g.setFont(new Font("Ink Free", Font.BOLD, 75));
    FontMetrics metrics2 = getFontMetrics(g.getFont());
    g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);
  }

  //****** Event Listener Functionality ******
  @Override
  public void actionPerformed(ActionEvent e) {

    if (running) {
      move(); // move the snake
      checkApple(); // check if the snake has consumed an apple
      checkCollisions(); // check if the snake has collided with the walls or itself
    }
    repaint(); // repaint the screen
  }

  public class MyKeyAdapter extends KeyAdapter {
    @Override
    public void keyPressed(KeyEvent e) {
      switch (e.getKeyCode()) {
      case KeyEvent.VK_LEFT: // if the left arrow key is pressed
        if (direction != 'R') { // and the current direction is not right
          direction = 'L'; // change the direction to left
        }
        break;
      case KeyEvent.VK_RIGHT: // if the right arrow key is pressed
        if (direction != 'L') { // and the current direction is not left
          direction = 'R'; // change the direction to right
        }
        break;
      case KeyEvent.VK_UP: // if the up arrow key is pressed
        if (direction != 'D') { // and the current direction is not down
          direction = 'U'; // change the direction to up
        }
        break;
      case KeyEvent.VK_DOWN: // if the down arrow key is pressed
        if (direction != 'U') { // and the current direction is not up
          direction = 'D'; // change the direction to down
        }
        break;
      }
    }
  }
}