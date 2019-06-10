package DedicatedServer.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

class TeacherController {

    private static Socket ProfTCPSocket;


    public TeacherController(Socket profTCPSocket) {
        super();
        ProfTCPSocket = profTCPSocket;
    }


    public void run() {
        processMessage(ProfTCPSocket);
    }

    /**
     * 向教师端返回第一个数字的结果
     * 0 - 连接成功
     * 1 - 签到已开启
     * 2 - 收到新的签到信息
     * 3 - 签到结束
     * 4 - 连接即将断开
     *
     * 来自教师端信息的第一个数字含义
     * 0 - 尝试连接
     * 1 - 开启签到
     * 2 - 关闭签到
     * 3 - 断开连接
     */
    public static void processMessage(Socket tcpSocket) {
        ProfTCPSocket = tcpSocket;
        Thread thread = new Thread(() -> {
            try {
                PrintStream printStream = (PrintStream) ProfTCPSocket.getOutputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(ProfTCPSocket.getInputStream()));
                String line = bufferedReader.readLine();

                String message = "";
                while(line!=null&&line!="") {
                    message += line;
                    printStream.println("Hello prof, your message received.");
                    printStream.flush();
                    System.out.println("From prof:"+line);
                }
                //TODO:implement the processing of prof message.
                String result = ProcessContent(message);

                printStream.close();
                bufferedReader.close();
                ProfTCPSocket.close();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        });
        thread.start();

    }

    private static String ProcessContent(String content){
        //Todo

        return "";
    }

}
