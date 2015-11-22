import java.sql.*;
import java.util.LinkedList;
import java.util.List;


public class DBMSWorkFlow {
    private Student currentStudent;
    private Connection conn;
    private QuarterYear currentQuarterYear;
    private QuarterYear nextQuarterYear;

    public Student getStudent(){
        return currentStudent;
    }

    public QuarterYear getCurrentQuarterYear(){return currentQuarterYear;}

    private void serverConnInit() throws SQLException{
        conn = DriverManager.getConnection("jdbc:mysql://database495.coxgjlcsyyhn.us-east-1.rds.amazonaws.com:3306/project3-nudb?user=root&password=12345678");
    }

    private void updateDate() {
        this.currentQuarterYear = new QuarterYear();
        this.nextQuarterYear = currentQuarterYear.nextQuarterYear();
    }

    public boolean login(int id, String password) throws SQLException{
        //initialization
        serverConnInit();
        updateDate();

        CallableStatement cStmt;
        ResultSet rs;

        // call procedure
        String callProcedure = "{call login(?, ?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, id);
        cStmt.setString(2, password);

        // check validity
        rs = getCallResult(cStmt);
        if (rs == null) {
            return false;
        }
        while(rs.next()) {
            currentStudent = new Student(rs.getInt("id"), rs.getString("name"), rs.getString("password"));
        }
        System.out.println();
        System.out.println(currentStudent.getName() + ", Welcome!");

        releaseConnection(cStmt, rs);
        return true;
    }

    public String[] listCurrentCourses() throws SQLException {
        //initialization
        CallableStatement curCourses;
        ResultSet coursesSet;

        // call procedure
        String callProcedure = "{call list_current_courses(?,?,?)}";
        curCourses = conn.prepareCall(callProcedure);
        curCourses.setInt(1, currentStudent.getStudentId());
        curCourses.setString(2, this.currentQuarterYear.quarterToString());
        curCourses.setInt(3, this.currentQuarterYear.getYear());

        // list courses
        coursesSet = getCallResult(curCourses);
        String[] resultList = makeList(coursesSet);
        releaseConnection(curCourses,coursesSet);
        return resultList;
    }

    public String[] listTranscript() throws SQLException {
        //initialization
        CallableStatement transcript;
        ResultSet transcriptSet;

        // call procedure
        String callProcedure = "{call list_transcript(?)}";
        transcript = conn.prepareCall(callProcedure);
        transcript.setInt(1, currentStudent.getStudentId());

        // list courses
        transcriptSet = getCallResult(transcript);
        String[] resultList = makeList(transcriptSet);
        releaseConnection(transcript,transcriptSet);
        return resultList;
    }

    public String listCourseDetail(String uoscode) throws SQLException {
        //initialization
        CallableStatement courseDetail;
        ResultSet courseDetailSet;

        // call procedure
        String callProcedure = "{call list_course_detail(?, ?)}";
        courseDetail = conn.prepareCall(callProcedure);
        courseDetail.setInt(1, currentStudent.getStudentId());
        courseDetail.setString(2, uoscode);

        // list courses
        courseDetailSet = getCallResult(courseDetail);
        String resultString = makeString(courseDetailSet);
        releaseConnection(courseDetail, courseDetailSet);
        return resultString;
    }

    public void updateProfile(String password, String address, int flag) throws SQLException {
        //initialization
        CallableStatement update;

        //update profile
        String query = "{call update_profile(?,?,?,?)}";

        update = conn.prepareCall(query);
        update.setInt(1, currentStudent.getStudentId());
        update.setString(2, password);
        update.setString(3, address);
        update.setInt(4, flag);

        update.execute();
    }

    public String[] listEnrollCandidates() throws SQLException{
        CallableStatement cStmt;
        ResultSet rs;

        String callProcedure = "{call print_enroll_candidate_courses(?,?,?,?,?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, this.currentQuarterYear.quarterToString());
        cStmt.setInt(3, this.currentQuarterYear.getYear());
        cStmt.setString(4, this.nextQuarterYear.quarterToString());
        cStmt.setInt(5, this.nextQuarterYear.getYear());

        rs = getCallResult(cStmt);
        String[] resultList = makeList(rs);
        releaseConnection(cStmt,rs);
        return resultList;
    }

    public boolean enroll(String uoscode, String semester, int year) throws SQLException{
        // initialization
        CallableStatement cStmt;

        // call procedure
        String callProcedure = "{call enroll(?,?,?,?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, uoscode);
        cStmt.setString(3, semester);
        cStmt.setInt(4, year);

        cStmt.execute();
        return true;
    }

    public String[] listPrerequisites(String uoscode) throws SQLException{
        CallableStatement cStmt;
        ResultSet rs;

        String callProcedure = "{call print_prerequisites(?, ?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, uoscode);

        rs = getCallResult(cStmt);
        String[] resultList = makeList(rs);
        releaseConnection(cStmt, rs);
        return resultList;
    }

    public String[] listWithdrawCandidates() throws SQLException{
        CallableStatement cStmt;
        ResultSet rs;

        String callProcedure = "{call print_withdraw_candidate_courses(?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());

        rs = getCallResult(cStmt);
        String[] resultList = makeList(rs);
        releaseConnection(cStmt, rs);
        return resultList;
    }

    public boolean withdraw(String uoscode) throws SQLException{
        // initialization
        setWarningMessage(0);
        CallableStatement cStmt;

        // call procedure
        String callProcedure = "{call withdraw(?,?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, uoscode);

        cStmt.execute();
        return true;
    }

    private void setWarningMessage (int warningMessage) throws SQLException{
        String callProcedure = "{call set_warning_message(?)}";
        CallableStatement cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, warningMessage);
        cStmt.execute();
    }

    public int checkWarningMessage () throws SQLException{
        String callProcedure = "{call check_warning_message()}";
        CallableStatement cStmt = conn.prepareCall(callProcedure);
        ResultSet rs = getCallResult(cStmt);
        if (rs == null) return 0;
        rs.first();
        return  Integer.parseInt(rs.getObject(1).toString());

    }

    private ResultSet getCallResult(CallableStatement cStmt) throws SQLException {
        if (!cStmt.execute()) {
            return null;
        }
        return cStmt.getResultSet();
    }

    public String[] makeList(ResultSet rs) throws SQLException {
        if (rs==null) return new String[0];
        List<String> resultList = new LinkedList<String>();
        int colNums = rs.getMetaData().getColumnCount();
        while(rs.next()){
            StringBuilder sb = new StringBuilder();
            for (int col = 1; col <= colNums; col++) {
                sb.append((rs.getObject(col) != null?rs.getObject(col).toString():"n.a.")+" ");
            }
            resultList.add(sb.toString());
        }
        String[] array = new String[resultList.size()];
        return resultList.toArray(array);
    }

    public String makeString(ResultSet rs) throws SQLException {
        if (rs==null || !rs.first()) return new String();
        int colNums = rs.getMetaData().getColumnCount();
        StringBuilder sb = new StringBuilder();
        for (int col = 1; col <= colNums; col++) {
            sb.append((rs.getObject(col) != null?rs.getObject(col).toString():"n.a.")+" ");
        }
        return sb.toString();
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
