# LibraryMongoDB

A JavaFX desktop client for a MongoDB book library — the same MVC structure as
the MySQL variant, rebuilt on the document model.

Built with Ahmed El Yasini as a pair project for Databasteknik at KTH.

## What works

- **Search** by title, ISBN, author, genre, or rating. Author and genre use
  case-insensitive substring matching; ISBN is exact. Title uses a MongoDB text
  index, which matches whole words rather than substrings — searching `Lor`
  will not find *The Lord of the Rings*.
- **Add and remove books**, with multiple authors and genres per book.
- **Rate a book** from 1 to 10, or write a text review.
- **Average rating** recalculated automatically across all ratings above zero,
  so text-only reviews do not drag the average down.
- **Accounts** — register and log in; book management is disabled until you do.

Six indexes are created on the `BOOKS` collection at connect time: a text index
on `title`, ascending indexes on `ISBN`, `authors.name` and `genres.name`, a
descending index on `average_rating`, and a unique index on `book_id`.

Most database work runs on JavaFX `Task` threads. The initial connect and both
login and registration do not — they run on the UI thread and will block the
window while they wait.

## Requirements

- **JDK 21** — the POM targets 21.
- **MongoDB** running on `localhost:27017`.
- Maven is not required; the repository ships the Maven wrapper.

## Database setup

The application connects as a MongoDB user named **`appUser`**, authenticating
against the `library` database itself rather than `admin`. That user must exist
before the app can connect. The connection string, including the password, is a
compile-time constant in
`src/main/java/se/kth/awad/librarymongodb/model/BooksDbMongo.java`.

Sample data lives in `Database/`. The files are JSON arrays, so `mongoimport`
needs `--jsonArray`, and **the collection names must be uppercase** — the code
looks for `BOOKS`, `REVIEWS`, `USERS` and `COUNTERS`, and a lowercase
collection silently returns nothing.

```bash
mongoimport --db library --collection BOOKS    --jsonArray --file Database/library.BOOKS.json
mongoimport --db library --collection REVIEWS  --jsonArray --file Database/library.REVIEWS.json
mongoimport --db library --collection USERS    --jsonArray --file Database/library.USERS.json
mongoimport --db library --collection COUNTERS --jsonArray --file Database/library.COUNTERS.json
```

The database is named **`library`**, lowercase. The UI reports connecting to
"LibraryDB", but that string is passed to a method that discards it — `library`
is the database actually used.

`MongoDB Setup/` contains screenshots of the Compass account setup. There is no
written guide.

## Building and running

```bash
./mvnw javafx:run      # Linux / macOS
mvnw.cmd javafx:run    # Windows
```

Nothing connects at startup. Use **File → Connect**, then log in.

## Known gaps

- **A user gets one review *or* one rating per book, ever.** Ratings and text
  reviews are the same document and share a single uniqueness guard, so writing
  a review permanently blocks you from rating that book and vice versa. Trying
  either a second time fails with "You have already added a review for this
  book." There is no update path in the model — a rating can never be changed.
- **An account with no stored password can be logged into with any password.**
  If a user document has a missing, null, or empty `password` field, the login
  dialog accepts whatever you type. Passwords are also stored and compared in
  plaintext, and the MongoDB password is committed to the repository.
- **Imported sample users show as "Unknown".** The sample data writes `userID`
  while the lookup queries `user_ID`, so the "added by" line in book details
  cannot resolve any imported user. Accounts created inside the running app
  are unaffected.

## License

[MIT](LICENSE)
