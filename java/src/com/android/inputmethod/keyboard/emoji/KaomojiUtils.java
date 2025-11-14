package com.android.inputmethod.keyboard.emoji;

import android.content.res.Resources;
import com.android.inputmethod.latin.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KaomojiUtils {

    public static final List<Integer> categoryNameString = new ArrayList<Integer>() {{
        add(R.string.joy);
        add(R.string.love);
        add(R.string.embarrassment);
        add(R.string.sympathy);
        add(R.string.dissatisfaction);
        add(R.string.anger);
        add(R.string.sadness);
        add(R.string.pain);
        add(R.string.fear);
        add(R.string.indifference);
        add(R.string.confusion);
        add(R.string.doubt);
        add(R.string.surprise);
        add(R.string.greeting);
        add(R.string.hugging);
        add(R.string.winking);
        add(R.string.apologizing);
        add(R.string.nosebleeding);
        add(R.string.hiding);
        add(R.string.writing);
        add(R.string.running);
        add(R.string.sleeping);
        add(R.string.cat);
        add(R.string.bear);
        add(R.string.dog);
        add(R.string.rabbit);
        add(R.string.pig);
        add(R.string.bird);
        add(R.string.spider);
        add(R.string.friends);
        add(R.string.enemies);
        add(R.string.magic);
        add(R.string.food);
        add(R.string.music);
        add(R.string.games);
        add(R.string.faces);
        add(R.string.special);
    }};

    public static final List<String> categoryName = new ArrayList<String>() {{
        add("Joy");
        add("Love");
        add("Embarrassment");
        add("Sympathy");
        add("Dissatisfaction");
        add("Anger");
        add("Sadness");
        add("Pain");
        add("Fear");
        add("Indifference");
        add("Confusion");
        add("Doubt");
        add("Surprise");
        add("Greeting");
        add("Hugging");
        add("Winking");
        add("Apologizing");
        add("Nosebleeding");
        add("Hiding");
        add("Writing");
        add("Running");
        add("Sleeping");
        add("Cat");
        add("Bear");
        add("Dog");
        add("Rabbit");
        add("Pig");
        add("Bird");
        add("Spider");
        add("Friends");
        add("Enemies");
        add("Magic");
        add("Food");
        add("Music");
        add("Games");
        add("Faces");
        add("Special");
    }};

    private static final List<Integer> categoryArray = new ArrayList<Integer>() {{
        add(R.array.emoji_kaomojis_joy);
        add(R.array.emoji_kaomojis_love);
        add(R.array.emoji_kaomojis_embarrassment);
        add(R.array.emoji_kaomojis_sympathy);
        add(R.array.emoji_kaomojis_dissatisfaction);
        add(R.array.emoji_kaomojis_anger);
        add(R.array.emoji_kaomojis_sadness);
        add(R.array.emoji_kaomojis_pain);
        add(R.array.emoji_kaomojis_fear);
        add(R.array.emoji_kaomojis_indifference);
        add(R.array.emoji_kaomojis_confusion);
        add(R.array.emoji_kaomojis_doubt);
        add(R.array.emoji_kaomojis_surprise);
        add(R.array.emoji_kaomojis_greeting);
        add(R.array.emoji_kaomojis_hugging);
        add(R.array.emoji_kaomojis_winking);
        add(R.array.emoji_kaomojis_apologizing);
        add(R.array.emoji_kaomojis_nosebleeding);
        add(R.array.emoji_kaomojis_hiding);
        add(R.array.emoji_kaomojis_writing);
        add(R.array.emoji_kaomojis_running);
        add(R.array.emoji_kaomojis_sleeping);
        add(R.array.emoji_kaomojis_cat);
        add(R.array.emoji_kaomojis_bear);
        add(R.array.emoji_kaomojis_dog);
        add(R.array.emoji_kaomojis_rabbit);
        add(R.array.emoji_kaomojis_pig);
        add(R.array.emoji_kaomojis_bird);
        add(R.array.emoji_kaomojis_spider);
        add(R.array.emoji_kaomojis_friends);
        add(R.array.emoji_kaomojis_enemies);
        add(R.array.emoji_kaomojis_magic);
        add(R.array.emoji_kaomojis_food);
        add(R.array.emoji_kaomojis_music);
        add(R.array.emoji_kaomojis_games);
        add(R.array.emoji_kaomojis_faces);
        add(R.array.emoji_kaomojis_special);
    }};

    public static Map<String, String[]> getKaomojiMap(Resources res) {
        int categoryCount = categoryArray.size();

        // Load all categories once
        String[][] cachedCategories = new String[categoryCount][];
        int totalSize = 0;

        for (int i = 0; i < categoryCount; i++) {
            String[] arr = res.getStringArray(categoryArray.get(i));
            cachedCategories[i] = arr;
            totalSize += arr.length;
        }

        // Build "All" with exact sizing (no resizing, no lists)
        String[] all = new String[totalSize];
        int pos = 0;
        for (String[] cat : cachedCategories) {
            System.arraycopy(cat, 0, all, pos, cat.length);
            pos += cat.length;
        }

        // Build result map
        Map<String, String[]> result = new HashMap<>(categoryCount + 1);
        result.put("All", all);

        for (int i = 0; i < categoryCount; i++) {
            result.put(categoryName.get(i), cachedCategories[i]);
        }

        return result;
    }

}