package me.cayve.ludorium.ymls;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.entity.Player;

import me.cayve.ludorium.utils.HexColors;
import me.cayve.ludorium.ymls.YmlFiles.YmlFileInfo;
import net.md_5.bungee.api.ChatColor;

public class TextYml {

	private static Map<String, YmlFileInfo> infos = new HashMap<String, YmlFileInfo>();

	public static String getText(Locale locale, String text) {
		String lang = "_";
		
		//Make sure fallback text is reloaded
		if (!infos.containsKey(lang))
			infos.put(lang, YmlFiles.reload("text/Text.yml"));
		
		//Attempt to get locale text
		try {
			lang += locale.getISO3Language();
			
			if (!YmlFiles.exists("text/Text" + lang + ".yml"))
				lang = "_";
			else if (!infos.containsKey(lang))
				infos.put(lang, YmlFiles.reload("text/Text" + lang + ".yml"));
			
		} catch (Exception e) {}
		
		String path = text;

		if (!infos.get(lang).customConfig.contains(path))
			return ChatColor.RED + "text/Text" + lang + ".yml path: " + text + " not found.";
		return HexColors.Convert(ChatColor.translateAlternateColorCodes('&', infos.get(lang).customConfig.getString(path)));
	}
	
	public static String getText(Player player, String text) {
		return getText(player.locale(), text);
	}
}
