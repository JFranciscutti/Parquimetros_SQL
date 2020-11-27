import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import quick.dbtable.DBTable;
import javax.swing.JComboBox;

public class Tarjetas extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBTable table;
	private Login login;
	private JButton btnRegresar;
	private JLabel lblRegresar, lblUbicaciones, lblParquimetros, lblTarjetas;
	private JComboBox<String> ubicaciones;
	private JComboBox<Integer> tarjetas, parquimetros;
	private JButton btnAcceder;

	private int parq_actual, id_tarjeta_actual;

	public Tarjetas(Login prev, DBTable t) {
		table = t;
		login = prev;
		getContentPane().setLayout(null);

		table.setEditable(false);
		table.setBounds(17, 250, 950, 300);
		getContentPane().add(table);

		btnRegresar = new JButton("");
		btnRegresar.setForeground(Color.BLACK);
		btnRegresar.setBackground(Color.BLACK);
		btnRegresar.setIcon(new ImageIcon(
				Tarjetas.class.getResource("/com/sun/javafx/scene/control/skin/caspian/fxvk-backspace-button.png")));
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

		ubicaciones = new JComboBox<String>();
		ubicaciones.setBounds(69, 81, 319, 20);
		ubicaciones.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String[] calle_alt = getCalleAltura((String) ubicaciones.getSelectedItem());
				parquimetros.removeAllItems();
				cargarParquimetros(calle_alt[0], calle_alt[1]);
				parquimetros.setEnabled(true);
			}
		});
		getContentPane().add(ubicaciones);

		parquimetros = new JComboBox<Integer>();
		parquimetros.setBounds(492, 81, 319, 20);
		parquimetros.setEnabled(false);
		getContentPane().add(parquimetros);

		tarjetas = new JComboBox<Integer>();
		tarjetas.setBounds(364, 160, 319, 20);
		getContentPane().add(tarjetas);

		lblUbicaciones = new JLabel("Seleccione una ubicaci\u00F3n");
		lblUbicaciones.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 16));
		lblUbicaciones.setBounds(99, 56, 277, 23);
		getContentPane().add(lblUbicaciones);

		lblParquimetros = new JLabel("Seleccione un parquimetro");
		lblParquimetros.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 16));
		lblParquimetros.setBounds(517, 56, 294, 23);
		getContentPane().add(lblParquimetros);

		lblTarjetas = new JLabel("Seleccione una tarjeta");
		lblTarjetas.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 16));
		lblTarjetas.setBounds(114, 158, 240, 20);
		getContentPane().add(lblTarjetas);

		btnAcceder = new JButton("Acceder");
		btnAcceder.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		btnAcceder.setBounds(364, 216, 319, 23);
		btnAcceder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parq_actual = (Integer) parquimetros.getSelectedItem();
				id_tarjeta_actual = (Integer) tarjetas.getSelectedItem();
				Connection c = table.getConnection();
				try {
					Statement st = c.createStatement();
					ResultSet rs = st.executeQuery("call conectar(" + id_tarjeta_actual + ", " + parq_actual + ")");
					table.refresh(rs);
					for (int i = 0; i < table.getColumnCount(); i++) {
						table.getColumn(i).setPreferredWidth(310);
					}

				} catch (SQLException e) {
					JOptionPane.showMessageDialog(getContentPane(), "Error al ejecutar el stored procedure", "ERROR",
							JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		getContentPane().add(btnAcceder);

		cargarUbicaciones();
		cargarTarjetas();

	}

	private void cargarUbicaciones() {
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select calle,altura from ubicaciones");
			boolean fin = rs.next();
			while (fin) {
				String calle_alt = rs.getString("calle") + " " + rs.getString("altura");
				ubicaciones.addItem(calle_alt);
				fin = rs.next();
			}

			st.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void cargarParquimetros(String calle, String altura) {
		Connection c = table.getConnection();

		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery(
					"select id_parq from parquimetros where calle = '" + calle + "' and altura = '" + altura + "'");
			boolean fin = rs.next();
			int id_parq;
			while (fin) {
				id_parq = rs.getInt("id_parq");
				parquimetros.addItem(id_parq);
				fin = rs.next();
			}

			rs.close();
			st.close();

		} catch (SQLException e) {
			System.out.println("error al cargar parquimetros");
		}

	}

	private void cargarTarjetas() {
		Connection c = table.getConnection();

		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select id_tarjeta,patente from tarjetas");
			boolean fin = rs.next();
			while (fin) {
				tarjetas.addItem(rs.getInt("id_tarjeta"));
				fin = rs.next();
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			System.out.println("error al cargar tarjetas");

		}

	}

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
