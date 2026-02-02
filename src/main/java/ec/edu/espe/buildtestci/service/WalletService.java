package ec.edu.espe.buildtestci.service;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;

import java.util.Optional;


public class WalletService {

    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    //Generamos el constructor
    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    //Generamos dos validaciones
    //Crear una cuenta si cumple con las reglas del negocio
    public WalletResponse createWallet(String ownerEmail, double initialBalance){
        //validaciones de casos negativos o sin datos
        if (ownerEmail == null || ownerEmail.isEmpty() || !ownerEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        //Regla de negocio: usuario bloqueado
        if(riskClient.isBloqued(ownerEmail)){
            throw new IllegalStateException("User blocked");
        }

        //Regla de negocio: no duplicar cuenta por email
        if(walletRepository.existsByOwnerEmail(ownerEmail)) {
            throw new IllegalStateException("Wallet already exists");
        }

        //Si pasa todas las validaciones, se crea la cuenta
        Wallet wallet = new Wallet(ownerEmail, initialBalance);
        Wallet save = walletRepository.save(wallet);

        return new WalletResponse(save.getId(), save.getBalance());
    }

    //Depositar dinero en la cuenta
    public double deposit(String walletId, double amount){
        //validaciones
        if(amount < 0){
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        Optional<Wallet> found = walletRepository.findById(walletId);
        if(found.isEmpty()){
            throw new IllegalStateException("Wallet not found");
        }

        Wallet wallet = found.get();
        wallet.deposit(amount);

        //Persistimos el nuevo saldo
        walletRepository.save(wallet);
        return wallet.getBalance();

    }

}
