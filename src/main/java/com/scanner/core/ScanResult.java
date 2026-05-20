package com.scanner.core;

/**
 * Clasa ce reprezintă rezultatul scanării pentru un singur port.
 * Stochează portul, starea acestuia (deschis/închis) și un posibil banner (ex: HTTP, SSH).
 */
public class ScanResult {
    private final int port;
    private final boolean isOpen;
    private final String serviceBanner;

    public ScanResult(int port, boolean isOpen, String serviceBanner) {
        this.port = port;
        this.isOpen = isOpen;
        this.serviceBanner = serviceBanner;
    }

    public int getPort() {
        return port;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public String getServiceBanner() {
        return serviceBanner;
    }

    @Override
    public String toString() {
        return "Port " + port + " este " + (isOpen ? "DESCHIS" : "ÎNCHIS") + 
               (serviceBanner != null && !serviceBanner.isEmpty() ? " [" + serviceBanner + "]" : "");
    }
}
