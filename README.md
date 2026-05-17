# Java Multithreaded Port Scanner

Acest proiect reprezintă o aplicație desktop completă de **Port Scanner**, dezvoltată în limbajul Java pentru stagiul de practică universitar. Aplicația permite scanarea eficientă a porturilor TCP pentru un anumit Host sau adresă IP, oferind feedback vizual în timp real și posibilitatea de a exporta rezultatele auditului.

## 🚀 Caracteristici Principale

* **Performanță Ridicată (Multithreading):** Logica de scanare utilizează un pool de fire de execuție (`ExecutorService`), permițând verificarea simultană a zecilor de porturi și reducând masiv timpul total de execuție.
* **Interfață Grafică Responsivă (GUI Swing):** Designul este realizat integral în Swing. Pentru a preveni blocarea ferestrei pe durata scanărilor intense, logica de rețea este decuplată de firul principal de execuție grafică (EDT) prin intermediul tehnologiei `SwingWorker`.
* **Arhitectură Curată (Separation of Concerns):** Proiectul respectă principiile programării orientate pe obiecte (POO), având o separare clară între logica de business (rețelistică/socket-uri), interfața vizuală și utilitarele sistemului.
* **Funcționalitate de Export:** Aplicația oferă posibilitatea salvării live a rezultatelor scanării într-un fișier local (format CSV/TXT) pentru un audit de securitate ulterior.

## 🛠️ Tehnologii și Concepte Utilizate

* **Limbaj:** Java 17
* **Gestiune Proiect:** Structură modulară bazată pe standardul Maven (`pom.xml`)
* **Networking:** `java.net.Socket` (conexiuni bazate pe protocolul TCP/IP cu Timeout personalizabil)
* **Conconcurență:** `java.util.concurrent.ExecutorService`, `ThreadPoolExecutor`
* **GUI:** Java Swing (`JFrame`, `JTable`, `JProgressBar`, `SwingWorker`)

## 📂 Structura Proiectului

* `src/main/java/com/scanner/Main.java` - Punctul de intrare (Entry Point) în aplicație.
* `src/main/java/com/scanner/core/` - Conține motorul de scanare (`PortScanner`) și modelul de date pentru rezultate (`ScanResult`).
* `src/main/java/com/scanner/gui/` - Conține clasa responsabilă de interfața grafică (`MainFrame`).
* `src/main/java/com/scanner/util/` - Conține logica de scriere și export pe disc a fișierelor de raport (`ResultExporter`).

## ⚙️ Cum se rulează proiectul

### Din Terminal
Pentru a compila și rula proiectul fără un IDE, navighează în directorul rădăcină și execută:
```bash
javac -d out src/main/java/com/scanner/Main.java src/main/java/com/scanner/core/*.java src/main/java/com/scanner/gui/*.java src/main/java/com/scanner/util/*.java
java -cp out com.scanner.Main
