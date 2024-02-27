/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import mgsiap.MGSiap;

/**
 *
 * @author TomMe
 */
public class ProgressaoCargo {

    private String model;
    private String Codigo;
    private String Nome;
    private String CodigoCargo;
    private String CodigoClasse;
    private String CodigoNivel;
    private String ValorSalario;

    public ProgressaoCargo() {
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCodigo() {
        return Codigo;
    }

    public void setCodigo(String Codigo) {
        this.Codigo = Codigo;
    }

    public String getNome() {
        return Nome;
    }

    public void setNome(String Nome) {
        this.Nome = Nome;
    }

    public String getCodigoCargo() {
        return CodigoCargo;
    }

    public void setCodigoCargo(String CodigoCargo) {
        this.CodigoCargo = CodigoCargo;
    }

    public String getCodigoClasse() {
        return CodigoClasse;
    }

    public void setCodigoClasse(String CodigoClasse) {
        this.CodigoClasse = CodigoClasse;
    }

    public String getCodigoNivel() {
        return CodigoNivel;
    }

    public void setCodigoNivel(String CodigoNivel) {
        this.CodigoNivel = CodigoNivel;
    }

    public String getValorSalario() {
        return ValorSalario;
    }

    public void setValorSalario(String ValorSalario) {
        this.ValorSalario = ValorSalario;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("ProgressaoCargo{Codigo=").append(Codigo);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("Nome=").append(Nome);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("CodigoCargo=").append(CodigoCargo);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("CodigoClasse=").append(CodigoClasse);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("CodigoNivel=").append(CodigoNivel);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("ValorSalario=").append(ValorSalario);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("ValorSalario=").append(ValorSalario);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append("model=").append(model);
        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
        sb.append('}');
        return sb.toString();
    }

}
