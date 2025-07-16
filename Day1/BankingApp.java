import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BankingApp extends JFrame {
    private double balance = 1000.00;
    private double loanAmount = 0.0;
    private double emiAmount = 0.0;
    private int remainingEmiMonths = 0;

    private JTextField depositField, withdrawField, loanField, emiField;
    private JTextArea transactionLog;
    private JButton payEmiBtn;

    public BankingApp() {
        setTitle("Banking Application");
        setSize(600, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        
        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        JLabel balanceLabel = new JLabel("Account Status: Savings Balance: $" + balance);
        JLabel loanLabel = new JLabel("Loan Status: Loan Amount: $" + loanAmount);
        JLabel emiLabel = new JLabel("EMI Status: EMI: $" + emiAmount + " (Remaining: " + remainingEmiMonths + " months)");
        topPanel.add(balanceLabel);
        topPanel.add(loanLabel);
        topPanel.add(emiLabel);
        add(topPanel, BorderLayout.NORTH);

        
        JPanel centerPanel = new JPanel(new GridLayout(5, 3, 5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Banking Operations"));

        depositField = new JTextField();
        withdrawField = new JTextField();
        loanField = new JTextField();
        emiField = new JTextField(); emiField.setEditable(false);

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton requestLoanBtn = new JButton("Request Loan");
        payEmiBtn = new JButton("Pay EMI");
        payEmiBtn.setEnabled(false);

        centerPanel.add(new JLabel("Deposit Amount:"));
        centerPanel.add(depositField);
        centerPanel.add(depositBtn);

        centerPanel.add(new JLabel("Withdraw Amount:"));
        centerPanel.add(withdrawField);
        centerPanel.add(withdrawBtn);

        centerPanel.add(new JLabel("Loan Amount:"));
        centerPanel.add(loanField);
        centerPanel.add(requestLoanBtn);

        centerPanel.add(new JLabel("Pay EMI:"));
        centerPanel.add(emiField);
        centerPanel.add(payEmiBtn);

        add(centerPanel, BorderLayout.CENTER);

        
        transactionLog = new JTextArea();
        transactionLog.setEditable(false);
        add(new JScrollPane(transactionLog), BorderLayout.SOUTH);

        // Deposit Action
        depositBtn.addActionListener(e -> {
            double amount = Double.parseDouble(depositField.getText());
            balance += amount;
            transactionLog.append("Deposited: $" + amount + "\n");
            updateStatus(balanceLabel, loanLabel, emiLabel);
        });

        // Withdraw Action
        withdrawBtn.addActionListener(e -> {
            double amount = Double.parseDouble(withdrawField.getText());
            if (balance >= amount) {
                balance -= amount;
                transactionLog.append("Withdrawn: $" + amount + "\n");
            } else {
                transactionLog.append("Withdrawal failed: Insufficient funds\n");
            }
            updateStatus(balanceLabel, loanLabel, emiLabel);
        });

        // Loan Request
        requestLoanBtn.addActionListener(e -> {
            double amount = Double.parseDouble(loanField.getText());
            loanAmount += amount;
            emiAmount = amount / 12;
            remainingEmiMonths = 12;
            balance += amount;
            transactionLog.append("Loan Approved: $" + amount + " | EMI: $" + emiAmount + "\n");
            emiField.setText(String.valueOf(emiAmount));
            payEmiBtn.setEnabled(true);
            updateStatus(balanceLabel, loanLabel, emiLabel);
        });

        // Pay EMI (with Multithreading)
        payEmiBtn.addActionListener(e -> {
            new Thread(() -> {
                if (remainingEmiMonths > 0 && balance >= emiAmount) {
                    balance -= emiAmount;
                    loanAmount -= emiAmount;
                    remainingEmiMonths--;
                    transactionLog.append("EMI Paid: $" + emiAmount + " | Remaining months: " + remainingEmiMonths + "\n");
                    if (remainingEmiMonths == 0) {
                        emiAmount = 0;
                        emiField.setText("0");
                        payEmiBtn.setEnabled(false);
                    }
                    SwingUtilities.invokeLater(() -> updateStatus(balanceLabel, loanLabel, emiLabel));
                } else {
                    transactionLog.append("EMI Payment failed: Insufficient funds or no EMI left\n");
                }
            }).start();
        });

        setVisible(true);
    }

    private void updateStatus(JLabel balanceLabel, JLabel loanLabel, JLabel emiLabel) {
        balanceLabel.setText("Account Status: Savings Balance: $" + String.format("%.2f", balance));
        loanLabel.setText("Loan Status: Loan Amount: $" + String.format("%.2f", loanAmount));
        emiLabel.setText("EMI Status: EMI: $" + String.format("%.2f", emiAmount) + " (Remaining: " + remainingEmiMonths + " months)");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankingApp::new);
    }
}
