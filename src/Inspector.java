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
import java.sql.Types;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import java.awt.Color;

public class Inspector extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBTable table;
	private Login login;
	private String legajo;
	private JTextField patArea;
	private DefaultListModel<String> listaPatentes;
	private JList<String> list;
	private JComboBox<String> ubicaciones;
	private JComboBox<Integer> cbParq;
	private JButton btnMulta, btnRegresar, btnAgregar, btnSig;
	private JLabel lblIngPatente, lblSeleccioneUbicacion, lblMultas, lblPatentes, lblSeleccioneParquimetro, lblRegresar;

	public Inspector(Login prev, DBTable t, String l) {
		getContentPane().setForeground(Color.BLACK);
		login = prev;
		table = t;
		table.setEditable(false);
		legajo = l;
		listaPatentes = new DefaultListModel<String>();
		getContentPane().setLayout(null);

		lblIngPatente = new JLabel("Ingrese patentes");
		lblIngPatente.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblIngPatente.setBounds(42, 36, 254, 74);
		getContentPane().add(lblIngPatente);

		patArea = new JTextField();
		patArea.setFont(new Font("Tahoma", Font.PLAIN, 14));
		patArea.setBounds(69, 121, 161, 32);
		getContentPane().add(patArea);
		patArea.setColumns(10);

		btnAgregar = new JButton("AGREGAR");
		btnAgregar.setBounds(42, 164, 89, 23);
		btnAgregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				agregarPatente(listaPatentes, patArea.getText());
				patArea.setText("");
			}
		});
		getContentPane().add(btnAgregar);

		btnSig = new JButton("SIGUIENTE");
		btnSig.setBounds(166, 164, 100, 23);
		btnSig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnAgregar.setEnabled(false);
				ubicaciones.setEnabled(true);
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

		lblSeleccioneUbicacion = new JLabel("Seleccione ubicación");
		lblSeleccioneUbicacion.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblSeleccioneUbicacion.setBounds(313, 36, 299, 74);
		getContentPane().add(lblSeleccioneUbicacion);

		ubicaciones = new JComboBox<String>();
		ubicaciones.setBounds(313, 122, 266, 32);
		ubicaciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String aux = (String) ubicaciones.getSelectedItem();
				String[] calle_alt = getCalleAltura(aux);
				cbParq.setEnabled(true);
				cbParq.removeAllItems();
				id_parquimetros(calle_alt[0], calle_alt[1]);
			}
		});
		ubicaciones.setEnabled(false);

		getContentPane().add(ubicaciones);

		btnMulta = new JButton("GENERAR MULTAS");
		btnMulta.setBounds(488, 167, 266, 39);
		btnMulta.setEnabled(false);
		btnMulta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				accionGenerarMulta(arg0);
			}

		});
		getContentPane().add(btnMulta);

		lblPatentes = new JLabel("Patentes");
		lblPatentes.setHorizontalAlignment(SwingConstants.CENTER);
		lblPatentes.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblPatentes.setBounds(69, 184, 161, 74);
		getContentPane().add(lblPatentes);

		table.setBounds(315, 245, 633, 305);
		getContentPane().add(table);

		lblMultas = new JLabel("Multas");
		lblMultas.setHorizontalAlignment(SwingConstants.CENTER);
		lblMultas.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblMultas.setBounds(280, 184, 161, 74);
		getContentPane().add(lblMultas);

		lblSeleccioneParquimetro = new JLabel("Seleccione parquimetro");
		lblSeleccioneParquimetro.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 22));
		lblSeleccioneParquimetro.setBounds(622, 36, 313, 74);
		getContentPane().add(lblSeleccioneParquimetro);

		cbParq = new JComboBox<Integer>();
		cbParq.setEnabled(false);
		cbParq.setBounds(622, 122, 266, 32);
		getContentPane().add(cbParq);

		btnRegresar = new JButton("");
		btnRegresar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				regresarLogin();

			}
		});
		btnRegresar.setBounds(10, 11, 35, 23);
		getContentPane().add(btnRegresar);

		lblRegresar = new JLabel("Regresar");
		lblRegresar.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		lblRegresar.setHorizontalAlignment(SwingConstants.CENTER);
		lblRegresar.setBounds(42, 10, 77, 24);
		getContentPane().add(lblRegresar);

	}

	/*
	 * A partir de la calle y altura seleccionadas, se crea una lista con las
	 * patentes estacionadas en esa ubicacion. Luego, compara las patentes
	 * ingresadas por el inspector con las patentes estacionadas y genera una lista
	 * con las patentes infractoras. Se registra el acceso del inspector al
	 * parquimetro. Si el inspector puede labrar multas, las hace y se muestran por
	 * pantalla. Sino, se notifica que no puede.
	 */
	private void accionGenerarMulta(ActionEvent arg0) {
		String aux = (String) ubicaciones.getSelectedItem();
		String[] calle_alt = getCalleAltura(aux);
		int id_parq = (Integer) cbParq.getSelectedItem();
		DefaultListModel<String> estacionadas = new DefaultListModel<String>();
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select patente from estacionados where calle = '" + calle_alt[0]
					+ "' and altura = '" + calle_alt[1] + "'");
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

			st.executeUpdate("insert into accede(legajo,id_parq,fecha,hora) values (" + legajo + "," + id_parq + ",'"
					+ fechaActual + "','" + horaActual.toString() + "')");

			if (checkInspector(calle_alt[0], calle_alt[1], fechaActual, horaActual)) {
				for (int i = 0; i < patentesInfractoras.size(); i++) {
					labrarMulta(patentesInfractoras.elementAt(i), calle_alt[0], calle_alt[1], fechaActual.toString(),
							horaActual.toString());
				}
				table.setSelectSql(
						"select numero as 'Nro de Multa',fecha as 'Fecha',hora as 'Hora',calle as 'Calle',altura as 'Altura',patente as 'Patente',legajo as 'Legajo' from multa natural join asociado_con where fecha = curdate() and hora = '"
								+ horaActual + "' and legajo = '" + legajo + "' order by patente");
				table.createColumnModelFromQuery();
				for (int i = 0; i < table.getColumnCount(); i++) {
					if (table.getColumn(i).getType() == Types.TIME) {
						table.getColumn(i).setType(Types.CHAR);
					}
					if (table.getColumn(i).getType() == Types.DATE) {
						table.getColumn(i).setDateFormat("dd/MM/YYYY");
					}
					table.getColumn(i).setPreferredWidth(90);
					table.getColumn(i).setResizable(false);
				}

				table.refresh();
				btnMulta.setEnabled(false);

			} else {
				JOptionPane.showMessageDialog(getContentPane(), "Ud no se encuentra habilitado para labrar multas",
						"FUERA DE HORARIO", JOptionPane.WARNING_MESSAGE);

			}

			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Dado el String del formato "<nombre_calle> <altura>", devuelve un arreglo con
	 * dos elementos: el nombre de la calle y la altura. Lo usamos para cuando la
	 * calle tiene mas de una palabra y no podemos separarlo a partir de los
	 * espacios
	 */
	private String[] getCalleAltura(String aux) {
		String[] calle_altura = aux.split(" ");
		String[] salida = new String[2];
		String calle = "";
		for (int i = 0; i < calle_altura.length - 1; i++) {
			calle += calle_altura[i];
			calle += " ";
		}
		calle = calle.substring(0, calle.length() - 1);
		String altura = calle_altura[calle_altura.length - 1];
		salida[0] = calle;
		salida[1] = altura;
		return salida;
	}

	/*
	 * Dada una patente "pat", la agrega a la lista listModel. La patente deberá
	 * cumplir con el formato <3 letras minusculas><3 numeros>
	 */
	private void agregarPatente(DefaultListModel<String> listModel, String pat) {
		Pattern p = Pattern.compile("[A-Z]{3}[0-9]{3}");
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

	/*
	 * Agrega al comboBox las ubicaciones disponibles del inspector que accede
	 */
	private void ubicaciones() {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select calle,altura from asociado_con where legajo = '" + legajo + "'");
			boolean fin = rs.next();
			while (fin) {
				String calle_alt = rs.getString("calle") + " " + rs.getString("altura");
				if (!ub_repetida(ubicaciones, calle_alt))
					ubicaciones.addItem(calle_alt);
				fin = rs.next();
			}

			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void id_parquimetros(String calle, String altura) {
		Connection c = table.getConnection();

		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(
					"select id_parq from parquimetros where calle = '" + calle + "' and altura = '" + altura + "'");
			boolean fin = rs.next();
			int id_parq;
			while (fin) {
				id_parq = rs.getInt("id_parq");
				cbParq.addItem(id_parq);
				fin = rs.next();
			}

		} catch (SQLException e) {
			System.out.println("error al cargar parquimetros");
		}

	}

	/*
	 * Verifica que no se agreguen dos ubicaciones iguales al comboBox.
	 */
	private boolean ub_repetida(JComboBox<String> cb, String calle_alt) {
		boolean salida = false;

		for (int i = 0; i < cb.getItemCount(); i++)
			if (cb.getItemAt(i).equals(calle_alt))
				salida = true;
		return salida;
	}

	/*
	 * Dada la lista de patentes que ingresó el inspector y la lista de patentes
	 * estacionadas en la ubicacion indicada, genera una lista con las patentes
	 * infractoras
	 */
	private DefaultListModel<String> patentesMulta(DefaultListModel<String> ingresadas,
			DefaultListModel<String> estacionados) {
		DefaultListModel<String> l = new DefaultListModel<String>();

		for (int i = 0; i < ingresadas.size(); i++) {
			if (!esta(ingresadas.getElementAt(i), estacionados))
				l.addElement(ingresadas.getElementAt(i));
		}

		return l;
	}

	/*
	 * Verifica si un String s está en una lista dada
	 */
	private boolean esta(String s, DefaultListModel<String> lista) {
		for (int i = 0; i < lista.size(); i++) {
			if (lista.getElementAt(i).equals(s))
				return true;
		}
		return false;
	}

	/*
	 * Dada una patente,ubicacion,fecha y hora, genera una multa y la agrega a la
	 * base de datos
	 */
	private void labrarMulta(String patente, String calle, String altura, String fecha, String hora) {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select id_asociado_con from asociado_con where legajo = '" + legajo
					+ "' and calle = '" + calle + "' and altura = " + altura);
			rs.next();
			int id = rs.getInt("id_asociado_con");
			if (existePatente(patente))
				st.executeUpdate("insert into multa(fecha,hora,patente,id_asociado_con) values ('" + fecha + "','"
						+ hora + "','" + patente + "'," + id + ")");

			rs.close();
			st.close();

		} catch (SQLException e) {
			System.out.println("flashé en labrar multa");
		}

	}

	private boolean existePatente(String pat) {
		Connection c = table.getConnection();
		boolean encontre = false;
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select patente from automoviles");
			boolean fin = rs.next();
			while (fin && !encontre) {
				encontre = rs.getString("patente").equals(pat);
				fin = rs.next();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Flashé en existePatente");
		}

		return encontre;
	}

	/*
	 * Verifica si el inspector puede hacer multas. Si el dia y turno actual
	 * corresponden a los horarios de trabajo del inspector, entonces devuelve true
	 * y queda habilitado para labrar multas
	 */
	@SuppressWarnings("deprecation")
	private boolean checkInspector(String calle, String altura, java.sql.Date fecha, Time hora) {

		String diaActual = null;
		String turnoActual = null;
		DefaultListModel<String> dias = new DefaultListModel<String>();
		DefaultListModel<String> turnos = new DefaultListModel<String>();
		boolean salida1 = false;
		boolean salida2 = false;
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select dia,turno from asociado_con where legajo = '" + legajo
					+ "' and calle = '" + calle + "' and altura = '" + altura + "'");
			boolean fin = rs.next();
			while (fin) {
				dias.addElement(rs.getString("dia"));
				turnos.addElement(rs.getString("turno"));
				fin = rs.next();
			}

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
			else if (hora.getHours() >= 14 && hora.getHours() <= 20
					&& !(hora.getHours() == 20 && hora.getMinutes() > 0))
				turnoActual = "t";
			else {
				JOptionPane.showMessageDialog(getContentPane(), "Se encuentra fuera de horario", "FUERA DE HORARIO",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}

		} catch (SQLException e) {
			System.out.println("le erre en la consulta del checkInspector");

		}
		for (int i = 0; i < dias.getSize(); i++) {
			if (dias.getElementAt(i).equals(diaActual))
				salida1 = true;
		}

		for (int i = 0; i < turnos.getSize() && salida1; i++) {
			if (turnos.getElementAt(i).equals(turnoActual))
				salida2 = true;
		}

		return salida1 && salida2;
	}

	private void regresarLogin() {
		try {
			table.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		login = new Login();
		login.setSize(600, 400);
		login.setResizable(false);
		login.setLocationRelativeTo(null);
		login.setVisible(true);
		login.setDefaultCloseOperation(EXIT_ON_CLOSE);

		this.dispose();

	}
}
