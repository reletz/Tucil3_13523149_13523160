# Tucil3_13523149_13523160

Aplikasi pencari solusi untuk permainan puzzle Rush Hour dengan implementasi beberapa algoritma pencarian.

## Features

- Graphical User Interface
- Implementasi beberapa algoritma pencarian:
  - Greedy Best-First Search
  - A\* Search
  - Uniform Cost Search (UCS)
  - Branch and Bound Search
- Berbagai fungsi heuristik untuk algoritma pencarian terinformasi:
  - Manhattan Distance
  - Blocking Pieces
  - Distance to Exit
  - Piece Density
  - Combined
- Pemutaran solusi langkah demi langkah
- Statistik pencarian solusi
  - Waktu eksekusi
  - Jumlah simpul yang dikunjungi
  - Jumlah gerakan menuju solusi
- Unggah file .txt untuk puzzle kustom
- Ekspor solusi ke file teks
- Memainkan puzzle yang diunggah

## Dependencies

### Maven

Pada Linux:

```bash
sudo apt install maven
```

### Java 21

JDK 21 atau lebih baru diperlukan untuk menjalankan aplikasi ini.

## Compilation & Run

### Clean & Kompilasi

```bash
mvn clean compile
```

### Jalankan program

1. Build JAR package:

```bash
mvn clean package -P gui
```

2. Jalankan JAR pada directory .jar berada:

```bash
java -jar rush-hour-solver-1.0-SNAPSHOT.jar
```

## Panduan Penggunaan

### Memuat File Konfigurasi

1. Klik tombol "Browse..." untuk memilih file konfigurasi puzzle
2. Pilih file konfigurasi puzzle yang valid
3. Puzzle akan ditampilkan di papan

### Memilih Algoritma Pencarian

1. Pilih algoritma pencarian dari dropdown: UCS, GBFS, A\*, atau Branch and Bound
2. Untuk algoritma selain UCS, pilih juga heuristik yang akan digunakan

### Menjalankan Pencarian

1. Klik tombol "Solve Puzzle" untuk mencari solusi
2. Statistik pencarian akan ditampilkan (waktu eksekusi, jumlah node, dll.)
3. Langkah-langkah solusi akan divisualisasikan pada panel utama

### Navigasi Solusi

1. Gunakan tombol navigasi untuk melihat langkah-langkah solusi
2. Gunakan tombol "Play" untuk menjalankan animasi otomatis
3. Atur kecepatan animasi dengan slider

### Menyimpan Solusi

1. Klik tombol "Save to TXT" untuk menyimpan solusi lengkap ke file teks

### Memainkan Puzzle

1. Klik tombol "Play This Puzzle" untuk membuka jendela permainan interaktif

## Contributor

| Name                     | ID       |
| ------------------------ | -------- |
| Naufarrel Zhafif Abhista | 13523149 |
| I Made Wiweka Putera     | 13523160 |
