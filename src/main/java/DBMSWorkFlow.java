import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


public class DBMSWorkFlow {
    private Student currentStudent;
    private Connection conn;
    private String semester;
    private int year;

    public Student getStudent(){
        return currentStudent;
    }

//    public void mainController() throws IOException, SQLException{
//        System.out.println("----Login In----");
//        if (!login()) {
//            return;
//        }
//        listCurrentCourses();
//        boolean quit = false;
//        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
//        while(!quit){
//            System.out.println();
//            System.out.println("----Main Menu----");
//            System.out.println("Please Enter Action Code:");
//            System.out.println("Possible Actions: 1 for list transcript");
//            System.out.println("Possible Actions: 2 for enroll");
//            System.out.println("Possible Actions: 3 for withdraw");
//            System.out.println("Possible Actions: 4 for update profile");
//            System.out.println("Possible Actions: 0 for logout");
//            int action_code = Integer.parseInt(bufferRead.readLine());
//            switch (action_code){
//                case 0:
//                    quit = true;
//                    break;
//                case 1:
//                    listTranscript();
//                    boolean quitTranscript = false;
//                    BufferedReader bufferReadTranscript = new BufferedReader(new InputStreamReader(System.in));
//                    while(!quitTranscript){
//                        System.out.println();
//                        System.out.println("----Course Detail View----");
//                        System.out.println("Please Enter Action Code:");
//                        System.out.println("Possible Actions: 1 for see course detail");
//                        System.out.println("Possible Actions: 0 for go back");
//                        int transcript_action_code = Integer.parseInt(bufferReadTranscript.readLine());
//                        switch (transcript_action_code) {
//                            case 0:
//                                quitTranscript = true;
//                                break;
//                            case 1:
//                                listCourseDetail();
//                                break;
//                            default:
//                                break;
//                        }
//                    }
//                    break;
//                case 2:
//                    enroll();
//                    break;
//                case 3:
//                    withdraw();
//                    break;
//                case 4:
//                    updateProfile();
//                    break;
//                default:
//                    break;
//            }
//
//        }
//    }

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
        curCourses.setString(2, this.semester);
        curCourses.setInt(3, this.year);

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
//
//    private void updateProfile() throws IOException, SQLException {
//        //initialization
//        CallableStatement update;
//        String password;
//        String address;
//
//        // input new password and address
//        System.out.println("Enter New Password:");
//        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
//        password = bufferRead.readLine();
//        System.out.println("Enter New Address:");
//        address = bufferRead.readLine();
//
//        //update profile
//        String query = "{call update_profile(?,?,?)}";
//
//        update = conn.prepareCall(query);
//        update.setInt(1, currentStudent.getStudentId());
//        update.setString(2, password);
//        update.setString(3, address);
//
//        update.execute();
//        System.out.println("Profile get updated!");
//    }

    public String[] listEnrollCandidates() throws SQLException{
        CallableStatement cStmt;
        ResultSet rs;

        String callProcedure = "{call print_enroll_candidate_courses(?,?,?)}";
        cStmt = conn.prepareCall(callProcedure);
        cStmt.setInt(1, currentStudent.getStudentId());
        cStmt.setString(2, this.semester);
        cStmt.setInt(3, this.year);

        String hint = "Enroll Candidate Courses:";

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
//
//    private void printWithdrawCandidates() throws SQLException{
//        CallableStatement cStmt;
//        ResultSet rs;
//
//        String callProcedure = "{call print_withdraw_candidate_courses(?)}";
//        cStmt = conn.prepareCall(callProcedure);
//        cStmt.setInt(1, currentStudent.getStudentId());
//
//        String hint = "Withdraw Candidate Courses:";
//
//        rs = getCallResult(cStmt,"");
//        list(rs,hint);
//        releaseConnection(cStmt,rs);
//    }
//
//    private boolean withdraw() throws IOException, SQLException{
//        printWithdrawCandidates();
//
//        // initialization
//        CallableStatement cStmt;
//        String uoscode, semester;
//        int year;
//
//        // read input
//        System.out.println("Enter Course Code:");
//        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
//        uoscode = bufferRead.readLine();
//        System.out.println("Enter Course Semester:");
//        semester = bufferRead.readLine();
//        System.out.println("Enter Course Year:");
//        year = Integer.parseInt(bufferRead.readLine());
//
//        // call procedure
//        String callProcedure = "{call withdraw(?,?,?,?)}";
//        cStmt = conn.prepareCall(callProcedure);
//        cStmt.setInt(1, currentStudent.getStudentId());
//        cStmt.setString(2, uoscode);
//        cStmt.setString(3, semester);
//        cStmt.setInt(4, year);
//
//        // check validity
//        try {
//            cStmt.execute();
//        } catch (SQLException e){
//            System.out.println("SQLException: " + e.getMessage());
//            return false;
//        }
//        System.out.println("Successfully Withdrew!");
//        return true;
//    }

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
