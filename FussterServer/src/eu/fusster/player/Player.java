package eu.fusster.player;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.comboro.Client;
import net.comboro.ClientListener;
import eu.fusster.Fusster;
import eu.fusster.command.CommandMap;
import eu.fusster.command.CommandSender;

public class Player implements CommandSender, ClientListener,
		Comparable<Player> {

	private String name;
	public Client client;

	/**
	 * Constructs a players with a socket, input listener and output writer.
	 * Adds the player to the PlayerManager.
	 * 
	 * @param client
	 *            The {@link Client} instance
	 * @param name
	 *            The player name
	 * @see Client
	 */
	public Player(Client client, String name) {
		this.client = client;
		this.name = name;

		// Check if it is an existing player
		for (Player pl : PlayerManager.getPlayers()) {
			if (pl.client.equals(this.client)) {
				sendMessage("Already registered.");
				return;
			}
		}

		// Basic checks
		if (Fusster.getBanList().contains(getIP())
				|| Fusster.getBanList().contains(name)) {
			closeConnection(Fusster.BAN_MESSEGE);
		} else if (PlayerManager.getPlayers().size() >= Fusster.getMaxPlayers()) {
			closeConnection("Server full.");
		} else if (!PlayerManager.checkAvailable(name)) {
			closeConnection("Duplicate name");
		} else if (name.length() < 4 || name.length() > 32) {
			closeConnection("Invalid name lenght. Must be between 4 and 32 characters");
		} else {
			Pattern p = Pattern.compile("[a-zA-Z_0-9]");
			Matcher m = p.matcher(name);
			boolean b = m.find();
			if (!b) {
				closeConnection("Invalid name. Only letters and numbers allowed");
			} else {
				this.client.addListener(this);

				PlayerManager.addPlayer(this);
			}
		}

	}

	@Override
	public void afterClose() {}

	public void closeConnection(String reason) {
		client.closeConnection(reason);
	}

	@Override
	public int compareTo(Player other) {
		return 31 * name.compareTo(other.getName())
				+ client.compareTo(other.client);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Player))
			return false;
		Player other = (Player) obj;
		return this.getName().equals(other.getName())
				&& this.getIP().equals(other.getIP());
	}

	public String getIP() {
		return client.getIP();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getSeparator() {
		return ":";
	}

	@Override
	public int hashCode() {
		return Objects.hash(client, name);
	}

	@Override
	public void onClose(String reason) {
		send("disc:" + reason);
		if (PlayerManager.getPlayers().contains(this))
			PlayerManager.removePlayer(this, reason, false);
	}

	@Override
	public void onInput(String input) {
		if (Fusster.containsKey(input))
			client.send(Fusster.getProperty(input));
		else
			CommandMap.dispatch(this, input);
	}

	public void send(String string) {
		client.send(string);
	}

	@Override
	public void sendMessage(String message) {
		client.send("msg:" + message);
	}

}
