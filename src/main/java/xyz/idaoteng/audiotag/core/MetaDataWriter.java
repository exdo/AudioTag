package xyz.idaoteng.audiotag.core;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.id3.AbstractID3Tag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v24Tag;
import org.jaudiotagger.tag.images.StandardArtwork;
import org.jaudiotagger.tag.wav.WavInfoTag;
import org.jaudiotagger.tag.wav.WavTag;
import xyz.idaoteng.audiotag.bean.AudioMetaData;
import xyz.idaoteng.audiotag.constant.EditableTag;

import java.io.File;
import java.util.List;

public class MetaDataWriter {
    /**
     * 写入标签
     * @param metaData 音频文件元数据
     * @param tagName 可编辑标签类型
     */
    public static void write(AudioMetaData metaData, EditableTag tagName) {
        write(metaData, List.of(tagName));
    }

    /**
     * 写入标签
     * @param metaData 音频文件元数据
     * @param tagNames 可编辑标签类型列表
     */
    public static void write(AudioMetaData metaData, List<EditableTag> tagNames) {
        File file = new File(metaData.getAbsolutePath());
        AudioFile audioFile;
        Tag tag;
        try {
            audioFile = AudioFileIO.read(file);
            tag = getUnifiedVersionTag(audioFile);
        } catch (Exception e) {
            handleReadError(metaData, e);
            return;
        }

        for (EditableTag tagName : tagNames) {
            setTagFiled(metaData, tag, tagName);
        }

        try {
            AudioFileIO.write(audioFile);
        } catch (Exception e) {
            handleWriteError(metaData, e);
        }
    }

    private static void handleReadError(AudioMetaData metaData, Exception e) {
        System.out.println("读取或删除音频文件音频文件标签失败：" + metaData.getAbsolutePath());
        System.out.println(e.getMessage());
        e.printStackTrace();
    }

    private static void handleWriteError(AudioMetaData metaData, Exception e) {
        System.out.println("写入标签时IO异常: " + metaData.getAbsolutePath());
        System.out.println(e.getMessage());
        e.printStackTrace();
    }

    /**
     * 获取统一版本标签实例
     * @param audioFile 音频文件
     * @return 统一版本标签实例
     */
    private static Tag getUnifiedVersionTag(AudioFile audioFile) throws Exception {
        Tag tag = audioFile.getTag();

        if (tag instanceof ID3v24Tag) return tag;

        if (tag instanceof WavTag wavTag) {
            if (wavTag.isExistingId3Tag()) {
                AbstractID3v2Tag id3Tag = wavTag.getID3Tag();
                if (id3Tag instanceof ID3v24Tag) {
                    return wavTag;
                } else {
                    // 将 wavTag 中的 ID3tag 设置为 ID3v24Tag 并将旧版本标签内容转移到新版本标签
                    wavTag.setID3Tag(new ID3v24Tag(id3Tag));
                }
            } else {
                // wagTag 不存在 ID3tag 时创建一个空的 ID3v24Tag
                wavTag.setID3Tag(new ID3v24Tag());
            }

            // 如果wavTag 中还存在 WavInfoTag，将 WavInfoTag 中的内容同步到 WavTag 中
            if (wavTag.isExistingInfoTag()) {
                wavTag.syncToInfoFromId3IfEmpty();
            }

            audioFile.setTag(wavTag);
            return wavTag;
        }

        if (tag == null) {
            tag = createDefaultTag(audioFile);
        }

        if (tag instanceof AbstractID3Tag) {
            tag = new ID3v24Tag((AbstractID3Tag) tag);
            audioFile.delete();
            audioFile.setTag(tag);
        }

        return tag;
    }

    /**
     * 创建默认的标签实例
     * @param audioFile 音频文件
     * @return 默认标签实例
     */
    private static Tag createDefaultTag(AudioFile audioFile) {
        Tag tag = audioFile.createDefaultTag();
        // 默认生成的 WavTag 没有设置 ID3Tag 和 WavInfoTag 实例，因此需要手动设置
        // 否则会报空指针异常
        if (tag instanceof WavTag wavTag) {
            wavTag.setID3Tag(new ID3v24Tag());
            wavTag.setInfoTag(new WavInfoTag());
        }
        audioFile.setTag(tag);
        return tag;
    }

    private static void setTagFiled(AudioMetaData metaData, Tag tag, EditableTag tagName) {
        try {
            switch (tagName) {
                case TITLE -> tag.setField(FieldKey.TITLE, metaData.getTitle());
                case ARTIST -> tag.setField(FieldKey.ARTIST, metaData.getArtist());
                case ALBUM -> tag.setField(FieldKey.ALBUM, metaData.getAlbum());
                case DATE -> tag.setField(FieldKey.YEAR, metaData.getDate());
                case GENRE -> tag.setField(FieldKey.GENRE, metaData.getGenre());
                case TRACK -> tag.setField(FieldKey.TRACK, metaData.getTrack());
                case COMMENT -> tag.setField(FieldKey.COMMENT, metaData.getComment());
                case COVER -> {
                    if (metaData.getCover() != null) {
                        tag.deleteArtworkField();
                        tag.setField(generateArtwork(metaData.getCover()));
                    } else {
                        tag.deleteArtworkField();
                    }
                }
                case ALL -> {
                    tag.setField(FieldKey.TITLE, metaData.getTitle());
                    tag.setField(FieldKey.ARTIST, metaData.getArtist());
                    tag.setField(FieldKey.ALBUM, metaData.getAlbum());
                    tag.setField(FieldKey.YEAR, metaData.getDate());
                    tag.setField(FieldKey.GENRE, metaData.getGenre());
                    tag.setField(FieldKey.TRACK, metaData.getTrack());
                    tag.setField(FieldKey.COMMENT, metaData.getComment());
                    if (metaData.getCover() != null) {
                        tag.deleteArtworkField();
                        tag.setField(generateArtwork(metaData.getCover()));
                    } else {
                        tag.deleteArtworkField();
                    }
                }
            }
        } catch (FieldDataInvalidException e) {
            e.printStackTrace();
            System.out.println("写入标签时字段数据非法：" + metaData.getAbsolutePath());
            System.out.println(e.getMessage());
        }
    }

    /** 
     * 写入图片时需要先包装成 StandardArtwork
     * @param binaryData 图片二进制数据
     * @return StandardArtwork
     */
    private static StandardArtwork generateArtwork(byte[] binaryData) {
        StandardArtwork artwork = new StandardArtwork();
        artwork.setBinaryData(binaryData);
        artwork.setImageFromData();
        return artwork;
    }
}
