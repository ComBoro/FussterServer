package eu.fusster.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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
import eu.fusster.player.PlayerManager;
import eu.fusster.plugin.PluginException;

public class BetterUI extends JFrame {
	private static final long serialVersionUID = 1L;
	
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
    private JList<String> playersList;
    private JScrollPane playersScrollPane;
    
	private boolean debugging = false;
    
	private Image logoBlue;
	
	private static List<String> lastCommands = new ArrayList<String>();

	private int UPpressed = 0;
	
	private boolean licenseOpened = false;
	private JScrollPane licenseScrollPane;
	
	private static final AttributeSet timeAset = StyleContext
			.getDefaultStyleContext().addAttribute(SimpleAttributeSet.EMPTY,
					StyleConstants.Foreground, Color.GRAY);
	
    public BetterUI() {
        initComponents();
        
		Runtime.getRuntime().addShutdownHook(new Thread(()->Fusster.shutdown(false)));
    }
    
	public void clearCommandLine(){
		commandLine.setText("");
	}

	private void onButtonClick() {
		String command = commandLine.getText().trim();
		CommandMap.dispatch(ConsoleCommandSender.getInstance(), command);
		clearCommandLine();
		if (command == null || command.equals(""))
			lastCommands.add(command);
	}

	public void append(String str) {
		append(str, Color.BLACK);
	}

	private void openLicense(){
		SwingUtilities.invokeLater(()->{
			try{
				if(!licenseOpened){
					File licenseFile = new File(Loader.path_src + "/eu/fusster/","license.txt");
					Path licensePath = Paths.get(licenseFile.toURI());
					
					JTextPane tf = new JTextPane();
					tf.setEditable(false);
					licenseScrollPane = new JScrollPane(tf);
					Document doc = tf.getDocument();
					List<String> lines = Files.readAllLines(licensePath);
					
					AttributeSet simple = new SimpleAttributeSet();
					
					for(String line : lines){
						doc.insertString(doc.getLength(), line + System.lineSeparator(), simple);
					}
					
					StyledDocument docm = tf.getStyledDocument();
					SimpleAttributeSet center = new SimpleAttributeSet();
					StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
					docm.setParagraphAttributes(0, doc.getLength(), center, false);
					
					tf.setCaretPosition(0);
					
					consoleTabbedPane.add("Legal", licenseScrollPane);
				} else {
					// Close license
					consoleTabbedPane.remove(licenseScrollPane);
				}
				
				licenseOpened = !licenseOpened;
				
			} catch(Exception e){
				e.printStackTrace();
			}
		});
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
					
					consoleTextPane.setCaretPosition(consoleTextPane.getDocument().getLength());
				} catch (BadLocationException e) {
					append(str, c);
				}
			}

		});
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

    private void initComponents() {
    	
    	// Out & Err
		try {
			PrintStream out = new PrintStream(new ConsoleOutputStream(Color.black), true,
					"UTF-8");
			System.setOut(out);

			PrintStream err = new PrintStream(new ConsoleOutputStream(Fusster.error), true,
					"UTF-8");
			System.setErr(err);
		} catch (IOException e) {
			e.printStackTrace();
		}

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
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        debuggingCheckBox.setText("Debugging");
        debuggingCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debuggingCheckBoxActionPerformed(evt);
            }
        });

        commandLine.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyPressed(KeyEvent e) {
        		int code = e.getKeyCode();
        		if(code == KeyEvent.VK_ENTER) onButtonClick();
        		else if(code == KeyEvent.VK_UP) onArrowClick(true);
        		else if(code == KeyEvent.VK_DOWN) onArrowClick(false);
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
		JMenu plugin = new JMenu("Plugin");
		
		JMenuItem plugin_import = new JMenuItem("Import");
		plugin_import.addActionListener(ae -> {
				if(Fusster.getPluginLoader() == null) return;
				JFileChooser fc = new JFileChooser();
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.showOpenDialog(null);
				File file = fc.getSelectedFile();
				try {
					if(file != null)
					Fusster.getPluginLoader().load(file);
				} catch (PluginException e1) {
					Fusster.error(e1.getMessage());
				}
		});
		plugin.add(plugin_import);
		
		JMenuItem plugin_importAll = new JMenuItem("Import directory");
		plugin_importAll.addActionListener(ae -> {
			if(Fusster.getPluginLoader() == null) return;
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.showOpenDialog(null);
			File file = fc.getSelectedFile();
			if(file!=null)
			Fusster.getPluginLoader().loadAll(file);
		});
		plugin.add(plugin_importAll);
		
		JMenuItem plugin_loadAll = new JMenuItem("Load all");
		plugin_loadAll.addActionListener(ae ->{
			if(Fusster.getPluginLoader() != null) Fusster.getPluginLoader().loadAll();
		});
		plugin.add(plugin_loadAll);
		
		JMenuItem plugin_reloadAll = new JMenuItem("Reload all");
		plugin_reloadAll.addActionListener(ae ->{
			if(Fusster.getPluginLoader() != null) Fusster.getPluginLoader().reloadAll();
		});
		plugin.add(plugin_reloadAll);
		
		JMenuItem plugin_unloadAll = new JMenuItem("Unload all");
		plugin_unloadAll.addActionListener(ae -> {
			if(Fusster.getPluginLoader() != null) Fusster.getPluginLoader().unloadAll();
		});
		plugin.add(plugin_unloadAll);
		
		menuBar.add(plugin);
		
		JMenu help = new JMenu("Help");
		
		JMenuItem legal = new JMenuItem("Legal");
		legal.addActionListener(ae->{
			openLicense();
		});
		help.add(legal);
		
		menuBar.add(help);
		
		
		setJMenuBar(menuBar);
		
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientsTabbedPane, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(consoleTabbedPane)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(debuggingCheckBox)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 488, Short.MAX_VALUE)
                        .addComponent(jButton1))
                    .addComponent(commandLine))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(consoleTabbedPane)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(commandLine, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(debuggingCheckBox)))
                    .addComponent(clientsTabbedPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 621, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
        setVisible(true);
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
	
	public void updatePlayersPane() {
		Fusster.setProperty("players",
				String.valueOf(PlayerManager.getPlayers().size()));
		
		List<Player> players = PlayerManager.getPlayers();
		String[] listData = new String[players.size()];
		
		for(int i = 0; i < players.size() ;i++)
			listData[i] = players.get(i).getName();
		
		playersList.setListData(listData);
	}
	
	public void updateClientsPane(){
		List<Client> clients = Server.getClients();
		String[] listData = new String[clients.size()];
		
		for(int i = 0; i < clients.size() ;i++){
			Client client = clients.get(i);
			listData[i] = client.getIP() + " [" + client.dateCreated().toString() + "]";
		}
		
		clientsList.setListData(listData);
	}
	
	public boolean isDebugging(){
		return debugging;
	}

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) { 
    	onButtonClick();
    }                                        

    private void debuggingCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {                                                  
    	setDebugging(debuggingCheckBox.isSelected());
    }

	public JTabbedPane getConsoleTabbedPane() {
		return consoleTabbedPane;
	}                                                 
}
