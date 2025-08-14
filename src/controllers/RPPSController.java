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
import mgsiap.MGSiapRPPS;
import validations.Validations;

/**
 *
 * @author TomMe
 */
public class RPPSController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "RPPS.xml";

    public RPPSController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) RPPS(s) como lote
     *
     * @param beneficiarios
     * @return
     */
    public ResultSet getRPPSBatch(String beneficiarios) {
        String sqlComplementar = "left join esocial_params ep on 1=1 "
                + "left join siaporgao so on so.cnpj = replace(replace(replace(o.idorgao, '/', ''), '-', ''), '.', '') "
                + "where so.cardug = '" + MGSiapRPPS.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "rows 1"; // Limita a apenas 1 registro
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("orgao o", "", sqlComplementar, "", false);
        return tabelaRecebe;
    }

    public void toXmlFile(ResultSet resultSet) {
        MGSiapRPPS.toLogs(false, "Executando o Leiaute " + fileName, 0);
        try {

            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("SIAP");
            document.appendChild(root);

            Element codigo = document.createElement("Codigo");
            Element exercicio = document.createElement("Exercicio");
            Element mes = document.createElement("Mes");

            codigo.appendChild(
                    document.createTextNode(MGSiapRPPS.getOpcoes().getCodigoOrgao().substring(0, 6)));
            exercicio.appendChild(document.createTextNode(MGSiapRPPS.getOpcoes().getAno()));
            mes.appendChild(document.createTextNode(MGSiapRPPS.getOpcoes().getMes()));

            root.appendChild(codigo);
            root.appendChild(exercicio);
            root.appendChild(mes);

            Validations v = new Validations();

            boolean error = false;
            if (resultSet.first()) {
                resultSet.beforeFirst();
                while (resultSet.next()) {
                    String startLog = "RPPS " + resultSet.getString("ORGAO") + " ("
                            + resultSet.getString("IDORGAO") + "): ";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    
                    Element CNPJEnteFederativo = document.createElement("CNPJEnteFederativo");
                    Element NomeEnteFederativo = document.createElement("NomeEnteFederativo");
                    Element CNPJRPPS = document.createElement("CNPJRPPS");
                    Element DataCriacao = document.createElement("DataCriacao");
                    Element TipoAto = document.createElement("TipoAto");
                    Element AtoCriacao = document.createElement("AtoCriacao");
                    Element DataAto = document.createElement("DataAto");
                    Element DataPublicacao = document.createElement("DataPublicacao");
                    Element VeiculoPublicacao = document.createElement("VeiculoPublicacao");
                    Element Ementa = document.createElement("Ementa");
                    Element TipoRegime = document.createElement("TipoRegime");
                    Element TipoMassa = document.createElement("TipoMassa");

                    // CNPJEnteFederativo - vem do CNPJ_EFR da tabela ESOCIAL_PARAMS
                    if (v.isValueOrError(resultSet.getString("CNPJ_EFR"))
                            && v.isNumberOrError(resultSet.getString("CNPJ_EFR").trim()
                                    .replaceAll("[^0-9]", ""))
                            && resultSet.getString("CNPJ_EFR").trim().replaceAll("[^0-9]", "").length() == 14) {
                        CNPJEnteFederativo.appendChild(
                                document.createTextNode(resultSet.getString("CNPJ_EFR")
                                        .trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("CNPJ Ente Federativo inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("CNPJ_EFR"), 14, "R")
                                        .trim() + "', ");
                    }

                    // NomeEnteFederativo - Nome do órgão
                    if (v.isValueOrError(resultSet.getString("ORGAO"))) {
                        NomeEnteFederativo.appendChild(document
                                .createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "ORGAO"), 255, "R")
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Nome Ente Federativo inválido: '" + resultSet.getString("ORGAO") + "', ");
                    }

                    // CNPJRPPS - CNPJ do próprio RPPS (pode ser o mesmo campo IDORGAO ou um campo específico)
                    String cnpjRpps = resultSet.getString("IDORGAO"); // Assumindo que IDORGAO contém o CNPJ do RPPS
                    if (v.isValueOrError(cnpjRpps)
                            && v.isNumberOrError(cnpjRpps.trim().replaceAll("[^0-9]", ""))
                            && cnpjRpps.trim().replaceAll("[^0-9]", "").length() == 14) {
                        CNPJRPPS.appendChild(
                                document.createTextNode(cnpjRpps.trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("CNPJ RPPS inválido: '"
                                + v.isValueOrEmpty(cnpjRpps, 14, "R").trim() + "', ");
                    }

                    // DataCriacao - Data de criação do RPPS da tabela SIAPORGAO
                    if (v.isValueOrError(resultSet.getString("DATACRIACAO"))) {
                        DataCriacao.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "DATACRIACAO"))
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Data Criação inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("DATACRIACAO"))
                                + "', ");
                    }

                    // TipoAto - 1=Lei, 2=Decreto (definir valor padrão ou buscar de outro campo)
                    TipoAto.appendChild(document.createTextNode("1")); // Padrão: Lei

                    // AtoCriacao - Ato de criação da tabela SIAPORGAO
                    if (v.isValueOrError(resultSet.getString("ATOCRIACAO"))) {
                        AtoCriacao.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "ATOCRIACAO"), 32, "R")
                                        .trim()));
                    } else {
                        String atoCriacao = v.isValueOrEmpty("Lei de Criação", 32, "R"); // Valor padrão
                        AtoCriacao.appendChild(document.createTextNode(atoCriacao));
                    }

                    // DataAto - Data do ato de criação da tabela SIAPORGAO
                    if (v.isValueOrError(resultSet.getString("DATAATOCRIACAO"))) {
                        DataAto.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "DATAATOCRIACAO"))
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Data Ato inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("DATAATOCRIACAO"))
                                + "', ");
                    }

                    // DataPublicacao - Usa a mesma data do ato de criação
                    if (v.isValueOrError(resultSet.getString("DATAATOCRIACAO"))) {
                        DataPublicacao.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "DATAATOCRIACAO"))
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Data Publicação inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("DATAATOCRIACAO"))
                                + "', ");
                    }

                    // VeiculoPublicacao - Baseado no campo VEICULOPUBLICACAOATOCRIACAO ou valor padrão
                    String veiculoPublicacao = resultSet.getString("VEICULOPUBLICACAOATOCRIACAO");
                    if (v.isValueOrError(veiculoPublicacao)) {
                        // Mapear o texto para código conforme necessário
                        // Por enquanto usando valor padrão, mas pode ser mapeado conforme regras específicas
                        VeiculoPublicacao.appendChild(document.createTextNode("1"));
                    } else {
                        VeiculoPublicacao.appendChild(document.createTextNode("1")); // Padrão: Diário Oficial da União
                    }

                    // Ementa - Baseada no veículo de publicação ou descrição padrão
                    String veiculoPublicacaoTexto = resultSet.getString("VEICULOPUBLICACAOATOCRIACAO");
                    String ementa;
                    if (v.isValueOrError(veiculoPublicacaoTexto)) {
                        ementa = "Criação do Regime Próprio de Previdência Social - " + 
                                v.isValueOrEmpty(resultSet.getString("ORGAO"), 255, "R").trim() +
                                ". Publicado em: " + veiculoPublicacaoTexto.trim();
                    } else {
                        ementa = "Criação do Regime Próprio de Previdência Social - " + 
                                v.isValueOrEmpty(resultSet.getString("ORGAO"), 255, "R").trim();
                    }
                    Ementa.appendChild(document.createTextNode(v.isValueOrEmpty(ementa, 1024, "R")));

                    // TipoRegime - Tipo do regime previdenciário (valor padrão: 1)
                    TipoRegime.appendChild(document.createTextNode("1"));

                    // TipoMassa - 1=Massa única, 2=Massa segregada (valor padrão)
                    TipoMassa.appendChild(document.createTextNode("1"));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sbW.toString(), MGSiapRPPS.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sb.toString(), MGSiapRPPS.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("RPPS");
                        layout.appendChild(CNPJEnteFederativo);
                        layout.appendChild(NomeEnteFederativo);
                        layout.appendChild(CNPJRPPS);
                        layout.appendChild(DataCriacao);
                        layout.appendChild(TipoAto);
                        layout.appendChild(AtoCriacao);
                        layout.appendChild(DataAto);
                        layout.appendChild(DataPublicacao);
                        layout.appendChild(VeiculoPublicacao);
                        layout.appendChild(Ementa);
                        layout.appendChild(TipoRegime);
                        layout.appendChild(TipoMassa);

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
            // Este arquivo deve ser gerado para pasta 0 e 1 de MGSiapRPPS.getFileFolder
                for (int folderIndex = 0; folderIndex <= 1; folderIndex++) {
                    try {
                        String xmlFilePath = MGSiapRPPS.getFileFolder(folderIndex) + fileName;
                        if (error)
                            xmlFilePath = MGSiapRPPS.getFileFolder(folderIndex) + "Com_Erros_" + fileName;
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource domSource = new DOMSource(document);
                        StreamResult streamResult = new StreamResult(new File(xmlFilePath));
                        transformer.transform(domSource, streamResult);
                        MGSiapRPPS.toLogs(false, "Arquivo XML " + fileName + " salvo em: " + xmlFilePath, 0);
                    } catch (TransformerException ex) {
                        Logger.getLogger(RPPSController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                try {
                    ResultSet tabelaAuxiliares = bDCommands.getTabelaGenerico("", "", "",
                            "select count(*) from auxiliares where dominio = 'siap' "
                                    + "and meta = 'rpps'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql(
                                "insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                        + "(select coalesce(max(id)+1,1) from auxiliares),"
                                        + "(select timestamp 'NOW' from rdb$database),"
                                        + "'siap','rpps','exec')");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(RPPSController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(RPPSController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
