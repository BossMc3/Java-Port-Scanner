package com.scanner.util;

import com.scanner.core.ScanResult;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Clasă utilitară pentru exportul rezultatelor (Funcționalitate Bonus).
 */
public class ResultExporter {

    /**
     * Exportă o listă de rezultate într-un fișier CSV.
     * 
     * @param results Lista de rezultate scanate
     * @param file    Fișierul de destinație
     * @throws IOException Dacă apare o eroare la scriere
     */
    public static void exportToCSV(List<ScanResult> results, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Scriem antetul CSV
            writer.write("Port,Status,Serviciu\n");
            for (ScanResult result : results) {
                String status = result.isOpen() ? "DESCHIS" : "INCHIS";
                String service = result.getServiceBanner() != null ? result.getServiceBanner() : "";
                writer.write(String.format("%d,%s,%s\n", result.getPort(), status, service));
            }
        }
    }

    /**
     * Exportă o listă de rezultate într-un fișier TXT.
     * 
     * @param results Lista de rezultate scanate
     * @param file    Fișierul de destinație
     * @throws IOException Dacă apare o eroare la scriere
     */
    public static void exportToTXT(List<ScanResult> results, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("=== REZULTATE SCANARE PORTURI ===\n\n");
            for (ScanResult result : results) {
                writer.write(result.toString() + "\n");
            }
        }
    }
}
