package financeapp;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;

public class TransactionManager {
    private final MongoCollection<Document> collection;

    public TransactionManager() {
        MongoDatabase db = MongoDBConnection.getDatabase();
        collection = db.getCollection("transactions");
    }

    public void addTransaction(Transaction t) {
        collection.insertOne(t.toDocument());
    }

    public ArrayList<Transaction> getAllTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();
        MongoCursor<Document> cursor = collection.find().iterator();

        try {
            while (cursor.hasNext()) {
                Document d = cursor.next();
                list.add(new Transaction(
                        d.getObjectId("_id"),
                        d.getString("Vrsta"),
                        d.getDouble("Iznos"),
                        d.getString("Opis"),
                        d.getString("Kategorija")
                ));
            }
        } finally {
            cursor.close();
        }
        return list;
    }

    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : getAllTransactions()) {
            if (t.getType().equals("Prihod")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public double getTotalExpense() {
        double total = 0;
        for (Transaction t : getAllTransactions()) {
            if (t.getType().equals("Rashod")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public void updateTransaction(ObjectId id, String type, double amount, String description, String category) {
        Document filter = new Document("_id", id);

        Document updateFields = new Document("Vrsta", type)
                .append("Iznos", amount)
                .append("Opis", description)
                .append("Kategorija", category);
        Document update = new Document("$set", updateFields);

        collection.updateOne(filter, update);
    }

    public void deleteTransaction(ObjectId id) {
        Document filter = new Document("_id", id);
        collection.deleteOne(filter);
    }
}

