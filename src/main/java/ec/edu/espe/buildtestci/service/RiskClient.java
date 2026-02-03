package ec.edu.espe.buildtestci.service;

public interface RiskClient {
    //Verificar si el usuario esta bloqueado
    boolean isBloqued(String ownerEmail);
}
