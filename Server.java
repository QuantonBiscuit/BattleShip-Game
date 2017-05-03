import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * Created by Jacob on 4/23/2017.
 * Server for Main game and ChatClient
 */
public class Server extends JFrame{

    //JLabels
    private JLabel jlIP = new JLabel("IP  ");
    private JLabel jlPort = new JLabel("Port  ");

    //JTextFields
    private JTextField jtfIP = new JTextField(20);
    private JTextField jtfPort = new JTextField(5);

    //JTextArea
    private JTextArea jta = new JTextArea(20,40);

    //JButton
    private JButton jbConnect = new JButton("Connect");

    //JPanels
    private JPanel jpInput = new JPanel();
    private JPanel jpButton = new JPanel();
    private JPanel jpTextArea = new JPanel();

    //Border for JTextArea
    private Border border = BorderFactory.createLineBorder(Color.BLACK);

    //ArrayList of Clients
    private ArrayList<Socket> clients = new ArrayList<Socket>();

    public static void main(String[] args){
        new Server();
    }

    private Server(){
        jta.setBorder(BorderFactory.createCompoundBorder(border,
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        jta.setEnabled(false);

        jpInput.add(jlIP);
        jpInput.add(jtfIP);
        jpInput.add(jlPort);
        jpInput.add(jtfPort);

        jpTextArea.add(jta);

        jpButton.add(jbConnect);

        jlIP.setHorizontalAlignment(JLabel.RIGHT);
        jlPort.setHorizontalAlignment(JLabel.RIGHT);

        add(jpInput,BorderLayout.NORTH);
        add(jpTextArea, BorderLayout.CENTER);
        add(jpButton, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setSize(500,500);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        ServerSocket ss;

        try{
            ss = new ServerSocket(16789);
            while(true){
                Socket s = ss.accept();
                clients.add(s);
                ServerThread st = new ServerThread(s);
                st.start();
            }


        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    class ServerThread extends Thread{
        Socket sock;
        BufferedReader br;
        ObjectInputStream obr;

        public ServerThread(Socket _s){
            sock = _s;
            try{
                br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                obr = new ObjectInputStream(sock.getInputStream());
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public void run(){
            try{
                while(br.ready()){
                    //read in the first line to determine what type of data is being sent in
                    String command = br.readLine();

                    //If a message is being sent from the chat
                    if(command == "CHAT"){
                        String message = br.readLine();
                        sendMessage(message);
                    }
                    else if(command == "SPECTATOR-CHAT"){
                        String message = br.readLine();
                        sendSpectatorMessage(message);
                    }
                    else if(command == "DATA"){
                        String player = br.readLine();
                        int buttoneNum1 = obr.readInt();
                        int buttonNum2 = obr.readInt();
                        sendButtonNumber(buttoneNum1, buttonNum2, player);
                    }
                    else if(command == "RESULT"){
                        String player = br.readLine();
                        boolean isHit = obr.readBoolean();
                        sendResult(isHit, player);
                    }
                }
            }
            catch(NullPointerException e){
                try {
                    sock.close();
                }
                catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public synchronized void sendMessage(String msg){
            try{
                for(Socket s: clients){
                    try {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                        pw.println("MESSAGE");
                        pw.flush();
                        pw.println(msg);
                        pw.flush();
                        pw.close();
                    }
                    catch(NullPointerException e){
                        try{
                            s.close();
                        }
                        catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public synchronized void sendSpectatorMessage(String msg){
            try{
                for(Socket s: clients){
                    try {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                        pw.println("SPECTATOR-MESSAGE");
                        pw.flush();
                        pw.println(msg);
                        pw.flush();
                        pw.close();
                    }
                    catch(NullPointerException e){
                        try{
                            s.close();
                        }
                        catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public synchronized void sendButtonNumber(int i, int y, String s){
            try{
                for(Socket so: clients) {
                    try {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(so.getOutputStream()));
                        pw.println("DATA");
                        pw.flush();
                        pw.println(s);
                        pw.flush();
                        pw.close();
                        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                        oos.writeInt(i);
                        oos.flush();
                        oos.writeInt(y);
                        oos.flush();
                        oos.close();
                    }
                    catch(NullPointerException e){
                        try{
                            so.close();
                        }
                        catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }

        public synchronized void sendResult(Boolean b, String s){
            try{
                for(Socket so: clients) {
                    try {
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
                        pw.println("RESULT");
                        pw.flush();
                        pw.println(s);
                        pw.flush();
                        pw.close();
                        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                        oos.writeBoolean(b);
                        oos.flush();
                        oos.close();
                    }
                    catch(NullPointerException e){
                        try {
                            so.close();
                        }
                        catch(IOException ioe){
                            ioe.printStackTrace();
                        }
                    }
                }
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
    }
}
