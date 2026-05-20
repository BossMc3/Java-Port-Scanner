package com.scanner.gui;

import com.scanner.core.PortScanner;
import com.scanner.core.ScanResult;
import com.scanner.util.ResultExporter;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fereastra principală a aplicației (GUI) folosind Swing.
 */
public class MainFrame extends JFrame {

    private JTextField ipField;
    private JTextField startPortField;
    private JTextField endPortField;
    private JTextField threadsField;
    private JTextField timeoutField;
    
    private JButton scanButton;
    private JButton exportButton;
    private JProgressBar progressBar;
    
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    
    private ScanWorker currentWorker;
    private List<ScanResult> allResults;

    public MainFrame() {
        setTitle("Java Port Scanner - Practică");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        allResults = new ArrayList<>();
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // --- PANOU DE SETĂRI (TOP) ---
        JPanel settingsPanel = new JPanel(new GridLayout(2, 6, 5, 5));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Setări Scanare"));

        settingsPanel.add(new JLabel("IP/Host:"));
        ipField = new JTextField("127.0.0.1");
        settingsPanel.add(ipField);

        settingsPanel.add(new JLabel("Port Start:"));
        startPortField = new JTextField("1");
        settingsPanel.add(startPortField);

        settingsPanel.add(new JLabel("Port Final:"));
        endPortField = new JTextField("1024");
        settingsPanel.add(endPortField);

        settingsPanel.add(new JLabel("Thread-uri:"));
        threadsField = new JTextField("50");
        settingsPanel.add(threadsField);

        settingsPanel.add(new JLabel("Timeout (ms):"));
        timeoutField = new JTextField("200");
        settingsPanel.add(timeoutField);

        scanButton = new JButton("Start Scanare");
        scanButton.addActionListener(e -> startScan());
        settingsPanel.add(scanButton);

        add(settingsPanel, BorderLayout.NORTH);

        // --- ZONA CENTRALĂ (TABEL) ---
        String[] columnNames = {"Port", "Stare", "Serviciu/Banner"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabelul nu este editabil
            }
        };
        resultsTable = new JTable(tableModel);
        resultsTable.setFillsViewportHeight(true);
        add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        // --- PANOU DE JOS (PROGRESS & EXPORT) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        bottomPanel.add(progressBar, BorderLayout.CENTER);

        exportButton = new JButton("Exportă Rezultate");
        exportButton.setEnabled(false);
        exportButton.addActionListener(e -> exportResults());
        bottomPanel.add(exportButton, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Validează input-urile și pornește procesul de scanare pe un thread separat (SwingWorker).
     */
    private void startScan() {
        if (currentWorker != null && !currentWorker.isDone()) {
            // Dacă deja scanează, butonul ar trebui să funcționeze ca "Stop"
            currentWorker.cancel(true);
            scanButton.setText("Start Scanare");
            return;
        }

        try {
            String ip = ipField.getText().trim();
            int startPort = Integer.parseInt(startPortField.getText().trim());
            int endPort = Integer.parseInt(endPortField.getText().trim());
            int threads = Integer.parseInt(threadsField.getText().trim());
            int timeout = Integer.parseInt(timeoutField.getText().trim());

            if (startPort > endPort || startPort < 1 || endPort > 65535) {
                JOptionPane.showMessageDialog(this, "Interval de porturi invalid (1-65535).", "Eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Resetăm UI-ul
            tableModel.setRowCount(0);
            allResults.clear();
            progressBar.setMinimum(0);
            progressBar.setMaximum(endPort - startPort + 1);
            progressBar.setValue(0);
            scanButton.setText("Stop Scanare");
            exportButton.setEnabled(false);

            // Pornim worker-ul care execută multithreading-ul în fundal
            currentWorker = new ScanWorker(ip, startPort, endPort, threads, timeout);
            currentWorker.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Te rog introdu valori numerice valide pentru porturi, thread-uri și timeout.", "Eroare Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Lansează un dialog pentru a salva fișierul.
     */
    private void exportResults() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Exportă rezultate");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fișiere CSV (*.csv)", "csv"));
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Fișiere Text (*.txt)", "txt"));
        fileChooser.setAcceptAllFileFilterUsed(true);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();

            try {
                if (filePath.toLowerCase().endsWith(".csv")) {
                    ResultExporter.exportToCSV(allResults, fileToSave);
                } else {
                    // Dacă nu e specificat csv, salvăm default ca txt (sau dacă user-ul alege txt)
                    if (!filePath.toLowerCase().endsWith(".txt")) {
                        fileToSave = new File(filePath + ".txt");
                    }
                    ResultExporter.exportToTXT(allResults, fileToSave);
                }
                JOptionPane.showMessageDialog(this, "Rezultatele au fost exportate cu succes!", "Export Reușit", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Eroare la export: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * SwingWorker procesează logica de rețea (PortScanner) în fundal.
     * Nu blochează Event Dispatch Thread (EDT), menținând interfața responsivă.
     */
    private class ScanWorker extends SwingWorker<Void, ScanResult> {
        private String ip;
        private int startPort, endPort, threads, timeout;
        private int scannedCount = 0;

        public ScanWorker(String ip, int startPort, int endPort, int threads, int timeout) {
            this.ip = ip;
            this.startPort = startPort;
            this.endPort = endPort;
            this.threads = threads;
            this.timeout = timeout;
        }

        @Override
        protected Void doInBackground() throws Exception {
            PortScanner scanner = new PortScanner();
            
            // Apelăm metoda care blochează DOAR acest thread de fundal, nu tot GUI-ul
            scanner.scanPorts(ip, startPort, endPort, threads, timeout, result -> {
                if (isCancelled()) return;
                
                // publish() trimite rezultatul sigur către EDT (metoda process)
                publish(result); 
            });
            return null;
        }

        @Override
        protected void process(List<ScanResult> chunks) {
            // Această metodă rulează pe Event Dispatch Thread (EDT), este safe să actualizăm UI-ul
            for (ScanResult res : chunks) {
                scannedCount++;
                progressBar.setValue(scannedCount);
                
                allResults.add(res);

                // Dacă portul e deschis, îl adăugăm în tabel
                if (res.isOpen()) {
                    tableModel.addRow(new Object[]{
                            res.getPort(),
                            "Deschis",
                            res.getServiceBanner()
                    });
                }
            }
        }

        @Override
        protected void done() {
            scanButton.setText("Start Scanare");
            exportButton.setEnabled(true);
            
            if (isCancelled()) {
                JOptionPane.showMessageDialog(MainFrame.this, "Scanarea a fost oprită manual.", "Info", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(MainFrame.this, "Scanare completă!", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
}
