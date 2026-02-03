package ec.edu.espe.buildtestci.repository;

import ec.edu.espe.buildtestci.model.Wallet;

import java.util.Optional;

public interface WalletRepository {
    //Guardar una cuenta
    Wallet save (Wallet wallet);

    //Buscar una cuenta por id
    Optional<Wallet> findById(String id);

    //Verificar si existe una cuenta por email
    boolean existsByOwnerEmail(String ownerEmail);
}
