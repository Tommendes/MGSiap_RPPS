package controllers;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
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
import config.Functions;
import mgsiap.MGSiapRPPS;
import validations.Validations;

/**
 *
 * @author TomMe
 */
public class ItemFolhaRPPSController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "ItemFolhaRPPS.xml";

    public ItemFolhaRPPSController(BDCommands bDCommands, boolean gerarXml) {
        this.bDCommands = bDCommands;
        this.gerarXml = gerarXml;
    }

    /**
     * Captura os dados do(s) ItemFolhaRPPS(s) como lote
     *
     * @param beneficiarios Lista de beneficiários
     * @return ResultSet com os dados dos itens de folha
     */
    public ResultSet getItemFolhaRPPSBatch(String beneficiarios) {
        String sql = "select S.CPF, S.IDSERVIDOR as MATRICULA, F.MES as MESCOMPETENCIA, F.ANO as ANO, " +
                     "O.IDORGAO as CNPJFONTEPAGADORA, E.SIAPNATUREZA as NATUREZA, E.SIAPTIPO as TIPO, " +
                     "E.EVENTO as DESCRICAO, E.SIAPRPPS as INCIDECONTRIBUICAORPPS, E.SIAPIRRF as INCIDEIRRF, " +
                     "E.SIAPTETO as TETOREMUNERATORIO, E.SIAPRGPS as INCIDECONTRIBUICAORGPS, " +
                     "E.SIAPFGTS as INCIDEFGTS, sum(F.N_VALOR) as VALOR " +
                     "from SERVIDORES S " +
                     "join FINANCEIRO F on F.IDSERVIDOR = S.IDSERVIDOR " +
                     "join EVENTOS E on E.IDEVENTO = F.IDEVENTO " +
                     "join ORGAO O on O.IDORGAO is not null " +
                     "where F.ANO = '" + MGSiapRPPS.getOpcoes().getAno() + "' and " +
                     "F.MES = '" + MGSiapRPPS.getOpcoes().getMes() + "' and " +
                     "F.N_VALOR > 0 " +
                     (beneficiarios != null && !beneficiarios.isEmpty() ? 
                         "and S.IDSERVIDOR in (" + beneficiarios + ") " : "") +
                     "group by S.CPF, S.IDSERVIDOR, F.ANO, F.MES, O.IDORGAO, E.SIAPNATUREZA, " +
                     "E.SIAPTIPO, E.EVENTO, E.SIAPRPPS, E.SIAPIRRF, E.SIAPTETO, E.SIAPRGPS, E.SIAPFGTS " +
                     "order by S.IDSERVIDOR";
        
        MGSiapRPPS.toLogs(false, "Executando query ItemFolhaRPPS: " + sql, 0);
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("", "", "", sql, true);
        return tabelaRecebe;
    }

    public void toXmlFile(ResultSet resultSet) {
        MGSiapRPPS.toLogs(false, "Executando o Leiaute " + fileName, 0);
        StringBuilder sb = new StringBuilder();
        Functions f = new Functions();
        Validations v = new Validations();
        
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

            int itemCount = 0;
            
            // Processar registros do ResultSet
            if (resultSet != null) {
                while (resultSet.next()) {
                    itemCount++;
                    
                    Element itemFolhaRPPS = document.createElement("ItemFolhaRPPS");
                    
                    // CPF (obrigatório, 11 dígitos numéricos)
                    Element cpf = document.createElement("CPF");
                    String cpfValue = v.isNumberOrEmpty(resultSet.getString("cpf"), 11, "L");
                    if (cpfValue.length() != 11) {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("CPF deve ter 11 dígitos: '" + cpfValue + "', ");
                    } else if (!v.isCPFOrError(cpfValue)) {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("CPF inválido: '" + cpfValue + "', ");
                    }
                    cpf.appendChild(document.createTextNode(cpfValue));
                    
                    // Matrícula (obrigatório, máximo 16 caracteres)
                    Element matricula = document.createElement("Matricula");
                    String matriculaValue = v.isValueOrEmpty(resultSet.getString("matricula"), 16, "L");
                    matricula.appendChild(document.createTextNode(matriculaValue));
                    
                    // Mês Competência (obrigatório, 2 dígitos)
                    Element mesCompetencia = document.createElement("MesCompetencia");
                    String mesCompValue = v.isNumberOrEmpty(resultSet.getString("mescompetencia"), 2, "L");
                    if (mesCompValue.length() == 1) {
                        mesCompValue = "0" + mesCompValue; // Preenche com zero à esquerda
                    }
                    // Validar se está entre 01 e 13
                    try {
                        int mesNum = Integer.parseInt(mesCompValue.isEmpty() ? "0" : mesCompValue);
                        if (mesNum < 1 || mesNum > 13) {
                            MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                            sb.append("Mês competência deve estar entre 01 e 13: '" + mesCompValue + "', ");
                        }
                    } catch (NumberFormatException e) {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                        sb.append("Mês competência inválido: '" + mesCompValue + "', ");
                    }
                    mesCompetencia.appendChild(document.createTextNode(mesCompValue));
                    
                    // Ano (obrigatório, 4 dígitos)
                    Element ano = document.createElement("Ano");
                    String anoValue = v.isNumberOrEmpty(resultSet.getString("ano"), 4, "L");
                    ano.appendChild(document.createTextNode(anoValue));
                    
                    // CNPJ Fonte Pagadora (obrigatório, máximo 14 caracteres)
                    Element cnpjFontePagadora = document.createElement("CNPJFontePagadora");
                    String cnpjValue = v.isNumberOrEmpty(resultSet.getString("cnpjfontepagadora"), 14, "L");
                    if (cnpjValue.isEmpty()) {
                        cnpjValue = v.isNumberOrEmpty(MGSiapRPPS.getOpcoes().getCodigoOrgao(), 14, "L");
                        if (cnpjValue.isEmpty()) {
                            cnpjValue = "00000000000000"; // CNPJ padrão se não informado
                            MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                            sb.append("CNPJ não informado, usando padrão, ");
                        }
                    }
                    if (cnpjValue.length() == 14 && !v.isCNPJOrError(cnpjValue)) {
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("CNPJ inválido: '" + cnpjValue + "', ");
                    }
                    cnpjFontePagadora.appendChild(document.createTextNode(cnpjValue));
                    
                    // Natureza (obrigatório, código de 4 dígitos)
                    Element natureza = document.createElement("Natureza");
                    String naturezaValue = v.isNumberOrEmpty(resultSet.getString("natureza"), 4, "L");
                    if (naturezaValue.isEmpty()) {
                        naturezaValue = "1000"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("Natureza não informada, usando padrão '1000', ");
                    }
                    natureza.appendChild(document.createTextNode(naturezaValue));
                    
                    // Tipo (obrigatório, limitado ao código)
                    Element tipo = document.createElement("Tipo");
                    String tipoValue = v.isNumberOrEmpty(resultSet.getString("tipo"), 1, "L");
                    if (tipoValue.isEmpty()) {
                        tipoValue = "1"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("Tipo não informado, usando padrão '1', ");
                    }
                    tipo.appendChild(document.createTextNode(tipoValue));
                    
                    // Descrição (obrigatório)
                    Element descricao = document.createElement("Descricao");
                    String descricaoValue = v.isValueOrEmpty(resultSet.getString("descricao"));
                    descricaoValue = f.removeAcentos(descricaoValue); // Remove acentos
                    descricao.appendChild(document.createTextNode(descricaoValue));
                    
                    // Incide Contribuição RPPS (obrigatório, limitado ao código)
                    Element incideContribuicaoRPPS = document.createElement("IncideContribuicaoRPPS");
                    String incideRPPSValue = v.isNumberOrEmpty(resultSet.getString("incidecontribuicaorpps"), 1, "L");
                    if (incideRPPSValue.isEmpty()) {
                        incideRPPSValue = "1"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("IncideContribuicaoRPPS não informado, usando padrão '1', ");
                    }
                    incideContribuicaoRPPS.appendChild(document.createTextNode(incideRPPSValue));
                    
                    // Incide IRRF (obrigatório, limitado ao código de 4 dígitos)
                    Element incideIRRF = document.createElement("IncideIRRF");
                    String incideIRRFValue = v.isNumberOrEmpty(resultSet.getString("incideirrf"), 4, "L");
                    if (incideIRRFValue.isEmpty()) {
                        incideIRRFValue = "0000"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("IncideIRRF não informado, usando padrão '0000', ");
                    }
                    incideIRRF.appendChild(document.createTextNode(incideIRRFValue));
                    
                    // Teto Remuneratório (obrigatório, limitado ao código)
                    Element tetoRemuneratorio = document.createElement("TetoRemuneratorio");
                    String tetoValue = v.isNumberOrEmpty(resultSet.getString("tetoremuneratorio"), 1, "L");
                    if (tetoValue.isEmpty()) {
                        tetoValue = "1"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("TetoRemuneratorio não informado, usando padrão '1', ");
                    }
                    tetoRemuneratorio.appendChild(document.createTextNode(tetoValue));
                    
                    // Incide Contribuição RGPS (obrigatório, limitado ao código de 2 dígitos)
                    Element incideContribuicaoRGPS = document.createElement("IncideContribuicaoRGPS");
                    String incideRGPSValue = v.isNumberOrEmpty(resultSet.getString("incidecontribuicaorgps"), 2, "L");
                    if (incideRGPSValue.isEmpty()) {
                        incideRGPSValue = "00"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("IncideContribuicaoRGPS não informado, usando padrão '00', ");
                    }
                    incideContribuicaoRGPS.appendChild(document.createTextNode(incideRGPSValue));
                    
                    // Incide FGTS (obrigatório, limitado ao código de 2 dígitos)
                    Element incideFGTS = document.createElement("IncideFGTS");
                    String incideFGTSValue = v.isNumberOrEmpty(resultSet.getString("incidefgts"), 2, "L");
                    if (incideFGTSValue.isEmpty()) {
                        incideFGTSValue = "00"; // Valor padrão
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("IncideFGTS não informado, usando padrão '00', ");
                    }
                    incideFGTS.appendChild(document.createTextNode(incideFGTSValue));
                    
                    // Valor (obrigatório, decimal formato americano)
                    Element valor = document.createElement("Valor");
                    String valorValue = v.isDecimalOrEmpty(resultSet.getString("valor"));
                    if (valorValue.isEmpty()) {
                        valorValue = "0.00";
                        MGSiapRPPS.setErrorsCount(MGSiapRPPS.WARNING_TYPE);
                        sb.append("Valor não informado, usando 0.00, ");
                    } else {
                        // Formatar como decimal com 2 casas decimais (formato americano)
                        try {
                            // Primeiro, normalizar entrada (trocar vírgula por ponto se necessário)
                            valorValue = valorValue.replace(",", ".");
                            double valorNum = Double.parseDouble(valorValue);
                            
                            // Usar DecimalFormat com locale americano para garantir formato com ponto
                            DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
                            valorValue = df.format(valorNum);
                        } catch (NumberFormatException e) {
                            valorValue = "0.00";
                            MGSiapRPPS.setErrorsCount(MGSiapRPPS.ERROR_TYPE);
                            sb.append("Valor inválido, usando 0.00: '" + resultSet.getString("valor") + "', ");
                        }
                    }
                    valor.appendChild(document.createTextNode(valorValue));
                    
                    // Adicionar todos os elementos ao ItemFolhaRPPS
                    itemFolhaRPPS.appendChild(cpf);
                    itemFolhaRPPS.appendChild(matricula);
                    itemFolhaRPPS.appendChild(mesCompetencia);
                    itemFolhaRPPS.appendChild(ano);
                    itemFolhaRPPS.appendChild(cnpjFontePagadora);
                    itemFolhaRPPS.appendChild(natureza);
                    itemFolhaRPPS.appendChild(tipo);
                    itemFolhaRPPS.appendChild(descricao);
                    itemFolhaRPPS.appendChild(incideContribuicaoRPPS);
                    itemFolhaRPPS.appendChild(incideIRRF);
                    itemFolhaRPPS.appendChild(tetoRemuneratorio);
                    itemFolhaRPPS.appendChild(incideContribuicaoRGPS);
                    itemFolhaRPPS.appendChild(incideFGTS);
                    itemFolhaRPPS.appendChild(valor);
                    
                    root.appendChild(itemFolhaRPPS);
                }
            }
            
            MGSiapRPPS.toLogs(false, "Total de itens de folha processados: " + itemCount, 0);
            
            // Log de erros se houver
            if (sb.length() > 0) {
                MGSiapRPPS.toLogs(true, "Erros/Avisos no ItemFolhaRPPS: " + sb.toString(), 0);
            }

            if (gerarXml) {
                try {
                    String xmlFilePath = MGSiapRPPS.getFileFolder(1) + fileName;
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);
                    StreamResult streamResult = new StreamResult(new File(xmlFilePath));                 
                    transformer.transform(domSource, streamResult);
                    
                    MGSiapRPPS.toLogs(false, "Arquivo XML " + fileName + " salvo em: " + xmlFilePath, 0);

                    ResultSet tabelaAuxiliares = bDCommands.getTabelaGenerico("", "", "",
                            "select count(*) from auxiliares where dominio = 'siap' "
                                    + "and meta = 'itemFolhaRPPS'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql(
                                "insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                        + "(select coalesce(max(id)+1,1) from auxiliares),"
                                        + "(select timestamp 'NOW' from rdb$database),"
                                        + "'siap','itemFolhaRPPS','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(ItemFolhaRPPSController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(ItemFolhaRPPSController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
