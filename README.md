# Tucil3_13523149_13523160

## Dependencies

### Maven

`sudo apt install maven`

### Java 21

## Compilation & Run

### Clean & Kompilasi

`mvn clean compile`

## Jalankan program

### Mode CLI

`mvn exec:java`

### Mode GUI

1. Build JAR package (untuk sekarang JAR dipake khusus GUI dulu):
`mvn clean package -P gui`

2. Pindahkan file JAR ke Windows (kalo dari WSL):
`cp bin/rush-hour-solver-1.0-SNAPSHOT.jar /mnt/c/Users/YourUsername/Desktop/`

3. Di Windows, jalankan JAR:
`java -jar rush-hour-solver-1.0-SNAPSHOT.jar`

## JAR

### Kompilasi dan build JAR ke bin/

`mvn clean package`

### Jalankan JAR

`java -jar bin/rush-hour-solver-1.0-SNAPSHOT.jar`
