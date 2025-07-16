import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BankingApp extends JFrame {
    private double balance = 1000.00;

    private Map<String, Double> loanAmounts = new HashMap<>();
    private Map<String, Double> emiAmounts = new HashMap<>();
    private Map<String, Integer> emiMonthsRemaining = new HashMap<>();
    private Map<String, Integer> emiDurations = new HashMap<>();

    private JTextField depositField, withdrawField, loanField, durationField;
    private JComboBox<String> loanTypeCombo;
    private JTextArea transactionLog;

    private JButton payPersonalBtn, payHomeBtn, payCarBtn;
    private final File logFile = new File("transaction_log.txt");

    public BankingApp() {
        setTitle("Advanced Banking Application");
        setSize(750, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] loanTypes = {"Personal", "Home", "Car"};
        for (String type : loanTypes) {
            loanAmounts.put(type, 0.0);
            emiAmounts.put(type, 0.0);
            emiMonthsRemaining.put(type, 0);
            emiDurations.put(type, 0);
        }

        // Top Panel: Balance
        JPanel topPanel = new JPanel(new GridLayout(1, 1));
        JLabel balanceLabel = new JLabel("Savings Balance: $" + balance);
        topPanel.add(balanceLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Operations
        JPanel centerPanel = new JPanel(new GridLayout(7, 3, 5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Banking Operations"));

        depositField = new JTextField();
        withdrawField = new JTextField();
        loanField = new JTextField();
        durationField = new JTextField();
        loanTypeCombo = new JComboBox<>(loanTypes);

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton requestLoanBtn = new JButton("Request Loan");
        JButton showStatementBtn = new JButton("Show Statement");
        JButton printStatementBtn = new JButton("Print Statement");

        payPersonalBtn = new JButton("Pay Personal EMI");
        payHomeBtn = new JButton("Pay Home EMI");
        payCarBtn = new JButton("Pay Car EMI");

        centerPanel.add(new JLabel("Deposit:"));
        centerPanel.add(depositField);
        centerPanel.add(depositBtn);

        centerPanel.add(new JLabel("Withdraw:"));
        centerPanel.add(withdrawField);
        centerPanel.add(withdrawBtn);

        centerPanel.add(new JLabel("Loan Amount:"));
        centerPanel.add(loanField);
        centerPanel.add(new JLabel(""));

        centerPanel.add(new JLabel("Loan Type:"));
        centerPanel.add(loanTypeCombo);
        centerPanel.add(requestLoanBtn);

        centerPanel.add(new JLabel("EMI Duration (months):"));
        centerPanel.add(durationField);
        centerPanel.add(new JLabel(""));

        centerPanel.add(payPersonalBtn);
        centerPanel.add(payHomeBtn);
        centerPanel.add(payCarBtn);

        centerPanel.add(showStatementBtn);
        centerPanel.add(printStatementBtn);
        centerPanel.add(new JLabel(""));

        add(centerPanel, BorderLayout.CENTER);

        // Transaction Log Area
        transactionLog = new JTextArea(10, 50);
        transactionLog.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(transactionLog);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction Log"));
        add(scrollPane, BorderLayout.SOUTH);

        // Deposit Action
        depositBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(depositField.getText());
                balance += amount;
                logTransaction("Deposited: $" + amount);
                balanceLabel.setText("Savings Balance: $" + String.format("%.2f", balance));
                showMessage("Deposit Success", "Deposited $" + amount);
            } catch (Exception ex) {
                showMessage("Error", "Invalid deposit amount");
            }
        });

        // Withdraw Action
        withdrawBtn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(withdrawField.getText());
                if (balance >= amount) {
                    balance -= amount;
                    logTransaction("Withdrawn: $" + amount);
                    showMessage("Withdraw Success", "Withdrawn $" + amount);
                } else {
                    logTransaction("Withdrawal Failed: Insufficient funds");
                    showMessage("Error", "Insufficient funds");
                }
                balanceLabel.setText("Savings Balance: $" + String.format("%.2f", balance));
            } catch (Exception ex) {
                showMessage("Error", "Invalid withdraw amount");
            }
        });

        // Request Loan Action
        requestLoanBtn.addActionListener(e -> {
            try {
                String type = (String) loanTypeCombo.getSelectedItem();
                double amount = Double.parseDouble(loanField.getText());
                int duration = Integer.parseInt(durationField.getText());

                if (duration <= 0) {
                    showMessage("Invalid Duration", "Duration must be more than 0 months.");
                    return;
                }

                double emi = amount / duration;
                loanAmounts.put(type, loanAmounts.get(type) + amount);
                emiAmounts.put(type, emi);
                emiMonthsRemaining.put(type, duration);
                emiDurations.put(type, duration);
                balance += amount;

                logTransaction("Loan Approved (" + type + "): $" + amount + " | EMI: $" + String.format("%.2f", emi) + " for " + duration + " months");
                balanceLabel.setText("Savings Balance: $" + String.format("%.2f", balance));
                showMessage("Loan Approved", type + " Loan of $" + amount + "\nEMI: $" + String.format("%.2f", emi) + " for " + duration + " months.");
            } catch (Exception ex) {
                showMessage("Error", "Invalid input. Check amount and duration.");
            }
        });

        // Pay EMI for Personal
        payPersonalBtn.addActionListener(e -> payEmi("Personal", balanceLabel));

        // Pay EMI for Home
        payHomeBtn.addActionListener(e -> payEmi("Home", balanceLabel));

        // Pay EMI for Car
        payCarBtn.addActionListener(e -> payEmi("Car", balanceLabel));

        // Show Statement
        showStatementBtn.addActionListener(e -> {
            try {
                JTextArea display = new JTextArea(30, 60);
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    display.append(line + "\n");
                }
                reader.close();
                JOptionPane.showMessageDialog(this, new JScrollPane(display), "Bank Statement", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                showMessage("Error", "Statement file not found.");
            }
        });

        // Print Statement
        printStatementBtn.addActionListener(e -> {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("bank_statement.txt"))) {
                writer.write("=== BANK STATEMENT ===\n");
                for (String type : loanAmounts.keySet()) {
                    writer.write("\n--- " + type + " Loan ---\n");
                    writer.write("Total Loan: $" + String.format("%.2f", loanAmounts.get(type)) + "\n");
                    writer.write("EMI: $" + String.format("%.2f", emiAmounts.get(type)) + "\n");
                    writer.write("Remaining Months: " + emiMonthsRemaining.get(type) + "\n");
                }
                writer.write("\nTotal Balance: $" + String.format("%.2f", balance) + "\n");
                writer.write("\nTransaction Log:\n");
                BufferedReader reader = new BufferedReader(new FileReader(logFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                }
                reader.close();
                showMessage("Statement Saved", "Statement saved to 'bank_statement.txt'");
            } catch (IOException ex) {
                showMessage("Error", "Error writing bank statement.");
            }
        });

        setVisible(true);
    }

    private void payEmi(String type, JLabel balanceLabel) {
        new Thread(() -> {
            double emi = emiAmounts.get(type);
            int months = emiMonthsRemaining.get(type);
            if (months > 0 && balance >= emi) {
                balance -= emi;
                loanAmounts.put(type, loanAmounts.get(type) - emi);
                emiMonthsRemaining.put(type, months - 1);
                logTransaction("EMI Paid for " + type + ": $" + String.format("%.2f", emi) + " | Remaining: " + (months - 1) + " months");

                SwingUtilities.invokeLater(() -> {
                    balanceLabel.setText("Savings Balance: $" + String.format("%.2f", balance));
                    showMessage("EMI Paid", type + " EMI of $" + String.format("%.2f", emi) + " paid.\nRemaining Months: " + (months - 1));
                });
            } else {
                logTransaction("EMI Failed for " + type + ": Insufficient funds or no EMI left");
                SwingUtilities.invokeLater(() ->
                        showMessage("EMI Failed", "Insufficient balance or no EMI left for " + type));
            }
        }).start();
    }

    private void logTransaction(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String entry = "[" + timestamp + "] " + message;
        transactionLog.append(entry + "\n");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.write(entry);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showMessage(String title, String msg) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingApp::new);
    }
}
