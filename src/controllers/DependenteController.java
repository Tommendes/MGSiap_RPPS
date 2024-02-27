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
public class DependenteController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Dependente.xml";

    public DependenteController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getDependentesBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String sqlComplementar = "join servidores s on s.idservidor = dependentes.idservidor "
                + "join mensal m on m.idservidor = s.idservidor "
                + "join centros c on c.idcentro = m.idcentro "
                + "join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' "
                + "and m.ano = '" + MGSiap.getOpcoes().getAno() + "' and m.mes = '" + MGSiap.getOpcoes().getMes()
                + "' and m.parcela = '000' "
                + "and m.situacao = 'ADMITIDO' "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 "
                + "and (dependentes.d_irrf > cast('now' as date)) and dependentes.irpf = 'S' "
                + "group by ff.idservidor) > 0 "
                + "order by s.servidor";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("dependentes", "", sqlComplementar,
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
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ")" + " (Dependente) "
                            + resultSet.getString("dependente") + ": ";
                    StringBuilder sb = new StringBuilder();
                    sb.append(startLog);
                    StringBuilder sbW = new StringBuilder();
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element CPFDependente = document.createElement("CPFDependente");
                    Element NomeDependente = document.createElement("NomeDependente");
                    Element DataNascimento = document.createElement("DataNascimento");
                    Element GrauParentesco = document.createElement("GrauParentesco");

                    if (v.isValueOrError(resultSet.getString("CPF"))
                            && v.isNumberOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))) {
                        CPF.appendChild(
                                document.createTextNode(resultSet.getString("CPF").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CPF inválido: '"
                                + v.isNumberOrEmpty(resultSet.getString("CPF"), 11, "R").trim() + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("CPF_DEP"))
                            && v.isNumberOrError(resultSet.getString("CPF_DEP").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF_DEP").trim().replaceAll("[^0-9]", ""))
                            && !resultSet.getString("CPF_DEP").trim().replaceAll("[^0-9]", "")
                                    .equalsIgnoreCase(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))) {
                        CPFDependente.appendChild(document
                                .createTextNode(resultSet.getString("CPF_DEP").trim().replaceAll("[^0-9]", "")));
                    } else {
                        String repetido = (v.isValueOrError(resultSet.getString("CPF_DEP"))
                                && resultSet.getString("CPF_DEP").trim().replaceAll("[^0-9]", "")
                                        .equalsIgnoreCase(resultSet.getString("CPF").trim().replaceAll("[^0-9]", "")))
                                                ? " ou igual ao CPF do servidor"
                                                : "";
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append(" CPF do dependente "
                                + String.format("%1$4s", resultSet.getString("IDDEPENDENTE")).replace(" ", "0")
                                + " inválido" + repetido + ": '"
                                + v.isNumberOrEmpty(resultSet.getString("CPF_DEP"), 11, "R").trim() + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("dependente"))) {
                        NomeDependente.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("dependente"), 255, "R").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Nome inválido: '" + resultSet.getString("dependente") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("D_NASCIMENTO"))) {
                        DataNascimento.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("D_NASCIMENTO")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataNascimento inválida: '" + resultSet.getString("D_NASCIMENTO") + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("TIPO"))) {
                        String grauParentesco = "11";
                        switch (resultSet.getString("TIPO")) {
                            case "CONJUGE":
                                grauParentesco = "1";
                                break;
                            case "FILHO":
                                grauParentesco = "3";
                                break;
                            case "PAI":
                                grauParentesco = "7";
                                break;
                            case "MAE":
                                grauParentesco = "7";
                                break;
                            case "SOBRINHO":
                                grauParentesco = "11";
                                break;
                            case "NETO":
                                grauParentesco = "5";
                                break;
                            default:
                                grauParentesco = "11";
                                break;
                        }
                        GrauParentesco.appendChild(document.createTextNode(grauParentesco));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("TIPO de dependência inválida: '" + resultSet.getString("TIPO") + "', ");
                    }

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Dependente");
                        layout.appendChild(CPF);
                        layout.appendChild(CPFDependente);
                        layout.appendChild(NomeDependente);
                        layout.appendChild(DataNascimento);
                        layout.appendChild(GrauParentesco);

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
                                    + "and meta = 'dependentes'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','dependentes','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(DependenteController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(DependenteController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
