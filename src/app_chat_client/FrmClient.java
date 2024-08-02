package app_chat_client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class FrmClient extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField textField;
    private JTabbedPane tabbedPane;
    private PrintWriter out;
    private Map<String, JTextArea> userTextAreas = new HashMap<>();
    private JPanel buttonPanel;
    private String currentUserName;
    private Socket socket;
    private Map<String, byte[]> receivedFiles = new HashMap<>();
    private Map<String, String> receivedFileNames = new HashMap<>();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    FrmClient frame = new FrmClient();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the frame.
     */
    public FrmClient() {
        // Solicitar el nombre de usuario al iniciar la aplicación
        currentUserName = JOptionPane.showInputDialog(this, "Ingresa tu nombre de usuario:");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 652, 424);

        // Crear barra de menú
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Añadir menú Archivo
        JMenu mnNewMenu = new JMenu("Archivo");
        menuBar.add(mnNewMenu);

        // Añadir ítem Salir al menú Archivo
        JMenuItem mntmNewMenuItem = new JMenuItem("Salir");
        mnNewMenu.add(mntmNewMenuItem);

        // Crear panel principal con un diseño BorderLayout y color de fondo
        contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(0, 0));
        contentPane.setBackground(Color.decode("#D5D9DF"));
        setContentPane(contentPane);

        // Panel izquierda
        JPanel leftPanel = new JPanel();
        leftPanel.setPreferredSize(new Dimension(200, 0));
        leftPanel.setBackground(Color.decode("#365c7e"));
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(8, 0, 0, 0, Color.decode("#B8BCC4")),
                BorderFactory.createMatteBorder(0, 0, 18, 0, Color.decode("#365c7e"))
        ));
        contentPane.add(leftPanel, BorderLayout.WEST);
        leftPanel.setLayout(new BorderLayout(0, 0));

        // Panel contenedor con padding dentro del panel izquierdo
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout(0, 0));
        containerPanel.setBackground(Color.decode("#B8BCC4"));
        containerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        leftPanel.add(containerPanel, BorderLayout.CENTER);

        // Crear etiqueta de nombre de usuario
        JLabel lblUsername = new JLabel(" " + currentUserName);
        lblUsername.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblUsername.setForeground(Color.BLACK);
        lblUsername.setBackground(new Color(213, 217, 223));
        lblUsername.setOpaque(true);
        lblUsername.setHorizontalAlignment(SwingConstants.LEFT);
        lblUsername.setBorder(new LineBorder(Color.GRAY, 1, true));
        lblUsername.setPreferredSize(new Dimension(lblUsername.getPreferredSize().width, 30));
        containerPanel.add(lblUsername, BorderLayout.NORTH);

        // Panel para los botones dentro del contenedor
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        containerPanel.add(buttonPanel, BorderLayout.CENTER);

        // Panel derecho que contendrá el topPanel y el tabbedPane
        JPanel rightPanel = new JPanel();
        rightPanel.setBorder(BorderFactory.createMatteBorder(18, 7, 18, 7, Color.decode("#365c7e")));
        rightPanel.setLayout(new BorderLayout(0, 0));
        contentPane.add(rightPanel, BorderLayout.CENTER);

        // Panel superior para el nombre de usuario
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(Color.decode("#D5D9DF"));
        topPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        rightPanel.add(topPanel, BorderLayout.NORTH);

        // Crear etiqueta de nombre de usuario en la parte superior del panel derecho
        JLabel lblUserNameTop = new JLabel("Nombre de Usuario: " + currentUserName, JLabel.CENTER);
        lblUserNameTop.setFont(new Font("Tahoma", Font.PLAIN, 16));
        lblUserNameTop.setForeground(Color.BLACK);
        topPanel.add(lblUserNameTop, BorderLayout.CENTER);

        // Crear tabbedPane para las pestañas de chat
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        rightPanel.add(tabbedPane, BorderLayout.CENTER);

        // Conectar al servidor
        connectToServer(currentUserName);
    }

    // Método para crear una pestaña de chat
    private void createChatTab(String userName) {
        JPanel panelChat = new JPanel();
        panelChat.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.decode("#D5D9DF")));
        tabbedPane.addTab(userName, null, panelChat, null);
        panelChat.setLayout(new BorderLayout(0, 0));

        // Crear área de texto para mostrar mensajes de chat
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        textArea.setEditable(false);
        panelChat.add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Crear panel de botones en la parte superior del chat
        JPanel panelButtons = new JPanel();
        panelButtons.setBackground(Color.decode("#D5D9DF"));
        panelButtons.setLayout(new BorderLayout());
        panelButtons.setBorder(BorderFactory.createMatteBorder(0, 0, 10, 0, Color.decode("#D5D9DF")));
        panelChat.add(panelButtons, BorderLayout.NORTH);

        // Crear contenedor de botones dentro del panel de botones
        JPanel buttonContainer = new JPanel();
        buttonContainer.setBackground(Color.decode("#D5D9DF"));
        buttonContainer.setLayout(new BorderLayout(20, 0));
        panelButtons.add(buttonContainer, BorderLayout.EAST);

        // Crear botones de recibir y compartir archivo
        JButton btnReceiveFile = new JButton("Recibir Archivo");
        btnReceiveFile.setPreferredSize(new Dimension(140, 30));
        btnReceiveFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Manejar la recepción de archivos
                ReceiveFileDialog receiveFileDialog = new ReceiveFileDialog(FrmClient.this, receivedFiles, receivedFileNames, currentUserName, userName, out);
                receiveFileDialog.setVisible(true);
            }
        });
        buttonContainer.add(btnReceiveFile, BorderLayout.WEST);

        JButton btnShareFile = new JButton("Compartir Archivo");
        btnShareFile.setPreferredSize(new Dimension(140, 30));
        btnShareFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Manejar la selección y envío de archivos
                ShareFileDialog shareFileDialog = new ShareFileDialog(FrmClient.this, out, currentUserName, userName);
                shareFileDialog.setVisible(true);
            }
        });
        buttonContainer.add(btnShareFile, BorderLayout.EAST);

        // Crear panel de envío de mensajes en la parte inferior del chat
        JPanel panelSend = new JPanel();
        panelChat.add(panelSend, BorderLayout.SOUTH);
        panelSend.setLayout(new BorderLayout(0, 0));

        // Crear campo de texto para escribir mensajes
        textField = new JTextField();
        textField.setFont(new Font("Monospaced", Font.PLAIN, 16));
        panelSend.add(textField, BorderLayout.CENTER);
        textField.setColumns(10);
        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = textField.getText();
                    if (!message.isEmpty()) {
                        out.println(currentUserName + "@" + userName + ": " + message);
                        userTextAreas.get(userName).append("Yo: " + message + "\n");
                        textField.setText("");
                    }
                }
            }
        });

        userTextAreas.put(userName, textArea);
    }

    // Método para crear un botón de contacto
    private JButton createContactButton(String name, int tabIndex) {
        JButton button = new JButton(name);
        button.setFont(new Font("Tahoma", Font.PLAIN, 16));
        button.setBackground(Color.decode("#FFC608"));
        button.setHorizontalAlignment(SwingConstants.CENTER);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setSelectedIndex(tabIndex);
            }
        });
        return button;
    }

    // Método para conectar al servidor
    private void connectToServer(String userName) {
        try {
            socket = new Socket("localhost", 12346);
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(userName);

            // Hilo para recibir mensajes del servidor
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println("Mensaje recibido del servidor: " + message); // Debugging
                            if (message.startsWith("USER_LIST")) {
                                updateUsersList(message);
                            } else if (message.startsWith("FILE_AVAILABLE:")) {
                                handleFileAvailable(message);
                            } else if (message.startsWith("FILE:")) {
                                handleFileReception(message);
                            } else {
                                String[] parts = message.split(":", 2);
                                if (parts.length == 2) {
                                    String[] userParts = parts[0].split("@");
                                    if (userParts.length == 2) {
                                        String sender = userParts[0].trim();
                                        String receiver = userParts[1].trim();
                                        String text = parts[1].trim();
                                        if (receiver.equals(currentUserName)) {
                                            if (userTextAreas.containsKey(sender)) {
                                                userTextAreas.get(sender).append(sender + ": " + text + "\n");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para actualizar la lista de usuarios
    private void updateUsersList(String userListMessage) {
        String[] users = userListMessage.split(" ");
        for (int i = 1; i < users.length; i++) {
            String userName = users[i];
            if (!userName.equals(currentUserName) && !userTextAreas.containsKey(userName)) {
                createChatTab(userName);
                JButton newButton = createContactButton(userName, tabbedPane.getTabCount() - 1);
                buttonPanel.add(newButton);
            }
        }
        buttonPanel.revalidate();
        buttonPanel.repaint();
    }

    private void handleFileAvailable(String message) {
        String[] parts = message.split(":", 2);
        if (parts.length == 2) {
            String key = parts[1];
            String[] keyParts = key.split(":");
            if (keyParts.length == 3) {
                String fileName = keyParts[2];
                receivedFileNames.put(key, fileName);
                System.out.println("CLIENT: Archivo disponible: " + fileName + ", Key: " + key);
            }
        }
    }

    private void handleFileReception(String message) {
        String[] parts = message.split(":", 4);
        if (parts.length == 4) {
            String senderReceiver = parts[1];
            String fileName = parts[2];
            byte[] fileContent = Base64.getDecoder().decode(parts[3]);
            if (fileContent == null || fileContent.length == 0) {
                System.out.println("CLIENT: El contenido del archivo recibido es nulo o vacío. Key: " + senderReceiver + ":" + fileName);
            } else {
                // Guardar el archivo temporalmente en el disco
                try {
                    String key = senderReceiver + ":" + fileName;
                    receivedFiles.put(key, fileContent);
                    receivedFileNames.put(key, fileName);
                    File tempFile = new File("temp_" + fileName);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(fileContent);
                    }
                    System.out.println("CLIENT: Archivo recibido y guardado temporalmente: " + fileName + ", Key: " + key + ", Tamaño: " + fileContent.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
