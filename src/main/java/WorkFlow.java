import java.io.*;
import java.sql.*;

public class WorkFlow {

    public Student getStudent(){
        return currentStudent;
    }

    void serverConnInit() {
        try {
            conn = DriverManager.getConnection("jdbc:mysql://database495.coxgjlcsyyhn.us-east-1.rds.amazonaws.com:3306/project3-nudb?" + "user=root&password=12345678");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }

    boolean login(){
        serverConnInit();

        String username;
        String password;
        CallableStatement cStmt = null;
        ResultSet rs = null;

        try{
            System.out.println("Enter Student Name:");
            BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
            username = bufferRead.readLine();
            System.out.println("Enter Student Password:");
            bufferRead = new BufferedReader(new InputStreamReader(System.in));
            password = bufferRead.readLine();

            String callProcedure = "{call login(?, ?)}";

            cStmt = conn.prepareCall(callProcedure);
            cStmt.setString(1, username);
            cStmt.setString(2, password);
            if (!cStmt.execute()) {
                System.out.println("Login Failed - Wrong Student Name or Wrong Password");
                return false;
            }
            rs = cStmt.getResultSet();
            while(rs.next()) {
                currentStudent = new Student(rs.getInt("id"), rs.getString("name"), rs.getString("password"));
            }
            System.out.println(currentStudent.getName() + ", Welcome!");

        } catch (IOException e) {
            e.printStackTrace();
        } catch(SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally{
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                }
            }
            if (cStmt != null) {
                try {
                    cStmt.close();
                } catch (SQLException sqlEx) {
                }
            }
        }
        return true;
    }
    void listCurrentCourses(){}
    void listTranscript(){}
    void listCourseDetail(){}
    private Student currentStudent;
    private Connection conn;
}
