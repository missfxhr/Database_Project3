import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;

public class WorkFlow {
    private Student currentStudent;
    private Connection conn;

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

    boolean login() throws IOException, SQLException{
        //initialization
        serverConnInit();
        String username;
        String password;
        CallableStatement cStmt = null;
        ResultSet rs = null;

        // get username and password
        System.out.println("Enter Student Name:");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        username = bufferRead.readLine();
        System.out.println("Enter Student Password:");
        bufferRead = new BufferedReader(new InputStreamReader(System.in));
        password = bufferRead.readLine();

        // call procedure
        String callProcedure = "{call login(?, ?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setString(1, username);
        cStmt.setString(2, password);

        // check validity
        if (!cStmt.execute()) {
            System.out.println("Login Failed - Wrong Student Name or Wrong Password");
            return false;
        }
        rs = cStmt.getResultSet();
        while(rs.next()) {
            currentStudent = new Student(rs.getInt("id"), rs.getString("name"), rs.getString("password"));
        }
        System.out.println(currentStudent.getName() + ", Welcome!");

        releaseConnection(cStmt, rs);
        return true;
    }

    void listCurrentCourses() throws SQLException {
        //initialization
        CallableStatement curCourses = null;
        ResultSet rowCurCourses = null;

        // call procedure
        String callProcedure = "{call list_current_courses(?)}";
        curCourses = conn.prepareCall(callProcedure);
        curCourses.setInt(1, currentStudent.getStudentId());

        // list courses
        if (!curCourses.execute()) {
            System.out.println("No current courses!");
            return;
        }

        rowCurCourses = curCourses.getResultSet();
        System.out.println("Current Courses:");
        int colNums = rowCurCourses.getMetaData().getColumnCount();
        while(rowCurCourses.next()) {
            for (int col = 1; col <= colNums; col++) {
                Object colVal = rowCurCourses.getObject(col) != null? rowCurCourses.getObject(col): "n.a.";
                System.out.print(colVal + " ");
            }
            System.out.println();
        }

        releaseConnection(curCourses, rowCurCourses);
    }
    void listTranscript(){}
    void listCourseDetail(){}

    void releaseConnection(CallableStatement cStmt, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (cStmt != null) {
            cStmt.close();
        }
    }
}
