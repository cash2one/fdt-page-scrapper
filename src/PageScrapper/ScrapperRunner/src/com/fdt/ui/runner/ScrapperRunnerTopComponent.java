/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.ui.runner;

import com.fdt.scrapper.ICallback;
import com.fdt.scrapper.ScrapperTaskRunner;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFileChooser;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//com.fdt.ui.runner//ScrapperRunner//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ScrapperRunnerTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.fdt.ui.runner.ScrapperRunnerTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ScrapperRunnerAction",
preferredID = "ScrapperRunnerTopComponent")
@Messages({
    "CTL_ScrapperRunnerAction=ScrapperRunner",
    "CTL_ScrapperRunnerTopComponent=ScrapperRunner Window",
    "HINT_ScrapperRunnerTopComponent=This is a ScrapperRunner window"
})
public final class ScrapperRunnerTopComponent extends TopComponent {
    
    private static final String DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";
    private static final Logger log = Logger.getLogger(ScrapperRunnerTopComponent.class);
    
    private ScrapperTaskRunner taskRunner;

    public ScrapperRunnerTopComponent() {
        initComponents();
        DOMConfigurator.configure("../log4j.xml");
        setName(Bundle.CTL_ScrapperRunnerTopComponent());
        setToolTipText(Bundle.HINT_ScrapperRunnerTopComponent());

    }
    
    public static String now(String DATE_FORMAT) {
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
	return sdf.format(cal.getTime());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser2 = new javax.swing.JFileChooser();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jLabel7 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();

        jFileChooser2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooser2ActionPerformed(evt);
            }
        });

        jTextField1.setEditable(false);
        jTextField1.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField1.text")); // NOI18N
        jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField1MouseClicked(evt);
            }
        });

        jTextField2.setEditable(false);
        jTextField2.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField2.text")); // NOI18N
        jTextField2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField2MouseClicked(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel3.text")); // NOI18N

        jTextField3.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel5.text")); // NOI18N

        jTextField4.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField4.text")); // NOI18N
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });

        jPasswordField1.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jPasswordField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel6.text")); // NOI18N

        jTextField5.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField5.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox1, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jCheckBox1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jCheckBox2, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jCheckBox2.text")); // NOI18N
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel7.text")); // NOI18N

        jTextField6.setEditable(false);
        jTextField6.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField6.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jLabel8.text")); // NOI18N

        jTextField7.setText(org.openide.util.NbBundle.getMessage(ScrapperRunnerTopComponent.class, "ScrapperRunnerTopComponent.jTextField7.text")); // NOI18N
        jTextField7.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField7KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(92, 92, 92)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel6)
                            .addComponent(jLabel8))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField7)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jCheckBox1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jCheckBox2))
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jTextField2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                                        .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jTextField3)
                                        .addComponent(jTextField5)))
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField4)
                            .addComponent(jPasswordField1)
                            .addComponent(jTextField6))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addGap(39, 39, 39)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBox1)
                    .addComponent(jCheckBox2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 28, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addComponent(jButton1))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(51, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jFileChooser2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFileChooser2ActionPerformed

    private void jTextField1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField1MouseClicked
        int returnVal = jFileChooser2.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileLinkList = jFileChooser2.getSelectedFile();
            // What to do with the file, e.g. display it in a TextArea
            jTextField1.setText(fileLinkList.getAbsolutePath());
        } else {
            log.info("File access cancelled by user.");
        }
    }//GEN-LAST:event_jTextField1MouseClicked

    private void jTextField2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField2MouseClicked
        int returnVal = jFileChooser2.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File fileProxyList = jFileChooser2.getSelectedFile();
            // What to do with the file, e.g. display it in a TextArea
            jTextField2.setText(fileProxyList.getAbsolutePath());
        } else {
            log.info("File access cancelled by user.");
        }
    }//GEN-LAST:event_jTextField2MouseClicked

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        jButton1.setEnabled(false);
        String fieldValue = null;
        String login = jTextField4.getText();
        char[] pass = jPasswordField1.getPassword();
        String urlsFilePath = jTextField1.getText();
        String proxyFilePath = jTextField2.getText();
        boolean scrapResultFromInternet = jCheckBox1.isSelected();
        boolean scrapResultAppendToPrevResult = jCheckBox2.isSelected();

        int maxThreadCount = 100;
        fieldValue = jTextField3.getText();
        if (fieldValue != null || !"".equals(fieldValue)) {
            maxThreadCount = Integer.valueOf(fieldValue.trim());
        }

        long proxyDelay = 10000L;
        fieldValue = jTextField5.getText();
        if (fieldValue != null || !"".equals(fieldValue)) {
            proxyDelay = Long.valueOf(fieldValue.trim())*1000L;
        }
        
        int topCountForScan = 0;
        fieldValue = jTextField7.getText();
        if (fieldValue != null || !"".equals(fieldValue)) {
            topCountForScan = Integer.valueOf(fieldValue.trim());
        }

        //TODO save previous results
        String resultFileName = "../success_result";
        String extension = ".csv";
        File resultFile = new File(resultFileName+extension);
        if(resultFile.exists()){
            try {
                FileUtils.copyFile(resultFile, new File(resultFileName + "_" + now(DATE_FORMAT) +extension));
            } catch (IOException e) {
                log.error("Error occured during copy result file",e);
            }
        }
        if(resultFile.exists() && !scrapResultAppendToPrevResult){
            resultFile.delete();
        }
                
        //TODO Start scrapper
        try {
            jButton1.setEnabled(false);
            taskRunner = new ScrapperTaskRunner(login, pass, proxyFilePath, urlsFilePath, maxThreadCount, proxyDelay, "../success_result.csv",scrapResultFromInternet, scrapResultAppendToPrevResult, topCountForScan, new CallBack());
            taskRunner.getTaskFactory().setOnLoadTaskListener(new ICallback(){
                @Override       
                public Object callback(){
                    jTextField6.setText("Обработано 0 из " + taskRunner.getTaskFactory().getTotalCount());
                    return null;
                }
            });
            Thread thread = new Thread(taskRunner);
            thread.start();
        } catch (Throwable e) {
            log.error("Error occured during page scrap",e);
            jButton1.setEnabled(true);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private class CallBack implements ICallback{
        @Override       
        public Object callback(){
            int success = taskRunner.getTaskFactory().getSuccessCount();
            int error = taskRunner.getTaskFactory().getErrorCount();
            int total = taskRunner.getTaskFactory().getTotalCount();
            jTextField6.setText(String.format("Обработано %d ( из них с ошибкой %d ) из %d", (success + error),error, total));
            if(total == (success + error)){
                jButton1.setEnabled(true);
            }else{
                jButton1.setEnabled(false);
            }
            return null;
        }
    }
    
    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void jTextField7KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField7KeyReleased
        // TODO add your handling code here:
        String value = jTextField7.getText();
        if("0".equals(value)){
            jCheckBox1.setEnabled(true);
            jCheckBox2.setEnabled(true);
        }else{
            jCheckBox1.setSelected(true);
            jCheckBox1.setEnabled(false);
            jCheckBox2.setSelected(false);
            jCheckBox2.setEnabled(false);
        }
    }//GEN-LAST:event_jTextField7KeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JFileChooser jFileChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
