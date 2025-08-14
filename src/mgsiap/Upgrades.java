package mgsiap;

public class Upgrades {

    public static String VERSION = "1.250814.1818";

    public Upgrades() {
    }

    public String upgrades() {
        StringBuilder sb = new StringBuilder();
        // sb.append(" " + "\n");
        // sb.append(" " + "\n");
        sb.append("20 de março de 2025: v. " + VERSION + "\n");
        sb.append("      1. Operações e correções gerais no sistema" + "\n");
        sb.append("      2. Validações XSD aprimoradas" + "\n");
        sb.append("      3. Melhorias na estrutura de dados" + "\n");
        sb.append("20 de março de 2025: v. 1.250320.1752\n");
        sb.append("      1. Correção da informação vazia da data de início das funções gratificadas" + "\n");
        sb.append("11 de março de 2024: v. 0.04\n");
        sb.append("      1. Correções nos leiautes" + "\n");
        sb.append("      2. Criação do leiaute VinculosRPPS" + "\n");
        sb.append("07 de março de 2024: v. 0.03\n");
        sb.append("      1. Novo leiaute RPPS" + "\n");
        
        return sb.toString();
    }
}
