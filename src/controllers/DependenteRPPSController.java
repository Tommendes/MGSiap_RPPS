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
public class DependenteRPPSController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "DependenteRPPS.xml";

    public DependenteRPPSController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) DependenteRPPS(s) como lote
     *
     * @param idBeneficiarioI
     * @param idBeneficiarioF
     * @return
     */
    public ResultSet getDependenteRPPSBatch(String idBeneficiarioI, String idBeneficiarioF) {
        idBeneficiarioI = String.format("%1$8s", idBeneficiarioI).replace(" ", "0");
        idBeneficiarioF = String.format("%1$8s", idBeneficiarioF).replace(" ", "0");
        String select = "S.IDSERVIDOR, S.CPF, D.CPF_DEP, D.DEPENDENTE, D.D_NASCIMENTO, D.TIPO, M.ANO, M.MES, M.PARCELA";
        String sqlRaw = "select " + select + " from DEPENDENTES D "
                + "join SERVIDORES S on D.IDSERVIDOR = S.IDSERVIDOR "
                + "left join MENSAL M on M.IDSERVIDOR = S.IDSERVIDOR "
                + "left join CENTROS C on C.IDCENTRO = M.IDCENTRO "
                + "left join SIAPORGAO SO on SO.C_UA = C.CODIGO_UA and SO.CNPJ = replace(replace(replace(C.CNPJ_UA, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idBeneficiarioI + "' AND '" + idBeneficiarioF + "' "
                + "and ano = '" + MGSiapRPPS.getOpcoes().getAno() + "' and mes = '"
                + MGSiapRPPS.getOpcoes().getMes() + "' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                + "and S.IDVINCULO in ('4', '5') "
                + "and so.cardug = '" + MGSiapRPPS.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "order by s.servidor";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("", "", "", sqlRaw, false);
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
                    String startLog = "DependenteRPPS " + resultSet.getString("dependente") + " ("
                            + resultSet.getString("IDSERVIDOR") + "): ";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element CPFDependente = document.createElement("CPFDependente");
                    Element NomeDependente = document.createElement("NomeDependente");
                    Element DataNascimento = document.createElement("DataNascimento");
                    Element GrauParentesco = document.createElement("GrauParentesco");

                    // CPF
                    if (v.isValueOrError(resultSet.getString("CPF"))
                            && v.isNumberOrError(resultSet.getString("CPF").trim()
                                    .replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF").trim()
                                    .replaceAll("[^0-9]", ""))) {
                        CPF.appendChild(
                                document.createTextNode(resultSet.getString("CPF")
                                        .trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("CPF inválido: '"
                                + v.isNumberOrEmpty(resultSet.getString("CPF"), 11, "R")
                                        .trim());
                    }
                    // CPFDependente
                    if (v.isValueOrError(resultSet.getString("CPF_DEP"))
                            && v.isNumberOrError(resultSet.getString("CPF_DEP").trim()
                                    .replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF_DEP").trim()
                                    .replaceAll("[^0-9]", ""))) {
                        CPFDependente.appendChild(
                                document.createTextNode(resultSet.getString("CPF_DEP")
                                        .trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("CPF Dependente inválido: '"
                                + v.isNumberOrEmpty(resultSet.getString("CPF_DEP"), 11, "R")
                                        .trim() + "', ");
                    }
                    // NomeDependente
                    if (v.isValueOrError(resultSet.getString("dependente"))) {
                        NomeDependente.appendChild(document
                                .createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "dependente"), 255, "R")
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("NomeDependente inválido: '" + resultSet.getString("dependente") + "', ");
                    }
                    // DataNascimento
                    if (v.isValueOrError(resultSet.getString("D_NASCIMENTO"))) {
                        DataNascimento.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "D_NASCIMENTO"))
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Data Nascimento inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("D_NASCIMENTO"))
                                + "', ");
                    }

                    /* Graus de parentesco */
                    // 1. Cônjuge
                    // 2. Companheiro(a) com o(a) qual tenha filho ou viva há mais de 5 (cinco) anos
                    // ou possua declaração de união estável.
                    // 3. Filho(a) ou enteado(a)
                    // 4. Filho(a) ou enteado(a), universitário(a) ou cursando escola técnica de 2º
                    // grau
                    // 5. Irmão(ã), neto(a) ou bisneto(a) sem arrimo dos pais, do(a) qual detenha a
                    // guarda judicial.
                    // 6. Irmão(ã), neto(a) ou bisneto(a) sem arrimo dos pais, universitário(a) ou
                    // cursando escola técnica de 2° grau, do(a) qual detenha a guarda judicial.
                    // 7. Pais, avós e bisavós
                    // 8. Menor pobre do qual detenha a guarda judicial.
                    // 9. A pessoa absolutamente incapaz, da qual seja tutor ou curador.
                    // 10. Ex-cônjuge
                    // 11. Agregado/Outros
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
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("TIPO de dependência inválida: '" + resultSet.getString("TIPO") + "', ");
                    }

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sbW.toString(), MGSiapRPPS.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sb.toString(), MGSiapRPPS.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("DependenteRPPS");
                        layout.appendChild(CPF);
                        layout.appendChild(NomeDependente);
                        layout.appendChild(CPF);
                        layout.appendChild(DataNascimento);
                        layout.appendChild(GrauParentesco);

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
                    String xmlFilePath = MGSiapRPPS.getFileFolder(1) + fileName;
                    if (error)
                        xmlFilePath = MGSiapRPPS.getFileFolder(1) + "Com_Erros_" + fileName;
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);
                    StreamResult streamResult = new StreamResult(new File(xmlFilePath));
                    transformer.transform(domSource, streamResult);
                    MGSiapRPPS.toLogs(false, "Arquivo XML " + fileName + " salvo em: " + xmlFilePath, 0);

                    ResultSet tabelaAuxiliares = bDCommands.getTabelaGenerico("", "", "",
                            "select count(*) from auxiliares where dominio = 'siap' "
                                    + "and meta = 'dependentes'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql(
                                "insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                        + "(select coalesce(max(id)+1,1) from auxiliares),"
                                        + "(select timestamp 'NOW' from rdb$database),"
                                        + "'siap','dependentes','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(DependenteRPPSController.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(DependenteRPPSController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
