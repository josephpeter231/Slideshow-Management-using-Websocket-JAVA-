import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientUI extends JFrame {
    private static final String FOLDER_PATH = "slide_0";
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;
    private static final String UPDATE_URL = "https://ibics.co.in/cgi-bin/mescoep_groupa_keytext.cgi";

    private JTextArea logTextArea;
    private JTextArea notesTextArea;
    private File[] imageFiles;
    private int currentIndex = 0;
    private Socket socket;
    private ObjectOutputStream outputStream;

    public ClientUI() {
        setTitle("Image Client");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        logTextArea = new JTextArea(10, 30);
        logTextArea.setEditable(false);

        notesTextArea = new JTextArea(5, 30);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex > 0) {
                    currentIndex--;
                    sendImage(imageFiles[currentIndex]);
                }
            }
        });

        JButton nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentIndex < imageFiles.length - 1) {
                    currentIndex++;
                    sendImage(imageFiles[currentIndex]);
                }
            }
        });

        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateServer();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(backButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(updateButton);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(logTextArea), BorderLayout.NORTH);
        panel.add(new JScrollPane(notesTextArea), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        loadImages();
        connectToServer();
    }

    private void loadImages() {
        File folder = new File(FOLDER_PATH);
        imageFiles = folder.listFiles();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendImage(File imageFile) {
        try {
            String imageName = imageFile.getName();
            BufferedImage image = ImageIO.read(imageFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);

            byte[] imageData = baos.toByteArray();

            // Send image name and image data
            outputStream.writeObject(imageName);
            outputStream.writeInt(imageData.length);
            outputStream.write(imageData);
            outputStream.flush();

            logTextArea.append("Sent image: " + imageName + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateServer() {
        try {
            String textData = notesTextArea.getText();
            String imageName = imageFiles[currentIndex].getName();

            URL url = new URL(UPDATE_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            String postData = "image=" + imageName + "&text=" + textData;
            conn.getOutputStream().write(postData.getBytes("UTF-8"));

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                logTextArea.append("Update sent to server.\n");
            } else {
                logTextArea.append("Failed to update server. Response code: " + responseCode + "\n");
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientUI().setVisible(true);
            }
        });
    }
}
