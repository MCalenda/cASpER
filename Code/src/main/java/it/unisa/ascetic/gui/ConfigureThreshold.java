package it.unisa.ascetic.gui;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ConfigureThreshold extends DialogWrapper {

    private JComboBox<String> algorith;

    private HashMap<String, JPanel> livelli;
    private static ArrayList<String> smell;
    private JCheckBox standard;
    private HashMap<String, JSlider> sogliaT;
    private HashMap<String, JTextField> coseno;
    private HashMap<String, JTextField> dipendenze;
    private JPanel app;

    private static HashMap<String, Double> cosStandard;
    private static HashMap<String, Integer> dipendenceStandard;
    private static HashMap<String, Double> cos;
    private static HashMap<String, Integer> dipendence;
    private static String algoritmi;
    private static JPanel slider, app2;

    public ConfigureThreshold() {
        super(true);
        setResizable(false);
        smell = new ArrayList<String>();
        smell.add("Feature envy");
        smell.add("Misplaced class");
        smell.add("Blob");
        smell.add("Promiscuous package");
        init();
        setTitle("CONFIGURE THRESHOLD");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {

        cosStandard = new HashMap<String, Double>();
        dipendenceStandard = new HashMap<String, Integer>();
        cos = new HashMap<String, Double>();
        dipendence = new HashMap<String, Integer>();
        livelli = new HashMap<String, JPanel>();
        JPanel primo, secondo;
        JLabel textual, structural;
        sogliaT = new HashMap<String, JSlider>();
        coseno = new HashMap<String, JTextField>();
        dipendenze = new HashMap<String, JTextField>();

        cosStandard.put("cosenoFeature envy", 0.5);
        cosStandard.put("cosenoMisplaced class", 0.0);
        cosStandard.put("cosenoBlob", 0.5);
        cosStandard.put("cosenoPromiscuous package", 0.5);
        dipendenceStandard.put("dipendenzaFeature envy", 0);
        dipendenceStandard.put("dipendenzaMisplaced class", 0);
        dipendenceStandard.put("dipendenzaBlob", 350);
        dipendenceStandard.put("dipendenzaBlob2", 20);
        dipendenceStandard.put("dipendenzaBlob3", 500);
        Boolean set = true;

        JPanel contentPane = new JPanel();
        contentPane.setPreferredSize(new Dimension(1200, 500));
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel centerPanel = new JPanel();

        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        contentPane.add(centerPanel, BorderLayout.CENTER);

        try {
            FileReader f = new FileReader(System.getProperty("user.home") + File.separator + ".ascetic" + File.separator + "threshold.txt");
            BufferedReader b = new BufferedReader(f);
            String[] list;

            for (String s : smell) {
                list = b.readLine().split(",");
                cos.put("coseno" + s, Double.parseDouble(list[0]));
                if (!s.equalsIgnoreCase("promiscuous package")) {
                    dipendence.put("dipendenza" + s, Integer.parseInt(list[1]));
                    if (!s.equalsIgnoreCase("blob")) {
                        algoritmi = list[2];
                    } else {
                        dipendence.put("dipendenza" + s + "2", Integer.parseInt(list[2]));
                        dipendence.put("dipendenza" + s + "3", Integer.parseInt(list[3]));
                        algoritmi = list[4];
                    }
                }

                if (!(Double.parseDouble(list[0]) == 0.5 || (s.equalsIgnoreCase("misplaced class") && Double.parseDouble(list[0]) == 0.0)) || !((!s.equalsIgnoreCase("blob") && Integer.parseInt(list[1]) == 0) || (s.equalsIgnoreCase("blob") && (Integer.parseInt(list[1]) == 350) && (Integer.parseInt(list[2]) == 20) && (Integer.parseInt(list[3]) == 500))) || !algoritmi.equalsIgnoreCase("all")) {
                    set = false;
                }
            }
        } catch (Exception e) {
            reset(cosStandard, dipendenceStandard);
        }

        for (String s : smell) {
            livelli.put(s, new JPanel());
            livelli.get(s).setLayout(new BoxLayout(livelli.get(s), BoxLayout.Y_AXIS));
            primo = new JPanel();
            secondo = new JPanel();

            primo.setLayout(new GridLayout(0, 2, 0, 0));

            textual = new JLabel("Soglia algoritmi testuale:");
            textual.setHorizontalAlignment(SwingConstants.CENTER);
            primo.add(textual);
            app = new JPanel();

            addFieldTextual("coseno" + s);
            primo.add(app);
            livelli.get(s).add(primo);

            if (!s.equalsIgnoreCase("promiscuous package")) {
                livelli.get(s).add(secondo);

                secondo.setLayout(new GridLayout(0, 2, 0, 0));

                structural = new JLabel("Soglia algoritmi strutturali:");
                structural.setHorizontalAlignment(SwingConstants.CENTER);
                secondo.add(structural);

                app = new JPanel();
                if (!s.equalsIgnoreCase("Blob")) {
                    app.add(new JLabel("Dipendenze ="));
                } else {
                    app.add(new JLabel("LCOM ="));
                }
                addFieldStructural("dipendenza" + s);

                if (s.equalsIgnoreCase("Blob")) {
                    app.add(new JLabel("FeatureSum ="));
                    addFieldStructural("dipendenza" + s + "2");
                    app.add(new JLabel("ELOC ="));
                    addFieldStructural("dipendenza" + s + "3");
                }
                secondo.add(app);
            }

            livelli.get(s).setBorder(new TitledBorder(s));
            centerPanel.add(livelli.get(s));
        }

        JPanel scelta = new JPanel();
        app = new JPanel();
        algorith = new ComboBox<>();
        algorith.addItem("All");
        algorith.addItem("Textual");
        algorith.addItem("Structural");
        algorith.setSelectedItem(algoritmi);
        standard = new JCheckBox("Soglie di default");
        standard.setHorizontalAlignment(SwingConstants.CENTER);
        filtro(algoritmi);
        app.add(new JLabel("Tipo di algoritmo da usare:"));
        app.add(algorith);
        scelta.add(app);
        JPanel sotto = new JPanel();
        sotto.setLayout(new GridLayout(0, 2, 0, 0));

        sotto.add(scelta);

        algorith.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent action) {
                JComboBox cb = (JComboBox) action.getSource();
                algoritmi = (String) cb.getSelectedItem();
                filtro(algoritmi);
            }
        });

        standard.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent a) {

                if (!standard.isSelected()) {
                    reset(cos, dipendence);
                    standard.setSelected(false);
                } else {
                    stato();
                    reset(cosStandard, dipendenceStandard);
                    standard.setSelected(true);
                }
            }
        });

        standard.setSelected(set);
        sotto.add(standard);
        contentPane.add(sotto, BorderLayout.SOUTH);

        return contentPane;
    }

    @NotNull
    @Override
    protected Action[] createActions() {
        Action okAction = new DialogWrapperExitAction("OK", 1) {
            @Override
            protected void doAction(ActionEvent actionEvent) {
                try {
                    scrivi();
                } finally {
                    super.doAction(actionEvent);
                }
            }
        };
        Action exitAction = new DialogWrapperExitAction("Cancel", 0) {
            @Override
            protected void doAction(ActionEvent actionEvent) {
                try {
                    new FileReader(System.getProperty("user.home") + File.separator + ".ascetic" + File.separator + "threshold.txt");
                } catch (Exception e) {
                    scrivi();
                } finally {
                    super.doAction(actionEvent);
                }
            }
        };
        return new Action[]{okAction, exitAction};
    }

    private void reset(HashMap<String, Double> valoriCos, HashMap<String, Integer> valoriDip) {
        for (String k : sogliaT.keySet()) {
            sogliaT.get(k).setVisible(true);
            if (k.equals("sogliaMisplaced class")) {
                sogliaT.get(k).setValue((int) (valoriCos.get(k) * 100));
            } else {
                sogliaT.get(k).setValue((int) (valoriCos.get(k) * 100));
            }
        }
        for (String k : coseno.keySet()) {
            coseno.get(k).setVisible(true);
            if (k.equals("cosenoMisplaced class")) {
                coseno.get(k).setText(valoriCos.get(k) + "");
            } else {
                coseno.get(k).setText(valoriCos.get(k) + "");
            }
        }
        for (String k : dipendenze.keySet()) {
            dipendenze.get(k).setVisible(true);
            if (k.contains("dipendenzaBlob")) {
                dipendenze.get("dipendenzaBlob").setText(valoriDip.get("dipendenzaBlob") + "");
                dipendenze.get("dipendenzaBlob2").setText(valoriDip.get("dipendenzaBlob2") + "");
                dipendenze.get("dipendenzaBlob3").setText(valoriDip.get("dipendenzaBlob3") + "");
            } else {
                dipendenze.get(k).setText(valoriDip.get(k) + "");
            }
        }

        algorith.setSelectedIndex(0);
        algoritmi = "All";
    }

    private void addFieldTextual(String name) {
        slider = new JPanel();
        slider.setLayout(new BoxLayout(slider, BoxLayout.X_AXIS));
        app2 = new JPanel();
        app.setLayout(new GridLayout(0, 2, 0, 0));
        slider.add(new JLabel("Coseno ="));
        sogliaT.put(name, new JSlider());
        sogliaT.get(name).setForeground(Color.WHITE);
        sogliaT.get(name).setFont(new Font("Arial", Font.PLAIN, 12));
        sogliaT.get(name).setToolTipText("");
        sogliaT.get(name).setPaintTicks(true);
        sogliaT.get(name).setValue((int) (cos.get(name) * 100));
        sogliaT.get(name).setMinorTickSpacing(10);
        sogliaT.get(name).setMinimum(0);
        slider.add(sogliaT.get(name));
        app.add(slider);

        coseno.put(name, new JTextField());
        coseno.get(name).setText(cos.get(name) + "");
        if (name.equalsIgnoreCase("misplaced class")) {
            coseno.get(name).setToolTipText("Tale coseno verra' successivamente riportata nell'intervallo [0-1]");
        }
        coseno.get(name).setColumns(10);
        app2.add(coseno.get(name));
        app.add(app2);

        sogliaT.get(name).addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent event) {
                JSlider t = (JSlider) event.getSource();
                String corrente = null;
                Iterator<String> list = sogliaT.keySet().iterator();
                while (list.hasNext() && !sogliaT.get(corrente = list.next()).equals(t)) {
                }
                coseno.get(corrente).setText(String.valueOf(((double) t.getValue()) / 100));
                if (t.getValue() != cosStandard.get(corrente)) {
                    standard.setSelected(false);
                }
            }
        });

        coseno.get(name).setToolTipText("Inserire valore tra [0-1]");
        coseno.get(name).addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent c) {
                try {
                    JTextField t = (JTextField) c.getSource();
                    double valore = Double.parseDouble(t.getText());
                    Iterator<String> list = coseno.keySet().iterator();
                    String corrente = null;
                    while (list.hasNext() && !coseno.get(corrente = list.next()).equals(t)) {
                    }

                    if (valore >= 0.0 && valore <= 1.0) {
                        sogliaT.get(corrente).setValue((int) (valore * 100));
                    } else {
                        if (valore < 0.0) {
                            sogliaT.get(corrente).setValue(0);
                            t.setText(0.0 + "");
                        } else {
                            if (valore > 1.0) {
                                sogliaT.get(corrente).setValue(100);
                                t.setText("1.0");
                            } else {
                                sogliaT.get(corrente).setValue((int) valore);
                                t.setText(valore + "");
                            }
                        }
                    }
                    if (t.getText().equals(cosStandard.get(corrente))) {
                        standard.setSelected(false);
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Inserire valori decimali con \".\" [0-1]", "", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }

    private void addFieldStructural(String name) {
        dipendenze.put(name, new JTextField());
        dipendenze.get(name).setText(dipendence.get(name) + "");
        app.add(dipendenze.get(name));
        dipendenze.get(name).setColumns(10);
        dipendenze.get(name).setToolTipText("Inserire valore maggiore o uguale di 0");
        dipendenze.get(name).addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent d) {
                String corrente = null;
                try {
                    JTextField t = (JTextField) d.getSource();
                    Iterator<String> list = dipendenze.keySet().iterator();
                    while (list.hasNext() && !dipendenze.get(corrente = list.next()).equals(t)) {  //per capire cosa ho cliccato
                    }
                    if (Integer.parseInt(t.getText()) >= 0) {
                        if (!t.getText().equals(dipendenceStandard.get(corrente))) {
                            standard.setSelected(false);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Inserire valori intero >=0", "Errore", JOptionPane.WARNING_MESSAGE);
                        dipendenze.get(corrente).setText("0");
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Inserire valori intero >=0", "Errore", JOptionPane.WARNING_MESSAGE);
                    dipendenze.get(corrente).setText("0");
                }
            }
        });
    }

    private void stato() {
        for (String s : cos.keySet()) {
            cos.put(s, Double.parseDouble(coseno.get(s).getText()));
        }
        for (String s : dipendence.keySet()) {
            dipendence.put(s, Integer.parseInt(dipendenze.get(s).getText()));
        }
    }

    private void scrivi() {
        try {
            FileWriter f = new FileWriter(System.getProperty("user.home") + File.separator + ".ascetic" + File.separator + "threshold.txt");
            BufferedWriter out = new BufferedWriter(f);
            for (String a : smell) {
                if (a.equalsIgnoreCase("misplaced class") && standard.isSelected()) {
                    out.write("0.0,");
                } else {
                    out.write("0" + coseno.get("coseno" + a).getText() + ",");
                }

                if (!a.equalsIgnoreCase("promiscuous package")) {
                    out.write("0" + dipendenze.get("dipendenza" + a).getText() + ",");
                } else {
                    out.write("0,");
                }

                if (a.equalsIgnoreCase("blob")) {
                    out.write("0" + dipendenze.get("dipendenza" + a + "2").getText() + ",");
                    out.write("0" + dipendenze.get("dipendenza" + a + "3").getText() + ",");
                }
                out.write(String.valueOf(algorith.getSelectedItem()));
                out.flush();
                out.newLine();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void filtro(String algoritmi) {
        switch (algoritmi) {
            case "All":
                for (String s : livelli.keySet()) {
                    livelli.get(s).getComponent(0).setVisible(true);
                    if (!s.equals("Promiscuous package")) {
                        livelli.get(s).getComponent(1).setVisible(true);
                    } else {
                        livelli.get(s).setVisible(true);
                    }
                }
                break;
            case "Structural":
                for (String s : livelli.keySet()) {
                    livelli.get(s).getComponent(0).setVisible(false);
                    if (!s.equals("Promiscuous package")) {
                        livelli.get(s).getComponent(1).setVisible(true);
                    } else {
                        livelli.get(s).setVisible(false);
                    }
                }
                break;
            case "Textual":
                for (String s : livelli.keySet()) {
                    livelli.get(s).getComponent(0).setVisible(true);
                    if (!s.equals("Promiscuous package")) {
                        livelli.get(s).getComponent(1).setVisible(false);
                    } else {
                        livelli.get(s).setVisible(true);
                    }
                }
        }
        standard.setSelected(false);
    }


}