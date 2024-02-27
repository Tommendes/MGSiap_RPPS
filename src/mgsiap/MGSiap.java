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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import controllers.AdicionalController;
import controllers.AdmissaoController;
import controllers.AlteracaoJornadaDeTrabalhoController;
import controllers.CargoController;
import controllers.CarreiraController;
import controllers.CessaoController;
import controllers.ClasseController;
import controllers.DependenteController;
import controllers.DesignacaoCargoComissaoFuncaoGratificadaController;
import controllers.DesligamentoController;
import controllers.DisponibilidadeController;
import controllers.FuncaoGratificadaCargoComissionadoController;
import controllers.ItemFolhaController;
import controllers.LicencaController;
import controllers.NivelController;
import controllers.OrgaoController;
import controllers.ProgressaoCargoController;
import controllers.ProgressaoFuncionalController;
import controllers.ReadaptacaoController;
import controllers.ReconducaoController;
import controllers.ReenquadramentoController;
import controllers.ReintegracaoController;
import controllers.ServidorController;
import controllers.SetorController;
import controllers.VinculoController;

/**
 *
 * @author TomMe
 */
public final class MGSiap extends javax.swing.JFrame {

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

    public static String VERSION = "0.52";

    public static final String BD_ROOT = System.getProperty("user.dir");// "C:/Windows/MGFolha/";//"C:\\Windows\\MGFolha";//
    public static final String SIAP_ROOT = BD_ROOT + "/SIAP/";// "C:/Windows/MGFolha/SIAP/";//"C:\\Fontes\\Mega\\MGFolha\\SIAP\\";////"C:\\Windows\\MGFolha\\SIAP\\";//

    public static String fileFolder;
    public static File myLogObj;
    public static File myWarningObj;

    /*
     * As variáveis a seguir controlam a geração dos arquivos XML
     * true = gera
     * false = não gera
     */
    private static boolean geraServidor = true;
    private static boolean geraDependente = true;
    private static boolean geraOrgao = true;
    private static boolean geraSetor = true;
    private static boolean geraCarreira = true;
    private static boolean geraCargo = true;
    private static boolean geraNivel = true;
    private static boolean geraClasse = true;
    private static boolean geraProgressaoCargo = true;
    private static boolean geraFuncaoGratificadaCargoComissionado = true;
    private static boolean geraVinculo = true;
    private static boolean geraAdicional = true;
    private static boolean geraAdmissão = true;
    private static boolean geraAposentadoria = true;
    private static boolean geraAlteracaoJornadaDeTrabalho = true;
    private static boolean geraCessao = true;
    private static boolean geraDisponibilidade = true;
    private static boolean geraDesligamento = true;
    private static boolean geraDesignacaoCargoComissaoFuncaoGratificada = true;
    private static boolean geraLicenca = true;
    private static boolean geraPensao = true;
    private static boolean geraPensionista = true;
    private static boolean geraProgressaoFuncional = true;
    private static boolean geraReadaptacao = true;
    private static boolean geraReconducao = true;
    private static boolean geraReintegracao = true;
    private static boolean geraReenquadramento = true;
    private static boolean geraItemFolha = true;

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
    public MGSiap(String[] args) {
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
        setTitle("Geração de SIAP");
        // create output directory is not exists
        File folder = new File(SIAP_ROOT);
        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    /**
     * Insere um log para o usuário de acordo com as opções
     * type: INFO_TYPE, ERROR_TYPE, WARNING_TYPE ou UPGR_TYPE
     * 
     * @param msgs
     * @param type
     */
    public static void toLogs(String msgs, int type) {
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
            toLogs("Faltando Código Cardug", INFO_TYPE);
        }
    }// GEN-LAST:event_btnVisualizarActionPerformed

    private void cbAnoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbAnoActionPerformed
        MGSiap.getOpcoes().setAno(cbAno.getModel().getSelectedItem().toString());
        getbDCommands().listMeses(getConn(), cbMes, MGSiap.getOpcoes().getAno());
        Integer mes = (new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().getMonthValue());
        if (getShell().equals("--"))
            setSelectedValue(cbMes, String.format("%02d", mes));
    }// GEN-LAST:event_cbAnoActionPerformed

    private void cbMesActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbMesActionPerformed
        if (getShell().equals("-p")) {
            setSelectedValue(cbMes, MGSiap.getOpcoes().getMes());
        } else {
            if (cbMes.getItemCount() > 0) {
                MGSiap.getOpcoes().setMes(cbMes.getModel().getSelectedItem().toString());
            }
        }
        getbDCommands().listComplementares(getConn(), cbComplementar, MGSiap.getOpcoes().getAno(),
                MGSiap.getOpcoes().getMes());
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
            Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            File fileW = new File(myWarningObj.toPath().toString());
            if (fileW.isFile())
                Desktop.getDesktop().open(fileW);
        } catch (IOException ex) {
            Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_jBtnLogsActionPerformed

    private void jBtnFolderActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            File folder = new File(SIAP_ROOT);
            Desktop.getDesktop().open(folder);
        } catch (IOException ex) {
            Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cbComplementarActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbComplementarActionPerformed
        if (cbComplementar.getItemCount() > 0)
            MGSiap.getOpcoes().setComplementar(cbComplementar.getModel().getSelectedItem().toString());
    }// GEN-LAST:event_cbComplementarActionPerformed

    private void cbCodigoOrgaoActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cbCodigoOrgaoActionPerformed
        MGSiap.getOpcoes().setCodigoOrgao(cbCodigoOrgao.getModel().getSelectedItem().toString());
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
            Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
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
            new MGSiap(args).setVisible(true);
        });
    }

    public static void initVariables(String[] args) {
        getJltOper().setText("");
        // Caso seja acionado via shell
        if (args.length >= 4 && args[0].equals("-p")) {
            setShell(args[0]);
            MGSiap.getOpcoes().setAno((String) args[1]);
            MGSiap.getOpcoes().setMes((String) args[2]);
            MGSiap.getOpcoes().setComplementar((String) args[3]);

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

        File file = new File(System.getProperty("user.dir") + "/MGSiap.jar");
        if (file.isFile()) {
            Path path = Paths.get(System.getProperty("user.dir") + "/MGSiap.jar");
            BasicFileAttributes attr;
            try {
                attr = Files.readAttributes(path, BasicFileAttributes.class);
                LocalDateTime localDateTime = attr.creationTime()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                MGSiap.getOpcoes().setVersion(VERSION + "." + localDateTime.format(
                        DateTimeFormatter.ofPattern("ddMMyy.HHmm")));
            } catch (IOException ex) {
                Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        jlVersion.setText("V.: " + MGSiap.getOpcoes().getVersion());

        MGSiap.getOpcoes().setOrder("S.SERVIDOR");
    }

    public static void initUpgrades() {
        Upgrades u = new Upgrades();
        MGSiap.toLogs(u.upgrades(), UPGR_TYPE);
    }

    public static String getItemsFromList(List<String> dados, int countChars) {
        String ret = "";
        for (int i = 0; i < dados.size(); i++) {
            ret += "\"" + dados.get(i).substring(0, countChars) + "\",";
        }
        ret = ret.substring(0, ret.length() - 1);
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
        String cardug = MGSiap.cbCodigoOrgao.getSelectedItem().toString().substring(0, 4);
        if (cardug.trim().equals("null"))
            return false;
        else
            return true;
    }

    public static void Execute() {
        timeI = System.currentTimeMillis();
        String orgaoCnpj = "Não informado";
        try {
            ResultSet orgaoTo = bDCommands.getOrgao();
            if (orgaoTo.isFirst()) {
                orgaoCnpj = orgaoTo.getString("idorgao");
            }
        } catch (SQLException ex) {
            Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Geração dos arquivos SIAP
        MGSiap.getOpcoes().setTitulo(
                "Geração de SIAP " + functions.getMesExtenso(MGSiap.getOpcoes().getMes()) + "/"
                        + MGSiap.getOpcoes().getAno()
                        + (MGSiap.getOpcoes().getComplementar().equals("000") ? ""
                                : " - Complementar: " + MGSiap.getOpcoes().getComplementar()));
        MGSiap.getOpcoes().setDescricao("CNPJ: " + orgaoCnpj);

        Opcoes.setTimeI(System.currentTimeMillis());
        String nome = MGSiap.cbCodigoOrgao.getSelectedItem().toString().replaceAll(" ", "_");
        fileFolder = MGSiap.SIAP_ROOT + "/" + nome.substring(7) + "_" + nome.substring(0, 6) + "_"
                + MGSiap.getOpcoes().getAno() + "_" + MGSiap.getOpcoes().getMes()
                        .replace(" ", "_")
                + "/";
        File folder = new File(fileFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }
        try {
            FileUtils.deleteDirectory(folder);
            MGSiap.toLogs("Pasta " + fileFolder + " excluída com sucesso", 0);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Erro ao tentar excluir a pasta em " + fileFolder);
        }
        try {
            if (!folder.exists()) {
                folder.mkdir();
                MGSiap.toLogs("Pasta " + fileFolder + " recriada", 0);
            }
        } catch (Exception e) {
            MGSiap.toLogs(e.getMessage(), ERROR_TYPE);
        }
        myLogObj = new File(
                SIAP_ROOT + "log_erros_" + nome.substring(7) + "_" + nome.substring(0, 6) + "_"
                        + MGSiap.getOpcoes().getAno() + "_" + MGSiap.getOpcoes().getMes()
                                .replace(" ", "_")
                        + ".txt");
        myWarningObj = new File(
                SIAP_ROOT + "log_erros_que_nao_impedem_a_transmissao_" + nome.substring(7) + "_" + nome.substring(0, 6)
                        + "_"
                        + MGSiap.getOpcoes().getAno() + "_" + MGSiap.getOpcoes().getMes()
                                .replace(" ", "_")
                        + ".txt");
        try {
            FileUtils.deleteQuietly(myLogObj);
            MGSiap.toLogs("Arquivo de log de erros excluído com sucesso", 0);
            FileUtils.deleteQuietly(myWarningObj);
            MGSiap.toLogs("Arquivo de log de avisos excluído com sucesso", 0);
        } catch (Exception e) {
            MGSiap.toLogs(e.getMessage(), ERROR_TYPE);
        }

        try {
            /* Leiaute Servidor 9.1.1 */
            ServidorController ServidorController = new ServidorController(bDCommands, geraServidor);
            ResultSet rsCad = ServidorController.getServidoresBatch("00000000", "99999999");
            ServidorController.toXmlFile(rsCad);
            /* Leiaute Dependente 9.1.2 */
            DependenteController dependenteController = new DependenteController(bDCommands, geraDependente);
            ResultSet rsDep = dependenteController.getDependentesBatch("00000000", "99999999");
            dependenteController.toXmlFile(rsDep);
            /* Leiaute Orgao 9.1.3 */
            OrgaoController orgaoController = new OrgaoController(bDCommands, geraOrgao);
            ResultSet rsOrgao = orgaoController.getOrgaosBatch("00000000", "99999999");
            orgaoController.toXmlFile(rsOrgao);
            /* Leiaute Setor 9.1.4 */
            SetorController setorController = new SetorController(bDCommands, geraSetor);
            ResultSet rsSetor = setorController.getSetorBatch("00000000", "99999999");
            setorController.toXmlFile(rsSetor);
            /* Leiaute Carreira 9.1.5 */
            CarreiraController carreiraController = new CarreiraController(bDCommands, geraCarreira);
            ResultSet rsCarreira = carreiraController.getCarreiraBatch("00000000", "99999999");
            carreiraController.toXmlFile(rsCarreira);
            /* Leiaute Cargo 9.1.6 */
            CargoController cargoController = new CargoController(bDCommands, geraCargo);
            ResultSet rsCargo = cargoController.getCargoBatch("00000000", "99999999");
            cargoController.toXmlFile(rsCargo);
            /* Leiaute Nivel 9.1.7 */
            NivelController nivelController = new NivelController(bDCommands, geraNivel);
            ResultSet rsNivel = nivelController.getNivelBatch("00000000", "99999999");
            nivelController.toXmlFile(rsNivel);
            /* Leiaute Classe 9.1.8 */
            ClasseController classeController = new ClasseController(bDCommands, geraClasse);
            ResultSet rsClasse = classeController.getClasseBatch("00000000", "99999999");
            classeController.toXmlFile(rsClasse);
            /* Leiaute ProgressaoCargo 9.1.9 */
            ProgressaoCargoController progressaoCargoController = new ProgressaoCargoController(bDCommands,
                    geraProgressaoCargo);
            ResultSet rsProgressaoCargoController = progressaoCargoController.getServidoresBatch("00000000",
                    "99999999");
            progressaoCargoController.toXmlFile(rsProgressaoCargoController);
            /* Leiaute FuncaoGratificadaCargoComissionado 9.1.10 */
            FuncaoGratificadaCargoComissionadoController funcaoGratificadaCargoComissionadoController = new FuncaoGratificadaCargoComissionadoController(
                    bDCommands, geraFuncaoGratificadaCargoComissionado);
            ResultSet rsFuncaoGratificadaCargoComissionadoController = funcaoGratificadaCargoComissionadoController
                    .getFuncaoGratificadaCargoComissionadoBatch("00000000", "99999999");
            funcaoGratificadaCargoComissionadoController.toXmlFile(rsFuncaoGratificadaCargoComissionadoController);
            /* Leiaute Vinculo 9.1.11 */
            VinculoController vinculoController = new VinculoController(bDCommands, geraVinculo);
            ResultSet rsVinculoController = vinculoController.getVinculoBatch("00000000", "99999999");
            vinculoController.toXmlFile(rsVinculoController);
            /* Leiaute Adicional 9.2.1 */
            AdicionalController adicionalController = new AdicionalController(bDCommands, geraAdicional);
            ResultSet rsAdicionalController = adicionalController.getAdicionalBatch("00000000", "99999999");
            adicionalController.toXmlFile(rsAdicionalController);
            /* Leiaute Admissao 9.2.2 */
            AdmissaoController adimissaoController = new AdmissaoController(bDCommands, geraAdmissão);
            ResultSet rsAdmissaoController = adimissaoController.getAdmissaoBatch("00000000", "99999999");
            adimissaoController.toXmlFile(rsAdmissaoController);
            /* Leiaute Aposentadoria 9.2.3 */
            // AposentadoriaController aposentadoriaController = new
            // AposentadoriaController(bDCommands,
            // geraAposentadoria);
            // ResultSet rsAposentadoriaController =
            // aposentadoriaController.getAposentadoriaBatch("00000000",
            // "99999999");
            // aposentadoriaController.toXmlFile(rsAposentadoriaController);
            /* Leiaute AlteracaoJornadaDeTrabalho 9.2.4 */
            AlteracaoJornadaDeTrabalhoController alteracaoJornadaDeTrabalhoController = new AlteracaoJornadaDeTrabalhoController(
                    bDCommands, geraAlteracaoJornadaDeTrabalho);
            alteracaoJornadaDeTrabalhoController.toXmlFile();
            /* Leiaute Cessao 9.2.5 */
            CessaoController cessaoController = new CessaoController(bDCommands, geraCessao);
            ResultSet rsCessaoController = cessaoController.getCessaoBatch("00000000", "99999999");
            cessaoController.toXmlFile(rsCessaoController);
            /* Leiaute Disponibilidade 9.2.6 */
            DisponibilidadeController disponibilidadeController = new DisponibilidadeController(bDCommands,
                    geraDisponibilidade);
            ResultSet rsDisponibilidadeController = disponibilidadeController.getDisponibilidadeBatch("00000000",
                    "99999999");
            disponibilidadeController.toXmlFile(rsDisponibilidadeController);
            /* Leiaute Desligamento 9.2.7 */
            DesligamentoController desligamentoController = new DesligamentoController(bDCommands,
                    geraDesligamento);
            ResultSet rsDesligamentoController = desligamentoController.getDesligamentoBatch("00000000",
                    "99999999");
            desligamentoController.toXmlFile(rsDesligamentoController);
            /* Leiaute DesignacaoCargoComissaoFuncaoGratificada 9.2.8 */
            DesignacaoCargoComissaoFuncaoGratificadaController designacaoCgCmFnGratifController = new DesignacaoCargoComissaoFuncaoGratificadaController(
                    bDCommands, geraDesignacaoCargoComissaoFuncaoGratificada);
            ResultSet rsDesignacaoCargoComissaoFuncaoGratificadaController = designacaoCgCmFnGratifController
                    .getDesignacaoCgCmFnGratifBatch("00000000", "99999999");
            designacaoCgCmFnGratifController.toXmlFile(rsDesignacaoCargoComissaoFuncaoGratificadaController);
            /* Leiaute DesignacaoCargoComissaoFuncaoGratificada 9.2.9 */
            LicencaController licencaController = new LicencaController(bDCommands, geraLicenca);
            ResultSet rsLicencaController = licencaController.getLicencaBatch("00000000", "99999999");
            licencaController.toXmlFile(rsLicencaController);
            /* Leiaute Pensao 9.2.10 */
            // PensaoController pensaoController = new PensaoController(bDCommands,
            // geraPensao);
            // ResultSet rsPensaoController = pensaoController.getPensaoBatch("00000000",
            // "99999999");
            // pensaoController.toXmlFile(rsPensaoController);
            /* Leiaute Pensionista 9.2.11 */
            // PensionistaController pensionistaController = new
            // PensionistaController(bDCommands, geraPensionista);
            // ResultSet rsPensionistaController =
            // pensionistaController.getPensionistaBatch("00000000", "99999999");
            // pensionistaController.toXmlFile(rsPensionistaController);
            /* Leiaute ProgressaoFuncional 9.2.12 */
            ProgressaoFuncionalController progressaoFuncionalController = new ProgressaoFuncionalController(
                    bDCommands, geraProgressaoFuncional);
            ResultSet rsProgressaoFuncionalController = progressaoFuncionalController.getServidoresBatch("00000000",
                    "99999999");
            progressaoFuncionalController.toXmlFile(rsProgressaoFuncionalController);
            /* Leiaute Readaptacao 9.2.13 */
            ReadaptacaoController readaptacaoController = new ReadaptacaoController(bDCommands, geraReadaptacao);
            ResultSet rsReadaptacaoController = readaptacaoController.getReadaptacaoBatch("00000000",
                    "99999999");
            readaptacaoController.toXmlFile(rsReadaptacaoController);
            /* Leiaute Reconducao 9.2.14 */
            ReconducaoController reconducaoController = new ReconducaoController(bDCommands, geraReconducao);
            ResultSet rsReconducaoController = reconducaoController.getReconducaoBatch("00000000",
                    "99999999");
            reconducaoController.toXmlFile(rsReconducaoController);
            /* Leiaute Reintegracao 9.2.15 */
            ReintegracaoController reintegracaoController = new ReintegracaoController(bDCommands,
                    geraReintegracao);
            ResultSet rsReintegracaoController = reintegracaoController.getReintegracaoBatch("00000000",
                    "99999999");
            reintegracaoController.toXmlFile(rsReintegracaoController);
            /* Leiaute Reintegracao 9.2.16 */
            ReenquadramentoController reenquadramentoController = new ReenquadramentoController(
                    bDCommands, geraReenquadramento);
            reenquadramentoController.toXmlFile();
            /* Leiaute ItemFolha 9.2.15 */
            ItemFolhaController itemFolhaController = new ItemFolhaController(bDCommands, geraItemFolha);
            ResultSet rsItemFolhaController = itemFolhaController.getItemFolhaBatch("00000000",
                    "99999999");
            itemFolhaController.toXmlFile(rsItemFolhaController);

        } catch (Exception ex) {
            Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (jTAErrors.getText().trim().length() > 0 || jTAWarnings.getText().trim().length() > 0) {
                jBtnLogs.setVisible(true);
            }
            jBtnFolder.setVisible(true);
            boolean errorsWarning = false;
            if (getErrorsCount() > 0 && getWarningsCount() > 0) {
                MGSiap.toLogs(
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
                        MGSiap.toLogs(erro, MGSiap.ERROR_TYPE);
                    }
                    /* Fim de ordenar erros */
                    if (getErrorsCount() > 0) {
                        if (!errorsWarning) {
                            MGSiap.toLogs("Os Arquivos XML foram gerados com " + getErrorsCount() + " erros",
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
                        MGSiap.toLogs(msg, 0);
                    }
                    try (BufferedWriter logWriter = new BufferedWriter(
                            new FileWriterWithEncoding(myLogObj.toPath().toString(), StandardCharsets.UTF_8,
                                    false))) {
                        jTAErrors.write(logWriter);
                        logWriter.close();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                if (getWarningsCount() > 0) {
                    /* Ordenar avisos */
                    String[] avisos = new String[jTAWarnings.getLineCount()];
                    avisos = jTAWarnings.getText().split("\\n");
                    Arrays.sort(avisos);
                    jTAWarnings.setText("");
                    for (String aviso : avisos) {
                        MGSiap.toLogs(aviso, MGSiap.WARNING_TYPE);
                    }
                    jTAWarnings.setCaretPosition(0);
                    /* Fim de ordenar avisos */
                    if (getWarningsCount() > 0) {
                        if (!errorsWarning) {
                            MGSiap.toLogs(
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
                        MGSiap.toLogs(msg, 0);
                    }
                    try (BufferedWriter warningWriter = new BufferedWriter(
                            new FileWriterWithEncoding(myWarningObj.toPath().toString(), StandardCharsets.UTF_8,
                                    false))) {
                        jTAWarnings.write(warningWriter);
                        warningWriter.close();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(MGSiap.class.getName()).log(Level.SEVERE, null, ex);
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
        MGSiap.conn = conn;
    }

    public static void setErrorsCount(int type) {
        if (type == ERROR_TYPE)
            MGSiap.errorsCount++;
        else if (type == WARNING_TYPE)
            MGSiap.warningsCount++;
    }

    public static Integer getErrorsCount() {
        return MGSiap.errorsCount;
    }

    public static Integer getWarningsCount() {
        return MGSiap.warningsCount;
    }

    public static Opcoes getOpcoes() {
        return opcoes;
    }

    public static void setOpcoes(Opcoes opcoes) {
        MGSiap.opcoes = opcoes;
    }

    public static BDCommands getbDCommands() {
        return bDCommands;
    }

    public static void setbDCommands(BDCommands bDCommands) {
        MGSiap.bDCommands = bDCommands;
    }

    public static String getShell() {
        return shell;
    }

    public static void setShell(String shell) {
        MGSiap.shell = shell;
    }

    public static Long getTimeI() {
        return timeI;
    }

    public static void setTimeI(Long timeI) {
        MGSiap.timeI = timeI;
    }

    public static Long getTimeF() {
        return timeF;
    }

    public static void setTimeF(Long timeF) {
        MGSiap.timeF = timeF;
    }

    public static Long getTimeLeft() {
        return timeLeft;
    }

    public static void setTimeLeft(Long timeLeft) {
        MGSiap.timeLeft = timeLeft;
    }

    public static JLabel getJltOper() {
        return jltOper;
    }

    public static void setJltOper(JLabel jltOper) {
        MGSiap.jltOper = jltOper;
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
        MGSiap.btnVisualizar = btnVisualizar;
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
        MGSiap.cbAno = cbAno;
    }

    public static JComboBox<String> getCbComplementar() {
        return cbComplementar;
    }

    public static void setCbComplementar(JComboBox<String> cbComplementar) {
        MGSiap.cbComplementar = cbComplementar;
    }

    public static JComboBox<String> getCbMes() {
        return cbMes;
    }

    public static void setCbMes(JComboBox<String> cbMes) {
        MGSiap.cbMes = cbMes;
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

    public static String getFileFolder() {
        return fileFolder;
    }

}
