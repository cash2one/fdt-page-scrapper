/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fdt.ui;

import com.fdt.scrapper.task.PageTasks;
import com.fdt.scrapper.util.ResultParser;
import com.fdt.scrapper.util.ResultParserFilter;
import java.util.ArrayList;
import javax.swing.JPanel;
import javax.swing.tree.TreeModel;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.swing.outline.DefaultOutlineModel;
import org.netbeans.swing.outline.OutlineModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd = "-//org.mypck//FBrowser//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ScrapPagesTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window", id = "com.fdt.ui.ScrapPagesTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ScrapPagesTopComponentAction",
    preferredID = "ScrapPagesTopComponent"
)
@Messages({
    "CTL_FBrowserAction=ScrapPagesTopComponent",
    "CTL_FBrowserTopComponent=FBrowser Window",
    "HINT_FBrowserTopComponent=This is a FBrowser window"
})
public final class ScrapPagesTopComponent extends TopComponent {

    private static final Logger log = Logger.getLogger(ScrapPagesTopComponent.class);
    private static ArrayList<PageTasks> scrappResults = new ArrayList<PageTasks>();
    public ScrapPagesTopComponent() {
        initComponents();
        DOMConfigurator.configure("log4j.xml");
        reload();
        setName(Bundle.CTL_FBrowserTopComponent());
        setToolTipText(Bundle.HINT_FBrowserTopComponent());
    }
    
    private void reload(){
        ResultParser rp = new ResultParser();
        scrappResults = rp.parseResultFile("success_result.csv");
        TreeModel treeMdl = new FileTreeModel(scrappResults);
        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new FileRowModel(), true);
        outline1.setRootVisible(false);
        outline1.setRenderDataProvider(new DomainRenderer());  
        outline1.setModel(mdl);
        
        TreeModel treeMdl2 = new FileTreeModel(rp.parseResultFile("success_result.csv"));
        OutlineModel mdl2 = DefaultOutlineModel.createOutlineModel(treeMdl2, new FileRowModel(), true);
        outline2.setRootVisible(false);
        outline2.setRenderDataProvider(new DomainRenderer());  
        outline2.setModel(mdl2);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        outline1 = new org.netbeans.swing.outline.Outline();
        jPanel2 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        outline2 = new org.netbeans.swing.outline.Outline();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();

        jPanel1.setPreferredSize(new java.awt.Dimension(705, 542));
        jPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                jPanel1ComponentResized(evt);
            }
        });

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jScrollPane1.border.title"))); // NOI18N
        jScrollPane1.setViewportView(outline1);

        jPanel2.setMaximumSize(new java.awt.Dimension(32767, 65));
        jPanel2.setMinimumSize(new java.awt.Dimension(100, 65));

        org.openide.awt.Mnemonics.setLocalizedText(jButton2, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jButton2.text")); // NOI18N
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jScrollPane2.border.title"))); // NOI18N
        jScrollPane2.setViewportView(outline2);

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButton3, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jButton3.text")); // NOI18N
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jLabel1.text")); // NOI18N

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField1.setText(org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jTextField1.text")); // NOI18N
        jTextField1.setMaximumSize(new java.awt.Dimension(46, 20));
        jTextField1.setMinimumSize(new java.awt.Dimension(46, 20));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jLabel2.text")); // NOI18N

        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField2.setText(org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jTextField2.text")); // NOI18N
        jTextField2.setMaximumSize(new java.awt.Dimension(65, 20));
        jTextField2.setMinimumSize(new java.awt.Dimension(65, 20));
        jTextField2.setPreferredSize(new java.awt.Dimension(65, 20));
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jLabel3.text")); // NOI18N

        jTextField3.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField3.setText(org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jTextField3.text")); // NOI18N
        jTextField3.setMaximumSize(new java.awt.Dimension(65, 20));
        jTextField3.setMinimumSize(new java.awt.Dimension(65, 20));
        jTextField3.setPreferredSize(new java.awt.Dimension(65, 20));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jLabel4.text")); // NOI18N

        jTextField4.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField4.setText(org.openide.util.NbBundle.getMessage(ScrapPagesTopComponent.class, "ScrapPagesTopComponent.jTextField4.text")); // NOI18N
        jTextField4.setMaximumSize(new java.awt.Dimension(65, 20));
        jTextField4.setMinimumSize(new java.awt.Dimension(65, 20));
        jTextField4.setPreferredSize(new java.awt.Dimension(65, 20));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 774, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 425, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jPanel1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_jPanel1ComponentResized
       JPanel jPanel = (JPanel)evt.getSource();
       System.out.println("Height: " + jPanel.getHeight());
       int totalListOutHeight = (jPanel.getHeight()-jPanel2.getHeight()-jPanel3.getHeight()-12)/2;
       int filterListOutHeight = jPanel.getHeight()-jPanel2.getHeight()-jPanel3.getHeight()-totalListOutHeight;
       jScrollPane1.setSize(jScrollPane1.getWidth(), totalListOutHeight);
       jScrollPane2.setSize(jScrollPane2.getWidth(), filterListOutHeight);
    }//GEN-LAST:event_jPanel1ComponentResized

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        ResultParser rp = new ResultParser();
        TreeModel treeMdl = new FileTreeModel(scrappResults);
        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new FileRowModel(), true);
        outline2.setRootVisible(false);
        outline2.setRenderDataProvider(new DomainRenderer());  
        outline2.setModel(mdl);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        ResultParser rp = new ResultParser();
        
        int minDomainCount = Integer.MIN_VALUE;
        int maxAlexaRank = Integer.MAX_VALUE;
        int minAllIndex = Integer.MIN_VALUE;
        int minWeekIndex = Integer.MIN_VALUE;
        
        //domain count
        String textFieldValue = jTextField1.getText().trim();
        if(!"".equals(textFieldValue)){
            minDomainCount = Integer.valueOf(textFieldValue);
        }else{
            minDomainCount = Integer.MIN_VALUE;
        }
        
        //alexa rank count
        textFieldValue = jTextField2.getText().trim();
        if(!"".equals(textFieldValue)){
            maxAlexaRank = Integer.valueOf(textFieldValue);
        }else{
            maxAlexaRank = Integer.MAX_VALUE;
        }
        
        //google all index
        textFieldValue = jTextField3.getText().trim();
        if(!"".equals(textFieldValue)){
            minAllIndex = Integer.valueOf(textFieldValue);
        }else{
            minAllIndex = Integer.MIN_VALUE;
        }
        
        //google week index
        textFieldValue = jTextField4.getText().trim();
        if(!"".equals(textFieldValue)){
            minWeekIndex = Integer.valueOf(textFieldValue);
        }else{
            minWeekIndex = Integer.MIN_VALUE;
        }
        
        TreeModel treeMdl = new FileTreeModel(rp.filterResults(scrappResults, new ResultParserFilter(minDomainCount, maxAlexaRank, minAllIndex, minWeekIndex)));
        OutlineModel mdl = DefaultOutlineModel.createOutlineModel(treeMdl, new FileRowModel(), true);
        outline2.setRootVisible(false);
        outline2.setRenderDataProvider(new DomainRenderer());  
        outline2.setModel(mdl);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        reload();
    }//GEN-LAST:event_jButton2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private org.netbeans.swing.outline.Outline outline1;
    private org.netbeans.swing.outline.Outline outline2;
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
