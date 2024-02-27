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
import mgsiap.MGSiap;
import validations.Validations;

/**
 *
 * @author TomMe
 */
public class ServidorController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Servidor.xml";

    public ServidorController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) servidor(es) como lote
     *
     * @param idServidorI
     * @param idServidorF
     * @return
     */
    public ResultSet getServidoresBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String sqlComplementar = "left join mensal m on m.idservidor = s.idservidor "
                + "left join centros c on c.idcentro = m.idcentro "
                + "left join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor between '" + idServidorI + "' AND '" + idServidorF + "' "
                + "and ano = '" + MGSiap.getOpcoes().getAno() + "' and mes = '"
                + MGSiap.getOpcoes().getMes() + "' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                + "and (s.idvinculo != '11' or s.idvinculo is null) "
                + "and so.cardug = '" + MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)
                + "' /*and (select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and ff.ano = m.ano and ff.mes = m.mes "
                + "and ff.parcela = m.parcela and ff.idevento in ('001','002','003') and ff.n_valor > 0 group by ff.idservidor) > 0 "
                + "*/order by s.servidor";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("servidores s", "", sqlComplementar, "", false);
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

            codigo.appendChild(
                    document.createTextNode(MGSiap.getOpcoes().getCodigoOrgao().substring(0, 6)));
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
                    Element NumeroCTPS = document.createElement("NumeroCTPS");
                    Element NumeroPISPasep = document.createElement("NumeroPISPasep");
                    Element NumeroTituloEleitor = document.createElement("NumeroTituloEleitor");
                    Element TituloEleitorZona = document.createElement("TituloEleitorZona");
                    Element TituloEleitorSecao = document.createElement("TituloEleitorSecao");
                    Element TituloEleitorUF = document.createElement("TituloEleitorUF");
                    Element DataNascimento = document.createElement("DataNascimento");
                    Element UFNascimento = document.createElement("UFNascimento");
                    Element CidadeNascimento = document.createElement("CidadeNascimento");
                    Element Escolaridade = document.createElement("Escolaridade");
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("RG inválido: '" + resultSet.getString("RG") + "', ");
                    }
                    // DATA EXPEDICAO RG
                    if (v.isValueOrError(resultSet.getString("D_RG"))) {
                        DataExpedicaoRG.appendChild(document.createTextNode(v
                                .isValueOrEmpty(resultSet.getString("D_RG")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("DataExpedicaoRG inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("D_RG"))
                                + "', ");
                    }
                    // CTPS
                    if (v.isValueOrError(resultSet.getString("CARTEIRA_PROFISSIONAL"))) {
                        NumeroCTPS.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet
                                        .getString("CARTEIRA_PROFISSIONAL"))
                                        .trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        NumeroCTPS.appendChild(document.createTextNode(""));
                        sbW.append("NumeroCTPS inválido: '"
                                + v.isValueOrEmpty(resultSet
                                        .getString("CARTEIRA_PROFISSIONAL"))
                                + "', ");
                    }
                    // NumeroPISPasep
                    if (v.isValueOrError(resultSet.getString("PISPASEP"))
                            && v.isNumberOrError(resultSet.getString("PISPASEP")
                                    .replaceAll("[^0-9]", ""))
                            && v.isPISOrError(resultSet.getString("PISPASEP")
                                    .replaceAll("[^0-9]", ""))) {
                        NumeroPISPasep.appendChild(document
                                .createTextNode(resultSet.getString("PISPASEP").trim()
                                        .replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        sbW.append("Numero PISPasep inválido: '"
                                + resultSet.getString("PISPASEP") + "', ");
                        NumeroPISPasep.appendChild(document.createTextNode(""));
                    }
                    // NumeroTituloEleitor
                    // if (v.isValueOrError(resultSet.getString("TITULO"))
                    // && v.isNumberOrError(resultSet.getString("TITULO")
                    // .replaceAll("[^0-9]", ""))
                    // // && v.isTituloOrError(resultSet.getString("TITULO")
                    // // .replaceAll("[^0-9]", ""))
                    // ) {
                    if (resultSet.getString("TITULO") != null
                            && !resultSet.getString("TITULO").isEmpty())
                        NumeroTituloEleitor.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("TITULO")
                                        .trim().replaceAll("[^0-9]", ""), 12, "L")));
                    else
                        NumeroTituloEleitor.appendChild(document.createTextNode(""));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sbW.append("Numero TituloEleitor inválido: '"
                    // + v.isValueOrEmpty(resultSet.getString("TITULO")).trim().replaceAll("[^0-9]",
                    // "")
                    // + "', ");
                    // NumeroTituloEleitor.appendChild(document.createTextNode(""));
                    // }
                    // TituloEleitorZona
                    // if (v.isValueOrError(resultSet.getString("TITULOZONA"))) {
                    if (resultSet.getString("TITULOZONA") != null
                            && !resultSet.getString("TITULOZONA").isEmpty())
                        TituloEleitorZona.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("TITULOZONA"))
                                        .trim().replaceAll("[^0-9]", "")));
                    else
                        TituloEleitorZona.appendChild(document.createTextNode(""));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sbW.append("Titulo Eleitor Zona inválido: '"
                    // + v.isValueOrEmpty(resultSet
                    // .getString("TITULOZONA"))
                    // + "', ");
                    // TituloEleitorZona.appendChild(document.createTextNode(""));
                    // }
                    // TituloEleitorSecao
                    // if (v.isValueOrError(resultSet.getString("TITULOSECAO"))) {
                    if (resultSet.getString("TITULOSECAO") != null
                            && !resultSet.getString("TITULOSECAO").isEmpty())
                        TituloEleitorSecao.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("TITULOSECAO")
                                        .trim().replaceAll("[^0-9]", ""))));
                    else
                        TituloEleitorSecao.appendChild(document.createTextNode(""));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sbW.append("Titulo Eleitor Secao inválido: '"
                    // + v.isValueOrEmpty(resultSet.getString("TITULOSECAO")) + "', ");
                    // TituloEleitorSecao.appendChild(document.createTextNode(""));
                    // }
                    // TituloEleitorUF
                    // if (v.isValueOrError(resultSet.getString("TITULOELEITORUF")) &&
                    // resultSet.getString("TITULOELEITORUF").trim().length() == 2) {
                    if (resultSet.getString("TITULOELEITORUF") != null
                            && !resultSet.getString("TITULOELEITORUF").isEmpty())
                        TituloEleitorUF.appendChild(document.createTextNode(
                                v.isValueOrEmpty(resultSet.getString("TITULOELEITORUF"), 2, "L")
                                        .trim()));
                    else
                        TituloEleitorUF.appendChild(document.createTextNode(""));
                    // } else {
                    // MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                    // sb.append("Titulo Eleitor UF inválido: '"
                    // + v.isValueOrEmpty(
                    // resultSet.getString(
                    // "TITULOELEITORUF"))
                    // + "', ");
                    // TituloEleitorUF.appendChild(document.createTextNode(""));
                    // }
                    // DataNascimento
                    if (v.isValueOrError(resultSet.getString("D_NASCIMENTO"))) {
                        DataNascimento.appendChild(
                                document.createTextNode(v
                                        .isValueOrEmpty(resultSet.getString(
                                                "D_NASCIMENTO"))
                                        .trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Cidade Nascimento inválido: '"
                                + v.isValueOrEmpty(resultSet.getString("NATURALIDADE"))
                                + "', ");
                    }

                    String escolaridade = "";
                    switch (v.isValueOrEmpty(
                            resultSet.getString("ESCOLARIDADERAIS"),
                            2, "L").trim()) {
                        case "01":
                            escolaridade = "1";
                            break;
                        case "02":
                            escolaridade = "2";
                            break;
                        case "03":
                            escolaridade = "3";
                            break;
                        case "04":
                            escolaridade = "3";
                            break;
                        case "05":
                            escolaridade = "3";
                            break;
                        case "06":
                            escolaridade = "3";
                            break;
                        case "07":
                            escolaridade = "4";
                            break;
                        case "08":
                            escolaridade = "4";
                            break;
                        case "09":
                            escolaridade = "5";
                            break;
                        case "10":
                            escolaridade = "7";
                            break;
                        case "11":
                            escolaridade = "8";
                            break;
                    }
                    if (v.isValueOrError(escolaridade)) {
                        Escolaridade.appendChild(document.createTextNode(escolaridade));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Escolaridade inválido: '"
                                + v.isValueOrEmpty(
                                        resultSet.getString(
                                                "ESCOLARIDADERAIS"))
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
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
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
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
                    if ((celularDdd + celular).length() >= 10)
                        TelefoneCelular.appendChild(
                                document.createTextNode(celularDdd + celular));
                    else
                        TelefoneCelular.appendChild(document.createTextNode(""));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Servidor");
                        layout.appendChild(CPF);
                        layout.appendChild(Nome);
                        layout.appendChild(NomeSocial);
                        layout.appendChild(RG);
                        layout.appendChild(DataExpedicaoRG);
                        layout.appendChild(NumeroCTPS);
                        layout.appendChild(NumeroPISPasep);
                        layout.appendChild(NumeroTituloEleitor);
                        layout.appendChild(TituloEleitorZona);
                        layout.appendChild(TituloEleitorSecao);
                        layout.appendChild(TituloEleitorUF);
                        layout.appendChild(DataNascimento);
                        layout.appendChild(UFNascimento);
                        layout.appendChild(CidadeNascimento);
                        layout.appendChild(Escolaridade);
                        layout.appendChild(Sexo);
                        layout.appendChild(EstadoCivil);
                        layout.appendChild(NomeMae);
                        layout.appendChild(NomePai);
                        layout.appendChild(Email);
                        layout.appendChild(TelefoneFixo);
                        layout.appendChild(TelefoneCelular);

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
                                    + "and meta = 'servidores'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql(
                                "insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                        + "(select coalesce(max(id)+1,1) from auxiliares),"
                                        + "(select timestamp 'NOW' from rdb$database),"
                                        + "'siap','servidores','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(ServidorController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
