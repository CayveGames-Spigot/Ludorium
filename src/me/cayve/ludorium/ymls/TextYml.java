package me.cayve.ludorium.ymls;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.entity.Player;

import me.cayve.ludorium.ymls.YmlFiles.YmlFileInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TextYml {

	private static Map<String, YmlFileInfo> infos = new HashMap<String, YmlFileInfo>();

	public static Component getText(Locale locale, String path, TagResolver... tagResolvers) {
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

		if (!infos.get(lang).customConfig.contains(path))
			return Component.text("<red>text/Text" + lang + ".yml path: " + path + " not found.");
		return MiniMessage.miniMessage().deserialize(infos.get(lang).customConfig.getString(path)
				.replace("<highlight>", infos.get(lang).customConfig.getString("style.highlight"))
				.replace("<secondary>", infos.get(lang).customConfig.getString("style.secondary")), tagResolvers);
	}
	
	public static Component getText(Player player, String path, TagResolver... tagResolvers) {
		return getText(player.locale(), path, tagResolvers);
	}
	
	public static TagResolver tag(String key, Component component) {
		return TagResolver.resolver(key, Tag.inserting(component));
	}
}
