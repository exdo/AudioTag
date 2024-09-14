package xyz.idaoteng.audiotag.constant;

import java.util.ArrayList;
import java.util.Arrays;

public class MusicGenre {
    private static final String[] GENRES = new String[]{
            "Abstract Hip Hop",//抽象嘻哈
            "Acid Rock",//酸性摇滚
            "Acoustic Blues",//原声蓝调
            "Acoustic Rock",//原声摇滚
            "Afrobeat",//非洲节奏
            "Alternative Dance",//另类舞曲
            "Alternative Metal",//另类金属
            "Alternative Rock",//另类摇滚
            "Ambient",//氛围音乐
            "Ambient Pop",//氛围流行
            "Ambient Techno",//氛围科技舞曲
            "Americana",//美式乡村
            "Anarcho-Punk",//无政府朋克
            "Art Pop",//艺术流行
            "Art Punk",//艺术朋克
            "Art Rock",//艺术摇滚
            "Avant-Folk",//先锋民谣
            "Avant-Garde Jazz",//先锋爵士
            "Baggy / Madchester",//柏格/曼彻斯特风潮
            "Baroque Pop",//巴洛克流行
            "Beat Poetry",//敲打诗社
            "Bebop",//波普
            "Big Band",//大乐队
            "Blue Eyed Soul",//白人灵魂乐
            "Blues",//蓝调
            "Blues Rock",//蓝调摇滚
            "Boogie Rock",//布吉摇滚
            "Britpop",//英伦流行
            "Canterbury Scene",//坎特柏里之声
            "Chamber Folk",//室内民谣
            "Chamber Music",//室内乐
            "Chamber Pop",//室内流行
            "Chicago Blues",//芝加哥蓝调
            "Chicago Soul",//芝加哥灵乐
            "Classical Period",//古典主义时期
            "Conscious Hip Hop",//意识嘻哈
            "Contemporary Folk",//当代民谣
            "Contemporary R&B",//当代R&B
            "Cool Jazz",//酷派爵士
            "Country",//乡村
            "Country Rock",//乡村摇滚
            "Dance-Pop",//舞曲流行
            "Dance-Punk",//舞曲朋克
            "Dark Cabaret",//黑色卡巴莱
            "Death Metal",//死亡金属
            "Deep Soul",//深度灵魂
            "Delta Blues",//三角洲蓝调
            "Disco",//迪斯科
            "Dixieland",//迪克西兰爵士乐
            "Doom Metal",//厄运金属
            "Downtempo",//慢速电子
            "Dream Pop",//梦幻流行
            "Drone",//嗡鸣电子
            "Dub",//混录雷吉
            "East Coast Hip Hop",//东海岸嘻哈
            "Electric Blues",//电子蓝调
            "Electronic",//电子
            "Electropop",//电子流行
            "Emo",//情绪摇滚
            "Euro-Disco",//欧洲迪斯科
            "Experimental",//实验音乐
            "Experimental Big Band",//实验大乐队
            "Experimental Hip Hop",//实验嘻哈
            "Experimental Rock",//实验摇滚
            "Field Recordings",//田野录音
            "Film Score",//电影配乐
            "Film Soundtrack",//电影原声
            "Folk Baroque",//民谣巴洛克
            "Folk Revival",//复兴民歌运动
            "Folk Rock",//民谣摇滚
            "Freakbeat",//怪拍
            "Free Improvisation",//自由即兴
            "Free Jazz",//自由爵士
            "Funk",//放克
            "Funk Rock",//放克摇滚
            "Gangsta Rap",//匪帮说唱
            "Garage Rock",//车库摇滚
            "Glam Rock",//迷惑摇滚
            "Glitch Pop",//脉冲流行
            "Gospel",//福音
            "Gothic Rock",//歌特摇滚
            "Grunge",//垃圾摇滚
            "Hard Bop",//硬波普
            "Hard Rock",//硬摇滚
            "Hardcore Hip Hop",//硬核嘻哈
            "Hardcore Punk",//硬核朋克
            "Heartland Rock",//中心地带摇滚
            "Heavy Metal",//重金属
            "Heavy Psych",//重迷幻
            "Hip Hop",//嘻哈
            "Hip Hop / Rap",//说唱
            "Honky Tonk",//酒吧音乐
            "Horror Punk",//恐怖朋克
            "House",//浩室
            "IDM",//智能舞曲
            "Indie Folk",//独立民谣
            "Indie Pop",//独立流行
            "Indie Rock",//独立摇滚
            "Industrial",//工业音乐
            "Industrial Metal",//工业金属
            "Industrial Rock",//工业摇滚
            "Instrumental Hip Hop",//器乐嘻哈
            "Jangle Pop",//争吵流行
            "Jazz",//爵士
            "Jazz Fusion",//爵士融合
            "Jazz Rap",//爵士说唱
            "Jazz-Rock",//爵士摇滚
            "Jump-Blues",//跃式曲唱
            "Krautrock",//德国摇滚
            "Lo-Fi Indie",//低保真独立
            "Math Rock",//数学摇滚
            "Merseybeat",//默西之声
            "Midwest Emo",//中西部情绪摇滚
            "Minimalism",//极简主义
            "Mod",//摩登派
            "Mod Revival",//摩登派复兴
            "Modal Jazz",//调式爵士
            "Modern Classical",//现代古典
            "Neo-Psychedelia",//新迷幻
            "New Wave",//新浪潮
            "No Wave",//无浪潮
            "Noise Pop",//噪音流行
            "Noise Rock",//噪音摇滚
            "NWOBHM",//英国重金属新浪潮
            "Outlaw Country",//叛道乡村
            "Piano Rock",//钢琴摇滚
            "Plunderphonics",//掠夺采样
            "Pop",//流行
            "Pop Punk",//流行朋克
            "Pop Rap",//流行说唱
            "Pop Reggae",//流行雷鬼
            "Pop Rock",//流行摇滚
            "Pop Soul",//流行灵乐
            "Post-Bop",//后波普
            "Post-Hardcore",//后硬核
            "Post-Punk",//后朋克
            "Post-Punk Revival",//后朋克复兴
            "Post-Rock",//后摇滚
            "Power Metal",//力量金属
            "Power Pop",//强力流行
            "Progressive Electronic",//前卫电子
            "Progressive Folk",//前卫民谣
            "Progressive Metal",//前卫金属
            "Progressive Pop",//前卫流行
            "Progressive Rock",//前卫摇滚
            "Progressive Rock",//迷幻摇滚
            "Proto-Punk",//原型朋克
            "Psychedelic Pop",//迷幻流行
            "Psychedelic Soul",//迷幻灵歌
            "Punk Rock",//朋克
            "Reggae",//雷鬼
            "Rhythm & Blues",//节奏蓝调
            "R&B",
            "Rock",//摇滚
            "Rock & Roll",//摇滚
            "Rock Opera",//摇滚歌剧
            "Rockabilly",//山地摇滚
            "Romanticism",//浪漫主义
            "Roots Reggae",//根源雷鬼
            "Roots Rock",//根源摇滚
            "Shoegaze",//自赏派
            "Singer/Songwriter",//唱作人
            "Ska",//斯卡
            "Smooth Soul",//轻松灵歌
            "Soul",//灵魂乐
            "Sound Collage",//声音拼贴
            "Southern Rock",//南方摇滚
            "Southern Soul",//南方灵歌
            "Space Rock",//太空摇滚
            "Spaghetti Western",//意大利式西部片
            "Speed Metal",//速度金属
            "Spiritual Jazz",//灵性爵士
            "Spoken Word",//诵读音乐
            "Stoner Metal",//石人金属
            "Stoner Rock",//石人摇滚
            "Sunshine Pop",//阳光流行
            "Surf Rock",//冲浪摇滚
            "Swamp Rock",//沼泽摇滚
            "Symphonic Metal",//交响金属
            "Symphonic Prog",//交响前卫
            "Symphonic Rock",//交响摇滚
            "Symphony",//交响乐
            "Synth Funk",//合成放克
            "Synthpop",//合成流行
            "Technical Death Metal",//技术死亡金属
            "Third Stream",//第三乐派
            "Thrash Metal",//激流金属
            "Traditional Country",//传统乡村
            "Traditional Doom Metal",//传统厄运金属
            "Trip Hop",//神游舞曲
            "Tropicália",//热带主义
            "Turntablism",//唱盘主义
            "Twee Pop",//矫饰流行
    };

    public static ArrayList<String> getGenres() {
        return new ArrayList<>(Arrays.asList(GENRES));
    }
}
