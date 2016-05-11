package eu.fusster.command.defaults;

import java.math.BigDecimal;

import eu.fusster.Fusster;
import eu.fusster.ServerInfo;
import eu.fusster.command.CommandSender;
import eu.fusster.command.ConsoleCommandSender;
import eu.fusster.player.PlayerManager;

public class ThisCommand extends DefaultCommand {

	public ThisCommand() {
		super("ThisCommand", "Manipulated server information",
				"this <properties/clear/name/version/mem/rst/sort>");
	}

	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof ConsoleCommandSender))
			return false;

		final int argsLengh = args.length;

		if (argsLengh == 0) {
			sender.sendMessage("Usage: " + super.getUsageMessage());
			return false;
		}

		switch (args[0]) {
		case "properties":
			switch (argsLengh) {
			case 1:
				sender.sendMessage("Properties: "
						+ Fusster.getProperties().toString());
				break;
			case 2:
				String prop = args[1];
				if (Fusster.containsKey(prop)) {
					sender.sendMessage("Value of property \'" + prop + "\': "
							+ Fusster.getProperty(prop));
				} else {
					sender.sendMessage("No such property \'" + prop + "\'.");
				}
				break;
			case 4:
				if (args[1].equals("set")) {
					String key = args[2];
					String value = args[3];
					Fusster.setProperty(key, value);
					sender.sendMessage("Property \'" + key + "\' set to: "
							+ value);
				}
				break;
			}
			break;
		case "clear":
			clear();
			break;
		case "name":
			sender.sendMessage("Server name: " + Fusster.getName());
			break;
		case "version":
			sender.sendMessage("Server version: " + ServerInfo.VERSION);
			break;
		case "mem":
			sender.sendMessage("Used memory: " + usedMem() + " MB in "
					+ Thread.activeCount() + " threads.");
			break;
		case "rst":
			PlayerManager.disconnectAll("Server internaly restarting.");
			Fusster.getPluginLoader().reloadAll();
			clear();
			break;
		case "sort":
			if(args.length > 1){
				if(args[1].equals("date")){
					PlayerManager.sort(PlayerManager.BY_DATE_CONNECTED);
					sender.sendMessage("Players sorted by connection date");
				} else if(args[1].equals("name")){
					PlayerManager.sort(PlayerManager.BY_NAME);
					sender.sendMessage("Players sorted by name alhpabetically");
				}
			} else
				sender.sendMessage("Invalid arguments. Usage: /this sort <date,name>" );
			Fusster.updatePlayersPane();
			break;
		default:
			sender.sendMessage("Usage: " + getUsageMessage());
			break;
		}

		return false;
	}
	
	public static void clear(){
		Fusster.getServerUI().clearCommandLine();
		Runtime.getRuntime().gc();
	}
	
	public static float usedMem() {
		float usedMemBytes = (float) (Runtime.getRuntime().totalMemory() - Runtime
				.getRuntime().freeMemory());
		float usedMemMB = usedMemBytes / (1024 * 1024);
		float usedMemMBrounded = BigDecimal.valueOf(usedMemMB)
				.setScale(4, BigDecimal.ROUND_HALF_UP).floatValue();
		return usedMemMBrounded;

	}

}
