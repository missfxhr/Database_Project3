import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

public class LoginFrame extends JFrame{
    private DBMSWorkFlow workFlow;
    private JButton loginButton;
    private JPanel panel;
    private JTextField id;
    private JPasswordField password;

    public LoginFrame(DBMSWorkFlow workflow) {
        super("Login");
        setContentPane(panel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        workFlow = workflow;

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                login();
            }
        });

        panel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                login();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        setVisible(true);

    }

    public void login(){
        try {
            if (!workFlow.login(Integer.parseInt(id.getText()), new String(password.getPassword()))) {
                JOptionPane.showMessageDialog(LoginFrame.this,
                        "Wrong ID or Password",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                MainMenuFrame mainMenuFrame = new MainMenuFrame(workFlow);
                dispose();
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }
}
