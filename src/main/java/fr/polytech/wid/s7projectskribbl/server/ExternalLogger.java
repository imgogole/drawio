package fr.polytech.wid.s7projectskribbl.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

public class ExternalLogger extends Thread {
    private final int Port;
    private PrintWriter out;
    private final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private volatile boolean running = true;

    public ExternalLogger(int Port) {
        this.Port = Port;
    }

    public void run() {
        try (ServerSocket server = new ServerSocket(Port)) {
            server.setSoTimeout(1000);

            Socket s = null;
            while (running && s == null) {
                try {
                    s = server.accept();
                } catch (SocketTimeoutException e) {  }
            }

            if (s != null) {
                this.out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
                while (running) {
                    String msg = queue.poll();
                    if (msg != null) out.println(msg);
                    else Thread.sleep(100);
                }
            }
        } catch (IOException | InterruptedException e) {
            if (running) System.err.println("Logger Error: " + e.getMessage());
        }
    }

    public void LogLn(Object o) {
        String msg = String.valueOf(o);
        if (out != null) {
            out.println(msg);
        } else {
            queue.add(msg);
        }
    }

    public void Close()
    {
        running = false;
        if (out != null) out.close();
    }
}