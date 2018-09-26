package com.company;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MuzMachina {

    JPanel panelGlowny;
    ArrayList<JCheckBox> listaCheckBox;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame mainFrame;
    String[] nazwyInstumentow = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat","Acoustic Snare",
            "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo", "Maracas",
            "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open Hi Conga"};

    Integer[] instrumentyInt = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    LinkedHashMap<Integer, String> instrumenty = new LinkedHashMap<>();

    public static void main(String[] args) {
        new MuzMachina().tworzGUI();
    }

    private void tworzGUI() {
        mainFrame = new JFrame("MuzMachina");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout uklad = new BorderLayout();
        JPanel panelTla = new JPanel(uklad);
        panelTla.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        int counter = 0;
        for (Integer i:instrumentyInt) {
            //System.out.println(i+" || "+i.intValue()+" || "+nazwyInstumentow[counter]);
            instrumenty.put(i.intValue(),nazwyInstumentow[counter]);
            counter++;
        }
        System.out.println(instrumenty.toString());

        listaCheckBox = new ArrayList<>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(a->utworzSciezkeIOdtworz());

        JButton stop = new JButton("Stop");
        stop.addActionListener(a->sequencer.stop());

        JButton szybciej = new JButton("Szybciej");
        szybciej.addActionListener(a->{
            float wspTempa = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (wspTempa * 1.03));
        });

        JButton wolniej = new JButton("Wolniej");
        wolniej.addActionListener(a->{
            float wspTempa = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float) (wspTempa * .97));
        });
        buttonBox.add(start);
        buttonBox.add(stop);
        buttonBox.add(szybciej);
        buttonBox.add(wolniej);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        instrumenty.forEach((k,v)->nameBox.add(new Label(v)));

        panelTla.add(BorderLayout.EAST, buttonBox);
        panelTla.add(BorderLayout.WEST, nameBox);

        mainFrame.getContentPane().add(panelTla);

        GridLayout siatkaPolWyboru = new GridLayout(16,16);
        siatkaPolWyboru.setVgap(1);
        siatkaPolWyboru.setHgap(2);

        panelGlowny = new JPanel(siatkaPolWyboru);
        panelTla.add(BorderLayout.CENTER, panelGlowny);

        for(int i =0; i<256; i++){
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            listaCheckBox.add(c);
            panelGlowny.add(c);

        }

        konfigurujMidi();

        mainFrame.setBounds(50,50,300,300);
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    public void konfigurujMidi() {
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void utworzSciezkeIOdtworz(){
        int[] listaSciezki;

        sequence.deleteTrack(track);
        track = sequence.createTrack();

        for(int i=0; i<16; i++){
            listaSciezki = new int[16];
            int klucz = instrumentyInt[i];

            for (int j=0;j<16;j++){

                JCheckBox jc = listaCheckBox.get(j+(16*i));
                if (jc.isSelected()){
                    listaSciezki[j] = klucz;
                } else {
                    listaSciezki[j]=0;
                }
            }

            utworzSciezke(listaSciezki);
            track.add(tworzZdarzenie(ShortMessage.CONTROL_CHANGE,1,127,0,16));
        }
        track.add(tworzZdarzenie(192,9,1,0,15));

        try{
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void utworzSciezke(int[] listaSciezki) {
        for (int i = 0; i<16; i++){
            int klucz = listaSciezki[i];
            if(klucz!=0){
                track.add(tworzZdarzenie(144,9,klucz,100,i));
                track.add(tworzZdarzenie(128,9,klucz,100,i+1));
            }
        }
    }

    public MidiEvent tworzZdarzenie(int plc, int kanal, int jeden, int dwa, int takt) {
        MidiEvent zdarzenie = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(plc, kanal, jeden, dwa);
            zdarzenie = new MidiEvent(a, takt);
        }catch (Exception e){
            e.printStackTrace();
        }
        return zdarzenie;
    }
}
