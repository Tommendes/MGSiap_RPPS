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
     * Captura os dados do(s) VinculoRPPS(s) como lote
     *
     * @param idBeneficiarioI
     * @param idBeneficiarioF
     * @return
     */
    public ResultSet getVinculoRPPSBatch(String idBeneficiarioI, String idBeneficiarioF) {
        ResultSet tabelaRecebe = bDCommands.getTabelaGenerico("orgao s", "", "", "", false);
        return tabelaRecebe;
    }

    public void toXmlFile(ResultSet resultSet) {
        MGSiap.toLogs(false, "Executando o Leiaute " + fileName, 0);
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

            if (gerarXml)
                try {
                    String xmlFilePath = MGSiap.getFileFolder(1) + fileName;
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);
                    StreamResult streamResult = new StreamResult(new File(xmlFilePath));
                    transformer.transform(domSource, streamResult);
                    
                    MGSiap.toLogs(false, "Arquivo XML " + fileName + " salvo em: " + xmlFilePath, 0);

                    ResultSet tabelaAuxiliares = bDCommands.getTabelaGenerico("", "", "",
                            "select count(*) from auxiliares where dominio = 'siap' "
                                    + "and meta = 'vinculoRPPS'",
                            false);
                    tabelaAuxiliares.first();
                    if (tabelaAuxiliares.getInt("count") == 0) {
                        this.bDCommands.executeSql(
                                "insert into auxiliares (id,created_at,dominio,meta,valor) values ("
                                        + "(select coalesce(max(id)+1,1) from auxiliares),"
                                        + "(select timestamp 'NOW' from rdb$database),"
                                        + "'siap','vinculoRPPS','exec')");
                    }
                } catch (TransformerException ex) {
                    Logger.getLogger(VinculoRPPSController.class.getName()).log(Level.SEVERE, null,
                            ex);
                }
        } catch (ParserConfigurationException | SQLException ex) {
            Logger.getLogger(VinculoRPPSController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
