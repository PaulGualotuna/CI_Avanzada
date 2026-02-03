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
        //email debe contener @
        if (ownerEmail == null || ownerEmail.isEmpty() || !ownerEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
        //Saldo inicial debe ser mayor a 0
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        //Regla de negocio: usuario bloqueado
        //Rechazar cliente bloqueado (servicio externo)
        if(riskClient.isBloqued(ownerEmail)){
            throw new IllegalStateException("User blocked");
        }

        //Regla de negocio: no duplicar cuenta por email
        //Rechazar si ya existe una cuenta con ese email
        if(walletRepository.existsByOwnerEmail(ownerEmail)) {
            throw new IllegalStateException("Wallet already exists");
        }

        //Si pasa todas las validaciones, se crea la cuenta
        Wallet wallet = new Wallet(ownerEmail, initialBalance);
        //Guardar en repositorio
        Wallet save = walletRepository.save(wallet);

        //Retornar un DTO con id y balance
        return new WalletResponse(save.getId(), save.getBalance());
    }

    //Depositar dinero en la cuenta
    public double deposit(String walletId, double amount){
        //validaciones
        //El monto debe ser mayor a 0
        if(amount < 0){
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        //La cuenta debe existir
        Optional<Wallet> found = walletRepository.findById(walletId);
        if(found.isEmpty()){
            throw new IllegalStateException("Wallet not found");
        }

        //Usar la función deposit del modelo
        Wallet wallet = found.get();
        wallet.deposit(amount);

        //Persistimos el nuevo saldo
        walletRepository.save(wallet);
        //Retornar el balance
        return wallet.getBalance();

    }


    //AGREGADO LUEGO DE HACER LO DEL GIT
    //Retiro de dinero
    public double withdraw(String walletId, double amount){
        //validaciones
        //amount debe ser mayor a 0
        if(amount <= 0){
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        //Wallet debe existir
        Wallet wallet = walletRepository.findById(walletId).orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        //saldo suficiente(si no, lanzar excepción)
        if(wallet.getBalance() < amount){
            throw new IllegalStateException("Insufficient funds");
        }

        //Actualizar saldo y guardar
        wallet.withdraw(amount);
        walletRepository.save(wallet);
        return wallet.getBalance();
    }

}
