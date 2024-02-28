package mgsiap;

public class Upgrades {

    public Upgrades() {
    }

    public String upgrades() {
        StringBuilder sb = new StringBuilder();
        // sb.append(" " + "\n");
        sb.append("26 de fevereiro de 2024: v. " + MGSiapRPPS.VERSION + "\n");
        sb.append("      1. Novo leiaute" + "\n");
        sb.append("      2. Exclusão dos leiautes do RPPS do envio habitual" + "\n");
        sb.append("27 de julho de 2023: v. " + MGSiapRPPS.VERSION + "\n");
        sb.append("      1. Nova correção na geração do arquivo Servidores.xml" + "\n");
        sb.append("26 de julho de 2023: v. 0.48" + "\n");
        sb.append("      1. Nova correção na geração do arquivo Servidores.xml" + "\n");
        sb.append("26 de julho de 2023: v. 0.47" + "\n");
        sb.append("      1. Correção na geração do arquivo Servidores.xml" + "\n");
        sb.append("18 de junho de 2023: v. 0.46" + "\n");
        sb.append("      1. Correção na geração do arquivo ProgressaoCargo.xml" + "\n");
        sb.append("14 de março de 2023: v. 0.45" + "\n");
        sb.append("      1. Correção na geração do arquivo Cessão.xml" + "\n");
        sb.append("27 de fevereiro de 2023: v. 0.44" + "\n");
        sb.append("      1. Correção na validação nos dados das aposentadorias que passaram a ser obrigatórios" + "\n");
        sb.append("27 de fevereiro de 2023: v. 0.43" + "\n");
        sb.append("      1. Validação nos dados das aposentadorias que passaram a ser obrigatórios" + "\n");
        sb.append("27 de fevereiro de 2023: v. 0.42" + "\n");
        sb.append("      1. Melhoria na geração do leiaute Dependentes" + "\n");
        sb.append("23 de fevereiro de 2023: v. 0.41" + "\n");
        sb.append("      1. Correção na geração do leiaute Pensão" + "\n");
        sb.append("17 de fevereiro de 2023: v. 0.40" + "\n");
        sb.append("      1. Melhoria na geração do leiaute de cargos" + "\n");
        sb.append("13 de fevereiro de 2023: v. 0.39\n");
        sb.append("      1. Correção em diversos leiautes para adaptação às novas exigências para 2023" + "\n");
        sb.append("30 de junho de 2022: v. 0.38" + "\n");
        sb.append("      1. Melhoria na validação do CBO do cargo para o arquivo Vinculo.xml" + "\n");
        sb.append("30 de junho de 2022: v. 0.37" + "\n");
        sb.append("      1. Correção na validação da escolaridade para o arquivo Servidores.xml" + "\n");
        sb.append("29 de junho de 2022: v. 0.36" + "\n");
        sb.append("      1. Acrescentado um contador na quantidade de dígitos do título" + "\n");
        sb.append("29 de junho de 2022: v. 0.35" + "\n");
        sb.append("      1. Correção na validação do estado civil" + "\n");
        sb.append("28 de junho de 2022: v. 0.34" + "\n");
        sb.append("      1. Correção na informação da carga horária em Vinculos.xml" + "\n");
        sb.append("28 de junho de 2022: v. 0.33" + "\n");
        sb.append("      1. Validação dos números telefônicos do servidor" + "\n");
        sb.append("      2. Validação do estado civil" + "\n");
        sb.append("28 de junho de 2022: v. 0.32" + "\n");
        sb.append("      1. Correção de um bug na geração do arquivo Servidores.xml" + "\n");
        sb.append("      2. Validação do código Cardug antes de iniciar a geração dos arquivos" + "\n");
        sb.append("28 de junho de 2022: v. 0.31" + "\n");
        sb.append("      1. Correção de um bug na geração do arquivo Aposentadoria.xml" + "\n");
        sb.append("28 de junho de 2022: v. 0.30" + "\n");
        sb.append("      1. Arquivo log_avisos... renomeado como log_erros_que_nao_impedem_a_transmissao_..." + "\n");
        sb.append("27 de junho de 2022: v. 0.29" + "\n");
        sb.append("      1. Correção na informação da escolaridade" + "\n");
        sb.append("27 de junho de 2022: v. 0.28" + "\n");
        sb.append(
                "      1. Desobrigação de alguns dados: Número PISPasep, Número do Titulo Eleitoral e seus dados auxiliares, Escolaridade e Data do Óbito para pensionistas"
                        + "\n");
        sb.append(
                "      2. Melhoria na informação do campo CodigoCargo em diversos lugares removendo um espaço indesejado no meio do valor"
                        + "\n");
        sb.append("23 de junho de 2022: v. 0.27" + "\n");
        sb.append(
                "      1. Com a inclusão do vínculo empregatício 11-Pensão Alimentícia, registros com esse vínculo deixam de ser incluídos no lote Servidores.XML"
                        + "\n");
        sb.append("23 de junho de 2022: v. 0.26" + "\n");
        sb.append("      1. Melhoria no código de validação do PIS" + "\n");
        sb.append("21 de junho de 2022: v. 0.25" + "\n");
        sb.append("      1. Validação dos número de telefone fixo e/ou celular" + "\n");
        sb.append("      2. Exigência dos títulos eleitorais e seus dados auxiliares" + "\n");
        sb.append("      3. Validação do PIS" + "\n");
        sb.append(
                "      4. Pasta de armazenamento dos arquivos XML gerados com a seguinte formatação ['nome_cardug_exercicio_mes']"
                        + "\n");
        sb.append("20 de junho de 2022: v. 0.24" + "\n");
        sb.append(
                "      1. Exclusão da validação de dados de servidores falecidos e sem pensionistas vinculados" + "\n");
        sb.append(
                "      2. Exclusão da validação de alguns dados desnecessários para registros de pensionistas" + "\n");
        sb.append("      3. Validação do título eleitoral do SIAP suspensa" + "\n");
        sb.append("14 de junho de 2022: v. 0.23" + "\n");
        sb.append("      1. Adição de uma aba contendo os avisos que não impedem o envio dos arquivos gerados" + "\n");
        sb.append("      2. Separação entre erros e avisos" + "\n");
        sb.append("      3. Revisão e adequação dos campos obrigatórios" + "\n");
        sb.append("      4. Geração dos arquivos XML não obrigatórios suspensa" + "\n");
        sb.append("10 de junho de 2022: v. 0.22" + "\n");
        sb.append("      1. Retirada a exigência dos dados de publicação das Rubricas" + "\n");
        sb.append("      2. Retirada a exigência dos dados de publicação dos Cargos" + "\n");
        sb.append("      3. Retirada a exigência dos dados de publicação dos Departamentos" + "\n");
        sb.append("      4. Retirada a exigência dos dados de publicação dos Servidores (Admissão e Rubricas)" + "\n");
        sb.append("10 de junho de 2022: v. 0.21" + "\n");
        sb.append("      1. Apenas dependentes do dentro do Prazo IRRF/SIAP necessitarão ter o CPF informado" + "\n");
        sb.append("      2. Continua obrigatório CPF diferentes para os servidores e seus dependentes" + "\n");
        sb.append("      3. Retirada a exigência dos dados de publicação do Órgão" + "\n");
        sb.append("      4. Retirada a exigência dos dados de publicação do PCC ( Carreira )" + "\n");
        return sb.toString();
    }
}
