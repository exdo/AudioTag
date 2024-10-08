package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.wav.WavOptions;
import org.jaudiotagger.audio.wav.WavSaveOptions;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.id3.ID3v23Tag;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jaudiotagger.tag.reference.ID3V2Version;
import org.jaudiotagger.tag.wav.WavInfoTag;
import org.jaudiotagger.tag.wav.WavTag;
import xyz.idaoteng.audiotag.bean.AudioMetaData;

import java.io.File;

public class MetaDataWriter {
    static {
        // 默认使用ID3v2.3（目前ID3v2.3仍然是主流）
        TagOptionSingleton optionSingleton = TagOptionSingleton.getInstance();
        optionSingleton.setID3V2Version(ID3V2Version.ID3_V23);
        // 只读取wav文件的ID3标签
        optionSingleton.setWavOptions(WavOptions.READ_ID3_ONLY);
        // 保存wav文件的ID3标签和INFO标签
        optionSingleton.setWavSaveOptions(WavSaveOptions.SAVE_BOTH);
    }

    public static void write(AudioMetaData metaData) {
        File file = new File(metaData.getAbsolutePath());
        // 写入标签前先删除原始标签以统一标签版本
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
            audioFile.delete();
        } catch (Exception e) {
            System.out.println("读取音频文件或删除音频文件标签失败：" + metaData.getAbsolutePath());
            System.out.println(e.getMessage());
            e.printStackTrace();
            return;
        }

        Tag tag = audioFile.createDefaultTag();
        // 默认生成的WavTag 没有设置ID3Tag和WavInfoTag实例，需要手动设置
        // 否则会报空指针异常
        if (tag instanceof WavTag wavTag) {
            wavTag.setID3Tag(new ID3v23Tag());
            wavTag.setInfoTag(new WavInfoTag());
        }

        // 部分 field 不允许为空，空标签值不写入也可减少IO操作
        try {
            if (!"".equals(metaData.getTitle())) {
                tag.setField(FieldKey.TITLE, metaData.getTitle());
            }
            if (!"".equals(metaData.getArtist())) {
                tag.setField(FieldKey.ARTIST, metaData.getArtist());
            }
            if (!"".equals(metaData.getAlbum())) {
                tag.setField(FieldKey.ALBUM, metaData.getAlbum());
            }
            if (!"".equals(metaData.getDate())) {
                tag.setField(FieldKey.YEAR, metaData.getDate());
            }
            if (!"".equals(metaData.getGenre())) {
                tag.setField(FieldKey.GENRE, metaData.getGenre());
            }
            if (!"".equals(metaData.getTrack())) {
                tag.setField(FieldKey.TRACK, metaData.getTrack());
            }
            if (!"".equals(metaData.getComment())) {
                tag.setField(FieldKey.COMMENT, metaData.getComment());
            }
            if (metaData.getCover() != null) {
                tag.setField(generateArtwork(metaData.getCover()));
            }
            audioFile.setTag(tag);
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            System.out.println("写入标签时字段数据非法：" + metaData.getAbsolutePath());
            System.out.println(e.getMessage());
        }

        try {
            AudioFileIO.write(audioFile);
        } catch (Exception e) {
            System.out.println("写入标签时IO异常: metaData.getAbsolutePath()");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // 写入图片时需要先包装成StandardArtwork
    private static StandardArtwork generateArtwork(byte[] binaryData) {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setBinaryData(binaryData);
        artwork.setImageFromData();
        return artwork;
    }
}
