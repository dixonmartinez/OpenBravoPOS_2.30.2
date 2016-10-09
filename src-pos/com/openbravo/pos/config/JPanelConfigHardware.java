//    Openbravo POS is a point of sales application designed for touch screens.
//    Copyright (C) 2007-2009 Openbravo, S.L.
//    http://www.openbravo.com/product/pos
//
//    This file is part of Openbravo POS.
//
//    Openbravo POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Openbravo POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.config;

import com.openbravo.data.user.DirtyManager;
import java.awt.CardLayout;
import java.awt.Component;
import com.openbravo.pos.forms.AppConfig;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.util.ReportUtils;
import com.openbravo.pos.util.StringParser;

/**
 *
 * @author adrianromero
 */
public class JPanelConfigHardware extends javax.swing.JPanel implements PanelConfig {

    private final DirtyManager dirty = new DirtyManager();

    private final ParametersConfig printer1printerparams;
    
    /** Creates new form JPanelConfigGeneral */
    public JPanelConfigHardware() {

        initComponents();

        String[] printernames = ReportUtils.getPrintNames();

        jcboMachineDisplay.addActionListener(dirty);
        jcboConnDisplay.addActionListener(dirty);
        jcboSerialDisplay.addActionListener(dirty);
        m_jtxtJPOSName.getDocument().addDocumentListener(dirty);

        jcboMachinePrinter.addActionListener(dirty);
        jcboConnPrinter.addActionListener(dirty);
        jcboSerialPrinter.addActionListener(dirty);
        m_jtxtJPOSPrinter.getDocument().addDocumentListener(dirty);
        m_jtxtJPOSDrawer.getDocument().addDocumentListener(dirty);
        
        printer1printerparams = new ParametersPrinter(printernames);
        printer1printerparams.addDirtyManager(dirty);
        m_jPrinterParams1.add(printer1printerparams.getComponent(), "printer");
        
        jcboMachineScale.addActionListener(dirty);
        jcboSerialScale.addActionListener(dirty);

        jcboMachineScanner.addActionListener(dirty);
        jcboSerialScanner.addActionListener(dirty);

        cboPrinters.addActionListener(dirty);
        
        // Printers
        cboPrinters.addItem("(Default)");
        cboPrinters.addItem("(Show dialog)");
        for (String name : printernames) {
            cboPrinters.addItem(name);
        }
    }

    @Override
    public boolean hasChanged() {
        return dirty.isDirty();
    }

    @Override
    public Component getConfigComponent() {
        return this;
    }

    @Override
    public String getPanelConfigName() {
       return AppLocal.getIntString("Label.POSHardware");
    }

    @Override
    public void loadProperties(AppConfig config) {
        StringParser p = new StringParser(config.getProperty("machine.printer"));
        String sparam = unifySerialInterface(p.nextToken(':'));
        if (null != sparam) 
            switch (sparam) {
            case "serial":
            case "file":
                jcboMachinePrinter.setSelectedItem("epson");
                jcboConnPrinter.setSelectedItem(sparam);
                jcboSerialPrinter.setSelectedItem(p.nextToken(','));
                break;
            case "javapos":
                jcboMachinePrinter.setSelectedItem(sparam);
                m_jtxtJPOSPrinter.setText(p.nextToken(','));
                m_jtxtJPOSDrawer.setText(p.nextToken(','));
                break;
            case "printer":
                jcboMachinePrinter.setSelectedItem(sparam);
                printer1printerparams.setParameters(p);
                break;
            default:
                jcboMachinePrinter.setSelectedItem(sparam);
                jcboConnPrinter.setSelectedItem(unifySerialInterface(p.nextToken(',')));
                jcboSerialPrinter.setSelectedItem(p.nextToken(','));
                break;
        }
        
        p = new StringParser(config.getProperty("machine.display"));
        sparam = unifySerialInterface(p.nextToken(':'));
        if (null != sparam) switch (sparam) {
            case "serial":
            case "file":
                jcboMachineDisplay.setSelectedItem("epson");
                jcboConnDisplay.setSelectedItem(sparam);
                jcboSerialDisplay.setSelectedItem(p.nextToken(','));
                break;
            case "javapos":
                jcboMachineDisplay.setSelectedItem(sparam);
                m_jtxtJPOSName.setText(p.nextToken(','));
                break;
            default:
                jcboMachineDisplay.setSelectedItem(sparam);
                jcboConnDisplay.setSelectedItem(unifySerialInterface(p.nextToken(',')));
                jcboSerialDisplay.setSelectedItem(p.nextToken(','));
                break;
        }

        p = new StringParser(config.getProperty("machine.scale"));
        sparam = p.nextToken(':');
        jcboMachineScale.setSelectedItem(sparam);
        if ("dialog1".equals(sparam) || "samsungesp".equals(sparam)) {
            jcboSerialScale.setSelectedItem(p.nextToken(','));
        }

        p = new StringParser(config.getProperty("machine.scanner"));
        sparam = p.nextToken(':');
        jcboMachineScanner.setSelectedItem(sparam);
        if ("scanpal2".equals(sparam)) {
            jcboSerialScanner.setSelectedItem(p.nextToken(','));
        }

        cboPrinters.setSelectedItem(config.getProperty("machine.printername"));

        dirty.setDirty(false);
    }

    @Override
    public void saveProperties(AppConfig config) {
        String sMachinePrinter = comboValue(jcboMachinePrinter.getSelectedItem());
        if (null != sMachinePrinter) switch (sMachinePrinter) {
            case "epson":
            case "tmu220":
            case "star":
            case "ithaca":
            case "surepos":
                config.setProperty("machine.printer", sMachinePrinter + ":" + comboValue(jcboConnPrinter.getSelectedItem()) + "," + comboValue(jcboSerialPrinter.getSelectedItem()));
                break;
            case "javapos":
                config.setProperty("machine.printer", sMachinePrinter + ":" + m_jtxtJPOSPrinter.getText() + "," + m_jtxtJPOSDrawer.getText());
                break;
            case "printer":
                config.setProperty("machine.printer", sMachinePrinter + ":" + printer1printerparams.getParameters());
                break;
            default:
                config.setProperty("machine.printer", sMachinePrinter);
                break;
        }
        
        String sMachineDisplay = comboValue(jcboMachineDisplay.getSelectedItem());
        if (null != sMachineDisplay) switch (sMachineDisplay) {
            case "epson":
            case "ld200":
            case "surepos":
                config.setProperty("machine.display", sMachineDisplay + ":" + comboValue(jcboConnDisplay.getSelectedItem()) + "," + comboValue(jcboSerialDisplay.getSelectedItem()));
                break;
            case "javapos":
                config.setProperty("machine.display", sMachineDisplay + ":" + m_jtxtJPOSName.getText());
                break;
            default:
                config.setProperty("machine.display", sMachineDisplay);
                break;
        }

        // La bascula
        String sMachineScale = comboValue(jcboMachineScale.getSelectedItem());
        if ("dialog1".equals(sMachineScale) || "samsungesp".equals(sMachineScale)) {
            config.setProperty("machine.scale", sMachineScale + ":" + comboValue(jcboSerialScale.getSelectedItem()));
        } else {
            config.setProperty("machine.scale", sMachineScale);
        }

        // El scanner
        String sMachineScanner = comboValue(jcboMachineScanner.getSelectedItem());
        if ("scanpal2".equals(sMachineScanner)) {
            config.setProperty("machine.scanner", sMachineScanner + ":" + comboValue(jcboSerialScanner.getSelectedItem()));
        } else {
            config.setProperty("machine.scanner", sMachineScanner);
        }

        config.setProperty("machine.printername", comboValue(cboPrinters.getSelectedItem()));

        dirty.setDirty(false);
    }

    private String unifySerialInterface(String sparam) {

        // for backward compatibility
        return ("rxtx".equals(sparam))
                ? "serial"
                : sparam;
    }

    private String comboValue(Object value) {
        return value == null ? "" : value.toString();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel13 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        jcboMachineDisplay = new javax.swing.JComboBox();
        m_jDisplayParams = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jlblConnDisplay = new javax.swing.JLabel();
        jcboConnDisplay = new javax.swing.JComboBox();
        jlblDisplayPort = new javax.swing.JLabel();
        jcboSerialDisplay = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        m_jtxtJPOSName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jcboMachinePrinter = new javax.swing.JComboBox();
        m_jPrinterParams1 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jlblConnPrinter = new javax.swing.JLabel();
        jcboConnPrinter = new javax.swing.JComboBox();
        jlblPrinterPort = new javax.swing.JLabel();
        jcboSerialPrinter = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        m_jtxtJPOSPrinter = new javax.swing.JTextField();
        m_jtxtJPOSDrawer = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jcboMachineScale = new javax.swing.JComboBox();
        jLabel26 = new javax.swing.JLabel();
        jcboMachineScanner = new javax.swing.JComboBox();
        jLabel1 = new javax.swing.JLabel();
        cboPrinters = new javax.swing.JComboBox();
        m_jScaleParams = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jlblPrinterPort4 = new javax.swing.JLabel();
        jcboSerialScale = new javax.swing.JComboBox();
        m_jScannerParams = new javax.swing.JPanel();
        jPanel24 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jlblPrinterPort5 = new javax.swing.JLabel();
        jcboSerialScanner = new javax.swing.JComboBox();

        jLabel15.setText(AppLocal.getIntString("Label.MachineDisplay")); // NOI18N

        jcboMachineDisplay.setModel(new javax.swing.DefaultComboBoxModel(mDisplay));
        jcboMachineDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachineDisplayActionPerformed(evt);
            }
        });

        m_jDisplayParams.setLayout(new java.awt.CardLayout());
        m_jDisplayParams.add(jPanel2, "empty");

        jlblConnDisplay.setText(AppLocal.getIntString("label.machinedisplayconn")); // NOI18N

        jcboConnDisplay.setModel(new javax.swing.DefaultComboBoxModel(type));

        jlblDisplayPort.setText(AppLocal.getIntString("label.machinedisplayport")); // NOI18N

        jcboSerialDisplay.setEditable(true);
        jcboSerialDisplay.setModel(new javax.swing.DefaultComboBoxModel(portConn));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlblConnDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboConnDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlblDisplayPort, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboSerialDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboConnDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblDisplayPort)
                    .addComponent(jcboSerialDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblConnDisplay))
                .addGap(59, 59, 59))
        );

        m_jDisplayParams.add(jPanel1, "comm");

        jLabel20.setText(AppLocal.getIntString("Label.Name")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jtxtJPOSName, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(280, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jtxtJPOSName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20))
                .addGap(184, 184, 184))
        );

        m_jDisplayParams.add(jPanel3, "javapos");

        jLabel7.setText(AppLocal.getIntString("Label.MachinePrinter")); // NOI18N

        jcboMachinePrinter.setModel(new javax.swing.DefaultComboBoxModel(mPrinter));
        jcboMachinePrinter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachinePrinterActionPerformed(evt);
            }
        });

        m_jPrinterParams1.setLayout(new java.awt.CardLayout());
        m_jPrinterParams1.add(jPanel5, "empty");

        jlblConnPrinter.setText(AppLocal.getIntString("label.machinedisplayconn")); // NOI18N

        jcboConnPrinter.setModel(new javax.swing.DefaultComboBoxModel(type));

        jlblPrinterPort.setText(AppLocal.getIntString("label.machineprinterport")); // NOI18N

        jcboSerialPrinter.setEditable(true);
        jcboSerialPrinter.setModel(new javax.swing.DefaultComboBoxModel(portConn));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlblConnPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboConnPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jlblPrinterPort, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboSerialPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboConnPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblPrinterPort)
                    .addComponent(jcboSerialPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblConnPrinter))
                .addGap(195, 195, 195))
        );

        m_jPrinterParams1.add(jPanel6, "comm");

        jLabel21.setText(AppLocal.getIntString("label.javapos.drawer")); // NOI18N

        jLabel24.setText(AppLocal.getIntString("label.javapos.printer")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel24, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jtxtJPOSPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(m_jtxtJPOSDrawer, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(66, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jtxtJPOSPrinter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(m_jtxtJPOSDrawer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24))
                .addGap(184, 184, 184))
        );

        m_jPrinterParams1.add(jPanel4, "javapos");

        jLabel25.setText(AppLocal.getIntString("label.scale")); // NOI18N

        jcboMachineScale.setModel(new javax.swing.DefaultComboBoxModel(mScale));
        jcboMachineScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachineScaleActionPerformed(evt);
            }
        });

        jLabel26.setText(AppLocal.getIntString("label.scanner")); // NOI18N

        jcboMachineScanner.setModel(new javax.swing.DefaultComboBoxModel(mScanner));
        jcboMachineScanner.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jcboMachineScannerActionPerformed(evt);
            }
        });

        jLabel1.setText(AppLocal.getIntString("label.reportsprinter")); // NOI18N

        m_jScaleParams.setLayout(new java.awt.CardLayout());
        m_jScaleParams.add(jPanel16, "empty");

        jlblPrinterPort4.setText(AppLocal.getIntString("label.machineprinterport")); // NOI18N

        jcboSerialScale.setEditable(true);
        jcboSerialScale.setModel(new javax.swing.DefaultComboBoxModel(portConn));

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlblPrinterPort4, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboSerialScale, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(280, Short.MAX_VALUE))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboSerialScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblPrinterPort4))
                .addGap(135, 135, 135))
        );

        m_jScaleParams.add(jPanel17, "comm");

        m_jScannerParams.setLayout(new java.awt.CardLayout());
        m_jScannerParams.add(jPanel24, "empty");

        jlblPrinterPort5.setText(AppLocal.getIntString("label.machineprinterport")); // NOI18N

        jcboSerialScanner.setEditable(true);
        jcboSerialScanner.setModel(new javax.swing.DefaultComboBoxModel(portConn));

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jlblPrinterPort5, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jcboSerialScanner, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(280, Short.MAX_VALUE))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jcboSerialScanner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jlblPrinterPort5))
                .addGap(235, 235, 235))
        );

        m_jScannerParams.add(jPanel19, "comm");

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcboMachineDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jDisplayParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcboMachinePrinter, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jPrinterParams1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jcboMachineScale, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jScaleParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addComponent(jLabel26, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jcboMachineScanner, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel13Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cboPrinters, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(m_jScannerParams, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel15)
                        .addComponent(jcboMachineDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jDisplayParams, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel7)
                        .addComponent(jcboMachinePrinter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jPrinterParams1, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel25)
                        .addComponent(jcboMachineScale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(m_jScaleParams, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(jcboMachineScanner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(cboPrinters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(m_jScannerParams, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jcboMachineScannerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachineScannerActionPerformed
        CardLayout cl = (CardLayout) (m_jScannerParams.getLayout());

        if ("scanpal2".equals(jcboMachineScanner.getSelectedItem())) {
            cl.show(m_jScannerParams, "comm");
        } else {
            cl.show(m_jScannerParams, "empty");
        }
    }//GEN-LAST:event_jcboMachineScannerActionPerformed

    private void jcboMachineScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachineScaleActionPerformed
        CardLayout cl = (CardLayout) (m_jScaleParams.getLayout());

        if ("dialog1".equals(jcboMachineScale.getSelectedItem()) || "samsungesp".equals(jcboMachineScale.getSelectedItem())) {
            cl.show(m_jScaleParams, "comm");
        } else {
            cl.show(m_jScaleParams, "empty");
        }
    }//GEN-LAST:event_jcboMachineScaleActionPerformed

    private void jcboMachinePrinterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachinePrinterActionPerformed
        CardLayout cl = (CardLayout) (m_jPrinterParams1.getLayout());

        if ("epson".equals(jcboMachinePrinter.getSelectedItem()) || "tmu220".equals(jcboMachinePrinter.getSelectedItem()) || "star".equals(jcboMachinePrinter.getSelectedItem()) || "ithaca".equals(jcboMachinePrinter.getSelectedItem()) || "surepos".equals(jcboMachinePrinter.getSelectedItem())) {
            cl.show(m_jPrinterParams1, "comm");
        } else if ("javapos".equals(jcboMachinePrinter.getSelectedItem())) {
            cl.show(m_jPrinterParams1, "javapos");
        } else if ("printer".equals(jcboMachinePrinter.getSelectedItem())) {
            cl.show(m_jPrinterParams1, "printer");
        } else {
            cl.show(m_jPrinterParams1, "empty");
        }
    }//GEN-LAST:event_jcboMachinePrinterActionPerformed

    private void jcboMachineDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcboMachineDisplayActionPerformed
        CardLayout cl = (CardLayout) (m_jDisplayParams.getLayout());

        if ("epson".equals(jcboMachineDisplay.getSelectedItem()) || "ld200".equals(jcboMachineDisplay.getSelectedItem()) || "surepos".equals(jcboMachineDisplay.getSelectedItem())) {
            cl.show(m_jDisplayParams, "comm");
        } else if ("javapos".equals(jcboMachineDisplay.getSelectedItem())) {
            cl.show(m_jDisplayParams, "javapos");
        } else {
            cl.show(m_jDisplayParams, "empty");
        }
    }//GEN-LAST:event_jcboMachineDisplayActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cboPrinters;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JComboBox jcboConnDisplay;
    private javax.swing.JComboBox jcboConnPrinter;
    private javax.swing.JComboBox jcboMachineDisplay;
    private javax.swing.JComboBox jcboMachinePrinter;
    private javax.swing.JComboBox jcboMachineScale;
    private javax.swing.JComboBox jcboMachineScanner;
    private javax.swing.JComboBox jcboSerialDisplay;
    private javax.swing.JComboBox jcboSerialPrinter;
    private javax.swing.JComboBox jcboSerialScale;
    private javax.swing.JComboBox jcboSerialScanner;
    private javax.swing.JLabel jlblConnDisplay;
    private javax.swing.JLabel jlblConnPrinter;
    private javax.swing.JLabel jlblDisplayPort;
    private javax.swing.JLabel jlblPrinterPort;
    private javax.swing.JLabel jlblPrinterPort4;
    private javax.swing.JLabel jlblPrinterPort5;
    private javax.swing.JPanel m_jDisplayParams;
    private javax.swing.JPanel m_jPrinterParams1;
    private javax.swing.JPanel m_jScaleParams;
    private javax.swing.JPanel m_jScannerParams;
    private javax.swing.JTextField m_jtxtJPOSDrawer;
    private javax.swing.JTextField m_jtxtJPOSName;
    private javax.swing.JTextField m_jtxtJPOSPrinter;
    // End of variables declaration//GEN-END:variables


    private static final String NOT_DEFINED =  "Not defined";
    private static final String SCREEN =  "screen";
    private static final String WINDOW =  "window";
    private static final String PRINTER =  "printer";
    
    
    private final String mPrinter [] = {
        NOT_DEFINED,
        SCREEN,
        PRINTER,
        "epson",
        "tmu220",
        "star",
        "ithaca",
        "surepos",
        "plain",
        "javapos"
    };
    
    private final String mDisplay [] = {
        NOT_DEFINED,
        SCREEN,
        WINDOW,
        "javapos",
        "epson",
        PRINTER,
        "ld200",
        "surepos"
    };
    
    private final String mScale [] = {
        NOT_DEFINED,
        SCREEN,
        "dialog1",
        "samsungesp"
    };

    private final String mScanner [] = {
        NOT_DEFINED,
        "scanpal2",
    };
    
    private final String type [] = {
        "serial",
        "file"
    };
    
    private final String portConn [] = {
        "COM1",
        "COM2",
        "COM3",
        "COM4",
        "LPT1",
        "/dev/ttyS0",
        "/dev/ttyS1",
        "/dev/ttyS2",
        "/dev/ttyS3",
        "/dev/ttyS4"        
    };
        
}
