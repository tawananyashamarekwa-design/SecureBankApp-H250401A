package bank.model;
import java.math.BigDecimal;



public class SavingsAccount extends Account {

public static final BigDecimal MINIMUM_BAL = new BigDecimal ("10.00");
public SavingsAccount(String accountNumber , String ownerUsername ,BigDecimal openingBalance){
    super (accountNumber , ownerUsername , openingBalance);
}

@Override 
public String getType(){return "Savings"; }

@Override 

public void withdraw(BigDecimal amount){
requirePositiveAmount(amount);
BigDecimal remaining = getBalance().subtract(amount);
if (remaining.compareTo(MINIMUM_BAL)<0){

    throw new IllegalArgumentException (
"Savings accounts must keep a minimum balance of $ " + MINIMUM_BAL);
}

super.withdraw(amount);
}

}