/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.time.ZoneId;
import java.util.Date;

import mgsiap.MGSiapRPPS;

/**
 *
 * @author TomMe
 */
public class Opcoes {

    private String ano;
    private String mes;
    private String complementar;
    private String codigoOrgao;
    private String titulo;
    private String descricao;
    private String order;
    private String build;
    private String version;
    private static Long timeI;
    private static Long timeF;
    private static Long timeLeft;

    public Opcoes() {
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
        if (this.ano.length() != 4) {
            Date date = new Date();
            this.ano = String.valueOf(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getYear());
        }
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
        if (this.mes.length() != 2) {
            Date date = new Date();
            this.mes = String.valueOf(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
        }
    }

    public String getComplementar() {
        return complementar;
    }

    public void setComplementar(String complementar) {
        this.complementar = complementar;
        if (this.complementar.length() != 3) {
            this.complementar = "000";
        }
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
        if (this.titulo.length() == 0) {
            this.titulo = "Relatório Geral da Folha de Pagamento";
        }
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
        if (this.descricao.length() == 0) {
            this.descricao = "Período - "
                    + getMes() + "/" + getAno()
                    + (getComplementar().equals("000") ? "" : " - Complementar: " + getComplementar());
        }
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public static Long getTimeI() {
        return timeI;
    }

    public static void setTimeI(Long timeI) {
        Opcoes.timeI = timeI;
    }

    public static Long getTimeF() {
        return timeF;
    }

    public static void setTimeF(Long timeF) {
        Opcoes.timeF = timeF;
    }

    public static Long getTimeLeft() {
        return timeLeft;
    }

    public static void setTimeLeft(Long timeLeft) {
        Opcoes.timeLeft = timeLeft;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCodigoOrgao() {
        return codigoOrgao;
    }

    public void setCodigoOrgao(String codigoOrgao) {
        this.codigoOrgao = codigoOrgao;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append("Opcoes{ano=").append(ano);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", mes=").append(mes);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", complementar=").append(complementar);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", codigoOrgao=").append(codigoOrgao);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", titulo=").append(titulo);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", descricao=").append(descricao);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", order=").append(order);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", build=").append(build);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append(", version=").append(version);
        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
        sb.append('}');
        return sb.toString();
    }

}
