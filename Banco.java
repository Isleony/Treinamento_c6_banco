package banco;

public class Banco {

    String titular = "admin";
    double saldo;
    int conta;

    void Conta(int conta, String titular, double saldoInicial) {
        this.titular = titular;
        this.conta = conta;
        this.saldo = saldoInicial;
    }

    void Depositar(double valor) {
        saldo += valor;
    }
    boolean Sacar(double valor){
        if(saldo >= valor ){
            saldo -= valor;
            return true;
        }
        return false;
    }
    
}





