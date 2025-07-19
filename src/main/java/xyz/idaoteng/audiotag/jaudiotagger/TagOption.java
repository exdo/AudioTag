package xyz.idaoteng.audiotag.jaudiotagger;

import org.jaudiotagger.audio.wav.WavOptions;
import org.jaudiotagger.audio.wav.WavSaveOptions;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.reference.ID3V2Version;

public class TagOption {
    public static void setupOptions() {
        TagOptionSingleton option = TagOptionSingleton.getInstance();
        // 设置默认的 ID3 版本为 2.4
        option.setID3V2Version(ID3V2Version.ID3_V24);
        // 只读取 wav文件的 ID3 标签
        option.setWavOptions(WavOptions.READ_ID3_ONLY_AND_SYNC);
        // 保存 wav 文件的 ID3 标签和 INFO 标签
        option.setWavSaveOptions(WavSaveOptions.SAVE_BOTH);
    }
}
