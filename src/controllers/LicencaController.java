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
public class LicencaController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Licenca.xml";

    public LicencaController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getLicencaBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        /* Auxílio Doença */
        String sqlRaw = "select s.cpf, s.servidor, s.idservidor, md.numeroato, md.dataato, md.veiculopublicacao, md.d_afastamento, md.d_retorno, m.ano, m.mes, m.parcela, '1' as Motivo, "
                + "iif ((select sum(ff.n_valor) from financeiro ff "
                + "where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0, '1','2') Remunerada "
                + "from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join parametros p on p.ano = m.ano and p.mes = m.mes and p.parcela = m.parcela "
                + "join matemporario md on md.idservidor = m.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' and m.ano = '" + MGSiap.getOpcoes().getAno() + "' and m.mes = '" + MGSiap.getOpcoes().getMes() + "' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "and extract(year from MD.D_AFASTAMENTO) = '" + MGSiap.getOpcoes().getAno() + "' and extract(month from MD.D_AFASTAMENTO) = '" + MGSiap.getOpcoes().getMes() + "' "
                // + "and DATEADD (-1 DAY TO DATEADD (1 MONTH TO CAST(EXTRACT(YEAR FROM p.d_situacao)||'-'||EXTRACT(MONTH FROM p.d_situacao)||'-01' AS DATE))) between "
                // + "CAST(EXTRACT(YEAR FROM md.d_afastamento)||'-'||EXTRACT(MONTH FROM md.d_afastamento)||'-01' AS DATE) and "
                // + "DATEADD (-1 DAY TO DATEADD (1 MONTH TO CAST(EXTRACT(YEAR FROM md.d_retorno)||'-'||EXTRACT(MONTH FROM md.d_retorno)||'-01' AS DATE))) "
                + "UNION ALL ";
        /* Sem vencimento */
        sqlRaw += "select s.cpf, s.servidor, s.idservidor, md.numeroato, md.dataato, md.veiculopublicacao, md.d_afastamento, md.d_retorno, m.ano, m.mes, m.parcela, '14' as Motivo, '2' Remunerada "
                + "from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join parametros p on p.ano = m.ano and p.mes = m.mes and p.parcela = m.parcela "
                + "join mntemporario md on md.idservidor = m.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' and m.ano = '"
                + MGSiap.getOpcoes().getAno() + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "and extract(year from MD.D_AFASTAMENTO) = '" + MGSiap.getOpcoes().getAno() + "' and extract(month from MD.D_AFASTAMENTO) = '" + MGSiap.getOpcoes().getMes() + "' "
                // + "and DATEADD (-1 DAY TO DATEADD (1 MONTH TO CAST(EXTRACT(YEAR FROM p.d_situacao)||'-'||EXTRACT(MONTH FROM p.d_situacao)||'-01' AS DATE))) between "
                // + "CAST(EXTRACT(YEAR FROM md.d_afastamento)||'-'||EXTRACT(MONTH FROM md.d_afastamento)||'-01' AS DATE) and "
                // + "DATEADD (-1 DAY TO DATEADD (1 MONTH TO CAST(EXTRACT(YEAR FROM md.d_retorno)||'-'||EXTRACT(MONTH FROM md.d_retorno)||'-01' AS DATE))) "
                + "UNION ALL ";
        /* Maternidade / Paternidade */
        sqlRaw += "select s.cpf, s.servidor, s.idservidor, md.numeroato, md.dataato, md.veiculopublicacao, md.d_afastamento, md.d_retorno, m.ano, m.mes, m.parcela, iif(s.sexo='M','3','2') as Motivo, "
                + "iif ((select sum(ff.n_valor) from financeiro ff "
                + "where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes and ff.parcela = m.parcela and ff.idevento in ('887','888') and ff.n_valor > 0 group by ff.idservidor) > 0,'1','2') Remunerada "
                + "from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join parametros p on p.ano = m.ano and p.mes = m.mes and p.parcela = m.parcela "
                + "join mtemporario md on md.idservidor = m.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' and m.ano = '"
                + MGSiap.getOpcoes().getAno() + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "and extract(year from MD.D_AFASTAMENTO) = '" + MGSiap.getOpcoes().getAno() + "' and extract(month from MD.D_AFASTAMENTO) = '" + MGSiap.getOpcoes().getMes() + "' "
                // + "and DATEADD (-1 DAY TO DATEADD (1 MONTH TO CAST(EXTRACT(YEAR FROM p.d_situacao)||'-'||EXTRACT(MONTH FROM p.d_situacao)||'-01' AS DATE))) between "
                // + "CAST(EXTRACT(YEAR FROM md.d_afastamento)||'-'||EXTRACT(MONTH FROM md.d_afastamento)||'-01' AS DATE) and "
                // + "DATEADD (-1 DAY TO DATEADD (1 MONTH TO CAST(EXTRACT(YEAR FROM md.d_retorno)||'-'||EXTRACT(MONTH FROM md.d_retorno)||'-01' AS DATE))) "
                + "group by s.cpf, s.servidor, s.idservidor, md.numeroato, md.dataato, md.veiculopublicacao, md.d_afastamento, md.d_retorno, m.ano, m.mes, m.parcela, s.sexo "
                + "order by 2 ";
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
                            + resultSet.getString("IDSERVIDOR") + ")" + " (Licenca): ";
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
                    Element DataFim = document.createElement("DataFim");
                    Element Motivo = document.createElement("Motivo");
                    Element Remunerada = document.createElement("Remunerada");

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
                    /* DataInicio */
                    if (v.isValueOrError(resultSet.getString("d_afastamento"))) {
                        DataInicio.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("d_afastamento")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data Inicio inválido: '" + resultSet.getString("d_afastamento") + "', ");
                    }
                    /* DataFim */
                    if (v.isValueOrError(resultSet.getString("d_retorno"))) {
                        DataFim.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("d_retorno")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataFim inválido: '" + resultSet.getString("d_retorno") + "', ");
                    }
                    /* Motivo */
                    if (v.isValueOrError(resultSet.getString("Motivo"))) {
                        Motivo.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("Motivo").trim())));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Motivo inválido: '" + resultSet.getString("Motivo") + "', ");
                    }
                    /** Remunerada */
                    if (v.isDecimalOrError(resultSet.getString("Remunerada"))) {
                        if (resultSet.getFloat("Remunerada") > 0)
                            Remunerada.appendChild(document.createTextNode("1"));
                        else
                            Remunerada.appendChild(document.createTextNode("2"));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Se é Remunerada inválido: '" + v.isDecimalOrEmpty(resultSet.getString("Remunerada"))
                                + "', ");
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Licenca");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(Processo);
                        layout.appendChild(NumeroAto);
                        layout.appendChild(DataAto);
                        layout.appendChild(VeiculoPublicacao);
                        layout.appendChild(DataInicio);
                        layout.appendChild(DataFim);
                        layout.appendChild(Motivo);
                        layout.appendChild(Remunerada);
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
                                    + "and meta = 'Licenca'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','Licenca','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(LicencaController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(LicencaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}