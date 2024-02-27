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
public class AdmissaoController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Admissao.xml";

    public AdmissaoController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getAdmissaoBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "s.cpf, s.servidor, s.idservidor, s.numeroato, s.dataato, s.veiculopublicacao, s.d_admissao, " +
                "s.idvinculo, s.numeroedital, s.numeroinscricao, m.idcargo, m.idpccs, so.codigo, m.ano, m.mes, m.parcela";
        String especialSelect = "(select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and " +
                "ff.ano = m.ano and ff.mes = m.mes and ff.parcela = m.parcela and ff.idevento in ('001','002','003') " +
                "and ff.n_valor > 0 group by ff.idservidor) Salario";
        String sqlRaw = "select " + select + ", " + especialSelect + " from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '"
                + idServidorF + "' and m.ano = '" + MGSiap.getOpcoes().getAno()
                + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                // + "and s.idvinculo != '11' "
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "group by " + select + " order by s.servidor";
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
                    if (v.isValueOrError(resultSet.getString("idvinculo"))
                            && v.isValueOrError(resultSet.getString("idcargo"))
                            && v.isValueOrError(resultSet.getString("idpccs"))) {
                        cgCodigo = resultSet.getString("idvinculo").trim()
                                + String.format("%1$4s", resultSet.getString("idcargo").trim()).replace(" ", "0")
                                + String.format("%1$4s", resultSet.getString("idpccs").trim()).replace(" ", "0");
                    }
                    // if (resultSet.getString("IDSERVIDOR").equals("00001858")){
                    // System.out.println(resultSet.getString("IDSERVIDOR") + ": " +
                    // v.isValueOrError(cgCodigo) + " > " +
                    // (v.isValueOrError(resultSet.getString("idvinculo"))
                    // && v.isValueOrError(resultSet.getString("idcargo"))
                    // && v.isValueOrError(resultSet.getString("idpccs"))));
                    // }
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ")" + " (Admissão): ";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element Matricula = document.createElement("Matricula");
                    Element Processo = document.createElement("Processo");
                    Element NumeroAto = document.createElement("NumeroAto");
                    Element DataAto = document.createElement("DataAto");
                    Element VeiculoPublicacao = document.createElement("VeiculoPublicacao");
                    Element DataInicio = document.createElement("DataInicio");
                    Element Tipo = document.createElement("Tipo");
                    Element NumeroEdital = document.createElement("NumeroEdital");
                    Element NumeroInscricao = document.createElement("NumeroInscricao");
                    Element CodigoCargo = document.createElement("CodigoCargo");
                    Element CodigoCarreira = document.createElement("CodigoCarreira");
                    Element CodigoOrgao = document.createElement("CodigoOrgao");
                    Element Salario = document.createElement("Salario");

                    /* CPF */
                    if (v.isValueOrError(resultSet.getString("CPF"))
                            && v.isNumberOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))) {
                        CPF.appendChild(document
                                .createTextNode(resultSet.getString("CPF").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CPF inválido: '"
                                + v.isNumberOrEmpty(resultSet.getString("CPF"), 11, "R").trim() + "', ");
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
                    if (v.isValueOrError(resultSet.getString("NumeroAto"))) {
                        NumeroAto.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("NumeroAto")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("NumeroAto inválido: '" + resultSet.getString("NumeroAto") + "', ");
                    }
                    /* DataAto */
                    if (v.isValueOrError(resultSet.getString("DataAto"))) {
                        DataAto.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("DataAto")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataAto inválido: '" + resultSet.getString("DataAto") + "', ");
                    }
                    /* VeiculoPublicacao */
                    if (v.isValueOrError(resultSet.getString("VeiculoPublicacao"))) {
                        VeiculoPublicacao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("VeiculoPublicacao"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("VeiculoPublicacao inválido: '" + resultSet.getString("VeiculoPublicacao")
                                + "', ");
                    }
                    /* DataInicio */
                    if (v.isValueOrError(resultSet.getString("d_admissao"))) {
                        DataInicio.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("d_admissao")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataInicio inválido: '" + resultSet.getString("d_admissao") + "', ");
                    }
                    /* Tipo */
                    if (v.isValueOrError(resultSet.getString("idvinculo"))) {
                        if (null != resultSet.getString("idvinculo")) {
                            switch (resultSet.getString("idvinculo")) {
                                // Códigos do Vínculo no MGFolha ---- Códigos do SIAP
                                // utilizados pelo MGFolha
                                // 1 EFETIVO ------------------------ 1. Efetivo
                                // 2 COMISSIONADO ------------------- 4. Comissionado
                                // 3 CONTRATADO --------------------- 3. Contratado Temporário
                                // 4 APOSENTADO --------------------- >>>>> Não informar no SIAP <<<<<
                                // 5 PENSIONISTA -------------------- >>>>> Não informar no SIAP <<<<<
                                // 6 ELETIVO ------------------------ 6. Eletivo
                                // 7 ESTAGIARIO --------------------- 8. Estagiário
                                // 8 CONTRATADO POR PROCESSO SELETIVO 3. Contratado Temporário
                                // 9 ESTABILIZADO ------------------- 2. Estabilizado (pré-CF/88)
                                // 10 REQUISITADO ------------------- 7. Cedido
                                // 11 PENSÃO ALIMENTÍCIA >>>>> Não informar no SIAP <<<<<

                                // Códigos do SIAP Não utilizados pelo MGFolha
                                // 5. Celetista
                                // 9. Aprendiz

                                case "2": // Comissionado
                                    Tipo.appendChild(document.createTextNode("4")); // Comissionado
                                    break;
                                case "3": // Contratado
                                case "8": // Contratado
                                    Tipo.appendChild(document.createTextNode("3")); // Contratado
                                    break;
                                case "4": // Aposentado
                                    Tipo.appendChild(document.createTextNode("10")); // Aposentado
                                    break;
                                case "5": // Pensionista
                                    Tipo.appendChild(document.createTextNode("11")); // Pensionista
                                    break;
                                case "6": // Eletivo
                                    Tipo.appendChild(document.createTextNode("6")); // Eletivo
                                    break;
                                case "7": // Estagiário
                                    Tipo.appendChild(document.createTextNode("8")); // Estagiário
                                    break;
                                case "9": // Estabilizado
                                    Tipo.appendChild(document.createTextNode("2")); // Estabilizado
                                    break;
                                case "10": // Requisitado
                                    Tipo.appendChild(document.createTextNode("7")); // Cedido
                                    break;
                                default:
                                    Tipo.appendChild(document.createTextNode("1")); // Efetivo
                                    break;
                            }
                        }
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Tipo inválido: '" + resultSet.getString("idvinculo") + "', ");
                    }
                    /* NumeroEdital */
                    String tipoVinculo = resultSet.getString("idvinculo");
                    if (!(tipoVinculo.equals("1") || tipoVinculo.equals("8")))
                        NumeroEdital.appendChild(document.createTextNode(""));
                    else if ((tipoVinculo.equals("1") || tipoVinculo.equals("8"))
                            && v.isValueOrError(resultSet.getString("NumeroEdital"))) {
                        NumeroEdital.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("NumeroEdital"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Numero Edital inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("NumeroEdital")) + "', ");
                    }
                    /* NumeroInscricao */
                    if (!(tipoVinculo.equals("1") || tipoVinculo.equals("8")))
                        NumeroInscricao.appendChild(document.createTextNode(""));
                    else if ((tipoVinculo.equals("1") || tipoVinculo.equals("8"))
                            && v.isValueOrError(resultSet.getString("NumeroInscricao"))) {
                        NumeroInscricao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("NumeroInscricao"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Numero Inscricao inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("NumeroInscricao")) + "', ");
                    }
                    /* CodigoCargo */
                    if (v.isValueOrError(cgCodigo)) {
                        CodigoCargo.appendChild(document.createTextNode(cgCodigo));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo Cargo inválido: '" + cgCodigo + "', ");
                    }
                    /* CodigoCarreira */
                    if (v.isValueOrError(resultSet.getString("idpccs"))) {
                        CodigoCarreira.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("idpccs")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Codigo Carreira inválido: '" + resultSet.getString("idpccs") + "', ");
                    }
                    /* CodigoOrgao */
                    if (v.isValueOrError(resultSet.getString("codigo"))) {
                        CodigoOrgao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("codigo"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Codigo Orgao inválido: '" + resultSet.getString("codigo") + "', ");
                    }
                    /* Salario */
                    if (v.isDecimalOrError(resultSet.getString("salario"))) {
                        Salario.appendChild(document
                                .createTextNode(v.isDecimalOrEmpty(resultSet.getString("salario")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sb.append("Codigo Orgao inválido: '" + resultSet.getString("codigo") + "', ");
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Admissao");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(Processo);
                        layout.appendChild(NumeroAto);
                        layout.appendChild(DataAto);
                        layout.appendChild(VeiculoPublicacao);
                        layout.appendChild(DataInicio);
                        layout.appendChild(Tipo);
                        layout.appendChild(NumeroEdital);
                        layout.appendChild(NumeroInscricao);
                        layout.appendChild(CodigoCargo);
                        layout.appendChild(CodigoCarreira);
                        layout.appendChild(CodigoOrgao);
                        layout.appendChild(Salario);

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
                                    + "and meta = 'admissao'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','admissao','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(AdmissaoController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(AdmissaoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
