import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Uploader {
    private static final Object lock = new Object();
    private ArrayList<String> fileNames = new ArrayList<>();
    private ArrayList<String> filePaths = new ArrayList<>();
    private DatagramSocket socket;

    public Uploader(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                listen();
            }
        }).start();
    }

    public void serveFile(String name, String path){
        synchronized(lock){
            fileNames.add(name);
            filePaths.add(path);
        }
    }

    private void listen(){
        try{
            socket = new DatagramSocket(22);
            byte[] buffer = new byte[512];
            setEmptyBurffer(buffer);
            while(true){
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                synchronized(lock){
                    for(int i = 0; i < fileNames.size(); i++){
                        if(fileNames.get(i).equals(byteToString(packet.getData()))){
                            sendFile(packet.getAddress(), packet.getPort(), fileNames.get(i), filePaths.get(i));
                            break;
                        }
                    }
                }
                setEmptyBurffer(buffer);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void sendFile(InetAddress address, int port, String fileName, String filePath){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket((int) (Math.random() * 64000 + 1000));
                    byte[] file = castToByte(filePath);
                    int packetNum = (file.length / (512- 4) + 1);
                    String message = "" + packetNum + " " + file.length;
                    byte[] buffer = message.getBytes();
                    System.out.println("Sending file " + fileName + " to : " + address.getHostName() + " " + port);
                    DatagramPacket packet = new DatagramPacket(buffer, 0, buffer.length, address, port);
                    try{
                        buffer = new byte[512];
                        socket.send(packet);
                        int packets = 0;
                        int index = 0;
                        while(packets != packetNum){
                            setEmptyBurffer(buffer);
                            buffer[0] = (byte) (packets >> 24);
                            buffer[1] = (byte) (packets >> 16);
                            buffer[2] = (byte) (packets >> 8);
                            buffer[3] = (byte) (packets);
                            for(int i = 4; i < 512; i++){
                                buffer[i] = file[index];
                                index++;
                                if(index == file.length) break;
                            }
                            packet = new DatagramPacket(buffer, 0, buffer.length, address, port);
                            socket.send(packet);
                            socket.receive(new DatagramPacket(new byte[1], 1));
                            packets++;
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public static byte[] castToByte(String path) {
        File file = new File(path);
        ArrayList<Byte> arrayList = new ArrayList<>();
        try {
            FileInputStream inputStream = new FileInputStream(file);
            byte b;
            int i;
            while ((i = inputStream.read()) != -1) {
                b = (byte) i;
                arrayList.add(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] binaries = new byte[arrayList.size()];
        for (int i = 0; i < binaries.length; i++) {
            binaries[i] = arrayList.get(i);
        }
        return binaries;
    }
    public static String byteToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            if (b == -127) break;
            sb.append((char) b);
        }
        return sb.toString();
    }
    public static void setEmptyBurffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = -127;
        }
    }
}
