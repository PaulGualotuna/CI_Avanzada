package ec.edu.espe.buildtestci.dto;

public class WalletResponse {
    private final String walletId;
    private final double balance;

    //generamos el constructor

    public WalletResponse(String walletId, double balance) {
        this.walletId = walletId;
        this.balance = balance;
    }


    //Getters de todos
    public String getWalletId() {
        return walletId;
    }

    public double getBalance() {
        return balance;
    }
}
