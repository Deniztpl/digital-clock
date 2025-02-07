import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

public class TimerApp {
    private static final int WORK_DURATION = 52; // 52 minutes
    private static final int BREAK_DURATION = 17; // 17 minutes
    private static boolean isWorkPhase = true; // Start with the 52-minute phase
    private static int remainingTime;
    private static javax.swing.Timer timer;
    private static JLabel timerLabel;
    private static JFrame frame;
    private static JButton startStopButton; // Make the button a class variable

    public static void main(String[] args) {
        // Set up the main window
        frame = new JFrame("17-52 Timer");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Hide the window on close
        frame.setSize(300, 200);
        frame.setLayout(new BorderLayout());

        // Position the window in the lower-right corner of the screen
        positionWindowLowerRight();

        // Timer display
        timerLabel = new JLabel("52:00", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 48));
        frame.add(timerLabel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        startStopButton = new JButton("Start"); // Initialize the button
        JButton nextButton = new JButton("Next");
        buttonPanel.add(startStopButton);
        buttonPanel.add(nextButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        startStopButton.addActionListener(e -> {
            playClickSound(); // Play click sound
            if (timer.isRunning()) {
                timer.stop();
                startStopButton.setText("Start");
            } else {
                timer.start();
                startStopButton.setText("Stop");
            }
        });

        nextButton.addActionListener(e -> {
            playClickSound(); // Play click sound
            timer.stop();
            startNextPhase();
            startStopButton.setText("Start");
        });

        // Initialize the timer
        remainingTime = WORK_DURATION * 60;
        timer = new javax.swing.Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                remainingTime--;
                if (remainingTime < 0) {
                    timer.stop();
                    playBellSound();
                    frame.setVisible(true); // Make the window visible when the session ends
                    startStopButton.setText("Start"); // Update the button text to "Start"
                    startNextPhase();
                } else {
                    updateTimerLabel();

                    // Play the third sound at the 20th and 40th minute during the 52-minute session
                    if (isWorkPhase) {
                        int minutesElapsed = (WORK_DURATION * 60 - remainingTime) / 60;
                        if (minutesElapsed == 20 || minutesElapsed == 40) {
                            playThirdSound(); // Play the third sound
                        }
                    }
                }
            }
        });

        // Show the window
        frame.setVisible(true);

        // Set up the system tray icon
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(TimerApp.class.getResource("/resources/icon.jpg"));
            TrayIcon trayIcon = new TrayIcon(image, "17-52 Timer");

            // Add a MouseListener to handle double-click events
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) { // Detect double-click
                        frame.setVisible(true); // Show the main window
                    }
                }
            });

            // Add a popup menu to the tray icon
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(e -> frame.setVisible(true));
            popup.add(showItem);

            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> {
                System.exit(0);
            });
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
        }
    }

    private static void positionWindowLowerRight() {
        // Get the screen dimensions
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle bounds = defaultScreen.getDefaultConfiguration().getBounds();

        // Calculate the position for the lower-right corner
        int x = (int) (bounds.getMaxX() - frame.getWidth()) - 100;
        int y = (int) (bounds.getMaxY() - frame.getHeight()) - 175;

        // Set the window location
        frame.setLocation(x, y);
    }

    private static void startNextPhase() {
        isWorkPhase = !isWorkPhase;
        remainingTime = (isWorkPhase ? WORK_DURATION : BREAK_DURATION) * 60;
        updateTimerLabel();
    }

    private static void updateTimerLabel() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private static void playBellSound() {
        playSound("/resources/bell.wav"); // Play bell sound
    }

    private static void playClickSound() {
        playSound("/resources/click.wav"); // Play click sound
    }

    private static void playThirdSound() {
        playSound("/resources/third.wav"); // Play third sound
    }

    private static void playSound(String soundFile) {
        try {
            InputStream soundStream = TimerApp.class.getResourceAsStream(soundFile);
            if (soundStream == null) {
                throw new IOException("Resource not found: " + soundFile);
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundStream);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}