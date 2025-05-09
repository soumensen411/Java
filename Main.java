import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        // Create the main frame (window)
        JFrame frame = new JFrame("Swing Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);
        frame.setLocationRelativeTo(null); // Center the window

        // Create components
        JLabel label = new JLabel("Enter two numbers and click 'Add'");
        JTextField number1Field = new JTextField(10); // Text field for the first number
        JTextField number2Field = new JTextField(10); // Text field for the second number
        JButton addButton = new JButton("Add");

        // Create a result label to display the sum
        JLabel resultLabel = new JLabel("Result: ");

        // Add an action listener to the button
        addButton.addActionListener(e -> {
            try {
                // Parse the numbers from the text fields
                double num1 = Double.parseDouble(number1Field.getText());
                double num2 = Double.parseDouble(number2Field.getText());

                // Calculate the sum and update the result label
                double sum = num1 + num2;
                resultLabel.setText("Result: " + sum);
            } catch (NumberFormatException ex) {
                resultLabel.setText("Invalid input! Please enter valid numbers.");
            }
        });

        // Create a panel and add components
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());  // Use FlowLayout for simple layout
        panel.add(label);
        panel.add(new JLabel("Number 1:"));
        panel.add(number1Field);
        panel.add(new JLabel("Number 2:"));
        panel.add(number2Field);
        panel.add(addButton);
        panel.add(resultLabel);

        // Add the panel to the frame
        frame.add(panel);

        // Make the frame visible
        frame.setVisible(true);
    }
}