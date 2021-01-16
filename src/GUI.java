import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class GUI  extends JFrame {
    private JPanel GUIpanel;
    private JTextField percentField;
    private JButton startButton;

    public GUI() {

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {

                    String percent = percentField.getText();
                    float fpercent=Float.parseFloat(percent);

                    Main m = new Main();
                    JOptionPane.showMessageDialog(null, m.run(fpercent));


                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("App");
        frame.setContentPane(new GUI().GUIpanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        //Main m = new Main();
       // m.run(1);

    }
}