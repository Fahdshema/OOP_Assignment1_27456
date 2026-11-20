TAMS - Tax Administration Management System (teaching simulation, fictional rates)
Folder: AUCA_ID   | Java 8+ | console | arrays only | one shared Scanner

COMPILE (from inside AUCA_ID):
    mkdir out
    javac -d out $(find src -name "*.java")
    (Windows: dir /s /b src\*.java > files.txt   then   javac -d out @files.txt)
RUN:
    java -cp out com.auca.main.Main

PACKAGES
    com.auca.interfaces      Identifiable, Validatable
    com.auca.abstracts       TaxParty, RegisteredTaxpayer
    com.auca.<classname>     one concrete class per package (11 concrete classes)
    com.auca.exceptions      9 custom exceptions (all extend ValidationException)
    com.auca.main            Main (menu, 4-step form engine, business rules)

FORM KEYS:  NEXT / BACK / CANCEL, or type a field number to edit that field.
All rates, deadlines and amounts are illustrative - not official tax law.
