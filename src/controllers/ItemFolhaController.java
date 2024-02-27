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
public class ItemFolhaController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "ItemFolha.xml";

    public ItemFolhaController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getItemFolhaBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "f.idevento, s.cpf, s.servidor, s.idservidor, m.ano, m.mes, so.cnpj, e.siapnatureza, e.siaptipo, e.siaprpps, e.siaprgps, e.siapirrf, e.siapteto, e.siapfgts";
        String specialSelect = "sum(n_valor) valor";
        String sqlRaw = "select " + select + ", " + specialSelect + " from servidores s "
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
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício
                // 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and f.n_valor > 0 group by " + select + " order by s.servidor";
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
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ") (ItemFolha) (Rubrica: "
                            + resultSet.getString("idevento") + "): ";
                    StringBuilder sb = new StringBuilder();
                    sb.append(startLog);
                    StringBuilder sbW = new StringBuilder();
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element Matricula = document.createElement("Matricula");
                    Element MesCompetencia = document.createElement("MesCompetencia");
                    Element Ano = document.createElement("Ano");
                    Element CNPJFontePagadora = document.createElement("CNPJFontePagadora");
                    Element Natureza = document.createElement("Natureza");
                    Element Tipo = document.createElement("Tipo");
                    Element Descricao = document.createElement("Descricao");
                    Element IncideContribuicaoRPPS = document.createElement("IncideContribuicaoRPPS");
                    Element IncideIRRF = document.createElement("IncideIRRF");
                    Element TetoRemuneratorio = document.createElement("TetoRemuneratorio");
                    Element IncideContribuicaoRGPS = document.createElement("IncideContribuicaoRGPS");
                    Element IncideFGTS = document.createElement("IncideFGTS");
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
                    /* MesCompetencia */
                    if (v.isValueOrError(resultSet.getString("mes"))) {
                        MesCompetencia.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("mes"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Mes de Competência inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("mes")) + "', ");
                    }
                    /* Ano */
                    if (v.isValueOrError(resultSet.getString("ano"))) {
                        Ano.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("ano"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Ano inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("ano")) + "', ");
                    }
                    /* CNPJFontePagadora */
                    if (v.isValueOrError(resultSet.getString("cnpj"))
                            && v.isNumberOrError(resultSet.getString("cnpj").trim().replaceAll("[^0-9]", ""))
                            && v.isCNPJOrError(resultSet.getString("cnpj").trim().replaceAll("[^0-9]", ""))) {
                        CNPJFontePagadora.appendChild(
                                document.createTextNode(resultSet.getString("cnpj").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CNPJ da Fonte Pagadora inválido: '"
                                + v.isNumberOrEmpty(resultSet.getString("cnpj"), 14, "L").trim()
                                + "', ");
                    }
                    /* Natureza */
                    if (v.isValueOrError(resultSet.getString("siapnatureza"))) {
                        Natureza.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("siapnatureza"), 4, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Natureza inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("siapnatureza")) + "', ");
                    }
                    /* Tipo */
                    // Antigo ----------------------------- Novo (Leiaute 2024)
                    // 1. Vencimento, provento ou pensão -- 4. Vencimento, provento ou pensão
                    // 2. Gratificação -------------------- 5. Gratificação
                    // 3. Adicional ----------------------- 6. Adicional
                    // 4. Desconto ------------------------ 7. Indenização
                    // 5. IRRF ---------------------------- 8. Desconto
                    // 6. Contribuição Previdenciária ----- 9. IRRF
                    // 7. Outros -------------------------- 10. Contribuição Previdenciária
                    if (v.isValueOrError(resultSet.getString("siaptipo"))) {
                        Tipo.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("siaptipo"), 2, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Tipo inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("siaptipo")) + "', ");
                    }
                    /* Descricao */
                    if (v.isValueOrError(resultSet.getString("siapnatureza"))) {
                        Descricao.appendChild(document
                                .createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("siapnatureza").substring(7)).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Descricao inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("siapnatureza")) + "', ");
                    }
                    /* IncideContribuicaoRPPS */
                    if (v.isValueOrError(resultSet.getString("siaprpps"))) {
                        IncideContribuicaoRPPS.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("siaprpps"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Incide Contribuicao RPPS inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("siaprpps")) + "', ");
                    }
                    /* IncideIRRF */
                    if (v.isValueOrError(resultSet.getString("siapirrf"))) {
                        IncideIRRF.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("siapirrf"), 4, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Incide IRRF inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("siapirrf")) + "', ");
                    }
                    /* TetoRemuneratorio */
                    if (v.isValueOrError(resultSet.getString("siapteto"))) {
                        TetoRemuneratorio.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("siapteto"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Teto Remuneratorio (Sim ou Não) inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("siapteto")) + "', ");
                    }
                    /* IncideContribuicaoRGPS */
                    // Deixou de ser obrigatório no leiaute v.3
                    // if (v.isValueOrError(resultSet.getString("siaprgps"))) {
                    IncideContribuicaoRGPS.appendChild(document
                            .createTextNode(v.isValueOrEmpty(resultSet.getString("siaprgps"), 2, "L").trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // IncideContribuicaoRGPS.appendChild(document.createTextNode(""));
                    // sbW.append("Incide Contribuicao RGPS inválido: '"
                    // + v.isValueOrEmpty(resultSet.getString("siaprgps")) + "', ");
                    // }

                    /* IncideFGTS */
                    if (v.isValueOrError(resultSet.getString("siapfgts"))) {
                        IncideFGTS.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("siapfgts"), 2, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Incide FGTS inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("siapfgts")) + "', ");
                    }
                    /* Valor */
                    if (v.isDecimalOrError(resultSet.getString("valor"))) {
                        Valor.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("valor")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Valor inválido: '" + v.isValueOrEmpty(resultSet.getString("valor")) + "', ");
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("ItemFolha");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(MesCompetencia);
                        layout.appendChild(Ano);
                        layout.appendChild(CNPJFontePagadora);
                        layout.appendChild(Natureza);
                        layout.appendChild(Tipo);
                        layout.appendChild(Descricao);
                        layout.appendChild(IncideContribuicaoRPPS);
                        layout.appendChild(IncideIRRF);
                        layout.appendChild(TetoRemuneratorio);
                        layout.appendChild(IncideContribuicaoRGPS);
                        layout.appendChild(IncideFGTS);
                        layout.appendChild(Valor);

                        root.appendChild(layout);
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
                                    + "and meta = 'itemFolha'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','itemFolha','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(ItemFolhaController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(ItemFolhaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
