/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mgsiap;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.UIManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;

import config.BDCommands;
import config.ConnMGFP01;
import config.Functions;
import config.GradientPanel;
import config.Opcoes;
import controllers.AcompanhamentoMetaAtuarialController;
import controllers.AposentadoriaConcedidaController;
import controllers.BeneficiarioController;
import controllers.CarteiraInvestimentoController;
import controllers.CertificacaoRPPSController;
import controllers.CertificadoRegularidadePrevidenciariaController;
import controllers.CompensacaoPrevidenciariaController;
import controllers.DependenteRPPSController;
import controllers.GestorFinanceiroController;
import controllers.GruposColegiadosController;
import controllers.ItemFolhaRPPSController;
import controllers.MembroColegioController;
import controllers.ParcelamentoController;
import controllers.ParcelasParcelamentoController;
import controllers.PensaoConcedidaController;
import controllers.PensionistaController;
import controllers.PlanoCusteioController;
import controllers.PoliticaInvestimentoController;
import controllers.RPPSController;
import controllers.ResultadoAtuarialController;
import controllers.VinculoRPPSController;

/**
 *
 * @author TomMe
 */
public final class MGSiapRPPS extends javax.swing.JFrame {

    private static BDCommands bDCommands;
    private static Connection conn;
    private static Opcoes opcoes;
    private static String shell;
    private static Long timeI;
    private static Long timeF;
    private static Long timeLeft;
    public static Functions functions;
    public static ResultSet orgao;
    public static final int INFO_TYPE = 0;
    public static final int ERROR_TYPE = 1;
    public static final int WARNING_TYPE = 2;
    public static final int UPGR_TYPE = 3;
    private static Integer errorsCount = 0;
    private static Integer warningsCount = 0;

    private static String nome;

    public static final String BD_ROOT = System.getProperty("user.dir");// "C:/Windows/MGFolha/";//"C:\\Windows\\MGFolha";//
    public static final String SIAP_ROOT = BD_ROOT + "/SIAP_RPPS/";// "C:/Windows/MGFolha/SIAP_RPPS/";//"C:\\Fontes\\Mega\\MGFolha\\SIAP_RPPS\\";////"C:\\Windows\\MGFolha\\SIAP_RPPS\\";//

    public static File myLogObj;
    public static File myWarningObj;

    /*
     * As variáveis a seguir controlam a geração dos arquivos XML
     * true = gera
     * false = não gera
     */
    // Abertura do Exercício - RPPS e/ou Movimentação Mensal - RPPS
    private static boolean geraRPPS = true;
    private static boolean geraCertificacaoRPPS = true;
    private static boolean geraCertificadoRegularidadePrevidenciaria = true;
    private static boolean geraGruposColegiados = true;
    private static boolean geraMembroColegio = true;
    private static boolean geraBeneficiario = true;
    private static boolean geraDependenteRPPS = true;
    private static boolean geraVinculoRPPS = true;
    private static boolean geraPensionista = true;
    private static boolean geraAposentadoriaConcedida = true;
    private static boolean geraPensaoConcedida = true;
    private static boolean geraItemFolhaRPPS = true;
    private static boolean geraCompensacaoPrevidenciaria = true;
    private static boolean geraParcelamento = true;
    private static boolean geraParcelasParcelamento = true;
    private static boolean geraPoliticaInvestimento = true;
    private static boolean geraCarteiraInvestimento = true;
    private static boolean geraAcompanhamentoMetaAtuarial = true;
    private static boolean geraGestorFinanceiro = true;
    // Encerramento do Exercício - RPPS
    private static boolean geraPlanoCusteio = true;
    private static boolean geraResultadoAtuarial = true;

    private void setSiapOrgaos() {
        getbDCommands().listCardug(getConn(), cbCodigoOrgao);
        cbCodigoOrgao.setSelectedItem(0);
    }

    private void setYears() {
        getbDCommands().listAnos(getConn(), cbAno);
        cbAno.setSelectedItem(0);
    }

    /**
     * Creates new form Principal
     *
     * @param args
     */
    public MGSiapRPPS(String[] args) {
        initComponents();
        setSiapOrgaos();
        setYears();
        jBtnLogs.setVisible(false);
        jBtnFolder.setVisible(false);
        jLabel5.setVisible(false);
        cbComplementar.setVisible(false);
        initVariables(args);
        initUpgrades();
        jTbPanLogs.setSelectedIndex(UPGR_TYPE);
        jTAUpgrade.setCaretPosition(0);
        setTitle("Geração de SIAP - RPPS");
        // Criar a pasta SIAP-RPPS
        // File folder = new File(SIAP_ROOT);
        // if (!folder.exists()) {
        // folder.mkdirs();
        // MGSiap.toLogs(true, "Pasta root " + folder.getName() + " recriada em: " +
        // folder.getAbsolutePath(), 0);
        // }
    }

    /**
     * Insere um log para o usuário de acordo com as opções
     * type: INFO_TYPE, ERROR_TYPE, WARNING_TYPE ou UPGR_TYPE
     * 
     * @param msgs
     * @param type
     */
    public static void toLogs(boolean log, String msgs, int type) {
        switch (type) {
            case ERROR_TYPE:
                if (msgs.length() > 0)
                    jTAErrors.append(msgs.substring(0, msgs.length() - 1).replace("null", "Não informado(a)") + "\n");
                break;
            case WARNING_TYPE:
                if (msgs.length() > 0)
                    jTAWarnings.append(msgs.substring(0, msgs.length() - 1).replace("null", "Não informado(a)") + "\n");
                break;
            case UPGR_TYPE:
                if (msgs.length() > 0)
                    jTAUpgrade.append(msgs.substring(0, msgs.length() - 1).replace("null", "Não informado(a)") + "\n");
                break;
            default:
                jTAProgress.append(msgs + "\n");
                break;
        }
        if (log == true) {
            System.out.println("Retorno (toLogs): " + msgs);
        }
        // if (type == ERROR_TYPE) {
        // System.out.println(msgs);
        // System.exit(ERROR);
        // }
        jTbPanLogs.setSelectedIndex(type);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jLabel11 = new javax.swing.JLabel();
        jFormattedTextField5 = new javax.swing.JFormattedTextField();
        jLabel12 = new javax.swing.JLabel();
        jFormattedTextField6 = new javax.swing.JFormattedTextField();
        jPanelWithBackground1 = new GradientPanel();
        cbAno = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        cbMes = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        cbComplementar = new javax.swing.JComboBox<>();
        btnFechar = new javax.swing.JButton();
        btnVisualizar = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jltOper = new javax.swing.JLabel();
        jlVersion = new javax.swing.JLabel();
        jTbPanLogs = new javax.swing.JTabbedPane();
        jPanProgress = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTAProgress = new javax.swing.JTextArea();
        jPanErrors = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTAErrors = new javax.swing.JTextArea();
        jPanUpgrade = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTAUpgrade = new javax.swing.JTextArea();
        jTAWarnings = new javax.swing.JTextArea();
        jPanWarnings = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jBtnLogs = new javax.swing.JButton();
        jBtnFolder = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        cbCodigoOrgao = new javax.swing.JComboBox<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();

        jLabel11.setBackground(getBackground());
        jLabel11.setText("Inicial:");
        jLabel11.setEnabled(false);

        jFormattedTextField5.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
                new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0000"))));
        jFormattedTextField5.setEnabled(false);

        jLabel12.setBackground(getBackground());
        jLabel12.setText("Final:");
        jLabel12.setEnabled(false);

        jFormattedTextField6.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(
                new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0000"))));
        jFormattedTextField6.setEnabled(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Impressão de relatórios");
        setBackground(new java.awt.Color(168, 35, 35));
        setFont(new java.awt.Font("Arial", 0, 14)); // NOI18N
        setMinimumSize(new java.awt.Dimension(1170, 750));
        setResizable(false);
        setSize(new java.awt.Dimension(1170, 750));
        getContentPane().setLayout(null);

        jPanelWithBackground1.setBackground(new java.awt.Color(71, 10, 4));
        jPanelWithBackground1.setForeground(new java.awt.Color(212, 208, 208));
        jPanelWithBackground1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jPanelWithBackground1.setMinimumSize(new java.awt.Dimension(660, 650));
        jPanelWithBackground1.setPreferredSize(new java.awt.Dimension(840, 710));
        jPanelWithBackground1.setLayout(null);

        cbAno.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cbAno.setMaximumRowCount(10);
        cbAno.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cbAno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbAnoActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(cbAno);
        cbAno.setBounds(40, 100, 70, 23);

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel2.setForeground(jPanelWithBackground1.getForeground());
        jLabel2.setText("Ano");
        jPanelWithBackground1.add(jLabel2);
        jLabel2.setBounds(40, 80, 60, 17);

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel4.setForeground(jPanelWithBackground1.getForeground());
        jLabel4.setText("Mês");
        jPanelWithBackground1.add(jLabel4);
        jLabel4.setBounds(120, 80, 23, 17);

        cbMes.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cbMes.setMaximumRowCount(13);
        cbMes.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cbMes.setMinimumSize(new java.awt.Dimension(60, 23));
        cbMes.setPreferredSize(new java.awt.Dimension(50, 23));
        cbMes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMesActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(cbMes);
        cbMes.setBounds(120, 100, 60, 23);

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel5.setForeground(jPanelWithBackground1.getForeground());
        jLabel5.setText("Complementar");
        jPanelWithBackground1.add(jLabel5);
        jLabel5.setBounds(190, 80, 90, 17);

        cbComplementar.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cbComplementar.setMaximumRowCount(13);
        cbComplementar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cbComplementar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbComplementarActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(cbComplementar);
        cbComplementar.setBounds(190, 100, 90, 23);

        btnFechar.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnFechar.setText("Fechar");
        btnFechar.setToolTipText("");
        btnFechar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnFechar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFecharActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(btnFechar);
        btnFechar.setBounds(590, 90, 170, 30);

        btnVisualizar.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        btnVisualizar.setToolTipText("");
        btnVisualizar.setActionCommand("Gerar SIAP");
        btnVisualizar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        btnVisualizar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnVisualizar.setText("Gerar SIAP");
        btnVisualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVisualizarActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(btnVisualizar);
        btnVisualizar.setBounds(320, 90, 170, 30);

        jLabel9.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jLabel9.setForeground(jPanelWithBackground1.getForeground());
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Confirme os valores abaixo");
        jPanelWithBackground1.add(jLabel9);
        jLabel9.setBounds(300, 10, 230, 17);

        jltOper.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        jltOper.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jltOper.setText("T.Oper.: ");
        jltOper.setEnabled(false);
        jPanelWithBackground1.add(jltOper);
        jltOper.setBounds(20, 640, 240, 17);

        jBtnLogs.setText("Abrir arquivo de logs");
        jBtnLogs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnLogsActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(jBtnLogs);
        jBtnLogs.setBounds(620, 640, 160, 22);

        jBtnFolder.setText("Abrir pasta de arquivos");
        jBtnFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnFolderActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(jBtnFolder);
        jBtnFolder.setBounds(800, 640, 160, 22);

        jlVersion.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        jlVersion.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jlVersion.setText("V.");
        jlVersion.setEnabled(false);
        jPanelWithBackground1.add(jlVersion);
        jlVersion.setBounds(980, 640, 240, 17);

        jTAProgress.setColumns(20);
        jTAProgress.setRows(5);
        jScrollPane1.setViewportView(jTAProgress);

        javax.swing.GroupLayout jPanProgressLayout = new javax.swing.GroupLayout(jPanProgress);
        jPanProgress.setLayout(jPanProgressLayout);
        jPanProgressLayout.setHorizontalGroup(
                jPanProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanProgressLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 1118,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)));
        jPanProgressLayout.setVerticalGroup(
                jPanProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE));

        jTbPanLogs.addTab("Progresso", jPanProgress);

        jTAErrors.setColumns(20);
        jTAErrors.setLineWrap(true);
        jTAErrors.setRows(5);
        jTAErrors.setWrapStyleWord(true);
        jScrollPane2.setViewportView(jTAErrors);

        javax.swing.GroupLayout jPanErrorsLayout = new javax.swing.GroupLayout(jPanErrors);
        jPanErrors.setLayout(jPanErrorsLayout);
        jPanErrorsLayout.setHorizontalGroup(
                jPanErrorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1120, Short.MAX_VALUE));
        jPanErrorsLayout.setVerticalGroup(
                jPanErrorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE));

        jTbPanLogs.addTab("Erros que impedem a geração", jPanErrors);

        jTAWarnings.setColumns(20);
        jTAWarnings.setLineWrap(true);
        jTAWarnings.setRows(5);
        jTAWarnings.setWrapStyleWord(true);
        jScrollPane4.setViewportView(jTAWarnings);

        javax.swing.GroupLayout jPanWarningsLayout = new javax.swing.GroupLayout(jPanWarnings);
        jPanWarnings.setLayout(jPanWarningsLayout);
        jPanWarningsLayout.setHorizontalGroup(
                jPanWarningsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 1120, Short.MAX_VALUE));
        jPanWarningsLayout.setVerticalGroup(
                jPanWarningsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE));

        jTbPanLogs.addTab("Avisos", jPanWarnings);

        jTAUpgrade.setColumns(20);
        jTAUpgrade.setLineWrap(true);
        jTAUpgrade.setRows(5);
        jTAUpgrade.setWrapStyleWord(true);
        jScrollPane3.setViewportView(jTAUpgrade);

        javax.swing.GroupLayout jPanUpgradeLayout = new javax.swing.GroupLayout(jPanUpgrade);
        jPanUpgrade.setLayout(jPanUpgradeLayout);
        jPanUpgradeLayout.setHorizontalGroup(
                jPanUpgradeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1120, Short.MAX_VALUE));
        jPanUpgradeLayout.setVerticalGroup(
                jPanUpgradeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE));

        jTbPanLogs.addTab("Histórico de atualizações", jPanUpgrade);

        jPanelWithBackground1.add(jTbPanLogs);
        jTbPanLogs.setBounds(20, 140, 1120, 480);

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        jLabel6.setForeground(jPanelWithBackground1.getForeground());
        jLabel6.setText("Selecione o Órgão (Unidade autônoma)");
        jPanelWithBackground1.add(jLabel6);
        jLabel6.setBounds(40, 30, 230, 17);

        cbCodigoOrgao.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        cbCodigoOrgao.setMaximumRowCount(13);
        cbCodigoOrgao.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        cbCodigoOrgao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCodigoOrgaoActionPerformed(evt);
            }
        });
        jPanelWithBackground1.add(cbCodigoOrgao);
        cbCodigoOrgao.setBounds(40, 50, 720, 23);

        getContentPane().add(jPanelWithBackground1);
        jPanelWithBackground1.setBounds(0, 0, 1160, 710);

        jMenu1.setText("Sair (Alt+F4)");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        jMenuItem2.setText("Sair");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnFecharActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnFecharActionPerformed
        System.exit(0);
    }// GEN-LAST:event_btnFecharActionPerformed

    private void btnVisualizarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnVisualizarActionPerformed
        getJltOper().setText("");
        jBtnLogs.setVisible(false);
        jBtnFolder.setVisible(false);
        jTAErrors.setText("");
        jTAWarnings.setText("");
        if (getCanExecute()) {
            Execute();
        } else {
            toLogs(false, "Faltando Código Cardug", INFO_TYPE);
        }
    }// GEN-LAST:event_btnVisualizarActionPerformed

    private void cbAnoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbAnoActionPerformed
        MGSiapRPPS.getOpcoes().setAno(cbAno.getModel().getSelectedItem().toString());
        getbDCommands().listMeses(getConn(), cbMes, MGSiapRPPS.getOpcoes().getAno());
        Integer mes = (new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
        if (getShell().equals("--"))
            setSelectedValue(cbMes, String.format("%02d", mes));
    }// GEN-LAST:event_cbAnoActionPerformed

    private void cbMesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbMesActionPerformed
        if (getShell().equals("-p")) {
            setSelectedValue(cbMes, MGSiapRPPS.getOpcoes().getMes());
        } else {
            if (cbMes.getItemCount() > 0) {
                MGSiapRPPS.getOpcoes().setMes(cbMes.getModel().getSelectedItem().toString());
            }
        }
        getbDCommands().listComplementares(getConn(), cbComplementar, MGSiapRPPS.getOpcoes().getAno(),
                MGSiapRPPS.getOpcoes().getMes());
        if (getShell().equals("--"))
            cbComplementar.setSelectedItem(0);
    }// GEN-LAST:event_cbMesActionPerformed

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenu1ActionPerformed
        System.exit(0);
    }// GEN-LAST:event_jMenu1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jMenuItem2ActionPerformed

    }// GEN-LAST:event_jMenuItem2ActionPerformed

    private void jBtnLogsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_jBtnLogsActionPerformed
        try {
            File fileE = new File(myLogObj.toPath().toString());
            if (fileE.isFile())
                Desktop.getDesktop().open(fileE);
        } catch (IOException ex) {
            Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            File fileW = new File(myWarningObj.toPath().toString());
            if (fileW.isFile())
                Desktop.getDesktop().open(fileW);
        } catch (IOException ex) {
            Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_jBtnLogsActionPerformed

    private void jBtnFolderActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            File folder = new File(SIAP_ROOT);
            Desktop.getDesktop().open(folder);
        } catch (IOException ex) {
            Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cbComplementarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbComplementarActionPerformed
        if (cbComplementar.getItemCount() > 0)
            MGSiapRPPS.getOpcoes().setComplementar(cbComplementar.getModel().getSelectedItem().toString());
    }// GEN-LAST:event_cbComplementarActionPerformed

    private void cbCodigoOrgaoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbCodigoOrgaoActionPerformed
        MGSiapRPPS.getOpcoes().setCodigoOrgao(cbCodigoOrgao.getModel().getSelectedItem().toString());
    }// GEN-LAST:event_cbCodigoOrgaoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        /* Set the Nimbus look and feel */
        // <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
        // (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the default
         * look and feel.
         * For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
        }
        // </editor-fold>
        UIManager.put("OptionPane.yesButtonText", "Confirmar");
        /* Connect to MGFP01.fdb */
        ConnMGFP01 connMGFP01 = new ConnMGFP01();
        connMGFP01.conectar();
        setConn(connMGFP01.getConnection());
        setOpcoes(new Opcoes());
        setbDCommands(new BDCommands(getConn()));
        setShell("--");

        functions = new Functions();

        orgao = getbDCommands().getOrgao();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            new MGSiapRPPS(args).setVisible(true);
        });
    }

    public static void initVariables(String[] args) {
        getJltOper().setText("");
        // Caso seja acionado via shell
        if (args.length >= 4 && args[0].equals("-p")) {
            setShell(args[0]);
            MGSiapRPPS.getOpcoes().setAno((String) args[1]);
            MGSiapRPPS.getOpcoes().setMes((String) args[2]);
            MGSiapRPPS.getOpcoes().setComplementar((String) args[3]);

            setSelectedValue(cbAno, args[1]);
            setSelectedValue(cbMes, args[2]);
            setSelectedValue(cbComplementar, args[3]);
            btnVisualizar.setEnabled(true);
            btnVisualizar.requestFocus();
            setShell("--");
        } else {
            btnVisualizar.setEnabled(true);
        }
        // Fim de caso seja acionado via shell

        Upgrades.getVERSION();
        MGSiapRPPS.getOpcoes().setVersion(Upgrades.getVERSION());
        jlVersion.setText("V.: " + MGSiapRPPS.getOpcoes().getVersion());

        MGSiapRPPS.getOpcoes().setOrder("S.SERVIDOR");
    }

    public static void initUpgrades() {
        Upgrades u = new Upgrades();
        MGSiapRPPS.toLogs(false, u.upgrades(), UPGR_TYPE);
    }

    public static String getItemsFromList(List<String> dados, int countChars) {
        if (dados == null || dados.isEmpty()) {
            return "''"; // Retorna um valor que não vai dar match, mas é SQL válido
        }
        
        String ret = "";
        for (int i = 0; i < dados.size(); i++) {
            String item = dados.get(i);
            if (item != null && !item.trim().isEmpty()) {
                try {
                    // Remove caracteres não numéricos e preenche com zeros à esquerda
                    String numeroLimpo = item.replaceAll("[^0-9]", "");
                    if (!numeroLimpo.isEmpty()) {
                        String itemFormatado = String.format("%0" + countChars + "d", Integer.parseInt(numeroLimpo));
                        ret += "\"" + itemFormatado + "\",";
                    }
                } catch (NumberFormatException ex) {
                    // Se não conseguir converter para número, usa o item original (se tiver tamanho suficiente)
                    if (item.length() >= countChars) {
                        ret += "\"" + item.substring(0, countChars) + "\",";
                    }
                    // Se for menor que countChars, simplesmente ignora o item
                }
            }
        }
        
        if (ret.length() > 0) {
            ret = ret.substring(0, ret.length() - 1);
            return ret;
        } else {
            return "''"; // Se nenhum item válido, retorna valor seguro
        }
    }
    
    /**
     * Adiciona instituidores da pensão à lista de beneficiários para garantir cross-references corretas
     */
    private static void addInstituitesPensao(ArrayList<String> beneficiarios, BDCommands bDCommands) {
        try {
            if (beneficiarios.isEmpty()) {
                return;
            }
            
            String beneficiariosStr = getItemsFromList(beneficiarios, 8).replaceAll("\"", "\'");
            
            // Query para encontrar instituidores da pensão relacionados aos beneficiários atuais
            String sqlInstituidores = "select distinct s.idservidor "
                    + "from servidores s "
                    + "join servidor_pensionista sp on sp.cpfcontribuidor = s.cpf "
                    + "join servidores pensionista on pensionista.idservidor = sp.idservidor "
                    + "where pensionista.idservidor in (" + beneficiariosStr + ") "
                    + "and s.idservidor not in (" + beneficiariosStr + ")";
                    
            ResultSet rsInstituidores = bDCommands.getTabelaGenerico("", "", "", sqlInstituidores, false);
            
            if (rsInstituidores != null && rsInstituidores.first()) {
                rsInstituidores.beforeFirst();
                int countAdded = 0;
                while (rsInstituidores.next()) {
                    String idInstituidor = rsInstituidores.getString("idservidor");
                    if (idInstituidor != null && !beneficiarios.contains(idInstituidor)) {
                        beneficiarios.add(idInstituidor);
                        countAdded++;
                    }
                }
                toLogs(false, "Adicionados " + countAdded + " instituidores da pensão", 0);
            }
        } catch (SQLException ex) {
            toLogs(false, "Erro ao adicionar instituidores da pensão: " + ex.getMessage(), ERROR_TYPE);
        }
    }

    public static String getItemsFromList(List<String> dados, int countChars, boolean output) {
        String ret = getItemsFromList(dados, countChars);
        System.out.println("Lista refeita: " + ret);
        return ret;
    }

    public static void setSelectedValue(JComboBox<String> comboBox, String value) {
        String item;
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {
            item = comboBox.getItemAt(i).toString();
            if (item.equalsIgnoreCase(value)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    public static void setSelectedValue(JComboBox<String> comboBox, String value, int size) {
        String item;
        for (int i = 0; i < comboBox.getModel().getSize(); i++) {
            item = comboBox.getItemAt(i).toString().substring(0, size);
            if (item.equalsIgnoreCase(value)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    public static boolean getCanExecute() {
        String cardug = MGSiapRPPS.cbCodigoOrgao.getSelectedItem().toString().substring(0, 4);
        if (cardug.trim().equals("null"))
            return false;
        else
            return true;
    }

    public static void Execute() {
        timeI = System.currentTimeMillis();
        bDCommands.executeSql(
                "update FINANCEIRO set ADDATAINICIO = ADDATAATO where ADDATAINICIO is null and ADDATAATO is not null and ANO >= 2025",
                true);
        String orgaoCnpj = "Não informado";
        try {
            ResultSet orgaoTo = bDCommands.getOrgao();
            if (orgaoTo.isFirst()) {
                orgaoCnpj = orgaoTo.getString("idorgao");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Geração dos arquivos SIAP
        MGSiapRPPS.getOpcoes().setTitulo(
                "Geração de SIAP - RPPS " + functions.getMesExtenso(MGSiapRPPS.getOpcoes().getMes()) + "/"
                        + MGSiapRPPS.getOpcoes().getAno()
                        + (MGSiapRPPS.getOpcoes().getComplementar().equals("000") ? ""
                                : " - Complementar: " + MGSiapRPPS.getOpcoes().getComplementar()));
        MGSiapRPPS.getOpcoes().setDescricao("CNPJ: " + orgaoCnpj);

        Opcoes.setTimeI(System.currentTimeMillis());
        setNome(MGSiapRPPS.cbCodigoOrgao.getSelectedItem().toString().replaceAll(" ", "_"));
        // Criar a pasta Abertura
        File folder0 = new File(getFileFolder(0));
        // Criar a pasta Movimentação
        File folder1 = new File(getFileFolder(1));
        // Criar a pasta Encerramento
        File folder2 = new File(getFileFolder(2));
        try {
            FileUtils.deleteDirectory(folder0);
            FileUtils.deleteDirectory(folder1);
            FileUtils.deleteDirectory(folder2);
            MGSiapRPPS.toLogs(false, "Pasta " + folder0 + " excluída com sucesso", 0);
            MGSiapRPPS.toLogs(false, "Pasta " + folder1 + " excluída com sucesso", 0);
            MGSiapRPPS.toLogs(false, "Pasta " + folder2 + " excluída com sucesso", 0);
        } catch (IOException e) {
            e.printStackTrace();
            MGSiapRPPS.toLogs(true, "Erro ao tentar excluir uma ou mais pastas. Erro: " + e.getMessage(), ERROR_TYPE);
        }
        try {
            if (!folder0.exists()) {
                folder0.mkdirs();
                MGSiapRPPS.toLogs(false, "Pasta " + folder0.getName() + " recriada", 0);
            }
            if (!folder1.exists()) {
                folder1.mkdirs();
                MGSiapRPPS.toLogs(false, "Pasta " + folder1.getName() + " recriada", 0);
            }
            if (!folder2.exists()) {
                folder2.mkdirs();
                MGSiapRPPS.toLogs(false, "Pasta " + folder2.getName() + " recriada", 0);
            }
        } catch (Exception e) {
            MGSiapRPPS.toLogs(true, "Erro ao tentar criar uma ou mais pastas. Erro: " + e.getMessage(), ERROR_TYPE);
        }
        myLogObj = new File(
                SIAP_ROOT + "log_erros_" + nome.substring(7) + "_" + nome.substring(0, 6) + "_"
                        + MGSiapRPPS.getOpcoes().getAno() + "_" + MGSiapRPPS.getOpcoes().getMes()
                                .replace(" ", "_")
                        + ".txt");
        myWarningObj = new File(
                SIAP_ROOT + "log_erros_que_nao_impedem_a_transmissao_" + nome.substring(7) + "_" + nome.substring(0, 6)
                        + "_"
                        + MGSiapRPPS.getOpcoes().getAno() + "_" + MGSiapRPPS.getOpcoes().getMes()
                                .replace(" ", "_")
                        + ".txt");
        try {
            FileUtils.deleteQuietly(myLogObj);
            MGSiapRPPS.toLogs(false, "Arquivo de log de erros excluído com sucesso", 0);
            FileUtils.deleteQuietly(myWarningObj);
            MGSiapRPPS.toLogs(false, "Arquivo de log de avisos excluído com sucesso", 0);
        } catch (Exception e) {
            MGSiapRPPS.toLogs(true, "Erro ao tentar excluir uma ou mais arquivos de log. Erro: " + e.getMessage(),
                    ERROR_TYPE);
        }

        // System.exit(0);

        try {
            /* Geração dos arquivos */
            ArrayList<String> beneficiarios = new ArrayList<>();
            /* Leiaute Beneficiario */
            BeneficiarioController BeneficiarioController = new BeneficiarioController(bDCommands, geraBeneficiario,
                    beneficiarios);
            ResultSet rsBeneficiario = BeneficiarioController.getBeneficiarioBatch("00000000", "99999999");
            BeneficiarioController.toXmlFile(rsBeneficiario);
            
            // Adiciona instituidores da pensão à lista de beneficiários
            addInstituitesPensao(beneficiarios, bDCommands);
            
            String beneficiariosStr = getItemsFromList(beneficiarios, 8).replaceAll("\"", "\'");
            /* Leiaute RPPS */
            RPPSController RPPSController = new RPPSController(bDCommands, geraRPPS);
            ResultSet rsRPPS = RPPSController.getRPPSBatch(beneficiariosStr);
            RPPSController.toXmlFile(rsRPPS);
            /* Leiaute CertificacaoRPPS */
            CertificacaoRPPSController CertificacaoRPPSController = new CertificacaoRPPSController(bDCommands,
                    geraCertificacaoRPPS);
            ResultSet rsCertificacaoRPPS = CertificacaoRPPSController.getCertificacaoRPPSBatch(beneficiariosStr);
            CertificacaoRPPSController.toXmlFile(rsCertificacaoRPPS);
            /* Leiaute CertificadoRegularidadePrevidenciaria */
            CertificadoRegularidadePrevidenciariaController CertificadoRegularidadePrevidenciariaController = new CertificadoRegularidadePrevidenciariaController(
                    bDCommands, geraCertificadoRegularidadePrevidenciaria);
            ResultSet rsCertificadoRegularidadePrevidenciaria = CertificadoRegularidadePrevidenciariaController
                    .getCertificadoRegularidadePrevidenciariaBatch(beneficiariosStr);
            CertificadoRegularidadePrevidenciariaController.toXmlFile(rsCertificadoRegularidadePrevidenciaria);
            /* Leiaute GruposColegiados */
            GruposColegiadosController GruposColegiadosController = new GruposColegiadosController(bDCommands,
                    geraGruposColegiados);
            ResultSet raGruposColegiados = GruposColegiadosController.getGruposColegiadosBatch(beneficiariosStr);
            GruposColegiadosController.toXmlFile(raGruposColegiados);
            /* Leiaute MembroColegio */
            MembroColegioController MembroColegioController = new MembroColegioController(bDCommands,
                    geraMembroColegio);
            ResultSet raMembroColegio = MembroColegioController.getMembroColegioBatch(beneficiariosStr);
            MembroColegioController.toXmlFile(raMembroColegio);
            /* Leiaute DependenteRPPS */
            DependenteRPPSController DependenteRPPSController = new DependenteRPPSController(bDCommands,
                    geraDependenteRPPS);
            ResultSet rsDependenteRPPS = DependenteRPPSController.getDependenteRPPSBatch(beneficiariosStr);
            DependenteRPPSController.toXmlFile(rsDependenteRPPS);
            /* Leiaute VinculoRPPS */
            VinculoRPPSController VinculoRPPSController = new VinculoRPPSController(bDCommands, geraVinculoRPPS);
            ResultSet rsVinculoRPPS = VinculoRPPSController.getVinculoRPPSBatch(beneficiariosStr);
            VinculoRPPSController.toXmlFile(rsVinculoRPPS);
            /* Leiaute Pensionista */
            PensionistaController PensionistaController = new PensionistaController(bDCommands, geraPensionista);
            ResultSet rsPensionista = PensionistaController.getPensionistaBatch(beneficiariosStr);
            PensionistaController.toXmlFile(rsPensionista);
            /* Leiaute AposentadoriaConcedida */
            AposentadoriaConcedidaController AposentadoriaConcedidaController = new AposentadoriaConcedidaController(
                    bDCommands, geraAposentadoriaConcedida);
            ResultSet rsAposentadoriaConcedida = AposentadoriaConcedidaController
                    .getAposentadoriaConcedidaBatch(beneficiariosStr);
            AposentadoriaConcedidaController.toXmlFile(rsAposentadoriaConcedida);
            /* Leiaute PensaoConcedida */
            PensaoConcedidaController PensaoConcedidaController = new PensaoConcedidaController(bDCommands,
                    geraPensaoConcedida);
            ResultSet rsPensaoConcedida = PensaoConcedidaController.getPensaoConcedidaBatch(beneficiariosStr);
            PensaoConcedidaController.toXmlFile(rsPensaoConcedida);
            /* Leiaute ItemFolhaRPPS */
            ItemFolhaRPPSController ItemFolhaRPPSController = new ItemFolhaRPPSController(bDCommands,
                    geraItemFolhaRPPS);
            ResultSet rsItemFolhaRPPS = ItemFolhaRPPSController.getItemFolhaRPPSBatch(beneficiariosStr);
            ItemFolhaRPPSController.toXmlFile(rsItemFolhaRPPS);
            /* Leiaute CompensacaoPrevidenciaria */
            CompensacaoPrevidenciariaController CompensacaoPrevidenciariaController = new CompensacaoPrevidenciariaController(
                    bDCommands, geraCompensacaoPrevidenciaria);
            ResultSet rsCompensacaoPrevidenciaria = CompensacaoPrevidenciariaController
                    .getCompensacaoPrevidenciariaBatch(beneficiariosStr);
            CompensacaoPrevidenciariaController.toXmlFile(rsCompensacaoPrevidenciaria);
            /* Leiaute Parcelamento */
            ParcelamentoController ParcelamentoController = new ParcelamentoController(bDCommands, geraParcelamento);
            ResultSet rsParcelamento = ParcelamentoController.getParcelamentoBatch(beneficiariosStr);
            ParcelamentoController.toXmlFile(rsParcelamento);
            /* Leiaute ParcelasParcelamento */
            ParcelasParcelamentoController ParcelasParcelamentoController = new ParcelasParcelamentoController(
                    bDCommands, geraParcelasParcelamento);
            ResultSet rsParcelasParcelamento = ParcelasParcelamentoController
                    .getParcelasParcelamentoBatch(beneficiariosStr);
            ParcelasParcelamentoController.toXmlFile(rsParcelasParcelamento);
            /* Leiaute PoliticaInvestimento */
            PoliticaInvestimentoController PoliticaInvestimentoController = new PoliticaInvestimentoController(
                    bDCommands, geraPoliticaInvestimento);
            ResultSet rsPoliticaInvestimento = PoliticaInvestimentoController
                    .getPoliticaInvestimentoBatch(beneficiariosStr);
            PoliticaInvestimentoController.toXmlFile(rsPoliticaInvestimento);
            /* Leiaute CarteiraInvestimento */
            CarteiraInvestimentoController CarteiraInvestimentoController = new CarteiraInvestimentoController(
                    bDCommands, geraCarteiraInvestimento);
            ResultSet rsCarteiraInvestimento = CarteiraInvestimentoController
                    .getCarteiraInvestimentoBatch(beneficiariosStr);
            CarteiraInvestimentoController.toXmlFile(rsCarteiraInvestimento);
            /* Leiaute AcompanhamentoMetaAtuarial */
            AcompanhamentoMetaAtuarialController AcompanhamentoMetaAtuarialController = new AcompanhamentoMetaAtuarialController(
                    bDCommands, geraAcompanhamentoMetaAtuarial);
            ResultSet rsAcompanhamentoMetaAtuarial = AcompanhamentoMetaAtuarialController
                    .getAcompanhamentoMetaAtuarialBatch(beneficiariosStr);
            AcompanhamentoMetaAtuarialController.toXmlFile(rsAcompanhamentoMetaAtuarial);
            /* Leiaute GestorFinanceiro */
            GestorFinanceiroController GestorFinanceiroController = new GestorFinanceiroController(bDCommands,
                    geraGestorFinanceiro);
            ResultSet rsGestorFinanceiro = GestorFinanceiroController.getGestorFinanceiroBatch(beneficiariosStr);
            GestorFinanceiroController.toXmlFile(rsGestorFinanceiro);
            /* Leiaute PlanoCusteio */
            PlanoCusteioController PlanoCusteioController = new PlanoCusteioController(bDCommands, geraPlanoCusteio);
            ResultSet rsPlanoCusteio = PlanoCusteioController.getPlanoCusteioBatch(beneficiariosStr);
            PlanoCusteioController.toXmlFile(rsPlanoCusteio);
            /* Leiaute ResultadoAtuarial */
            ResultadoAtuarialController ResultadoAtuarialController = new ResultadoAtuarialController(bDCommands,
                    geraResultadoAtuarial);
            ResultSet rsResultadoAtuarial = ResultadoAtuarialController.getResultadoAtuarialBatch(beneficiariosStr);
            ResultadoAtuarialController.toXmlFile(rsResultadoAtuarial);

        } catch (Exception ex) {
            Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (jTAErrors.getText().trim().length() > 0 || jTAWarnings.getText().trim().length() > 0) {
                jBtnLogs.setVisible(true);
            }
            jBtnFolder.setVisible(true);
            boolean errorsWarning = false;
            if (getErrorsCount() > 0 && getWarningsCount() > 0) {
                MGSiapRPPS.toLogs(false,
                        "Os Arquivos XML foram gerados com " + getErrorsCount() + " erros e "
                                + getWarningsCount() + " avisos que TEMPORARIAMENTE NÃO IMPEDEM A TRANSMISSÃO",
                        0);
                errorsWarning = true;
            }
            try {
                if (getErrorsCount() > 0) {
                    /* Ordenar erros */
                    String[] erros = new String[jTAErrors.getLineCount()];
                    erros = jTAErrors.getText().split("\\n");
                    Arrays.sort(erros);
                    jTAErrors.setText("");
                    for (String erro : erros) {
                        MGSiapRPPS.toLogs(false, erro, MGSiapRPPS.ERROR_TYPE);
                    }
                    /* Fim de ordenar erros */
                    if (getErrorsCount() > 0) {
                        if (!errorsWarning) {
                            MGSiapRPPS.toLogs(false, "Os Arquivos XML foram gerados com " + getErrorsCount() + " erros",
                                    0);
                            errorsWarning = true;
                        }
                        String msg = "Um arquivo de log de ERROS foi salvo em " + SIAP_ROOT + "log_erros_"
                                + cbCodigoOrgao.getSelectedItem().toString().replace(" ", "_");
                        Integer maxMsgLength = 115;
                        if (msg.length() > maxMsgLength) {
                            msg = msg.substring(0, maxMsgLength);
                        }
                        msg = msg + "....txt. Favor verificar e corrigir";
                        MGSiapRPPS.toLogs(false, msg, 0);
                    }
                    try (BufferedWriter logWriter = new BufferedWriter(
                            new FileWriterWithEncoding(myLogObj.toPath().toString(), StandardCharsets.UTF_8,
                                    false))) {
                        jTAErrors.write(logWriter);
                        logWriter.close();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                if (getWarningsCount() > 0) {
                    /* Ordenar avisos */
                    String[] avisos = new String[jTAWarnings.getLineCount()];
                    avisos = jTAWarnings.getText().split("\\n");
                    Arrays.sort(avisos);
                    jTAWarnings.setText("");
                    for (String aviso : avisos) {
                        MGSiapRPPS.toLogs(false, aviso, MGSiapRPPS.WARNING_TYPE);
                    }
                    jTAWarnings.setCaretPosition(0);
                    /* Fim de ordenar avisos */
                    if (getWarningsCount() > 0) {
                        if (!errorsWarning) {
                            MGSiapRPPS.toLogs(false,
                                    "Os Arquivos XML foram gerados com " + getWarningsCount()
                                            + " avisos que TEMPORARIAMENTE NÃO IMPEDEM A TRANSMISSÃO",
                                    0);
                            errorsWarning = true;
                        }
                        String msg = "Um arquivo de log de AVISOS foi salvo em " + SIAP_ROOT + "log_avisos_"
                                + cbCodigoOrgao.getSelectedItem().toString().replace(" ", "_");
                        Integer maxMsgLength = 115;
                        if (msg.length() > maxMsgLength) {
                            msg = msg.substring(0, maxMsgLength);
                        }
                        msg = msg + "....txt. Favor verificar e corrigir";
                        MGSiapRPPS.toLogs(false, msg, 0);
                    }
                    try (BufferedWriter warningWriter = new BufferedWriter(
                            new FileWriterWithEncoding(myWarningObj.toPath().toString(), StandardCharsets.UTF_8,
                                    false))) {
                        jTAWarnings.write(warningWriter);
                        warningWriter.close();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MGSiapRPPS.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                jTAErrors.setCaretPosition(0);
                jTAWarnings.setCaretPosition(0);
                jTAUpgrade.setCaretPosition(0);
            }
            Opcoes.setTimeF(System.currentTimeMillis());
            Opcoes.setTimeLeft(Opcoes.getTimeF() - Opcoes.getTimeI());
            long miliseconds = Opcoes.getTimeLeft();
            int seconds = (int) (Opcoes.getTimeLeft() / 1000) % 60;
            int minutes = (int) ((Opcoes.getTimeLeft() / (1000 * 60)) % 60);
            String tempo = String.format("%02d:%02d:%04d", minutes, seconds, miliseconds);
            System.out.println("T.Oper.: " + tempo + " (mm:ss:ssss)");
            getJltOper().setText("T.Oper.: " + tempo + " (mm:ss:ssss)");
            Toolkit.getDefaultToolkit().beep();
            getBtnVisualizar().setEnabled(true);
            getCbAno().setEnabled(true);
            getCbMes().setEnabled(true);
            getCbComplementar().setEnabled(true);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFechar;
    private static javax.swing.JButton btnVisualizar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private static javax.swing.JComboBox<String> cbAno;
    public static javax.swing.JComboBox<String> cbCodigoOrgao;
    private static javax.swing.JComboBox<String> cbComplementar;
    private static javax.swing.JComboBox<String> cbMes;
    private static javax.swing.JButton jBtnLogs;
    private static javax.swing.JButton jBtnFolder;
    private javax.swing.JFormattedTextField jFormattedTextField5;
    private javax.swing.JFormattedTextField jFormattedTextField6;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel9;
    private static javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private static javax.swing.JMenuItem jMenuItem2;
    public static javax.swing.JPanel jPanErrors;
    public static javax.swing.JPanel jPanWarnings;
    public static javax.swing.JPanel jPanUpgrade;
    public static javax.swing.JPanel jPanProgress;
    private javax.swing.JPanel jPanelWithBackground1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    public static javax.swing.JTextArea jTAErrors;
    public static javax.swing.JTextArea jTAWarnings;
    public static javax.swing.JTextArea jTAUpgrade;
    public static javax.swing.JTextArea jTAProgress;
    public static javax.swing.JTabbedPane jTbPanLogs;
    private static javax.swing.JLabel jlVersion;
    private static javax.swing.JLabel jltOper;
    // End of variables declaration//GEN-END:variables

    public static Connection getConn() {
        return conn;
    }

    public static void setConn(Connection conn) {
        MGSiapRPPS.conn = conn;
    }

    public static void setErrorsCount(int type) {
        if (type == ERROR_TYPE)
            MGSiapRPPS.errorsCount++;
        else if (type == WARNING_TYPE)
            MGSiapRPPS.warningsCount++;
    }

    public static Integer getErrorsCount() {
        return MGSiapRPPS.errorsCount;
    }

    public static Integer getWarningsCount() {
        return MGSiapRPPS.warningsCount;
    }

    public static Opcoes getOpcoes() {
        return opcoes;
    }

    public static void setOpcoes(Opcoes opcoes) {
        MGSiapRPPS.opcoes = opcoes;
    }

    public static BDCommands getbDCommands() {
        return bDCommands;
    }

    public static void setbDCommands(BDCommands bDCommands) {
        MGSiapRPPS.bDCommands = bDCommands;
    }

    public static String getShell() {
        return shell;
    }

    public static void setShell(String shell) {
        MGSiapRPPS.shell = shell;
    }

    public static Long getTimeI() {
        return timeI;
    }

    public static void setTimeI(Long timeI) {
        MGSiapRPPS.timeI = timeI;
    }

    public static Long getTimeF() {
        return timeF;
    }

    public static void setTimeF(Long timeF) {
        MGSiapRPPS.timeF = timeF;
    }

    public static Long getTimeLeft() {
        return timeLeft;
    }

    public static void setTimeLeft(Long timeLeft) {
        MGSiapRPPS.timeLeft = timeLeft;
    }

    public static JLabel getJltOper() {
        return jltOper;
    }

    public static void setJltOper(JLabel jltOper) {
        MGSiapRPPS.jltOper = jltOper;
    }

    public JButton getBtnFechar() {
        return btnFechar;
    }

    public void setBtnFechar(JButton btnFechar) {
        this.btnFechar = btnFechar;
    }

    public static JButton getBtnVisualizar() {
        return btnVisualizar;
    }

    public static void setBtnVisualizar(JButton btnVisualizar) {
        MGSiapRPPS.btnVisualizar = btnVisualizar;
    }

    public ButtonGroup getButtonGroup1() {
        return buttonGroup1;
    }

    public void setButtonGroup1(ButtonGroup buttonGroup1) {
        this.buttonGroup1 = buttonGroup1;
    }

    public ButtonGroup getButtonGroup2() {
        return buttonGroup2;
    }

    public void setButtonGroup2(ButtonGroup buttonGroup2) {
        this.buttonGroup2 = buttonGroup2;
    }

    public static JComboBox<String> getCbAno() {
        return cbAno;
    }

    public static void setCbAno(JComboBox<String> cbAno) {
        MGSiapRPPS.cbAno = cbAno;
    }

    public static JComboBox<String> getCbComplementar() {
        return cbComplementar;
    }

    public static void setCbComplementar(JComboBox<String> cbComplementar) {
        MGSiapRPPS.cbComplementar = cbComplementar;
    }

    public static JComboBox<String> getCbMes() {
        return cbMes;
    }

    public static void setCbMes(JComboBox<String> cbMes) {
        MGSiapRPPS.cbMes = cbMes;
    }

    public javax.swing.JFormattedTextField getJFormattedTextField5() {
        return this.jFormattedTextField5;
    }

    public void setJFormattedTextField5(javax.swing.JFormattedTextField jFormattedTextField5) {
        this.jFormattedTextField5 = jFormattedTextField5;
    }

    public javax.swing.JFormattedTextField getJFormattedTextField6() {
        return this.jFormattedTextField6;
    }

    public void setJFormattedTextField6(javax.swing.JFormattedTextField jFormattedTextField6) {
        this.jFormattedTextField6 = jFormattedTextField6;
    }

    public javax.swing.JLabel getJLabel11() {
        return this.jLabel11;
    }

    public void setJLabel11(javax.swing.JLabel jLabel11) {
        this.jLabel11 = jLabel11;
    }

    public javax.swing.JLabel getJLabel12() {
        return this.jLabel12;
    }

    public void setJLabel12(javax.swing.JLabel jLabel12) {
        this.jLabel12 = jLabel12;
    }

    public javax.swing.JLabel getJLabel2() {
        return this.jLabel2;
    }

    public void setJLabel2(javax.swing.JLabel jLabel2) {
        this.jLabel2 = jLabel2;
    }

    public javax.swing.JLabel getJLabel4() {
        return this.jLabel4;
    }

    public void setJLabel4(javax.swing.JLabel jLabel4) {
        this.jLabel4 = jLabel4;
    }

    public javax.swing.JLabel getJLabel5() {
        return this.jLabel5;
    }

    public void setJLabel5(javax.swing.JLabel jLabel5) {
        this.jLabel5 = jLabel5;
    }

    public javax.swing.JLabel getJLabel6() {
        return this.jLabel6;
    }

    public void setJLabel6(javax.swing.JLabel jLabel6) {
        this.jLabel6 = jLabel6;
    }

    public javax.swing.JLabel getJLabel9() {
        return this.jLabel9;
    }

    public void setJLabel9(javax.swing.JLabel jLabel9) {
        this.jLabel9 = jLabel9;
    }

    public javax.swing.JMenuBar getJMenuBar1() {
        return this.jMenuBar1;
    }

    public void setJMenuBar1(javax.swing.JMenuBar jMenuBar1) {
        this.jMenuBar1 = jMenuBar1;
    }

    public javax.swing.JPanel getJPanelWithBackground1() {
        return this.jPanelWithBackground1;
    }

    public void setJPanelWithBackground1(javax.swing.JPanel jPanelWithBackground1) {
        this.jPanelWithBackground1 = jPanelWithBackground1;
    }

    public javax.swing.JScrollPane getJScrollPane1() {
        return this.jScrollPane1;
    }

    public void setJScrollPane1(javax.swing.JScrollPane jScrollPane1) {
        this.jScrollPane1 = jScrollPane1;
    }

    public javax.swing.JScrollPane getJScrollPane2() {
        return this.jScrollPane2;
    }

    public void setJScrollPane2(javax.swing.JScrollPane jScrollPane2) {
        this.jScrollPane2 = jScrollPane2;
    }

    public static String getFileFolder(int tipo) {
        String fileFolder = getNome().substring(7) + "_" + getNome().substring(0, 6) + "_"
                + MGSiapRPPS.getOpcoes().getAno() + "_" + MGSiapRPPS.getOpcoes().getMes().replace(" ", "_") + "/";
        String fileFolderMounted = SIAP_ROOT + '/' + fileFolder + '/';
        switch (tipo) {
            case 0:
                fileFolderMounted += "Abertura/";
                break;
            case 1:
                fileFolderMounted += "Movimentacao/";
                break;
            case 2:
                fileFolderMounted += "Encerramento/";
                break;
            default:
                // Lançar uma exceção informando que o tipo de arquivo não foi informado
                throw new IllegalArgumentException("O tipo de arquivo não foi informado");
        }
        return fileFolderMounted;
    }

    public static void setNome(String nome) {
        MGSiapRPPS.nome = nome;
    }

    public static String getNome() {
        return MGSiapRPPS.nome;
    }
}
