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
public class FuncaoGratificadaCargoComissionadoController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "FuncaoGratificadaCargoComissionado.xml";

    public FuncaoGratificadaCargoComissionadoController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getFuncaoGratificadaCargoComissionadoBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "S.IDVINCULO, M.IDCARGO, M.IDPCCS, CG.CARGO, SO.CODIGO, CG.DATACRIACAO, CG.DATAATOCRIACAO, CG.ATOCRIACAO, CG.VEICULOPUBLICACAOATOCRIACAO, M.ANO, M.MES, M.PARCELA ";
        String sqlSalario = "(select first 1 A.N_VALOR from REFERENCIAS A where A.IDPCCS = M.IDPCCS and A.D_DATA <= '"
                + MGSiap.getOpcoes().getAno() + "-" + MGSiap.getOpcoes().getMes()
                + "-01' order by A.D_DATA desc) SALARIO ";
        String sqlRaw = "select " + select + ", " + sqlSalario + " from CARGOS CG "
                + "join MENSAL M on CG.IDCARGO = M.IDCARGO "
                + "join CENTROS C on C.IDCENTRO = M.IDCENTRO "
                + "join SERVIDORES S on S.IDSERVIDOR = M.IDSERVIDOR "
                + "join SIAPORGAO SO on SO.C_UA = C.CODIGO_UA and SO.CNPJ = replace(replace(replace(C.CNPJ_UA, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' "
                + "and m.ano = '" + MGSiap.getOpcoes().getAno() + "' and m.mes = '" + MGSiap.getOpcoes().getMes() + "' "
                + "and S.IDVINCULO in ('2') "
                + "and CG.TIPOCARGO = '3 - Comissionado' "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "group by " + select + " order by S.IDVINCULO, M.IDCARGO, M.IDPCCS";
        System.out.println("getFuncaoGratificadaCargoComissionadoBatch");
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
                    String idVinculo = "";
                    String idCargo = "";
                    String idPcc = "";
                    String startLog = "";

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
                        startLog = "Função Gratificada (" + cgCodigo + ")";
                        sb.append(startLog);
                    } else {
                        // Codigo.appendChild(document.createTextNode(""));
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Função Gratificada (" + cgCodigo + ") com erro, ");
                    }
                    Element Codigo = document.createElement("Codigo");
                    Element Nome = document.createElement("Nome");
                    Element CodigoOrgao = document.createElement("CodigoOrgao");
                    Element DataCriacaoFG = document.createElement("DataCriacaoFG");
                    Element DataAtoCriacaoFG = document.createElement("DataAtoCriacaoFG");
                    Element AtoCriacaoFG = document.createElement("AtoCriacaoFG");
                    Element VeiculoPublicacaoAtoCriacao = document.createElement("VeiculoPublicacaoAtoCriacao");
                    Element DataExtincao = document.createElement("DataExtincao");
                    Element DataAtoExtincao = document.createElement("DataAtoExtincao");
                    Element AtoExtincao = document.createElement("AtoExtincao");
                    Element VeiculoPublicacaoAtoExtincao = document.createElement("VeiculoPublicacaoAtoExtincao");
                    Element ValorGratificacao = document.createElement("ValorGratificacao");
                    Element Percentual = document.createElement("Percentual");

                    /* Codigo */
                    if (v.isValueOrError(cgCodigo)) {
                        Codigo.appendChild(document.createTextNode(cgCodigo));
                    } else {
                        // Codigo.appendChild(document.createTextNode(""));
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo do cargo inválido: '" + cgCodigo + "', ");
                    }
                    /* Nome */
                    if (v.isValueOrError(resultSet.getString("CARGO"))) {
                        Nome.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("CARGO")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nome inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("CARGO")).trim() + "', ");
                    }
                    /* CodigoOrgao */
                    if (v.isValueOrError(resultSet.getString("Codigo"))) {
                        CodigoOrgao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Codigo"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo Orgao inválido: '" + resultSet.getString("Codigo") + "', ");
                    }
                    // if (v.isValueOrError(resultSet.getString("DataCriacao"))) {
                    DataCriacaoFG.appendChild(
                            document.createTextNode(v.isValueOrEmpty(resultSet.getString("DataCriacao")).trim()));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("DataCriacaoFG inválida: '" +
                    // v.isValueOrEmpty(resultSet.getString("DataCriacao")) + "', ");
                    // }
                    /* DataAtoCriacaoFG */
                    if (v.isValueOrError(resultSet.getString("DataAtoCriacao"))) {
                        DataAtoCriacaoFG.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataAtoCriacaoFG inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("DataAtoCriacao")) + "', ");
                    }
                    /* AtoCriacaoFG */
                    if (v.isValueOrError(resultSet.getString("AtoCriacao"))) {
                        AtoCriacaoFG.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("AtoCriacao"), 32,
                                        "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("AtoCriacaoFG inválido: '" + resultSet.getString("AtoCriacao") +
                                "', ");
                    }
                    /* VeiculoPublicacaoAtoCriacao */
                    if (v.isValueOrError(resultSet.getString("VeiculoPublicacaoAtoCriacao"))) {
                        VeiculoPublicacaoAtoCriacao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("VeiculoPublicacaoAtoCriacao"), 1,
                                        "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Veiculo de Publicacao de Criacao inválido: '"
                                + resultSet.getString("VeiculoPublicacaoAtoCriacao") + "', ");
                    }

                    DataExtincao.appendChild(document.createTextNode(""));
                    DataAtoExtincao.appendChild(document.createTextNode(""));
                    AtoExtincao.appendChild(document.createTextNode(""));
                    VeiculoPublicacaoAtoExtincao.appendChild(document.createTextNode(""));

                    /* ValorGratificacao */
                    if (v.isDecimalOrError(resultSet.getString("salario"))) {
                        ValorGratificacao.appendChild(document.createTextNode(resultSet.getString("salario")));
                    } else {
                        MGSiap.toLogs("Função Gratificada (Vínculo | Cargo | PCC)" + idVinculo + " | " + idCargo + " | "
                                + idPcc + ": ValorGratificacao inválido: '"
                                + resultSet.getString("salario") + "'", 1);
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        // sb.append("ValorGratificacao inválido: '"
                        // + resultSet.getString("salario") + "', ");
                    }

                    /* Percentual */
                    Percentual.appendChild(document.createTextNode("0.00"));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("FuncaoGratificadaCargoComissionado");
                        layout.appendChild(Codigo);
                        layout.appendChild(Nome);
                        layout.appendChild(CodigoOrgao);
                        layout.appendChild(DataCriacaoFG);
                        layout.appendChild(DataAtoCriacaoFG);
                        layout.appendChild(AtoCriacaoFG);
                        layout.appendChild(VeiculoPublicacaoAtoCriacao);
                        layout.appendChild(DataExtincao);
                        layout.appendChild(DataAtoExtincao);
                        layout.appendChild(AtoExtincao);
                        layout.appendChild(VeiculoPublicacaoAtoExtincao);
                        layout.appendChild(ValorGratificacao);
                        layout.appendChild(Percentual);

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
                                    + "and meta = 'funcaoGratificadaCargoComissionado'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','funcaoGratificadaCargoComissionado','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(FuncaoGratificadaCargoComissionadoController.class.getName()).log(Level.SEVERE,
                            null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(FuncaoGratificadaCargoComissionadoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
