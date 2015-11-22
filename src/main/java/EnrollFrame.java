import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class EnrollFrame extends JFrame{
    private JPanel panel;
    private JList<String> enrollCandidateList;
    private JList<String> prerequisiteList;
    private JTextArea enrollResult;
    private JButton enrollSelectedCourseButton;

    private DBMSWorkFlow workFlow;

    public EnrollFrame(DBMSWorkFlow workflow){
        super("Enroll Menu");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        workFlow = workflow;

        try{
            enrollCandidateList.setListData(workFlow.listEnrollCandidates());
        }catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }

        enrollSelectedCourseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String[] selectedValues = enrollCandidateList.getSelectedValue().split(" ");
                try {
                    if (workFlow.enroll(selectedValues[0],selectedValues[1],Integer.parseInt(selectedValues[2]))){
                        enrollCandidateList.setListData(workFlow.listEnrollCandidates());
                        enrollResult.setText("Enrollment Succeeded");
                    }
                } catch (SQLException e1) {
                    try {
                        enrollResult.setText(e1.getMessage());
                        if (Integer.parseInt(e1.getSQLState())==45004) {
                            prerequisiteList.setListData(workFlow.listPrerequisites(selectedValues[0]));
                        }
                    } catch (SQLException e2) {
                        System.out.println("SQLException: " + e2.getMessage());
                        System.out.println("SQLState: " + e2.getSQLState());
                        System.out.println("VendorError: " + e2.getErrorCode());
                    }
                    System.out.println("SQLException: " + e1.getMessage());
                    System.out.println("SQLState: " + e1.getSQLState());
                    System.out.println("VendorError: " + e1.getErrorCode());
                }
            }
        });

        setVisible(true);

    }
}
