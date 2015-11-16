import java.io.IOException;
import java.sql.SQLException;

public class ClientMain {
    public static void main(String[] argvs){
        WorkFlow workFlow = new WorkFlow();
        try {
            if (!workFlow.login()) {
                return;
            }
            workFlow.listCurrentCourses();

            workFlow.listTranscript();

            workFlow.enroll();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
}
