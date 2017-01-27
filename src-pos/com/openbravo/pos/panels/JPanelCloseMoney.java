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
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.ComboBoxValModel;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.data.loader.SentenceList;
import com.openbravo.format.Formats;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.BeanFactoryApp;
import com.openbravo.pos.forms.BeanFactoryException;
import com.openbravo.pos.forms.DataLogicSales;
import com.openbravo.pos.forms.DataLogicSystem;
import com.openbravo.pos.forms.JPanelView;
import com.openbravo.pos.inventory.TaxCategoryInfo;
import com.openbravo.pos.scripting.ScriptEngine;
import com.openbravo.pos.scripting.ScriptException;
import com.openbravo.pos.scripting.ScriptFactory;
import com.sun.glass.ui.Pixels.Format;

import net.miginfocom.swing.MigLayout;

//Clase para editar los montos
public class JPanelCloseMoney extends javax.swing.JPanel implements JPanelView, BeanFactoryApp {
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
	private JLabel lblTotalDollarCash;
	private JLabel lblTotalCards;
	private JLabel lblTotalPay;
	private JLabel lblTotalCheque;
	private JLabel lblPeople;
	private JTextField txtCheque;
	private boolean isFirstTxt;// Determina si es el primer jtextfield que se
								// crear para colocar el focus
	private AppView m_App;
	private PaymentsModel m_PaymentsToClose;
	private Double totalCash;
	private Double totalCards;
	private Double totalCheque;
	private Double totalPay;
	private JButton btnCloseCash;
	private JButton btnClearTxt;
	private JTextField txtFirstTextField;
	private JLabel lblInfo;
	private JTextField txtCard;
	private JTable tableCard;
	private JTable tableCheque;
	private JComboBox<Object> JCBNombreBanco;
	private JComboBox<Object> JCBtipoTarjeta;
	private JComboBox<Object> JCBtipoPersona;
	private JComboBox<Object> JCBNombreBancoCheque;
	private JComboBox<String> JCBTipoTrans;
	private JComboBox JCBPeople;
	private DefaultTableModel dtmCard;
	private DefaultTableModel dtmCheque;
	private TaxCategoryInfo m_People;
	
	private static Double amtCash = 0.0;
	private static Double amtCashDollar = 0.0;
	private static Double amtCard = 0.0;
	private static Double amtChek  = 0.0;

	@Override
	public void init(AppView app) throws BeanFactoryException {
		dlSystem = (DataLogicSystem) app.getBean(DataLogicSystem.class.getName());
		dlSales = (DataLogicSales) app.getBean(DataLogicSales.class.getName());
		vectorTxtCash = new ArrayList<>();
		vectorTxtCashDollar = new ArrayList<>();
		vectorTxtCT = new ArrayList<>();
		vectorTxtCard = new ArrayList<>();
		isFirstTxt = true;
		m_App = app;
		setLayout(new MigLayout());
		setBorder(new EmptyBorder(5, 160, 5, 5));

		add(panelCloseCash(), "width 150, dock south, growx, wrap");
		add(panelCash(), "dock west,cell 0 0"); 
		add(panelCashDollar(), "dock west,cell 0 0"); 
		add(panelCard(), "width 150, dock west, cell 0 1"); 
		add(panelCheque(),"width 200, dock west, cell 0 1");
	}

	// Panel efectivo
	public JPanel panelCash() {
		JLabel lblImage = new JLabel(AppLocal.getIntString("Label.Cash"));
		lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cash.png")));
		JPanel pnlCash = new JPanel();
		pnlCash.setLayout(new MigLayout());
		pnlCash.setBorder(BorderFactory.createEtchedBorder());
		pnlCash.add(lblImage, "span, wrap 15");
		pnlCash.add(new JLabel(AppLocal.getIntString("label.amount") +":" ), "align center");
		pnlCash.add(new JLabel(AppLocal.getIntString("Label.Qty") + ":"), "wrap,align center");
		pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
		createPanelFromScript(pnlCash, "payment.cash", vectorTxtCash);
		JLabel lblTotal = new JLabel(AppLocal.getIntString("label.money"));
		lblTotalCash = new JLabel(Formats.CURRENCY.formatValue(0.0));
		pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
		pnlCash.add(lblTotal, "align center");
		pnlCash.add(lblTotalCash, "width 50,align center");
		return pnlCash;
	}

	// Panel efectivo
	public JPanel panelCashDollar() {
		JLabel lblImage = new JLabel(AppLocal.getIntString("Label.Cash.Dollar"));
		lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/cash.png")));
		JPanel pnlCash = new JPanel();
		pnlCash.setLayout(new MigLayout());
		pnlCash.setBorder(BorderFactory.createEtchedBorder());
		pnlCash.add(lblImage, "span, wrap 15");
		pnlCash.add(new JLabel(AppLocal.getIntString("label.amount") +":" ), "align center");
		pnlCash.add(new JLabel(AppLocal.getIntString("Label.Qty") + ":"), "wrap,align center");
		pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
		createPanelFromScript(pnlCash, "payment.dollar.cash", vectorTxtCashDollar);
		JLabel lblTotal = new JLabel(AppLocal.getIntString("label.money"));
		lblTotalDollarCash = new JLabel(Formats.CURRENCY.formatValue(0.0));
		pnlCash.add(new JSeparator(), "gapleft rel, growx,wrap,span");
		pnlCash.add(lblTotal, "align center");
		pnlCash.add(lblTotalDollarCash, "width 50,align center");
		return pnlCash;
	}

	// Panel Para el Pago con tarjeta
	public JPanel panelCard() {
		JLabel lblImage = new JLabel();
		txtCard = new JTextField();
		JPanel pnlCard = new JPanel();
		String cardType = dlSystem.getResourceAsXML("pointofsale.name");
		String arraycard[] = cardType.split(",");
		String cardType2 = dlSystem.getResourceAsXML("card.type");
		String arraycard2[] = cardType2.split(",");
		ArrayList<String> aCard = new ArrayList<String>();
		ArrayList<String> bCard = new ArrayList<String>();
		aCard.addAll(Arrays.asList(arraycard));
		bCard.addAll(Arrays.asList(arraycard2));
		JCBNombreBanco = new JComboBox<>(aCard.toArray());
		JCBtipoTarjeta = new JComboBox<>(bCard.toArray());
		JCBNombreBanco.setEnabled(false);
		JCBtipoTarjeta.setEnabled(false);
		Object[][] datosCard = null;
		String[] columnNamesCard = { 
				AppLocal.getIntString("Label.PointSaleTerminal"), 
				AppLocal.getIntString("label.card.type"), 
				AppLocal.getIntString("label.amount") 
				};
		dtmCard = new DefaultTableModel(datosCard, columnNamesCard);
		tableCard = new JTable(dtmCard);
		tableCard.setEnabled(false);
		lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/vcard.png")));
		lblImage.setText(AppLocal.getIntString("label.card"));
		JLabel lblCardCred = new JLabel();
		lblCardCred.setText("");
		pnlCard.setLayout(new MigLayout());
		pnlCard.setBorder(BorderFactory.createEtchedBorder());
		pnlCard.add(lblImage, "span,wrap 15");
		tableCard.getColumnModel().getColumn(0).setMaxWidth(100);
		tableCard.getColumnModel().getColumn(1).setMaxWidth(60);
		tableCard.getColumnModel().getColumn(2).setMaxWidth(60);
		JScrollPane scrollPaneCard;
		scrollPaneCard = new JScrollPane(tableCard);
		JLabel jlabelBanco = new JLabel(AppLocal.getIntString("Label.PointSaleTerminal") + ":");
		JLabel jlabelTipoT = new JLabel(AppLocal.getIntString("label.card.type") + ":");
		JLabel jlabelMonto = new JLabel(AppLocal.getIntString("label.amount") + ":");
		JLabel lblTotal = new JLabel();
		addKeyListenertoTxt(txtCard, true);// Agregando acciones al campo texto
		lblTotal.setText(AppLocal.getIntString("label.money"));
		lblTotalCards = new JLabel(Formats.CURRENCY.formatValue(0.0));
		ListenerCard();
		pnlCard.add(jlabelMonto, "cell 0 2");
		pnlCard.add(txtCard, "cell 1 2,width 105,align right,width 200");
		pnlCard.add(jlabelBanco, "cell 0 3");
		pnlCard.add(JCBNombreBanco, "width 200,cell 1 3");
		pnlCard.add(jlabelTipoT, "cell 0 4");
		pnlCard.add(JCBtipoTarjeta, "cell 1 4,width 200");
		pnlCard.add(scrollPaneCard, "cell 0 6,gapleft rel, growx,wrap,span");
		pnlCard.add(new JSeparator(), "gapleft rel, growx,wrap,span");
		pnlCard.add(lblTotal, "align center");
		pnlCard.add(lblTotalCards, "width 50,align center");
		return pnlCard;
	}

	public JPanel panelCheque() {
		JLabel lblImage = new JLabel();
		lblImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/desktop.png")));
		lblImage.setText("Cheques/Transf.");
		JLabel lblTotal2 = new JLabel();
		String cardType = dlSystem.getResourceAsXML("pointofsale.name");
		String arraycard[] = cardType.split(",");
		String cardType2 = dlSystem.getResourceAsXML("tipo.persona");
		String arraycard2[] = cardType2.split(",");
		ArrayList<String> aCheque = new ArrayList<String>();
		ArrayList<String> bCheque = new ArrayList<String>();
		aCheque.addAll(Arrays.asList(arraycard));
		bCheque.addAll(Arrays.asList(arraycard2));
		String[] transType = { "Transferencia", AppLocal.getIntString("Label.Cheque") };
		JCBTipoTrans = new JComboBox<>(transType);
		JCBNombreBancoCheque = new JComboBox<>(aCheque.toArray());
		JCBtipoPersona = new JComboBox<>(bCheque.toArray());
		JPanel pnlCheque = new JPanel();
		pnlCheque.setLayout(new MigLayout());
		pnlCheque.setBorder(BorderFactory.createEtchedBorder());
		Object[][] datos2 = null;
		String[] columnNames2 = { "Banco", "Tipo", AppLocal.getIntString("label.amount") };
		dtmCheque = new DefaultTableModel(datos2, columnNames2);
		tableCheque = new JTable(dtmCheque);
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
		JLabel jlabelBanco = new JLabel("Banco: ");
		JLabel jlabelMonto = new JLabel(AppLocal.getIntString("label.amount") + ":");
		JLabel jlabelPersona = new JLabel("Persona: ");
		lblTotal2.setText(AppLocal.getIntString("label.money"));
		lblTotalCheque = new JLabel("0");
		txtCheque = new JTextField();
		addKeyListenertoTxt(txtCheque, true);
		ListenerCheque();
		pnlCheque.add(lblImage, "span,wrap 15");
		pnlCheque.add(jlabelMonto, "cell 0 2");
		pnlCheque.add(txtCheque, "cell 1 2,width 120,align right");
		pnlCheque.add(jlabelBanco, "cell 0 3");
		pnlCheque.add(JCBNombreBancoCheque, "cell 1 3");
		pnlCheque.add(jlabelBanco, "cell 0 4");
		pnlCheque.add(JCBNombreBancoCheque, "cell 1 4");
		pnlCheque.add(jlabelPersona, "cell 0 5");
		pnlCheque.add(JCBtipoPersona, "width 118, cell 1 5");
		pnlCheque.add(scrollPaneCheque, "cell 0 6,gapleft rel, growx,wrap,span");
		pnlCheque.add(new JSeparator(), "gapleft rel, growx,wrap,span");
		pnlCheque.add(lblTotal2);
		pnlCheque.add(lblTotalCheque);
		return pnlCheque;
	}

	private JPanel panelCloseCash() {

		btnCloseCash = new JButton();
		btnCloseCash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/close_cash.png")));
		btnCloseCash.setText(AppLocal.getIntString("Close.Cash"));
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
		btnClearTxt.setText(AppLocal.getIntString("Label.Clean.Fields"));
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

		List catlist = null;
		try {
			catlist = m_sentcat.list();
		} catch (BasicException ex) {
			ex.getMessage();
		}
		catlist.add(0, null);
		m_PeopleModel = new ComboBoxValModel(catlist);

		lblPeople = new JLabel(AppLocal.getIntString("Label.Cashier") + ":");
		JCBPeople = new JComboBox<>();
		JCBPeople.setModel(m_PeopleModel);
		addKeyListenertoCombo(JCBPeople);

		lblTotalPay = new JLabel(Formats.CURRENCY.formatValue(0.0));
		lblInfo = new JLabel();
		lblInfo.setForeground(Color.RED);

		JLabel lblTotal = new JLabel("Total en caja");
		JPanel pnlCloseCash = new JPanel();
		pnlCloseCash.setLayout(new MigLayout());
		pnlCloseCash.setBorder(BorderFactory.createEtchedBorder());
		pnlCloseCash.add(btnClearTxt, "cell 0 0");
		pnlCloseCash.add(btnCloseCash, "cell 1 0,wrap");

		pnlCloseCash.add(lblPeople, "cell 1 0,wrap");
		pnlCloseCash.add(JCBPeople, "cell 1 0,wrap");

		pnlCloseCash.add(lblTotal, "align center");
		pnlCloseCash.add(lblTotalPay, "wrap");
		pnlCloseCash.add(lblInfo, "span 2,align center");
		return pnlCloseCash;
	}

	private void clearTextFields() {
		for (int i = 0; i < vectorTxtCard.size(); i++) {
			JTextField txtAmountCard = (JTextField) vectorTxtCard.get(i);
			txtAmountCard.setText("");
		}
		for (int i = 0; i < vectorTxtCT.size(); i++) {
			JTextField txtAmountCT = (JTextField) vectorTxtCT.get(i);
			txtAmountCT.setText("");
		}
		for (int i = 0; i < vectorTxtCash.size(); i++) {
			JTextField txtAmountCash = (JTextField) vectorTxtCash.get(i);
			txtAmountCash.setText("");
		}
		if (txtCheque != null) {
			txtCheque.setText("");
		}
		if (txtCard != null) {
			txtCard.setText("");
		}
		if (JCBNombreBanco != null) {
			JCBNombreBanco.setSelectedIndex(0);
		}

		if (JCBNombreBanco != null) {
			JCBNombreBanco.setEnabled(false);
		}

		if (JCBtipoTarjeta != null) {
			JCBtipoTarjeta.setSelectedIndex(0);
		}
		if (JCBtipoTarjeta != null) {
			JCBtipoTarjeta.setEnabled(false);
		}

		if (lblTotalCash != null) {
			lblTotalCash.setText(Formats.CURRENCY.formatValue(0.0));
		}

		if (lblTotalPay != null) {
			lblTotalPay.setText(Formats.CURRENCY.formatValue(0.0));
		}

		if (lblInfo != null) {
			lblInfo.setText("");
		}

		if (dtmCard != null) {
			dtmCard.setRowCount(0);
		}

		if (dtmCheque != null) {
			dtmCheque.setRowCount(0);
		}

		if (lblTotalCheque != null) {
			lblTotalCheque.setText(Formats.CURRENCY.formatValue(0.0));
		}

		if (lblTotalCards != null) {
			lblTotalCards.setText(Formats.CURRENCY.formatValue(0.0));
		}

		if (JCBPeople != null) {
			JCBPeople.setSelectedIndex(0);
		}
		buttonsActions(0.0);
	}

	private void btnClearTxtActionPerformed(ActionEvent evt) {

		int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.clearTxtCloseCash"),
				AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (res == JOptionPane.YES_OPTION) {
			// Limpia los campos de los paneles de pagos
			clearTextFields();
		}
	}

	private void cargarAddGrillaCard() {
		Object[] newRow = { JCBNombreBanco.getSelectedItem(), JCBtipoTarjeta.getSelectedItem(), txtCard.getText() };
		dtmCard.addRow(newRow);
	}

	private void cargarAddGrillacheque() {
		Object[] newRow = { JCBNombreBancoCheque.getSelectedItem(), JCBtipoPersona.getSelectedItem(),
				txtCheque.getText() };
		dtmCheque.addRow(newRow);
	}

	private void btnCloseCashActionPerformed(ActionEvent evt) {
		btnCalculateActionPerformed(null);
		callCloseCash();
	}

	private void btnCalculateActionPerformed(ActionEvent evt) {
		calculateAmountCash();
		calculateAmountDollarCash();
		calculateAmountCard();
		calculateAmountCheque();
		totalCash = amtCash;
		totalCards = amtCard;
		totalCheque = amtChek;
		totalPay = totalCash + totalCards + totalCheque;
		lblTotalPay.setText(Formats.CURRENCY.formatValue(totalPay));
		buttonsActions(totalPay);
	}

	// Valida si hay ventas o el monto es mayor a cero para poder hacer el
	// cierre de caja
	private void buttonsActions(Double total) {
		try {
			boolean isDiffZero = total != 0.0;
			String sUser = "";

			isDiffZero = isDiffZero && JCBPeople.getSelectedIndex() > 0;

			if (JCBPeople.getSelectedIndex() > 0) {
				m_People = (TaxCategoryInfo) JCBPeople.getSelectedItem();
				sUser = m_People.getID().toString();

				m_PaymentsToClose = PaymentsModel.loadInstance(m_App);
				boolean isPayReg = false;
				// Revisamos si hay ventas para realizar el cuadre de caja
				if ((m_PaymentsToClose.getPayments() != 0 || m_PaymentsToClose.getSales() != 0)) {
					lblInfo.setText("");
					isPayReg = true;
				} else {
					lblInfo.setText(AppLocal.getIntString("label.nosales"));
				}

				btnClearTxt.setEnabled(isDiffZero);
				btnCloseCash.setEnabled(isDiffZero && isPayReg);
			}
		} catch (BasicException ex) {
			Logger.getLogger(JPanelCloseMoney.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void calculateAmountCash() {
		Double cant;
		Double amount = 0.0;
		for (int i = 0; i < vectorTxtCash.size(); i++) {
			JTextField txtAmountCash = (JTextField) vectorTxtCash.get(i);
			cant = txtAmountCash.getText() != null && !txtAmountCash.getText().trim().equals("") ? Double.parseDouble(txtAmountCash.getText()) : 0.0 ;
			
			if (cant != 0.0) {
				amount = amount + (cant * Double.parseDouble(txtAmountCash.getName()));
			}
		}
		if(lblTotalCash != null) {
			try {
				amtCash = amount;
				String amt = Formats.CURRENCY.parseValue(amtCash.toString()).toString();
				lblTotalCash.setText(amt);
			} catch (BasicException e) {
			}
			
		}
	}
	
	private void calculateAmountDollarCash() {
		Double cant;
		Double amount = 0.0;
		for (int i = 0; i < vectorTxtCashDollar.size(); i++) {
			JTextField txtAmountCash = (JTextField) vectorTxtCashDollar.get(i);
			cant = txtAmountCash.getText() != null && !txtAmountCash.getText().trim().equals("") ? Double.parseDouble(txtAmountCash.getText()) : 0.0 ;
			
			if (cant != 0.0) {
				amount = amount + (cant * Double.parseDouble(txtAmountCash.getName()));
			}
		}
		if(lblTotalDollarCash!= null) {
			try {
				amtCashDollar = amount;
				String amt = Formats.CURRENCY.parseValue(amtCashDollar.toString()).toString();
				lblTotalDollarCash.setText(amt);
			} catch (BasicException e) {
			}
			
		}
	}
	
	private void calculateAmountCard() {
		Double cant = txtCard.getText() != null && !txtCard.getText().trim().equals("") ? Double.parseDouble(txtCard.getText()) : 0.0;
		amtCard = amtCard + cant;
		lblTotalCards.setText(Formats.CURRENCY.formatValue(amtCard));
	}

	private void calculateAmountCheque() {
		Double cant = txtCheque.getText() != null && !txtCheque.getText().trim().equals("") ? Double.parseDouble(txtCheque.getText()) : 0.0;
		amtChek = amtChek + cant;
		lblTotalCheque.setText(Formats.CURRENCY.formatValue(amtChek));
	}

	// Busca el script de configuracion para los pagos efectivo,CT, y tarjeta
	public void createPanelFromScript(JPanel panel, String resourcePay, ArrayList vectorAmount) {

		String scriptPay = dlSystem.getResourceAsXML(resourcePay);
		if (scriptPay != null) {
			try {
				ScriptEngine script = ScriptFactory.getScriptEngine(ScriptFactory.BEANSHELL);
				script.put("payment", new ScriptPayments(panel, vectorAmount));
				script.eval(scriptPay);
			} catch (ScriptException e) {
				MessageInf msg = new MessageInf(MessageInf.SGN_NOTICE, AppLocal.getIntString("message.cannotexecute"),
						e);
				msg.show(this);
			}
		}
	}

	private void addKeyListenertoCombo(final JComboBox comboBox) {
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comboBox.getSelectedIndex() > 0)
					btnCalculateActionPerformed(null);
			}
		});
	}

	// Añade acciones a los JTextField creados
	private void addKeyListenertoTxt(final JTextField textField, final boolean isDecimal) {
		textField.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				char c = e.getKeyChar();
				if (isDecimal) {
					if ((c < '0' || c > '9') && c != ',' && c != ' ')
						e.consume();
				} else {
					if ((c < '0' || c > '9'))
						e.consume();
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent ke) {
				if (textField.equals(txtCard)) {
					JCBNombreBanco.setEnabled(true);
				} else if (textField.equals(txtCheque)) {
					JCBNombreBancoCheque.setEnabled(true);
				} else
					btnCalculateActionPerformed(null);
			}
		});
	}

	public void callCloseCash() {
		int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannaclosecash"),
				AppLocal.getIntString("message.title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (JCBPeople.getSelectedIndex() > 0)
			m_People = (TaxCategoryInfo) JCBPeople.getSelectedItem();

		if (res == JOptionPane.YES_OPTION) {
			m_App.getAppUserView().showTask(JPanelCloseMoneyFinal.class.getName());
			clearTextFields();
		}
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
				if (!txtCard.getText().equals("")) {
					cargarAddGrillaCard();
					btnCalculateActionPerformed(null);
					txtCard.requestFocus();
				}
			}

		});
		JCBtipoTarjeta.addFocusListener(new FocusListener() {
			Double aux = 0.0;

			@Override
			public void focusGained(FocusEvent fe) {
			}

			@Override
			public void focusLost(FocusEvent fe) {
				aux = amtCard;
				JCBNombreBanco.setEnabled(false);
				JCBtipoTarjeta.setEnabled(false);
				txtCard.setText("");
				lblTotalCards.setText(Formats.CURRENCY.formatValue(aux));
			}

		});
		tableCard.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = tableCard.rowAtPoint(evt.getPoint());
				int col = tableCard.columnAtPoint(evt.getPoint());
				Double aux = 0.0;
				Double product = 0.0;

				if (row >= 0 && col >= 0) {
					int res;
					res = JOptionPane.showConfirmDialog(tableCard, "Desea Eliminar el Registro: " + (row + 1),
							"Eliminar Registro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (res == JOptionPane.YES_OPTION) {
						Object aa = dtmCard.getValueAt(row, 2);
						aux = aux + amtCard;
						product = product + Double.parseDouble(aa.toString());
						amtCard = aux - product;
						lblTotalCards.setText(Formats.CURRENCY.formatValue(amtCard));
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
				if (!txtCheque.getText().equals("")) {
					cargarAddGrillacheque();
					btnCalculateActionPerformed(null);
					txtCheque.requestFocus();
				}
			}

		});
		JCBtipoPersona.addFocusListener(new FocusListener() {
			Double aux = 0.0;

			@Override
			public void focusGained(FocusEvent fe) {
			}

			@Override
			public void focusLost(FocusEvent fe) {
				aux = amtChek;
				JCBNombreBancoCheque.setEnabled(false);
				JCBtipoPersona.setEnabled(false);
				txtCheque.setText("");
				lblTotalCheque.setText(Formats.CURRENCY.formatValue(aux));
			}

		});
		tableCheque.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = tableCheque.rowAtPoint(evt.getPoint());
				int col = tableCheque.columnAtPoint(evt.getPoint());
				Double aux = 0.0;
				Double product = 0.0;

				if (row >= 0 && col >= 0) {
					int res = JOptionPane.showConfirmDialog(tableCheque, "Desea Eliminar el Registro: " + (row + 1),
							"Eliminar Registro", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if (res == JOptionPane.YES_OPTION) {
						Object a2 = dtmCheque.getValueAt(row, 2);
						aux = aux + amtChek;
						product = product + Double.parseDouble(a2.toString());
						amtChek = aux - product;
						lblTotalCheque.setText(Formats.CURRENCY.formatValue(amtChek));
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

		public ScriptPayments(JPanel panel, ArrayList vectorAmount) {
			this.panel = panel;
			this.vectorAmount = vectorAmount;
		}

		public void addButton(String image, double amount) {
			String monto = amount + " BsF.";
			final JTextField tf = new JTextField(" ");
			tf.setName(amount + "");
			addKeyListenertoTxt(tf, false);
			JLabel jlabel = new JLabel(monto);
			ImageIcon imageIcon = new ImageIcon(dlSystem.getResourceAsImage(image));
			Image img = imageIcon.getImage();
			Image newimg = img.getScaledInstance(25, 25, java.awt.Image.SCALE_SMOOTH);
			jlabel.setIcon(new ImageIcon(newimg));
			vectorAmount.add(tf);
			panel.add(jlabel);
			panel.add(tf, "wrap 1, width 60,align center");
			// Si es el primer jtextField que se crea se coloca el focus
			if (isFirstTxt)
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
				if(txtFirstTextField != null) {
					txtFirstTextField.requestFocus();
				}
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

	public Double getTotalCash() {
		return totalCash;
	}

	public String getUserID() {
		return m_People.getID();
	}

	public void setTotalCash(Double totalCash) {
		this.totalCash = totalCash;
	}

	public Double getTotalCards() {
		return totalCards;
	}

	public void setTotalCards(Double totalCards) {
		this.totalCards = totalCards;
	}

	public Double getTotalCheque() {
		return totalCheque;
	}

	public void setTotalCheque(Double totalCheque) {
		this.totalCheque = totalCheque;
	}

	public Double getTotalPay() {
		return totalPay;
	}

	public void setTotalPay(Double totalPay) {
		this.totalPay = totalPay;
	}

}