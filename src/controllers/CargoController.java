/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
public class CargoController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Cargo.xml";

    public CargoController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getCargoBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");

        String d_situacao = MGSiap.getOpcoes().getAno() + "-" + MGSiap.getOpcoes().getMes() + "-01";
        LocalDate endDate = LocalDate.parse(d_situacao).with(TemporalAdjusters.lastDayOfMonth());
        String select = "s.idvinculo, cargos.idcargo, "
                + "cargos.cargo, cargos.datacriacao, cargos.dataatocriacao, "
                + "cargos.atocriacao, cargos.veiculopublicacaoatocriacao, "
                + "cargos.TipoCargo, cargos.acumulavel, cargos.contagemespecial, "
                + "cargos.habilitacaolegal, cargos.dedicacaoexclusiva, "
                + "cargos.aposentadoriaespecial, cargos.escolaridademinima, "
                + "m.idpccs, m.ano, m.mes, m.parcela";
        String specialSelect = "coalesce((select first 1 A.N_VALOR from REFERENCIAS A where A.IDPCCS = m.idpccs and  A.D_DATA <= '"
                + endDate + "' order by A.D_DATA desc),0) salario";
        String sqlRaw = "select " + select + "," + specialSelect + " from cargos "
                + "join mensal m on m.idcargo = cargos.idcargo "
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
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício 2024
                + "and s.idvinculo not in('11') "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "group by " + select + " order by cargos.cargo";
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
                    String startLog = "Cargo " + resultSet.getString("cargo") + "(" + resultSet.getString("idcargo")
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
                    Element DataExtincao = document.createElement("DataExtincao");
                    Element DataAtoExtincao = document.createElement("DataAtoExtincao");
                    Element AtoExtincao = document.createElement("AtoExtincao");
                    Element VeiculoPublicacaoAtoExtincao = document.createElement("VeiculoPublicacaoAtoExtincao");
                    Element TipoCargo = document.createElement("TipoCargo");
                    Element Acumulavel = document.createElement("Acumulavel");
                    Element ContagemEspecial = document.createElement("ContagemEspecial");
                    Element HabilitacaoLegal = document.createElement("HabilitacaoLegal");
                    Element DedicacaoExclusiva = document.createElement("DedicacaoExclusiva");
                    Element AposentadoriaEspecial = document.createElement("AposentadoriaEspecial");
                    Element EscolaridadeMinima = document.createElement("EscolaridadeMinima");
                    Element Salario = document.createElement("Salario");
                    Element Percentual = document.createElement("Percentual");
                    Element CodigoCarreira = document.createElement("CodigoCarreira");
                    if (v.isValueOrError(cgCodigo)) {
                        Codigo.appendChild(document.createTextNode(cgCodigo));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo inválido: '" + cgCodigo + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("cargo"))) {
                        Nome.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("cargo"), 255, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nome inválido: '" + resultSet.getString("cargo") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("DataCriacao"))) {
                        DataCriacao.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("DataCriacao")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Data de Criacao inválida: '" + v.isValueOrEmpty(resultSet.getString("DataCriacao"))
                                + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("DataAtoCriacao"))) {
                        DataAtoCriacao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data de Ato de Criacao inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")) + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("AtoCriacao"))) {
                        AtoCriacao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoCriacao"), 32, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Ato de Criacao inválido: '" + resultSet.getString("AtoCriacao") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("VeiculoPublicacaoAtoCriacao"))) {
                        VeiculoPublicacaoAtoCriacao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoAtoCriacao"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Veiculo de Publicacao de Criacao inválido: '"
                                + resultSet.getString("VeiculoPublicacaoAtoCriacao") + "', ");
                    }
                    DataExtincao.appendChild(document.createTextNode(""));
                    DataAtoExtincao.appendChild(document.createTextNode(""));
                    AtoExtincao.appendChild(document.createTextNode(""));
                    VeiculoPublicacaoAtoExtincao.appendChild(document.createTextNode(""));
                    if (v.isValueOrError(resultSet.getString("TipoCargo"))) {
                        TipoCargo.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("TipoCargo"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Tipo de Cargo inválido: '"
                                + resultSet.getString("TipoCargo") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("Acumulavel"))) {
                        Acumulavel.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Acumulavel"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Acumulavel inválido: '" + resultSet.getString("Acumulavel") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("ContagemEspecial"))) {
                        ContagemEspecial.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("ContagemEspecial"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Contagem Especial inválida: '" + resultSet.getString("ContagemEspecial") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("HabilitacaoLegal"))) {
                        HabilitacaoLegal.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("HabilitacaoLegal"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Habilitacao Legal inválido: '" + resultSet.getString("HabilitacaoLegal") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("DedicacaoExclusiva"))) {
                        DedicacaoExclusiva.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("DedicacaoExclusiva"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append(
                                "Dedicacao Exclusiva inválido: '" + resultSet.getString("DedicacaoExclusiva") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("AposentadoriaEspecial"))) {
                        AposentadoriaEspecial.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("AposentadoriaEspecial"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Aposentadoria Especial inválido: '" + resultSet.getString("AposentadoriaEspecial")
                                + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("EscolaridadeMinima"))) {
                        EscolaridadeMinima.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("EscolaridadeMinima"), 1, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append(
                                "Escolaridade Minima inválido: '" + resultSet.getString("EscolaridadeMinima") + "', ");
                    }
                    if (v.isDecimalOrError(resultSet.getString("Salario"))) {
                        Salario.appendChild(document
                                .createTextNode(v.isDecimalOrEmpty(resultSet.getString("Salario"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Salário inválido: '" + resultSet.getString("Salario") + "', ");
                    }
                    Percentual.appendChild(document.createTextNode(""));
                    if (v.isValueOrError(resultSet.getString("idpccs"))) {
                        CodigoCarreira.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("idpccs"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Codigo Carreira inválido: '" + resultSet.getString("idpccs") + "', ");
                    }

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Cargo");
                        layout.appendChild(Codigo);
                        layout.appendChild(Nome);
                        layout.appendChild(DataCriacao);
                        layout.appendChild(DataAtoCriacao);
                        layout.appendChild(AtoCriacao);
                        layout.appendChild(VeiculoPublicacaoAtoCriacao);
                        layout.appendChild(DataExtincao);
                        layout.appendChild(DataAtoExtincao);
                        layout.appendChild(AtoExtincao);
                        layout.appendChild(VeiculoPublicacaoAtoExtincao);
                        layout.appendChild(TipoCargo);
                        layout.appendChild(Acumulavel);
                        layout.appendChild(ContagemEspecial);
                        layout.appendChild(HabilitacaoLegal);
                        layout.appendChild(DedicacaoExclusiva);
                        layout.appendChild(AposentadoriaEspecial);
                        layout.appendChild(EscolaridadeMinima);
                        layout.appendChild(Salario);
                        layout.appendChild(Percentual);
                        layout.appendChild(CodigoCarreira);
                        root.appendChild(layout);
                    }
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
                                    + "and meta = 'cargo'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','cargo','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(CargoController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(CargoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
