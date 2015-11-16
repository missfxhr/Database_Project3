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

    public void serverConnInit() throws SQLException{
        conn = DriverManager.getConnection("jdbc:mysql://database495.coxgjlcsyyhn.us-east-1.rds.amazonaws.com:3306/project3-nudb?user=root&password=12345678");
    }

    public boolean login() throws IOException, SQLException{
        //initialization
        serverConnInit();
        int id;
        String password;
        CallableStatement cStmt;
        ResultSet rs;

        // get username and password
        System.out.println("Enter Student ID:");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        id = Integer.parseInt(bufferRead.readLine());
        System.out.println("Enter Student Password:");
        password = bufferRead.readLine();

        // call procedure
        String callProcedure = "{call login(?, ?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, id);
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

    public void listCurrentCourses() throws SQLException,IOException {
        //initialization
        CallableStatement curCourses;
        ResultSet coursesSet = null;

        // call procedure
        String callProcedure = "{call list_current_courses(?)}";
        curCourses = conn.prepareCall(callProcedure);
        curCourses.setInt(1, currentStudent.getStudentId());

        // list courses
        String err = "No current courses!";
        String hint = "Current Courses:";
        list(curCourses, coursesSet, err, hint);
        releaseConnection(curCourses, coursesSet);

        releaseConnection(curCourses, coursesSet);
    }

    void listTranscript() throws SQLException {
        //initialization
        CallableStatement transcript;
        ResultSet transcriptSet = null;

        // call procedure
        String callProcedure = "{call list_transcript(?)}";
        transcript = conn.prepareCall(callProcedure);
        transcript.setInt(1, currentStudent.getStudentId());

        // list courses
        String err = "No course on the transcript!";
        String hint = "Student's Transcript:";
        list(transcript, transcriptSet, err, hint);
        releaseConnection(transcript, transcriptSet);
    }

    void listCourseDetail(){}

    void list(CallableStatement cStmt, ResultSet rs, String err, String hint) throws SQLException {
        // list courses
        if (!cStmt.execute()) {
            System.out.println(err);
            return;
        }

        rs = cStmt.getResultSet();
        System.out.println(hint);
        int colNums = rs.getMetaData().getColumnCount();
        while(rs.next()) {
            for (int col = 1; col <= colNums; col++) {
                Object colVal = rs.getObject(col) != null? rs.getObject(col): "n.a.";
                System.out.print(colVal + " ");
            }
            System.out.println();
        }

        releaseConnection(cStmt, rs);
    }

    public boolean enroll() throws IOException, SQLException{
        // initialization
        CallableStatement cStmt;
        ResultSet rs;
        String uoscode, semester;
        int year;

        // read input
        System.out.println("Enter Course Code:");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        uoscode = bufferRead.readLine();
        System.out.println("Enter Course Semester:");
        semester = bufferRead.readLine();
        System.out.println("Enter Course Year:");
        year = Integer.parseInt(bufferRead.readLine());

        // call procedure
        String callProcedure = "{call enroll(?,?,?,?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, uoscode);
        cStmt.setString(3, semester);
        cStmt.setInt(4, year);

        // check validity
        try {
            cStmt.execute();
        } catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            if (Integer.parseInt(e.getSQLState())==45004) {
                listPrerequisites(uoscode);
            }
            return false;
        }
        System.out.println("Successfully Enrolled!");
        return true;
    }

    public void listPrerequisites(String uoscode) throws SQLException{
        CallableStatement cStmt;
        ResultSet rs = null;
        String callProcedure = "{call print_prerequisites(?, ?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, uoscode);
        list(cStmt, rs, "", "Pre-Requisites Courses:");

        releaseConnection(cStmt,rs);
    }

    private void releaseConnection(CallableStatement cStmt, ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
        if (cStmt != null) {
            cStmt.close();
        }
    }
}
