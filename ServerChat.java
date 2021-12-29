
/*
 * @Author: Outsider
 * @Date: 2021-12-28 10:03:13
 * @LastEditors: Outsider
 * @LastEditTime: 2021-12-28 11:17:44
 * @Description: In User Settings Edit
 * @FilePath: \Java_test\ChatApp\ServerChat.java
 */

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ServerChat {
    //main方法，程序的入口：
    public static void main(String[] args) throws Exception {
        ServerFrame serverJFrame = new ServerFrame();
        serverJFrame.init();
    }
}

class ServerFrame extends JFrame {
    //GUI相关属性
    JTextArea textArea = new JTextArea();//文本区域，用于显示消息

    JScrollPane scrollPane=new JScrollPane(textArea);//滚动面板，将文本区域加入到其中可以消息垂直滚动

    JTextField showOnline=new JTextField(15);
    JLabel showOn=new JLabel("当前在线人数:"+0);

    JPanel buttonPanel = new JPanel();
    JButton startButton = new JButton("启动");
    JButton stopButton = new JButton("停止");

    //服务器端口号
    private static final int PORT = 9999;
    //ServerSocket对象
    private ServerSocket serverSocket = null;
    //Socket对象
    private Socket socket = null;

    // 多个客户端访问时，客户端对象存放入List中
    private ArrayList<ClientCon> conList = new ArrayList<ClientCon>();


    // 服务器启动的标志
    private boolean isStart = false;

    public void init() throws Exception {
        this.setTitle("服务器端窗口");
        this.textArea.setEditable(false);//设置文本显示区域不可写
        this.textArea.setLineWrap(true);//设置文本自动换行
        //设置文本区域自动滚动到最后一行
        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        this.textArea.setBackground(new Color(203, 208, 206));//设置背景色
        //将按钮添加到按钮面板
        this.buttonPanel.setBackground(new Color(184, 222, 217));//设置背景色

        this.buttonPanel.add(showOn);
        this.buttonPanel.add(startButton);
        this.buttonPanel.add(stopButton);
        //设置滚动面板垂直滚动
        this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);//使用垂直滚动条
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);//不适用水平滚动条

        //框架默认布局为流式布局
        this.add(scrollPane, BorderLayout.CENTER);//放置在中间，默认使用剩下的所有区域
        this.add(buttonPanel, BorderLayout.SOUTH);//放置在南部

        //设置窗体位置
        this.setBounds(0, 0, 400, 500);

        //判断服务器是否已经开启
        if (isStart) {
            System.out.println("服务器已经启动\n");
        } else {
            System.out.println("服务器还没有启动，请点击启动服务器！\n");
        }

        //按钮监听监听服务器开启，置开始位false
        this.startButton.addActionListener(new ActionListener() {
            //重写按钮监听方法
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket == null) {
                        serverSocket = new ServerSocket(PORT);
                    }
                    isStart = true;
                    textArea.append("服务器已启动\n");
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        // 终止按钮监听停止服务器
        this.stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (serverSocket != null) {
                        serverSocket.close();
                        isStart = false;
                    }
                    //System.exit(0);
                    textArea.append("服务器已断开\n");
                    System.out.println("服务器已断开\n");

                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        /*
         * 服务器窗口关闭应该停止服务器，需改进的代码
         */
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        startServer();
    }

    /**
     * 服务器启动代码
     */
    public void startServer() throws Exception {
        try {
            try {
                serverSocket = new ServerSocket(PORT);
                isStart = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 可以接受多个客户端的连接
            // 接每一個信息时，服务器不可以终断，所以将其写入while（）中
            while (isStart) {
                socket = serverSocket.accept();
                ClientCon con=new ClientCon(socket);
                conList.add(con);
                this.showOn.setText("当前在线人数 : "+conList.size());//显示在线人数
                System.out.println(socket.getPort()+con.ClientName+"上线了\n");
                textArea.append(socket.getPort()+con.ClientName+"上线了\n");
            }
        } catch (SocketException e) {
            System.out.println("服务器终断了！！！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     *  内部类声明 对象 这个对象是属于服务器端的一个连接对象
     */
    class ClientCon implements Runnable {
        public String ClientName=null;
        Socket socket = null;

        public ClientCon(Socket socket) {
            this.socket = socket;
            /*
              线程启动在这里：
              初始化方法里 初始化一个线程 ，线程中封装的是自己，做整个线程的调用
             */
            (new Thread(this)).start();
        }

        //接受客户端信息（多线程run（）方法）
        @Override
        public void run() {
            try {
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                boolean flag=true;//判断是否是第一次连接
                // 为了让服务器能够接受到每个客户端的多句话
                while (isStart) {
                    //readUTF()是一种阻塞方法，接一句就执行完了，所以循环中
                    String str = dataInputStream.readUTF();
                    if (flag) {
                        //首次发送的是客户端用户设置的昵称
                        ClientName = str;
                        str=ClientName+"已上线";
                        flag = false;
                    }
                    showOn.setText("当前在线人数 : "+conList.size());//重新检查在线人数
                    System.out.println(socket.getPort() + ":" + ClientName + " : \n" + str+"\n");
                    textArea.append(socket.getPort() + ClientName + " : \n" + str+"\n");
                    //服务器向每个客户端发送别的客户端发来的信息
                    // 遍历ccList，调用send方法,在客户端里接受应该是多线程的接受
                    String strSend = socket.getPort() + ClientName + " : \n" + str+"\n";
                    for (ClientCon clientCon : conList) {
                        //给每一个客户端转发消息
                        if(clientCon.socket==null){
                            conList.remove(clientCon);
                        }else {
                            clientCon.send(strSend);
                        }

                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 服务器向每個连接对象发送数据的方法
        public void send(String str) {
            try {
                DataOutputStream dataOutputStream = new DataOutputStream(this.socket.getOutputStream());
                dataOutputStream.writeUTF(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}