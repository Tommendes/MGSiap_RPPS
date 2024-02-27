/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author TomMe
 */
public class ProgressaoFuncional {

    private String model;
    private String CPF;
    private String Matricula;
    private String Processo;
    private String NumeroAto;
    private String DataAto;
    private String VeiculoPublicacao;
    private String DataInicio;
    private String ClasseAnterior;
    private String NivelAnterior;
    private String Classe;
    private String Nivel;
    private String ValorA;
    private String Valor;

    public ProgressaoFuncional() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCPF() {
        return this.CPF;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

    public String getMatricula() {
        return this.Matricula;
    }

    public void setMatricula(String Matricula) {
        this.Matricula = Matricula;
    }

    public String getProcesso() {
        return this.Processo;
    }

    public void setProcesso(String Processo) {
        this.Processo = Processo;
    }

    public String getNumeroAto() {
        return this.NumeroAto;
    }

    public void setNumeroAto(String NumeroAto) {
        this.NumeroAto = NumeroAto;
    }

    public String getDataAto() {
        return this.DataAto;
    }

    public void setDataAto(String DataAto) {
        this.DataAto = DataAto;
    }

    public String getVeiculoPublicacao() {
        return this.VeiculoPublicacao;
    }

    public void setVeiculoPublicacao(String VeiculoPublicacao) {
        this.VeiculoPublicacao = VeiculoPublicacao;
    }

    public String getDataInicio() {
        return this.DataInicio;
    }

    public void setDataInicio(String DataInicio) {
        this.DataInicio = DataInicio;
    }

    public String getClasseAnterior() {
        return this.ClasseAnterior;
    }

    public void setClasseAnterior(String ClasseAnterior) {
        this.ClasseAnterior = ClasseAnterior;
    }

    public String getNivelAnterior() {
        return this.NivelAnterior;
    }

    public void setNivelAnterior(String NivelAnterior) {
        this.NivelAnterior = NivelAnterior;
    }

    public String getClasse() {
        return this.Classe;
    }

    public void setClasse(String Classe) {
        this.Classe = Classe;
    }

    public String getNivel() {
        return this.Nivel;
    }

    public void setNivel(String Nivel) {
        this.Nivel = Nivel;
    }

    public String getValor() {
        return this.Valor;
    }

    public void setValor(String Valor) {
        this.Valor = Valor;
    }

    public String getValorA() {
        return this.ValorA;
    }

    public void setValorA(String ValorA) {
        this.ValorA = ValorA;
    }

    @Override
    public String toString() {
        return "ProgressaoFuncional{" +
                " model='" + getModel() + "'" +
                ", CPF='" + getCPF() + "'" +
                ", Matricula='" + getMatricula() + "'" +
                ", Processo='" + getProcesso() + "'" +
                ", NumeroAto='" + getNumeroAto() + "'" +
                ", DataAto='" + getDataAto() + "'" +
                ", VeiculoPublicacao='" + getVeiculoPublicacao() + "'" +
                ", DataInicio='" + getDataInicio() + "'" +
                ", ClasseAnterior='" + getClasseAnterior() + "'" +
                ", NivelAnterior='" + getNivelAnterior() + "'" +
                ", Classe='" + getClasse() + "'" +
                ", Nivel='" + getNivel() + "'" +
                ", ValorA='" + getValorA() + "'" +
                ", Valor='" + getValor() + "'" +
                "}";
    }

}
