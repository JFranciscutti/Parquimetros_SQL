import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import quick.dbtable.DBTable;

import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class Admin extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DBTable table;
	private JTextArea textArea;
	private JList<String> listTablas, listAtributos;
	private JButton btnEjecutar, btnBorrar;
	private JLabel lblTablas;
	private Login login;

	public Admin(Login prev, DBTable t) {
		table = t;
		login = prev;
		listAtributos = new JList<String>();
		listTablas = new JList<String>();

		listTablas.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

		listarTablas();

		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				listarAtributos(listTablas.getSelectedValue());
			}
		};
		listTablas.addMouseListener(mouseListener);

		getContentPane().setLayout(null);

		textArea = new JTextArea();
		textArea.setBounds(129, 12, 500, 70);
		getContentPane().add(textArea);

		table.setEditable(false);
		table.setBounds(0, 100, 600, 460);
		getContentPane().add(table);

		btnEjecutar = new JButton("Ejecutar");
		btnEjecutar.setBounds(655, 13, 89, 23);
		getContentPane().add(btnEjecutar);
		btnEjecutar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refrescarTabla();
			}
		});

		btnBorrar = new JButton("Borrar");
		btnBorrar.setBounds(655, 47, 89, 23);
		getContentPane().add(btnBorrar);
		btnBorrar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});

		lblTablas = new JLabel("Lista de tablas");
		lblTablas.setBounds(678, 81, 89, 14);
		getContentPane().add(lblTablas);

		JLabel lblNewLabel = new JLabel("Lista de atributos");
		lblNewLabel.setBounds(834, 79, 103, 14);
		getContentPane().add(lblNewLabel);

		JButton btnRegresar = new JButton("");
		btnRegresar.setForeground(Color.BLACK);
		btnRegresar.setBackground(Color.BLACK);
		btnRegresar.setIcon(new ImageIcon(
				Inspector.class.getResource("/com/sun/javafx/scene/control/skin/caspian/fxvk-backspace-button.png")));
		btnRegresar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				regresarLogin();

			}
		});
		btnRegresar.setBounds(10, 11, 35, 23);
		getContentPane().add(btnRegresar);

		JLabel lblRegresar = new JLabel("Regresar");
		lblRegresar.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		lblRegresar.setHorizontalAlignment(SwingConstants.CENTER);
		lblRegresar.setBounds(42, 10, 77, 24);
		getContentPane().add(lblRegresar);

	}

	/*
	 * Genera y muestra en la app la lista de tablas de la BD Parquimetros
	 */
	private void listarTablas() {
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("show tables");
			boolean fin = rs.next();
			while (fin) {
				listModel.addElement(rs.getString("Tables_in_parquimetros"));
				fin = rs.next();
			}
			listTablas.setModel(listModel);
			listTablas.setBounds(638, 100, 162, 272);
			getContentPane().add(listTablas);
			rs.close();
			st.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Dada la tabla seleccionada en la lista de tablas, muestra sus atributos en la
	 * app
	 */
	private void listarAtributos(String selected) {
		DefaultListModel<String> listModel = new DefaultListModel<String>();
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("describe " + selected);
			boolean fin = rs.next();
			while (fin) {
				listModel.addElement(rs.getString("Field"));
				fin = rs.next();
			}
			listAtributos.setModel(listModel);
			listAtributos.setBounds(804, 100, 162, 272);
			getContentPane().add(listAtributos);
			rs.close();
			st.close();

		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

	/*
	 * 
	 */
	private void refrescarTabla() {
		try {
			String sql = this.textArea.getText();
			String[] palabras = sql.split(" ");
			if (palabras[0].toLowerCase().equals("select")) {
				table.setSelectSql(sql.trim());
				table.createColumnModelFromQuery();
			} else {
				System.out.println("aca irian las consultas con execute()");
			}

			for (int i = 0; i < table.getColumnCount(); i++) {
				if (table.getColumn(i).getType() == Types.TIME) {
					table.getColumn(i).setType(Types.CHAR);
				}
				if (table.getColumn(i).getType() == Types.DATE) {
					table.getColumn(i).setDateFormat("dd/MM/YYYY");
				}
			}
			table.refresh();

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
			JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), ex.getMessage() + "\n",
					"Error al ejecutar la consulta.", JOptionPane.ERROR_MESSAGE);

		}

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
