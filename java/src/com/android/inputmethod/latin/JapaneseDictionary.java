package com.android.inputmethod.latin;

import com.android.inputmethod.latin.common.ComposedData;
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion;

import org.mozc.android.inputmethod.japanese.protobuf.ProtoCandidateWindow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class JapaneseDictionary extends Dictionary{
    public JapaneseDictionary(String dictType, Locale locale) {
        super(dictType, locale);
        if (!locale.getISO3Language().equals("jpn")) {
            throw new UnsupportedOperationException("the Japanese Dictionary (powered by libmozc) will only work on the Japanese language, it doesnt work on " + locale.getLanguage());
        }
    }

    @Override
    public ArrayList<SuggestedWords.SuggestedWordInfo> getSuggestions(
            ComposedData composedData, NgramContext ngramContext,
            long proximityInfoHandle,
            SettingsValuesForSuggestion settingsValuesForSuggestion, int sessionId,
            float weightForLocale, float[] inOutWeightOfLangModelVsSpatialModel) {
        List<ProtoCandidateWindow.CandidateWindow.Candidate> words = MozcEngine.getInstance().getCandidates();
        if (words.isEmpty()) {
            return new ArrayList<>(0);
        } else {
            ArrayList<SuggestedWords.SuggestedWordInfo> infos = new ArrayList<>();
            for (int i = 0; i < words.size(); i++) {
                infos.add(new SuggestedWords.SuggestedWordInfo(words.get(i).getValue(), "",
                        1000 - (i), SuggestedWords.SuggestedWordInfo.KIND_CORRECTION, this, -1,
                        0));
            }
            return infos;
        }
    }

    @Override
    public boolean isInDictionary(String word) {
        return false;
    }
}
