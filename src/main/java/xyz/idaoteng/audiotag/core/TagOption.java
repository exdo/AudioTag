package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.wav.WavOptions;
import org.jaudiotagger.audio.wav.WavSaveOptions;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.reference.ID3V2Version;

public class TagOption {
    public static void setupOptions() {
        // 升级到 ID3V2.4
        TagOptionSingleton option = TagOptionSingleton.getInstance();
        option.setID3V2Version(ID3V2Version.ID3_V24);
        // 只读取 wav文件的 ID3 标签
        option.setWavOptions(WavOptions.READ_ID3_ONLY_AND_SYNC);
        // 保存 wav 文件的 ID3 标签和 INFO 标签
        option.setWavSaveOptions(WavSaveOptions.SAVE_BOTH);
    }
}
