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
		legajo = l;
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
				Fechas f = new Fechas();
				String[] selected = ((String) comboBox.getSelectedItem()).split(" ");
				DefaultListModel<String> estacionadas = new DefaultListModel<String>();
				Connection c = table.getConnection();
				try {
					Statement st = c.createStatement();
					ResultSet rs = st.executeQuery("select patente from estacionados where calle = '" + selected[0]
							+ "' and altura = '" + selected[1] + "'");
					boolean fin = rs.next();
					while (fin) {
						estacionadas.addElement(rs.getString("patente"));
						fin = rs.next();
					}
					DefaultListModel<String> patentesInfractoras = patentesMulta(listaPatentes, estacionadas);

					java.util.Date fechaActual = new java.util.Date(System.currentTimeMillis());

					if (checkInspector(selected[0], selected[1], fechaActual)) {
						rs = st.executeQuery("select id_parq from parquimetros where calle = '" + selected[0]
								+ "' and altura = '" + selected[1] + "'");
						rs.next();
						int id_parq = rs.getInt("id_parq");
						String fecha = f.convertirDateAString(fechaActual);
						String hora = fechaActual.getHours() + ":" + fechaActual.getMinutes() + ":"
								+ fechaActual.getSeconds();
						rs = st.executeQuery("insert into accede(legajo,id_parq,fecha,hora) values (" + legajo + ","
								+ id_parq + ",'" + fecha + "','" + hora + "'");

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
			if (esta(ingresadas.getElementAt(i), estacionados))
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

	private void labrarMulta(String patente) {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(
					"select numero as 'Numero de Multa',fecha,hora,calle,altura,patente,legajo from multa natural join asociado_con where patente = '"
							+ patente + "'");
			boolean fin = rs.next();
			while (fin) {
				System.out.println("Multa labrada:");
				System.out.println("Nro de multa: " + rs.getInt("Numero de Multa"));
				System.out.println("Fecha: " + rs.getDate("fecha"));
				System.out.println("Hora: " + rs.getTime("hora"));
				System.out.println("Calle: " + rs.getString("calle"));
				System.out.println("Altura: " + rs.getInt("altura"));
				System.out.println("Patente: " + rs.getString("patente"));
				System.out.println("Legajo del inspector: " + rs.getString("legajo"));
			}

		} catch (SQLException e) {

		}

	}

	private boolean checkInspector(String calle, String altura, java.util.Date fecha) {
		Fechas f = new Fechas();
		String diaActual = null;
		String turnoActual = null;
		String dia = null;
		String turno = null;

		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select dia,turno from asociado_con where legajo = '" + legajo
					+ "' and calle = '" + calle + "' and altura = '" + altura + "'");

			boolean tiene = rs.next();
			System.out.println("llegue a ejecutar la primera consulta");
			System.out.println(tiene);
			dia = rs.getString("dia");// "do"
			System.out.println(dia);
			turno = rs.getString("turno");// "t"
			System.out.println(turno);
			java.sql.Date fechaSQL = f.convertirDateADateSQL(fecha);
			rs = st.executeQuery("select dayofweek ('" + fechaSQL + "')");
			rs.next();
			int nroDia = rs.getInt("dayofweek ('" + fechaSQL + "')");

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
			System.out.println("estoy por entrar al if");
			if (fecha.getHours() <= 13 && fecha.getMinutes() <= 59 && fecha.getHours() >= 8)
				turnoActual = "m";
			else if (fecha.getHours() >= 14 && fecha.getHours() <= 20)
				turnoActual = "t";
			else
				JOptionPane.showMessageDialog(getContentPane(), "Está fuera de horario", "FUERA DE HORARIO",
						JOptionPane.WARNING_MESSAGE);
			System.out.println("sali del if pa");

		} catch (SQLException e) {

		}

		return dia.equals(diaActual) && turno.equals(turnoActual);
	}
}
