package rindong.project.runnable;

import rindong.project.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

//监听客户端向服务端发送信息的线程
public class MessageListener extends Thread {

    public BufferedReader reader;
    public Main.serverFrame server;
    public Socket self;

    public MessageListener(BufferedReader r, Main.serverFrame s, Socket self)
    {
        reader = r;
        server = s;
        this.self = self;
    }

    @Override
    public void run() {
        while (true)
        {
            try {
                String message = reader.readLine();
                server.SendMessageToHost(message,self);
                if (message.equals("exit"))
                {
                    self.close();
                    break;
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
                break;
            }
        }
    }
}
