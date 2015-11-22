import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class TranscriptFrame extends JFrame{
    private JPanel panel;
    private JButton showDetailButton;
    private JList<String> transcript;
    private JTextArea courseDetail;

    private DBMSWorkFlow workFlow;

    public TranscriptFrame(DBMSWorkFlow workflow){
        super("Transcript Menu");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        workFlow = workflow;


        try {
            transcript.setListData(workFlow.listTranscript());
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }



        showDetailButton.addActionListener(new ActionListener() {
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

        setVisible(true);

    }
}
