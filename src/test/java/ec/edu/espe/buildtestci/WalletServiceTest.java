package ec.edu.espe.buildtestci;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;
import ec.edu.espe.buildtestci.service.RiskClient;
import ec.edu.espe.buildtestci.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletServiceTest {

    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    public void setUp() {
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    //Crear cuenta con datos válidos, guardar y retornar respuesta
    @Test
    void createWallet_validData_shouldSaveAndReturnResponse() {
        //Arrange
        String email = "paul@espe.edu.ec";
        double initial = 100;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(Boolean.FALSE);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        //Act
        WalletResponse response = walletService.createWallet(email, initial);


        //Assert
        assertNotNull(response.getWalletId());
        assertEquals(100.0, response.getBalance());

        verify(riskClient).isBloqued(email);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).existsByOwnerEmail(email);
    }

    //Crear cuenta con correo no válido, lanzar excepción y no llamar dependencias
    @Test
    void createWallet_invalidEmail_shouldThrow_andNotCallDependencies() {
        //Arrange
        String invalidEmail = "paulespe.edu.ec";

        //Act + Assert
        assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(invalidEmail,
                50.0));

        //No debe llamar a ninguna dependencia porque falla la validacion
        verifyNoInteractions(walletRepository, riskClient);

    }

    //Depositar a una cuenta que no se encuentra y lanzar excepción
    @Test
    void deposit_walletNotFound_shouldThrow() {
        // Arrange
        String walletId = "no-exist-wallet";


        when(walletRepository.existsByOwnerEmail(walletId)).thenReturn(Optional.empty().isEmpty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> walletService
        .deposit(walletId, 60));

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any(Wallet.class));

    }

    //Depositar a una cuenta, actualizar balance, guardar y usar captor
    @Test void deposit_shouldUpdateBalance_andSave_usingCaptor(){
        //Arrange
        Wallet wallet = new Wallet("paul@espe.edu.ec", 300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()
                [0]);

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        //Act
        double newBalance = walletService.deposit(walletId, 300.0);

        //Assert
        assertEquals(600.0, newBalance);

        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();
        assertEquals(600.0, saved.getBalance());
    }


    //AGREGADO LUEGO DE HACER LO DEL GIT
    //Retirar de una cuenta con fondos insuficientes, lanzar excepción y no guardar
    @Test
    void withdraw_insufficientFunds_shouldThrow_andNotSave() {
        // Arrange
        Wallet wallet = new Wallet("paul@espe.edu.ec", 300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> walletService
                .withdraw(walletId, 500.0));
        assertEquals("Insufficient funds", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));

    }

    //
    //Retirar de una cuenta exitoso, actualizar saldo y guardarr
    //
    @Test
    void withdraw_validData_shouldUpdateBalance_andSave() {
        // Arrange
        Wallet wallet = new Wallet("paul@espe.edu.ec", 800.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Wallet> captor = ArgumentCaptor.forClass(Wallet.class);

        // Act
        double newBalance = walletService.withdraw(walletId, 300.0);

        // Assert
        assertEquals(500.0, newBalance);

        verify(walletRepository).save(captor.capture());
        Wallet saved = captor.getValue();
        assertEquals(500.0, saved.getBalance());
        }
}