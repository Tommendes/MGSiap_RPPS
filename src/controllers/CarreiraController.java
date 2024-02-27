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
public class CarreiraController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Carreira.xml";

    public CarreiraController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getCarreiraBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "pccs.idpccs, pccs.pccs, pccs.datacriacao, pccs.dataatocriacao, pccs.atocriacao, pccs.veiculopublicacaoatocriacao, "
                + "m.ano, m.mes, m.parcela";
        String sqlRaw = "select " + select + " from pccs "
                + "join mensal m on m.idpccs = pccs.idpccs "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "join servidores s on s.idservidor = m.idservidor "
                + "where s.idservidor between '" + idServidorI + "' AND '"
                + idServidorF + "' and m.ano = '" + MGSiap.getOpcoes().getAno()
                + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício
                // 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "group by " + select + " order by pccs.pccs";
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
                    String startLog = "Carreira (PCC) " + resultSet.getString("pccs") + " ("
                            + resultSet.getString("idpccs")
                            + "): ";
                    StringBuilder sb = new StringBuilder();
                    sb.append(startLog);
                    StringBuilder sbW = new StringBuilder();
                    sbW.append(startLog);
                    Element Codigo = document.createElement("Codigo");
                    Element Nome = document.createElement("Nome");
                    Element DataCriacao = document.createElement("DataCriacao");
                    Element DataAtoCriacao = document.createElement("DataAtoCriacao");
                    Element AtoCriacao = document.createElement("AtoCriacao");
                    Element VeiculoPublicacaoAtoCriacao = document.createElement("VeiculoPublicacaoAtoCriacao");
                    Element AlteracaoNomenclatura = document.createElement("AlteracaoNomenclatura");
                    Element DataAlteracao = document.createElement("DataAlteracao");
                    Element DataAtoAlteracao = document.createElement("DataAtoAlteracao");
                    Element AtoAlteracao = document.createElement("AtoAlteracao");
                    Element VeiculoPublicacaoAtoAlteracao = document.createElement("VeiculoPublicacaoAtoAlteracao");
                    Element DataExtincao = document.createElement("DataExtincao");
                    Element DataAtoExtincao = document.createElement("DataAtoExtincao");
                    Element AtoExtincao = document.createElement("AtoExtincao");
                    Element VeiculoPublicacaoAtoExtincao = document.createElement("VeiculoPublicacaoAtoExtincao");

                    if (v.isValueOrError(resultSet.getString("idpccs"))) {
                        Codigo.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("idpccs"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo inválido: '" + resultSet.getString("idpccs") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("pccs"))) {
                        Nome.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("pccs"), 255, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nome inválido: '" + resultSet.getString("pccs") + "', ");
                    }
                    // if (v.isValueOrError(resultSet.getString("DataCriacao"))) {
                    DataCriacao.appendChild(
                            document.createTextNode(v.isValueOrEmpty(resultSet.getString("DataCriacao")).trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("Data de Criacao inválida: '" +
                    // v.isValueOrEmpty(resultSet.getString("DataCriacao"))
                    // + "', ");
                    // }
                    if (v.isValueOrError(resultSet.getString("DataAtoCriacao"))) {
                        DataAtoCriacao.appendChild(document
                                .createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("DataAtoCriacao"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sb.append("Data de Ato de Criacao inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")) + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("AtoCriacao"))) {
                        AtoCriacao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoCriacao"), 32,
                                        "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sb.append("Ato de Criacao inválido: '" + resultSet.getString("AtoCriacao") +
                                "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("VeiculoPublicacaoAtoCriacao"))) {
                        VeiculoPublicacaoAtoCriacao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoAtoCriacao"), 1,
                                        "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sb.append("Veiculo de Publicacao de Criacao inválido: '"
                                + resultSet.getString("VeiculoPublicacaoAtoCriacao") + "', ");
                    }
                    
                    AlteracaoNomenclatura.appendChild(document.createTextNode(""));
                    DataAlteracao.appendChild(document.createTextNode(""));
                    DataAtoAlteracao.appendChild(document.createTextNode(""));
                    AtoAlteracao.appendChild(document.createTextNode(""));
                    VeiculoPublicacaoAtoAlteracao.appendChild(document.createTextNode(""));
                    DataExtincao.appendChild(document.createTextNode(""));
                    DataAtoExtincao.appendChild(document.createTextNode(""));
                    AtoExtincao.appendChild(document.createTextNode(""));
                    VeiculoPublicacaoAtoExtincao.appendChild(document.createTextNode(""));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Carreira");
                        layout.appendChild(Codigo);
                        layout.appendChild(Nome);
                        layout.appendChild(DataCriacao);
                        layout.appendChild(DataAtoCriacao);
                        layout.appendChild(AtoCriacao);
                        layout.appendChild(VeiculoPublicacaoAtoCriacao);
                        layout.appendChild(AlteracaoNomenclatura);
                        layout.appendChild(DataAlteracao);
                        layout.appendChild(DataAtoAlteracao);
                        layout.appendChild(AtoAlteracao);
                        layout.appendChild(VeiculoPublicacaoAtoAlteracao);
                        layout.appendChild(DataExtincao);
                        layout.appendChild(DataAtoExtincao);
                        layout.appendChild(AtoExtincao);
                        layout.appendChild(VeiculoPublicacaoAtoExtincao);

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
                                    + "and meta = 'carreira'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','carreira','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(CarreiraController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(CarreiraController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
