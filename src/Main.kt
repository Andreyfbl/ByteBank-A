// Sistema Bancário ByteBank Evolution

// Classe base
open class Conta(
    val cliente: String,
    val numero: Int,
    saldoInicial: Double
) {

    // protected: as subclasses (ContaCorrente, ContaPoupanca, ContaSalario)
    // conseguem LER o saldo, mas não escrever direto nele — "private set"
    // trava a escrita só pra dentro desta classe. Toda alteração de saldo
    // passa por creditar()/debitar(), que é o único lugar que decide se
    // a operação é válida (garante a regra de não ficar negativo).
    protected var saldo: Double = saldoInicial
        private set

    protected val historico = mutableListOf<String>()

    init {
        require(saldoInicial >= 0) {
            "O saldo inicial não pode ser negativo."
        }
    }

    fun consultarSaldo(): Double {
        return saldo
    }

    /** Registra uma operação no histórico. Usado pelas subclasses também. */
    protected fun registrar(descricao: String) {
        historico.add(descricao)
    }

    /** Soma ao saldo. Só deve ser chamado com valor positivo. */
    protected fun creditar(valor: Double) {
        saldo += valor
    }

    /** Subtrai do saldo. Retorna false (sem alterar nada) se faltar saldo. */
    protected fun debitar(valor: Double): Boolean {
        if (valor <= 0 || valor > saldo) return false
        saldo -= valor
        return true
    }

    open fun depositar(valor: Double): Boolean {
        if (valor <= 0) {
            println("Depósito inválido!")
            return false
        }
        creditar(valor)
        registrar("Depósito de R$ %.2f realizado.".format(valor))
        println("Depósito realizado com sucesso!")
        return true
    }

    open fun sacar(valor: Double): Boolean {
        if (valor <= 0) {
            println("Valor de saque inválido!")
            return false
        }
        if (!debitar(valor)) {
            println("Saldo insuficiente para realizar esta operação!")
            return false
        }
        registrar("Saque de R$ %.2f realizado.".format(valor))
        println("Saque realizado com sucesso!")
        return true
    }

    open fun exibirDados() {
        println("Cliente: $cliente")
        println("Conta: $numero")
        println("Saldo Atual: R$ %.2f".format(saldo))
    }

    fun exibirHistorico() {
        println("====================================")
        println("HISTÓRICO DE OPERAÇÕES")
        println("====================================")
        historico.forEach { println(it) }
    }
}

// =====================================================================
// Sistema Bancário ByteBank Evolution
// Parte: CONTAS ESPECIALIZADAS (herança, sobrescrita e polimorfismo)
// Autor: [SEU NOME]
// =====================================================================
// Depende da classe base Conta (ver CONTRATO-CLASSE-BASE.md).
// Membros usados da base:
//   protected fun creditar(valor: Double)
//   protected fun debitar(valor: Double): Boolean
//   protected fun registrar(descricao: String)
//   open fun sacar(valor: Double): Boolean
//   open fun exibirDados()
// =====================================================================

/**
 * Conta Corrente: cobra taxa por saque e taxa de manutenção mensal.
 */
class ContaCorrente(
    cliente: String,
    numero: Int,
    saldoInicial: Double,
    private val taxaPorSaque: Double = 0.50,
    private val taxaMensal: Double = 30.0
) : Conta(cliente, numero, saldoInicial) {

    override fun sacar(valor: Double): Boolean {
        if (valor <= 0) {
            println("Valor de saque inválido!")
            return false
        }

        val valorTotal = valor + taxaPorSaque

        if (!debitar(valorTotal)) {
            println("Saldo insuficiente para realizar esta operação!")
            return false
        }

        registrar("Saque de R$ %.2f realizado (taxa de R$ %.2f).".format(valor, taxaPorSaque))
        println("Saque realizado com sucesso!")
        return true
    }

    /**
     * Cobra a taxa fixa de manutenção da conta.
     */
    fun aplicarTaxaMensal(): Boolean {
        println("Aplicando taxa mensal...")

        if (!debitar(taxaMensal)) {
            println("Saldo insuficiente para cobrar a taxa mensal!")
            return false
        }

        registrar("Taxa mensal de R$ %.2f cobrada.".format(taxaMensal))
        println("Novo saldo: R$ %.2f".format(consultarSaldo()))
        return true
    }

    override fun exibirDados() {
        super.exibirDados()
        println("Tipo: Conta Corrente")
        println("Taxa mensal: R$ %.2f".format(taxaMensal))
    }
}

/**
 * Conta Poupança: não cobra taxa de saque e possui rendimento mensal.
 */
class ContaPoupanca(
    cliente: String,
    numero: Int,
    saldoInicial: Double,
    private val taxaRendimento: Double = 0.005 // 0,5% ao mês
) : Conta(cliente, numero, saldoInicial) {

    /**
     * Aplica o rendimento sobre o saldo atual.
     */
    fun aplicarRendimento(): Double {
        val rendimento = consultarSaldo() * taxaRendimento

        if (rendimento <= 0) {
            println("Sem saldo para render neste mês.")
            return 0.0
        }

        creditar(rendimento)
        registrar("Rendimento de R$ %.2f creditado.".format(rendimento))
        println("Rendimento aplicado: R$ %.2f".format(rendimento))
        println("Novo saldo: R$ %.2f".format(consultarSaldo()))
        return rendimento
    }

    override fun exibirDados() {
        super.exibirDados()
        println("Tipo: Conta Poupança")
        println("Rendimento mensal: %.2f%%".format(taxaRendimento * 100))
    }
}

/**
 * Conta Salário: possui limite de saques gratuitos por mês.
 * Demonstra que o sistema aceita novos tipos de conta sem alterar a base.
 */
class ContaSalario(
    cliente: String,
    numero: Int,
    saldoInicial: Double,
    private val saquesGratuitos: Int = 3,
    private val taxaSaqueExtra: Double = 2.00
) : Conta(cliente, numero, saldoInicial) {

    private var saquesRealizados: Int = 0

    override fun sacar(valor: Double): Boolean {
        if (valor <= 0) {
            println("Valor de saque inválido!")
            return false
        }

        val taxa = if (saquesRealizados >= saquesGratuitos) taxaSaqueExtra else 0.0

        if (!debitar(valor + taxa)) {
            println("Saldo insuficiente para realizar esta operação!")
            return false
        }

        saquesRealizados++
        registrar("Saque de R$ %.2f realizado (taxa de R$ %.2f).".format(valor, taxa))
        println("Saque realizado com sucesso!")

        if (taxa > 0) {
            println("Limite de saques gratuitos excedido. Taxa de R$ %.2f cobrada.".format(taxa))
        }
        return true
    }

    fun reiniciarCicloMensal() {
        saquesRealizados = 0
        registrar("Ciclo mensal reiniciado: saques gratuitos liberados.")
    }

    override fun exibirDados() {
        super.exibirDados()
        println("Tipo: Conta Salário")
        println("Saques gratuitos restantes: ${maxOf(0, saquesGratuitos - saquesRealizados)}")
    }
}

fun main(args: Array<out String>) {

    // Criando as contas
    val contaCorrente = ContaCorrente(
        cliente = "Andrey",
        numero = 1001,
        saldoInicial = 1000.0
    )

    val contaPoupanca = ContaPoupanca(
        cliente = "Maria",
        numero = 1002,
        saldoInicial = 2000.0
    )

    val contaSalario = ContaSalario(
        cliente = "João",
        numero = 1003,
        saldoInicial = 1500.0
    )

    var opcao: Int

    do {
        println()
        println("====================================")
        println("       BYTEBANK EVOLUTION")
        println("====================================")
        println("1 - Exibir Conta Corrente")
        println("2 - Exibir Conta Poupança")
        println("3 - Exibir Conta Salário")
        println("4 - Depositar")
        println("5 - Sacar")
        println("6 - Aplicar taxa mensal")
        println("7 - Aplicar rendimento da poupança")
        println("8 - Reiniciar ciclo da conta salário")
        println("9 - Consultar históricos")
        println("0 - Sair")
        println("====================================")
        print("Escolha uma opção: ")

        opcao = readln().toIntOrNull() ?: -1

        when (opcao) {

            1 -> {
                println()
                println("----- CONTA CORRENTE -----")
                contaCorrente.exibirDados()
            }

            2 -> {
                println()
                println("----- CONTA POUPANÇA -----")
                contaPoupanca.exibirDados()
            }

            3 -> {
                println()
                println("----- CONTA SALÁRIO -----")
                contaSalario.exibirDados()
            }

            4 -> {
                println()
                println("Escolha a conta para depósito:")
                println("1 - Conta Corrente")
                println("2 - Conta Poupança")
                println("3 - Conta Salário")
                print("Opção: ")

                val conta = readln().toIntOrNull()

                print("Digite o valor do depósito: R$ ")
                val valor = readln().toDoubleOrNull()

                if (valor == null) {
                    println("Valor inválido!")
                } else {
                    when (conta) {
                        1 -> contaCorrente.depositar(valor)
                        2 -> contaPoupanca.depositar(valor)
                        3 -> contaSalario.depositar(valor)
                        else -> println("Conta inválida!")
                    }
                }
            }

            5 -> {
                println()
                println("Escolha a conta para saque:")
                println("1 - Conta Corrente")
                println("2 - Conta Poupança")
                println("3 - Conta Salário")
                print("Opção: ")

                val conta = readln().toIntOrNull()

                print("Digite o valor do saque: R$ ")
                val valor = readln().toDoubleOrNull()

                if (valor == null) {
                    println("Valor inválido!")
                } else {
                    when (conta) {
                        1 -> contaCorrente.sacar(valor)
                        2 -> contaPoupanca.sacar(valor)
                        3 -> contaSalario.sacar(valor)
                        else -> println("Conta inválida!")
                    }
                }
            }

            6 -> {
                println()
                println("----- TAXA MENSAL -----")
                contaCorrente.aplicarTaxaMensal()
            }

            7 -> {
                println()
                println("----- RENDIMENTO DA POUPANÇA -----")
                contaPoupanca.aplicarRendimento()
            }

            8 -> {
                println()
                println("----- CONTA SALÁRIO -----")
                contaSalario.reiniciarCicloMensal()
                println("Ciclo mensal reiniciado com sucesso!")
            }

            9 -> {
                println()
                println("Escolha a conta:")
                println("1 - Conta Corrente")
                println("2 - Conta Poupança")
                println("3 - Conta Salário")
                print("Opção: ")

                when (readln().toIntOrNull()) {

                    1 -> {
                        println()
                        println("HISTÓRICO - CONTA CORRENTE")
                        contaCorrente.exibirHistorico()
                    }

                    2 -> {
                        println()
                        println("HISTÓRICO - CONTA POUPANÇA")
                        contaPoupanca.exibirHistorico()
                    }

                    3 -> {
                        println()
                        println("HISTÓRICO - CONTA SALÁRIO")
                        contaSalario.exibirHistorico()
                    }

                    else -> println("Conta inválida!")
                }
            }

            0 -> {
                println()
                println("Encerrando o ByteBank Evolution...")
                println("Obrigado por utilizar o sistema!")
            }

            else -> {
                println()
                println("Opção inválida!")
            }
        }

    } while (opcao != 0)
}
