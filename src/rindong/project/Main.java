package rindong.project;

import rindong.project.runnable.MessageListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args)
    {
        new serverFrame();
    }

    public static class serverFrame extends JFrame
    {
        private ServerSocket server;

        //用来记录所有用户的信息输出流
        private Map<Socket, MessageListener> users=new HashMap<>();

        //用来记录所有用户的信息输入流
        private Map<Socket, PrintWriter> socketInputStream = new HashMap<>();

        //用来记录所有用户的链接socket
        private List<Socket> sockets = new ArrayList<>();

        //客户端向服务端发送信息的显示的地方
        private JTextArea conMessage=new JTextArea();

        private JScrollPane panel=new JScrollPane(conMessage);

        //服务端向客户端发送信息的地方
        private JTextField inputArea = new JTextField();

        public serverFrame()
        {
            setTitle("socket服务器");
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setBounds(500,400,800,600);
            addButton();
            try {
                server=new ServerSocket(8390);
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this,"错误！" + ex.getMessage());
                ex.printStackTrace();
            }
            runnable();
            setVisible(true);
        }

        private void addButton()
        {
            conMessage.setFont(new Font(null,Font.PLAIN,30));
            conMessage.setEditable(false);
            getContentPane().add(panel,BorderLayout.CENTER);
            inputArea.setFont(new Font(null,Font.PLAIN,30));
            //添加文本域回车事件（发送信息）
            inputArea.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (!inputArea.getText().equals(""))
                    {
                        conMessage.append("你:" + inputArea.getText() + "\n");

                        if (users.size() != 0)
                        {
                            //向所有客户端的信息输入流传入信息
                            for (Socket s : socketInputStream.keySet())
                            {
                                socketInputStream.get(s).println("主机：" + inputArea.getText());
                            }
                        }
                        inputArea.setText("");
                    }
                }
            });
            getContentPane().add(inputArea,"South");
        }

        private void runnable()
        {
            //监听客户端链接线程
            new Thread()
            {
                @Override
                public void run()
                {
                    try {
                        while (true)
                        {
                            Socket socket = server.accept();
                            if (!users.containsKey(socket))
                            {
                                //客户端链接阈值
                                if (users.size() < 5)
                                {
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                    MessageListener listener = new MessageListener(reader,serverFrame.this,socket);
                                    listener.start();
                                    users.put(socket,listener);
                                    socketInputStream.put(socket,new PrintWriter(socket.getOutputStream(),true));
                                    sockets.add(socket);
                                    conMessage.append("客户端连接，ip：" + socket.getInetAddress() + "\n");
                                    conMessage.append("现在聊天室人数：" + (1 + users.size()) + "\n");
                                    for (Socket s : socketInputStream.keySet())
                                    {
                                        socketInputStream.get(s).println("客户端连接，ip：" + socket.getInetAddress());
                                        socketInputStream.get(s).println("现在聊天室人数：" + (1 + users.size()));
                                    }
                                }
                                else
                                {
                                    System.out.println("客户端接入数量已满！已拒绝一名接入！");
                                    conMessage.append("客户端接入数量已满！已拒绝一名接入！");
                                    socket.close();
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        JOptionPane.showMessageDialog(serverFrame.this,"错误！" + ex.getMessage());
                        ex.printStackTrace();
                        for (Socket s : socketInputStream.keySet())
                        {
                            socketInputStream.get(s).println("服务器出现错误，连接终止。");
                            socketInputStream.remove(s);
                            sockets.remove(s);
                            users.remove(s);
                            try {
                                s.close();
                            }
                            catch (IOException ex2)
                            {
                                ex2.printStackTrace();
                            }
                        }
                        dispose();

                    }
                }
            }.start();
        }

        //在监听客户端发送信息的线程中调用此方法，向服务端输出接收到的信息，同时向其他客户端输出。
        public void SendMessageToHost(String message,Socket s)
        {
            //如果信息是exit则客户端退出
            if (message.equals("exit"))
            {
                users.remove(s);
                sockets.remove(s);
                socketInputStream.remove(s);
                conMessage.append("客户端：" + s.getInetAddress() + " 已退出");
                conMessage.append("现在聊天室人数：" + (1 + users.size()));
                if (users.size() != 0)
                {
                    for (Socket s_2 : socketInputStream.keySet())
                    {
                        socketInputStream.get(s_2).println("客户端：" + s.getInetAddress() + " 已退出");
                        socketInputStream.get(s_2).println("现在聊天室人数：" + (1 + users.size()));
                    }
                }
                return;
            }
            else
            {
                conMessage.append("\n" + message);
                for (Socket s_2 : socketInputStream.keySet())
                {
                    if (!s_2.equals(s))
                    {
                        socketInputStream.get(s_2).println(message);
                    }
                }
            }
        }


        @Override
        public void dispose()
        {
            try {
                server.close();
                for (Socket s : sockets)
                {
                    users.remove(s);
                    socketInputStream.remove(s);
                    sockets.remove(s);
                    s.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
            super.dispose();
        }
    }
}
