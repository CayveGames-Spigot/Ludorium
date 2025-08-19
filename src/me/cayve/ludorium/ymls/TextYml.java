package me.cayve.ludorium.ymls;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.bukkit.entity.Player;

import me.cayve.ludorium.ymls.YmlFiles.YmlFileInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class TextYml {

	private static Map<String, YmlFileInfo> infos = new HashMap<String, YmlFileInfo>();

	/**
	 * Gets text from the TextYml file
	 * @param viewer The player that will view the text (locale functionality)
	 * @param path The path to the text within the file
	 * @param rawText Function to manipulate the raw text string before converted to a component
	 * @param tagResolvers Any tags to resolve
	 * @return
	 */
	public static Component getText(Player viewer, String path, Function<String, String> rawText, TagResolver... tagResolvers) 
		{ return getText(viewer.locale(), path, rawText, tagResolvers); }
	
	/**
	 * Gets text from the TextYml file
	 * @param viewer The player that will view the text (locale functionality)
	 * @param path The path to the text within the file
	 * @param tagResolvers Any tags to resolve
	 * @return
	 */
	public static Component getText(Player viewer, String path, TagResolver... tagResolvers) 
		{ return getText(viewer, path, null, tagResolvers); }
	/**
	 * Gets text from the TextYml file
	 * @param locale The locale file that should be chosen
	 * @param path The path to the text within the file
	 * @param tagResolvers Any tags to resolve
	 * @return
	 */
	public static Component getText(Locale locale, String path, TagResolver... tagResolvers) 
		{ return getText(locale, path, null, tagResolvers); }
	/**
	 * Gets text from the TextYml file
	 * @param locale The locale file that should be chosen
	 * @param path The path to the text within the file
	 * @param rawText Function to manipulate the raw text string before converted to a component
	 * @param tagResolvers Any tags to resolve
	 * @return
	 */
	public static Component getText(Locale locale, String path, Function<String, String> rawText, TagResolver... tagResolvers) {
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
		
		String raw = infos.get(lang).customConfig.getString(path)
				.replace("<highlight>", infos.get(lang).customConfig.getString("style.highlight"))
				.replace("<secondary>", infos.get(lang).customConfig.getString("style.secondary"));
		
		if (rawText != null)
			raw = rawText.apply(raw);
		
		return MiniMessage.miniMessage().deserialize(raw, tagResolvers);
	}
	
	
}
