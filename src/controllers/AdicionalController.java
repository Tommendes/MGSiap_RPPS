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
public class AdicionalController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Adicional.xml";

    public AdicionalController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getAdicionalBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "f.idevento, s.cpf, s.servidor, s.idservidor, f.adnumeroato, f.addataato, f.adveiculopublicacaoato, "
                + "f.addatainicio, e.tipoevento, f.n_valor, f.ad, m.ano, m.mes, m.parcela";
        String sqlRaw = "select " + select + " from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join financeiro f on f.idservidor = m.idservidor and f.ano = m.ano and f.mes = m.mes and f.parcela = m.parcela "
                + "join eventos e on e.idevento = f.idevento "
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
                + "' and f.n_valor > 0 "
                + "and substring(e.tipoevento from 1 for 1) in ('1','2','3','4','5','6','7','8')"
                + " and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
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
                    /* Verifica se os dados adicionais foram informados com base no file AD */
                    String ad = resultSet.getString("ad");
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ")" + " (Rubrica Adicional: "
                            + resultSet.getString("idevento") + "): ";                    
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);

                    if (!v.isValueOrError(ad)) {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sb.append(": Dados adicionais não localizados");
                    } else {
                        Element CPF = document.createElement("CPF");
                        Element Matricula = document.createElement("Matricula");
                        Element Processo = document.createElement("Processo");
                        Element NumeroAto = document.createElement("NumeroAto");
                        Element DataAto = document.createElement("DataAto");
                        Element VeiculoPublicacao = document.createElement("VeiculoPublicacao");
                        Element DataInicio = document.createElement("DataInicio");
                        Element Tipo = document.createElement("Tipo");
                        Element Percentual = document.createElement("Percentual");
                        Element Valor = document.createElement("Valor");

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
                        if (v.isValueOrError(resultSet.getString("adNumeroAto"))) {
                            NumeroAto.appendChild(document
                                    .createTextNode(v.isValueOrEmpty(resultSet.getString("adNumeroAto")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("NumeroAto inválido: '" + resultSet.getString("adNumeroAto") + "', ");
                        }
                        /* DataAto */
                        if (v.isValueOrError(resultSet.getString("adDataAto"))) {
                            DataAto.appendChild(
                                    document.createTextNode(v.isValueOrEmpty(resultSet.getString("adDataAto")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("DataAto inválido: '" + resultSet.getString("adDataAto") + "', ");
                        }
                        /* VeiculoPublicacao */
                        if (v.isValueOrError(resultSet.getString("adVeiculoPublicacaoato"))) {
                            VeiculoPublicacao.appendChild(document.createTextNode(
                                    v.isValueOrEmpty(resultSet.getString("adVeiculoPublicacaoato"), 1, "L").trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("VeiculoPublicacao inválido: '" + resultSet.getString("adVeiculoPublicacaoato")
                                    + "', ");
                        }
                        /* DataInicio */
                        if (v.isValueOrError(resultSet.getString("adDataInicio"))) {
                            DataInicio.appendChild(document
                                    .createTextNode(v.isValueOrEmpty(resultSet.getString("adDataInicio")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                            sb.append("DataInicio inválido: '" + resultSet.getString("adDataInicio") + "', ");
                        }
                        /* Tipo */
                        if (v.isNumberOrError(resultSet.getString("tipoevento"))
                                && v.isValueOrError(resultSet.getString("tipoevento"))) {
                            Tipo.appendChild(
                                    document.createTextNode(resultSet.getString("tipoevento").substring(0, 1)));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                            sb.append("Tipo inválido: '" + resultSet.getString("tipoevento") + "', ");
                        }
                        /* Percentual */
                        Percentual.appendChild(document.createTextNode(""));
                        /* Valor */
                        if (v.isDecimalOrError(resultSet.getString("n_valor"))) {
                            Valor.appendChild(
                                    document.createTextNode(v.isValueOrEmpty(resultSet.getString("n_valor")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("Valor inválido: '" + v.isValueOrEmpty(resultSet.getString("n_valor")) + "', ");
                        }
                        if (!sbW.toString().equalsIgnoreCase(startLog)) {
                            MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                        }
                        if (!sb.toString().equalsIgnoreCase(startLog)) {
                            MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                            if (error == false)
                                error = true;
                        } else {
                            Element layout = document.createElement("Adicional");
                            layout.appendChild(CPF);
                            layout.appendChild(Matricula);
                            layout.appendChild(Processo);
                            layout.appendChild(NumeroAto);
                            layout.appendChild(DataAto);
                            layout.appendChild(VeiculoPublicacao);
                            layout.appendChild(DataInicio);
                            layout.appendChild(Tipo);
                            layout.appendChild(Percentual);
                            layout.appendChild(Valor);
                            root.appendChild(layout);
                        }
                    }
                }
                if (error) {
                    Element layout = document.createElement("Informacao");
                    layout.appendChild(
                            document.createTextNode("Arquivo gerado com erros! Ver o log"));
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
                                    + "and meta = 'adicional'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','adicional','exec')");
                    }

                } catch (TransformerException ex) {
                    Logger.getLogger(AdicionalController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(AdicionalController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
