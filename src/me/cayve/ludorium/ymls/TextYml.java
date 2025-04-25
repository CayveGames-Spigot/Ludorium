package me.cayve.ludorium.ymls;

import me.cayve.ludorium.utils.HexColors;
import me.cayve.ludorium.ymls.YmlFiles.YmlFileInfo;
import net.md_5.bungee.api.ChatColor;

public class TextYml {

	private static YmlFileInfo info;

	public static String getText(String text) {
		if (info == null)
			info = YmlFiles.reload("Text.yml");
		String path = text;

		if (!info.customConfig.contains(path))
			return ChatColor.RED + "Ludorium: Could not find Text.yml path: " + text;
		return HexColors.Convert(ChatColor.translateAlternateColorCodes('&', info.customConfig.getString(path)));
	}
}
