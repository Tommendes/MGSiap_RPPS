package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class BeneficiarioController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Beneficiario.xml";
    private ArrayList<String> beneficiarios;

    public BeneficiarioController(BDCommands bDCommands, boolean gerarXml, ArrayList<String> beneficiarios) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
        this.beneficiarios = beneficiarios;
    }

    /**
     * Captura os dados do(s) Beneficiario(s) como lote
     *
     * @param idBeneficiarioI
     * @param idBeneficiarioF
     * @return
     */
    public ResultSet getBeneficiarioBatch(String idBeneficiarioI, String idBeneficiarioF) {
        idBeneficiarioI = String.format("%1$8s", idBeneficiarioI).replace(" ", "0");
        idBeneficiarioF = String.format("%1$8s", idBeneficiarioF).replace(" ", "0");
        String sqlComplementar = "left join mensal m on m.idservidor = s.idservidor "
                + "left join centros c on c.idcentro = m.idcentro "
                + "left join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idBeneficiarioI + "' AND '" + idBeneficiarioF + "' "
                + "and ano = '" + MGSiapRPPS.getOpcoes().getAno() + "' and mes = '"
                + MGSiapRPPS.getOpcoes().getMes() + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                + "and S.IDVINCULO in ('1', '4', '5') "
                + "and so.cardug = '" + MGSiapRPPS.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "order by s.servidor";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("servidores s", "", sqlComplementar, "", true);
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
                    String startLog = "Beneficiario " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + "): ";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element Nome = document.createElement("Nome");
                    Element NomeSocial = document.createElement("NomeSocial");
                    Element RG = document.createElement("RG");
                    Element DataExpedicaoRG = document.createElement("DataExpedicaoRG");
                    Element DataNascimento = document.createElement("DataNascimento");
                    Element UFNascimento = document.createElement("UFNascimento");
                    Element CidadeNascimento = document.createElement("CidadeNascimento");
                    Element Sexo = document.createElement("Sexo");
                    Element EstadoCivil = document.createElement("EstadoCivil");
                    Element NomeMae = document.createElement("NomeMae");
                    Element NomePai = document.createElement("NomePai");
                    Element Email = document.createElement("Email");
                    Element TelefoneFixo = document.createElement("TelefoneFixo");
                    Element TelefoneCelular = document.createElement("TelefoneCelular");

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
                    // NOME
                    if (v.isValueOrError(resultSet.getString("servidor"))) {
                        Nome.appendChild(document
                                .createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "servidor"), 255, "R")
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Nome inválido: '" + resultSet.getString("servidor") + "', ");
                    }
                    NomeSocial.appendChild(document.createTextNode(""));
                    // RG
                    if (v.isValueOrError(resultSet.getString("RG"))) {
                        RG.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "RG"), 32, "R")
                                        .trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("RG inválido: '" + resultSet.getString("RG") + "', ");
                    }
                    // DATA EXPEDICAO RG
                    if (v.isValueOrError(resultSet.getString("D_RG"))) {
                        DataExpedicaoRG.appendChild(document.createTextNode(v
                                .isValueOrEmpty(resultSet.getString("D_RG")).trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("DataExpedicaoRG inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("D_RG"))
                                + "', ");
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
                    // UFNascimento
                    if (v.isValueOrError(resultSet.getString("NATURALIDADEUF"))
                            && resultSet.getString("NATURALIDADEUF").trim().length() == 2) {
                        UFNascimento.appendChild(
                                document.createTextNode(resultSet
                                        .getString("NATURALIDADEUF").trim()
                                        .substring(0, 2)));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("UF Nascimento inválido: '"
                                + v.isValueOrEmpty(
                                        resultSet.getString("NATURALIDADEUF"))
                                + "', ");
                    }
                    // CidadeNascimento
                    if (v.isValueOrError(resultSet.getString("NATURALIDADE"))) {
                        CidadeNascimento.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("NATURALIDADE"),
                                        255, "R").trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Cidade Nascimento inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("NATURALIDADE"))
                                + "', ");
                    }
                    Sexo.appendChild(document.createTextNode(v.isFMO(resultSet.getString("SEXO"))));
                    // EstadoCivil
                    if (v.isValueOrError(resultSet.getString("ESTADO_CIVIL"))
                            && v.isMarriage(resultSet.getString("ESTADO_CIVIL")) != null) {
                        EstadoCivil.appendChild(
                                document.createTextNode(v.isMarriage(
                                        resultSet.getString("ESTADO_CIVIL"))));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("EstadoCivil inválido: '"
                                + v.isValueOrEmpty(resultSet
                                        .getString("ESTADO_CIVIL"))
                                + "', ");
                        // EstadoCivil.appendChild(document
                        // .createTextNode(""));
                    }
                    // NomeMae
                    if (v.isValueOrError(resultSet.getString("MAE"))) {
                        NomeMae.appendChild(
                                document.createTextNode(v.isValueOrEmpty(
                                        resultSet.getString("MAE"), 255, "R").trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("NomeMae inválido: '"
                                + v.isValueOrEmpty(resultSet
                                        .getString("MAE"))
                                + "', ");
                    }
                    NomePai.appendChild(
                            document.createTextNode(v.isValueOrEmpty(
                                    resultSet.getString("PAI"), 255, "R").trim()));
                    if (resultSet.getString("EMAIL") != null
                            && !resultSet.getString("EMAIL").isEmpty()
                            && v.isEmailOrError(resultSet.getString("EMAIL"))) {
                        String email = resultSet.getString("EMAIL");
                        Pattern INVALID_XML_CHAR_PATTERN = Pattern
                                .compile(
                                        "[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\\x{10000}-\\x{10FFFF}]");
                        Matcher matcher = INVALID_XML_CHAR_PATTERN.matcher(email);
                        if (matcher.find()) {
                            email = matcher.replaceAll(""); //$NON-NLS-1$
                        }
                        Email.appendChild(document.createTextNode(email));
                    } else {
                        Email.appendChild(document.createTextNode(""));
                    }
                    String telefoneDdd = v.isValueOrEmpty(resultSet.getString("DDD_FONE"));
                    if (!telefoneDdd.isEmpty() && telefoneDdd != null) {
                        telefoneDdd = v.isValueOrEmpty(telefoneDdd.replaceAll("[^0-9]", ""), 2, "L");
                    }
                    String telefone = v.isValueOrEmpty(resultSet.getString("FONE"));
                    if (!telefone.isEmpty() && telefone != null) {
                        telefone = v.isValueOrEmpty(telefone.replaceAll("[^0-9]", ""), 2, "L");
                    }
                    if ((telefoneDdd + telefone).length() >= 9)
                        TelefoneFixo.appendChild(
                                document.createTextNode(telefoneDdd + telefone));
                    else
                        TelefoneFixo.appendChild(document.createTextNode(""));

                    String celularDdd = v.isValueOrEmpty(resultSet.getString("DDD_CELULAR"));
                    if (!celularDdd.isEmpty() && celularDdd != null) {
                        celularDdd = v.isValueOrEmpty(celularDdd.replaceAll("[^0-9]", ""), 2, "L");
                    }
                    String celular = v.isValueOrEmpty(resultSet.getString("CELULAR"));
                    if (!celular.isEmpty() && celular != null) {
                        celular = v.isValueOrEmpty(celular.replaceAll("[^0-9]", ""), 9, "L");
                    }
                    if ((celularDdd + celular).length() == 10)
                        TelefoneCelular.appendChild(
                                document.createTextNode(celularDdd + celular));
                    else
                        TelefoneCelular.appendChild(document.createTextNode("00000000000"));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sbW.toString(), MGSiapRPPS.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sb.toString(), MGSiapRPPS.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Beneficiario");
                        layout.appendChild(CPF);
                        layout.appendChild(Nome);
                        layout.appendChild(NomeSocial);
                        layout.appendChild(RG);
                        layout.appendChild(DataExpedicaoRG);
                        layout.appendChild(DataNascimento);
                        layout.appendChild(UFNascimento);
                        layout.appendChild(CidadeNascimento);
                        layout.appendChild(Sexo);
                        layout.appendChild(EstadoCivil);
                        layout.appendChild(NomeMae);
                        layout.appendChild(NomePai);
                        layout.appendChild(Email);
                        layout.appendChild(TelefoneFixo);
                        layout.appendChild(TelefoneCelular);

                        root.appendChild(layout);
                    }
                    // Para cada beneficiario adicione um elemento ao array de beneficiarios
                    this.beneficiarios.add(resultSet.getString("IDSERVIDOR"));
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
                                    + "and meta = 'beneficiarios'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql(
                                "insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                        + "(select coalesce(max(id)+1,1) from auxiliares),"
                                        + "(select timestamp 'NOW' from rdb$database),"
                                        + "'siap','beneficiarios','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(BeneficiarioController.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(BeneficiarioController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
