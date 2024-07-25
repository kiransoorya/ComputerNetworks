import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class TCP_Client_Multiclient {
    private JFrame frame1;
    private JLabel status = new JLabel("Trying to connect to Server");
    private JTextArea Chat_Server;
    private JTextArea Text_Server;
    private JButton Send_Server;
    private JTextField Port_Input;
    private JTextField Host_Input;
    private Socket socket;
    private BufferedWriter sendWrite;
    private BufferedReader receiveRead;
    private String name;

    private String receiveMessage;

    public TCP_Client_Multiclient(String name) {
        this.name = name;
    }

    public void TCP_Client(String host, int port) throws Exception {
        socket = new Socket(host, port);
        status.setText("Connected to " + socket.getInetAddress() + ":" + socket.getPort());
        Send_Server.setEnabled(true);

        OutputStream ostream = socket.getOutputStream();
        sendWrite = new BufferedWriter(new OutputStreamWriter(ostream));
        InputStream istream = socket.getInputStream();
        receiveRead = new BufferedReader(new InputStreamReader(istream));

        Thread receiveThread = new Thread(() -> {
            try {
                while ((receiveMessage = receiveRead.readLine()) != null) {
                    SwingUtilities.invokeLater(() -> Chat_Server.append(receiveMessage + "\n"));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    status.setText("Disconnected");
                    Chat_Server.append("Server disconnected\n");
                    JOptionPane.showMessageDialog(null, "Server Disconnected", "Warning", JOptionPane.WARNING_MESSAGE);
                });
                try {
                    socket.close();
                    Send_Server.setEnabled(false);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        receiveThread.start();
    }

    public void TCP_CLIENT_INIT() {
        frame1 = new JFrame("Client - " + name);
        frame1.setLayout(null);
        frame1.setBounds(0, 0, 800, 500);
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        status.setBounds(100, 25, 300, 25);
        status.setVisible(true);
        frame1.add(status);

        Chat_Server = new JTextArea();
        Chat_Server.setBounds(100, 50, 600, 300);
        Chat_Server.setBackground(Color.LIGHT_GRAY);
        Chat_Server.setEditable(false);
        Chat_Server.setVisible(true);
        frame1.add(Chat_Server);

        Text_Server = new JTextArea();
        Text_Server.setBounds(100, 400, 450, 50);
        Text_Server.setBackground(Color.LIGHT_GRAY);
        Text_Server.setVisible(true);
        frame1.add(Text_Server);

        Send_Server = new JButton("Send");
        Send_Server.setBounds(600, 400, 100, 50);
        Send_Server.setVisible(true);
        Send_Server.setEnabled(false);

        Send_Server.addActionListener(e -> {
            synchronized (Text_Server) {
                String message = Text_Server.getText();
                if (!message.isEmpty()) {
                    try {
                        sendWrite.write("[" + name + "] : " + message + "\n");
                        sendWrite.flush();
                        Text_Server.setText("");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });
        frame1.add(Send_Server);

        Port_Input = new JTextField();
        Port_Input.setBounds(500, 25, 100, 25);
        Port_Input.setVisible(true);
        frame1.add(Port_Input);

        Host_Input = new JTextField("127.0.0.1");
        Host_Input.setBounds(400, 25, 100, 25);
        Host_Input.setVisible(true);
        frame1.add(Host_Input);

        JButton Connect_Button = new JButton("Connect");
        Connect_Button.setBounds(600, 25, 100, 25);
        Connect_Button.setVisible(true);
        Connect_Button.addActionListener(e -> {
            String portText = Port_Input.getText();
            String hostText = Host_Input.getText();
            try {
                int port = Integer.parseInt(portText);
                TCP_Client(hostText, port); // Connect to the server on the specified host and port
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to connect", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame1.add(Connect_Button);

        frame1.setVisible(true);
    }

    public void Run() {
        TCP_CLIENT_INIT();
    }

    public static void main(String[] args) {
        // Define clients with different names
        String[] names = {"Client1", "Client2", "Client3"}; // Names for clients

        // Create and run each client
        for (String name : names) {
            // Create a new client instance
            TCP_Client_Multiclient client = new TCP_Client_Multiclient(name);

            // Run the client in a separate thread
            new Thread(() -> client.Run()).start();
        }
    }
}
