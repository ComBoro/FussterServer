package eu.fusster.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import net.comboro.Client;
import net.comboro.Server;
import eu.fusster.Fusster;
import eu.fusster.Loader;
import eu.fusster.command.CommandMap;
import eu.fusster.command.ConsoleCommandSender;
import eu.fusster.command.defaults.ThisCommand;
import eu.fusster.player.Player;
import eu.fusster.player.PlayerException;
import eu.fusster.player.PlayerManager;
import eu.fusster.plugin.FussterPlugin;
import eu.fusster.plugin.PluginException;
import eu.fusster.plugin.PluginMap;

public class BetterUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private static List<String> lastCommands = new ArrayList<>();
	private static final AttributeSet timeAset = StyleContext
			.getDefaultStyleContext().addAttribute(SimpleAttributeSet.EMPTY,
					StyleConstants.Foreground, Color.GRAY);
	private JList<String> clientsList;
	private JScrollPane clientsScrollPane;
	private JTabbedPane clientsTabbedPane;
	private JTextPane consoleTextPane;
	private JCheckBox debuggingCheckBox;
	private JButton jButton1;
	private JScrollPane jScrollPane1;
	private JTabbedPane consoleTabbedPane;
	private JTextField commandLine;
	private JMenuBar menuBar;
	private JMenu plugin;

	private JList<String> playersList;

	private JScrollPane playersScrollPane;

	private boolean debugging = false;

	private Image logoBlue;

	private int UPpressed = 0;

	// License stuff
	private boolean licenseOpened = false;

	private JScrollPane licenseScrollPane;

	// Creating plugin help page stuff
	private boolean pluginsTabOpened = false;

	private JScrollPane pluginHelpTabScrollPane;

	public BetterUI() {
		initComponents();

		Runtime.getRuntime().addShutdownHook(
				new Thread(() -> Fusster.shutdown(false)));
	}

	public void append(String str) {
		append(str, Color.BLACK);
	}

	public void append(final String str, final Color c) {
		if (str == null || str.trim().length() == 0)
			return;

		SwingUtilities.invokeLater(new Runnable() {

			AttributeSet aset = StyleContext.getDefaultStyleContext()
					.addAttribute(SimpleAttributeSet.EMPTY,
							StyleConstants.Foreground, c);

			@Override
			public void run() {
				try {
					if (consoleTextPane == null)
						return;

					Document doc = consoleTextPane.getDocument();
					String time = "[ "
							+ DateFormat.getTimeInstance().format(
									new Date(System.currentTimeMillis()))
							+ " ] ";

					doc.insertString(doc.getLength(), time, timeAset);

					String line = str + System.lineSeparator();

					doc.insertString(doc.getLength(), line, aset);

					Fusster.log(time + line);

					consoleTextPane.setCaretPosition(consoleTextPane
							.getDocument().getLength());
				} catch (BadLocationException e) {
					append(str, c);
				}
			}

		});
	}

	public void clearCommandLine() {
		commandLine.setText("");
	}

	private void debuggingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
		setDebugging(debuggingCheckBox.isSelected());
	}

	public JTabbedPane getConsoleTabbedPane() {
		return consoleTabbedPane;
	}

	private void initComponents() {

		// Out & Err
		try {
			PrintStream out = new PrintStream(new ConsoleOutputStream(
					Color.black), true, "UTF-8");
			System.setOut(out);

			PrintStream err = new PrintStream(new ConsoleOutputStream(
					Fusster.error), true, "UTF-8");
			System.setErr(err);
		} catch (IOException e) {
			e.printStackTrace();
		}

		logoBlue = Loader.loadImage("/res/logoNoBG25x25.png");
		setIconImage(logoBlue);

		clientsTabbedPane = new JTabbedPane();
		playersScrollPane = new JScrollPane();
		playersList = new JList<>();
		playersList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			Font font = new Font("helvitica", Font.BOLD, 12);

			@Override
			public Component getListCellRendererComponent(JList<?> list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {

				JLabel label = (JLabel) super.getListCellRendererComponent(
						list, value, index, isSelected, cellHasFocus);
				label.setIcon(new ImageIcon(logoBlue));
				label.setHorizontalTextPosition(SwingConstants.RIGHT);
				label.setFont(font);
				return label;
			}
		});

		clientsScrollPane = new JScrollPane();
		clientsList = new JList<>();
		consoleTabbedPane = new JTabbedPane();
		jScrollPane1 = new JScrollPane();
		consoleTextPane = new JTextPane();
		jButton1 = new JButton();
		commandLine = new JTextField();
		debuggingCheckBox = new JCheckBox();
		menuBar = new JMenuBar();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Fusster");
		setMinimumSize(new java.awt.Dimension(880, 660));
		setPreferredSize(new java.awt.Dimension(880, 660));

		consoleTextPane.setEditable(false);

		playersScrollPane.setToolTipText("Displays all the registered players");

		playersScrollPane.setViewportView(playersList);

		clientsTabbedPane.addTab("Players", playersScrollPane);

		clientsList.setToolTipText("Shows all the connected clients");
		clientsScrollPane.setViewportView(clientsList);

		clientsTabbedPane.addTab("Clients", clientsScrollPane);

		jScrollPane1.setViewportView(consoleTextPane);

		consoleTabbedPane.addTab("Console", jScrollPane1);

		jButton1.setText("Execute");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});

		debuggingCheckBox.setText("Debugging");
		debuggingCheckBox
				.addActionListener(new java.awt.event.ActionListener() {
					@Override
					public void actionPerformed(java.awt.event.ActionEvent evt) {
						debuggingCheckBoxActionPerformed(evt);
					}
				});

		commandLine.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				if (code == KeyEvent.VK_ENTER)
					onButtonClick();
				else if (code == KeyEvent.VK_UP) {
					commandLine.setText(onArrowClick(true));
				} else if (code == KeyEvent.VK_DOWN) {
					commandLine.setText(onArrowClick(false));
				}
			}
		});

		playersList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {

					if (PlayerManager.getPlayers().size() >= 1) {

						@SuppressWarnings("rawtypes")
						JList list = (JList) e.getSource();
						final int row = list.locationToIndex(e.getPoint());
						list.setSelectedIndex(row);

						JPopupMenu pm = new JPopupMenu();

						JMenuItem kickMenuItem = new JMenuItem("Kick");
						JMenuItem messageMenuItem = new JMenuItem("Message");

						kickMenuItem.addActionListener(ae -> {
							try {
								String playerName = PlayerManager.getPlayers()
										.get(row).getName();
								Player player = PlayerManager.get(playerName);
								PlayerManager.removePlayer(player,
										"Kicked by Console");
							} catch (PlayerException e1) {
								append("Not working :(");
							}
						});

						messageMenuItem.addActionListener(ae -> {
							Player p = PlayerManager.getPlayers().get(row);
							String toSend = JOptionPane.showInputDialog(null,
									"Send Message",
									"Send a message to " + p.getName(),
									JOptionPane.INFORMATION_MESSAGE);
							if (toSend != null)
								p.sendMessage(toSend);
						});

						pm.add(kickMenuItem);
						pm.add(messageMenuItem);
						list.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								pm.show(e.getComponent(), e.getX(), e.getY());
							}
						});
					}
				}
			}
		});

		menuBar = new JMenuBar();

		JMenu thisMenu = new JMenu("Server");

		JMenuItem internalRestart = new JMenuItem("Internal Restart");
		internalRestart.addActionListener(ae -> {
			PlayerManager.disconnectAll("Server internaly restarting.");
			Fusster.getPluginLoader().reloadAll();
			ThisCommand.clear();
		});
		thisMenu.add(internalRestart);

		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(ae -> {
			Fusster.shutdown(true);
		});
		thisMenu.add(exit);

		menuBar.add(thisMenu);

		// Plugin Menu
		plugin = new JMenu("Plugin");

		JMenuItem plugin_import = new JMenuItem("Import");
		plugin_import.addActionListener(ae -> {
			if (Fusster.getPluginLoader() == null)
				return;
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.showOpenDialog(null);
			File file = fc.getSelectedFile();
			try {
				if (file != null)
					Fusster.getPluginLoader().load(file);
			} catch (PluginException e1) {
				Fusster.error(e1.getMessage());
			}
		});
		plugin.add(plugin_import);

		JMenuItem plugin_importAll = new JMenuItem("Import directory");
		plugin_importAll.addActionListener(ae -> {
			if (Fusster.getPluginLoader() == null)
				return;
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.showOpenDialog(null);
			File file = fc.getSelectedFile();
			if (file != null)
				Fusster.getPluginLoader().loadAll(file);
		});
		plugin.add(plugin_importAll);

		plugin.addSeparator();

		JMenuItem plugin_loadAll = new JMenuItem("Load all");
		plugin_loadAll.addActionListener(ae -> {
			if (Fusster.getPluginLoader() != null)
				Fusster.getPluginLoader().loadAll();
		});
		plugin.add(plugin_loadAll);

		JMenuItem plugin_reloadAll = new JMenuItem("Reload all");
		plugin_reloadAll.addActionListener(ae -> {
			if (Fusster.getPluginLoader() != null)
				Fusster.getPluginLoader().reloadAll();
		});
		plugin.add(plugin_reloadAll);

		JMenuItem plugin_unloadAll = new JMenuItem("Unload all");
		plugin_unloadAll.addActionListener(ae -> {
			if (Fusster.getPluginLoader() != null)
				Fusster.getPluginLoader().unloadAll();
		});
		plugin.add(plugin_unloadAll);

		plugin.addSeparator();

		updatePluginsPane();

		menuBar.add(plugin);

		JMenu help = new JMenu("Help");

		JMenuItem legal = new JMenuItem("Legal");
		legal.addActionListener(ae -> {
			openLicense();
		});
		help.add(legal);

		JMenuItem pluginsTab = new JMenuItem("Creating a plugin");
		pluginsTab.addActionListener(ae -> {
			openPluginsTab();
		});
		help.add(pluginsTab);

		menuBar.add(help);

		setJMenuBar(menuBar);

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(clientsTabbedPane,
										GroupLayout.PREFERRED_SIZE, 190,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(consoleTabbedPane)
												.addGroup(
														GroupLayout.Alignment.TRAILING,
														layout.createSequentialGroup()
																.addComponent(
																		debuggingCheckBox)
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.RELATED,
																		488,
																		Short.MAX_VALUE)
																.addComponent(
																		jButton1))
												.addComponent(commandLine))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		consoleTabbedPane)
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.UNRELATED)
																.addComponent(
																		commandLine,
																		GroupLayout.PREFERRED_SIZE,
																		30,
																		GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.UNRELATED)
																.addGroup(
																		layout.createParallelGroup(
																				GroupLayout.Alignment.BASELINE)
																				.addComponent(
																						jButton1)
																				.addComponent(
																						debuggingCheckBox)))
												.addComponent(
														clientsTabbedPane,
														GroupLayout.Alignment.TRAILING,
														GroupLayout.DEFAULT_SIZE,
														621, Short.MAX_VALUE))
								.addContainerGap()));

		pack();
		setVisible(true);
	}

	public void updatePluginsPane() {
		int count = plugin.getItemCount();
		final int defaultOnes = 7;

		count -= defaultOnes;

		if (count < 0)
			return;
		else {
			// Clear
			for (int i = 0; i < count; i++)
				plugin.remove(defaultOnes + i);

			// Add all
			PluginMap pmap = Fusster.getPluginMap();
			if (pmap != null)
				for (FussterPlugin plg : pmap.getPlugins())
					plugin.add(genMenu(plg));
		}

	}

	private JMenu genMenu(FussterPlugin plg) {
		JMenu temp = new JMenu(plg.getDescription().getName());

		JMenuItem reload = new JMenuItem("Reload");
		reload.addActionListener(ae ->{
			if(Fusster.getPluginMap().getPlugins().contains(plg))
			Fusster.getPluginLoader().reload(plg);
			else plugin.remove(temp);
		});
		temp.add(reload);

		JMenuItem unload = new JMenuItem("Unload");
		unload.addActionListener(ae -> {
			if(Fusster.getPluginMap().getPlugins().contains(plg))
			plugin.remove(temp);
			else plugin.remove(temp);

		});
		temp.add(unload);

		return temp;
	}

	public boolean isDebugging() {
		return debugging;
	}

	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
		onButtonClick();
	}

	private String onArrowClick(boolean isUParrow) {
		int size = lastCommands.size();
		if (size == 0)
			return "";
		if (isUParrow) {
			UPpressed++;

			int result = size - UPpressed;

			if (result < 0) {
				UPpressed--;
				try {
					return lastCommands.get(0);
				} catch (IndexOutOfBoundsException e) {
					return "";
				}
			}

			return lastCommands.get(result);
		} else {
			UPpressed--;
			int arraySize = size - 1;

			int result = size - UPpressed;

			if (result > arraySize) {
				UPpressed++;
				try {
					return lastCommands.get(arraySize);
				} catch (IndexOutOfBoundsException e) {
					return "";
				}
			}

			try {
				return lastCommands.get(result);
			} catch (IndexOutOfBoundsException e) {
				return "";
			}
		}
	}

	private void onButtonClick() {
		String command = commandLine.getText().trim();
		CommandMap.dispatch(ConsoleCommandSender.getInstance(), command);
		clearCommandLine();
		if (!(command == null || command.equals("")))
			lastCommands.add(command);
	}

	private void openLicense() {
		SwingUtilities
				.invokeLater(() -> {
					try {
						if (!licenseOpened) {
							if (licenseScrollPane == null) {
								JTextPane tf = new JTextPane();
								tf.setEditable(false);
								licenseScrollPane = new JScrollPane(tf);
								Document doc = tf.getDocument();

								AttributeSet simple = new SimpleAttributeSet();

								InputStreamReader isr = new InputStreamReader(
										Class.class
												.getResourceAsStream("/license.txt"));
								BufferedReader buff = new BufferedReader(isr);

								String line = "";

								while ((line = buff.readLine()) != null) {
									doc.insertString(doc.getLength(), line
											+ System.lineSeparator(), simple);
								}

								StyledDocument docm = tf.getStyledDocument();
								SimpleAttributeSet center = new SimpleAttributeSet();
								StyleConstants.setAlignment(center,
										StyleConstants.ALIGN_CENTER);
								docm.setParagraphAttributes(0, doc.getLength(),
										center, false);

								tf.setCaretPosition(0);

								licenseScrollPane = new JScrollPane(tf);
							}

							consoleTabbedPane.add("Legal", licenseScrollPane);
						} else {
							// Close license
							consoleTabbedPane.remove(licenseScrollPane);
						}

						licenseOpened = !licenseOpened;

					} catch (Exception e) {
						e.printStackTrace();
					}
				});
	}

	private void openPluginsTab() {
		SwingUtilities
				.invokeLater(() -> {
					try {
						if (!pluginsTabOpened) {
							if (pluginHelpTabScrollPane == null) {

								InputStreamReader isr = new InputStreamReader(
										Class.class
												.getResourceAsStream("/pluginsHelp/CreatingPlugins.html"));
								BufferedReader buff = new BufferedReader(isr);

								String text = "", line;

								while ((line = buff.readLine()) != null) {
									text += line;
								}

								JEditorPane epane = new JEditorPane(
										"text/html", text);
								epane.setEditable(false);
								pluginHelpTabScrollPane = new JScrollPane(epane);
							}

							consoleTabbedPane.add("Creating a plugin",
									pluginHelpTabScrollPane);
						} else {
							consoleTabbedPane.remove(pluginHelpTabScrollPane);
						}

						pluginsTabOpened = !pluginsTabOpened;
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
	}

	public void setDebugging(boolean debugging) {
		debuggingCheckBox.setSelected(debugging);
		Fusster.setDebugging(debugging);
		if (debugging) {
			append("Debugging is turned on.", Color.GREEN);
		} else if (this.debugging) {
			append("Debugging is turned off.", Color.RED);
		}
		this.debugging = debugging;

	}

	public void updateClientsPane() {
		List<Client> clients = Server.getClients();
		String[] listData = new String[clients.size()];

		for (int i = 0; i < clients.size(); i++) {
			Client client = clients.get(i);
			listData[i] = client.getIP() + " ["
					+ client.dateCreated().toString() + "]";
		}

		clientsList.setListData(listData);
	}

	public void updatePlayersPane() {
		Fusster.setProperty("players",
				String.valueOf(PlayerManager.getPlayers().size()));

		List<Player> players = PlayerManager.getPlayers();
		String[] listData = new String[players.size()];

		for (int i = 0; i < players.size(); i++)
			listData[i] = players.get(i).getName();

		playersList.setListData(listData);
	}
}
