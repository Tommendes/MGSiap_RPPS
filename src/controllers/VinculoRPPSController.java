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
public class VinculoRPPSController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "VinculoRPPS.xml";

    public VinculoRPPSController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Método auxiliar para obter strings do ResultSet de forma segura
     */
    private String getStringOrEmpty(ResultSet rs, String columnName) {
        try {
            String value = rs.getString(columnName);
            return value != null ? value : "";
        } catch (SQLException ex) {
            return "";
        }
    }

    /**
     * Captura os dados do(s) VinculoRPPS(s) como lote
     *
     * @param beneficiarios
     * @return
     */
    public ResultSet getVinculoRPPSBatch(String beneficiarios) {
        // Se não há beneficiários, retorna null para evitar erro SQL
        if (beneficiarios == null || beneficiarios.trim().isEmpty()) {
            MGSiapRPPS.toLogs(false, "Nenhum beneficiário encontrado para gerar VinculoRPPS", 0);
            return null;
        }

        String sqlComplementar = "left join mensal m on m.idservidor = s.idservidor "
                + "left join centros c on c.idcentro = m.idcentro "
                + "left join siaporgao so on so.c_ua = c.codigo_ua and so.cnpj = replace(replace(replace(c.cnpj_ua, '/', ''), '-', ''), '.', '') "
                + "where s.idservidor in (" + beneficiarios + ") "
                + "and ano = '" + MGSiapRPPS.getOpcoes().getAno() + "' and mes = '"
                + MGSiapRPPS.getOpcoes().getMes() + "' and m.parcela = '000' "
                + "and ((m.situacao = 'ADMITIDO') or exists (select md.idservidor from mdefinitivo md where md.idservidor = s.idservidor and md.onus = '3 - Falecimento' "
                + "and ((select count(*) from servidor_aposentadoria sa where sa.idservidor = s.idservidor) > 0 or "
                + "(select count(*) from servidor_pensionista sp where sp.cpfcontribuidor = s.cpf) > 0))) "
                + "and (trim(S.IDVINCULO) in ('', '1', '4', '5') or (S.IDVINCULO is null)) "
                + "and so.cardug = '" + MGSiapRPPS.getOpcoes().getCodigoOrgao().substring(0, 6) + "' "
                + "order by s.servidor";
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("servidores s", "", sqlComplementar, "", false);
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
            boolean hasData = false;

            // Verifica se há dados, mas sempre gera o arquivo XML
            if (resultSet != null) {
                try {
                    hasData = resultSet.first();
                    if (hasData) {
                        resultSet.beforeFirst();
                    }
                } catch (SQLException ex) {
                    MGSiapRPPS.toLogs(false, "Erro ao verificar dados: " + ex.getMessage(), 0);
                    hasData = false;
                }
            }

            if (hasData) {
                while (resultSet.next()) {
                    String servidor = getStringOrEmpty(resultSet, "SERVIDOR");
                    String idServidor = getStringOrEmpty(resultSet, "IDSERVIDOR");

                    String startLog = "Beneficiario " + servidor + " (" + idServidor + "): ";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element Matricula = document.createElement("Matricula");
                    Element DataInicio = document.createElement("DataInicio");
                    Element TipoVinculo = document.createElement("TipoVinculo");
                    Element TipoFundo = document.createElement("TipoFundo");

                    // CPF
                    String cpfValue = getStringOrEmpty(resultSet, "CPF");
                    if (v.isValueOrError(cpfValue)
                            && v.isNumberOrError(cpfValue.trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(cpfValue.trim().replaceAll("[^0-9]", ""))) {
                        CPF.appendChild(
                                document.createTextNode(cpfValue.trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("CPF inválido: '"
                                + v.isNumberOrEmpty(cpfValue, 11, "R").trim());
                    }
                    // Matricula
                    String idServidorValue = getStringOrEmpty(resultSet, "idservidor");
                    if (v.isValueOrError(idServidorValue)) {
                        Matricula.appendChild(document
                                .createTextNode(v.isValueOrEmpty(idServidorValue, 8, "R").trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Matricula inválida: '" + idServidorValue + "', ");
                    }
                    // DataInicio
                    String dataAdmissaoValue = getStringOrEmpty(resultSet, "d_admissao");
                    if (v.isValueOrError(dataAdmissaoValue)) {
                        DataInicio.appendChild(
                                document.createTextNode(v.isValueOrEmpty(dataAdmissaoValue, 10, "R").trim()));
                    } else {
                        setarDataInicioIgualAdmissao(resultSet);
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("DataInicio inválido: '" + dataAdmissaoValue +
                                ". Gere novamente este mesmo SIAP!', ");
                    }
                    // TipoVinculo
                    String idVinculoValue = getStringOrEmpty(resultSet, "IDVINCULO");
                    
                    // Se idVinculoValue for null, "", "1", "2", "4" está correto e se for null ou "" então retorne "1"
                    if (idVinculoValue == null || idVinculoValue.trim().isEmpty()) {
                        TipoVinculo.appendChild(document.createTextNode("1"));
                    } else if (v.isValueOrError(idVinculoValue)) {
                        if (idVinculoValue.trim().equals("4"))
                            TipoVinculo.appendChild(document.createTextNode("1"));
                        else
                            TipoVinculo.appendChild(document.createTextNode("2"));
                    } else if (idVinculoValue.trim().equals("1") || idVinculoValue.trim().equals("2")) {
                        TipoVinculo.appendChild(document.createTextNode(idVinculoValue.trim()));
                    } else {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("TipoVinculo inválida: '" + v.isValueOrEmpty(idVinculoValue) + "', ");
                    }

                    // if (v.isValueOrError(idVinculoValue)) {
                    //     if (idVinculoValue.trim().equals("4"))
                    //         TipoVinculo.appendChild(document.createTextNode("1"));
                    //     else
                    //         TipoVinculo.appendChild(document.createTextNode("2"));
                    // } else {
                    //     MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                    //     sb.append("TipoVinculo inválida: '" + v.isValueOrEmpty(idVinculoValue) + "', ");
                    // }
                    
                    // TipoFundo
                    TipoFundo.appendChild(document.createTextNode("1"));

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sbW.toString(), MGSiapRPPS.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiapRPPS.toLogs(false, sb.toString(), MGSiapRPPS.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("VinculoRPPS");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(DataInicio);
                        layout.appendChild(TipoVinculo);
                        layout.appendChild(TipoFundo);

                        root.appendChild(layout);
                    }
                }
                if (error) {
                    Element layout = document.createElement("Informacao");
                    layout.appendChild(
                            document.createTextNode("Arquivo gerado com erros! Ver o log"));
                    root.appendChild(layout);
                }
            } else {
                // Sem dados de vínculo, mas arquivo deve ser gerado com estrutura básica
                MGSiapRPPS.toLogs(false, "Nenhum vínculo encontrado, gerando arquivo vazio", 0);
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

    /**
     * Verifica se a data de admissão existe em servidores e se não existir
     * captura de mdefinitivo o valor de d_afastamento para o idservidor
     *
     * @param resultSet ResultSet contendo os dados dos servidores.
     */
    public void setarDataInicioIgualAdmissao(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                String idServidor = resultSet.getString("idservidor");
                String dataAdmissao = resultSet.getString("d_admissao");

                // Verifica se a data de admissão não existe ou está vazia
                if (dataAdmissao == null || dataAdmissao.isEmpty() || dataAdmissao.equals("null")) {
                    // Busca a data de afastamento na tabela mdefinitivo
                    ResultSet rsAfastamento = bDCommands.getTabelaGenerico("mdefinitivo",
                            "",
                            "where idservidor = '" + idServidor + "'",
                            "", false);

                    if (rsAfastamento != null && rsAfastamento.next()) {
                        String dataAfastamento = rsAfastamento.getString("d_afastamento");
                        if (dataAfastamento != null && !dataAfastamento.isEmpty() && !dataAfastamento.equals("null")) {
                            // Atualiza a data de admissão com a data de afastamento
                            bDCommands.executeSql(
                                    "update servidores set d_admissao = '" + dataAfastamento + "' where idservidor = '"
                                            + idServidor + "'",
                                    true);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(VinculoRPPSController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
