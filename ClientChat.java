/*
 * @Author: Outsider
 * @Date: 2021-12-28 10:07:57
 * @LastEditors: Outsider
 * @LastEditTime: 2021-12-29 09:52:10
 * @Description: In User Settings Edit
 * @FilePath: \Java_test\ChatApp\ClientChat.java
 */

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class ClientChat {
    public static void main(String[] args) {
        ConServer con=new ConServer();//打开连接界面
//        ClientJframe clientJframe = new ClientJframe();
//        clientJframe.init();
    }
}


class ConServer extends JFrame{
    JTextField text=null;
    ConServer(){
        this.getContentPane().setLayout(null);//不是用布局管理器
        JLabel label=new JLabel("昵称:");
        label.setBounds(35,50,45,30);
        this.getContentPane().add(label);
        text= new JTextField(10);
        text.setText("客户端");
        text.setBounds(100,50,100,30);
        this.getContentPane().add(text);

        JButton button=new JButton("连接");
        button.setBounds(75,100,70,30);
        this.getContentPane().add(button);
        button.addActionListener(new listener());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setBounds(200,200,250,200);
        this.setVisible(true);


    }
    class listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ClientJframe clientJframe = new ClientJframe();
            clientJframe.ClientName=text.getText();
            clientJframe.init();
            Close();
        }
    }
    public void Close(){
        this.setVisible(false);
    }
}

class ClientJframe extends JFrame {
    public String ClientName=null;

    //GUI布局
    //聊天记录显示区
    public JTextArea textArea = new JTextArea(10, 20);
    public JScrollPane scrollPane=new JScrollPane(textArea);//滚动面板，将文本区域加入到其中可以消息垂直滚动
    //聊天记录输入区
    //private JTextField textField = new JTextField(30);
    public JTextArea messageArea =new JTextArea(3,30);//可以输入多行
    public JButton sentButton=new JButton("发送");
    public JPanel panel=new JPanel();

    //端口
    // 静态常量主机端口号
    private static final String HOST = "127.0.0.1";
    // 静态常量服务器端口号
    private static final int PORT = 9999;
    public Socket socket = null;

    //Client发送数据
    public DataOutputStream dataOutputStream = null;

    //客户端连接上服务器判断符号
    private boolean isConn = false;

    /**
     * 无参的构造方法 throws HeadlessException
     */
    public ClientJframe() throws HeadlessException {
        super();
    }

    public void init() {
        this.setTitle("客户端窗口");
        this.textArea.setLineWrap(true);//设置文本区自动换行
        //设置文本区域自动滚动到最后一行
        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        //设置滚动面板垂直滚动
        //ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS一直使用垂直滚动条
        //VERTICAL_SCROLLBAR_AS_NEEDED需要时使用垂直滚动条
        this.scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);//使用垂直滚动条
        this.scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);//不适用水平滚动条

        ((DefaultCaret) textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JMenuBar menuBar=new JMenuBar();
        JMenu connSetting=new JMenu("连接设置");
        JMenu userSetting=new JMenu("用户设置");
        menuBar.add(connSetting);
        menuBar.add(userSetting);

        JMenuItem con=new JMenuItem("连接");
        connSetting.add(con);

        this.setJMenuBar(menuBar);

//        JPanel jpanel=new JPanel();
//        GridBagLayout gbl=new GridBagLayout();
//        GridBagConstraints constraints=new GridBagConstraints();
//        JTextField name=new JTextField(10);
//        JButton but=new JButton("打开");
//        constraints.gridx=0;
//        constraints.gridy=0;
//        constraints.gridwidth=2;
//        //constraints.weightx=2;
//        //constraints.weighty=1;
//        constraints.gridheight=1;
//        gbl.setConstraints(name,constraints);
//        jpanel.setLayout(gbl);
//        jpanel.add(name);
//        constraints.gridx=2;
//        constraints.gridy=0;
//        constraints.gridwidth=2;
//        constraints.gridheight=1;
//        gbl.setConstraints(but,constraints);
//        jpanel.add(but);
//        this.add(jpanel,BorderLayout.NORTH);

        this.add(scrollPane, BorderLayout.CENTER);


        this.panel.add(messageArea);
        this.sentButton.setBorderPainted(false);//不显示按钮边界
        this.panel.add(sentButton);
//        this.panel.add(new JLabel("在线状态"),BorderLayout.EAST);

        this.add(panel, BorderLayout.SOUTH);

        this.setBounds(300, 300, 400, 400);

        //设置颜色
        this.panel.setBackground(new Color(164, 168, 167));
        this.textArea.setBackground(new Color(148, 167, 163));

        //点击发送按钮发送消息
        this.sentButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String strSent= messageArea.getText();
                send(strSent);
                messageArea.setText("");//发送后文本设为空
            }
        });

//        // 添加监听，使回车键可以发送消息(判断数据合法性)，
//        // 并輸入到聊天框，换行
//        this.textField.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String strSend = textField.getText();
//                // 去掉空格判断长度是否为空
//                if (strSend.trim().length() == 0) {
//                    return;
//                }
//                //客户端信息strSend发送到服务器上
//                send(strSend);
//                textField.setText("");
//                //textArea.append(strSend + "\n");
//            }
//        });

        //关闭事件
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        textArea.setEditable(false);//聊天区域不可以输入
        messageArea.requestFocus();//光标聚焦

        try {
            socket = new Socket(HOST, PORT);
            isConn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 启动多线程
        new Thread(new Receive()).start();
        send(ClientName);
        this.setVisible(true);//设置窗口可见
    }

    /**
     * 客户端发送信息到服务器上的方法
     */
    public void send(String str) {
        try {
            //获取输出流对象
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *多线程的类，实现了Runnable接口的类
     * 用于接收客户端转发来的数据
     */
    class Receive implements Runnable {
        @Override
        public void run() {
            try {
                while (isConn) {
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    String str = dataInputStream.readUTF();
                    //将消息写到文本区中
                    textArea.append(str);
                }
            } catch (SocketException e) {
                System.out.println("服务器已关闭！");
                textArea.append("服务器已关闭！");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}