# PIET College Canteen Billing System

A desktop-based canteen management and billing application built with **Java Swing, JDBC and MySQL**. The application provides role-based access for administrators and staff, food-menu and stock management, order billing, receipts, reports and audit history.

> **Project:** PIET College Canteen Billing System  
> **Application type:** Desktop application  
> **IDE project:** Apache NetBeans Ant project  
> **Main class:** `canteen.Main`  
> **Java source/target:** 17

## Features

- PIET-branded login screen and dashboard
- Role-based access for ADMIN and STAFF
- Food menu management with food images
- Billing with percentage discount and GST calculation
- Cash, UPI and Card payment methods
- Order history and printable receipts
- Inventory/stock management and low-stock notifications
- Stock movement history
- Sales reports and charts
- User management and password-change flow
- Activity/audit logs
- Database backup and restore utilities
- CSV export and application help/about screen
- Password hashing using PBKDF2-HMAC-SHA256

**Refund note:** The application supports full-order refunds. Partial refunds are not implemented. A refunded order remains in order history and is excluded from net-sales reports.

## Technology Stack

| Layer | Technology |
|---|---|
| Programming language | Java 17+ |
| Desktop UI | Java Swing |
| Database connectivity | JDBC |
| Database | MySQL |
| IDE/build system | Apache NetBeans / Ant |
| Database driver | MySQL Connector/J |

## Project Structure

```text
PIET_Canteen_Billing_System1/
├── database/
│   └── 01_migration.sql
├── docs/
│   ├── approved-ui-reference/
│   ├── APPROVED_UI_ASSETS.md
│   └── MANUAL_TEST_CHECKLIST.md
├── food_images/
├── libs/
│   └── README.txt
├── nbproject/
│   ├── project.properties
│   └── project.xml
├── src/
│   └── canteen/
│       ├── Main.java
│       ├── DBConnection.java
│       ├── LoginFrame.java
│       ├── DashboardFrame.java
│       └── ...other application classes
├── build.xml
├── manifest.mf
└── README.md
```

## Requirements

Install the following before running the project:

1. **JDK 17 or newer**
2. **Apache NetBeans IDE** with Java/Ant project support
3. **MySQL Server** (running locally or reachable over your network)
4. **MySQL Connector/J** JDBC driver
5. MySQL command-line client tools (`mysqldump` and `mysql`) if you want to use the in-app backup/restore feature

## Setup and Run

### 1. Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
```

Or download the repository as a ZIP and extract it.

### 2. Create/configure the MySQL database

Start MySQL Server. Open MySQL Workbench and execute:

```text
database/01_migration.sql
```

Alternatively, from the project directory run:

```bash
mysql -u root -p < database/01_migration.sql
```

The migration creates `piet_canteen_db` and the required tables/columns. **Back up an existing database before running migrations.** The migration is designed to preserve existing records; do not manually drop the database or tables.

### 3. Add MySQL Connector/J

Download the official MySQL Connector/J driver:

https://dev.mysql.com/downloads/connector/j/

Copy the downloaded `.jar` file into the project's `libs` directory and rename it to:

```text
mysql-connector-j.jar
```

In NetBeans, confirm that the JAR is listed under the project's **Libraries** node. The project configuration expects `libs/mysql-connector-j.jar`.

### 4. Configure database credentials

The application reads these environment variables:

- `PIET_DB_URL` — optional; default is `jdbc:mysql://localhost:3306/piet_canteen_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true`
- `PIET_DB_USER` — optional; default is `root`
- `PIET_DB_PASSWORD` — your MySQL password

**Windows PowerShell example** (run in the same PowerShell session from which you launch NetBeans):

```powershell
$env:PIET_DB_URL = "jdbc:mysql://localhost:3306/piet_canteen_db?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true"
$env:PIET_DB_USER = "root"
$env:PIET_DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
```

Do not put your real database password in Java source code, README files, screenshots or Git commits. The current source has a fallback password for local development; set `PIET_DB_PASSWORD` explicitly on your machine and consider changing the fallback before publishing a public repository.

### 5. Open and run in NetBeans

1. Open Apache NetBeans.
2. Select **File → Open Project**.
3. Browse to the `PIET_Canteen_Billing_System1` folder (the folder containing `build.xml`).
4. Wait for NetBeans to load the Ant project.
5. Confirm the MySQL Connector/J JAR is under **Libraries**.
6. Right-click the project and select **Clean and Build**.
7. Right-click the project and select **Run**.

The configured entry point is `canteen.Main`.

## First Administrator Setup

For a **fresh database with no users**, configure a private one-time setup token before starting the application.

PowerShell:

```powershell
$env:PIET_ADMIN_SETUP_TOKEN = "YOUR_PRIVATE_TOKEN_AT_LEAST_16_CHARACTERS"
```

Then:

1. Run the application.
2. Choose **FIRST-TIME ADMIN SETUP** on the login screen.
3. Enter the exact setup token.
4. Choose an administrator username and a strong password (at least 10 characters).
5. After the first administrator is created, the first-time setup route is disabled.

Keep the setup token private and remove it from your environment after setup. The first administrator can create staff accounts. Staff users with temporary passwords must change their password on first login.

For a database that already contains users, do not use first-admin setup. Log in with an existing account. Legacy passwords are upgraded to password hashes after a successful login, according to the application logic.

## Database and Data Safety

- Back up your database before applying schema changes.
- Use a separate test database when testing cancellation, refunds, restore and other data-changing operations.
- Do not commit real database credentials, production database dumps, personal data or backup files.
- Keep the `food_images` directory because the application uses these image assets.
- The migration script is in `database/01_migration.sql`.

## Manual Testing

A manual verification checklist is available at:

`docs/MANUAL_TEST_CHECKLIST.md`

It covers login and roles, menu operations, billing calculations, stock updates, refunds, reports, exports and backup/restore.

The project source has been compile-checked with Java 21 using Java 17 source compatibility. A live MySQL server was not available during that check, so database-dependent operations must be tested on your own MySQL installation before treating the application as fully verified.

## Troubleshooting

| Problem | What to check |
|---|---|
| `No suitable driver` | Confirm `libs/mysql-connector-j.jar` exists and is added to NetBeans Libraries. |
| `Access denied for user` | Check MySQL username/password and database permissions. |
| `Unknown database` | Start MySQL and execute `database/01_migration.sql`. |
| `Unknown column` or missing table | Back up the database, then run the migration script. |
| Cannot connect to MySQL | Check the MySQL service, host, port `3306`, database name and environment variables. |
| Backup/restore command not found | Install MySQL client tools and add their `bin` directory to Windows PATH. |
| Project does not build | Confirm JDK 17+, Connector/J library path and that NetBeans opened the folder containing `build.xml`. |

## Security

- Never commit passwords, setup tokens, database dumps or private user data.
- Use a strong administrator password.
- Restrict access to the MySQL server.
- Test backup restoration using a disposable database.
- Review configuration before deploying this application outside a local academic/demo environment.

## Author

**Kevaram**

Developed as a college project for managing canteen billing, menu items, stock and sales operations.

## License

No license has been specified yet. Unless a license is added, standard copyright applies and others do not automatically receive permission to reuse or redistribute this project.
