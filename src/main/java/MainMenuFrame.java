import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class MainMenuFrame extends JFrame{
    private DBMSWorkFlow workFlow;
    private JPanel panel;
    private JList<String> currentCourses;
    private JButton detailButton;
    private JList<String> transcript;
    private JTextArea courseDetail;
    private JButton showTranscriptButton;
    private JButton enrollACourseButton;
    private JButton withdrawACourseButton;

    public MainMenuFrame(DBMSWorkFlow workflow){
        super("Main Menu");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        workFlow = workflow;


        showTranscriptButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    transcript.setListData(workFlow.listTranscript());
                } catch (SQLException e) {
                    System.out.println("SQLException: " + e.getMessage());
                    System.out.println("SQLState: " + e.getSQLState());
                    System.out.println("VendorError: " + e.getErrorCode());
                }
            }
        });

        detailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    courseDetail.setText(workFlow.listCourseDetail(transcript.getSelectedValue().split(" ")[0]));
                } catch (SQLException e) {
                    System.out.println("SQLException: " + e.getMessage());
                    System.out.println("SQLState: " + e.getSQLState());
                    System.out.println("VendorError: " + e.getErrorCode());
                }
            }
        });

        enrollACourseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFrame enrollFrame = new EnrollFrame(workFlow);
            }
        });

        try{
            currentCourses.setListData(workFlow.listCurrentCourses());
        }catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }

        setVisible(true);
    }
}
