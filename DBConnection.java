/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author VEENA SHASTRI
 */
import java.awt.Component;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

//Defining constant strings

public class DBConnection {
        static final String JDBC_Driver="com.mysql.cj.jdbc.Driver";
        static final String Db_url="jdbc:mysql://localhost:3306/CricketTournamentManagementSystem";
        static final String pass;
        static final String user;
        static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Could not read db.properties", e);
        }
        
        user = props.getProperty("user");
        pass = props.getProperty("password");
        }
    

        // ================= PART 1: CONNECTION =================

        //Every screen calls this to get its connection
        public static Connection getConnection() throws SQLException {
            Connection conn=null;
            try {
                Class.forName(JDBC_Driver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            conn=DriverManager.getConnection(Db_url,user,pass);
            return conn;
        }

        // ================= PART 2: HELPERS THE SCREENS USE =================

        //Loads any ResultSet into a JTable model (headings + rows)
        public static void fill(DefaultTableModel model, ResultSet rs) throws SQLException {
            ResultSetMetaData md=rs.getMetaData();
            int cols=md.getColumnCount();
            Object[] heading=new Object[cols];
            for (int i=1; i<=cols; i++) {
                heading[i-1]=md.getColumnLabel(i);
            }
            model.setDataVector(new Object[0][0],heading);
            while (rs.next()) {
                Object[] row=new Object[cols];
                for (int i=1; i<=cols; i++) {
                    row[i-1]=rs.getObject(i);
                }
                model.addRow(row);
            }
        }

        //Fills a dropdown: query must return (id, label)
        public static List<Item> items(String query) throws SQLException {
            List<Item> list=new ArrayList<>();
            Connection conn=null;
            PreparedStatement pstmt=null;
            ResultSet rs=null;
            try {
                conn=getConnection();
                pstmt=conn.prepareStatement(query);
                rs=pstmt.executeQuery();
                while (rs.next()) {
                    list.add(new Item(rs.getString(1),rs.getString(2)));
                }
            } finally {
                if (rs!=null) rs.close();
                if (pstmt!=null) pstmt.close();
                if (conn!=null) conn.close();
            }
            return list;
        }

        //Shows MySQL's message (trigger T1/T2, foreign key, CHECK) in a pop-up
        public static void showError(Component parent, SQLException e) {
            JOptionPane.showMessageDialog(parent,e.getMessage(),"Database error",JOptionPane.ERROR_MESSAGE);
        }

        // ================= PART 3: SELECT (READ) =================

        //Shows every row of a table
        public static void selectAll(String table, DefaultTableModel model) throws SQLException {
            Connection conn=null;
            PreparedStatement pstmt=null;
            ResultSet rs=null;
            try {
                conn=getConnection();
                pstmt=conn.prepareStatement("SELECT * FROM `"+table+"` ORDER BY 1");
                rs=pstmt.executeQuery();
                fill(model,rs);
            } finally {
                if (rs!=null) rs.close();
                if (pstmt!=null) pstmt.close();
                if (conn!=null) conn.close();
            }
        }

        // ================= PART 4: INSERT / UPDATE / DELETE =================

        //INSERT INTO `table` (`c1`,`c2`) VALUES (?,?)
        public static void insertRow(String table, String[] columns, String[] values) throws SQLException {
            StringBuilder cols=new StringBuilder();
            StringBuilder marks=new StringBuilder();
            for (int i=0; i<columns.length; i++) {
                if (i>0) {
                    cols.append(",");
                    marks.append(",");
                }
                cols.append("`").append(columns[i]).append("`");
                marks.append("?");
            }
            String query="INSERT INTO `"+table+"` ("+cols+") VALUES ("+marks+")";
            execute(query,values);
        }

        //UPDATE `table` SET `c1`=?, `c2`=? WHERE `key1`=? AND `key2`=?
        public static void updateRow(String table, String[] setCols, String[] setVals,
                                     String[] keyCols, String[] keyVals) throws SQLException {
            StringBuilder sets=new StringBuilder();
            for (int i=0; i<setCols.length; i++) {
                if (i>0) sets.append(", ");
                sets.append("`").append(setCols[i]).append("`=?");
            }
            StringBuilder where=new StringBuilder();
            for (int i=0; i<keyCols.length; i++) {
                if (i>0) where.append(" AND ");
                where.append("`").append(keyCols[i]).append("`=?");
            }
            String query="UPDATE `"+table+"` SET "+sets+" WHERE "+where;
            //values go in the same order as the ? marks: SET values first, then key values
            String[] all=new String[setVals.length+keyVals.length];
            System.arraycopy(setVals,0,all,0,setVals.length);
            System.arraycopy(keyVals,0,all,setVals.length,keyVals.length);
            execute(query,all);
        }

        //DELETE FROM `table` WHERE `key1`=? AND `key2`=?
        public static void deleteRow(String table, String[] keyCols, String[] keyVals) throws SQLException {
            StringBuilder where=new StringBuilder();
            for (int i=0; i<keyCols.length; i++) {
                if (i>0) where.append(" AND ");
                where.append("`").append(keyCols[i]).append("`=?");
            }
            String query="DELETE FROM `"+table+"` WHERE "+where;
            execute(query,keyVals);
        }

        //Runs one INSERT/UPDATE/DELETE; empty values are stored as NULL
        private static void execute(String query, String[] values) throws SQLException {
            Connection conn=null;
            PreparedStatement pstmt=null;
            try {
                conn=getConnection();
                pstmt=conn.prepareStatement(query);
                for (int i=0; i<values.length; i++) {
                    if (values[i]==null || values[i].isEmpty()) {
                        pstmt.setNull(i+1,Types.NULL);
                    } else {
                        pstmt.setString(i+1,values[i]);
                    }
                }
                pstmt.executeUpdate();
            } finally {
                if (pstmt!=null) pstmt.close();
                if (conn!=null) conn.close();
            }
        }

        // ================= PART 5: PROCEDURES AND FUNCTION =================

        //Calls procedure points_table(tournament id) and shows the result
        public static void pointsTable(int tourId, DefaultTableModel model) throws SQLException {
            Connection conn=null;
            CallableStatement cstmt=null;
            ResultSet rs=null;
            try {
                conn=getConnection();
                cstmt=conn.prepareCall("{CALL points_table(?)}");
                cstmt.setInt(1,tourId);
                rs=cstmt.executeQuery();
                fill(model,rs);
            } finally {
                if (rs!=null) rs.close();
                if (cstmt!=null) cstmt.close();
                if (conn!=null) conn.close();
            }
        }

        //Calls function PlayerCount(team id): a function is used inside a SELECT
        public static int playerCount(int teamId) throws SQLException {
            int count=0;
            Connection conn=null;
            PreparedStatement pstmt=null;
            ResultSet rs=null;
            try {
                conn=getConnection();
                pstmt=conn.prepareStatement("SELECT PlayerCount(?)");
                pstmt.setInt(1,teamId);
                rs=pstmt.executeQuery();
                if (rs.next()) {
                    count=rs.getInt(1);
                }
            } finally {
                if (rs!=null) rs.close();
                if (pstmt!=null) pstmt.close();
                if (conn!=null) conn.close();
            }
            return count;
        }

        //Calls procedure sp_team_salary_cursor(team id): total of the team's coach salaries
        public static BigDecimal teamSalary(int teamId) throws SQLException {
            BigDecimal salary=BigDecimal.ZERO;
            Connection conn=null;
            CallableStatement cstmt=null;
            ResultSet rs=null;
            try {
                conn=getConnection();
                cstmt=conn.prepareCall("{CALL sp_team_salary_cursor(?)}");
                cstmt.setInt(1,teamId);
                rs=cstmt.executeQuery();
                if (rs.next()) {
                    salary=rs.getBigDecimal("Total_Coach_Salary");
                }
            } finally {
                if (rs!=null) rs.close();
                if (cstmt!=null) cstmt.close();
                if (conn!=null) conn.close();
            }
            return salary;
        }
}
