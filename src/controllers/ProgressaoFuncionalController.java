/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
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
import models.ProgressaoFuncional;
import validations.Validations;

/**
 *
 * @author TomMe
 */
public class ProgressaoFuncionalController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "ProgressaoFuncional.xml";

    public ProgressaoFuncionalController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) dependente(s) como lote
     *
     * @param idpccs
     * @param comparation
     * @param i_ano_inicial
     * @return
     */
    public ResultSet getProgressaoFuncionalBatch(String idpccs, String comparation, Integer i_ano_inicial,
            String end_date) {
        String sqlRaw = "select first 1 r1.*, cl.classe, cl.i_ano_inicial, cl.i_ano_final, "
                + "pccs.dataatocriacao DataAto, pccs.atocriacao NumeroAto, pccs.veiculopublicacaoatocriacao VeiculoPublicacao "
                + "from referencias r1 "
                + "join classes cl on cl.idclasse = r1.idclasse and cl.idpccs = r1.idpccs "
                + "join pccs on pccs.idpccs = cl.idpccs "
                + "where r1.idpccs = '" + idpccs + "' and cl.i_ano_inicial <"
                + comparation + " " + i_ano_inicial
                + " and r1.d_data <= '" + end_date + "' order by cl.i_ano_final desc, r1.d_data desc";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("", "", "", sqlRaw, false);
        return tabelaRecebe;
    }

    public ResultSet getServidoresBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "s.cpf, s.servidor, s.idservidor, s.d_admissao, m.idpccs, m.ano, m.mes, m.parcela, p.d_situacao, "
                + "ma.ano anoa, ma.mes mesa, ma.parcela parcelaa, ma.idpccs pcca, pa.d_situacao d_situacaoa";
        String sqlRaw = "select " + select + " from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join parametros p on p.ano = m.ano and p.mes = m.mes and p.parcela = m.parcela "
                + "left join parametros pa on pa.ano = extract(year from dateadd(month, -1, p.d_situacao)) and pa.mes = extract(month from dateadd(month, -1, p.d_situacao)) and pa.parcela = p.parcela "
                + "left join mensal ma on ma.idservidor = m.idservidor and ma.ano = pa.ano and ma.mes = pa.mes and ma.parcela = pa.parcela "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '"
                + idServidorF + "' and m.ano = '" + MGSiap.getOpcoes().getAno()
                + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and (select sum(n_valor) from financeiro f where f.idservidor = s.idservidor and f.ano = m.ano and f.mes = m.mes "
                + "and f.parcela = m.parcela and f.idevento in ('001','002','003') and f.n_valor > 0 group by f.idservidor) > 0 "
                + " and ((m.idpccs in (select pccs.idpccs from pccs join classes on classes.idpccs = pccs.idpccs "
                + "group by pccs.idpccs having count(pccs.idpccs) > 1 order by pccs.idpccs)) or (m.idpccs in (select pccs.idpccs from pccs "
                + "join classes on classes.idpccs = pccs.idpccs group by pccs.idpccs having count(pccs.idpccs) > 1 order by pccs.idpccs))) "
                + "group by s.cpf, s.servidor, s.idservidor, s.d_admissao, m.idpccs, m.ano, m.mes, m.parcela, p.d_situacao, "
                + "ma.ano, ma.mes, ma.parcela, ma.idpccs, pa.d_situacao order by s.servidor";
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

            List<ProgressaoFuncional> item = new ArrayList<>();
            Validations v = new Validations();

            boolean error = false;
            if (resultSet.first()) {
                resultSet.beforeFirst();
                while (resultSet.next()) {
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ") Progressão Funcional: ";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    if (resultSet.getString("d_admissao") == null || resultSet.getString("d_admissao").isEmpty()) {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data de admissão inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("d_admissao")).trim());
                        break;
                    }
                    String admissao = resultSet.getString("d_admissao");
                    String d_situacao = resultSet.getString("d_situacao");
                    LocalDate startDate = LocalDate.parse(admissao);
                    LocalDate endDate = LocalDate.parse(d_situacao).with(TemporalAdjusters.lastDayOfMonth());
                    Period period = Period.between(startDate, endDate);
                    Boolean progride = period.getYears() > 2 && period.getYears() % 3 == 0 && period.getMonths() == 0
                            && period.getDays() > 0;
                    // System.out.println("<------------>");
                    // System.out.printf("idservidor: %s\n", resultSet.getString("idservidor"));
                    ResultSet referencia = getProgressaoFuncionalBatch(resultSet.getString("idpccs"),
                            progride ? "=" : "",
                            period.getYears(), endDate.toString());

                    String d_situacaoA = resultSet.getString("d_situacaoa");
                    LocalDate startDateA = LocalDate.parse(admissao);
                    LocalDate endDateA = LocalDate.parse(d_situacaoA).with(TemporalAdjusters.lastDayOfMonth());
                    // System.out.printf("endDate: %s\n", endDateA);
                    Period periodA = Period.between(startDateA, endDateA);
                    Boolean progrideA = periodA.getYears() > 2 && periodA.getYears() % 3 == 0
                            && periodA.getMonths() == 0 && periodA.getDays() > 0;
                    ResultSet referenciaa = getProgressaoFuncionalBatch(resultSet.getString("pcca"),
                            progrideA ? "=" : "",
                            periodA.getYears(), endDateA.toString());
                    // System.out.println("<------------>");
                    referenciaa.next();
                    // if (progride) {
                    // System.out.printf("admissao: %s\n", admissao);
                    // System.out.printf("d_situacao: %s\n", d_situacao);
                    // System.out.printf("startDate: %s\n", startDate);
                    // System.out.println(
                    // String.format("Admissão(%s): %s -> %d anos, %d meses e %d dias. Progride de
                    // classe: %b",
                    // resultSet.getString("idservidor"), admissao, period.getYears(),
                    // period.getMonths(), period.getDays(), progride));
                    // }
                    ProgressaoFuncional progressaoFuncional = new ProgressaoFuncional();
                    // Coloca o ponteiro no primeiro registro
                    if (referenciaa.first() && referencia.first()) {
                        referencia.beforeFirst();
                        while (referencia.next()) {
                            String classeAnterior, classeAtual, nivelAnterior, nivelAtual;
                            classeAnterior = classeAtual = nivelAnterior = nivelAtual = "";
                            /* CPF */
                            if (v.isValueOrError(resultSet.getString("CPF"))
                                    && v.isNumberOrError(resultSet.getString("CPF").trim()
                                            .replaceAll("[^0-9]", ""))
                                    && v.isCPFOrError(resultSet.getString("CPF").trim()
                                            .replaceAll("[^0-9]", ""))) {
                                progressaoFuncional
                                        .setCPF(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""));
                            } else {
                                MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                                sb.append("CPF inválido: '"
                                        + v.isNumberOrEmpty(resultSet.getString("CPF"), 11, "R")
                                                .trim());
                            }
                            /* Matricula */
                            if (v.isValueOrError(resultSet.getString("idservidor"))) {
                                progressaoFuncional.setMatricula(resultSet.getString("idservidor"));
                            } else {
                                MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                                sb.append("Matrícula inválida: '" + resultSet.getString("idservidor") + "', ");
                            }
                            /* Processo */
                            progressaoFuncional.setProcesso("");
                            /* NumeroAto */
                            if (v.isValueOrError(referencia.getString("NumeroAto"))) {
                                progressaoFuncional.setNumeroAto(referencia.getString("NumeroAto"));
                            } else {
                                MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                                sbW.append("Nr do Ato de Publicação inválido: '" + referencia.getString("NumeroAto")
                                        + "', ");
                            }
                            /* DataAto */
                            if (v.isValueOrError(referencia.getString("DataAto"))) {
                                progressaoFuncional.setDataAto(referencia.getString("DataAto"));
                            } else {
                                MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                                sbW.append("Data de Publicação inválido: '" + referencia.getString("DataAto") + "', ");
                            }
                            /* VeiculoPublicacao */
                            if (v.isValueOrError(referencia.getString("VeiculoPublicacao"))) {
                                progressaoFuncional.setVeiculoPublicacao(v.isValueOrEmpty(referencia.getString("VeiculoPublicacao"), 1, "L").trim());
                            } else {
                                MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                                sbW.append(
                                        "Veículo de Publicação inválido: '" + referencia.getString("VeiculoPublicacao")
                                                + "', ");
                            }
                            /* DataInicio */
                            progressaoFuncional.setDataInicio(endDate.toString());
                            /* ClasseAnterior */
                            if (v.isValueOrError(referenciaa.getString("idpccs")) &&
                                    v.isValueOrError(referenciaa.getString("idclasse"))) {
                                classeAnterior = v.isValueOrEmpty(
                                        referenciaa.getString("idpccs") + referenciaa.getString("idclasse"),
                                        10, "L").trim();
                                progressaoFuncional.setClasseAnterior(classeAnterior);
                            } else {
                                MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                                sb.append("Classe Anterior inválida: '" + referenciaa.getString("idpccs")
                                        + referenciaa.getString("idclasse")
                                        + "', ");
                            }
                            /* NivelAnterior */
                            if (v.isValueOrError(referenciaa.getString("idpccs"))) {
                                nivelAnterior = v.isValueOrEmpty(referenciaa.getString("idpccs"), 10, "L")
                                        .trim();
                                progressaoFuncional.setNivelAnterior(nivelAnterior);
                            } else {
                                MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                                sb.append("Nível Anterior inválido: '" + referenciaa.getString("idpccs")
                                        + "', ");
                            }
                            /* Classe */
                            if (v.isValueOrError(referencia.getString("idpccs")) &&
                                    v.isValueOrError(referencia.getString("idclasse"))) {
                                classeAtual = v.isValueOrEmpty(
                                        referencia.getString("idpccs") + referencia.getString("idclasse"),
                                        10, "L").trim();
                                progressaoFuncional.setClasse(classeAtual);
                            } else {
                                MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                                sb.append("Classe inválida: '" + referencia.getString("idpccs")
                                        + referencia.getString("idclasse")
                                        + "', ");
                            }
                            /* Nivel */
                            if (v.isValueOrError(referencia.getString("idpccs"))) {
                                nivelAtual = v.isValueOrEmpty(referencia.getString("idpccs"), 10, "L").trim();
                                progressaoFuncional.setNivel(nivelAtual);
                            } else {
                                MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                                sb.append("Nível inválido: '" + referencia.getString("idpccs")
                                        + "', ");
                            }
                            /* Valor */
                            progressaoFuncional.setValorA(referenciaa.getString("n_valor"));
                            /* ValorD */
                            progressaoFuncional.setValor(referencia.getString("n_valor"));
                            progressaoFuncional.setModel(progressaoFuncional.getCPF()
                                    + progressaoFuncional.getMatricula()
                                    + progressaoFuncional.getProcesso()
                                    + progressaoFuncional.getNumeroAto()
                                    + progressaoFuncional.getDataAto()
                                    + progressaoFuncional.getVeiculoPublicacao()
                                    + progressaoFuncional.getDataInicio()
                                    + progressaoFuncional.getClasseAnterior()
                                    + progressaoFuncional.getNivelAnterior()
                                    + progressaoFuncional.getClasse()
                                    + progressaoFuncional.getNivel()
                                    + progressaoFuncional.getValorA()
                                    + progressaoFuncional.getValor());
                            item.removeIf(x -> (x.getModel() == null ? progressaoFuncional.getModel() == null
                                    : x.getModel().equals(progressaoFuncional.getModel())));
                            if (!(classeAnterior.equalsIgnoreCase(classeAtual)
                                    && nivelAnterior.equalsIgnoreCase(nivelAtual)))
                                if (sb.toString().equalsIgnoreCase(startLog))
                                    item.add(progressaoFuncional);
                        }
                    }
                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    }
                }
                if (error) {
                    Element layout = document.createElement("Informacao");
                    layout.appendChild(document.createTextNode("Arquivo gerado com erros! Ver o log"));
                    root.appendChild(layout);
                }
                // } else {
                // Element layout = document.createElement("ProgressaoFuncional");
                // root.appendChild(layout);
            }

            item.forEach(x -> {
                Element CPF = document.createElement("CPF");
                Element Matricula = document.createElement("Matricula");
                Element Processo = document.createElement("Processo");
                Element NumeroAto = document.createElement("NumeroAto");
                Element DataAto = document.createElement("DataAto");
                Element VeiculoPublicacao = document.createElement("VeiculoPublicacao");
                Element DataInicio = document.createElement("DataInicio");
                Element ClasseAnterior = document.createElement("ClasseAnterior");
                Element NivelAnterior = document.createElement("NivelAnterior");
                Element Classe = document.createElement("Classe");
                Element Nivel = document.createElement("Nivel");
                Element ValorA = document.createElement("ValorA");
                Element Valor = document.createElement("Valor");

                CPF.appendChild(document.createTextNode(x.getCPF()));
                Matricula.appendChild(document.createTextNode(x.getMatricula()));
                Processo.appendChild(document.createTextNode(x.getProcesso()));
                NumeroAto.appendChild(document.createTextNode(x.getNumeroAto() != null ? x.getNumeroAto() : ""));
                DataAto.appendChild(document.createTextNode(x.getDataAto() != null ? x.getDataAto() : ""));
                VeiculoPublicacao.appendChild(document.createTextNode(x.getVeiculoPublicacao() != null ? x.getVeiculoPublicacao() : ""));
                DataInicio.appendChild(document.createTextNode(x.getDataInicio() != null ? x.getDataInicio() : ""));
                ClasseAnterior.appendChild(document.createTextNode(x.getClasseAnterior()));
                NivelAnterior.appendChild(document.createTextNode(x.getNivelAnterior()));
                Classe.appendChild(document.createTextNode(x.getClasse()));
                Nivel.appendChild(document.createTextNode(x.getNivel()));
                ValorA.appendChild(document.createTextNode(x.getValorA()));
                Valor.appendChild(document.createTextNode(x.getValor()));

                Element layout = document.createElement("ProgressaoFuncional");
                layout.appendChild(CPF);
                layout.appendChild(Matricula);
                layout.appendChild(Processo);
                layout.appendChild(NumeroAto);
                layout.appendChild(DataAto);
                layout.appendChild(VeiculoPublicacao);
                layout.appendChild(DataInicio);
                layout.appendChild(ClasseAnterior);
                layout.appendChild(NivelAnterior);
                layout.appendChild(Classe);
                layout.appendChild(Nivel);

                root.appendChild(layout);
            });

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
                                    + "and meta = 'progressaoFuncional'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','progressaoFuncional','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(ProgressaoFuncionalController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(ProgressaoFuncionalController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
