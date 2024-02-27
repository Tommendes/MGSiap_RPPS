/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import config.BDCommands;
import validations.Validations;
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
import mgsiap.MGSiap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author TomMe
 */
public class VinculoController {

    private final BDCommands bDCommands;
    private final boolean gerarXml;
    private final String fileName = "Vinculo.xml";

    public VinculoController(BDCommands bDCommands, boolean gerarXml) {
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
    public ResultSet getVinculoBatch(String idServidorI, String idServidorF) {
        idServidorI = String.format("%1$8s", idServidorI).replace(" ", "0");
        idServidorF = String.format("%1$8s", idServidorF).replace(" ", "0");
        String select = "s.cpf, s.servidor, s.idservidor, so.Codigo, s.idvinculo, m.idpccs, "
                + "cg.idcargo, s.d_admissao, m.fgdatainicio, "
                + "cg.cbo, s.n_carga_horaria, m.ano, m.mes, m.parcela, m.fg";
        String especialSelect = "(select sum(ff.n_valor) from financeiro ff where ff.idservidor = s.idservidor and " +
                "ff.ano = m.ano and ff.mes = m.mes and ff.parcela = m.parcela and ff.idevento in ('001','002','003') " +
                "and ff.n_valor > 0 group by ff.idservidor) salario_base, "
                + "coalesce(m.fg,'N')fg, coalesce((select sum(n_valor) from financeiro f "
                + "where f.idservidor = s.idservidor and f.ano = m.ano and f.mes = m.mes and f.parcela = m.parcela and f.cgc = 'S' and f.n_valor > 0 "
                + "group by f.idservidor),0) salario_fgcgc, "
                + "(select e.idevento from eventos e "
                + "join financeiro f on f.idevento = e.idevento "
                + "and f.idservidor = s.idservidor and f.ano = m.ano "
                + "and f.mes = m.mes and f.parcela = '000' "
                + "where e.tipoevento = '5 - Gratificação') ";
        String sqlRaw = "select " + select + ", " + especialSelect + " from servidores s "
                + "join mensal m on m.idservidor = s.idservidor "
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
                // + "and s.idvinculo != '11' " // Alterado no leiaute 1ª edição - Exercício
                // 2024
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

            Validations v = new Validations();

            boolean error = false;
            if (resultSet.first()) {
                resultSet.beforeFirst();
                while (resultSet.next()) {
                    String startLog = "Servidor " + resultSet.getString("SERVIDOR") + " ("
                            + resultSet.getString("IDSERVIDOR") + ") (Vínculo)";
                    StringBuilder sb = new StringBuilder();
                    StringBuilder sbW = new StringBuilder();
                    sb.append(startLog);
                    sbW.append(startLog);
                    Element CPF = document.createElement("CPF");
                    Element Matricula = document.createElement("Matricula");
                    Element CodigoOrgao = document.createElement("CodigoOrgao");
                    Element CodigoCarreira = document.createElement("CodigoCarreira");
                    Element CodigoCargo = document.createElement("CodigoCargo");
                    Element DataExercicio = document.createElement("DataExercicio");
                    Element DataPosse = document.createElement("DataPosse");
                    Element DataNomeacao = document.createElement("DataNomeacao");
                    Element DataExoneracao = document.createElement("DataExoneracao");
                    Element TipoVinculo = document.createElement("TipoVinculo");
                    Element CodigoFG = document.createElement("CodigoFG");
                    Element DataInicioFG = document.createElement("DataInicioFG");
                    Element Ocupacao = document.createElement("Ocupacao");
                    Element Jornada = document.createElement("Jornada");
                    Element PercentualComissionado = document.createElement("PercentualComissionado");
                    Element SalarioCargoEfetivo = document.createElement("SalarioCargoEfetivo");

                    if (v.isValueOrError(resultSet.getString("CPF"))
                            && v.isNumberOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))
                            && v.isCPFOrError(resultSet.getString("CPF").trim().replaceAll("[^0-9]", ""))) {
                        CPF.appendChild(
                                document.createTextNode(resultSet.getString("CPF").trim().replaceAll("[^0-9]", "")));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("CPF inválido: '" + v.isNumberOrEmpty(resultSet.getString("CPF"), 11, "R").trim()
                                + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("idservidor"))) {
                        Matricula.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("idservidor"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Matricula inválida: '" + v.isValueOrEmpty(resultSet.getString("idservidor"))
                                + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("Codigo"))) {
                        CodigoOrgao.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("Codigo"), 10, "L").trim()));
                    } else {
                        // CodigoOrgao.appendChild(document.createTextNode(""));
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo Orgao inválido: '" + resultSet.getString("Codigo") + "',");
                    }
                    if (v.isValueOrError(resultSet.getString("idpccs"))) {
                        CodigoCarreira.appendChild(document
                                .createTextNode(v.isValueOrEmpty(resultSet.getString("idpccs"), 10, "L").trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        CodigoCarreira.appendChild(document.createTextNode(""));
                        sbW.append("CodigoCarreira inválido: '" + resultSet.getString("idpccs") + "', ");
                    }
                    String cgCodigo = "";
                    if (v.isValueOrError(resultSet.getString("idvinculo"))
                            && v.isValueOrError(resultSet.getString("idcargo"))
                            && v.isValueOrError(resultSet.getString("idpccs"))) {
                        cgCodigo = resultSet.getString("idvinculo").trim()
                                + String.format("%1$4s", resultSet.getString("idcargo").trim()).replace(" ", "0")
                                + String.format("%1$4s", resultSet.getString("idpccs").trim()).replace(" ", "0");
                    }
                    if (v.isValueOrError(resultSet.getString("idvinculo"))
                            && v.isValueOrError(resultSet.getString("idcargo"))) {
                        CodigoCargo.appendChild(document.createTextNode(cgCodigo));
                    } else {
                        // CodigoCargo.appendChild(document.createTextNode(""));
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Codigo do cargo inválido: '" + cgCodigo + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("d_admissao"))) {
                        DataExercicio.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("d_admissao")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Data Exercicio (Admissão) inválida: '"
                                + v.isValueOrEmpty(resultSet.getString("d_admissao"))
                                + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("d_admissao"))) {
                        DataPosse.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("d_admissao")).trim()));
                    } else {
                        // DataPosse.appendChild(document.createTextNode(""));
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append(
                                "Data Posse (Admissão) inválida: '"
                                        + v.isValueOrEmpty(resultSet.getString("d_admissao")) + "', ");
                    }
                    if (v.isValueOrError(resultSet.getString("d_admissao"))) {
                        DataNomeacao.appendChild(
                                document.createTextNode(v.isValueOrEmpty(resultSet.getString("d_admissao")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                        DataNomeacao.appendChild(document.createTextNode(""));
                        sbW.append("DataNomeacao inválida: '" +
                                v.isValueOrEmpty(resultSet.getString("d_admissao"))
                                + "', ");
                    }
                    DataExoneracao.appendChild(document.createTextNode(""));
                    if (v.isValueOrError(resultSet.getString("idvinculo"))) {
                        if (null != resultSet.getString("idvinculo")) {
                            switch (resultSet.getString("idvinculo")) {
                                // Códigos do Vínculo no MGFolha ---- Códigos do SIAP --- Códigos do SIAP Não
                                // utilizados pelo MGFolha
                                // 1 EFETIVO ------------------------ 1. Efetivo -------- 5. Celetista
                                // 2 COMISSIONADO ------------------- 4. Comissionado --- 9. Aprendiz
                                // 3 CONTRATADO --------------------- 3. Contratado Temporário
                                // 4 APOSENTADO --------------------- 10. Aposentado
                                // 5 PENSIONISTA -------------------- 11. Pensionista
                                // 6 ELETIVO ------------------------ 6. Eletivo
                                // 7 ESTAGIARIO --------------------- 8. Estagiário
                                // 8 CONTRATADO POR PROCESSO SELETIVO 3. Contratado Temporário
                                // 9 ESTABILIZADO ------------------- 2. Estabilizado (pré-CF/88)
                                // 10 REQUISITADO ------------------- 7. Cedido
                                // 11 PENSÃO ALIMENTÍCIA >>>>> Não informar no SIAP <<<<<

                                case "2": // Comissionado
                                    TipoVinculo.appendChild(document.createTextNode("4")); // Comissionado
                                    break;
                                case "3": // Contratado
                                case "8": // Contratado
                                    TipoVinculo.appendChild(document.createTextNode("3")); // Contratado
                                    break;
                                case "4": // Aposentado
                                    TipoVinculo.appendChild(document.createTextNode("10")); // Aposentado
                                    break;
                                case "5": // Pensionista
                                    TipoVinculo.appendChild(document.createTextNode("11")); // Pensionista
                                    break;
                                case "6": // Eletivo
                                    TipoVinculo.appendChild(document.createTextNode("6")); // Eletivo
                                    break;
                                case "7": // Estagiário
                                    TipoVinculo.appendChild(document.createTextNode("8")); // Estagiário
                                    break;
                                case "9": // Estabilizado
                                    TipoVinculo.appendChild(document.createTextNode("2")); // Estabilizado
                                    break;
                                case "10": // Requisitado
                                    TipoVinculo.appendChild(document.createTextNode("7")); // Cedido
                                    break;
                                default:
                                    TipoVinculo.appendChild(document.createTextNode("1")); // Efetivo
                                    break;
                            }
                        }
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("TipoVinculo inválido: '" + resultSet.getString("idvinculo") + "', ");
                    }
                    
                    String fg = resultSet.getString("fg") == null ? "N" : resultSet.getString("fg");

                    if (fg.equalsIgnoreCase("S")) {
                        if (v.isValueOrError(resultSet.getString("idevento"))
                                && v.isValueOrError(resultSet.getString("idservidor"))) {
                            CodigoFG.appendChild(
                                    document.createTextNode(v.isValueOrEmpty(resultSet.getString("idevento"))
                                            + v.isValueOrEmpty(resultSet.getString("idservidor"))));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            CodigoFG.appendChild(document.createTextNode(""));
                            sbW.append("CodigoFG inválido: '" + v.isValueOrEmpty(resultSet.getString("idevento"))
                                    + v.isValueOrEmpty(resultSet.getString("idservidor")) + "', ");
                        }

                        if (v.isValueOrError(resultSet.getString("fgdatainicio"))) {
                            DataInicioFG.appendChild(document
                                    .createTextNode(v.isValueOrEmpty(resultSet.getString("fgdatainicio")).trim()));
                        } else {
                            MGSiap.setErrorsCount(MGSiap.WARNING_TYPE);
                            DataInicioFG.appendChild(document.createTextNode(""));
                            sbW.append("DataInicioFG inválida: '"
                                    + v.isValueOrEmpty(resultSet.getString("fgdatainicio")) + "', ");
                        }
                    } else {
                        CodigoFG.appendChild(document.createTextNode(""));
                        DataInicioFG.appendChild(document.createTextNode(""));
                    }
                    if (v.isValueOrError(resultSet.getString("cbo"))
                            && resultSet.getString("cbo").replaceAll("[^0-9]", "").length() > 0) {
                        Ocupacao.appendChild(document
                                .createTextNode(
                                        v.isValueOrEmpty(resultSet.getString("cbo").replaceAll("[^0-9]", "")).trim()));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Ocupacao (CBO do cargo) inválida: '" + v.isValueOrEmpty(resultSet.getString("cbo"))
                                + "', ");
                    }
                    if (v.isDecimalOrError(resultSet.getString("n_carga_horaria"))
                            && resultSet.getInt("n_carga_horaria") > 0 && resultSet.getInt("n_carga_horaria") < 99) {
                        Jornada.appendChild(
                                document.createTextNode(String.format("%d", resultSet.getInt("n_carga_horaria"))));
                    } else {
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Jornada inválida: '" + resultSet.getString("n_carga_horaria") + "', ");
                    }
                    PercentualComissionado.appendChild(document.createTextNode(""));

                    /* PercentualComissionado */
                    // PercentualComissionado é o valor resultante da divisão do valor da
                    // gratificação
                    // (resultSet.getString("n_valor")) pelo salário
                    // (resultSet.getString("salario")) do servidor
                    double percentual = 0.0;
                    if (v.isValueOrError(fg)
                            && v.isValueOrError(resultSet.getString("idvinculo"))
                            && v.isDecimalOrError(resultSet.getString("salario_base"))
                            && v.isDecimalOrError(resultSet.getString("salario_fgcgc"))) {
                        if (resultSet.getString("idvinculo").equals("1")
                                && fg.equalsIgnoreCase("S")) {
                            double salario_fgcgc = resultSet.getDouble("salario_fgcgc");
                            double salario_base = resultSet.getDouble("salario_base");
                            percentual = Math.round((salario_fgcgc / salario_base) * 100.0);
                            PercentualComissionado.appendChild(document.createTextNode(String.valueOf(percentual)));
                        } else
                            PercentualComissionado.appendChild(document.createTextNode("0.00"));
                    } else {
                        MGSiap.toLogs(
                                "Vinculo: Função Gratificada (Rubrica)"
                                        + v.isValueOrEmpty(resultSet.getString("idevento"))
                                        + v.isValueOrEmpty(resultSet.getString("idservidor"))
                                        + ": Percentual do Comissionado inválido: '" + String.valueOf(percentual) + "'",
                                1);
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Percentual inválido ou superior a 100%: '" + String.valueOf(percentual) + "', ");
                    }

                    double salario = Double.parseDouble(resultSet.getString("salario_base"));
                    if (v.isDecimalOrError(String.valueOf(salario)) && salario >= 0.0) {
                        SalarioCargoEfetivo
                                .appendChild(document.createTextNode(String.format("%.2f", salario).replace(",", ".")));
                    } else {
                        MGSiap.toLogs(
                                "Vinculo: Função Gratificada (Rubrica)"
                                        + v.isValueOrEmpty(resultSet.getString("idevento"))
                                        + v.isValueOrEmpty(resultSet.getString("idservidor"))
                                        + ": Salario do Cargo Efetivo inválido: '" + String.valueOf(percentual) + "'",
                                1);
                        MGSiap.setErrorsCount(MGSiap.ERROR_TYPE);
                        sb.append("Salario do Cargo Efetivo inválido: '" + salario + "', ");
                    }

                    if (!sbW.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sbW.toString(), MGSiap.WARNING_TYPE);
                    }
                    if (!sb.toString().equalsIgnoreCase(startLog)) {
                        MGSiap.toLogs(sb.toString(), MGSiap.ERROR_TYPE);
                        if (error == false)
                            error = true;
                    } else {
                        Element layout = document.createElement("Vinculo");
                        layout.appendChild(CPF);
                        layout.appendChild(Matricula);
                        layout.appendChild(CodigoOrgao);
                        layout.appendChild(CodigoCarreira);
                        layout.appendChild(CodigoCargo);
                        layout.appendChild(DataExercicio);
                        layout.appendChild(DataPosse);
                        layout.appendChild(DataNomeacao);
                        layout.appendChild(DataExoneracao);
                        layout.appendChild(TipoVinculo);
                        layout.appendChild(CodigoFG);
                        layout.appendChild(DataInicioFG);
                        layout.appendChild(Ocupacao);
                        layout.appendChild(Jornada);
                        layout.appendChild(PercentualComissionado);
                        layout.appendChild(SalarioCargoEfetivo);

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
                                    + "and meta = 'vinculo'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql("insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                + "(select coalesce(max(id)+1,1) from auxiliares),"
                                + "(select timestamp 'NOW' from rdb$database),"
                                + "'siap','vinculo','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(VinculoController.class.getName()).log(Level.SEVERE, null, ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(VinculoController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
