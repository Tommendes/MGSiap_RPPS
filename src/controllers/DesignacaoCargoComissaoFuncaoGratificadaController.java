/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import config.BDCommands;
import mgsiap.MGSiap;
import validations.Validations;

/**
 *
 * @author TomMe
 */
public class DesignacaoCargoComissaoFuncaoGratificadaController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "DesignacaoCargoComissaoFuncaoGratificada.xml";

    public DesignacaoCargoComissaoFuncaoGratificadaController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) dependente(s) como lote
     *
     * @param idServidorI
     * @param idServidorF
     * @return
     */
    public ResultSet getDesignacaoCgCmFnGratifBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "S.CPF, S.IDSERVIDOR, S.SERVIDOR, S.IDVINCULO, CG.TIPOCARGO, M.IDCARGO, M.IDPCCS, CG.CARGO, SO.CODIGO, CG.FGBASELEGAL, S.DATAATO, S.D_ADMISSAO, S.MANAD_NUMERONOMEACAO, S.VEICULOPUBLICACAO, M.ANO, M.MES, M.PARCELA ";
        String sqlSalario = "(select first 1 A.N_VALOR from REFERENCIAS A where A.IDPCCS = M.IDPCCS and A.D_DATA <= '"
                + MGSiap.getOpcoes().getAno() + "-" + MGSiap.getOpcoes().getMes()
                + "-01' order by A.D_DATA desc) SALARIO ";
        String sqlRaw = "select " + select + ", " + sqlSalario + " from CARGOS CG "
                + "join MENSAL M on CG.IDCARGO = M.IDCARGO "
                + "join CENTROS C on C.IDCENTRO = M.IDCENTRO "
                + "join SERVIDORES S on S.IDSERVIDOR = M.IDSERVIDOR "
                + "join SIAPORGAO SO on SO.C_UA = C.CODIGO_UA and SO.CNPJ = replace(replace(replace(C.CNPJ_UA, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' "
                + "and m.ano = '" + MGSiap.getOpcoes().getAno() + "' and m.mes = '" + MGSiap.getOpcoes().getMes() + "' "
                + "and S.IDVINCULO in ('2') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "group by " + select + " order by S.CPF";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("", "", "", sqlRaw, false);
        return tabelaRecebe;
    }

    public void toXmlFile(ResultSet resultSet) {
        MGSiap.toLogs("Executando o Leiaute " + fileName, 0);
        try {

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("SIAP");
            document.appendChild(root);

            Element codigo = document.createElement("Codigo");
            Element exercicio = document.createElement("Exercicio");
            Element mes = document.createElement("Mes");

            codigo.appendChild(document.createTextNode(MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)));
            exercicio.appendChild(document.createTextNode(MGSiap.getOpcoes().getAno()));
            mes.appendChild(document.createTextNode(MGSiap.getOpcoes().getMes()));

            root.appendChild(codigo);
            root.appendChild(exercicio);
            root.appendChild(mes);

            Validations v = new Validations();

            boolean error = false;
            if (resultSet.first()) {
                resultSet.beforeFirst();
                while (resultSet.next()) {

                    String cgCodigo = "";
                    String idVinculo = "";
                    String idCargo = "";
                    String idPcc = "";
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ")" + " (DesignacaoCargoComissaoFuncaoGratificada): ";

                    StringBuilder sb = new StringBuilder();
                    sb.append(startLog);
                    StringBuilder sbW = new StringBuilder();
                    sbW.append(startLog);

                    if (v.isValueOrError(resultSet.getString("idvinculo"))
                            && v.isValueOrError(resultSet.getString("idcargo"))
                            && v.isValueOrError(resultSet.getString("idpccs"))) {
                        idVinculo = resultSet.getString("idvinculo").trim();
                        idCargo = String.format("%1$4s", resultSet.getString("idcargo").trim()).replace(" ", "0");
                        idPcc = String.format("%1$4s", resultSet.getString("idpccs").trim()).replace(" ", "0");
                        cgCodigo = idVinculo + idCargo + idPcc;
                    } else {
                        // Codigo.appendChild(document.createTextNode(""));
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Designacao Cargo Comissão Funcao Gratificada ("
                                + v.isValueOrError(resultSet.getString("idcargo")) + ") com erro, ");
                    }

                    Element CPF = document.createElement("CPF");
                    Element Matricula = document.createElement("Matricula");
                    Element Processo = document.createElement("Processo");
                    Element NumeroAto = document.createElement("NumeroAto");
                    Element DataAto = document.createElement("DataAto");
                    Element VeiculoPublicacao = document.createElement("VeiculoPublicacao");
                    Element DataInicio = document.createElement("DataInicio");
                    Element FuncaoGratificada = document.createElement("FuncaoGratificada");
                    Element CargoComissao = document.createElement("CargoComissao");
                    Element PercentualSalarioEfeitvo = document.createElement("PercentualSalarioEfeitvo");
                    Element PercentualSalarioComissionado = document.createElement("PercentualSalarioComissionado");
                    Element ValorFuncaoGratificada = document.createElement("ValorFuncaoGratificada");
                    Element PercentualFuncaoGratificada = document.createElement("PercentualFuncaoGratificada");
                    Element BaseLegal = document.createElement("BaseLegal");

                    /* Validação do tipo de cargo. Obrigatoriamente deve ser "3 - Comissionado" */
                    String tipoCargo = v.isValueOrEmpty(resultSet.getString("TIPOCARGO"));
                    if (!(tipoCargo != null && tipoCargo.equalsIgnoreCase("3 - Comissionado"))) {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Cargo (" + idCargo + ") do tipo não comissionado (" + tipoCargo
                                + "). Ajustar o cargo antes de prosseguir..., ");
                    }

                    /* CPF */
                    if (v.isValueOrError(resultSet.getString("CPF"))
                            && v.isNumberOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))) {
                        CPF.appendChild(document
                                .createTextNode(resultSet.getString("CPF").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CPF inválido: (Matricula '" + v.isValueOrEmpty(resultSet.getString("idservidor"))
                                + "')" + v.isNumberOrEmpty(resultSet.getString("CPF"), 11, "R").trim() + "', ");
                    }
                    /* Matricula */
                    if (v.isValueOrError(resultSet.getString("idservidor"))) {
                        Matricula.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("idservidor"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Matricula inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("idservidor")) + "', ");
                    }
                    /* Processo */
                    Processo.appendChild(document.createTextNode(""));
                    /* NumeroAto */
                    if (v.isValueOrError(resultSet.getString("MANAD_NUMERONOMEACAO"))) {
                        NumeroAto.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("MANAD_NUMERONOMEACAO"), 32, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nr/Processo Admissão (MANAD/SIAP) inválido: (Servidor):'"
                                + v.isValueOrError(resultSet.getString("idservidor") + "', "));
                    }
                    /* DataAto */
                    if (v.isValueOrError(resultSet.getString("DATAATO"))) {
                        DataAto.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("DATAATO")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data Ato (Publicação) inválida: (Servidor):'"
                                + v.isValueOrError(resultSet.getString("idservidor")) + "', ");
                    }
                    /* VeiculoPublicacao */
                    if (v.isValueOrError(resultSet.getString("VEICULOPUBLICACAO"))) {
                        VeiculoPublicacao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("VEICULOPUBLICACAO"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Veiculo Publicacao inválido: (Servidor):'"
                                + v.isValueOrError(resultSet.getString("idservidor")) + "', ");
                    }
                    /* DataInicio */
                    if (v.isValueOrError(resultSet.getString("D_ADMISSAO"))) {
                        DataInicio.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("D_ADMISSAO"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data Início (Admissão) inválida: (Servidor):'"
                                + v.isValueOrError(resultSet.getString("idservidor")) + "', ");
                    }
                    /* FuncaoGratificada */
                    if (v.isValueOrError(cgCodigo)) {
                        FuncaoGratificada.appendChild(document.createTextNode(cgCodigo));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Função Gratificada inválida: (Cargo Comissionado):'" + idCargo + "', ");
                    }
                    /* CargoComissao */
                    if (v.isValueOrError(idCargo)) {
                        CargoComissao.appendChild(document.createTextNode(idCargo));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Cargo Comissão inválido(s): (Cargo Comissionado):'" + idCargo + "', ");
                    } // Caso hajam erros na declaração da FG ou CGC eles já foram reportados na
                      // condicional anterior da FuncaoGratificada
                    /* PercentualSalarioEfeitvo */
                    PercentualSalarioEfeitvo.appendChild(document.createTextNode("100.00"));
                    /* PercentualSalarioComissionado */
                    PercentualSalarioComissionado.appendChild(document.createTextNode("100.00"));

                    /* ValorFuncaoGratificada */
                    if (v.isValueOrError(resultSet.getString("salario"))) {
                        ValorFuncaoGratificada.appendChild(document.createTextNode(resultSet.getString("salario")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append(
                                "Valor Funcao Gratificada inválido(s): (Cargo Comissionado):'" + idCargo + "', ");
                    }
                    /* PercentualFuncaoGratificada */
                    PercentualFuncaoGratificada.appendChild(document.createTextNode("100.00"));

                    /* BaseLegal */
                    if (v.isValueOrError(resultSet.getString("fgbaselegal"))) {
                        BaseLegal.appendChild(document
                                .createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("fgbaselegal"), 1024, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Base Legal inválida: (Cargo Comissionado):'" + idCargo + "', ");
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("DesignacaoCargoComissaoFuncaoGratificada");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(Processo);
                        layout.appendChild(NumeroAto);
                        layout.appendChild(DataAto);
                        layout.appendChild(VeiculoPublicacao);
                        layout.appendChild(DataInicio);
                        layout.appendChild(FuncaoGratificada);
                        layout.appendChild(CargoComissao);
                        layout.appendChild(PercentualSalarioEfeitvo);
                        layout.appendChild(PercentualSalarioComissionado);
                        layout.appendChild(ValorFuncaoGratificada);
                        layout.appendChild(PercentualFuncaoGratificada);
                        layout.appendChild(BaseLegal);
                        root.appendChild(layout);
                    }
                }
                if (error) {
                    Element layout = document.createElement("Informacao");
                    layout.appendChild(document.createTextNode("Arquivo gerado com erros! Ver o log"));
                    root.appendChild(layout);
                }
            }

            if (gerarXml)
                try {
                    String xmlFilePath = MGSiap.getFileFolder() + fileName;
                    if (error)
                        xmlFilePath = MGSiap.getFileFolder() + "Com_Erros_" + fileName;
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);
                    StreamResult streamResult = new StreamResult(new File(xmlFilePath));

                    transformer.transform(domSource, streamResult);
                    MGSiap.toLogs("Arquivo XML " + fileName + " salvo em: " + xmlFilePath, 0);

                    ResultSet tabelaAuxiliares = bDCommands.getTabelaGenerico("", "", "",
                            "select count(*) from auxiliares where dominio = 'siap' "
                                    + "and meta = 'designacaoCargoComissaoFuncaoGratificada'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','designacaoCargoComissaoFuncaoGratificada','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(DesignacaoCargoComissaoFuncaoGratificadaController.class.getName()).log(
                            Level.SEVERE, null,
                            ex);
                }
        } catch (ParserConfigurationException |

                SQLException ex) {
            Logger.getLogger(DesignacaoCargoComissaoFuncaoGratificadaController.class.getName()).log(Level.SEVERE, null,
                    ex);
        }
    }
}
