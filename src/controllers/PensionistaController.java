/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
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
public class PensionistaController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Pensionista.xml";

    public PensionistaController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) auxilio doenca(s) como lote
     *
     * @param idServidorI
     * @param idServidorF
     * @return
     */
    public ResultSet getPensionistaBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "sb.cpf, sb.idservidor, sp.numeroato, sp.dataato, sp.veiculopublicacao, s.idservidor MatPensionista, "
                + "s.cpf CPFPensionista, s.servidor, s.d_nascimento, sp.grauparentesco, sp.tipobeneficio, "
                + "sp.datainicio, sp.datafim, sp.percentual, sp.responsavel, sp.revisao, md.d_afastamento, m.ano, m.mes, m.parcela";
        String specialSelectVPercentual = "sum(f.n_valor) valorPensao";
        String sqlRaw = "select " + select + ", " + specialSelectVPercentual + " from servidor_pensionista sp "
                + "join servidores s on sp.idservidor = s.idservidor "
                + "join mensal m on m.idservidor = sp.idservidor "
                + "join financeiro f on f.idservidor = s.idservidor and f.ano = m.ano and f.mes = m.mes and f.parcela = m.parcela "
                + "join servidores sb on sp.cpfcontribuidor = sb.cpf "
                + "join mdefinitivo md on md.idservidor = sb.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '"
                + idServidorF + "' and m.ano = '" + MGSiap.getOpcoes().getAno()
                + "' and m.mes = '" + MGSiap.getOpcoes().getMes() + "' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                + "and S.IDVINCULO in ('4', '5') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "and md.retorna = 'Desligamento' and f.tipo = 'C'"
                + "group by " + select.replace(" CPFPensionista", "").replace(" MatPensionista", "")
                + " order by s.servidor";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("", "", "", sqlRaw, true);
        return tabelaRecebe;
    }

    public void toXmlFile(ResultSet resultSet) {
        MGSiap.toLogs(false, "Executando o Leiaute " + fileName, 0);
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
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("MatPensionista") + ") (Pensionista): ";
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
                    Element CPFPensionista = document.createElement("CPFPensionista");
                    Element NomePensionista = document.createElement("NomePensionista");
                    Element DataNascimento = document.createElement("DataNascimento");
                    Element GrauParentesco = document.createElement("GrauParentesco");
                    Element TipoBeneficio = document.createElement("TipoBeneficio");
                    Element DataInicio = document.createElement("DataInicio");
                    Element DataFim = document.createElement("DataFim");
                    Element Percentual = document.createElement("Percentual");
                    Element Responsavel = document.createElement("Responsavel");
                    Element Revisao = document.createElement("Revisao");
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
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("NumeroAto inválido: '" + resultSet.getString("NumeroAto") + "', ");
                    }
                    /* DataAto */
                    if (v.isValueOrError(resultSet.getString("DataAto"))) {
                        DataAto.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("DataAto")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("DataAto inválido: '" + resultSet.getString("DataAto") + "', ");
                    }
                    /* VeiculoPublicacao */
                    if (v.isValueOrError(resultSet.getString("VeiculoPublicacao"))) {
                        VeiculoPublicacao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("VeiculoPublicacao"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("VeiculoPublicacao inválido: '" + resultSet.getString("VeiculoPublicacao")
                                + "', ");
                    }
                    /* CPFPensionista */
                    if (v.isValueOrError(resultSet.getString("CPFPensionista"))
                            && v.isNumberOrError(resultSet.getString("CPFPensionista").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPFPensionista").trim().replaceAll("[^0-9]", ""))) {
                        CPFPensionista.appendChild(document
                                .createTextNode(resultSet.getString("CPFPensionista").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CPF do Pensionista inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("CPFPensionista").trim()) + "', ");
                    }
                    /* NomePensionista */
                    if (v.isValueOrError(resultSet.getString("servidor"))) {
                        NomePensionista.appendChild(document
                                .createTextNode(resultSet.getString("servidor").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nome Pensionista inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("servidor")).trim() + "', ");
                    }
                    /* DataNascimento */
                    if (v.isValueOrError(resultSet.getString("d_nascimento"))) {
                        DataNascimento.appendChild(document
                                .createTextNode(resultSet.getString("d_nascimento").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data de Nascimento inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("d_nascimento").trim()) + "', ");
                    }
                    /* GrauParentesco */
                    if (v.isValueOrError(resultSet.getString("GrauParentesco"))) {
                        GrauParentesco.appendChild(document
                                .createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("GrauParentesco").trim(), 1, "L")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Grau de Parentesco inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("GrauParentesco").trim()) + "', ");
                    }
                    /* TipoBeneficio */
                    if (v.isValueOrError(resultSet.getString("TipoBeneficio"))) {
                        TipoBeneficio.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("TipoBeneficio").trim(), 1, "L")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Tipo de Beneficio inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("TipoBeneficio").trim()) + "', ");
                    }
                    /* DataInicio */
                    if (v.isValueOrError(resultSet.getString("datainicio"))) {
                        DataInicio.appendChild(document
                                .createTextNode(resultSet.getString("datainicio")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data de Inicio inválido: '" + resultSet.getString("datainicio") + "', ");
                    }
                    /* DataFim */
                    if (v.isValueOrEmpty(resultSet.getString("TipoBeneficio").trim(), 1, "L").equals("2")) {
                        DataFim.appendChild(document.createTextNode(""));
                    } else {
                        if (v.isValueOrError(resultSet.getString("datafim"))) {
                            DataFim.appendChild(document
                                    .createTextNode(resultSet.getString("datafim")));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("Data de Fim inválido: '" + resultSet.getString("datafim") + "', ");
                        }
                    }
                    /* Percentual */
                    if (v.isDecimalOrError(resultSet.getString("valorPensao"))
                            && v.isValueOrError(resultSet.getString("CPF"))
                            && v.isNumberOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))) {
                        ResultSet pensaoTotal = new PensaoController(bDCommands, false).getPensaoTotal(
                                resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""));
                        if (pensaoTotal.next()) {
                            Double valorTotalPensao = pensaoTotal.getDouble("valorTotalPensao");
                            Double valorPensao = resultSet.getDouble("valorPensao");
                            Double percentual = (valorPensao / valorTotalPensao) * 100;
                            Percentual.appendChild(document.createTextNode(
                                    v.isValueOrEmpty(String.format(Locale.ROOT, "%.2f", percentual).toString())));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                            sb.append("Percentual inválido, ");
                        }
                    } /*
                       * A mensagem de erro, caso ocorra já foi enviada com o Field CPF(01 deste
                       * leiaute) acima
                       */
                    /* Responsavel */
                    if (v.isValueOrError(resultSet.getString("Responsavel"))) {
                        Responsavel.appendChild(
                                document.createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("Responsavel").trim(), 1, "L")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Responsavel inválido: '" + resultSet.getString("Responsavel") + "', ");
                    }
                    /* Revisao */
                    if (v.isValueOrError(resultSet.getString("Revisao"))) {
                        Revisao.appendChild(
                                document.createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("Revisao").trim(), 1, "L")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Revisao inválido: '" + resultSet.getString("Revisao") + "', ");
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(false, sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(false, sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Pensionista");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(Processo);
                        layout.appendChild(NumeroAto);
                        layout.appendChild(DataAto);
                        layout.appendChild(VeiculoPublicacao);
                        layout.appendChild(CPFPensionista);
                        layout.appendChild(NomePensionista);
                        layout.appendChild(DataNascimento);
                        layout.appendChild(GrauParentesco);
                        layout.appendChild(TipoBeneficio);
                        layout.appendChild(DataInicio);
                        layout.appendChild(DataFim);
                        layout.appendChild(Percentual);
                        layout.appendChild(Responsavel);
                        layout.appendChild(Revisao);
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
                    String xmlFilePath = MGSiap.getFileFolder(1) + fileName;
                    if (error)
                        xmlFilePath = MGSiap.getFileFolder(1) + "Com_Erros_" + fileName;
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);
                    StreamResult streamResult = new StreamResult(new File(xmlFilePath));
                    transformer.transform(domSource, streamResult);
                    
                    MGSiap.toLogs(false, "Arquivo XML " + fileName + " salvo em: " + xmlFilePath, 0);

                    ResultSet tabelaAuxiliares = bDCommands.getTabelaGenerico("", "", "",
                            "select count(*) from auxiliares where dominio = 'siap' "
                                    + "and meta = 'pensionista'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','pensionista','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(PensionistaController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(PensionistaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
