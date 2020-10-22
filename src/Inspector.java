import javax.swing.JFrame;

import quick.dbtable.DBTable;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JComboBox;

public class Inspector extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBTable table;
	private JTextField patArea;
	private DefaultListModel<String> listaPatentes;
	private JList<String> list;
	private JComboBox<String> comboBox;
	private JButton btnMulta;

	public Inspector(DBTable t) {
		listaPatentes = new DefaultListModel<String>();

		getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("Ingrese patentes");
		lblNewLabel.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblNewLabel.setBounds(49, 25, 254, 74);
		getContentPane().add(lblNewLabel);

		patArea = new JTextField();
		patArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
		patArea.setBounds(69, 86, 161, 32);
		getContentPane().add(patArea);
		patArea.setColumns(10);

		JButton btnAgregar = new JButton("AGREGAR");
		btnAgregar.setBounds(42, 129, 89, 23);
		btnAgregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				agregarPatente(listaPatentes, patArea.getText());
			}
		});
		getContentPane().add(btnAgregar);

		JButton btnSig = new JButton("SIGUIENTE");
		btnSig.setBounds(166, 129, 100, 23);
		btnSig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnAgregar.setEnabled(false);
				comboBox.setEnabled(true);
				ubicaciones();
				btnMulta.setEnabled(true);
			}
		});
		getContentPane().add(btnSig);

		list = new JList<String>();
		list.setBounds(42, 193, 224, 305);
		getContentPane().add(list);

		JLabel lblSeleccioneUbicacion = new JLabel("Seleccione ubicacion");
		lblSeleccioneUbicacion.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblSeleccioneUbicacion.setBounds(430, 25, 299, 74);
		getContentPane().add(lblSeleccioneUbicacion);

		comboBox = new JComboBox<String>();
		comboBox.setBounds(430, 87, 266, 32);
		comboBox.setEnabled(false);
		getContentPane().add(comboBox);

		btnMulta = new JButton("GENERAR MULTAS");
		btnMulta.setBounds(430, 164, 266, 39);
		btnMulta.setEnabled(false);
		getContentPane().add(btnMulta);
		table = t;

	}

	private void agregarPatente(DefaultListModel<String> listModel, String pat) {
		listModel.addElement(pat);
		list.setModel(listModel);
	}

	private void ubicaciones() {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select calle,altura from parquimetros");
			boolean fin = rs.next();
			while (fin) {
				comboBox.addItem(rs.getString("calle") + " " + rs.getString("altura"));
				fin = rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
