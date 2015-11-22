import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class MainMenuFrame extends JFrame{
    private JPanel panel;
    private JList<String> currentCourses;
    private JButton showTranscriptButton;
    private JButton enrollACourseButton;
    private JButton withdrawACourseButton;
    private JButton refreshCurrentCourseButton;
    private JButton updateProfileButton;
    private JLabel currentQuarterYearLabel;
    private JLabel studentNameLabel;
    private JButton logoutButton;

    private DBMSWorkFlow workFlow;
    private LoginFrame loginFrame;

    public MainMenuFrame(DBMSWorkFlow workflow,LoginFrame loginframe){
        super("Main Menu");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        loginFrame = loginframe;
        workFlow = workflow;

        showTranscriptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFrame transcriptFrame = new TranscriptFrame(workFlow);
            }
        });

        enrollACourseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFrame enrollFrame = new EnrollFrame(workFlow);
            }
        });

        withdrawACourseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFrame withdrawFrame = new WithdrawFrame(workFlow);
            }
        });

        refreshCurrentCourseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                refreshCurrentCourse();
            }
        });

        updateProfileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFrame updateProfileFrame = new UpdateProfileFrame(workFlow);
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
                loginFrame.setVisible(true);
            }
        });

        studentNameLabel.setText(workFlow.getStudent().getName());
        currentQuarterYearLabel.setText(workFlow.getCurrentQuarterYear().toString());
        refreshCurrentCourse();

        setVisible(true);
    }

    private void refreshCurrentCourse(){
        try{
            currentCourses.setListData(workFlow.listCurrentCourses());
        }catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
}
