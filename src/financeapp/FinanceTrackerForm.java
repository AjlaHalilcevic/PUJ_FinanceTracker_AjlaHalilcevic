package financeapp;

import com.mongodb.client.model.ClusteredIndexOptions;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FinanceTrackerForm {
    private JPanel mainPanel;
    private JComboBox<String> typeCombo;
    private JTextField amountField;
    private JTextField descriptionField;
    private JButton addButton;
    private JTable transactionTable;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel balanceLabel;
    private JComboBox<String> categoryCombo;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton exportButton;

    private TransactionManager manager;

    public FinanceTrackerForm() {
        manager = new TransactionManager();

        loadDataIntoTable();
        updateSummary();

        transactionTable.getSelectionModel().addListSelectionListener(e -> {
            int row = transactionTable.getSelectedRow();
            if (row >= 0) {
                typeCombo.setSelectedItem(transactionTable.getValueAt(row, 0));
                amountField.setText(transactionTable.getValueAt(row, 1).toString());
                descriptionField.setText(transactionTable.getValueAt(row, 2).toString());
                categoryCombo.setSelectedItem(transactionTable.getValueAt(row, 3));
            }
        });

        addButton.addActionListener(e -> {
            try {
                String type = (String) typeCombo.getSelectedItem();
                double amount = Double.parseDouble(amountField.getText());
                String description = descriptionField.getText();
                String category = (String) categoryCombo.getSelectedItem();

                Transaction t = new Transaction(type, amount, description, category);
                manager.addTransaction(t);

                loadDataIntoTable();
                updateSummary();

                amountField.setText("");
                descriptionField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Iznos mora biti broj!");
            }
        });

        updateButton.addActionListener(e -> {
            int row = transactionTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Nije selektovana transakcija!");
                return;
            }

            java.util.ArrayList<Transaction> list = manager.getAllTransactions();
            Transaction original = list.get(row);

            String type = (String) typeCombo.getSelectedItem();
            String amountText = amountField.getText().trim().replace(',', '.');
            double amount;

            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Iznos mora biti broj!", "Greška",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            String description = descriptionField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();

            manager.updateTransaction(original.getId(), type, amount, description, category);

            loadDataIntoTable();
            updateSummary();

        });

        deleteButton.addActionListener(e -> {
            int row = transactionTable.getSelectedRow();
            if(row == -1) {
                JOptionPane.showMessageDialog(mainPanel, "Nije selektovana transakcija!");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    mainPanel,
                    "Da li sigurno želiš obrisati ovu transakciju?",
                    "Potvrda brisanja",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_NO_OPTION) {
                return;
            }

            ArrayList<Transaction> list = manager.getAllTransactions();
            Transaction t = list.get(row);

            manager.deleteTransaction(t.getId());

            loadDataIntoTable();
            updateSummary();
        });

        exportButton.addActionListener(e -> {
            ArrayList<Transaction> list = manager.getAllTransactions();

            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel,
                        "Nema transakcije za izvoz.",
                        "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            double totalIncome = 0;
            double totalExpense = 0;

            Map<String, Double> byCategory = new HashMap<>();

            for (Transaction t : list) {
                if ("Prihod".equals(t.getType())) {
                    totalIncome += t.getAmount();
                } else if ("Rashod".equals(t.getType())) {
                    totalExpense += t.getAmount();
                }
                String cat = t.getCategory();
                if (cat == null || cat.isEmpty()) {
                    cat = "Ostalo";
                }
                byCategory.put(cat, byCategory.getOrDefault(cat, 0.0) + t.getAmount());
            }
            double balance = totalIncome - totalExpense;

            try (FileWriter fw = new FileWriter("izvjestaj.txt")) {
                fw.write("Ukupni prihod:" + totalIncome + "\n");
                fw.write("Ukupni rashod:" + totalExpense + "\n");
                fw.write("Stanje:" + balance + "\n");
                fw.write("\n");

                fw.write("Suma po kategorijama:\n");

                String[] categories = {"Plata", "Hrana", "Racuni", "Zabava", "Prijevoz", "Ostalo"};

                for (String cat : categories) {
                    double sum = byCategory.getOrDefault(cat, 0.0);
                    fw.write(cat + ":" + sum + "\n");
                }

                for (String cat : byCategory.keySet()) {
                    boolean alreadyListed = false;
                    for (String c : categories) {
                        if (c.equals(cat)) {
                            alreadyListed = true;
                            break;
                        }
                    }
                    if (!alreadyListed) {
                        fw.write(cat + ":" + byCategory.get(cat) + "\n");
                    }
                }

            } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainPanel,
                            "Greška pri pisanju fajla!" + ex.getMessage(),
                            "Greška",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(mainPanel,
                        "Izvještaj je sačuvan kao 'izvjestaj.txt' (u folderu projekta).",
                        "Gotovo",
                        JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void loadDataIntoTable() {
        ArrayList<Transaction> list = manager.getAllTransactions();

        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Vrsta");
        model.addColumn("Iznos");
        model.addColumn("Opis");
        model.addColumn("Kategorija");

        for (Transaction t : list) {
            model.addRow(new Object[]{
                    t.getType(),
                    t.getAmount(),
                    t.getDescription(),
                    t.getCategory()
            });
        }
        transactionTable.setModel(model);
    }

    private void updateSummary() {
        double income = manager.getTotalIncome();
        double expence = manager.getTotalExpense();
        double balance = income - expence;

        incomeLabel.setText("Prihod: " + income);
        expenseLabel.setText("Rashod: " + expence);
        balanceLabel.setText("Stanje: " + balance);
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}

