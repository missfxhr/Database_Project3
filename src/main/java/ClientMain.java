import java.io.*;
import java.sql.SQLException;

public class ClientMain {
    public static void main(String[] argvs){
        WorkFlow workFlow = new WorkFlow();
        try {
            workFlow.mainController();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
}
