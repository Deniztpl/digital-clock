import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;


public class TimerApp {
    private static final int WORK_DURATION = 52; // 52 minutes
    private static final int BREAK_DURATION = 17; // 17 minutes
    private static boolean isWorkPhase = true; // Start with the 52-minute phase
    private static int remainingTime;
    private static javax.swing.Timer timer;
    private static JLabel timerLabel;
    private static JFrame frame;
    private static JButton startStopButton;
    private static boolean playedSound20 = false;
    private static boolean playedSound40 = false;

    public static void main(String[] args) {
        // Set up the main window
        frame = new JFrame("17-52 Timer");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
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
        startStopButton = new JButton("Start");
        JButton nextButton = new JButton("Next");
        buttonPanel.add(startStopButton);
        buttonPanel.add(nextButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        // Button actions
        startStopButton.addActionListener(e -> {
            playClickSound();
            if (timer.isRunning()) {
                timer.stop();
                startStopButton.setText("Start");
            } else {
                timer.start();
                startStopButton.setText("Stop");
            }
        });

        nextButton.addActionListener(e -> {
            playClickSound();
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
                    frame.setVisible(true);
                    startStopButton.setText("Start");
                    startNextPhase();
                } else {
                    updateTimerLabel();
                    if (isWorkPhase) {
                        int minutesElapsed = (WORK_DURATION * 60 - remainingTime) / 60;
                        if (minutesElapsed == 20 && !playedSound20) {
                            playThirdSound();
                            playedSound20 = true;
                        } else if (minutesElapsed == 40 && !playedSound40) {
                            playThirdSound();
                            playedSound40 = true;
                        }
                    }
                }
            }
        });

        frame.setVisible(true);
        setupSystemTray();
    }

    private static void positionWindowLowerRight() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice defaultScreen = ge.getDefaultScreenDevice();
        Rectangle bounds = defaultScreen.getDefaultConfiguration().getBounds();
        int x = (int) (bounds.getMaxX() - frame.getWidth()) - 100;
        int y = (int) (bounds.getMaxY() - frame.getHeight()) - 175;
        frame.setLocation(x, y);
    }

    private static void startNextPhase() {
        isWorkPhase = !isWorkPhase;
        remainingTime = (isWorkPhase ? WORK_DURATION : BREAK_DURATION) * 60;
        playedSound20 = false;  // Reset the flags for the next phase
        playedSound40 = false;
        updateTimerLabel();
    }

    private static void updateTimerLabel() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private static void playBellSound() {
        playSound("resources/bell.wav");
    }

    private static void playClickSound() {
        playSound("resources/click.wav");
    }

    private static void playThirdSound() {
        playSound("resources/third.wav");
    }

    private static void playSound(String soundFile) {
        try {
            InputStream audioSrc = TimerApp.class.getResourceAsStream(soundFile);
            if (audioSrc == null) {
                throw new IOException("Resource not found: " + soundFile);
            }
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private static void setupSystemTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(TimerApp.class.getResource("resources/icon.jpg"));
            TrayIcon trayIcon = new TrayIcon(image, "17-52 Timer");
            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        frame.setVisible(true);
                    }
                }
            });
            PopupMenu popup = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            showItem.addActionListener(e -> frame.setVisible(true));
            popup.add(showItem);
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.addActionListener(e -> System.exit(0));
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);
            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.out.println("TrayIcon could not be added.");
            }
        }
    }
}
