package financeapp;

import org.bson.Document;
import org.bson.types.ObjectId;

public class Transaction {
    private ObjectId id;
    private String type;
    private double amount;
    private String description;
    private String category;

    public Transaction(String type, double amount, String description, String category) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    public Transaction(ObjectId id, String type, double amount, String description, String category) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    public Transaction(String type, double amount, String description) {
        this(type, amount, description, "Ostalo");
    }

    public ObjectId getId() {
        return id;
    }

    public Document toDocument() {
        return new Document("Vrsta", type)
                .append("Iznos", amount)
                .append("Opis", description)
                .append("Kategorija", category);
    }

    public String getType() { return type; }
    public double getAmount() { return amount;}
    public String getDescription() { return description; }
    public String getCategory() { return category; }
}
