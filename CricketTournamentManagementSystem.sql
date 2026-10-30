-- =====================================================================
-- DBMS Mini Project: Cricket Tournament Management System
-- Cummins College of Engineering for Women, Pune
-- Batch A3: Pranjal Patil, Hitanshi Shah, Veena Shastri, Swarali Budruk
--
-- Run this whole file once on a fresh MySQL 8 server, e.g.:
--   mysql -u root -p < CricketTournamentManagementSystem.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS CricketTournamentManagementSystem;
USE CricketTournamentManagementSystem;

-- =====================================================================
-- 1. TABLES
-- =====================================================================

CREATE TABLE Team (
    Team_ID     INT AUTO_INCREMENT PRIMARY KEY,
    Team_Name   VARCHAR(50)  NOT NULL UNIQUE,
    Team_Logo   VARCHAR(100),
    City        VARCHAR(50)  NOT NULL,
    Captain_ID  INT NULL
);

CREATE TABLE Player (
    Player_ID    INT AUTO_INCREMENT PRIMARY KEY,
    Player_Name  VARCHAR(60) NOT NULL,
    P_Type       VARCHAR(20) NOT NULL,
    DOB          DATE        NOT NULL,
    Position     VARCHAR(40),
    Team_ID      INT         NOT NULL,
    CONSTRAINT chk_player_type CHECK (P_Type IN ('Batsman', 'Bowler', 'All-rounder', 'Wicketkeeper')),
    CONSTRAINT fk_player_team FOREIGN KEY (Team_ID) REFERENCES Team(Team_ID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Team and Player reference each other, so the captain link is added after Player exists
ALTER TABLE Team
    ADD CONSTRAINT fk_team_captain FOREIGN KEY (Captain_ID) REFERENCES Player(Player_ID)
        ON UPDATE CASCADE ON DELETE SET NULL;

-- Achievements is a multivalued attribute of Player, so it gets its own table
CREATE TABLE Player_Achievements (
    Player_ID     INT          NOT NULL,
    Achievements  VARCHAR(100) NOT NULL,
    PRIMARY KEY (Player_ID, Achievements),
    CONSTRAINT fk_ach_player FOREIGN KEY (Player_ID) REFERENCES Player(Player_ID)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Coach (
    Coach_ID    INT AUTO_INCREMENT PRIMARY KEY,
    Coach_Name  VARCHAR(60)   NOT NULL,
    Contact_No  DEC(10)       NOT NULL UNIQUE,
    Experience  INT           NOT NULL DEFAULT 0 CHECK (Experience >= 0),
    Salary      DECIMAL(10,2) NOT NULL CHECK (Salary > 0),
    Position    VARCHAR(30)   NOT NULL,
    Team_ID     INT           NOT NULL,
    CONSTRAINT fk_coach_team FOREIGN KEY (Team_ID) REFERENCES Team(Team_ID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE Tournament (
    Tournament_ID  INT AUTO_INCREMENT PRIMARY KEY,
    Tour_Name      VARCHAR(80)   NOT NULL UNIQUE,
    Start_Dt       DATE          NOT NULL,
    End_Dt         DATE          NOT NULL,
    PrizePool      DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (PrizePool >= 0),
    CONSTRAINT chk_tour_dates CHECK (End_Dt >= Start_Dt)
);

CREATE TABLE Umpire (
    Umpire_ID    INT AUTO_INCREMENT PRIMARY KEY,
    Umpire_Name  VARCHAR(60) NOT NULL,
    Experience   INT         NOT NULL DEFAULT 0 CHECK (Experience >= 0),
    Contact_No   DEC(10)     NOT NULL UNIQUE
);

CREATE TABLE Stadium (
    Stadium_ID    INT AUTO_INCREMENT PRIMARY KEY,
    Stadium_Name  VARCHAR(80) NOT NULL UNIQUE,
    Location      VARCHAR(50) NOT NULL,
    Capacity      INT         NOT NULL CHECK (Capacity > 0)
);

-- MATCH is a reserved word in MySQL, so it must always be written with backticks
CREATE TABLE `Match` (
    Match_ID  INT AUTO_INCREMENT,
    M_Dt      DATE NOT NULL,
    St_Time   TIME NOT NULL,
    Overs     INT  NOT NULL,
    WT_ID     INT  NULL,        -- winning team; NULL until the result is recorded
    Tour_ID   INT  NOT NULL,
    U_ID      INT  NOT NULL,
    St_ID     INT  NOT NULL,
    PRIMARY KEY (Match_ID),
    CONSTRAINT chk_overs CHECK (Overs BETWEEN 1 AND 50),
    CONSTRAINT uq_stadium_slot UNIQUE (St_ID, M_Dt, St_Time),
    CONSTRAINT fk_match_winner  FOREIGN KEY (WT_ID)   REFERENCES Team(Team_ID)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_match_tour    FOREIGN KEY (Tour_ID) REFERENCES Tournament(Tournament_ID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_match_umpire  FOREIGN KEY (U_ID)    REFERENCES Umpire(Umpire_ID)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_match_stadium FOREIGN KEY (St_ID)   REFERENCES Stadium(Stadium_ID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Statistics is a weak entity owned by Player
CREATE TABLE Statistics (
    Player_ID     INT          NOT NULL,
    Format        VARCHAR(10)  NOT NULL,
    Innings       INT          NOT NULL DEFAULT 0,
    Total_Runs    INT          NOT NULL DEFAULT 0,
    Dismissals    INT          NOT NULL DEFAULT 0,
    Strike_Rate   DECIMAL(6,2) NOT NULL DEFAULT 0,
    Wickets       INT          NOT NULL DEFAULT 0,
    Overs_Bowled  DECIMAL(6,1) NOT NULL DEFAULT 0,
    PRIMARY KEY (Player_ID, Format),
    CONSTRAINT chk_format     CHECK (Format IN ('T20', 'ODI', 'Test')),
    CONSTRAINT chk_stat_vals  CHECK (Innings >= 0 AND Total_Runs >= 0 AND Wickets >= 0
                                     AND Overs_Bowled >= 0 AND Strike_Rate >= 0),
    CONSTRAINT chk_dismissals CHECK (Dismissals <= Innings),
    CONSTRAINT fk_stat_player FOREIGN KEY (Player_ID) REFERENCES Player(Player_ID)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE Sponsors (
    Sponsor_ID    INT AUTO_INCREMENT PRIMARY KEY,
    Company_Name  VARCHAR(80) NOT NULL,
    Sponsor_Logo  VARCHAR(100),
    Contact_No    DEC(10)     NOT NULL UNIQUE
);

-- Many-to-many: Tournament <-> Sponsors
CREATE TABLE Tour_Sponsor (
    Tournament_ID  INT           NOT NULL,
    Sponsor_ID     INT           NOT NULL,
    Amount         DECIMAL(12,2) NOT NULL,
    PRIMARY KEY (Tournament_ID, Sponsor_ID),
    CONSTRAINT chk_amount CHECK (Amount > 0),
    CONSTRAINT fk_ts_tour    FOREIGN KEY (Tournament_ID) REFERENCES Tournament(Tournament_ID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_ts_sponsor FOREIGN KEY (Sponsor_ID) REFERENCES Sponsors(Sponsor_ID)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- Many-to-many: Match <-> Team (the two teams playing each match)
CREATE TABLE Match_Team (
    Match_ID  INT NOT NULL,
    T_ID      INT NOT NULL,
    PRIMARY KEY (Match_ID, T_ID),
    CONSTRAINT fk_mt_match FOREIGN KEY (Match_ID) REFERENCES `Match`(Match_ID)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_mt_team  FOREIGN KEY (T_ID) REFERENCES Team(Team_ID)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- =====================================================================
-- 2. SAMPLE DATA
-- =====================================================================

INSERT INTO Team (Team_ID, Team_Name, Team_Logo, City) VALUES
  (1, 'Pune Panthers', 'logos/pune_panthers.png', 'Pune'),
  (2, 'Mumbai Mavericks', 'logos/mumbai_mavericks.png', 'Mumbai'),
  (3, 'Nagpur Knights', 'logos/nagpur_knights.png', 'Nagpur'),
  (4, 'Nashik Titans', 'logos/nashik_titans.png', 'Nashik'),
  (5, 'Kolhapur Kings', 'logos/kolhapur_kings.png', 'Kolhapur');

INSERT INTO Player (Player_ID, Player_Name, P_Type, DOB, Position, Team_ID) VALUES
  (1, 'Aarav Mehta', 'Batsman', '1995-06-17', 'Opener', 1),
  (2, 'Rohan Desai', 'Bowler', '2002-11-28', 'Fast Bowler', 1),
  (3, 'Siddharth Kulkarni', 'Batsman', '1995-04-20', 'Number 3', 1),
  (4, 'Vihaan Joshi', 'Wicketkeeper', '2003-09-14', 'Wicketkeeper-Batsman', 1),
  (5, 'Aditya Patil', 'All-rounder', '2003-09-27', 'Batting All-rounder', 1),
  (6, 'Kunal Deshmukh', 'Bowler', '2005-08-25', 'Spinner', 1),
  (7, 'Omkar Bhosale', 'Batsman', '2003-08-08', 'Middle Order', 1),
  (8, 'Yash Jadhav', 'All-rounder', '1994-10-03', 'Bowling All-rounder', 1),
  (9, 'Tanmay Gokhale', 'Bowler', '1995-05-27', 'Medium Pacer', 1),
  (10, 'Sahil Pawar', 'All-rounder', '1995-08-01', 'Batting All-rounder', 1),
  (11, 'Nikhil Ranade', 'Batsman', '2004-08-22', 'Finisher', 1),
  (12, 'Rahul Sawant', 'Batsman', '1999-04-13', 'Opener', 2),
  (13, 'Karan Shetty', 'Bowler', '1998-06-12', 'Fast Bowler', 2),
  (14, 'Arjun Naik', 'Batsman', '2000-12-17', 'Number 3', 2),
  (15, 'Dhruv Parab', 'Wicketkeeper', '2004-02-24', 'Wicketkeeper-Batsman', 2),
  (16, 'Mihir Kamat', 'All-rounder', '1999-02-18', 'Batting All-rounder', 2),
  (17, 'Sumit Chavan', 'Bowler', '2002-05-10', 'Spinner', 2),
  (18, 'Pranav Mhatre', 'Batsman', '2001-03-21', 'Middle Order', 2),
  (19, 'Varun Salvi', 'All-rounder', '2005-12-19', 'Bowling All-rounder', 2),
  (20, 'Harsh Gaikwad', 'Bowler', '1998-01-28', 'Medium Pacer', 2),
  (21, 'Ishaan Wagh', 'All-rounder', '2005-06-12', 'Batting All-rounder', 2),
  (22, 'Neil Fernandes', 'Batsman', '2001-07-03', 'Finisher', 2),
  (23, 'Ayush Thakre', 'Batsman', '2000-10-18', 'Opener', 3),
  (24, 'Mayank Bhoyar', 'Bowler', '2001-02-14', 'Fast Bowler', 3),
  (25, 'Piyush Raut', 'Batsman', '2002-10-16', 'Number 3', 3),
  (26, 'Shubham Wankhede', 'Wicketkeeper', '2000-09-09', 'Wicketkeeper-Batsman', 3),
  (27, 'Gaurav Meshram', 'All-rounder', '2000-10-16', 'Batting All-rounder', 3),
  (28, 'Abhay Ingle', 'Bowler', '2002-09-26', 'Spinner', 3),
  (29, 'Tushar Dhote', 'Batsman', '1994-10-08', 'Middle Order', 3),
  (30, 'Rishi Barde', 'All-rounder', '1996-01-23', 'Bowling All-rounder', 3),
  (31, 'Chetan Lokhande', 'Bowler', '2002-11-04', 'Medium Pacer', 3),
  (32, 'Swapnil Kale', 'All-rounder', '2005-10-14', 'Batting All-rounder', 3),
  (33, 'Devang Pande', 'Batsman', '2001-03-16', 'Finisher', 3),
  (34, 'Pratik Gaikar', 'Batsman', '1997-02-21', 'Opener', 4),
  (35, 'Sanket Shinde', 'Bowler', '2005-08-22', 'Fast Bowler', 4),
  (36, 'Atharv Bhagat', 'Batsman', '2001-12-18', 'Number 3', 4),
  (37, 'Mandar Pagar', 'Wicketkeeper', '1998-09-13', 'Wicketkeeper-Batsman', 4),
  (38, 'Jayesh Gawali', 'All-rounder', '2004-05-18', 'Batting All-rounder', 4),
  (39, 'Amey Sonawane', 'Bowler', '2004-09-23', 'Spinner', 4),
  (40, 'Vedant Kharde', 'Batsman', '2001-07-14', 'Middle Order', 4),
  (41, 'Harshad Kadam', 'All-rounder', '1997-05-27', 'Bowling All-rounder', 4),
  (42, 'Tejas Ahire', 'Bowler', '1998-01-02', 'Medium Pacer', 4),
  (43, 'Soham Borse', 'All-rounder', '1996-11-20', 'Batting All-rounder', 4),
  (44, 'Rutvik Chaudhari', 'Batsman', '2001-09-16', 'Finisher', 4),
  (45, 'Yuvraj Mane', 'Batsman', '1999-04-09', 'Opener', 5),
  (46, 'Abhijeet Powar', 'Bowler', '2005-08-15', 'Fast Bowler', 5),
  (47, 'Saurabh Jagtap', 'Batsman', '2002-08-26', 'Number 3', 5),
  (48, 'Prasad Kamble', 'Wicketkeeper', '2000-09-22', 'Wicketkeeper-Batsman', 5),
  (49, 'Rohit Mohite', 'All-rounder', '1994-05-11', 'Batting All-rounder', 5),
  (50, 'Vishal Salunkhe', 'Bowler', '2001-02-15', 'Spinner', 5),
  (51, 'Akash Shelke', 'Batsman', '2005-12-26', 'Middle Order', 5),
  (52, 'Nilesh Patankar', 'All-rounder', '2002-05-15', 'Bowling All-rounder', 5),
  (53, 'Sagar Ghatge', 'Bowler', '1996-07-27', 'Medium Pacer', 5),
  (54, 'Anand Chougule', 'All-rounder', '2005-08-03', 'Batting All-rounder', 5),
  (55, 'Mohit Yadav', 'Batsman', '2004-05-16', 'Finisher', 5);

-- Captains are set after the players exist
UPDATE Team SET Captain_ID = 1  WHERE Team_ID = 1;
UPDATE Team SET Captain_ID = 12 WHERE Team_ID = 2;
UPDATE Team SET Captain_ID = 23 WHERE Team_ID = 3;
UPDATE Team SET Captain_ID = 34 WHERE Team_ID = 4;
UPDATE Team SET Captain_ID = 45 WHERE Team_ID = 5;

INSERT INTO Player_Achievements (Player_ID, Achievements) VALUES
  (1, 'Highest Run Scorer 2025'),
  (1, 'Player of the Series 2025'),
  (2, 'Best Bowling Figures 2025'),
  (5, 'Emerging Player 2024'),
  (5, 'Most Catches 2025'),
  (8, 'Century in Final 2024'),
  (12, 'Best Fielder 2024'),
  (12, 'Hat-trick 2025'),
  (13, 'Player of the Series 2025'),
  (16, 'Best Bowling Figures 2025'),
  (16, 'Highest Run Scorer 2025'),
  (19, 'Emerging Player 2024'),
  (23, 'Century in Final 2024'),
  (23, 'Most Catches 2025'),
  (24, 'Hat-trick 2025'),
  (27, 'Best Fielder 2024'),
  (27, 'Player of the Series 2025'),
  (30, 'Highest Run Scorer 2025'),
  (34, 'Best Bowling Figures 2025'),
  (34, 'Emerging Player 2024'),
  (35, 'Most Catches 2025'),
  (38, 'Century in Final 2024'),
  (38, 'Hat-trick 2025'),
  (41, 'Best Fielder 2024'),
  (45, 'Highest Run Scorer 2025'),
  (45, 'Player of the Series 2025'),
  (46, 'Best Bowling Figures 2025'),
  (49, 'Emerging Player 2024'),
  (49, 'Most Catches 2025'),
  (52, 'Century in Final 2024');

INSERT INTO Coach (Coach_ID, Coach_Name, Contact_No, Experience, Salary, Position, Team_ID) VALUES
  (1, 'Suresh Rane', 9876500001, 15, 120000, 'Head Coach', 1),
  (2, 'Amit Joshi', 9876500002, 8, 60000, 'Batting Coach', 1),
  (3, 'Rahul Nair', 9876500003, 12, 100000, 'Head Coach', 2),
  (4, 'Sandeep Kini', 9876500004, 9, 65000, 'Bowling Coach', 2),
  (5, 'Manoj Bhalerao', 9876500005, 18, 115000, 'Head Coach', 3),
  (6, 'Vikas Dongre', 9876500006, 6, 50000, 'Fielding Coach', 3),
  (7, 'Prakash Mundhe', 9876500007, 10, 90000, 'Head Coach', 4),
  (8, 'Dinesh Kapse', 9876500008, 7, 58000, 'Batting Coach', 4),
  (9, 'Ravindra Phadtare', 9876500009, 14, 105000, 'Head Coach', 5),
  (10, 'Anil Kumbhar', 9876500010, 11, 70000, 'Bowling Coach', 5);

INSERT INTO Tournament (Tournament_ID, Tour_Name, Start_Dt, End_Dt, PrizePool) VALUES
  (1, 'Maharashtra Premier League 2026', '2026-03-01', '2026-03-22', 5000000),
  (2, 'Maharashtra Cup 2026', '2026-11-05', '2026-11-20', 3000000);

INSERT INTO Stadium (Stadium_ID, Stadium_Name, Location, Capacity) VALUES
  (1, 'MCA International Stadium', 'Pune', 37000),
  (2, 'Brabourne Stadium', 'Mumbai', 20000),
  (3, 'VCA Stadium', 'Nagpur', 45000),
  (4, 'Gandhi Ground', 'Nashik', 15000),
  (5, 'Shahu Stadium', 'Kolhapur', 12000);

INSERT INTO Umpire (Umpire_ID, Umpire_Name, Experience, Contact_No) VALUES
  (1, 'Vinod Kulkarni', 20, 9822100001),
  (2, 'Ajay Bhide', 15, 9822100002),
  (3, 'Sanjay Phadke', 12, 9822100003),
  (4, 'Mahesh Tamhane', 9, 9822100004),
  (5, 'Ramesh Oak', 7, 9822100005),
  (6, 'Girish Apte', 4, 9822100006);

INSERT INTO `Match` (Match_ID, M_Dt, St_Time, Overs, WT_ID, Tour_ID, U_ID, St_ID) VALUES
  (1, '2026-03-01', '10:00:00', 20, 1, 1, 1, 1),
  (2, '2026-03-03', '14:30:00', 20, 3, 1, 2, 3),
  (3, '2026-03-05', '10:00:00', 20, 1, 1, 3, 2),
  (4, '2026-03-07', '14:30:00', 20, 5, 1, 4, 4),
  (5, '2026-03-09', '10:00:00', 20, 2, 1, 5, 1),
  (6, '2026-03-11', '14:30:00', 20, 4, 1, 1, 3),
  (7, '2026-03-13', '10:00:00', 20, 2, 1, 2, 2),
  (8, '2026-03-15', '14:30:00', 20, 3, 1, 3, 4),
  (9, '2026-03-17', '10:00:00', 20, 3, 1, 4, 1),
  (10, '2026-03-19', '14:30:00', 20, 5, 1, 5, 3),
  (11, '2026-11-05', '10:00:00', 50, NULL, 2, 1, 1),
  (12, '2026-11-07', '14:30:00', 50, NULL, 2, 2, 2);

INSERT INTO Match_Team (Match_ID, T_ID) VALUES
  (1, 1), (1, 2),
  (2, 1), (2, 3),
  (3, 1), (3, 4),
  (4, 1), (4, 5),
  (5, 2), (5, 3),
  (6, 2), (6, 4),
  (7, 2), (7, 5),
  (8, 3), (8, 4),
  (9, 3), (9, 5),
  (10, 4), (10, 5),
  (11, 1), (11, 3),
  (12, 2), (12, 5);

INSERT INTO Statistics (Player_ID, Format, Innings, Total_Runs, Dismissals, Strike_Rate, Wickets, Overs_Bowled) VALUES
  (1, 'T20', 38, 1057, 35, 147.43, 1, 2.3),
  (1, 'ODI', 12, 312, 10, 91.61, 0, 0.0),
  (1, 'Test', 14, 429, 12, 54.32, 2, 7.8),
  (2, 'T20', 55, 432, 49, 90.15, 57, 209.0),
  (2, 'ODI', 40, 475, 32, 78.8, 23, 82.6),
  (3, 'T20', 23, 1016, 18, 151.28, 2, 5.2),
  (4, 'T20', 25, 1094, 20, 148.5, 2, 5.2),
  (4, 'ODI', 35, 1008, 25, 96.14, 1, 2.0),
  (4, 'Test', 10, 269, 9, 50.21, 1, 3.9),
  (5, 'T20', 16, 378, 14, 123.19, 17, 54.0),
  (5, 'ODI', 25, 733, 21, 86.43, 33, 91.0),
  (6, 'T20', 28, 237, 24, 100.09, 53, 157.2),
  (7, 'T20', 50, 1679, 35, 130.86, 0, 0.0),
  (7, 'ODI', 27, 1179, 23, 80.96, 2, 4.8),
  (7, 'Test', 8, 227, 5, 52.95, 2, 7.6),
  (8, 'T20', 57, 1594, 52, 124.75, 21, 69.3),
  (8, 'ODI', 22, 461, 20, 81.88, 16, 51.1),
  (9, 'T20', 41, 299, 29, 95.31, 48, 166.3),
  (10, 'T20', 48, 1313, 33, 135.4, 35, 98.8),
  (10, 'ODI', 12, 341, 10, 87.91, 21, 69.7),
  (10, 'Test', 29, 561, 24, 40.49, 35, 95.7),
  (11, 'T20', 51, 2093, 42, 131.5, 2, 6.9),
  (11, 'ODI', 28, 1116, 23, 91.4, 2, 5.1),
  (12, 'T20', 21, 857, 18, 127.89, 1, 2.3),
  (13, 'T20', 37, 398, 28, 95.56, 55, 149.2),
  (13, 'ODI', 28, 327, 21, 70.82, 35, 98.8),
  (13, 'Test', 8, 66, 7, 47.96, 64, 192.9),
  (14, 'T20', 18, 500, 13, 125.69, 0, 0.0),
  (14, 'ODI', 27, 997, 21, 98.72, 1, 2.6),
  (15, 'T20', 55, 2114, 41, 143.45, 1, 2.6),
  (16, 'T20', 44, 991, 40, 131.9, 11, 38.8),
  (16, 'ODI', 31, 919, 24, 80.03, 17, 66.7),
  (16, 'Test', 30, 680, 26, 40.43, 30, 89.9),
  (17, 'T20', 22, 162, 18, 102.5, 52, 223.9),
  (17, 'ODI', 20, 131, 14, 66.12, 22, 62.1),
  (18, 'T20', 17, 482, 13, 135.62, 2, 7.9),
  (19, 'T20', 42, 1029, 37, 123.9, 20, 61.3),
  (19, 'ODI', 31, 615, 27, 77.5, 25, 63.6),
  (19, 'Test', 25, 647, 18, 47.14, 16, 58.9),
  (20, 'T20', 16, 115, 11, 104.37, 31, 136.9),
  (20, 'ODI', 30, 292, 27, 81.79, 50, 128.8),
  (21, 'T20', 47, 1299, 36, 123.52, 24, 87.2),
  (22, 'T20', 57, 2392, 46, 143.84, 0, 0.0),
  (22, 'ODI', 17, 661, 13, 97.24, 2, 7.6),
  (22, 'Test', 19, 837, 14, 48.49, 1, 2.7),
  (23, 'T20', 37, 1284, 32, 138.73, 1, 3.8),
  (23, 'ODI', 19, 561, 15, 92.56, 0, 0.0),
  (24, 'T20', 15, 189, 12, 114.41, 68, 241.2),
  (25, 'T20', 17, 648, 13, 127.53, 1, 3.1),
  (25, 'ODI', 32, 1281, 26, 99.98, 0, 0.0),
  (25, 'Test', 30, 1111, 28, 53.59, 1, 2.8),
  (26, 'T20', 48, 1988, 35, 144.85, 2, 4.4),
  (26, 'ODI', 22, 711, 19, 85.23, 0, 0.0),
  (27, 'T20', 57, 1552, 43, 119.06, 17, 50.0),
  (28, 'T20', 23, 293, 20, 95.66, 62, 233.1),
  (28, 'ODI', 30, 265, 26, 73.45, 66, 267.0),
  (28, 'Test', 25, 159, 18, 40.32, 26, 81.4),
  (29, 'T20', 16, 685, 11, 140.45, 2, 6.6),
  (29, 'ODI', 21, 571, 17, 86.05, 2, 4.6),
  (30, 'T20', 23, 422, 20, 123.16, 11, 41.9),
  (31, 'T20', 39, 276, 36, 99.04, 22, 70.3),
  (31, 'ODI', 13, 122, 10, 69.94, 37, 143.1),
  (31, 'Test', 26, 201, 23, 31.38, 20, 82.3),
  (32, 'T20', 31, 830, 26, 120.65, 18, 56.7),
  (32, 'ODI', 20, 575, 15, 89.48, 35, 113.4),
  (33, 'T20', 27, 1115, 24, 131.61, 2, 7.7),
  (34, 'T20', 37, 1250, 29, 149.1, 1, 2.4),
  (34, 'ODI', 28, 1077, 20, 82.55, 1, 2.7),
  (34, 'Test', 16, 671, 13, 51.67, 0, 0.0),
  (35, 'T20', 32, 219, 24, 119.17, 50, 202.2),
  (35, 'ODI', 28, 242, 21, 73.02, 16, 57.6),
  (36, 'T20', 41, 1328, 37, 145.38, 1, 3.8),
  (37, 'T20', 57, 2309, 44, 140.5, 1, 2.7),
  (37, 'ODI', 39, 1310, 29, 83.46, 1, 3.1),
  (37, 'Test', 18, 666, 14, 54.18, 1, 3.7),
  (38, 'T20', 15, 262, 14, 120.5, 11, 38.4),
  (38, 'ODI', 20, 344, 17, 79.18, 22, 72.0),
  (39, 'T20', 38, 214, 35, 110.77, 49, 197.9),
  (40, 'T20', 24, 774, 20, 131.58, 1, 2.4),
  (40, 'ODI', 32, 1020, 27, 91.35, 2, 6.1),
  (40, 'Test', 12, 336, 10, 49.06, 1, 2.1),
  (41, 'T20', 47, 1402, 36, 139.71, 23, 80.2),
  (41, 'ODI', 21, 443, 19, 84.59, 23, 88.0),
  (42, 'T20', 17, 172, 12, 94.0, 45, 185.3),
  (43, 'T20', 44, 861, 34, 121.84, 34, 122.3),
  (43, 'ODI', 28, 495, 24, 83.22, 10, 35.9),
  (43, 'Test', 24, 620, 20, 53.79, 17, 67.5),
  (44, 'T20', 47, 2092, 36, 142.87, 1, 2.6),
  (44, 'ODI', 15, 412, 13, 91.24, 0, 0.0),
  (45, 'T20', 48, 1722, 44, 136.48, 0, 0.0),
  (46, 'T20', 28, 146, 23, 97.23, 42, 147.7),
  (46, 'ODI', 26, 173, 21, 82.23, 64, 271.8),
  (46, 'Test', 25, 260, 19, 47.14, 38, 170.8),
  (47, 'T20', 54, 2149, 51, 130.67, 2, 6.2),
  (47, 'ODI', 33, 948, 29, 95.95, 2, 6.3),
  (48, 'T20', 17, 532, 12, 133.98, 0, 0.0),
  (49, 'T20', 49, 1274, 34, 118.2, 13, 36.1),
  (49, 'ODI', 15, 387, 12, 83.28, 27, 87.3),
  (49, 'Test', 8, 167, 6, 49.26, 25, 65.9),
  (50, 'T20', 41, 255, 33, 99.74, 60, 211.0),
  (50, 'ODI', 11, 63, 9, 76.72, 60, 233.8),
  (51, 'T20', 36, 1453, 33, 129.91, 0, 0.0),
  (52, 'T20', 20, 449, 15, 125.83, 34, 127.9),
  (52, 'ODI', 31, 917, 28, 79.25, 13, 42.9),
  (52, 'Test', 21, 482, 18, 46.54, 14, 55.3),
  (53, 'T20', 43, 536, 39, 106.77, 45, 120.8),
  (53, 'ODI', 33, 216, 28, 80.12, 37, 145.4),
  (54, 'T20', 25, 462, 23, 120.09, 21, 58.5),
  (55, 'T20', 42, 1116, 36, 154.13, 2, 7.6),
  (55, 'ODI', 18, 566, 14, 87.3, 0, 0.0),
  (55, 'Test', 22, 942, 15, 57.61, 1, 4.0);

INSERT INTO Sponsors (Sponsor_ID, Company_Name, Sponsor_Logo, Contact_No) VALUES
  (1, 'Tata Motors', 'logos/tata.png', 9988000001),
  (2, 'Reliance Jio', 'logos/jio.png', 9988000002),
  (3, 'Mahindra Group', 'logos/mahindra.png', 9988000003),
  (4, 'Bajaj Auto', 'logos/bajaj.png', 9988000004),
  (5, 'Kirloskar Brothers', 'logos/kirloskar.png', 9988000005);

INSERT INTO Tour_Sponsor (Tournament_ID, Sponsor_ID, Amount) VALUES
  (1, 1, 1500000),
  (1, 2, 1200000),
  (1, 3, 900000),
  (1, 4, 700000),
  (2, 1, 1000000),
  (2, 5, 600000);

-- =====================================================================
-- 3. VIEW AND INDEX
-- =====================================================================

-- Players together with their team name
CREATE VIEW vw_player_team AS
SELECT p.Player_ID, p.Player_Name, p.P_Type, t.Team_Name
FROM Player p
JOIN Team t ON t.Team_ID = p.Team_ID;

-- Speeds up searches by player name
CREATE INDEX idx_player_name ON Player(Player_Name);

-- =====================================================================
-- 4. TRIGGERS, FUNCTION AND PROCEDURES
-- (created after the sample data so the data loads without the checks)
-- =====================================================================

DELIMITER $$

-- T1: the winner of a match must be one of the two teams playing it
CREATE TRIGGER T1 BEFORE UPDATE ON `Match`
FOR EACH ROW
BEGIN
    IF NEW.WT_ID IS NOT NULL AND NOT EXISTS
       (SELECT 1 FROM Match_Team WHERE Match_ID = NEW.Match_ID AND T_ID = NEW.WT_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Winner must be one of the teams playing this match';
    END IF;
END$$

-- T2: the captain of a team must be a player of that same team
CREATE TRIGGER T2 BEFORE UPDATE ON Team
FOR EACH ROW
BEGIN
    IF NEW.Captain_ID IS NOT NULL AND NOT EXISTS
       (SELECT 1 FROM Player WHERE Player_ID = NEW.Captain_ID AND Team_ID = NEW.Team_ID) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Captain must be a player of the same team';
    END IF;
END$$

-- Function: number of players in a team
CREATE FUNCTION PlayerCount (p_team_id INT) RETURNS INT
READS SQL DATA
BEGIN
    DECLARE v_count INT;
    SELECT COUNT(*) INTO v_count FROM Player WHERE Team_ID = p_team_id;
    RETURN v_count;
END$$

-- Procedure with a cursor: adds up a team's coach salaries one row at a time
CREATE PROCEDURE sp_team_salary_cursor(IN p_team_id INT)
BEGIN
    DECLARE v_salary DECIMAL(10,2);
    DECLARE v_total  DECIMAL(12,2) DEFAULT 0;
    DECLARE v_done   INT DEFAULT 0;

    DECLARE cur_coach CURSOR FOR
        SELECT Salary FROM Coach WHERE Team_ID = p_team_id;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    OPEN cur_coach;
    read_loop: LOOP
        FETCH cur_coach INTO v_salary;
        IF v_done = 1 THEN
            LEAVE read_loop;
        END IF;
        SET v_total = v_total + v_salary;
    END LOOP;
    CLOSE cur_coach;

    SELECT p_team_id AS Team_ID, v_total AS Total_Coach_Salary;
END$$

-- Procedure: points table of a tournament (2 points per win)
CREATE PROCEDURE points_table(IN p_tour_id INT)
BEGIN
    SELECT t.Team_Name,
           COUNT(m.Match_ID)                            AS Played,
           SUM(m.WT_ID = t.Team_ID)                     AS Won,
           COUNT(m.Match_ID) - SUM(m.WT_ID = t.Team_ID) AS Lost,
           2 * SUM(m.WT_ID = t.Team_ID)                 AS Points
    FROM Team t
    JOIN Match_Team mt ON mt.T_ID = t.Team_ID
    JOIN `Match` m ON m.Match_ID = mt.Match_ID AND m.Tour_ID = p_tour_id AND m.WT_ID IS NOT NULL
    GROUP BY t.Team_ID, t.Team_Name
    ORDER BY Points DESC, t.Team_Name;
END$$

DELIMITER ;

-- =====================================================================
-- 5. QUICK CHECKS (optional; each should return rows)
-- =====================================================================
-- SELECT PlayerCount(1) AS Pune_Players;            -- 11
-- CALL sp_team_salary_cursor(1);                    -- 180000.00
-- CALL points_table(1);                             -- Nagpur Knights on top with 6 points
-- SELECT * FROM vw_player_team WHERE Team_Name = 'Pune Panthers';
