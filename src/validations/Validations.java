/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package validations;

import java.util.InputMismatchException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author TomMe
 */
public class Validations {

    // public static void main(String[] args) {
    // Validations validations = new Validations();
    // System.out.println(validations.isTituloOrError("394733890141"));
    // System.out.println(validations.isTituloOrError("188247300116"));
    // }

    public Validations() {
    }

    // public static void main(String[] args) {
    // System.out.println(new Validations().isPISOrError("119.83763.22.0"));
    // System.out.println(new Validations().isPISOrError("19006179879"));
    // System.out.println(new Validations().isPISOrError("19006179878"));
    // }

    /**
     * Usado para validar campos de valor não obrigatório. Não é possível
     * limitar o tamanho do retorno
     *
     * @param value
     * @return
     */
    public String isValueOrEmpty(String value) {
        if (value != null && !value.isEmpty()) {
            return isValueOrEmpty(value, 0, "L");
        } else {
            return "";
        }
    }

    /**
     * Usado para validar campos de valor não obrigatório.É possível limitar o
     * tamanho do retorno
     *
     * @param value
     * @param tamanho
     * @param Way
     * @return
     */
    public String isValueOrEmpty(String value, int tamanho, String Way) {
        if (value != null && !value.isEmpty()) {
            if (tamanho == 0) {
                tamanho = value.length();
            }
            if ("R".equals(Way)) {
                return StringUtils.right(value, tamanho);
            } else {
                return StringUtils.left(value, tamanho);
            }
        } else {
            return "";
        }
    }

    /**
     * Usado para validar campos de valor obrigatório
     *
     * @param value
     * @return
     */
    public Boolean isValueOrError(String value) {
        return value != null && !value.isEmpty() && value.trim().length() > 0;
    }

    /**
     * Usado para validar campos de valor não obrigatório.É possível limitar o
     * tamanho do retorno
     *
     * @param value
     * @param tamanho
     * @param Way
     * @return
     */
    public String isNumberOrEmpty(String value, int tamanho, String Way) {
        if (value == null) {
            return "";
        }
        value = value.replaceAll("[^0-9]", "");
        if (tamanho == 0) {
            tamanho = value.length();
        }
        if ("R".equals(Way)) {
            value = StringUtils.right(value, tamanho);
        } else {
            value = StringUtils.left(value, tamanho);
        }

        try {
            Long d = Long.parseLong(value);
            return d.toString();
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    /**
     * Usado para validar campos de valor obrigatório
     *
     * @param value
     * @return
     */
    public Boolean isNumberOrError(String value) {
        String val = isNumberOrEmpty(value, 0, "L");
        if (val == null) {
            return false;
        }
        try {
            Long d = Long.parseLong(val);
            return d instanceof Long;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * Usado para validar campos de valor decimais
     *
     * @param value
     * @return
     */
    public String isDecimalOrEmpty(String value) {
        if (value == null) {
            return "";
        }
        try {
            Double.parseDouble(value);
            return value;
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    /**
     * Usado para validar campos de valor decimais obrigatórios
     *
     * @param value
     * @return
     */
    public Boolean isDecimalOrError(String value) {
        if (value == null) {
            return false;
        }
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Usado para validar campos de valor F: feminino, M: masculino ou O:
     * outros. De toda forma retorna "O" caso nada seja passado em value
     *
     * @param value
     * @return
     */
    public String isFMO(String value) {
        if (isValueOrError(value)
                && (value.equalsIgnoreCase("F")
                        || value.equalsIgnoreCase("M")
                        || value.equalsIgnoreCase("O"))) {
            return value.toUpperCase();
        }
        return "O";
    }

    /**
     * Usado para validar campos de email
     *
     * @param value
     * @return
     */
    public boolean isEmailOrError(String value) {
        String regex = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        // System.out.println(value +" : "+ matcher.matches()+"\n");
        return matcher.matches();
    }

    /**
     * Usado para validar campos de valor 1: Solteiro, 2: União Estável, 3:
     * Casado, 4: Divorciado, 5: Viúvo ou 6: Outros. De toda forma retorna "¨6"
     * caso nada seja passado em value
     *
     * @param value
     * @return
     */
    public String isMarriage(String value) {
        String ret = null;
        try {
            if (!value.isEmpty() && value != null) {
                ret = isValueOrEmpty(value.replaceAll("[^0-9]", ""));
                switch (ret.substring(0, 1)) {
                    case "1":
                        ret = "3";
                        break;
                    case "2":
                        ret = "1";
                        break;
                    case "3":
                        ret = "5";
                        break;
                    case "4":
                        ret = "4";
                        break;
                    case "5":
                        ret = "4";
                        break;
                    case "6":
                        ret = "6";
                        break;
                    default:
                        ret = null;
                        break;
                }
            }
            return ret;
        } catch (
        Exception e) {
            System.out.println("Erro: " + e.getMessage() + ". Estado civil informado: " + value);
            return null;
        }
    }

    public Boolean isCPFOrError(String CPF) {
        // considera-se erro CPF's formados por uma sequencia de numeros iguais
        if (CPF.equals("00000000000")
                || CPF.equals("11111111111")
                || CPF.equals("22222222222") || CPF.equals("33333333333")
                || CPF.equals("44444444444") || CPF.equals("55555555555")
                || CPF.equals("66666666666") || CPF.equals("77777777777")
                || CPF.equals("88888888888") || CPF.equals("99999999999")
                || (CPF.length() != 11)) {
            return (false);
        }

        char dig10, dig11;
        int sm, i, r, num, peso;

        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 10;
            for (i = 0; i < 9; i++) {
                // converte o i-esimo caractere do CPF em um numero:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posicao de '0' na tabela ASCII)
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig10 = '0';
            } else {
                dig10 = (char) (r + 48); // converte no respectivo caractere numerico
            }
            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 11;
            for (i = 0; i < 10; i++) {
                num = (int) (CPF.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso - 1;
            }

            r = 11 - (sm % 11);
            if ((r == 10) || (r == 11)) {
                dig11 = '0';
            } else {
                dig11 = (char) (r + 48);
            }

            // Verifica se os digitos calculados conferem com os digitos informados.
            if ((dig10 == CPF.charAt(9)) && (dig11 == CPF.charAt(10))) {
                return (true);
            } else {
                return (false);
            }
        } catch (InputMismatchException erro) {
            return (false);
        }
    }

    public boolean isCNPJOrError(String CNPJ) {
        // considera-se erro CNPJ's formados por uma sequencia de numeros iguais
        if (CNPJ.equals("00000000000000") || CNPJ.equals("11111111111111")
                || CNPJ.equals("22222222222222") || CNPJ.equals("33333333333333")
                || CNPJ.equals("44444444444444") || CNPJ.equals("55555555555555")
                || CNPJ.equals("66666666666666") || CNPJ.equals("77777777777777")
                || CNPJ.equals("88888888888888") || CNPJ.equals("99999999999999")
                || (CNPJ.length() != 14)) {
            return (false);
        }

        char dig13, dig14;
        int sm, i, r, num, peso;

        // "try" - protege o código para eventuais erros de conversao de tipo (int)
        try {
            // Calculo do 1o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 11; i >= 0; i--) {
                // converte o i-ésimo caractere do CNPJ em um número:
                // por exemplo, transforma o caractere '0' no inteiro 0
                // (48 eh a posição de '0' na tabela ASCII)
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig13 = '0';
            } else {
                dig13 = (char) ((11 - r) + 48);
            }

            // Calculo do 2o. Digito Verificador
            sm = 0;
            peso = 2;
            for (i = 12; i >= 0; i--) {
                num = (int) (CNPJ.charAt(i) - 48);
                sm = sm + (num * peso);
                peso = peso + 1;
                if (peso == 10) {
                    peso = 2;
                }
            }

            r = sm % 11;
            if ((r == 0) || (r == 1)) {
                dig14 = '0';
            } else {
                dig14 = (char) ((11 - r) + 48);
            }

            // Verifica se os dígitos calculados conferem com os dígitos informados.
            if ((dig13 == CNPJ.charAt(12)) && (dig14 == CNPJ.charAt(13))) {
                return (true);
            } else {
                return (false);
            }
        } catch (InputMismatchException erro) {
            return (false);
        }
    }

    public Boolean isPISOrError(String numeroPIS) {
        String multiplicadorBase = "3298765432";

        Integer iSoma = 0;
        Integer iResto = 0;
        Integer iDV = 99;

        Integer multiplicando = 0;
        Integer multiplicador = 0;
        Integer iDVInformado = 99;

        Integer i;
        Integer iRet = 0;

        // Retira a mascara
        numeroPIS = numeroPIS.replaceAll("[^\\d]", "");
        // System.out.println("PIS: " + numeroPIS);

        if (numeroPIS.length() != 11 || "00000000000".equals(numeroPIS) || "99999999999".equals(numeroPIS) || "99999999990".equals(numeroPIS)) {
            iRet = iRet--;
        } else {
            for (i = 0; i < 10; i++) {
                multiplicando = Integer.parseInt(numeroPIS.substring(i, i + 1));
                multiplicador = Integer.parseInt(multiplicadorBase.substring(i, i + 1));

                iSoma += multiplicando * multiplicador;
            }

            // System.out.println("iSoma: " + iSoma);
            iResto = iSoma % 11;
            // System.out.println("iResto: " + iResto);
            if ((iResto > 0) && (11 - iResto != 10) && (11 - iResto != 11)) {
                iDV = 11 - iResto;
            } else {
                iDV = 0;
            }

            iDVInformado = Integer.parseInt(numeroPIS.substring(10, 11));
            if (!Objects.equals(iDV, iDVInformado)) {
                iRet--;
            }
        }
        return iRet == 0;
    }

    public Boolean isTituloOrError(String strTitulo) {
        int dig1;
        int dig2;
        int dig3;
        int dig4;
        int dig5;
        int dig6;
        int dig7;
        int dig8;
        int dig9;
        int dig10;
        int dig11;
        int dig12;
        int dv1;
        int dv2;
        int qDig;

        if (strTitulo.length() == 12) {

            qDig = strTitulo.length(); // Total de caracteres

            // Gravar posição dos caracteres
            dig1 = Integer.parseInt(Mid(strTitulo, qDig - 11, 1));
            dig2 = Integer.parseInt(Mid(strTitulo, qDig - 10, 1));
            dig3 = Integer.parseInt(Mid(strTitulo, qDig - 9, 1));
            dig4 = Integer.parseInt(Mid(strTitulo, qDig - 8, 1));
            dig5 = Integer.parseInt(Mid(strTitulo, qDig - 7, 1));
            dig6 = Integer.parseInt(Mid(strTitulo, qDig - 6, 1));
            dig7 = Integer.parseInt(Mid(strTitulo, qDig - 5, 1));
            dig8 = Integer.parseInt(Mid(strTitulo, qDig - 4, 1));
            dig9 = Integer.parseInt(Mid(strTitulo, qDig - 3, 1));
            dig10 = Integer.parseInt(Mid(strTitulo, qDig - 2, 1));
            dig11 = Integer.parseInt(Mid(strTitulo, qDig - 1, 1));
            dig12 = Integer.parseInt(Mid(strTitulo, qDig, 1));

            // Cálculo para o primeiro dígito validador
            dv1 = (dig1 * 2) + (dig2 * 3) + (dig3 * 4) + (dig4 * 5) + (dig5 * 6)
                    + (dig6 * 7) + (dig7 * 8) + (dig8 * 9);
            dv1 = dv1 % 11;

            if (dv1 == 10) {
                dv1 = 0; // Se o resto for igual a 10, dv1 igual a zero
            }

            // Cálculo para o segundo dígito validador
            dv2 = (dig9 * 7) + (dig10 * 8) + (dv1 * 9);
            dv2 = dv2 % 11;

            if (dv2 == 10) {
                dv2 = 0; // Se o resto for igual a 10, dv1 igual a zero
            }

            // Validação dos dígitos validadores, após o cálculo realizado
            return dig11 == dv1 && dig12 == dv2;
        } else {
            return false;
        }
    }
    // Função Mid

    public static String Mid(String texto, int inicio, int tamanho) {
        String strMid = texto.substring(inicio - 1, inicio + (tamanho - 1));
        return strMid;
    }

}
