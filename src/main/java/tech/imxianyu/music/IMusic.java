package tech.imxianyu.music;

public interface IMusic {
    long getId();

    String getName();

    String getPicUrl();

    String getPicUrl(int size);

    String getPlayUrl();

    String getArtists();

    long getDuration();


    /**
     * 获取歌曲长度 (秒)
     *
     * @return 秒
     */
    default int getDurationSecond() {
        return (int) (getDuration() / 1000);
    }

}
