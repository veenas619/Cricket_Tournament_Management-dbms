# Cricket Tournament Management System

A Java desktop application for managing a cricket tournament, built with Java Swing and JDBC on a MySQL database.

## Features
- Login screen leading to a dashboard
- Add, edit, delete and view records (CRUD) for the tournament's tables
- Points table for a chosen tournament
- Record which team won a match
- Team player count and total coach salary
- MySQL error messages shown in pop-ups when a rule is broken (triggers, foreign keys, CHECK constraints)

## Database design
The database `CricketTournamentManagementSystem` has these tables:

| Table | Purpose |
|---|---|
| `Team` | Teams, with a captain who is one of its players |
| `Player` | Players, each belonging to one team |
| `Player_Achievements` | Achievements of a player (multivalued attribute) |
| `Coach` | Coaches, each coaching one team |
| `Tournament` | Tournaments with dates and prize pool |
| `Umpire` | Umpires |
| `Stadium` | Stadiums with location and capacity |
| `Match` | Matches, linked to a tournament, umpire, stadium and winning team |
| `Match_Team` | The teams playing each match (many-to-many) |
| `Statistics` | Per-format player statistics (weak entity owned by `Player`) |
| `Sponsors` | Sponsors |
| `Tour_Sponsor` | Sponsors of a tournament with the amount (many-to-many) |

It also includes a view (`vw_player_team`), an index on player name, and sample data.

### Logic in the database
- **Trigger `T1`**: the winner of a match must be one of the two teams playing it.
- **Trigger `T2`**: a team's captain must be a player of that same team.
- **Function `PlayerCount(team_id)`**: number of players in a team.
- **Procedure `sp_team_salary_cursor(team_id)`**: adds up a team's coach salaries using a cursor.
- **Procedure `points_table(tournament_id)`**: points table of a tournament (2 points per win).
- CHECK constraints, foreign keys and UNIQUE constraints enforce the remaining rules.

How it works
- The screens are built with Java Swing and call helper methods in `DBConnection.java`, which connects to MySQL through JDBC.
- One generic CRUD panel builds INSERT, UPDATE, DELETE and SELECT queries from table and column names, using PreparedStatements.

## Setup
1. Install MySQL 8 and create the database by running the script once:
   ```
   mysql -u root -p < CricketTournamentManagementSystem.sql
   ```
2. Download MySQL Connector/J and add the jar to your classpath.
3. Create a `db.properties` file next to the code. It is listed in `.gitignore`, so your password is never uploaded:
   ```
   user=your_mysql_user
   password=your_mysql_password
   ```
4. Compile and run the application.

## Quick checks
After loading the script, these should work in MySQL:
```sql
SELECT PlayerCount(1);          -- 11
CALL sp_team_salary_cursor(1);  -- 180000.00
CALL points_table(1);           -- Nagpur Knights on top with 6 points
```

## Tech stack
Java, Swing, JDBC, MySQL
