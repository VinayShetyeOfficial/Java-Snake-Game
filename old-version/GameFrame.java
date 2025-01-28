import javax.swing.JFrame;

public class GameFrame extends JFrame {

  GameFrame() {

    // Create a new instance of GamePanel
    GamePanel panel = new GamePanel();

    // Add the panel to the frame
    this.add(panel);

    // Set the default close operation for the frame
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Disable resizing of the frame
    this.setResizable(false);

    // Make the frame visible
    this.setVisible(true);

    // Pack the frame to the size of its components
    this.pack();

    // Center the frame on the screen
    this.setLocationRelativeTo(null);
  }
}