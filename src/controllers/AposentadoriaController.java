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
public class AposentadoriaController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Aposentadoria.xml";

    public AposentadoriaController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getAposentadoriaBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "s.idservidor, s.cpf, s.servidor, s.idservidor, sa.processo, sa.numeroato, sa.dataato, sa.veiculopublicacao, "
                + "sa.datainicio, sa.tipo, sa.baselegal, sa.diastempoanterior, sa.diastempoefetivo, "
                + "sa.diastempoafastado, sa.diastempocomissionado, sa.diasaverbacaoprivado, "
                + "sa.diasaverbacaopublica, sa.reversao, sa.atoreversao, sa.datapublicacaoreversao, "
                + "sa.veiculopublicacaoreversao, sa.revisao, sa.atorevisao, sa.datapublicacaorevisao, "
                + "sa.veiculopublicacaorevisao, so.codigo, m.ano, m.mes, m.parcela";
        String sqlRaw = "select sa.idservidor dadosaposentadoria, " + select + " from mensal m "
                + "join servidores s on s.idservidor = m.idservidor "
                + "left join servidor_aposentadoria sa on m.idservidor = sa.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '"
                + idServidorF + "' and m.ano = '" + MGSiap.getOpcoes().getAno()
                + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO' and m.situacaofuncional = 'APOSENTADO') or exists "
                + "(select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                + "and s.idvinculo != '11' " 
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "group by sa.idservidor, " + select + " order by s.idservidor";
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
                            + resultSet.getString("IDSERVIDOR") + ")" + " (Aposentadoria): ";
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
                    Element BaseLegal = document.createElement("BaseLegal");
                    Element DiasTempoAnterior = document.createElement("DiasTempoAnterior");
                    Element DiasTempoEfetivo = document.createElement("DiasTempoEfetivo");
                    Element DiasTempoAfastado = document.createElement("DiasTempoAfastado");
                    Element DiasTempoComissionado = document.createElement("DiasTempoComissionado");
                    Element DiasAverbacaoPrivado = document.createElement("DiasAverbacaoPrivado");
                    Element DiasAverbacaoPublica = document.createElement("DiasAverbacaoPublica");
                    Element Reversao = document.createElement("Reversao");
                    Element AtoReversao = document.createElement("AtoReversao");
                    Element DataPublicacaoReversao = document.createElement("DataPublicacaoReversao");
                    Element VeiculoPublicacaoReversao = document.createElement("VeiculoPublicacaoReversao");
                    Element Revisao = document.createElement("Revisao");
                    Element AtoRevisao = document.createElement("AtoRevisao");
                    Element DataPublicacaoRevisao = document.createElement("DataPublicacaoRevisao");
                    Element VeiculoPublicacaoRevisao = document.createElement("VeiculoPublicacaoRevisao");

                    /* Dados da aposentadoria */
                    if (!v.isValueOrError(resultSet.getString("dadosaposentadoria"))) {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dados da aposentadoria ausentes, ");
                    }
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
                    Processo.appendChild(
                            document.createTextNode(v.isValueOrEmpty(resultSet.getString("Processo")).trim()));
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
                        sb.append("DataAto inválido: '" + resultSet.getString("DataAto") + "', ");
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
                    /* DataInicio */
                    if (v.isValueOrError(resultSet.getString("DataInicio"))) {
                        DataInicio.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("DataInicio")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataInicio inválido: '" + resultSet.getString("DataInicio") + "', ");
                    }
                    /* Tipo */
                    if (v.isValueOrError(resultSet.getString("Tipo"))) {
                        Tipo.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Tipo"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Tipo inválido: '" + resultSet.getString("Tipo") + "', ");
                    }
                    /* BaseLegal */
                    if (v.isValueOrError(resultSet.getString("BaseLegal"))) {
                        BaseLegal.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("BaseLegal"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Base Legal inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("BaseLegal")) + "', ");
                    }
                    /* DiasTempoAnterior */
                    if (v.isNumberOrError(resultSet.getString("DiasTempoAnterior"))) {
                        DiasTempoAnterior.appendChild(document
                                .createTextNode(v.isNumberOrEmpty(resultSet.getString("DiasTempoAnterior"), 11, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dias Tempo Anterior inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("DiasTempoAnterior")) + "', ");
                    }
                    /* DiasTempoEfetivo */
                    if (v.isNumberOrError(resultSet.getString("DiasTempoEfetivo"))) {
                        DiasTempoEfetivo.appendChild(document
                                .createTextNode(v.isNumberOrEmpty(resultSet.getString("DiasTempoEfetivo"), 11, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dias Tempo Efetivo inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("DiasTempoEfetivo")) + "', ");
                    }
                    /* DiasTempoAfastado */
                    if (v.isNumberOrError(resultSet.getString("DiasTempoAfastado"))) {
                        DiasTempoAfastado.appendChild(document
                                .createTextNode(v.isNumberOrEmpty(resultSet.getString("DiasTempoAfastado"), 11, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dias Tempo Afastado inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("DiasTempoAfastado")) + "', ");
                    }
                    /* DiasTempoComissionado */
                    if (v.isNumberOrError(resultSet.getString("DiasTempoComissionado"))) {
                        DiasTempoComissionado.appendChild(document
                                .createTextNode(
                                        v.isNumberOrEmpty(resultSet.getString("DiasTempoComissionado"), 11, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dias Tempo Comissionado inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("DiasTempoComissionado")) + "', ");
                    }
                    /* DiasAverbacaoPrivado */
                    if (v.isNumberOrError(resultSet.getString("DiasAverbacaoPrivado"))) {
                        DiasAverbacaoPrivado.appendChild(document
                                .createTextNode(
                                        v.isNumberOrEmpty(resultSet.getString("DiasAverbacaoPrivado"), 11, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dias Tempo Privado inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("DiasAverbacaoPrivado")) + "', ");
                    }
                    /* DiasAverbacaoPublica */
                    if (v.isNumberOrError(resultSet.getString("DiasAverbacaoPublica"))) {
                        DiasAverbacaoPublica.appendChild(document
                                .createTextNode(
                                        v.isNumberOrEmpty(resultSet.getString("DiasAverbacaoPublica"), 11, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Dias Tempo Pública inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("DiasAverbacaoPublica")) + "', ");
                    }
                    /* Reversao */
                    if (v.isNumberOrError(resultSet.getString("Reversao"))) {
                        Reversao.appendChild(document
                                .createTextNode(v.isNumberOrEmpty(resultSet.getString("Reversao"), 1, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Reversao inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("Reversao")) + "', ");
                    }
                    if (v.isNumberOrError(resultSet.getString("Reversao"))
                            && resultSet.getString("Reversao").substring(1, 1) == "1") {
                        /* AtoReversao */
                        if (v.isValueOrError(resultSet.getString("AtoReversao"))) {
                            AtoReversao.appendChild(document
                                    .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoReversao")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("AtoReversao inválido: '" + resultSet.getString("AtoReversao") + "', ");
                        }
                        /* DataPublicacaoReversao */
                        if (v.isValueOrError(resultSet.getString("DataPublicacaoReversao"))) {
                            DataPublicacaoReversao.appendChild(document
                                    .createTextNode(
                                            v.isValueOrEmpty(resultSet.getString("DataPublicacaoReversao")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("Data Publicacao Reversao inválida: '"
                                    + resultSet.getString("DataPublicacaoReversao") + "', ");
                        }
                        /* VeiculoPublicacaoReversao */
                        if (v.isValueOrError(resultSet.getString("VeiculoPublicacaoReversao"))) {
                            VeiculoPublicacaoReversao.appendChild(document.createTextNode(
                                    v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoReversao"), 1, "L").trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            sbW.append("Veiculo Publicacao Reversao inválido: '"
                                    + resultSet.getString("VeiculoPublicacaoReversao")
                                    + "', ");
                        }
                    }
                    /* Revisao */
                    if (v.isNumberOrError(resultSet.getString("Revisao"))) {
                        Revisao.appendChild(document
                                .createTextNode(v.isNumberOrEmpty(resultSet.getString("Revisao"), 1, "R")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Revisao inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("Revisao")) + "', ");
                    }
                    if (v.isNumberOrError(resultSet.getString("Revisao"))
                            && resultSet.getString("Revisao").substring(1, 1) == "1") {
                        /* AtoRevisao */
                        if (v.isValueOrError(resultSet.getString("AtoRevisao"))) {
                            AtoRevisao.appendChild(document
                                    .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoRevisao")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                            sb.append("AtoRevisao inválido: '" + resultSet.getString("AtoRevisao") + "', ");
                        }
                        /* DataPublicacaoRevisao */
                        if (v.isValueOrError(resultSet.getString("DataPublicacaoRevisao"))) {
                            DataPublicacaoRevisao.appendChild(document
                                    .createTextNode(
                                            v.isValueOrEmpty(resultSet.getString("DataPublicacaoRevisao")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                            sb.append("Data Publicacao Revisao inválida: '"
                                    + resultSet.getString("DataPublicacaoRevisao") + "', ");
                        }
                        /* VeiculoPublicacaoRevisao */
                        if (v.isValueOrError(resultSet.getString("VeiculoPublicacaoRevisao"))) {
                            VeiculoPublicacaoRevisao.appendChild(document.createTextNode(
                                    v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoRevisao"), 1, "L").trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                            sb.append("Veiculo Publicacao Revisao inválido: '"
                                    + resultSet.getString("VeiculoPublicacaoRevisao")
                                    + "', ");
                        }
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Aposentadoria");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(Processo);
                        layout.appendChild(NumeroAto);
                        layout.appendChild(DataAto);
                        layout.appendChild(VeiculoPublicacao);
                        layout.appendChild(DataInicio);
                        layout.appendChild(Tipo);
                        layout.appendChild(BaseLegal);
                        layout.appendChild(DiasTempoAnterior);
                        layout.appendChild(DiasTempoEfetivo);
                        layout.appendChild(DiasTempoAfastado);
                        layout.appendChild(DiasTempoComissionado);
                        layout.appendChild(DiasAverbacaoPrivado);
                        layout.appendChild(DiasAverbacaoPublica);
                        layout.appendChild(Reversao);
                        layout.appendChild(AtoReversao);
                        layout.appendChild(DataPublicacaoReversao);
                        layout.appendChild(VeiculoPublicacaoReversao);
                        layout.appendChild(Revisao);
                        layout.appendChild(AtoRevisao);
                        layout.appendChild(DataPublicacaoRevisao);
                        layout.appendChild(VeiculoPublicacaoRevisao);
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
                                    + "and meta = 'aposentadoria'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','aposentadoria','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(AposentadoriaController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(AposentadoriaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
