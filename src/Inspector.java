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
import java.sql.Time;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;

public class Inspector extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBTable table;
	private String legajo;
	private JTextField patArea;
	private DefaultListModel<String> listaPatentes;
	private JList<String> list;
	private JComboBox<String> comboBox;
	private JButton btnMulta;

	public Inspector(DBTable t, String l) {
		table = t;
		table.setEditable(false);
		legajo = l;
		listaPatentes = new DefaultListModel<String>();
		getContentPane().setLayout(null);

		JLabel lblIngPatente = new JLabel("Ingrese patentes");
		lblIngPatente.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblIngPatente.setBounds(49, 25, 254, 74);
		getContentPane().add(lblIngPatente);

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

				patArea.setText("");
			}
		});
		getContentPane().add(btnAgregar);

		JButton btnSig = new JButton("SIGUIENTE");
		btnSig.setBounds(166, 129, 100, 23);
		btnSig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnAgregar.setEnabled(false);
				comboBox.setEnabled(true);
				patArea.setText("");
				patArea.setEnabled(false);
				ubicaciones();
				btnMulta.setEnabled(true);
				btnSig.setEnabled(false);
			}
		});
		getContentPane().add(btnSig);

		list = new JList<String>();
		list.setBounds(42, 245, 224, 305);
		getContentPane().add(list);

		JLabel lblSeleccioneUbicacion = new JLabel("Seleccione ubicación");
		lblSeleccioneUbicacion.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblSeleccioneUbicacion.setBounds(430, 25, 299, 74);
		getContentPane().add(lblSeleccioneUbicacion);

		comboBox = new JComboBox<String>();
		comboBox.setBounds(430, 86, 266, 32);
		comboBox.setEnabled(false);

		getContentPane().add(comboBox);

		btnMulta = new JButton("GENERAR MULTAS");
		btnMulta.setBounds(430, 164, 266, 39);
		btnMulta.setEnabled(false);
		btnMulta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String aux = (String) comboBox.getSelectedItem();
				String[] calle_altura = aux.split(" ");
				String calle = "";
				for (int i = 0; i < calle_altura.length - 1; i++) {
					calle += calle_altura[i];
					calle += " ";
				}
				calle = calle.substring(0, calle.length() - 1);
				String altura = calle_altura[calle_altura.length - 1];

				DefaultListModel<String> estacionadas = new DefaultListModel<String>();
				Connection c = table.getConnection();
				try {
					Statement st = c.createStatement();
					ResultSet rs = st.executeQuery("select patente from estacionados where calle = '" + calle
							+ "' and altura = '" + altura + "'");
					boolean fin = rs.next();
					while (fin) {
						estacionadas.addElement(rs.getString("patente"));
						fin = rs.next();
					}
					DefaultListModel<String> patentesInfractoras = patentesMulta(listaPatentes, estacionadas);

					rs = st.executeQuery("select curdate(),curtime()");
					rs.next();
					java.sql.Date fechaActual = rs.getDate("curdate()");
					Time horaActual = rs.getTime("curtime()");

					if (checkInspector(calle, altura, fechaActual, horaActual)) {
						rs = st.executeQuery("select id_parq from parquimetros where calle = '" + calle
								+ "' and altura = '" + altura + "'");
						rs.next();
						int id_parq = rs.getInt("id_parq");
						st.executeUpdate("insert into accede(legajo,id_parq,fecha,hora) values (" + legajo + ","
								+ id_parq + ",'" + fechaActual + "','" + horaActual.toString() + "')");

						for (int i = 0; i < patentesInfractoras.size(); i++) {
							labrarMulta(patentesInfractoras.elementAt(i), calle, altura, fechaActual.toString(),
									horaActual.toString());
						}
						table.setSelectSql(
								"select numero as 'Numero de Multa',fecha,hora,calle,altura,patente,legajo from multa natural join asociado_con");
						table.createColumnModelFromQuery();
						table.refresh();

						btnMulta.setEnabled(false);

					}

					rs.close();
					st.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}

		});
		getContentPane().add(btnMulta);

		JLabel lblPatentes = new JLabel("Patentes");
		lblPatentes.setHorizontalAlignment(SwingConstants.CENTER);
		lblPatentes.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblPatentes.setBounds(69, 184, 161, 74);
		getContentPane().add(lblPatentes);

		table.setBounds(315, 245, 637, 305);
		getContentPane().add(table);

		JLabel lblMultas = new JLabel("Multas");
		lblMultas.setHorizontalAlignment(SwingConstants.CENTER);
		lblMultas.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblMultas.setBounds(280, 184, 161, 74);
		getContentPane().add(lblMultas);

	}

	private void agregarPatente(DefaultListModel<String> listModel, String pat) {
		Pattern p = Pattern.compile("[a-z]{3}[0-9]{3}");
		Matcher m = p.matcher(pat);
		if (m.matches()) {
			listModel.addElement(pat);
			list.setModel(listModel);
		} else {
			JOptionPane.showMessageDialog(getContentPane(), "Ingrese una patente válida", "Patente inválida",
					JOptionPane.WARNING_MESSAGE);
			patArea.setText("");
		}
	}

	private void ubicaciones() {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select calle,altura from asociado_con where legajo = '" + legajo + "'");
			boolean fin = rs.next();
			while (fin) {
				comboBox.addItem(rs.getString("calle") + " " + rs.getString("altura"));
				fin = rs.next();
			}

			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private DefaultListModel<String> patentesMulta(DefaultListModel<String> ingresadas,
			DefaultListModel<String> estacionados) {
		DefaultListModel<String> l = new DefaultListModel<String>();

		for (int i = 0; i < ingresadas.size(); i++) {
			if (!esta(ingresadas.getElementAt(i), estacionados))
				l.addElement(ingresadas.getElementAt(i));
		}

		return l;
	}

	private boolean esta(String s, DefaultListModel<String> lista) {
		for (int i = 0; i < lista.size(); i++) {
			if (lista.getElementAt(i).equals(s))
				return true;
		}
		return false;
	}

	private void labrarMulta(String patente, String calle, String altura, String fecha, String hora) {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select id_asociado_con from asociado_con where legajo = '" + legajo
					+ "' and calle = '" + calle + "' and altura = " + altura);
			rs.next();
			int id = rs.getInt("id_asociado_con");
			st.executeUpdate("insert into multa(fecha,hora,patente,id_asociado_con) values ('" + fecha + "','" + hora
					+ "','" + patente + "'," + id + ")");

			rs.close();
			st.close();

		} catch (SQLException e) {

		}

	}

	@SuppressWarnings("deprecation")
	private boolean checkInspector(String calle, String altura, java.sql.Date fecha, Time hora) {

		String diaActual = null;
		String turnoActual = null;
		String dia = null;
		String turno = null;

		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select dia,turno from asociado_con where legajo = '" + legajo
					+ "' and calle = '" + calle + "' and altura = '" + altura + "'");
			rs.next();
			dia = rs.getString("dia");
			turno = rs.getString("turno");

			rs = st.executeQuery("select dayofweek ('" + fecha + "')");
			rs.next();
			int nroDia = rs.getInt("dayofweek ('" + fecha + "')");

			rs.close();
			st.close();
			switch (nroDia) {
			case 1:
				diaActual = "do";
				break;
			case 2:
				diaActual = "lu";
				break;
			case 3:
				diaActual = "ma";
				break;
			case 4:
				diaActual = "mi";
				break;
			case 5:
				diaActual = "ju";
				break;
			case 6:
				diaActual = "vi";
				break;
			case 7:
				diaActual = "sa";
				break;
			}

			if (hora.getHours() >= 8 && hora.getHours() <= 13 && !(hora.getHours() == 14 && hora.getMinutes() == 0))
				turnoActual = "m";
			else if (hora.getHours() >= 14 && hora.getHours() <= 20 && !(hora.getHours() == 20 && hora.getHours() > 0))
				turnoActual = "t";
			else
				JOptionPane.showMessageDialog(getContentPane(), "Está fuera de horario", "FUERA DE HORARIO",
						JOptionPane.WARNING_MESSAGE);

		} catch (SQLException e) {
			System.out.println("le erre en la consulta del checkInspector");

		}

		return dia.equals(diaActual) && turno.equals(turnoActual);
	}
}
