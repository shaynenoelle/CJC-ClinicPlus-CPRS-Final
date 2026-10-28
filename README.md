# CJC ClinicPlus CPRS

CJC ClinicPlus CPRS is a Java desktop application for managing school-clinic operations. It provides a JavaFX interface for authentication, patient records, clinic visits, medicine inventory, reporting, and related clinic workflows.

## Repository contents

- `src/main/java/` — Java application source code.
- `src/main/resources/` — JavaFX FXML views, CSS stylesheets, images, and other application resources.
- `cjclinic.db` — SQLite database used by the application.
- `audit.log` — Login/access audit entries written by the application.
- `pom.xml` — Maven project configuration, dependencies, compiler settings, and packaging plugins.
- `.idea/` — IntelliJ IDEA project metadata.

## Database

The application uses a local SQLite database named `cjclinic.db`. No separate database server is required.

The database connection is configured in `DatabaseManager` as:

```text
jdbc:sqlite:cjclinic.db
```

Because this is a relative path, the application should normally be started from the repository/project root so that it opens the included database file. If the file is missing, SQLite may create a new empty database in the current working directory, which can cause missing-table or login errors.

The database stores application data such as:

- User accounts and password hashes used by the login screen.
- Patient records and demographic/medical information.
- Clinic visits, check-ins, attendance, and visit history.
- Medicine inventory.
- Configurable presets and application settings.

On startup and during database operations, the application can add required columns through `DatabaseManager.ensureColumns()`. Keep a backup of `cjclinic.db` before making schema or data changes.

### Opening the database manually

You can inspect the database with any SQLite-compatible tool, such as **DB Browser for SQLite**, the `sqlite3` command-line client, or an IDE database viewer.

Using the `sqlite3` command-line client:

```bash
sqlite3 cjclinic.db
```

Useful commands:

```sql
.tables
.schema
SELECT name FROM sqlite_master WHERE type = 'table';
.quit
```

When using a GUI database tool, open the repository's `cjclinic.db` file directly. Do not create a replacement database unless you intentionally want a clean/test database.

## Source code overview

The main Java package is:

```text
com.example.cjcschoolclinicmanagementsystem
```

Important source areas include:

- `Launcher.java` — starts the JavaFX application.
- `MainWrapper.java` — wrapper entry point used by the Maven Shade/Launch4j packaging configuration.
- `HelloApplication.java` — initializes the JavaFX stage and loads the login view.
- `controller/` — controllers for login, dashboard, patients, visits, logbook, medicine inventory, settings, and related screens.
- `database/DatabaseManager.java` — SQLite connection and database queries/updates.
- `model/` — application data models.
- `util/` — supporting utilities, including Excel import and user preferences.
- `src/main/resources/.../View/` — FXML user-interface definitions.
- `src/main/resources/.../Images/` — application images and icons.

The normal application flow is:

1. `MainWrapper` calls `Launcher`.
2. `Launcher` starts `HelloApplication` using JavaFX.
3. The login FXML view is displayed.
4. `LoginController` validates the supplied credentials through `DatabaseManager`.
5. After successful authentication, the dashboard and other FXML views are loaded.
6. Application data is read from and written to `cjclinic.db`.

## Required software and components

Install the following before building or running the application:

1. **Java Development Kit (JDK) 21**
   - The Maven configuration targets Java 21.
   - Use a full JDK, not only a Java Runtime Environment, when compiling.
2. **Apache Maven**
   - Maven downloads the JavaFX, SQLite, BCrypt, Apache POI, PDFBox, JUnit, and other dependencies defined in `pom.xml`.
3. **JavaFX 21 dependencies**
   - `javafx-controls`
   - `javafx-fxml`
   - `javafx-swing`
   - These are declared in `pom.xml` and normally do not need to be installed manually.
4. **SQLite JDBC driver**
   - Xerial SQLite JDBC `3.45.1.0`, managed by Maven.
5. **Optional IDE**
   - IntelliJ IDEA is recommended because the repository contains `.idea` metadata, but Eclipse or VS Code with Java/Maven support can also be used.
6. **Optional database tools**
   - DB Browser for SQLite or the `sqlite3` command-line client for inspecting `cjclinic.db`.

The project also uses these Maven-managed libraries:

- jBCrypt for password-hash verification.
- Apache POI and POI-OOXML for Excel import/report-related functionality.
- Apache PDFBox for PDF export.
- JUnit Jupiter API for tests.

## Open the project in IntelliJ IDEA

1. Clone the repository:

   ```bash
   git clone https://github.com/shaynenoelle/CJC-ClinicPlus-CPRS-Final.git
   cd CJC-ClinicPlus-CPRS-Final
   ```

2. Open IntelliJ IDEA.
3. Select **Open** and choose the cloned project directory.
4. Open/import the project as a **Maven** project using `pom.xml`.
5. Set the project SDK and Maven runner to **JDK 21**.
6. Allow Maven to download and index the dependencies.
7. Ensure the run configuration uses the project root as its working directory so the relative database path resolves to `cjclinic.db`.
8. Run either:
   - `com.example.cjcschoolclinicmanagementsystem.MainWrapper`, or
   - `com.example.cjcschoolclinicmanagementsystem.Launcher`.

`MainWrapper` is the preferred entry point because it is also configured as the packaged application's main class.

## Build and run with Maven

From the repository root:

```bash
mvn clean compile
```

To package the application:

```bash
mvn clean package
```

The Maven build is configured to create a shaded JAR with dependencies bundled. The packaged JAR is expected at:

```text
target/CJCClinicPlus.jar
```

Run the packaged JAR with:

```bash
java -jar target/CJCClinicPlus.jar
```

Run the application directly through Maven/IDE tooling if the JavaFX runtime configuration on your machine requires it. Maven supplies the JavaFX modules declared in `pom.xml`.

The build also includes a Launch4j plugin configuration for producing a Windows executable:

```text
target/CJCClinicPlus.exe
```

The executable packaging is intended for Windows deployment and requires the appropriate Windows-compatible Java/Launch4j build environment.

## Connecting to the database while running

The application connects automatically to `cjclinic.db` when database operations are performed. There is no host, port, database server, or separate connection string to enter in the application.

To ensure the correct database is used:

1. Place `cjclinic.db` in the project root.
2. Start the application with the project root as the working directory.
3. Confirm that the application process has read/write permission for the project directory.
4. Do not move or rename the database unless the JDBC URL in `DatabaseManager` is updated accordingly.

For a separate test database, copy the existing file before running:

```bash
cp cjclinic.db cjclinic-backup.db
cp cjclinic.db cjclinic-test.db
```

The current application configuration still expects the filename `cjclinic.db`, so rename the test copy to `cjclinic.db` only when intentionally testing against it, or update the JDBC URL in `DatabaseManager`.

## Login and access

The login screen checks the entered username and password against the `Users` table in the SQLite database. Passwords are verified against stored BCrypt hashes.

This repository does not document a universal default username or password. Use an account already present in the supplied database or ask the system administrator/project owner for valid credentials. Do not commit passwords or other secrets to the repository.

Successful login attempts are appended to `audit.log`.

## Troubleshooting

### `Unable to access jarfile` or missing dependencies

Run the build from the repository root and allow Maven to finish downloading dependencies:

```bash
mvn clean package
```

### Java version errors

Check the active Java version:

```bash
java -version
javac -version
```

Both should report JDK 21-compatible installations. Also check the JDK configured in IntelliJ IDEA and Maven.

### Login always fails

- Confirm that the application opened the intended `cjclinic.db`.
- Check that the database contains a `Users` table and account.
- Make sure the working directory is the project root.
- Inspect the console for SQLite or database errors.

### Missing tables or views

A new empty SQLite file may have been created because the application was started from the wrong working directory. Stop the application, remove or rename the unintended empty database, and restart from the repository root with the supplied `cjclinic.db`.

### FXML or image resource errors

Run the application from the Maven project or packaged JAR rather than executing an individual Java source file. Confirm that Maven resources were copied during the build:

```bash
mvn clean process-resources
```

## Data and security notes

- `cjclinic.db` contains application data and should be treated as sensitive.
- `audit.log` may contain usernames and access timestamps.
- Make regular backups before testing imports, edits, or schema changes.
- The application's “Remember Me” preference uses Java Preferences and basic Base64 encoding; Base64 is not encryption. Avoid enabling credential persistence on shared or untrusted computers.

## License

No license file is currently included in this repository. Add a license before redistributing the application if required by the project owner or institution.
