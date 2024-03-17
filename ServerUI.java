import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerUI extends JFrame {
    private JTextArea logTextArea;
    private JLabel imageLabel; // Added JLabel to display the image
    private Logger logger;

    public ServerUI() {
        setTitle("Image Server");
        setSize(600, 400); // Increased frame size for better image display
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logTextArea = new JTextArea(10, 30);
        logTextArea.setEditable(false);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(logTextArea), BorderLayout.NORTH);
        panel.add(imageLabel, BorderLayout.CENTER); // Added imageLabel to the panel

        add(panel);

        // Initialize logger
        initializeLogger();

        startServer();
    }

    private void initializeLogger() {
        try {
            FileHandler fh = new FileHandler("log.txt");
            logger = Logger.getLogger("ServerLog");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(12345);
                    logTextArea.append("Server started. Waiting for client...\n");

                    while (true) {
                        Socket socket = serverSocket.accept();
                        logTextArea.append("Client connected.\n");

                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                        String imageName;
                        while (!(imageName = (String) inputStream.readObject()).equals("DONE")) {
                            logTextArea.append("Received: " + imageName + "\n");

                            // Receive image data
                            int imageDataLength = inputStream.readInt();
                            byte[] imageData = new byte[imageDataLength];
                            inputStream.readFully(imageData);

                            displayImage(imageData); // Display the image
                            writeToLogFile(imageName);
                        }

                        inputStream.close();
                        socket.close();
                        logTextArea.append("Client disconnected.\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void displayImage(byte[] imageData) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
            ImageIcon icon = new ImageIcon(image);
            imageLabel.setIcon(icon); // Set the image icon to the label
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeToLogFile(String imageName) {
        if (logger != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String currentTime = sdf.format(new Date());
                logger.info(imageName + " - " + currentTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerUI().setVisible(true);
            }
        });
    }
}
