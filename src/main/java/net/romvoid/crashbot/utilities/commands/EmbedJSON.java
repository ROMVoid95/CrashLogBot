package net.romvoid.crashbot.utilities.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class EmbedJSON {
    public final List<EmbedField> fields = new ArrayList<>();
    public String author, authorImg, authorUrl;
    public String color;
    public String description;
    public String footer, footerImg;
    public String image;
    public String thumbnail;
    public String title, titleUrl;

    public MessageEmbed gen(Member member) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null) embedBuilder.setTitle(title, titleUrl);
        if (description != null) embedBuilder.setDescription(description);
        if (author != null) embedBuilder.setAuthor(author, authorUrl, authorImg);
        if (footer != null) embedBuilder.setFooter(footer, footerImg);
        if (image != null) embedBuilder.setImage(image);
        if (thumbnail != null) embedBuilder.setThumbnail(thumbnail);
        if (color != null) {
            Color col = null;
            try {
                col = (Color) Color.class.getField(color).get(null);
            } catch (Exception ignored) {
                String colorLower = color.toLowerCase();
                if (colorLower.equals("member")) {
                    if (member != null)
                        col = member.getColor();
                } else if (colorLower.matches("#?(0x)?[0123456789abcdef]{1,6}")) {
                    try {
                        col = Color.decode(colorLower.startsWith("0x") ? colorLower : "0x" + colorLower);
                    } catch (Exception ignored2) {
                    }
                }
            }
            if (col != null) embedBuilder.setColor(col);
        }

        fields.forEach(f -> {
            if (f == null) {
                embedBuilder.addBlankField(false);
            } else if (f.value == null) {
                embedBuilder.addBlankField(f.inline);
            } else {
                embedBuilder.addField(f.name == null ? "" : f.name, f.value, f.inline);
            }
        });

        return embedBuilder.build();
    }

    public static class EmbedField {
        public boolean inline;
        public String name, value;
    }
}
