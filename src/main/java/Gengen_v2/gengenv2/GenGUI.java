package Gengen_v2.gengenv2;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.border.BevelBorder;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import java.awt.GridLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JTextField;
import com.jgoodies.forms.layout.FormSpecs;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import javax.swing.SwingConstants;

public class GenGUI extends JFrame {

	private JPanel contentPane;
	Phonology phonology;
	
	JLabel[] propertyLabels = new JLabel[SegProp.values().length];
	JTextField[] prominenceLabels = new JTextField[SegProp.values().length];
	JTextField[] aggreganceLabels = new JTextField[SegProp.values().length];
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GenGUI frame = new GenGUI();
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GenGUI()
	{
		phonology = new Phonology();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		setBounds(100, 100, 800, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel ControlPanel = new JPanel();
		contentPane.add(ControlPanel, BorderLayout.SOUTH);
		
		JButton btnGenerateInventory = new JButton("Generate Inventory");
		ControlPanel.add(btnGenerateInventory);
		
		JTabbedPane mainContentPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(mainContentPane, BorderLayout.CENTER);
		
		JPanel propertiesPane = makePropertiesPane();
		mainContentPane.addTab("Properties", null, propertiesPane, null);
		
		JPanel inventoryPane = new JPanel();
		inventoryPane.setBackground(new Color(204, 204, 255));
		mainContentPane.addTab("Inventory", null, inventoryPane, null);
		inventoryPane.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel consonantPanel = new JPanel();
		consonantPanel.setOpaque(false);
		inventoryPane.add(consonantPanel);
		consonantPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel inventoryDisplay = new JPanel();
		consonantPanel.add(inventoryDisplay, BorderLayout.WEST);
		GridBagLayout gbl_inventoryDisplay = new GridBagLayout();
		gbl_inventoryDisplay.columnWidths = new int[]{74, 74, 74, 74, 74, 74, 74, 0};
		gbl_inventoryDisplay.rowHeights = new int[]{35, 35, 35, 35, 35, 35, 35, 0};
		gbl_inventoryDisplay.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_inventoryDisplay.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		inventoryDisplay.setLayout(gbl_inventoryDisplay);
		
		JLabel label = new JLabel("");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		inventoryDisplay.add(label, gbc_label);
		
		JLabel lblLabial = new JLabel("<html><strong> Labial ");
		GridBagConstraints gbc_lblLabial = new GridBagConstraints();
		gbc_lblLabial.fill = GridBagConstraints.VERTICAL;
		gbc_lblLabial.insets = new Insets(0, 0, 5, 5);
		gbc_lblLabial.gridx = 1;
		gbc_lblLabial.gridy = 0;
		inventoryDisplay.add(lblLabial, gbc_lblLabial);
		
		JLabel lblAlveolar = new JLabel("<html><strong>Alveolar");
		GridBagConstraints gbc_lblAlveolar = new GridBagConstraints();
		gbc_lblAlveolar.fill = GridBagConstraints.VERTICAL;
		gbc_lblAlveolar.insets = new Insets(0, 0, 5, 5);
		gbc_lblAlveolar.gridx = 2;
		gbc_lblAlveolar.gridy = 0;
		inventoryDisplay.add(lblAlveolar, gbc_lblAlveolar);
		
		JLabel lblPalatal = new JLabel("<html><strong>Palatal");
		GridBagConstraints gbc_lblPalatal = new GridBagConstraints();
		gbc_lblPalatal.fill = GridBagConstraints.VERTICAL;
		gbc_lblPalatal.insets = new Insets(0, 0, 5, 5);
		gbc_lblPalatal.gridx = 3;
		gbc_lblPalatal.gridy = 0;
		inventoryDisplay.add(lblPalatal, gbc_lblPalatal);
		
		JLabel lblVelar = new JLabel("<html><strong>Velar");
		GridBagConstraints gbc_lblVelar = new GridBagConstraints();
		gbc_lblVelar.fill = GridBagConstraints.VERTICAL;
		gbc_lblVelar.insets = new Insets(0, 0, 5, 5);
		gbc_lblVelar.gridx = 4;
		gbc_lblVelar.gridy = 0;
		inventoryDisplay.add(lblVelar, gbc_lblVelar);
		
		JLabel lblUvular = new JLabel("<html><b>Uvular");
		GridBagConstraints gbc_lblUvular = new GridBagConstraints();
		gbc_lblUvular.fill = GridBagConstraints.VERTICAL;
		gbc_lblUvular.insets = new Insets(0, 0, 5, 5);
		gbc_lblUvular.gridx = 5;
		gbc_lblUvular.gridy = 0;
		inventoryDisplay.add(lblUvular, gbc_lblUvular);
		
		JLabel lblGlottal = new JLabel("<html><b>Glottal");
		GridBagConstraints gbc_lblGlottal = new GridBagConstraints();
		gbc_lblGlottal.fill = GridBagConstraints.VERTICAL;
		gbc_lblGlottal.insets = new Insets(0, 0, 5, 0);
		gbc_lblGlottal.gridx = 6;
		gbc_lblGlottal.gridy = 0;
		inventoryDisplay.add(lblGlottal, gbc_lblGlottal);
		
		JLabel lblStop = new JLabel("<html><center><strong>Stop/ <br>Affricate");
		GridBagConstraints gbc_lblStop = new GridBagConstraints();
		gbc_lblStop.fill = GridBagConstraints.VERTICAL;
		gbc_lblStop.insets = new Insets(0, 0, 5, 5);
		gbc_lblStop.gridx = 0;
		gbc_lblStop.gridy = 1;
		inventoryDisplay.add(lblStop, gbc_lblStop);
		
		JLabel lbl_p_ph = new JLabel("New label");
		GridBagConstraints gbc_lbl_p_ph = new GridBagConstraints();
		gbc_lbl_p_ph.fill = GridBagConstraints.BOTH;
		gbc_lbl_p_ph.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_p_ph.gridx = 1;
		gbc_lbl_p_ph.gridy = 1;
		inventoryDisplay.add(lbl_p_ph, gbc_lbl_p_ph);
		
		JLabel lbl_t_th = new JLabel("New label");
		GridBagConstraints gbc_lbl_t_th = new GridBagConstraints();
		gbc_lbl_t_th.fill = GridBagConstraints.BOTH;
		gbc_lbl_t_th.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_t_th.gridx = 2;
		gbc_lbl_t_th.gridy = 1;
		inventoryDisplay.add(lbl_t_th, gbc_lbl_t_th);
		
		JLabel lbl_ch = new JLabel("New label");
		GridBagConstraints gbc_lbl_ch = new GridBagConstraints();
		gbc_lbl_ch.fill = GridBagConstraints.BOTH;
		gbc_lbl_ch.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_ch.gridx = 3;
		gbc_lbl_ch.gridy = 1;
		inventoryDisplay.add(lbl_ch, gbc_lbl_ch);
		
		JLabel lbl_k_kh = new JLabel("New label");
		GridBagConstraints gbc_lbl_k_kh = new GridBagConstraints();
		gbc_lbl_k_kh.fill = GridBagConstraints.BOTH;
		gbc_lbl_k_kh.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_k_kh.gridx = 4;
		gbc_lbl_k_kh.gridy = 1;
		inventoryDisplay.add(lbl_k_kh, gbc_lbl_k_kh);
		
		JLabel lbl_q_qh = new JLabel("New label");
		GridBagConstraints gbc_lbl_q_qh = new GridBagConstraints();
		gbc_lbl_q_qh.fill = GridBagConstraints.BOTH;
		gbc_lbl_q_qh.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_q_qh.gridx = 5;
		gbc_lbl_q_qh.gridy = 1;
		inventoryDisplay.add(lbl_q_qh, gbc_lbl_q_qh);
		
		JLabel lbl_apostrophe = new JLabel("New label");
		GridBagConstraints gbc_lbl_apostrophe = new GridBagConstraints();
		gbc_lbl_apostrophe.fill = GridBagConstraints.BOTH;
		gbc_lbl_apostrophe.insets = new Insets(0, 0, 5, 0);
		gbc_lbl_apostrophe.gridx = 6;
		gbc_lbl_apostrophe.gridy = 1;
		inventoryDisplay.add(lbl_apostrophe, gbc_lbl_apostrophe);
		
		JLabel lbl_b_bh = new JLabel("New label");
		GridBagConstraints gbc_lbl_b_bh = new GridBagConstraints();
		gbc_lbl_b_bh.fill = GridBagConstraints.BOTH;
		gbc_lbl_b_bh.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_b_bh.gridx = 1;
		gbc_lbl_b_bh.gridy = 2;
		inventoryDisplay.add(lbl_b_bh, gbc_lbl_b_bh);
		
		JLabel lbl_d_dh = new JLabel("New label");
		GridBagConstraints gbc_lbl_d_dh = new GridBagConstraints();
		gbc_lbl_d_dh.fill = GridBagConstraints.BOTH;
		gbc_lbl_d_dh.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_d_dh.gridx = 2;
		gbc_lbl_d_dh.gridy = 2;
		inventoryDisplay.add(lbl_d_dh, gbc_lbl_d_dh);
		
		JLabel lbl_j = new JLabel("New label");
		GridBagConstraints gbc_lbl_j = new GridBagConstraints();
		gbc_lbl_j.fill = GridBagConstraints.BOTH;
		gbc_lbl_j.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_j.gridx = 3;
		gbc_lbl_j.gridy = 2;
		inventoryDisplay.add(lbl_j, gbc_lbl_j);
		
		JLabel lblFricative = new JLabel("<html><strong>Fricative");
		GridBagConstraints gbc_lblFricative = new GridBagConstraints();
		gbc_lblFricative.fill = GridBagConstraints.VERTICAL;
		gbc_lblFricative.insets = new Insets(0, 0, 5, 5);
		gbc_lblFricative.gridx = 0;
		gbc_lblFricative.gridy = 3;
		inventoryDisplay.add(lblFricative, gbc_lblFricative);
		
		JLabel lbl_f = new JLabel("New label");
		GridBagConstraints gbc_lbl_f = new GridBagConstraints();
		gbc_lbl_f.fill = GridBagConstraints.BOTH;
		gbc_lbl_f.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_f.gridx = 1;
		gbc_lbl_f.gridy = 3;
		inventoryDisplay.add(lbl_f, gbc_lbl_f);
		
		JLabel lbl_s = new JLabel("New label");
		GridBagConstraints gbc_lbl_s = new GridBagConstraints();
		gbc_lbl_s.fill = GridBagConstraints.BOTH;
		gbc_lbl_s.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_s.gridx = 2;
		gbc_lbl_s.gridy = 3;
		inventoryDisplay.add(lbl_s, gbc_lbl_s);
		
		JLabel lbl_sh = new JLabel("New label");
		GridBagConstraints gbc_lbl_sh = new GridBagConstraints();
		gbc_lbl_sh.fill = GridBagConstraints.BOTH;
		gbc_lbl_sh.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_sh.gridx = 3;
		gbc_lbl_sh.gridy = 3;
		inventoryDisplay.add(lbl_sh, gbc_lbl_sh);
		
		JLabel lbl_h = new JLabel("New label");
		GridBagConstraints gbc_lbl_h = new GridBagConstraints();
		gbc_lbl_h.fill = GridBagConstraints.BOTH;
		gbc_lbl_h.insets = new Insets(0, 0, 5, 0);
		gbc_lbl_h.gridx = 6;
		gbc_lbl_h.gridy = 3;
		inventoryDisplay.add(lbl_h, gbc_lbl_h);
		
		JLabel lbl_v = new JLabel("New label");
		GridBagConstraints gbc_lbl_v = new GridBagConstraints();
		gbc_lbl_v.fill = GridBagConstraints.BOTH;
		gbc_lbl_v.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_v.gridx = 1;
		gbc_lbl_v.gridy = 4;
		inventoryDisplay.add(lbl_v, gbc_lbl_v);
		
		JLabel lbl_z = new JLabel("New label");
		GridBagConstraints gbc_lbl_z = new GridBagConstraints();
		gbc_lbl_z.fill = GridBagConstraints.BOTH;
		gbc_lbl_z.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_z.gridx = 2;
		gbc_lbl_z.gridy = 4;
		inventoryDisplay.add(lbl_z, gbc_lbl_z);
		
		JLabel lbl_zh = new JLabel("New label");
		GridBagConstraints gbc_lbl_zh = new GridBagConstraints();
		gbc_lbl_zh.fill = GridBagConstraints.BOTH;
		gbc_lbl_zh.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_zh.gridx = 3;
		gbc_lbl_zh.gridy = 4;
		inventoryDisplay.add(lbl_zh, gbc_lbl_zh);
		
		JLabel lblNasal = new JLabel("<html><strong>Nasal");
		GridBagConstraints gbc_lblNasal = new GridBagConstraints();
		gbc_lblNasal.fill = GridBagConstraints.VERTICAL;
		gbc_lblNasal.insets = new Insets(0, 0, 5, 5);
		gbc_lblNasal.gridx = 0;
		gbc_lblNasal.gridy = 5;
		inventoryDisplay.add(lblNasal, gbc_lblNasal);
		
		JLabel lbl_m = new JLabel("New label");
		GridBagConstraints gbc_lbl_m = new GridBagConstraints();
		gbc_lbl_m.fill = GridBagConstraints.BOTH;
		gbc_lbl_m.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_m.gridx = 1;
		gbc_lbl_m.gridy = 5;
		inventoryDisplay.add(lbl_m, gbc_lbl_m);
		
		JLabel lbl_n = new JLabel("New label");
		GridBagConstraints gbc_lbl_n = new GridBagConstraints();
		gbc_lbl_n.fill = GridBagConstraints.BOTH;
		gbc_lbl_n.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_n.gridx = 2;
		gbc_lbl_n.gridy = 5;
		inventoryDisplay.add(lbl_n, gbc_lbl_n);
		
		JLabel lbl_ng = new JLabel("New label");
		GridBagConstraints gbc_lbl_ng = new GridBagConstraints();
		gbc_lbl_ng.fill = GridBagConstraints.BOTH;
		gbc_lbl_ng.insets = new Insets(0, 0, 5, 5);
		gbc_lbl_ng.gridx = 4;
		gbc_lbl_ng.gridy = 5;
		inventoryDisplay.add(lbl_ng, gbc_lbl_ng);
		
		JLabel lblApproximant = new JLabel("<html><strong>Approximant");
		GridBagConstraints gbc_lblApproximant = new GridBagConstraints();
		gbc_lblApproximant.fill = GridBagConstraints.VERTICAL;
		gbc_lblApproximant.insets = new Insets(0, 0, 0, 5);
		gbc_lblApproximant.gridx = 0;
		gbc_lblApproximant.gridy = 6;
		inventoryDisplay.add(lblApproximant, gbc_lblApproximant);
		
		JLabel lbl_w = new JLabel("New label");
		GridBagConstraints gbc_lbl_w = new GridBagConstraints();
		gbc_lbl_w.fill = GridBagConstraints.BOTH;
		gbc_lbl_w.insets = new Insets(0, 0, 0, 5);
		gbc_lbl_w.gridx = 1;
		gbc_lbl_w.gridy = 6;
		inventoryDisplay.add(lbl_w, gbc_lbl_w);
		
		JLabel lbl_l_r = new JLabel("New label");
		GridBagConstraints gbc_lbl_l_r = new GridBagConstraints();
		gbc_lbl_l_r.fill = GridBagConstraints.BOTH;
		gbc_lbl_l_r.insets = new Insets(0, 0, 0, 5);
		gbc_lbl_l_r.gridx = 2;
		gbc_lbl_l_r.gridy = 6;
		inventoryDisplay.add(lbl_l_r, gbc_lbl_l_r);
		
		JLabel lbl_y = new JLabel("New label");
		GridBagConstraints gbc_lbl_y = new GridBagConstraints();
		gbc_lbl_y.fill = GridBagConstraints.BOTH;
		gbc_lbl_y.insets = new Insets(0, 0, 0, 5);
		gbc_lbl_y.gridx = 4;
		gbc_lbl_y.gridy = 6;
		inventoryDisplay.add(lbl_y, gbc_lbl_y);
		
		JPanel panel_1 = new JPanel();
		inventoryPane.add(panel_1);
		panel_1.setOpaque(false);
	}

	private JPanel makePropertiesPane()
	{
		JPanel propertiesPane = new JPanel();
		propertiesPane.setLayout(new BorderLayout(0, 0));
		
		JLabel lblProperties = new JLabel("Properties");
		lblProperties.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblProperties.setHorizontalAlignment(SwingConstants.CENTER);
		propertiesPane.add(lblProperties, BorderLayout.NORTH);

		JPanel propertiesBody = new JPanel();
		propertiesPane.add(propertiesBody, BorderLayout.SOUTH);
		propertiesBody.setLayout(new GridLayout(0, 2, 0, 0));
		
		
		
		int leftQuantity = 23;
		
		JPanel propertiesLeft = new JPanel(new GridLayout(leftQuantity, 3, 0, 0));
		propertiesBody.add(propertiesLeft);
		
		JPanel propertiesRight = new JPanel(new GridLayout(leftQuantity, 3, 0, 0));
		propertiesBody.add(propertiesRight);
		
		Phonology p = phonology;
		for (int i = 0; i < SegProp.values().length; i++)
		{
			// Label name
			propertyLabels[i] = new JLabel(SegProp.values()[i].name());
			propertyLabels[i].setFont(new Font("Tahoma", Font.BOLD, 11));
			
			// Prominence fields
			String label = Double.toString(p.prominence[i]);
			if (label.length() > 6)
				label = label.substring(0, 6);
				
			prominenceLabels[i] = new JTextField(label);
			prominenceLabels[i].setFont(new Font("Tahoma", Font.PLAIN, 11));
			prominenceLabels[i].getDocument().addDocumentListener(new TextFieldListener(prominenceLabels, i));
			
			if (p.prominence[i] == 0)
				prominenceLabels[i].setBackground(Color.RED);
			
			
			// Aggregance fields
			label = Double.toString(p.aggregance[i]);
			if (label.length() > 6)
				label = label.substring(0, 6);
			
			aggreganceLabels[i] = new JTextField(label);
			aggreganceLabels[i].setFont(new Font("Tahoma", Font.PLAIN, 11));
			aggreganceLabels[i].getDocument().addDocumentListener(new TextFieldListener(aggreganceLabels, i));
				
			if (p.aggregance[i] == 0)
				aggreganceLabels[i].setBackground(Color.RED);
			
			// Add components to panel
			JPanel dest = propertiesLeft;
			if (i >= leftQuantity)
				dest = propertiesRight;
			
			dest.add(propertyLabels[i]);
			dest.add(prominenceLabels[i]);
			dest.add(aggreganceLabels[i]);
		}
		
		
		
		
		return propertiesPane;
	}
	
	class TextFieldListener implements DocumentListener
	{
		JTextField[] target;
		int index;
		
		public TextFieldListener(JTextField[] target, int index)
		{
			this.target = target;
			this.index = index;
		}
		
		public void changedUpdate(DocumentEvent e) {}
		public void insertUpdate(DocumentEvent e) { update(); }
		public void removeUpdate(DocumentEvent e) { update(); }
		
		private void update()
		{
			double x;
			try
			{
				x = Double.parseDouble(target[index].getText());
				if (x == 0)
					throw new Exception();
				
				target[index].setBackground(Color.WHITE);
				
				
			} catch (Exception e)
			{
				x = 0;
				target[index].setBackground(Color.RED);
			}
			
			if (target == prominenceLabels)
				phonology.prominence[index] = x;
			else if (target == aggreganceLabels)
				phonology.aggregance[index] = x;
		}
	}
}
