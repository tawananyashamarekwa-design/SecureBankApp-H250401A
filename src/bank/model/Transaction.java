package bank.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



public enum Type {
    DEPOSIT, WITHDRAWAL
}
public final class Transaction {

private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

private final String accountNumber;
private final LocalDateTime timestamp;
private final BigDecimal amount;
private final BigDecimal balanceAfter;
private final Type type;

public Transaction(String accountNumber, LocalDateTime timestamp, BigDecimal amount, BigDecimal balanceAfter, Type type) {
    this.accountNumber = accountNumber;
    this.timestamp = timestamp;
    this.amount = amount;
    this.balanceAfter = balanceAfter;
    this.type = type;


}


public String getAccountNumber() {
    return accountNumber;
}



public String toFileLine() {
return FMT.format(timestamp) + "," + accountNumber + "," + type + "," + amount.toPlainString() + "," + balanceAfter.toPlainString();

}

public static Transaction fromFileLine(String line) {
String[] parts = line.split(",");
if (parts.length != 5) {
    throw new IllegalArgumentException("Invalid transaction line: " + line);

}
return new Transaction(
        parts[1],
        LocalDateTime.parse(parts[0], FMT),
        new BigDecimal(parts[3]),
        new BigDecimal(parts[4]),
        Type.valueOf(parts[2])
    );
}

    public String display(){

return String.format("%-20s %-11s $%-12s balance: $%s", FMT.format(timestamp), type, amount.toPlainString(), balanceAfter.toPlainString());


    }
}




