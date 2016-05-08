package eu.fusster.command.defaults;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import eu.fusster.command.Command;
import eu.fusster.command.CommandSender;

public class ServerInfoCommand extends Command {

	public ServerInfoCommand() {
		super("Server Info", "Obtains info for another FussterServer",
				"serverinfo <IP> <port>");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (args.length == 2) {
			String hostString = args[0];
			int port = 47247; // Default port
			try {
				port = Integer.parseInt(args[1]);
				if (port < 0 || port > 65536)
					throw new NumberFormatException();
			} catch (NumberFormatException e) {
				sender.sendMessage("Invalid port");
				return false;
			}
			try {
				InetAddress host = InetAddress.getByName(hostString);
				try {
					Socket socket = new Socket(host, port);

					BufferedReader read = new BufferedReader(
							new InputStreamReader(socket.getInputStream()));
					PrintWriter write = new PrintWriter(
							socket.getOutputStream(), true);

					write.println("name");
					String serverName = read.readLine();
					write.println("version");
					String version = read.readLine();
					write.println("players");
					String players = read.readLine();
					write.println("maxPlayers");
					String maxPlayers = read.readLine();

					socket.close();

					sender.sendMessage(serverName + "[v." + version + ", "
							+ players + "/" + maxPlayers + "]");
					return true;
				} catch (IOException io) {
					sender.sendMessage("Connection Error");
					return false;
				}
			} catch (UnknownHostException e) {
				sender.sendMessage("Invalid IP");
				return false;
			}
		} else {
			sender.sendMessage("Invalid arguments");
			return false;
		}
	}

}
