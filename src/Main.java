import java.util.Scanner;

public class Main {
    private static Uploader server;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            String command = sc.nextLine();
            String arg[] = command.split(" ");
            if (arg[1].equals("-receive")) {
                System.out.println(">>");
                startLoading(arg[2]);
            }
            if (arg[1].equals("-serve")) {
                System.out.println("?");
                startSending(arg[3], arg[5]);

            }
        }
    }


    private static void startLoading(String name) {
        new Loader(name);
    }

    private static void startSending(String name, String path) {
        if (server == null) server = new Uploader();
        server.serveFile(name, path);
    }

}
