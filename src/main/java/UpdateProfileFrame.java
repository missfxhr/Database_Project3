import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class UpdateProfileFrame extends JFrame {
    private JPanel panel;
    private JPasswordField newPassword;
    private JCheckBox updateAddressCheckBox;
    private JTextField newAddress;
    private JCheckBox updatePasswordCheckBox;
    private JButton updateButton;
    private JTextArea updateResult;

    private DBMSWorkFlow workFlow;

    public UpdateProfileFrame(DBMSWorkFlow workflow){
        super("UpdateProfile Menu");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        workFlow = workflow;

        updatePasswordCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newPassword.setEditable(updatePasswordCheckBox.isSelected());
            }
        });

        updateAddressCheckBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                newAddress.setEditable(updateAddressCheckBox.isSelected());
            }
        });

        updateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int updatePassword = updatePasswordCheckBox.isSelected()?1:0;
                int updateAddress = updateAddressCheckBox.isSelected()?1:0;
                int flag = updatePassword | (updateAddress<<1);
                try {
                    workFlow.updateProfile(new String(newPassword.getPassword()),newAddress.getText(),flag);
                    updateResult.setText("Updated!");
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
