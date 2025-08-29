import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Conta {
    private final String usuario;
    private String senha;
    private double saldo;
    private final List<String> historicoTransferencias = new ArrayList<>();

    private static final int OP_VER_SALDO = 1;
    private static final int OP_DEPOSITAR = 2;
    private static final int OP_SACAR = 3;
    private static final int OP_TRANSFERIR = 4;
    private static final int OP_HISTORICO = 5;
    private static final int OP_ALTERAR_SENHA = 6;
    private static final int OP_LOGOUT = 7;

    public Conta(String usuario, String senha, double saldoInicial) {
        this.usuario = Objects.requireNonNull(usuario);
        this.senha = Objects.requireNonNull(senha);
        this.saldo = saldoInicial;
    }

    public String getUsuario() {
        return usuario;
    }

    public double getSaldo() {
        return saldo;
    }

    public boolean autenticar(String usuario, String senha) {
        return this.usuario.equals(usuario) && this.senha.equals(senha);
    }

    public void alterarSenha(String senhaAtual, String novaSenha) {
        if (!this.senha.equals(senhaAtual)) {
            throw new SecurityException("Senha atual inválida.");
        }
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new IllegalArgumentException("Nova senha não pode ser vazia.");
        }
        this.senha = novaSenha;
    }

    public void depositar(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor de depósito deve ser positivo.");
        }
        this.saldo += valor;
    }

    public void sacar(double valor) {
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor de saque deve ser positivo.");
        }
        if (valor > this.saldo) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
        this.saldo -= valor;
    }

    public void transferir(Conta destino, double valor) {
        if (destino == null) {
            throw new IllegalArgumentException("Conta de destino inválida.");
        }
        if (destino == this) {
            throw new IllegalArgumentException("Não é possível transferir para a mesma conta.");
        }
        if (valor <= 0) {
            throw new IllegalArgumentException("Valor de transferência deve ser positivo.");
        }
        if (valor > this.saldo) {
            throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        }
        this.saldo -= valor;
        destino.saldo += valor;
        String registroSaida = String.format("Enviado para %s: R$ %.2f (saldo: R$ %.2f)", destino.usuario, valor, this.saldo);
        String registroEntrada = String.format("Recebido de %s: R$ %.2f (saldo: R$ %.2f)", this.usuario, valor, destino.saldo);
        this.historicoTransferencias.add(registroSaida);
        destino.historicoTransferencias.add(registroEntrada);
    }

    public void imprimirHistorico() {
        if (historicoTransferencias.isEmpty()) {
            System.out.println("Não há transferências no histórico.");
            return;
        }
        System.out.println("=== Histórico de Transferências ===");
        for (String r : historicoTransferencias) {
            System.out.println("- " + r);
        }
    }

    public static Conta[] criarContasIniciais() {
        return new Conta[] {
                new Conta("ana", "ana123", 1000.0),
                new Conta("bruno", "bru456", 750.0),
                new Conta("carla", "car789", 1500.0),
                new Conta("diego", "dig012", 200.0),
                new Conta("erika", "eri345", 3000.0)
        };
    }

    public static Conta encontrarPorUsuario(Conta[] contas, String usuario) {
        if (contas == null || usuario == null) return null;
        for (Conta c : contas) {
            if (c != null && usuario.equals(c.usuario)) {
                return c;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        try (Scanner input = new Scanner(System.in)) {
            Conta[] contas = criarContasIniciais();
            Conta contaLogada = null;
            boolean executando = true;

            while (executando) {
                if (contaLogada == null) {
                    contaLogada = realizarLogin(input, contas);
                    if (contaLogada == null) {
                        break; // usuário digitou 'sair'
                    }
                    System.out.println("Login realizado com sucesso!");
                }

                exibirMenu();
                try {
                    int opcao = lerInteiro(input, "Escolha uma opção: ");
                    switch (opcao) {
                        case OP_VER_SALDO -> System.out.printf("Saldo atual: R$ %.2f%n", contaLogada.getSaldo());
                        case OP_DEPOSITAR -> {
                            double valor = lerDouble(input, "Valor para depósito: R$ ");
                            contaLogada.depositar(valor);
                            System.out.println("Depósito realizado com sucesso!");
                        }
                        case OP_SACAR -> {
                            double valor = lerDouble(input, "Valor para saque: R$ ");
                            contaLogada.sacar(valor);
                            System.out.println("Saque realizado com sucesso!");
                        }
                        case OP_TRANSFERIR -> {
                            String usuarioDestino = lerLinha(input, "Usuário destino: ");
                            Conta destino = encontrarPorUsuario(contas, usuarioDestino);
                            if (destino == null) {
                                System.out.println("Conta destino não encontrada.");
                                break;
                            }
                            double valor = lerDouble(input, "Valor para transferência: R$ ");
                            contaLogada.transferir(destino, valor);
                            System.out.println("Transferência realizada com sucesso!");
                        }
                        case OP_HISTORICO -> contaLogada.imprimirHistorico();
                        case OP_ALTERAR_SENHA -> {
                            String atual = lerLinha(input, "Senha atual: ");
                            String nova = lerLinha(input, "Nova senha: ");
                            contaLogada.alterarSenha(atual, nova);
                            System.out.println("Senha alterada com sucesso!");
                        }
                        case OP_LOGOUT -> {
                            contaLogada = null;
                            System.out.println("Logout realizado com sucesso!");
                        }
                        default -> System.out.println("Opção inválida!");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Entrada inválida! Digite um número.");
                } catch (IllegalArgumentException | SecurityException e) {
                    System.out.println("Erro: " + e.getMessage());
                }
            }

            System.out.println("Sistema encerrado.");
        }
    }

    private static Conta realizarLogin(Scanner input, Conta[] contas) {
        while (true) {
            System.out.println("\n=== LOGIN ===");
            String usuario = lerLinha(input, "Usuário (ou 'sair' para encerrar): ");
            if ("sair".equalsIgnoreCase(usuario)) return null;
            String senha = lerLinha(input, "Senha: ");

            Conta conta = encontrarPorUsuario(contas, usuario);
            if (conta != null && conta.autenticar(usuario, senha)) {
                return conta;
            }
            System.out.println("Usuário ou senha incorretos!");
        }
    }

    private static void exibirMenu() {
        System.out.println("\n=== MENU ===");
        System.out.println(OP_VER_SALDO + " - Ver saldo");
        System.out.println(OP_DEPOSITAR + " - Depositar");
        System.out.println(OP_SACAR + " - Sacar");
        System.out.println(OP_TRANSFERIR + " - Transferir");
        System.out.println(OP_HISTORICO + " - Ver histórico de transferências");
        System.out.println(OP_ALTERAR_SENHA + " - Alterar senha");
        System.out.println(OP_LOGOUT + " - Logout");
    }

    private static String lerLinha(Scanner input, String prompt) {
        System.out.print(prompt);
        return input.nextLine();
    }

    private static int lerInteiro(Scanner input, String prompt) {
        System.out.print(prompt);
        return Integer.parseInt(input.nextLine());
    }

    private static double lerDouble(Scanner input, String prompt) {
        System.out.print(prompt);
        return Double.parseDouble(input.nextLine());
    }
}
