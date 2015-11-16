import javax.xml.transform.Result;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Calendar;

public class WorkFlow {
    private Student currentStudent;
    private Connection conn;
    private String semester;
    private int year;

    public Student getStudent(){
        return currentStudent;
    }

    private void serverConnInit() throws SQLException{
        conn = DriverManager.getConnection("jdbc:mysql://database495.coxgjlcsyyhn.us-east-1.rds.amazonaws.com:3306/project3-nudb?user=root&password=12345678");
    }

    private void updateDate() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        this.year = cal.get(Calendar.YEAR);
        if (month <= 2){
            this.semester = "Q1";
            this.year -= 1;
        }else if (month >= 9) {
            this.semester = "Q1";
        }else{
            this.semester = "Q2";
        }
    }

    public boolean login() throws IOException, SQLException{
        //initialization
        serverConnInit();
        updateDate();
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
        rs = getCallResult(cStmt,"Login Failed - Wrong Student Name or Wrong Password");
        if (rs == null) {
            return false;
        }
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
        ResultSet coursesSet;

        // call procedure
        String callProcedure = "{call list_current_courses(?,?,?)}";
        curCourses = conn.prepareCall(callProcedure);
        curCourses.setInt(1, currentStudent.getStudentId());
        curCourses.setString(2, this.semester);
        curCourses.setInt(3, this.year);

        // list courses
        String err = "No current courses!";
        String hint = "Current Courses:";
        coursesSet = getCallResult(curCourses,err);
        list(coursesSet, hint);
        releaseConnection(curCourses, coursesSet);
    }

    void listTranscript() throws SQLException {
        //initialization
        CallableStatement transcript;
        ResultSet transcriptSet;

        // call procedure
        String callProcedure = "{call list_transcript(?)}";
        transcript = conn.prepareCall(callProcedure);
        transcript.setInt(1, currentStudent.getStudentId());

        // list courses
        String err = "No course on the transcript!";
        String hint = "Student's Transcript:";
        transcriptSet = getCallResult(transcript,err);
        list(transcriptSet,hint);
        releaseConnection(transcript, transcriptSet);
    }

    void listCourseDetail() throws SQLException, IOException {
        //initialization
        CallableStatement courseDitail;
        ResultSet courseDitailSet;
        String uoscode;

        // read input
        System.out.println("Enter Course Code:");
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        uoscode = bufferRead.readLine();

        // call procedure
        String callProcedure = "{call list_course_detail(?, ?)}";
        courseDitail = conn.prepareCall(callProcedure);
        courseDitail.setInt(1, currentStudent.getStudentId());
        courseDitail.setString(2, uoscode);

        // list courses
        String err = "";
        String hint = "Course Detail:";
        courseDitailSet = getCallResult(courseDitail,err);
        list(courseDitailSet,hint);
        releaseConnection(courseDitail, courseDitailSet);
    }

    private ResultSet getCallResult(CallableStatement cStmt,String err) throws SQLException {
        if (!cStmt.execute()) {
            System.out.println(err);
            return null;
        }
        return cStmt.getResultSet();
    }

    private void list(ResultSet rs,  String hint) throws SQLException {
        // list courses
        if (rs==null) return;
        System.out.println(hint);
        int colNums = rs.getMetaData().getColumnCount();
        while(rs.next()) {
            for (int col = 1; col <= colNums; col++) {
                Object colVal = rs.getObject(col) != null? rs.getObject(col): "n.a.";
                System.out.print(colVal + " ");
            }
            System.out.println();
        }
    }

    private void printEnrollCandidates() throws SQLException{
        CallableStatement cStmt;
        ResultSet rs;

        String callProcedure = "{call print_candidate_courses(?,?,?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, this.semester);
        cStmt.setInt(3, this.year);

        String hint = "Candidate Courses:";

        rs = getCallResult(cStmt,"");
        list(rs,hint);
        releaseConnection(cStmt,rs);
    }

    public boolean enroll() throws IOException, SQLException{
        printEnrollCandidates();

        // initialization
        CallableStatement cStmt;
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
        ResultSet rs;

        String callProcedure = "{call print_prerequisites(?, ?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, uoscode);

        String hint = "Pre-Requisites Courses:";

        rs = getCallResult(cStmt,"");
        list(rs, hint);

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
