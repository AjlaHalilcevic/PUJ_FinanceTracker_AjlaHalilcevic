FinanceTracker - PUJ Projekat 1 (2025/2026)

1. Kratak opis projekta

Ovo je desktop aplikacija za praćenje ličnih finansija, rađena za predmet "Programiranje u Javi".

Aplikacija omogućava sledeće:
- Unos prihoda i rashoda;
- Čuvanje podataka u MongoDB bazi;
- Pregled svih transakcija u tabeli;
- Ažuriranje postojeće transakcije;
- Brisanje transakcije;
- Kategorizaciju transakcija (plata, hrana, racuni, zabava, prijevoz, ostalo);
- Export izvještaja u tekstualni fajl (ukupni iznos plus kategorije).

GUI je napravljen u Java Swingu pomoću IntelliJ GUI Designera, a za pristup bazi koristi se MongoDB Java driver ("mongodb-driver-sync").

2. Korištene tehnologije
  - Java 17;
  - Java Swing (Desktop GUI);
  - IntelliJ IDEA Community Edition 2025.2.5.;
  - MongoDB Community Server (lokalna baza);
  - MongoDB Java driver: "org.mongodb:mongodb-driver-sync:4.11.0.".

3. Kako pokrenuti projekat 

3.1. Priprema MongoDB baze
1. Instalacija MongoDB Community Server;
2. Pokretanje MongoDB servera lokalno (podrazumijevana adresa: "mongodb://localhost:27017");
3. Aplikacija sama koristi bazu i kolekciju:
- Naziv baze: "financeTrackerDB"
- Naziv kolekcije: "transactions"

Dovoljno je da radi MongoDB server, nije potrebno praviti ručno kolekciju.

3.2. Import projekta u IntelliJ IDEA Community Edition 2025.2.5
1. U IntelliJ-u: File -> Open... i izabrati folder projekta.
2. Kada se projekat otvori, po potrebi uključiti Git (nije obavezno za pokretanje, ali jeste za projekat);
3. Provjeriti da je dodat MongoDB driver: File -> Project Structure -> Libraries -> +
"From Maven..." upisati: "org.mongodb:mongodb-driver-sync:4.11.0." dodati biblioteku na modul (Apply/OK).

3.3. Pokretanje aplikacije
1. U paketu "financeapp" otvoriti klasu "Main";
2. Desni klik u editoru "Run "Main.main()"".

Ako je MongoDB pokrenut i biblioteke su dodane, otvori se glavni prozor aplikacije za praćenje finansija.

4. Funkcionalnosti aplikacije

4.1. Dodavanje transakcije

U gornjem dijelu forme nalaze se polja:
 - "Vrsta transakcije" - JComboBox: - Prihod;  -Rashod;
 - "Unos iznosa vašeg prihoda" - JTextField za unos double vrijednosti;
 - "Opišite ovaj izvor prihoda" - tekstualni opis transakcije;
 - "Kategorija" - JComboBox: -Plata; -Hrana; -Racuni; -Zabava, -Prijevoz, -Ostalo;
 - Dugme "Izračunaj": - provjerava da li je iznos validan broj; - kreira objekat "Transaction"; - poziva "TransactionManager.addTransaction(...)"; - upisuje transakciju u MongoDB kolekciju "transactions"; - ponovo učitava podatke u tabelu; - osvježava prikaz Prihoda, Rashoda i Stanja; - briše uneseni iznos i opis iz polja.
Ako iznos nije broj, prikazuje se poruka: "Iznos mora biti broj!"

4.2. Pregled transakcija (Tabela)

Pri dnu se nalazi "JTable" sa svim transakcijama. Kolone:
- Vrsta (Prihod / Rashod);
- Iznos;
- Opis;
- Kategorija.

Podaci se učitavaju u metodi "loadDataIntoTable()", koja preko "TransactionManager.getAllTransactions()" čita dokumente iz MongoDB baze i puni "DefaultTableModel".

4.3. Ažuriranje postojeće transakcije

Postupak:
1. Korisnik klikne na red u tabeli, zatim se podaci iz tog reda automatski učitaju u polja: - Vrsta; - Iznos; - Opis; - Kategorija.
2. Korisnik izmijeni vrijednost, te klikne dugme "Ažuriraj";
3. Aplikacija ažurira odgovarajuće dokumente u MongoDB bazi (preko njegovog "_id").

Tehnički detalji: - Klasa "Transaction" čuva polje "ObjectId id" koje odgovara MongoDB "_id" polju. "TransactionManager.getAllTransactions()" pri učitavanju mapira "_id" i ostala polja:



new Transaction(
d.getObjectId("_id"),
d.getString("Vrsta"),
d.getDouble("Iznos"),
d.getString("Opis"),
d.getString("Kategorija")
);

Metode updateTransaction(ObjectID id,...) u TransactionManager koristi $set update da promijeni potrebna polja u dokumentu.

4.4. Brisanje transakcije

Postupak za dugme "Obriši":
1. Potrebno je prvo selektovati red u tabeli, zatim klikom na "Obriši" pojavljuje se dijalog za potvrdu (JOptionPane.showConfirmDialog);
2. Ako korisnik potvrdi brisanje, poziva se: "deleteTransaction(id);" u TransactionManager, gdje se radi "collection.deleteOne(new Document("_id", id));"
3. Nakon brisanja ponovo se učita lista transakcija i osvježavaju se sume.

4.5. Kategorije

Za svaku transakciju bira se jedna od kategorija: 
- Plata;
- Hrana;
- Racuni;
- Zabava;
- Prijevoz;
- Ostalo

Kategorije se koriste u tabeli (posebna kolona) i u izvještaju (sabiranje po kategorijama). Ako neka transakcija nema postavljenu kategoriju, tretira se kao "Ostalo".

4.6. Export izvještaja

Dugme "Export" kreira tekstualni fajl (npr. izvjestaj.txt) u root folderu projekta.

Izvještaj sadrži: 
- ukupni prihod;
- ukupni rashod;
- stanje (saldo = prihod - rashod);
- iznose po kategorijama (Plata, Hrana, Racuni, Zabava, Prijevoz, Ostalo).

Primjer sadržaja fajla:
- Ukupni prihod: 100.0

- Ukupni rashod: 35.0

- Stanje: 65.0

Suma po kategorijama:
- Plata: 100.0 
- Hrana: 10.0 
- Racuni: 25.0 
- Zabava: 0.0 
- Prijevoz: 0.0 
- Ostalo: 0.0

Ako nema transakcija, program javlja da nema šta da izveze.

5. Struktura projekta (glavne klase)

Sve klase se nalaze u paketu: "package financeapp"

5.1. MongoDBConnection

On se brine o konekciji na MongoDB, čuva URI i naziv baze, ima statičku metodu: "public static MongoDatabase getDatabase()" koja vraća instancu MongoDatabase. Konekcija se kreira samo jednom.

5.2. Transaction

Predstavlja jednu transakciju u aplikaciji i u bazi. 

Polja:
- ObjectId id;
- String type (Prihod / Rashod);
- double amount;
- String description;
- String category.

Metode:
- konstruktori (sa i bez id);
- Document toDocument() - pretvara objekt u MongoDB dokument;
- getteri (getId, getType, getAmount, getDescription, getCategory).

5.3. TransactionManager

Sloj između aplikacije i MongoDB baze.

Glavne metode:
- "addTransaction(Transaction t)" - insert u kolekciju;
- "getAllTransactions()" - vraća listu svih transakcija iz kolekcije;
- "getTotalIncome()" - suma svih transakcija gdje je type = "Prihod";
- "getTotalExpense()" - suma svih transakcija gdje je type = "Rashod";
- "updateTransaction(ObjectId id,...)" - update dokumenta po _id;
- "deleteTransaction(ObjectId id)" - vrisanje dokumenta po _id.

5.4. FinanceTrackerForm

Swing forma (GUI) koju vidi korisnik.

Sadrži:
- polja/formu za unos;
- tabelu za prikaz transakcija;
- label-e za prikaz suma;
- dugmad: Izračunaj, Ažuriraj, Obriši, Export;
- logiku za: 
1. učitavanje podataka u tabelu (loadDataIntoTable);
2. osvježavanje suma (updateSummary);
3. reagovanje na klikove na dugmad;
4. učitavanje selektovanog reda u polja za uređivanje.


5. Main

Ulazna tačka programa. Kreira JFrame, postavlja FinanceTrackerForm.mainPanel kao sadržaj i prikazuje prozor.

6. Autor

- Ime i prezime: Ajla Halilčević
- IDE: IntelliJ IDEA Community Edition 2025.2.5
- Fakultet/smijer/godina: Internacionalna poslovno-informaciona akademija Tuzla / Informatika i računarstvo / treća godina studija.