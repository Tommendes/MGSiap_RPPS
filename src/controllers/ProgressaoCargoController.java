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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import config.BDCommands;
import mgsiap.MGSiap;
import models.ProgressaoCargo;
import validations.Validations;

/**
 *
 * @author TomMe
 */
public class ProgressaoCargoController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "ProgressaoCargo.xml";

    public ProgressaoCargoController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura dos dados de referências e classes do servidor
     * 
     * @param idpccs
     * @param comparation
     * @param i_ano_inicial
     * @return
     */
    public ResultSet getProgressaoCargoBatch(String idpccs, String comparation, Integer i_ano_inicial) {
        String sqlRaw = "select first 1 r1.*, cl.classe, cl.i_ano_inicial, cl.i_ano_final "
                + "from referencias r1 "
                + "join classes cl on cl.idclasse = r1.idclasse and cl.idpccs = r1.idpccs "
                + "where r1.idpccs = '" + idpccs + "' and cl.i_ano_inicial <"
                + comparation + " " + i_ano_inicial
                + " order by cl.i_ano_final desc, r1.d_data desc";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("", "", "", sqlRaw, false);
        return tabelaRecebe;
    }

    /**
     * Captura os dados do(s) servidores(s) como lote
     * 
     * @param idServidorI
     * @param idServidorF
     * @return
     */
    public ResultSet getServidoresBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "s.cpf, s.servidor, s.idservidor, s.d_admissao, m.idpccs, s.idvinculo, cg.idcargo, m.ano, m.mes, m.parcela, p.d_situacao";
        String sqlRaw = "select " + select + " from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join parametros p on p.ano = m.ano and p.mes = m.mes and p.parcela = m.parcela "
                + "join cargos cg on cg.idcargo = m.idcargo "
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

            List<ProgressaoCargo> item = new ArrayList<>();
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
                    + resultSet.getString("IDSERVIDOR") + ") (Progressão): ";

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
                        sb.append("Progressão Cargo (" + v.isValueOrError(resultSet.getString("idcargo")) + ") com erro, ");
                    }
                    
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
                    Boolean progride = period.getYears() > 2 && period.getYears() % 3 == 0 && period.getDays() > 0;
                    ResultSet referencia = getProgressaoCargoBatch(resultSet.getString("idpccs"), progride ? "=" : "",
                            period.getYears());
                    // System.out.println(String.format("Admissão: %s -> %d anos, %d meses e %d
                    // dias. Progride de classe: %b",
                    // admissao, period.getYears(), period.getMonths(), period.getDays(), progride)
                    // );

                    String idpccs = null;
                    String idclasse = null;
                    String idreferencia = null;
                    ProgressaoCargo progressaoCargo = new ProgressaoCargo();
                    // Coloca o ponteiro no primeiro registro
                    if (referencia.first()) {
                        referencia.beforeFirst();
                        while (referencia.next()) {
                            idpccs = StringUtils.right(referencia.getString("idpccs"), 3);
                            idclasse = StringUtils.right(referencia.getString("idclasse"), 3);
                            idreferencia = StringUtils.right(referencia.getString("idreferencia"), 3);

                            progressaoCargo.setCodigo(idpccs + idclasse + idreferencia);
                            progressaoCargo.setNome(idpccs + "." + idclasse + "." + idreferencia);
                            progressaoCargo.setCodigoCargo(cgCodigo);
                            progressaoCargo.setCodigoClasse(resultSet.getString("idpccs") + referencia.getString("idclasse"));
                            progressaoCargo.setCodigoNivel(resultSet.getString("idpccs"));
                            progressaoCargo.setValorSalario(referencia.getString("n_valor"));
                            progressaoCargo.setModel(progressaoCargo.getCodigo()
                                    + progressaoCargo.getNome()
                                    + progressaoCargo.getCodigoCargo()
                                    + progressaoCargo.getCodigoClasse()
                                    + progressaoCargo.getCodigoNivel()
                                    + progressaoCargo.getValorSalario());
                            item.removeIf(x -> (x.getModel() == null ? progressaoCargo.getModel() == null
                                    : x.getModel().equals(progressaoCargo.getModel())));
                            if (sb.toString().equalsIgnoreCase(startLog))
                                item.add(progressaoCargo);
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
                // Element layout = document.createElement("ProgressaoCargo");
                // root.appendChild(layout);
            }

            item.forEach(x -> {
                Element Codigo = document.createElement("Codigo");
                Element Nome = document.createElement("Nome");
                Element CodigoCargo = document.createElement("CodigoCargo");
                Element CodigoClasse = document.createElement("CodigoClasse");
                Element CodigoNivel = document.createElement("CodigoNivel");
                Element ValorSalario = document.createElement("ValorSalario");
                Codigo.appendChild(document.createTextNode(x.getCodigo() != null ? x.getCodigo() : ""));
                Nome.appendChild(document.createTextNode(x.getNome() != null ? x.getNome() : ""));
                CodigoCargo.appendChild(document.createTextNode(x.getCodigoCargo() != null ? x.getCodigoCargo() : ""));
                CodigoClasse.appendChild(document.createTextNode(x.getCodigoClasse() != null ? x.getCodigoClasse() : ""));
                CodigoNivel.appendChild(document.createTextNode(x.getCodigoNivel()));
                ValorSalario.appendChild(document.createTextNode(x.getValorSalario()));

                Element layout = document.createElement("ProgressaoCargo");
                layout.appendChild(Codigo);
                layout.appendChild(Nome);
                layout.appendChild(CodigoCargo);
                layout.appendChild(CodigoClasse);
                layout.appendChild(CodigoNivel);
                layout.appendChild(ValorSalario);

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
                                    + "and meta = 'progressaoCargo'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','progressaoCargo','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(ProgressaoCargoController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(ProgressaoCargoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
