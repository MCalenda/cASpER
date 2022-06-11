package it.unisa.casper.gui;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.table.JBTable;
import it.unisa.casper.gui.radarMap.RadarMapUtils;
import it.unisa.casper.gui.radarMap.RadarMapUtilsAdapter;
import it.unisa.casper.refactor.splitting_algorithm.SplitClasses;
import it.unisa.casper.refactor.splitting_algorithm.game_theory.GameTheorySplitClasses;
import it.unisa.casper.refactor.strategy.SplittingManager;
import it.unisa.casper.refactor.strategy.SplittingStrategy;
import it.unisa.casper.storage.beans.ClassBean;
import it.unisa.casper.structuralMetrics.CKMetrics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import src.main.java.it.unisa.casper.gui.StyleText;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class BlobPage extends DialogWrapper {

    private RadarMapUtils radarMapUtils;        //roba che serve per le radar map
    private ClassBean classBeanBlob;            //ClassBean sul quale avviene l'analisi
    private List<ClassBean> splittedClasses;    //lista di classi splittate

    private Project project;
    private JTextPane area;                     //area di testo dove viene mostrato in dettaglio il codice del CodeSmell selezionato
    private JPanel contentPanel;                //panel che raggruppa tutti gli elementi
    private JPanel panelRadarMapMaster;         //panel che ingloba la radar map
    private JPanel panelRadarMap;
    private JPanel panelMetric;                 //panel per le metriche
    private JPanel panelButton;                 //panel che raggruppa i bottoni
    private JPanel panelWest;                   //panel che raggruppa gli elementi a sinistra
    private JPanel panelEast;                   //panel che raggruppa gli elementi a destra
    private JPanel panelGrid2;                  //panel inserito nella seconda cella del gridLayout
    private JBTable table;                      //tabella dove sono visualizzati i codeSmell

    private JPanel gameTheoryPanel;
    private JCheckBox gameTheoryCheckbox;               //checkbox per attivare refactoring con Game Theory
    private JSlider gameTheorySlider;
    private SplittingStrategy splittingStrategy;

    private boolean errorOccured;               //serve per determinare se qualcosa è andato storto

    public BlobPage(ClassBean classBeanBlob, Project project) {
        super(true);
        this.classBeanBlob = classBeanBlob;
        this.project = project;
        this.errorOccured = false;
        setResizable(false);
        init();
        setTitle("BLOB PAGE");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        radarMapUtils = new RadarMapUtilsAdapter();

        panelRadarMap = radarMapUtils.createRadarMapFromClassBean(classBeanBlob, classBeanBlob.getFullQualifiedName());

        //INIZIALIZZO I PANEL
        contentPanel = new JPanel();            //pannello principale
        panelButton = new JPanel();             //pannello dei bottoni
        panelRadarMapMaster = new JPanel();     //pannello che ingloba le radarMap
        panelMetric = new JPanel();             //pannello che ingloba le metriche
        panelWest = new JPanel();               //pannello che ingloba gli elementi di sinistra
        panelEast = new JPanel();               //pannello che ingloba gli elementi di destra
        gameTheoryPanel = new JPanel();

        //INIZIALIZZO LA TABELLA E LA TEXT AREA
        area = new JTextPane();                 //text area dove viene visualizzato il codice in esame
        table = new JBTable();                  //tabella dove sono presenti gli smell da prendere in esame

        //SETTO TESTO NELLA TEXT AREA
        JPanel app = new JPanel();
        app.setLayout(new BorderLayout(0, 0));
        app.setBorder(new TitledBorder("Text content"));
        StyleText generator = new StyleText();
        area.setStyledDocument(generator.createDocument(classBeanBlob.getTextContent()));

        //SETTO LA TABELLA PER LE METRICHE
        table = new JBTable();
        Vector<String> tableHeaders = new Vector<>();
        tableHeaders.add("LOC");
        tableHeaders.add("WMC");
        tableHeaders.add("RFC");
        tableHeaders.add("CBO");
        tableHeaders.add("LCOM");

        tableHeaders.add("NOA");
        tableHeaders.add("NOM");
        tableHeaders.add("NOPA");

        Vector<String> tableElemet = new Vector<>();
        tableElemet.add(Integer.toString(CKMetrics.getLOC(classBeanBlob)));
        tableElemet.add(Integer.toString(CKMetrics.getWMC(classBeanBlob)));
        tableElemet.add(Integer.toString(CKMetrics.getRFC(classBeanBlob)));
        tableElemet.add(Integer.toString(CKMetrics.getCBO(classBeanBlob)));
        tableElemet.add(Integer.toString(CKMetrics.getLCOM(classBeanBlob)));

        tableElemet.add(Integer.toString(CKMetrics.getNOA(classBeanBlob)));
        tableElemet.add(Integer.toString(CKMetrics.getNOM(classBeanBlob)));
        tableElemet.add(Integer.toString(CKMetrics.getNOPA(classBeanBlob)));

        DefaultTableModel model = new DefaultTableModel(tableHeaders, 0);
        model.addRow(tableElemet);

        table.setModel(model);

        //Game Theory
        gameTheoryCheckbox = new JCheckBox("activate");
        gameTheoryCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        gameTheorySlider = new JSlider();
        gameTheorySlider.setPaintTicks(true);
        gameTheorySlider.setMinimum(1);
        gameTheorySlider.setMaximum(5);
        gameTheorySlider.setMinorTickSpacing(1);
        Hashtable<Integer, JLabel> labelTable = new Hashtable();
        labelTable.put( 1, new JLabel("0.1") );
        labelTable.put( 2, new JLabel("0.2") );
        labelTable.put( 3, new JLabel("0.3") );
        labelTable.put( 4, new JLabel("0.4") );
        labelTable.put( 5, new JLabel("0.5") );
        gameTheorySlider.setLabelTable(labelTable);
        gameTheorySlider.setPaintLabels(true);
        gameTheorySlider.setBorder(new TitledBorder("Jaccard Similarity threshold"));

        //SETTO I LAYOUT DEI PANEL

        gameTheoryPanel.setLayout(new GridLayout(2,1));
        gameTheoryPanel.setBorder(new TitledBorder("Game Theory"));

        panelButton.setLayout(new FlowLayout());
        panelRadarMapMaster.setLayout(new BorderLayout());
        panelWest.setLayout(new GridLayout(3, 1));
        panelEast.setLayout(new BorderLayout());
        panelMetric.setLayout((new BorderLayout()));
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        panelGrid2 = new JPanel();
        panelGrid2.setLayout(new BorderLayout());

        panelRadarMapMaster.add(panelRadarMap, BorderLayout.CENTER);

        panelMetric.setBorder(new TitledBorder("Metrics"));
        panelMetric.add(new JBScrollPane(table));
        table.setFillsViewportHeight(true);
        panelGrid2.add(panelMetric, BorderLayout.CENTER);
        panelGrid2.add(panelButton, BorderLayout.SOUTH);

        gameTheoryPanel.add(gameTheoryCheckbox);
        gameTheoryPanel.add(gameTheorySlider);

        panelWest.add(panelRadarMapMaster);
        panelWest.add(panelGrid2);
        panelWest.add(gameTheoryPanel);

        contentPanel.add(panelWest);
        JScrollPane scroll = new JBScrollPane(area);
        app.add(scroll, BorderLayout.CENTER);
        contentPanel.add(app);

        contentPanel.setPreferredSize(new Dimension(1050, 900));

        return contentPanel;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        Action okAction = new DialogWrapperAction("FIND SOLUTION") {
            String message;
            @Override
            protected void doAction(ActionEvent actionEvent) {

                message = "Something went wrong in computing solution";
                ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
                    try {
                        if (gameTheoryCheckbox.isSelected()) {
                            splittingStrategy = new GameTheorySplitClasses();
                            SplittingManager splittingManager = new SplittingManager(splittingStrategy);
                            splittedClasses = (List<ClassBean>) splittingManager.excuteSplitting(classBeanBlob, gameTheorySlider.getValue()/10.0);
                        } else {
                            splittingStrategy = new SplitClasses();
                            SplittingManager splittingManager = new SplittingManager(splittingStrategy);
                            splittedClasses = (List<ClassBean>) splittingManager.excuteSplitting(classBeanBlob, 0.09);
                        }
                        if (splittedClasses.size() == 1) {
                            if (gameTheoryCheckbox.isSelected()) {
                                message += "\nIt is not possible to extract more than one topic from the class";
                            } else {
                                message += "\nIt is not possible to split the class without introducing new smell";
                            }
                            errorOccured = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        errorOccured = true;
                    }
                }, "Blob", false, project);

                if (errorOccured) {
                    Messages.showMessageDialog(message, "Oh!No!", Messages.getErrorIcon());
                } else {
                    if (splittedClasses.size() < 2) {
                        message = "Error during creation of solution";
                        Messages.showMessageDialog(message, "Error", Messages.getErrorIcon());
                    } else {
                        BlobWizard blobWizardMock = new BlobWizard(classBeanBlob, splittedClasses, project, gameTheoryCheckbox.isSelected());
                        blobWizardMock.show();
                    }
                    close(0);
                }
            }
        };

        return new Action[]{okAction, new DialogWrapperExitAction("CANCEL", 0)};
    }
}