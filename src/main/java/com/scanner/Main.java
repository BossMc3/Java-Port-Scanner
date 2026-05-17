package com.scanner;

import com.scanner.gui.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punctul de intrare (Entry Point) al aplicației.
 */
public class Main {
    public static void main(String[] args) {
        // Setăm Look and Feel pentru ca interfața să arate ca o aplicație nativă de Windows/Mac/Linux
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // În caz de eroare se va folosi Look and Feel-ul implicit Java (Metal)
            System.err.println("Nu s-a putut seta System LookAndFeel.");
        }

        // Toate componentele Swing trebuie inițializate și modificate exclusiv pe Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
