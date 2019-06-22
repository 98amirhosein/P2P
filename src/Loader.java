import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;

public class Loader {
    private String fileName;
    private DatagramSocket socket;

    public Loader(String name) {
        fileName = name;

        try {
            socket = new DatagramSocket(512);
            socket.setBroadcast(true);
            requestFile();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestFile() {
        String message = fileName;
        byte[] buffer = message.getBytes();
        DatagramPacket packet;
        try {
            packet = new DatagramPacket(buffer, 0, buffer.length, Inet4Address.getLocalHost(), 22);
            socket.send(packet);
            buffer = new byte[512];
            setEmptyBurffer(buffer);
            packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String[] splits = byteToString(packet.getData()).split(" ");
            int fileSize = Integer.parseInt(splits[1]);
            int packetNum = Integer.parseInt(splits[0]);
            System.out.println("Receiving file from " + packet.getAddress().getHostName() + " " + packet.getPort() + ".");
            System.out.println("File size is : " + fileSize + " bytes");
            System.out.println("Number of packets to be received is " + packetNum);
            int packets = 0;
            byte[] file = new byte[fileSize];
            while (packets != packetNum) {
                setEmptyBurffer(buffer);
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byte[] data = packet.getData();
                int offset = data[0] << 24 | (data[1] & 0xFF) << 16 | (data[2] & 0xFF) << 8 | (data[3] & 0xFF);
                System.out.println("Packet received : " + offset + " " + packets + " " + packetNum);
                for (int i = 4; i < data.length; i++) {
                    try {
                        file[offset * (512 - 4) + i - 4] = data[i];
                    } catch (Exception e) {
                        break;
                    }
                }
                packets++;
                socket.send(new DatagramPacket(new byte[1], 0, 1, packet.getAddress(), packet.getPort()));
            }
            System.out.println("All packets received.");
            writeFile(fileName, file, fileSize);
            System.out.println("File created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String byteToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            if (b == -127) break;
            sb.append((char) b);
        }
        return sb.toString();
    }

    public static void writeFile(String fileName, byte[] byteArray, int size) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\AmirHosein\\Desktop\\P2P File\\" + fileName);
            fileOutputStream.write(byteArray, 0, size);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void setEmptyBurffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = -127;
        }
    }
}
