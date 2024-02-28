/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import mgsiap.MGSiapRPPS;

/**
 *
 * @author TomMe
 */
public class Tabelas {

    public Tabelas() {
    }

    public String VeiculosPublicacao(String value) {
        StringBuilder sb = new StringBuilder();
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("1 Diário Oficial da União").append("\n");
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("2 Diário Oficial do Estado de Alagoas").append("\n");
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("3 Diário Oficial do Tribunal de Contas de Alagoas").append("\n");
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("4 Diário Oficial da Associação de Municípios de Alagoas").append("\n");
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("5 Diário Oficial Próprio").append("\n");
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("6 Boletim Interno Próprio").append("\n");
        return sb.toString();
    }

}
