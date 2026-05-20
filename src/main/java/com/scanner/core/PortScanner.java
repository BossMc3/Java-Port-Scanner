package com.scanner.core;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Logica principală pentru scanarea porturilor.
 * Folosește ExecutorService pentru a rula teste pe porturi în paralel (multithreading).
 */
public class PortScanner {

    /**
     * Metodă sincronă (blocantă pe thread-ul curent) care pornește procesul de scanare.
     * Această metodă ar trebui apelată dintr-un Thread de fundal (ex: SwingWorker).
     * 
     * @param targetIp Adresa IP sau host-ul țintă
     * @param startPort Portul de început
     * @param endPort Portul de final
     * @param numThreads Numărul de thread-uri (viteză/concurență)
     * @param timeout Timeout-ul pentru conectare în milisecunde
     * @param resultCallback Funcție apelată (callback) de fiecare dată când un port a fost scanat
     */
    public void scanPorts(String targetIp, int startPort, int endPort, int numThreads, int timeout,
                          Consumer<ScanResult> resultCallback) {
                                 
        // Creăm un Thread Pool cu un număr fix de fire de execuție
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int port = startPort; port <= endPort; port++) {
            final int currentPort = port;
            executor.submit(() -> {
                ScanResult result = scanSinglePort(targetIp, currentPort, timeout);
                if (resultCallback != null) {
                    // Trimitem rezultatul înapoi prin callback
                    resultCallback.accept(result);
                }
            });
        }

        // Cerem oprirea pool-ului pentru a nu mai accepta task-uri noi
        executor.shutdown();

        try {
            // Așteptăm finalizarea tuturor task-urilor din coadă
            // Acest apel va bloca thread-ul curent (ex. SwingWorker) până la terminarea scanării
            executor.awaitTermination(24, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            // Dacă procesul de scanare a fost întrerupt (ex. utilizatorul dă cancel)
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verifică un singur port pe adresa IP dată.
     */
    private ScanResult scanSinglePort(String ip, int port, int timeout) {
        // Folosim try-with-resources pentru a ne asigura că Socket-ul este închis corect
        try (Socket socket = new Socket()) {
            // Încercăm conectarea la IP și Port cu timeout-ul specificat
            socket.connect(new InetSocketAddress(ip, port), timeout);
            
            // Dacă ajungem aici, înseamnă că nu s-a aruncat nicio excepție, deci portul e DESCHIS
            String banner = guessServiceFromPort(port);
            
            return new ScanResult(port, true, banner);
        } catch (Exception ex) {
            // Orice excepție (ConnectException, SocketTimeoutException etc.) indică un port închis sau filtrat
            return new ScanResult(port, false, "");
        }
    }

    /**
     * O metodă simplă pentru a asocia porturile comune cu serviciile lor (Service Banner Grabbing de bază).
     */
    private String guessServiceFromPort(int port) {
        switch (port) {
            case 20: return "FTP-DATA";
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            case 8080: return "HTTP-Proxy";
            case 27017: return "MongoDB";
            default: return "Unknown";
        }
    }
}
