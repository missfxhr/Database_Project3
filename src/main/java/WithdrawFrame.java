import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class WithdrawFrame extends JFrame{
    private JPanel panel;
    private JList<String> withdrawCandidateList;
    private JButton withdrawSelectedCourseButton;
    private JTextArea withdrawResult;

    private DBMSWorkFlow workFlow;

    public WithdrawFrame(DBMSWorkFlow workflow){
        super("Withdraw Menu");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        workFlow = workflow;

        try{
            withdrawCandidateList.setListData(workFlow.listWithdrawCandidates());
        }catch (SQLException e){
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }

        withdrawSelectedCourseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String[] selectedValues = withdrawCandidateList.getSelectedValue().split(" ");
                try {
                    if (workFlow.withdraw(selectedValues[0])){
                        withdrawCandidateList.setListData(workFlow.listWithdrawCandidates());
                        withdrawResult.setText("Withdraw Succeeded");
                        if (workFlow.checkWarningMessage() == 1) {
                            System.out.println("");
                            JOptionPane.showMessageDialog(WithdrawFrame.this,
                                    "Low Enrollment Rate (<50%) for Dropped Course!",
                                    "Withdraw Warning",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                } catch (SQLException e1) {
                    withdrawResult.setText(e1.getMessage());
                    System.out.println("SQLException: " + e1.getMessage());
                    System.out.println("SQLState: " + e1.getSQLState());
                    System.out.println("VendorError: " + e1.getErrorCode());
                }
            }
        });

        setVisible(true);

    }
}
