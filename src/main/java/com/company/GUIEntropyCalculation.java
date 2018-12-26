package com.company;

import org.apache.commons.math3.stat.Frequency;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.List;

public class GUIEntropyCalculation extends JFrame implements ActionListener {
    private FileWriter writer;
    private File file;
    private Button btnReset;
    private Button btnCount;
    private Button btnOpenFile;
    private Button btnSaveFile;
    private Label lblBinWidth;
    private Label lblNumberOfBins;
    private Label lbldeltaNw;
    private TextField tfBinWidth;
    private TextField tfProcess;
    private TextField tfdeltaNw;
    private TextField tfNumberOfBins;
    private ArrayList<Integer> Nw = new ArrayList<>();
    private double binWidth = 0;
    private int numberOfBins = 0;
    private int deltaNw = 1;

    private LinkedList<Double> dataInit = new LinkedList<>();
    private LinkedList<Double> dataSet = new LinkedList<>();
    private Map distributionMap = new TreeMap();
    private List<Double> freq = new ArrayList<>();
    private List<Double> discreteEntropyList = new ArrayList<>();
    private List<Double> continuousEntropyList = new ArrayList<>();
    private List<Double> w = new ArrayList<>();
    private List<Integer> numberOfBinsList = new ArrayList<>();
    private List yData = new ArrayList();
    private double continuousEntropy = 0;
    private double discreteEntropy = 0;
    public GUIEntropyCalculation() {

        /**
         * Open file
         */
        btnOpenFile = new Button("Open File");
        btnOpenFile.addActionListener(e -> {
            file = null;
            writer = null;
            dataInit.clear();
            dataSet.clear();
            FileDialog fileDialog = new FileDialog(GUIEntropyCalculation.this,"Select File:");
            fileDialog.setVisible(true);
            String fileLocation = fileDialog.getDirectory() + fileDialog.getFile();
            File file = new File(fileLocation);
            FileReader fr;
            try {
                fr = new FileReader(file);
                BufferedReader reader = new BufferedReader(fr);
                String line;
                while ((line = reader.readLine()) != null) {
                    dataSet.add(Double.parseDouble(line));
                }
            } catch (IOException err) {
                err.printStackTrace();
            }
            Collections.sort(dataSet);
            for (int i = 0; i < dataSet.size(); i++) {
                dataInit.add(dataSet.get(i) * 10 / dataSet.getLast());
            }
            System.out.println(fileLocation);
            System.out.println(dataInit);
            System.out.println(dataInit.size());
        });
        add(btnOpenFile);

        /**
         * Save file
         */
        btnSaveFile = new Button("Save File:");
        btnSaveFile.addActionListener(e -> {
            FileDialog fileDialog = new FileDialog(GUIEntropyCalculation.this,"Save File:");
            fileDialog.setVisible(true);
            String fileLocation = fileDialog.getDirectory() + fileDialog.getFile();
            file = new File(fileLocation);
            try {
                writer = new FileWriter(file);
                writer.write("Nw    Nw.F    w   discreteEntropy    continuousEntropy    normContEntropy  \r\n");
                writer.flush();
                for (int i = 0; i < numberOfBinsList.size(); i++) {
                    writer.write(
                            numberOfBinsList.get(i) + "  "
                                    + (double) numberOfBinsList.get(i) / (double) dataInit.size()
                                    + "   " + w.get(i) + "   " + discreteEntropyList.get(i)
                                    + "  " + continuousEntropyList.get(i)
                                    + "  " + continuousEntropyList.get(i) / Math.log(Double.parseDouble(tfBinWidth.getText()))
                                    + "  " + "    \r\n");
                    writer.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        add(btnSaveFile);

        /**
         * Set width of bin
         */
        lblBinWidth = new Label("Width of bin - w");
        add(lblBinWidth);
        tfBinWidth = new TextField(binWidth + "", 10);
        tfBinWidth.setEditable(true);
        add(tfBinWidth);
        btnCount = new Button("Count");
        add(btnCount);
        btnCount.addActionListener(this);

        /**
         * Reset
         */

        btnReset = new Button("Reset");

        btnReset.addActionListener(e -> {
            continuousEntropy = 0;
            discreteEntropy = 0;
            discreteEntropyList.clear();
            continuousEntropyList.clear();
            numberOfBinsList.clear();
            w.clear();
            numberOfBinsList.clear();
            Nw.clear();
            yData.clear();
            distributionMap.clear();
        });
        add(btnReset);


        /**
         * Set number of bins
         */
        lblNumberOfBins = new Label("Number of bins - Nw");
        add(lblNumberOfBins);
        tfNumberOfBins = new TextField(numberOfBins + "", 10);
        tfNumberOfBins.setEditable(true);
        add(tfNumberOfBins);

        /**
         * Set step of change Nw
         */
        lbldeltaNw = new Label("delta Nw");
        add(lbldeltaNw);
        tfdeltaNw = new TextField(deltaNw + "", 10);
        tfdeltaNw.setEditable(true);
        add(tfdeltaNw);

        tfProcess = new TextField(discreteEntropy + "\r\n",10);
        add(tfProcess);

        setTitle("Entropy");
        setSize(540,100);
        setLayout(new FlowLayout());
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }

    private Map<Double,Double> processRawData() {

        Frequency frequency = new Frequency();
        dataInit.forEach(d -> frequency.addValue(Double.parseDouble(d.toString())));

        dataInit.stream()
                .map(d -> Double.parseDouble(d.toString()))
                .distinct()
                .forEach(observation -> {
                    long observationFrequency = frequency.getCount(observation);
                    double upperBoundary = (observation > binWidth)
                            ? Math.ceil(observation / binWidth)* binWidth
                            : binWidth;
                    double lowerBoundary = (upperBoundary > binWidth)
                            ? upperBoundary* binWidth
                            : 0;
                    String bin = lowerBoundary + "-" + upperBoundary;
                    updateDistributionMap(lowerBoundary, bin, observationFrequency);
                });

        return distributionMap;
    }

    private void updateDistributionMap(double lowerBoundary, String bin, long observationFrequency) {

        double prevLowerBoundary = (lowerBoundary > binWidth) ? lowerBoundary - binWidth : 0;
        String prevBin = prevLowerBoundary + "-" + lowerBoundary;
        if(!distributionMap.containsKey(prevBin))
            distributionMap.put(prevBin, 0);

        if(!distributionMap.containsKey(bin)) {
            distributionMap.put(bin, observationFrequency);
        }
        else {
            long oldFrequency = Long.parseLong(distributionMap.get(bin).toString());
            distributionMap.replace(bin, oldFrequency + observationFrequency);
        }
    }

    public static void main(String[] args) {
        GUIEntropyCalculation guiEntropyCalculation = new GUIEntropyCalculation();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        deltaNw = Integer.parseInt(tfdeltaNw.getText());
        tfdeltaNw.setText(deltaNw + "");
        binWidth = Double.parseDouble(tfBinWidth.getText());
        double temp = binWidth;
        tfBinWidth.setText(binWidth + "");
        numberOfBins = Integer.parseInt(tfNumberOfBins.getText());
        tfNumberOfBins.setText(numberOfBins + "");

        for (int i = 1; i < numberOfBins; i++) {
            Nw.add(i);
        }

        for (int i = 0; i < Nw.size(); i = i + deltaNw) {
            binWidth = temp / Nw.get(i);
            Map distributionMap = processRawData();
            freq.addAll(distributionMap.values());
            yData.addAll(distributionMap.values());
            for (Object aYData : yData) {
                double relFreq = Double.parseDouble(aYData.toString()) / dataInit.size();
                System.out.println(Double.parseDouble(aYData.toString()) / dataInit.size());
                if (Double.parseDouble(aYData.toString()) != 0.0) {
                    continuousEntropy += -relFreq * Math.log(relFreq / binWidth);
                    discreteEntropy += -relFreq * Math.log(relFreq) / Math.log(Nw.get(i));
                }
            }
            yData.clear();
            freq.clear();
            distributionMap.clear();
            System.out.println(continuousEntropy + "entropy");
            discreteEntropyList.add(discreteEntropy);
            tfProcess.setText(continuousEntropy + "\r\n");
            continuousEntropyList.add(continuousEntropy);
            numberOfBinsList.add(Nw.get(i));
            w.add(binWidth);
            continuousEntropy = 0;
            discreteEntropy = 0;
        }
        Map distributionMap = processRawData();


        freq.addAll(distributionMap.values());
        yData.addAll(distributionMap.values());

        System.out.println(distributionMap);
        System.out.println(continuousEntropy);

    }
}
