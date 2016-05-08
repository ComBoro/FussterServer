/*
 * This file is part of Fusster.
 *	
 * Fusster Copyright (C) ComBoro
 *
 * Fusster is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Fusster is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Fusster.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.fusster.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import eu.fusster.Fusster;
import eu.fusster.Loader;
import eu.fusster.command.CommandMap;
import eu.fusster.command.ConsoleCommandSender;
import eu.fusster.player.Player;
import eu.fusster.player.PlayerException;
import eu.fusster.player.PlayerManager;
import eu.fusster.plugin.FussterPlugin;

public class ServerUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private JButton executeButton;
	private JScrollPane jScrollPane1;
	private JScrollPane jScrollPane2;
	@SuppressWarnings("rawtypes")
	private JList usernamePane;
	private JTextPane consolePane;
	private JTextField commandLine;
	private static JCheckBox debugCheckBox;
	private static boolean debugging = false;

	private static List<String> lastCommands = new ArrayList<String>();

	private int UPpressed = 0;

	private Image logoBlue;

	private static final AttributeSet timeAset = StyleContext
			.getDefaultStyleContext().addAttribute(SimpleAttributeSet.EMPTY,
					StyleConstants.Foreground, Color.GRAY);

	public ServerUI() {
		initComponents();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				Fusster.shutdown(false);
			}
		}));
	}
	
	public void clearCommandLine(){
		commandLine.setText("");
	}

	@SuppressWarnings("null")
	private void onButtonClick() {
		String command = commandLine.getText().trim();
		CommandMap.dispatch(ConsoleCommandSender.getInstance(), command);
		clearCommandLine();
		if (command != null || command.equals(""))
			lastCommands.add(command);
	}

	public static synchronized void append(String str) {
		append(str, Color.BLACK);
	}

	public static void error(String msg) {
		append(msg, new Color(178, 34, 34), true);
	}

	public static void append(final String str, final Color c) {
		if (str == null || str.trim().length() == 0)
			return;

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				AttributeSet aset = StyleContext.getDefaultStyleContext()
						.addAttribute(SimpleAttributeSet.EMPTY,
								StyleConstants.Foreground, c);

				ServerUI ui = Fusster.getServerUI();
				if (ui == null)
					return;
				try {
					JTextPane console = ui.getConsoleArea();

					if (console == null)
						return;

					Document doc = console.getDocument();
					String time = "[ "
							+ DateFormat.getTimeInstance().format(
									new Date(System.currentTimeMillis()))
							+ " ] ";

					doc.insertString(doc.getLength(), time, timeAset);

					String line = str + System.lineSeparator();

					doc.insertString(doc.getLength(), line, aset);

					Fusster.log(time + line);
					
					console.setCaretPosition(console.getDocument().getLength());
				} catch (BadLocationException e) {
					append(str, c);
				}
			}

		});
	}

	public static void append(String str, Color c, boolean requireDebbiging) {
		if (!requireDebbiging) {
			append(str, c);
		} else if (requireDebbiging && debugging) {
			append(str, c);
		}
	}

	public static void debug(String message) {
		if (Fusster.isDebugging()) {
			append(message, Color.yellow);
		}
	}

	public static void debug(FussterPlugin plugin, String message) {
		ServerUI.debug("[ " + plugin.getDescription().getName() + " ] "
				+ message);
	}

	private String onArrowClick(boolean isUParrow) {
		int size = lastCommands.size();
		if (isUParrow) {
			UPpressed++;

			int result = size - UPpressed;

			if (result < 0) {
				result = 0;
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
				result = arraySize;
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initComponents() {

		setTitle("Fusster : Server");
		setResizable(false);

		debugCheckBox = new JCheckBox();
		setUsernamePane(new JList());
		setConsoleArea(new JTextPane());

		// Anonymous classes
		consolePane = new JTextPane();
		
		try {
			PrintStream out = new PrintStream(new ConsoleOutputStream(), true,
					"UTF-8");
			System.setOut(out);

			PrintStream err = new PrintStream(new ConsoleOutputStream(), true,
					"UTF-8");
			System.setErr(err);
		} catch (IOException e) {
			e.printStackTrace();
		}

		getUsernamePane().setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			Font font = new Font("helvitica", Font.BOLD, 12);

			@Override
			public Component getListCellRendererComponent(JList list,
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

		try {
			setIconImage(Loader.loadImage("/res/LogoNoBG.png"));
			logoBlue = Loader.loadImage("/res/LogoNoBG25x25.png");
		} catch (Exception e) {
			e.printStackTrace();
		}

		jScrollPane1 = new JScrollPane();
		jScrollPane2 = new JScrollPane();
		commandLine = new JTextField();
		executeButton = new JButton();

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setMaximumSize(new Dimension(860, 640));
		setMinimumSize(new Dimension(860, 640));
		setPreferredSize(new Dimension(860, 640));

		commandLine.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					onButtonClick();
					UPpressed = 0;
				}
				if (e.getKeyCode() == KeyEvent.VK_UP)
					commandLine.setText(onArrowClick(true));

				if (e.getKeyCode() == KeyEvent.VK_DOWN)
					commandLine.setText(onArrowClick(false));

			}
		});

		debugCheckBox.setText("Debuging");
		debugCheckBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				setDebbuging(!isDebbuging());
			}
		});

		jScrollPane1.setViewportView(getUsernamePane());
		((DefaultCaret) consolePane.getCaret())
				.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		getUsernamePane().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {

					if (PlayerManager.getPlayers().size() >= 1) {

						JList list = (JList) e.getSource();
						final int row = list.locationToIndex(e.getPoint());
						list.setSelectedIndex(row);

						JPopupMenu pm = new JPopupMenu();

						JMenuItem kickMenuItem = new JMenuItem("Kick");
						JMenuItem messageMenuItem = new JMenuItem("Message");

						kickMenuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								try {
									String playerName = PlayerManager
											.getPlayers().get(row).getName();
									Player player = PlayerManager
											.get(playerName);
									PlayerManager.removePlayer(player,
											"Kicked by Console");
								} catch (PlayerException e1) {
									append("Not working :(");
								}

							}
						});

						messageMenuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								Player p = PlayerManager.getPlayers().get(row);
								String toSend = JOptionPane.showInputDialog(
										null, "Send Message",
										"Send a message to " + p.getName(),
										JOptionPane.INFORMATION_MESSAGE);
								if (toSend != null)
									p.sendMessage(toSend);
							}
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

		getConsoleArea().setEditable(false);
		jScrollPane2.setViewportView(getConsoleArea());

		executeButton.setText("Execute");

		executeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				onButtonClick();
			}

		});

		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addComponent(jScrollPane1,
										GroupLayout.PREFERRED_SIZE, 190,
										GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(
										LayoutStyle.ComponentPlacement.UNRELATED)
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(
														jScrollPane2,
														GroupLayout.DEFAULT_SIZE,
														535, Short.MAX_VALUE)
												.addComponent(commandLine)
												.addGroup(
														GroupLayout.Alignment.TRAILING,
														layout.createSequentialGroup()
																.addComponent(
																		debugCheckBox)
																.addGap(0,
																		0,
																		Short.MAX_VALUE)
																.addComponent(
																		executeButton)))
								.addContainerGap()));
		layout.setVerticalGroup(layout
				.createParallelGroup(GroupLayout.Alignment.LEADING)
				.addGroup(
						layout.createSequentialGroup()
								.addContainerGap()
								.addGroup(
										layout.createParallelGroup(
												GroupLayout.Alignment.LEADING)
												.addComponent(jScrollPane1)
												.addGroup(
														layout.createSequentialGroup()
																.addComponent(
																		jScrollPane2,
																		GroupLayout.PREFERRED_SIZE,
																		474,
																		GroupLayout.PREFERRED_SIZE)
																.addGap(18, 18,
																		18)
																.addComponent(
																		commandLine,
																		GroupLayout.PREFERRED_SIZE,
																		35,
																		GroupLayout.PREFERRED_SIZE)
																.addPreferredGap(
																		LayoutStyle.ComponentPlacement.RELATED,
																		12,
																		Short.MAX_VALUE)
																.addComponent(
																		debugCheckBox)
																.addComponent(
																		executeButton)))
								.addContainerGap()));

		pack();
		setVisible(true);
	}

	@SuppressWarnings("unchecked")
	public static void updateUsernameArea() {
		Fusster.setProperty("players",
				String.valueOf(PlayerManager.getPlayers().size()));
		List<String> names = new ArrayList<String>();
		for (Player p : PlayerManager.getPlayers()) {
			names.add(p.getName());
		}
		Fusster.getServerUI().getUsernamePane().setListData(names.toArray());
	}

	public static JCheckBox getDebugCheckBox() {
		return debugCheckBox;
	}

	public static boolean isDebbuging() {
		return debugging;
	}

	public static void setDebbuging(boolean debbuging) {
		ServerUI.getDebugCheckBox().setSelected(debbuging);
		Fusster.setDebugging(debbuging);
		if (debbuging) {
			append("Debugging is turned on.", Color.GREEN);
		} else if (isDebbuging()) {
			append("Debugging is turned off.", Color.RED);
		}
		ServerUI.debugging = debbuging;

	}

	@SuppressWarnings("rawtypes")
	public JList getUsernamePane() {
		return usernamePane;
	}

	public void setUsernamePane(@SuppressWarnings("rawtypes") JList usernameArea) {
		usernamePane = usernameArea;
	}

	public JTextPane getConsoleArea() {
		return consolePane;
	}

	public void setConsoleArea(JTextPane consoleArea) {
		consolePane = consoleArea;
	}

}