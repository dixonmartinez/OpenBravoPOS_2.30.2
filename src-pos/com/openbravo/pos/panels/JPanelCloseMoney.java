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
//      Modificado por Ghintech
//    You should have received a copy of the GNU General Public License
//    along with Openbravo POS.  If not, see <http://www.gnu.org/licenses/>.
package com.openbravo.pos.panels;
import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;


//Clase para editar los montos
public class JPanelCloseMoney  extends javax.swing.JPanel implements JPanelView, BeanFactoryApp{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SentenceList m_sentcat;
    private ComboBoxValModel m_PeopleModel;
    private DataLogicSystem dlSystem;
    private DataLogicSales dlSales;
    private ArrayList<JTextField> vectorTxtCash;
    private ArrayList<JTextField> vectorTxtCashDollar;
    private ArrayList vectorTxtCT;
    private ArrayList vectorTxtCard;
    private JLabel lblTotalCash;
    private JLabel lblTotalCT;
    private JLabel lblTotalCards;
    private JLabel lblTotalPay;
    private JLabel lblTotalCheque;
    private JLabel lblPeople;
    private JTextField txtCheque;
    private boolean isFirstTxt ;// Determina si es el primer jtextfield que se crear para colocar el focus
    private AppView m_App;
    private PaymentsModel m_PaymentsToClose;
    private BigDecimal totalCash;
    private BigDecimal totalCT;
    private BigDecimal totalCards;
    private BigDecimal totalTransfer;
    private BigDecimal totalCheque;
    private BigDecimal totalPay;
    private JButton btnCloseCash;
    private JButton btnClearTxt;
    private JTextField txtFirstTextField;
    private JLabel lblInfo;
    private JTextField txtTransfer;
    private JTextField txtCard;
    private JTable tableCT;
    private JTable tableCard;
    private JTable tableCheque;
    private JComboBox JCBNombreBanco;
    private JComboBox JCBtipoTarjeta;
    private JComboBox JCBtipoPersona; 
    private JComboBox JCBNombreBancoCheque;
    private JComboBox JCBTipoTrans;
    private JComboBox JCBPeople;
    private JComboBox TipoTicket;
    private JTextField txtMontoCT;
    private JTextField txtcantidadCT;
    private DefaultTableModel dtmCT;
    private DefaultTableModel dtmCard;
    private DefaultTableModel dtmCheque;
    private TaxCategoryInfo m_People;
 
       
    @Override
    public void init(AppView app) throws BeanFactoryException {
          dlSystem = (DataLogicSystem)app.getBean("com.openbravo.pos.forms.DataLogicSystem");
          dlSales = (DataLogicSales)app.getBean("com.openbravo.pos.forms.DataLogicSales");
          vectorTxtCash = new ArrayList();
          vectorTxtCashDollar = new ArrayList();
          vectorTxtCT = new ArrayList();
          vectorTxtCard = new ArrayList();
          isFirstTxt =true;
          m_App = app;
          setLayout( new MigLayout());
          setBorder(new EmptyBorder(5,160,5,5));
          
          add(panelCloseCash(),"width 150, dock south, growx, wrap");
          add(panelCash() ,"dock west,cell 0 0");
          add(panelCashDollar() ,"dock west,cell 0 0");
          add(panelCard(),"width 150, dock west, cell 0 1");
          add(panelCheque(),"width 150, dock west, cell 0 2");
          add(panelCT(),"width 210, wrap");
          add(panelNcr(),"width 250");
          //add(panelTransfer(),"width 250");
          addKeyButton();
    }
    
    // Panel efectivo
    public JPanel panelCash(){   
        JLabel lblImage = new JLabel();
        lblImage.setText("Ingresos en Efectivo");
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cash.png")));
        JPanel pnlCash = new JPanel();
	pnlCash.setLayout(new MigLayout());
        pnlCash.setBorder(BorderFactory.createEtchedBorder());
        pnlCash.add(lblImage, "span, wrap 15");
        pnlCash.add(new JLabel("Monto"),"align center");
        pnlCash.add(new JLabel("Cant."),"wrap,align center");
        pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        createPanelFromScript(pnlCash,"payment.cash",vectorTxtCash);
  
        // Labels para mostrar el total de Efectivo
        JLabel lblTotal = new JLabel();
        lblTotal.setText("Total");
        lblTotalCash = new JLabel("0");
        pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        pnlCash.add(lblTotal,"align center");
        pnlCash.add(lblTotalCash,"width 50,align center");
        return pnlCash;
    }
    
    // Panel efectivo
    public JPanel panelCashDollar(){   
        JLabel lblImage = new JLabel();
        lblImage.setText("Ingresos en Dolares");
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cash.png")));
        JPanel pnlCash = new JPanel();
        pnlCash.setLayout(new MigLayout());
        pnlCash.setBorder(BorderFactory.createEtchedBorder());
        pnlCash.add(lblImage, "span, wrap 15");
        pnlCash.add(new JLabel("Monto"),"align center");
        pnlCash.add(new JLabel("Cant."),"wrap,align center");
        pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        createPanelFromScript(pnlCash,"payment.dollar.cash",vectorTxtCashDollar);
  
        // Labels para mostrar el total de Efectivo
        JLabel lblTotal = new JLabel();
        lblTotal.setText("Total");
        lblTotalCash = new JLabel("0");
        pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        pnlCash.add(lblTotal,"align center");
        pnlCash.add(lblTotalCash,"width 50,align center");
        return pnlCash;
    }
    //Panel de los cesta ticket
    public JPanel panelCT(){
        JLabel lblImage = new JLabel();
        lblTotalCT = new JLabel("0");
        ArrayList<String> aCT;
        aCT = new ArrayList<String>();
        String TicketType = dlSystem.getResourceAsXML("tipo.ticket.alim");
        String arrayticket[] = TicketType.split(",");
        aCT.addAll(Arrays.asList(arrayticket));
        Object[][] datosCT = null;
        String[] columnNamesCT = {"Tipo","Cantidad","Monto"};
        dtmCT= new DefaultTableModel(datosCT,columnNamesCT);
        tableCT = new JTable(dtmCT);
        JScrollPane scrollPaneCT;
        scrollPaneCT = new JScrollPane(tableCT);
        txtcantidadCT = new JTextField(); 
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ticketlogo.png")));
        lblImage.setText("Ingresos Bono Alimentacion");
        tableCT.getColumnModel().getColumn(0).setMaxWidth(100);
        tableCT.getColumnModel().getColumn(1).setMaxWidth(60);
        tableCT.getColumnModel().getColumn(2).setMaxWidth(60);
        txtMontoCT = new JTextField();
        TipoTicket = new JComboBox(aCT.toArray());
        JPanel pnlCT = new JPanel();
        JLabel lblTotal = new JLabel();
        lblTotal.setText("Total:");
        lblTotalCT = new JLabel("0.000");
        addKeyListenertoTxt(txtcantidadCT,true);//Agregando acciones al campo texto
        addKeyListenertoTxt(txtMontoCT,true);//Agregando acciones al campo texto
        ListenerTipoticket();
        txtMontoCT.setEnabled(false);
        TipoTicket.setEnabled(false);
        tableCT.setEnabled(false);
	pnlCT.setLayout(new MigLayout());
        pnlCT.setBorder(BorderFactory.createEtchedBorder());
        pnlCT.add(lblImage, "span, wrap 15");
        pnlCT.add(new JLabel("Monto"),"cell 0 3 ,align center");// Etiqueta
        pnlCT.add(txtcantidadCT,"cell 1 3,width 105,align right");//TextField Cantidad
        pnlCT.add(new JLabel("Cant."),"cell 0 4 ,align center , wrap,align center");//TextField Cantidad
        pnlCT.add(txtMontoCT,"cell 1 4,width 105,align right,wrap");//TextField Monto
        pnlCT.add(TipoTicket,"gapleft rel, growx,wrap,span");//Combobox tipo de ticket
        pnlCT.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        pnlCT.add(scrollPaneCT, "cell 0 6,gapleft rel, growx,wrap,span");
        pnlCT.add(lblTotal,"align center");
        pnlCT.add(lblTotalCT,"width 50,align center,wrap");
        return pnlCT;
    }
        //Panel Para el Pago con tarjeta
    public JPanel panelCard(){
        JLabel lblImage = new JLabel();
        txtCard = new JTextField(); 
        JPanel pnlCard = new JPanel();
        String cardType = dlSystem.getResourceAsXML("pointofsale.name");
        String arraycard[] = cardType.split(",");
        String cardType2 = dlSystem.getResourceAsXML("card.type");
        String arraycard2[] = cardType2.split(",");
        ArrayList<String> aCard;
        aCard = new ArrayList<String>();
        ArrayList<String> bCard;
        bCard = new ArrayList<String>();
        aCard.addAll(Arrays.asList(arraycard));
        bCard.addAll(Arrays.asList(arraycard2));
        JCBNombreBanco = new JComboBox(aCard.toArray());        
        JCBtipoTarjeta = new JComboBox(bCard.toArray());
        JCBNombreBanco.setEnabled(false);
        JCBtipoTarjeta.setEnabled(false);
        Object[][] datosCard = null;
        String[] columnNamesCard = {"Punto de V.","Tipo","Monto"};
        dtmCard= new DefaultTableModel(datosCard,columnNamesCard);
        tableCard = new JTable(dtmCard); 
        tableCard.setEnabled(false);
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/vcard.png"))); 
        lblImage.setText("Ingresos en Tarjetas");
        JLabel lblCardCred = new JLabel();
        lblCardCred.setText("");       
        pnlCard.setLayout(new MigLayout());
        pnlCard.setBorder(BorderFactory.createEtchedBorder());
        pnlCard.add(lblImage,"span,wrap 15");
        tableCard.getColumnModel().getColumn(0).setMaxWidth(100);
        tableCard.getColumnModel().getColumn(1).setMaxWidth(60);
        tableCard.getColumnModel().getColumn(2).setMaxWidth(60);
        JScrollPane scrollPaneCard;
        scrollPaneCard = new JScrollPane(tableCard);
        JLabel jlabelBanco = new JLabel("Punto de V.: ");  
        JLabel jlabelTipoT = new JLabel("Tipo Tarjeta: ");  
        JLabel jlabelMonto = new JLabel("Monto: ");
        JLabel lblTotal = new JLabel();
        addKeyListenertoTxt(txtCard,true);//Agregando acciones al campo texto
        lblTotal.setText("Total:");
        lblTotalCards = new JLabel("0");
        ListenerCard();
        pnlCard.add(jlabelMonto,"cell 0 2");
        pnlCard.add(txtCard,"cell 1 2,width 105,align right,width 200");
        pnlCard.add(jlabelBanco,"cell 0 3");
        pnlCard.add(JCBNombreBanco,"width 200,cell 1 3");
        pnlCard.add(jlabelTipoT,"cell 0 4");
        pnlCard.add(JCBtipoTarjeta,"cell 1 4,width 200");
        pnlCard.add(scrollPaneCard,"cell 0 6,gapleft rel, growx,wrap,span");
        pnlCard.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        pnlCard.add(lblTotal,"align center");
        pnlCard.add(lblTotalCards,"width 50,align center");
        return pnlCard;
    }
       
    public JPanel panelCheque(){
        JLabel lblImage = new JLabel();
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/desktop.png"))); 
        lblImage.setText("Cheques/Transf.");
        JLabel lblTotal2 = new JLabel();
        String cardType = dlSystem.getResourceAsXML("pointofsale.name");
        String arraycard[] = cardType.split(",");
        String cardType2 = dlSystem.getResourceAsXML("tipo.persona");
        String arraycard2[] = cardType2.split(",");
        ArrayList<String> aCheque;
        aCheque = new ArrayList<String>();
        ArrayList<String> bCheque;
        bCheque = new ArrayList<String>();
        aCheque.addAll(Arrays.asList(arraycard));
        bCheque.addAll(Arrays.asList(arraycard2));
        String[] transType = { "Transferencia", "Cheque" };
        JCBTipoTrans = new JComboBox(transType);        
        JCBNombreBancoCheque = new JComboBox(aCheque.toArray());        
        JCBtipoPersona = new JComboBox(bCheque.toArray());
        JPanel pnlCheque = new JPanel();                    
        pnlCheque.setLayout(new MigLayout());
        pnlCheque.setBorder(BorderFactory.createEtchedBorder());
        Object[][] datos2 = null;
        String[] columnNames2 = {"Banco","Tipo","Monto"};
        dtmCheque= new DefaultTableModel(datos2,columnNames2);
        tableCheque= new JTable(dtmCheque); 
        tableCheque.setEnabled(false);
        JCBTipoTrans.setEnabled(false);
        JCBNombreBancoCheque.setEnabled(false);
        JCBNombreBancoCheque.setEnabled(false);
        JCBtipoPersona.setEnabled(false);
        tableCheque.getColumnModel().getColumn(0).setMaxWidth(100);
        tableCheque.getColumnModel().getColumn(1).setMaxWidth(60);
        tableCheque.getColumnModel().getColumn(2).setMaxWidth(60);
        JScrollPane scrollPaneCheque;
        scrollPaneCheque = new JScrollPane(tableCheque);
        JLabel jlabelTipoTrans = new JLabel("Tipo: ");  
        JLabel jlabelBanco = new JLabel("Banco: ");  
        JLabel jlabelMonto = new JLabel("Monto:");  
        JLabel jlabelPersona = new JLabel("Persona: ");
        lblTotal2.setText("Total:");
        lblTotalCheque = new JLabel("0");
        txtCheque = new JTextField();
        addKeyListenertoTxt(txtCheque,true);
        ListenerCheque();
        pnlCheque.add(lblImage,"span,wrap 15");
        pnlCheque.add(jlabelMonto,"cell 0 2");
        pnlCheque.add(txtCheque,"cell 1 2,width 120,align right");
        pnlCheque.add(jlabelBanco,"cell 0 3");
        pnlCheque.add(JCBNombreBancoCheque,"cell 1 3");
        pnlCheque.add(jlabelBanco,"cell 0 4");
        pnlCheque.add(JCBNombreBancoCheque,"cell 1 4");
        pnlCheque.add(jlabelPersona,"cell 0 5");
        pnlCheque.add(JCBtipoPersona,"width 118, cell 1 5");
        pnlCheque.add(scrollPaneCheque,"cell 0 6,gapleft rel, growx,wrap,span");
        pnlCheque.add(new JSeparator(), "gapleft rel, growx,wrap,span");
        pnlCheque.add(lblTotal2);
        pnlCheque.add(lblTotalCheque);
        return  pnlCheque; 
    }
    
    //Diseñando el Panel de NCR
    public JPanel panelNcr(){
        JLabel lblImage = new JLabel();
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ark2.png"))); 
        lblImage.setText("Notas de Crédito");
        JLabel lblCardCred = new JLabel();
        lblCardCred.setText("");
        JPanel pnlTransfer = new JPanel();
        pnlTransfer.setLayout(new MigLayout());
        pnlTransfer.setBorder(BorderFactory.createEtchedBorder());
        pnlTransfer.add(lblImage,"span,wrap 15");
        txtTransfer = new JTextField();
        addKeyListenertoTxt(txtTransfer,true);
        JLabel jlabel = new JLabel("Monto:");  
        pnlTransfer.add(jlabel);
        pnlTransfer.add(txtTransfer,"wrap 1, width 80,align center,wrap"); 
        return  pnlTransfer; 
    }
    //Diseñando el Panel de Transferencia
    public JPanel panelTransfer(){
        JLabel lblImage = new JLabel();
        lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/ark2.png"))); 
        lblImage.setText("Transferencias");
        JLabel lblCardCred = new JLabel();
        lblCardCred.setText("");
        JPanel pnlTransfer = new JPanel();
        pnlTransfer.setLayout(new MigLayout());
        pnlTransfer.setBorder(BorderFactory.createEtchedBorder());
        pnlTransfer.add(lblImage,"span,wrap 15");
        txtTransfer = new JTextField();
        addKeyListenertoTxt(txtTransfer,true);
        JLabel jlabel = new JLabel("Monto:");  
        pnlTransfer.add(jlabel);
        pnlTransfer.add(txtTransfer,"wrap 1, width 80,align center,wrap"); 
        return  pnlTransfer; 
    }
    private JPanel panelCloseCash(){
                
        btnCloseCash = new JButton();
        btnCloseCash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/close_cash.png")));
        btnCloseCash.setText("Cerrar caja (F2)");
        btnCloseCash.setName("btnCloseCash");      
        btnCloseCash.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnCloseCashActionPerformed(evt);
           }
        });
        btnCloseCash.setEnabled(false);
        
        btnClearTxt = new JButton();
        btnClearTxt.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/clean.png"))); 
        btnClearTxt.setText("Limpiar campos (F8)");
        btnClearTxt.setName("btnClearTxt");
        
        btnClearTxt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                btnClearTxtActionPerformed(evt);
           }
        });
        btnClearTxt.setEnabled(false);
       
        m_sentcat = dlSales.getUserList();
        m_PeopleModel = new ComboBoxValModel(); 
        
        List catlist=null;
        try {
            catlist = m_sentcat.list();
        } catch (BasicException ex) {
            ex.getMessage();
        }
        catlist.add(0, null);
        m_PeopleModel = new ComboBoxValModel(catlist);
        
        lblPeople = new JLabel("Usuario:");
        JCBPeople = new JComboBox();
        JCBPeople.setModel(m_PeopleModel);
        addKeyListenertoCombo(JCBPeople);
        //JCBPeople.setPreferredSize(new Dimension(200, 100));        
        
        lblTotalPay = new JLabel("0 BsF.");
        lblInfo = new JLabel();
        lblInfo.setForeground(Color.RED);
        
        JLabel lblTotal = new JLabel("Total en caja");
        JPanel pnlCloseCash = new JPanel();
        pnlCloseCash.setLayout(new MigLayout());
        pnlCloseCash.setBorder(BorderFactory.createEtchedBorder());
        pnlCloseCash.add(btnClearTxt,"cell 0 0");
        pnlCloseCash.add(btnCloseCash,"cell 1 0,wrap");

        pnlCloseCash.add(lblPeople,"cell 1 0,wrap");
        pnlCloseCash.add(JCBPeople,"cell 1 0,wrap");        
        
        pnlCloseCash.add(lblTotal,"align center");
        pnlCloseCash.add(lblTotalPay,"wrap");
        pnlCloseCash.add(lblInfo,"span 2,align center");
        return pnlCloseCash;
    }
    
    private void clearTextFields(){
          for (int i = 0; i < vectorTxtCard.size(); i++) {
                JTextField txtAmountCard = (JTextField)vectorTxtCard.get(i);
                txtAmountCard.setText("");
            }
            for (int i = 0; i < vectorTxtCT.size(); i++) {
                JTextField txtAmountCT = (JTextField)vectorTxtCT.get(i);
                txtAmountCT.setText("");
            }
            for (int i = 0; i < vectorTxtCash.size(); i++) {
                JTextField txtAmountCash = (JTextField)vectorTxtCash.get(i);
                txtAmountCash.setText("");
            }
            txtCheque.setText("");
            txtCard.setText("");
            JCBNombreBanco.setSelectedIndex(0);
            JCBNombreBanco.setEnabled(false);
            JCBtipoTarjeta.setSelectedIndex(0);
            JCBtipoTarjeta.setEnabled(false);
            txtTransfer.setText("");
            txtMontoCT.setText("");
            txtMontoCT.setEnabled(false);
            TipoTicket.setSelectedIndex(0);
            TipoTicket.setEnabled(false);
            txtcantidadCT.setText("");
            lblTotalCash.setText("0.000");
            lblTotalPay.setText("0.000 BsF.");
            lblInfo.setText("");
            dtmCT.setRowCount(0);
            dtmCard.setRowCount(0);
            dtmCheque.setRowCount(0);
            lblTotalCT.setText("0.000");
            lblTotalCheque.setText("0.000");
            lblTotalCards.setText("0.000");
            JCBPeople.setSelectedIndex(0);
            buttonsActions(BigDecimal.ZERO);
    }
    
     private void btnClearTxtActionPerformed(ActionEvent evt) {
        
        int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.clearTxtCloseCash"), AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
        // Limpia los campos de los paneles de pagos
            clearTextFields();      
        }
     }
      private void cargarAddGrillaCT(){
           Object[] newRow={TipoTicket.getSelectedItem(),txtcantidadCT.getText(),txtMontoCT.getText()};
            dtmCT.addRow(newRow);
     }
      private void cargarAddGrillaCard(){
           Object[] newRow={JCBNombreBanco.getSelectedItem(),JCBtipoTarjeta.getSelectedItem(),txtCard.getText()};
            dtmCard.addRow(newRow);
     }
      private void cargarAddGrillacheque(){
         Object[] newRow={JCBNombreBancoCheque.getSelectedItem(),JCBtipoPersona.getSelectedItem(),txtCheque.getText()};
            dtmCheque.addRow(newRow);
     }
    private void btnCloseCashActionPerformed(ActionEvent evt) {
        btnCalculateActionPerformed(null);
        callCloseCash();
    }
    
    public void addKeyButton(){
        KeyStroke F8 = KeyStroke.getKeyStroke( KeyEvent.VK_F8,0 );
        KeyStroke F2 = KeyStroke.getKeyStroke( KeyEvent.VK_F2,0 );

        btnClearTxt.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(F8, "actionClean");
        btnClearTxt.getActionMap().put("actionClean", Accion_F8()); 

        btnCloseCash.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(F2, "actionCloseCash");
        btnCloseCash.getActionMap().put("actionCloseCash", Accion_F2()); 
      
    }
    private void btnCalculateActionPerformed(ActionEvent evt) {
         calculateAmountCash();
         calculateAmountCT();
         calculateAmountCard();
         calculateAmountCheque();
         totalCash = new BigDecimal(lblTotalCash.getText());
         totalCT = new BigDecimal(lblTotalCT.getText());
         totalTransfer = txtTransfer.getText().trim().equals("")?BigDecimal.ZERO:new BigDecimal(Double.parseDouble(txtTransfer.getText()));
         totalPay  = totalCT.add(totalCards.setScale(3,RoundingMode.HALF_UP))
                            .add(totalCash)
                            .add(totalCheque.setScale(3, RoundingMode.HALF_UP))
                            .add(totalTransfer.setScale(3,RoundingMode.HALF_UP));
         lblTotalPay.setText(totalPay.setScale(3, RoundingMode.HALF_UP)+" BsF.");
         buttonsActions(totalPay.setScale(3, RoundingMode.HALF_UP));     
    }
    
    //Valida si hay ventas o el monto es mayor a cero para poder hacer el cierre de caja
    private void buttonsActions(BigDecimal TotalPay){
        try {
            boolean isDiffZero = TotalPay.compareTo(BigDecimal.ZERO)==0?false:true;
            String sUser = "";
                   
            isDiffZero = isDiffZero && JCBPeople.getSelectedIndex() > 0;
                        
            if(JCBPeople.getSelectedIndex() > 0) {
                m_People = (TaxCategoryInfo)JCBPeople.getSelectedItem();
                sUser = m_People.getID().toString();

            m_PaymentsToClose = PaymentsModel.loadInstance(m_App);
            boolean isPayReg = false;
            //boolean chequecero = txtCheque.
            // Revisamos si hay ventas para realizar el cuadre de caja
            if ((m_PaymentsToClose.getPayments() != 0 || m_PaymentsToClose.getSales() != 0 )){
                lblInfo.setText("");
                isPayReg = true;
            }else{
                lblInfo.setText(AppLocal.getIntString("label.notsales"));
            }
            
            btnClearTxt.setEnabled(isDiffZero);
            btnCloseCash.setEnabled(isDiffZero && isPayReg);
            }
        } catch (BasicException ex) {
            Logger.getLogger(JPanelCloseMoney.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void calculateAmountCash() {
        BigDecimal cant;
        BigDecimal amount = BigDecimal.ZERO;
        for (int i = 0; i < vectorTxtCash.size(); i++) {
            JTextField txtAmountCash = (JTextField)vectorTxtCash.get(i);
            cant = txtAmountCash.getText().trim().equals("")?
                    BigDecimal.ZERO:
                        new BigDecimal(Double.parseDouble(txtAmountCash.getText())); 
            if(cant!=BigDecimal.ZERO){                     
                amount = amount.add(txtAmountCash.getName().trim().equals("1000000.0")?
                        new BigDecimal(Double.parseDouble(txtAmountCash.getText())):
                        cant.multiply(new BigDecimal(txtAmountCash.getName())));
            }
        }
        lblTotalCash.setText(amount.setScale(3, RoundingMode.HALF_UP)+"");        
    }
    private void calculateAmountCT() {
        BigDecimal cant;
        BigDecimal amount2 = BigDecimal.ZERO;
        cant = txtMontoCT.getText().trim().equals("")?BigDecimal.ZERO:new BigDecimal(Double.parseDouble(txtMontoCT.getText())); 
             if(cant!=BigDecimal.ZERO){
                amount2 =amount2.add(txtcantidadCT.getText().trim().equals("1000000.0")?
                        new BigDecimal(Double.parseDouble(txtcantidadCT.getText())):  
                        amount2.add(cant.multiply(new BigDecimal(txtcantidadCT.getText()))));
                        
             }
        amount2=amount2.add(new BigDecimal(lblTotalCT.getText()));
        lblTotalCT.setText(amount2.setScale(3, RoundingMode.HALF_UP)+"");              
    }
    private void calculateAmountCard(){
        BigDecimal cant2 = txtCard.getText().trim().equals("")?BigDecimal.ZERO:new BigDecimal(Double.parseDouble(txtCard.getText())); 
        cant2 =cant2.add(new BigDecimal(lblTotalCards.getText()));
        lblTotalCards.setText(cant2.setScale(3, RoundingMode.HALF_UP)+"");
        totalCards = lblTotalCards.getText().trim().equals("")?BigDecimal.ZERO:new BigDecimal(Double.parseDouble(lblTotalCards.getText()));
    }
    private void calculateAmountCheque() {
        BigDecimal cant3 = txtCheque.getText().trim().equals("")?BigDecimal.ZERO:new BigDecimal(Double.parseDouble(txtCheque.getText())); 
        cant3 =cant3.add(new BigDecimal(lblTotalCheque.getText()));
        lblTotalCheque.setText(cant3.setScale(3, RoundingMode.HALF_UP)+"");
        totalCheque = lblTotalCheque.getText().trim().equals("")?BigDecimal.ZERO:new BigDecimal(Double.parseDouble(lblTotalCheque.getText()));
    }
    // Busca el script de configuracion para los pagos efectivo,CT, y tarjeta  
    public void createPanelFromScript(JPanel panel, String resourcePay,ArrayList vectorAmount){
        
        String scriptPay =  dlSystem.getResourceAsXML(resourcePay);
        if (scriptPay != null) {
            try {
                ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
                script.put("payment", new ScriptPayments(panel,vectorAmount));    
                script.eval(scriptPay);
            } catch (ScriptException e) {
                MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"), e);
                msg.show(this);
            }
        }
    }

    private void addKeyListenertoCombo(final JComboBox comboBox) {
        comboBox.addActionListener (new ActionListener () {
            public void actionPerformed(ActionEvent e) {
                if(comboBox.getSelectedIndex() > 0)
                    btnCalculateActionPerformed(null);
            }
        });
    }
    
    // Añade acciones a los JTextField creados
    private void addKeyListenertoTxt(final JTextField textField,final boolean isDecimal) {
           textField.addKeyListener(new KeyListener() {

                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    if(isDecimal){
                        if((c<'0'|| c>'9') && c!=',' && c!=' ')
                            e.consume();
                    }else{
                         if((c<'0'|| c>'9'))
                        e.consume();   
                     }
                }
                @Override
                public void keyPressed(KeyEvent e) {}

                @Override
                public void keyReleased(KeyEvent ke) {
                    if   (textField.equals(txtcantidadCT)){
                        txtMontoCT.setEnabled(true);
                    }else if(textField.equals(txtMontoCT)){    
                        TipoTicket.setEnabled(true);
                    }else if(textField.equals(txtCard)){
                        JCBNombreBanco.setEnabled(true);                    
                    }else if(textField.equals(txtCheque)){
                        JCBNombreBancoCheque.setEnabled(true);                    
                    }else
                        btnCalculateActionPerformed(null);           
                }
            });
    }
    
    public void callCloseCash(){
          int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannaclosecash"), AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(JCBPeople.getSelectedIndex() > 0)
                m_People = (TaxCategoryInfo)JCBPeople.getSelectedItem();

          if (res == JOptionPane.YES_OPTION){
             //exportToERP();
             m_App.getAppUserView().showTask("com.openbravo.pos.panels.JPanelCloseMoneyFinal");
             clearTextFields();

          }
    }

    private Action Accion_F8() {
      return new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) { 
         btnClearTxtActionPerformed(null);
        }
      };  
    }

    private Action Accion_F2() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { 
                btnCloseCashActionPerformed(null);
             }
         }; 
    }
    private Action Accion_F9() {
            return new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) { 
                    System.out.println("Presionando F)9");
                 }
             }; 
        }

 private void ListenerTipoticket() {
        TipoTicket.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                JComboBox w = (JComboBox)evt.getSource();
                if ((w.getSelectedItem().equals("Tarjeta")) || (w.getSelectedItem().equals("Ticket"))){
                   cargarAddGrillaCT();
                   btnCalculateActionPerformed(null);
                   txtcantidadCT.requestFocus();
                }
           }
        });
        TipoTicket.addFocusListener(new FocusListener(){
                        @Override
            public void focusGained(FocusEvent fe) {
            }
                        @Override
            public void focusLost(FocusEvent fe) {
              BigDecimal aux;
              aux =new BigDecimal(Double.parseDouble(lblTotalCT.getText())); 
              TipoTicket.setSelectedIndex(0);
              txtMontoCT.setText("");
              txtMontoCT.setEnabled(false);
              TipoTicket.setEnabled(false);
              txtcantidadCT.setText("");
              lblTotalCT.setText(aux.setScale(3, RoundingMode.HALF_UP)+"");
            }
			
        });
        tableCT.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                            int row = tableCT.rowAtPoint(evt.getPoint());
                            int col = tableCT.columnAtPoint(evt.getPoint());
                            BigDecimal aux = BigDecimal.ZERO,producto = BigDecimal.ZERO;
                            if (row >= 0 && col >= 0){  
                                int res;
                                res = JOptionPane.showConfirmDialog(tableCT,"Desea Eliminar el Registro: "+(row+1), "Eliminar Registro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                        if (res == JOptionPane.YES_OPTION) {
                                           Object[] a = {dtmCT.getValueAt(row, 1),dtmCT.getValueAt(row, 2)};
                                           aux=aux.add(new BigDecimal(lblTotalCT.getText()));
                                           producto=producto.add(new BigDecimal(a[0].toString()));
                                           producto=producto.multiply(new BigDecimal(a[1].toString()));
                                           lblTotalCT.setText(aux.subtract(producto).setScale(3, RoundingMode.HALF_UP)+""); 
                                           dtmCT.removeRow(row);
                                           btnCalculateActionPerformed(null);
                                        }
                               }
                            }
                });
 }

    private void ListenerCard() {
        JCBNombreBanco.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCBtipoTarjeta.setEnabled(true);
            }
        });
        JCBtipoTarjeta.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                    if (!txtCard.getText().equals("")){
                            cargarAddGrillaCard();
                            btnCalculateActionPerformed(null);
                            txtCard.requestFocus();
                    }
            }
            
        });
        JCBtipoTarjeta.addFocusListener(new FocusListener(){
            BigDecimal aux3=BigDecimal.ZERO;
                        @Override
            public void focusGained(FocusEvent fe) {
            }
                        @Override
            public void focusLost(FocusEvent fe) {
              aux3 =new BigDecimal(Double.parseDouble(lblTotalCards.getText())); 
              JCBNombreBanco.setEnabled(false);
              JCBtipoTarjeta.setEnabled(false);
              txtCard.setText("");
              lblTotalCards.setText(aux3.setScale(3, RoundingMode.HALF_UP)+"");
            }
			
        });
        tableCard.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                            int row = tableCard.rowAtPoint(evt.getPoint());
                            int col = tableCard.columnAtPoint(evt.getPoint());
                            BigDecimal aux1 = BigDecimal.ZERO,product = BigDecimal.ZERO;
                            if (row >= 0 && col >= 0){  
                                int res;
                                res = JOptionPane.showConfirmDialog(tableCard,"Desea Eliminar el Registro: "+(row+1), "Eliminar Registro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                        if (res == JOptionPane.YES_OPTION) {
                                           Object aa = dtmCard.getValueAt(row, 2);
                                           aux1=aux1.add(new BigDecimal(lblTotalCards.getText()));
                                           product=product.add(new BigDecimal(aa.toString()));
                                           lblTotalCards.setText(aux1.subtract(product).setScale(3, RoundingMode.HALF_UP)+""); 
                                           dtmCard.removeRow(row); 
                                           btnCalculateActionPerformed(null);
                                        }
                               }
                            }
                });
    }

    private void ListenerCheque() {
        JCBNombreBancoCheque.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JCBtipoPersona.setEnabled(true);
            }
        });
        JCBtipoPersona.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                    if (!txtCheque.getText().equals("")){
                            cargarAddGrillacheque();
                            btnCalculateActionPerformed(null);
                            txtCheque.requestFocus();
                    }
            }
            
        });
        JCBtipoPersona.addFocusListener(new FocusListener(){
            BigDecimal aux3=BigDecimal.ZERO;
                        @Override
            public void focusGained(FocusEvent fe) {
            }
                        @Override
            public void focusLost(FocusEvent fe) {
              aux3 =new BigDecimal(Double.parseDouble(lblTotalCheque.getText())); 
              JCBNombreBancoCheque.setEnabled(false);
              JCBtipoPersona.setEnabled(false);
              txtCheque.setText("");
              lblTotalCheque.setText(aux3.setScale(3, RoundingMode.HALF_UP)+"");
            }
			
        });
        tableCheque.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                            int row = tableCheque.rowAtPoint(evt.getPoint());
                            int col = tableCheque.columnAtPoint(evt.getPoint());
                            BigDecimal auxcheque = BigDecimal.ZERO,product2 = BigDecimal.ZERO;
                            if (row >= 0 && col >= 0){  
                                int res2;
                                res2 = JOptionPane.showConfirmDialog(tableCheque,"Desea Eliminar el Registro: "+(row+1), "Eliminar Registro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                                        if (res2 == JOptionPane.YES_OPTION) {
                                           Object a2 = dtmCheque.getValueAt(row, 2);
                                           auxcheque=auxcheque.add(new BigDecimal(lblTotalCheque.getText()));
                                           product2=product2.add(new BigDecimal(a2.toString()));
                                           lblTotalCheque.setText(auxcheque.subtract(product2).setScale(3, RoundingMode.HALF_UP)+""); 
                                           dtmCheque.removeRow(row); 
                                           btnCalculateActionPerformed(null);
                                        }
                               }
                            }
                });
    }
     public class ScriptPayments {
        private final JPanel panel;
        private final ArrayList vectorAmount;

        public ScriptPayments(JPanel panel,ArrayList vectorAmount) {
            this.panel = panel;
            this.vectorAmount = vectorAmount;
        }       
        // Se utiliza el método addButton porque se está usando el script usado en el modulo de pago
        public void addButton(String image, double amount) {
            //Agregando mensaje al final de la tension
            String monto = amount+" BsF.";
            final JTextField tf = new JTextField(" ");
            tf.setName(amount+"");
            addKeyListenertoTxt(tf,false);           
            JLabel jlabel = new JLabel(monto);
            ImageIcon imageIcon = new ImageIcon(dlSystem.getResourceAsImage(image));
            Image img = imageIcon.getImage() ;  
            Image newimg = img.getScaledInstance( 25, 25,  java.awt.Image.SCALE_SMOOTH ) ;  
            jlabel.setIcon(new ImageIcon( newimg ));
            vectorAmount.add(tf);
            panel.add(jlabel);
	    panel.add(tf,"wrap 1, width 60,align center");
            // Si es el primer jtextField que se crea se coloca el focus
            if(isFirstTxt)
                txtFirstTextField = tf;
            isFirstTxt = false;
        }
    }
     
    @Override
    public String getTitle() {
         return AppLocal.getIntString("Menu.CloseTPV");
    }

    @Override
    public void activate() throws BasicException {
         clearTextFields(); 
         EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 txtFirstTextField.requestFocus();
             }
         });     
    }

    @Override
    public boolean deactivate() {
       return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public Object getBean() {
       return this;
    } 
    public BigDecimal getTotalCash() {
        return totalCash;
    }

    public String getUserID() {
        return m_People.getID();
    }

    public void setTotalCash(BigDecimal totalCash) {
        this.totalCash = totalCash;
    }

    public BigDecimal getTotalCT() {
        return totalCT;
    }

    public void setTotalCT(BigDecimal totalCT) {
        this.totalCT = totalCT;
    }

    public BigDecimal getTotalCards() {
        return totalCards;
    }

    public void setTotalCards(BigDecimal totalCards) {
        this.totalCards = totalCards;
    }

    public BigDecimal getTotalCheque() {
        return totalCheque;
    }

    public void setTotalCheque(BigDecimal totalCheque) {
        this.totalCheque = totalCheque;
    }

    public BigDecimal getTotalPay() {
        return totalPay;
    }

    public void setTotalPay(BigDecimal totalPay) {
        this.totalPay = totalPay;
    }      

    public BigDecimal getTotalTransfer() {
        return totalTransfer;
    }

    public void setTotalTransfer(BigDecimal totalTransfer) {
        this.totalPay = totalTransfer;
    }      
}