import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import quick.dbtable.DBTable;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JRadioButton;

public class Login extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField user;
	private JPasswordField password;
	private ButtonGroup botones;
	private JRadioButton btnAdmin, btnInspec, btnTarjeta;
	private JLabel titulo, lblUser, lblPass;
	private JButton btnLogin;
	private Admin adm; // Ventana administrador
	private Inspector ins; // Ventana inspector
	private Tarjetas tar; // Ventana tarjetas
	public DBTable table; // Es la tabla que usaremos para la conexion a la BD
	private String[] datos; // Es para ponerle el nombre del inspector al titulo de su ventana

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Login login = new Login();
				login.setSize(600, 400);
				login.setResizable(false);
				login.setLocationRelativeTo(null);
				login.setVisible(true);
				login.setDefaultCloseOperation(EXIT_ON_CLOSE);
			}
		});

	}

	public Login() {
		setTitle("App Parquimetros");
		getContentPane().setLayout(null);

		titulo = new JLabel("\u00A1BIENVENIDO A LA APP PARQUIMETROS!");
		titulo.setHorizontalAlignment(SwingConstants.CENTER);
		titulo.setFont(new Font("Copperplate Gothic Light", Font.BOLD, 21));
		titulo.setBounds(10, 31, 568, 63);
		getContentPane().add(titulo);

		user = new JTextField();
		user.setBounds(232, 118, 132, 20);
		getContentPane().add(user);
		user.setColumns(10);

		password = new JPasswordField();
		password.setBounds(232, 176, 132, 20);
		getContentPane().add(password);

		table = new DBTable();

		lblUser = new JLabel("Usuario:");
		lblUser.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 14));
		lblUser.setBounds(154, 120, 68, 14);
		getContentPane().add(lblUser);

		lblPass = new JLabel("Contrase\u00F1a:");
		lblPass.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 14));
		lblPass.setBounds(120, 178, 102, 14);
		getContentPane().add(lblPass);

		btnLogin = new JButton("Iniciar sesi\u00F3n");
		btnLogin.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		btnLogin.setBounds(232, 224, 132, 23);
		btnLogin.setMnemonic(KeyEvent.VK_ENTER);
		getContentPane().add(btnLogin);

		btnAdmin = new JRadioButton("Ingreso como administrador", true);
		btnAdmin.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		btnAdmin.setBounds(232, 254, 284, 23);
		getContentPane().add(btnAdmin);

		btnInspec = new JRadioButton("Ingreso como inspector", false);
		btnInspec.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		btnInspec.setBounds(232, 282, 222, 23);
		getContentPane().add(btnInspec);

		btnTarjeta = new JRadioButton("Conexion de tarjeta", false);
		btnTarjeta.setFont(new Font("Copperplate Gothic Bold", Font.PLAIN, 11));
		btnTarjeta.setBounds(232, 308, 222, 23);
		getContentPane().add(btnTarjeta);

		botones = new ButtonGroup();
		botones.add(btnAdmin);
		botones.add(btnInspec);
		botones.add(btnTarjeta);

		btnLogin.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent arg0) {
				conectarBD(user.getText(), password.getText());
			}
		});

		btnAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnAdmin.isSelected())
					lblUser.setText("Usuario:");

			}
		});

		btnInspec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (btnInspec.isSelected())
					lblUser.setText("Legajo:");

			}
		});

	}

	/*
	 * Inicializa la base de datos, ingresando como admin o como inspector segun sea
	 * el usuario. Si es un inspector, se valida que los datos del mismo (legajo y
	 * password) sean los correctos. Cualquier error es notificado mediante un
	 * cartel.
	 */
	private void conectarBD(String user, String pw) {
		try {
			String driver = "com.mysql.cj.jdbc.Driver";
			String server = "localhost:3306";
			String bdd = "parquimetros";
			String url = "jdbc:mysql://" + server + "/" + bdd + "?serverTimezone=America/Argentina/Buenos_Aires";
			if (btnAdmin.isSelected() && user.equals("admin")) {
				table.connectDatabase(driver, url, user, pw);
				adminScreen();
			} else if (btnInspec.isSelected()) {
				table.connectDatabase(driver, url, "inspector", "inspector");
				if (checkInspector(user, pw))
					inspecScreen(user);
				else
					JOptionPane.showMessageDialog(getContentPane(), "Legajo o contraseña incorrectos", "ERROR",
							JOptionPane.WARNING_MESSAGE);

			} else if (btnTarjeta.isSelected() && user.equals("parquimetro") && pw.equals("parq")) {
				table.connectDatabase(driver, url, user, pw);
				tarjetaScreen();
			} else {
				JOptionPane.showMessageDialog(getContentPane(), "Usuario incorrecto. Intente nuevamente.", "ERROR",
						JOptionPane.WARNING_MESSAGE);
			}

		} catch (SQLException ex) {
			System.out.println("SQLException: " + ex.getMessage());
			JOptionPane.showMessageDialog(getContentPane(), "No fue posible conectarse a la base de datos.", "ERROR",
					JOptionPane.WARNING_MESSAGE);
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Verifica que el legajo (user) y el password (pw) correspondan a un inspector
	 * de la base de datos.
	 */
	private boolean checkInspector(String user, String pw) {
		boolean salida = true;
		Connection c = table.getConnection();
		try {
			Statement st = c.createStatement();
			ResultSet rs = st.executeQuery("select nombre,apellido from inspectores where legajo='" + user
					+ "' and password=md5('" + pw + "')");
			salida = rs.next();

			datos = new String[2];
			datos[0] = rs.getString("nombre");
			datos[1] = rs.getString("apellido");

			st.close();
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return salida;

	}

	/*
	 * Cambia a la ventana de la funcion del administrador y cierra la ventana
	 * inicial
	 */
	private void adminScreen() {
		adm = new Admin(this, table);
		adm.setTitle("Bienvenido administrador - App Parquimetros");
		adm.setSize(1000, 600);
		adm.setVisible(true);
		adm.setLocationRelativeTo(null);
		adm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.dispose();
	}

	/*
	 * Cambia a la ventana de la funcion del inspector y cierra la ventana inicial
	 */
	private void inspecScreen(String user) {
		ins = new Inspector(this, table, user);
		ins.setTitle("Bienvenido " + datos[0] + " " + datos[1] + " - App Parquimetros");
		ins.setSize(1000, 600);
		ins.setVisible(true);
		ins.setLocationRelativeTo(null);
		ins.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.dispose();
	}

	private void tarjetaScreen() {
		tar = new Tarjetas(this, table);
		tar.setSize(1000, 600);
		tar.setVisible(true);
		tar.setLocationRelativeTo(null);
		tar.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.dispose();
	}

	public void clear() {
		user.setText("");
		password.setText("");
		btnAdmin.setSelected(true);
		lblUser.setText("Usuario:");
		try {
			table.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		table = new DBTable();

	}

}
