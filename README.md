# LibraryMongoDB

> Built together with Ahmed as a pair project at KTH.

Ett bibliotekshanteringssystem byggt med JavaFX och MongoDB. Systemet tillåter användare att hantera böcker, recensioner och betyg i en grafisk användargränssnitt.

## Funktioner

### Bokhantering
- **Lägg till böcker**: Lägg till nya böcker med ISBN, titel, publiceringsdatum, författare och genrer
- **Ta bort böcker**: Ta bort böcker från biblioteket
- **Sökning**: Sök efter böcker baserat på:
  - Titel
  - ISBN
  - Författare
  - Genre
  - Betyg (rating)

### Recensioner och Betyg
- **Textrecensioner**: Skriv textrecensioner för böcker
- **Betyg**: Ge betyg (1-10) till böcker
- **Max ett betyg per bok**: Varje inloggad användare kan ge max ett betyg per bok
- **Genomsnittsbetyg**: Systemet beräknar automatiskt genomsnittsbetyg baserat på användarnas betyg
- **Flera textrecensioner**: Användare kan skriva flera textrecensioner för samma bok

### Användarhantering
- **Inloggning**: Logga in med ditt användarnamn
- **Registrering**: Skapa nya användarkonton
- **Användarspecifik data**: Dina recensioner och betyg är kopplade till ditt konto

## Teknologier

- **Java 21**: Programmeringsspråk
- **JavaFX 21.0.6**: Grafiskt användargränssnitt
- **MongoDB Driver 4.11.1**: Databasanslutning
- **Maven**: Byggverktyg och beroendehantering

## 📁 Projektstruktur

```
LibraryMongoDB/
├── src/main/java/se/kth/awad/librarymongodb/
│   ├── App.java                          # Huvudklass som startar applikationen
│   ├── Controller/
│   │   └── BookDBController.java         # Controller i MVC-mönstret
│   ├── model/
│   │   ├── Book.java                     # Bokmodell
│   │   ├── Author.java                   # Författarmodell
│   │   ├── Genre.java                    # Genremodell
│   │   ├── Review.java                   # Recensionsmodell
│   │   ├── User.java                     # Användarmodell
│   │   ├── BooksDbInterface.java         # Gränssnitt för databasoperationer
│   │   ├── BooksDbMongo.java             # MongoDB-implementation
│   │   └── BooksDbException.java          # Anpassat undantag
│   └── view/
│       ├── BooksPane.java                # Huvudvy med boklista och sökning
│       ├── AddBookDialog.java            # Dialog för att lägga till böcker
│       ├── RemoveBookDialog.java         # Dialog för att ta bort böcker
│       ├── ReviewDialog.java             # Dialog för textrecensioner
│       ├── UpdateGradeDialog.java        # Dialog för att uppdatera betyg
│       ├── LoginDialog.java              # Dialog för inloggning/registrering
│       └── GradeUpdate.java              # Hjälpklass för betygsdata
├── Database/                              # Exempeldata för MongoDB
│   ├── library.BOOKS.json
│   ├── library.REVIEWS.json
│   ├── library.USERS.json
│   └── library.COUNTERS.json
└── pom.xml                                # Maven-konfiguration
```

## Användning

### Starta applikationen

1. Starta applikationen med `mvn javafx:run`
2. Klicka på **"Connect"** i menyn för att ansluta till databasen
3. Logga in eller registrera ett nytt konto

### Grundläggande operationer

#### Ansluta till databasen
- Gå till **Database → Connect** i menyn
- Systemet ansluter till MongoDB-databasen "LibraryDB"

#### Söka efter böcker
1. Välj söktyp från dropdown-menyn (Title, ISBN, Author, Genre, Rating)
2. Ange sökterm i sökfältet
3. Klicka på **"Search"** eller tryck Enter

#### Lägga till en bok
1. Logga in först
2. Gå till **Book → Add Book**
3. Fyll i:
   - ISBN
   - Titel
   - Publiceringsdatum
   - Författare (lägg till flera om nödvändigt)
   - Genrer (lägg till flera om nödvändigt)
4. Klicka på **"Add"**

#### Ta bort en bok
1. Logga in först
2. Gå till **Book → Remove Book**
3. Ange exakt titel på boken
4. Klicka på **"Remove"**

#### Ge betyg till en bok
1. Logga in först
2. Gå till **Book → Update Grade**
3. Ange boktitel och välj betyg (1-10)
4. Klicka på **"Update"**
   - **Obs**: Du kan bara ha ett betyg per bok. Om du uppdaterar betyget ersätts det gamla.

#### Skriva en textrecension
1. Logga in först
2. Gå till **Book → Add Review**
3. Ange boktitel och skriv din recension
4. Klicka på **"Add Review"**
   - **Obs**: Du kan skriva flera textrecensioner för samma bok

#### Logga ut
- Gå till **User → Logout**


### Indexering

Systemet använder följande index för optimerad sökning:
- **Text index** på `title` för snabb titelsökning
- **Ascending index** på `isbn` för ISBN-sökning
- **Unique index** på `isbn` för att säkerställa unikhet

## Arkitektur

Projektet följer **MVC (Model-View-Controller)** arkitekturmönstret:

- **Model** (`model/`): Hanterar datalogik och databasoperationer
  - `BooksDbMongo`: Implementerar databasoperationer mot MongoDB
  - `Book`, `Author`, `Genre`, `Review`, `User`: Datamodeller

- **View** (`view/`): Hanterar användargränssnittet
  - `BooksPane`: Huvudvy med boklista
  - Dialoger för olika operationer (Add, Remove, Review, etc.)

- **Controller** (`Controller/`): Koordinerar mellan Model och View
  - `BookDBController`: Hanterar användarinteraktioner och uppdaterar både Model och View

### Designprinciper

- **Separation of Concerns**: Tydlig separation mellan data, logik och presentation
- **OOP-principer**: Användning av klasser, inkapsling och polymorfism
- **Asynkron hantering**: Användning av JavaFX `Task` för att undvika att frysa UI vid databasoperationer
- **Felhantering**: Anpassade undantag (`BooksDbException`) för tydlig felhantering

## Noteringar

- **Betyg vs Recensioner**: Ett betyg (rating) är numeriskt (1-10) och varje användare kan bara ha ett betyg per bok. Textrecensioner kan däremot vara flera per användare och bok.
- **Genomsnittsbetyg**: Beräknas automatiskt baserat på alla betyg > 0. Recensioner med bara text (rating = 0) räknas inte med.
- **Användarautentisering**: För närvarande enkel användarnamnsbaserad autentisering. Lösenord hanteras men kryptering kan förbättras för produktionsanvändning.

## Författare

Projektet är utvecklat som en del av kursen Databasteknik vid KTH.

## Deltagare
Ibrahim Awad & Ahmed El Yasini

## Licens

Detta projekt är utvecklat för utbildningssyfte.

