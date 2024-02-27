/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 *
 * @author TomMe
 */
public class BDCommands {

    private final Connection conn;

    public BDCommands(Connection conn) {
        this.conn = conn;
    }

    public ResultSet getTabelaGenerico(String tabela, String salto, String sqlAdd, String sqlRaw, boolean output) {
        ResultSet rs;
        try {
            if (sqlRaw.isEmpty()) {
                sqlRaw = "SELECT " + salto + " * from " + tabela + " " + sqlAdd;
            }
            if (output) {
                System.out.println("SQL: " + sqlRaw);
            }
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlRaw);
            return rs;
        } catch (SQLException ex) {
            System.out.println("Houve um erro ao ler a tabela " + tabela + ". Erro: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void listCardug(Connection conn, JComboBox<String> jComboBox) {
        String tabela = "siaporgao";
        jComboBox.removeAllItems();
        try {
            try (ResultSet tabelaRecebe = getTabelaGenerico(tabela, "", "", "select cardug, nome from " + tabela
                    + " group by cardug, nome order by nome desc", false)) {
                while (tabelaRecebe.next()) {
                    jComboBox.addItem(tabelaRecebe.getString("cardug") + "-" + tabelaRecebe.getString("nome"));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro em listCardug: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listAnos(Connection conn, JComboBox<String> jComboBox) {
        String tabela = "parametros";
        jComboBox.removeAllItems();
        try {
            try (ResultSet tabelaRecebe = getTabelaGenerico(tabela, "", "", "select ano from " + tabela
                    + " group by ano order by ano desc", false)) {
                while (tabelaRecebe.next()) {
                    jComboBox.addItem(tabelaRecebe.getString("ano"));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro em listAnos: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listMeses(Connection conn, JComboBox<String> jComboBox, String ano) {
        String tabela = "parametros";
        jComboBox.removeAllItems();
        try {
            try (ResultSet tabelaRecebe = getTabelaGenerico(tabela, "", "", "select mes from " + tabela
                    + " where ano = '" + ano + "' group by mes order by mes desc", false)) {
                while (tabelaRecebe.next()) {
                    jComboBox.addItem(tabelaRecebe.getString("mes"));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro em listMeses: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listComplementares(Connection conn, JComboBox<String> jComboBox, String ano, String mes) {
        String tabela = "parametros";
        jComboBox.removeAllItems();
        try {
            try (ResultSet tabelaRecebe = getTabelaGenerico(tabela, "", "", "select parcela from " + tabela
                    + " where ano = '" + ano + "' and mes = '" + mes + "' group by parcela order by parcela", false)) {
                while (tabelaRecebe.next()) {
                    jComboBox.addItem(tabelaRecebe.getString("parcela"));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro em listComplementares: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listItems(Connection conn, JComboBox<String> jComboBox, String tabela, String fieldId, int fieldIdSize,
            String fieldValue, String fieldOrder) {
        try {
            jComboBox.removeAllItems();
            try (ResultSet tabelaRecebe = getTabelaGenerico(tabela, "", "", "select LPAD("
                    + tabela + "." + fieldId + "," + fieldIdSize + ",'0') as " + fieldId + ","
                    + tabela + "." + fieldValue + " from " + tabela
                    + " group by " + fieldId + "," + fieldValue + " order by " + fieldOrder, false)) {
                while (tabelaRecebe.next()) {
                    jComboBox.addItem(tabelaRecebe.getString(fieldId) + "-" + tabelaRecebe.getString(fieldValue));
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro em listItems: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void listItems(Connection conn, JList<String> jList, String tabela, String fieldId, int fieldIdSize,
            String fieldValue, String fieldOrder) {
        try {
            jList.removeAll();

            try (ResultSet tabelaRecebe = getTabelaGenerico(tabela, "", "", "select LPAD("
                    + tabela + "." + fieldId + "," + fieldIdSize + ",'0') as " + fieldId + ","
                    + tabela + "." + fieldValue + " from " + tabela
                    + " group by " + fieldId + "," + fieldValue + " order by " + fieldOrder, false)) {
                List<Object> dados = new ArrayList<>();

                while (tabelaRecebe.next()) {
                    dados.add(tabelaRecebe.getString(fieldId) + "-" + tabelaRecebe.getString(fieldValue));
                }
                jList.setListData((String[]) dados.toArray());
            }
        } catch (SQLException ex) {
            System.out.println("Erro em listItems: " + ex.getMessage());
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void executeSql(String sql) {
        try {
            PreparedStatement ps;
            ps = this.conn.prepareStatement(sql);
            ps.execute();
            ps.close();
        } catch (SQLException ex) {
            Logger.getLogger(BDCommands.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Captura os dados do orgão
     *
     * @return
     */
    public ResultSet getOrgao() {
        ResultSet tabelaRecebe = getTabelaGenerico("orgao", "first 1", "", "", false);
        return tabelaRecebe;
    }

}
