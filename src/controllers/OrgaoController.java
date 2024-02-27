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
public class OrgaoController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Orgao.xml";

    public OrgaoController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getOrgaosBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String sqlComplementar = "where "
                // + "codigo is not null "
                // + "and cnpj is not null and nome is not null "
                // + "and sigla is not null and datacriacao is not null "
                // + "and atocriacao is not null "
                // + "and VeiculoPublicacaoAtoCriacao is not null "
                // + "and "
                + "cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' order by nome";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("siaporgao", "", sqlComplementar,
                "", false);
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
                    String startLog = "Orgão " + resultSet.getString("nome") + " (" + resultSet.getString("codigo")
                            + "): ";
                    StringBuilder sb = new StringBuilder();
                    sb.append(startLog);
                    StringBuilder sbW = new StringBuilder();
                    sbW.append(startLog);
                    Element Codigo = document.createElement("Codigo");
                    Element CNPJ = document.createElement("CNPJ");
                    Element Nome = document.createElement("Nome");
                    Element Sigla = document.createElement("Sigla");
                    Element DataCriacao = document.createElement("DataCriacao");
                    Element DataAtoCriacao = document.createElement("DataAtoCriacao");
                    Element AtoCriacao = document.createElement("AtoCriacao");
                    Element VeiculoPublicacaoAtoCriacao = document.createElement("VeiculoPublicacaoAtoCriacao");
                    Element DataExtincao = document.createElement("DataExtincao");
                    Element DataAtoExtincao = document.createElement("DataAtoExtincao");
                    Element AtoExtincao = document.createElement("AtoExtincao");
                    Element VeiculoPublicacaoAtoExtincao = document.createElement("VeiculoPublicacaoAtoExtincao");
                    Element CodigoOrgaoPai = document.createElement("CodigoOrgaoPai");

                    if (v.isValueOrError(resultSet.getString("Codigo"))) {
                        Codigo.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Codigo"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo inválido: '" + resultSet.getString("Codigo") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("CNPJ"))
                            && v.isNumberOrError(resultSet.getString("CNPJ").trim().replaceAll("[^0-9]", ""))
                            && v.isCNPJOrError(resultSet.getString("CNPJ").trim().replaceAll("[^0-9]", ""))) {
                        CNPJ.appendChild(
                                document.createTextNode(resultSet.getString("CNPJ").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CNPJ inválido: '" + v.isNumberOrEmpty(resultSet.getString("CNPJ"), 14, "L").trim()
                                + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("Nome"))) {
                        Nome.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Nome"), 255, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nome inválido: '" + resultSet.getString("Nome") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("Sigla"))) {
                        Sigla.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Sigla"), 255, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Sigla inválida: '" + resultSet.getString("Sigla") + "', ");
                    }
                    // if (v.isValueOrError(resultSet.getString("DataCriacao"))) {
                    // DataCriacao.appendChild(
                    // document.createTextNode(v.isValueOrEmpty(resultSet.getString("DataCriacao")).trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("Data de Criacao inválida: '" +
                    // v.isValueOrEmpty(resultSet.getString("DataCriacao"))
                    // + "', ");
                    // }
                    // if (v.isValueOrError(resultSet.getString("DataAtoCriacao"))) {
                    // DataAtoCriacao.appendChild(document
                    // .createTextNode(v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")).trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("Data de Ato de Criacao inválida: '"
                    // + v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")) + "', ");
                    // }
                    // if (v.isValueOrError(resultSet.getString("AtoCriacao"))) {
                    // AtoCriacao.appendChild(document
                    // .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoCriacao"), 32,
                    // "L").trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("Ato de Criacao inválido: '" + resultSet.getString("AtoCriacao") +
                    // "', ");
                    // }
                    // if (v.isValueOrError(resultSet.getString("VeiculoPublicacaoAtoCriacao"))) {
                    // VeiculoPublicacaoAtoCriacao.appendChild(document.createTextNode(
                    // v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoAtoCriacao"), 1,
                    // "L").trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("Veiculo de Publicacao de Criacao inválido: '"
                    // + resultSet.getString("VeiculoPublicacaoAtoCriacao") + "', ");
                    // }
                    DataCriacao.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("DataCriacao"), 10, "L").trim()));
                    DataAtoCriacao.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("DataAtoCriacao"), 10, "L").trim()));
                    AtoCriacao.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoCriacao"), 32, "L").trim()));
                    VeiculoPublicacaoAtoCriacao.appendChild(document.createTextNode(
                            v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoAtoCriacao"), 1, "L").trim()));

                    DataExtincao.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("DataExtincao"), 10, "L").trim()));
                    DataAtoExtincao.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("DataAtoExtincao"), 10, "L").trim()));
                    AtoExtincao.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoExtincao"), 32, "L").trim()));
                    VeiculoPublicacaoAtoExtincao.appendChild(document.createTextNode(
                            v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoAtoExtincao"), 1, "L").trim()));
                    CodigoOrgaoPai.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("CodigoOrgaoPai"), 10, "L").trim()));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Orgao");
                        layout.appendChild(Codigo);
                        layout.appendChild(CNPJ);
                        layout.appendChild(Nome);
                        layout.appendChild(Sigla);
                        layout.appendChild(DataCriacao);
                        layout.appendChild(DataAtoCriacao);
                        layout.appendChild(AtoCriacao);
                        layout.appendChild(VeiculoPublicacaoAtoCriacao);
                        layout.appendChild(DataExtincao);
                        layout.appendChild(DataAtoExtincao);
                        layout.appendChild(AtoExtincao);
                        layout.appendChild(VeiculoPublicacaoAtoExtincao);
                        layout.appendChild(CodigoOrgaoPai);

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
                                    + "and meta = 'orgao'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','orgao','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(OrgaoController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(OrgaoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
