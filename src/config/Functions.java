/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.text.Normalizer;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 *
 * @author TomMe
 */
public class Functions {

    public Functions() {
    }

    public String deAccent(String str) {
        String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("");
    }

    public String removeAcentos(String string) {
        if (string != null && !string.isEmpty()) {
            string = string.replaceAll("[ÂÀÁÄÃ]", "A");
            string = string.replaceAll("[âãàáä]", "a");
            string = string.replaceAll("[ÊÈÉË]", "E");
            string = string.replaceAll("[êèéë]", "e");
            string = string.replaceAll("ÎÍÌÏ", "I");
            string = string.replaceAll("îíìï", "i");
            string = string.replaceAll("[ÔÕÒÓÖ]", "O");
            string = string.replaceAll("[ôõòóö]", "o");
            string = string.replaceAll("[ÛÙÚÜ]", "U");
            string = string.replaceAll("[ûúùü]", "u");
            string = string.replaceAll("Ç", "C");
            string = string.replaceAll("ç", "c");
            string = string.replaceAll("[ýÿ]", "y");
            string = string.replaceAll("Ý", "Y");
            string = string.replaceAll("ñ", "n");
            string = string.replaceAll("Ñ", "N");
        } else {
            string = "";
        }
        return string;
    }

    public static Date addMonth(Date date, int i) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, i);
        return cal.getTime();
    }

    public String getMesExtenso(String mes) {
        // System.out.println("Mês: " + mes);
        String extenso;
        switch (mes) {
            case "01":
                extenso = "Janeiro";
                break;
            case "02":
                extenso = "Fevereiro";
                break;
            case "03":
                extenso = "Março";
                break;
            case "04":
                extenso = "Abril";
                break;
            case "05":
                extenso = "Maio";
                break;
            case "06":
                extenso = "Junho";
                break;
            case "07":
                extenso = "Julho";
                break;
            case "08":
                extenso = "Agosto";
                break;
            case "09":
                extenso = "Setembro";
                break;
            case "10":
                extenso = "Outubro";
                break;
            case "11":
                extenso = "Novembro";
                break;
            case "12":
                extenso = "Dezembro";
                break;
            case "13":
                extenso = "13º";
                break;
            default:
                extenso = "";
                break;
        }
        return extenso;
    }

}
