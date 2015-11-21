import javax.swing.*;

public class DBMSMain {
    private boolean loginRight;
    private DBMSWorkFlow workFlow;
    private JFrame loginFrame;
    private JFrame mainMenuFrame;

    DBMSMain(){
        workFlow = new DBMSWorkFlow();
    }

    public void DBMSRun(){
        loginFrame = new LoginFrame(workFlow);
    }


    public static void main(String[] args) {
        DBMSMain DBMSmain = new DBMSMain();
        DBMSmain.DBMSRun();
    }
}
