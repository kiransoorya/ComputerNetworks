import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
public class TCP_Server_MultiClient {
    private JFrame frame1;
    private JLabel status = new JLabel("Starting Server");
    private JTextArea Chat_Client;
    private JTextArea Text_Client;
    private JButton Send_Client;
    private JTextField Port_Input;
    private JButton Start_Server_Button;
    private ServerSocket serverSocket;
    private Map<Integer, ClientHandler> clientHandlers = new HashMap<>();
    private String receiveMessage;
    public void TCP_Server(int port) throws Exception {
        serverSocket = new ServerSocket(port);
        status.setText("Server running on port " + port);
        Start_Server_Button.setEnabled(false);
        Send_Client.setEnabled(true);  // Enable the send button once the server starts

        new Thread(this::acceptClients).start();
    }
    private void acceptClients() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.put(socket.getPort(), clientHandler);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private class ClientHandler implements Runnable {
        private Socket socket;
        private BufferedWriter sendWrite;
        private BufferedReader receiveRead;
        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            OutputStream ostream = socket.getOutputStream();
            sendWrite = new BufferedWriter(new OutputStreamWriter(ostream));
            InputStream istream = socket.getInputStream();
            receiveRead = new BufferedReader(new InputStreamReader(istream));
        }
        @Override
        public void run() {
            try {
                while ((receiveMessage = receiveRead.readLine()) != null) {
                    SwingUtilities.invokeLater(() -> Chat_Client.append(receiveMessage + "\n"));
                    // Broadcast received message to all clients
                    for (ClientHandler handler : clientHandlers.values()) {
                        handler.sendMessage(receiveMessage);
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> {
                    status.setText("Client disconnected");
                    Chat_Client.append("Client disconnected\n");
                });
                clientHandlers.remove(socket.getPort());
            }
        }
        public void sendMessage(String message) {
            if (sendWrite != null) {
                try {
                    sendWrite.write(message + "\n");
                    sendWrite.flush();
                    System.out.println("Sent message: " + message); // Debugging statement
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void TCP_SERVER_INIT() {
        frame1 = new JFrame("Server");
        frame1.setLayout(null);
        frame1.setBounds(0, 0, 800, 500);
        frame1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        status.setBounds(100, 25, 300, 25);
        status.setVisible(true);
        frame1.add(status);
        Chat_Client = new JTextArea();
        Chat_Client.setBounds(100, 50, 600, 300);
        Chat_Client.setBackground(Color.WHITE);
        Chat_Client.setEditable(false);
        Chat_Client.setVisible(true);
        frame1.add(Chat_Client);
        Text_Client = new JTextArea();
        Text_Client.setBounds(100, 400, 450, 50);
        Text_Client.setBackground(Color.WHITE);
        Text_Client.setVisible(true);
        frame1.add(Text_Client);
        Send_Client = new JButton("Send");
        Send_Client.setBounds(600, 400, 100, 50);
        Send_Client.setVisible(true);
        Send_Client.setEnabled(false); // Initially disabled
        Send_Client.addActionListener(e -> {
            synchronized (Text_Client) {
                String message = "Server: " + Text_Client.getText();
                if (!message.isEmpty()) {
                    System.out.println("Server sending message: " + message); // Debugging statement
                    for (ClientHandler handler : clientHandlers.values()) {
                        handler.sendMessage(message);
                    }
                    Chat_Client.append(message + "\n");
                    Text_Client.setText("");
                }
            }
        });
        frame1.add(Send_Client);
        Port_Input = new JTextField();
        Port_Input.setBounds(500, 25, 100, 25);
        Port_Input.setVisible(true);
        frame1.add(Port_Input);
        Start_Server_Button = new JButton("Start Server");
        Start_Server_Button.setBounds(600, 25, 150, 25);
        Start_Server_Button.setVisible(true);
        Start_Server_Button.addActionListener(e -> {
            String portText = Port_Input.getText();
            try {
                int port = Integer.parseInt(portText);
                if (serverSocket == null || serverSocket.isClosed()) {
                    TCP_Server(port); // Start the server on the specified port
                } else {
                    JOptionPane.showMessageDialog(null, "Server already running", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid port number", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to start server", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        frame1.add(Start_Server_Button);

        frame1.setVisible(true);
    }
    public void Run() {
        TCP_SERVER_INIT();
    }
    public static void main(String[] args) {
        TCP_Server_MultiClient server = new TCP_Server_MultiClient();
        server.Run();
    }
}
