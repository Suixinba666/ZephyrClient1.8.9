package tech.imxianyu.utils.lyric;

import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import tech.imxianyu.management.FontManager;
import tech.imxianyu.rendering.font.ZFontRenderer;
import tech.imxianyu.widget.impl.MusicLyrics;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网易云音乐歌词解析器
 */
public class LyricParser {

    public static List<LyricBean> parse(JsonObject input) {
//        System.out.println(input);

        if (input.has("uncollected")) {

            LyricBean lyricBean = new LyricBean(0, "00:00", "暂无歌词");

            return Collections.singletonList(lyricBean);
        }

        List<LyricBean> lyricBeans = _parse(input.getAsJsonObject("lrc").get("lyric").getAsString());

        if (input.has("tlyric")) {

            String tLyric = input.getAsJsonObject("tlyric").get("lyric").getAsString();

            if (!tLyric.isEmpty()) {
                MusicLyrics.hasTransLyrics = true;
                // 替换下一些神必空格
                List<LyricBean> translates = _parse(tLyric.replaceAll(" ", " ").replaceAll(" ", " ").replaceAll("　", " "));

                Map<Long, String> transMap = new HashMap<>();
                for (LyricBean t : translates) {
                    transMap.put(t.timeStamp, t.lyric);
                }

                for (LyricBean l : lyricBeans) {
                    String s = transMap.get(l.timeStamp);
                    if (s != null) {

                        if (l.cLyric != null)
                            continue;

                        l.cLyric = s;
                    }
                }
            }
        }

        // 日文罗马音解析
        if (input.has("romalrc")) {

            String romalrc = input.getAsJsonObject("romalrc").get("lyric").getAsString();

            if (!romalrc.isEmpty()) {
                MusicLyrics.hasRomalrc = true;
                // 替换下一些神必空格
                List<LyricBean> translates = _parse(romalrc.replaceAll(" ", " ").replaceAll(" ", " ").replaceAll("　", " "));

                Map<Long, String> transMap = new HashMap<>();
                for (LyricBean t : translates) {
                    transMap.put(t.timeStamp, t.lyric);
                }

                for (LyricBean l : lyricBeans) {
                    String s = transMap.get(l.timeStamp);
                    if (s != null) {

                        if (l.rLyric != null)
                            continue;

                        l.rLyric = s;
                    }
                }
            }



        }

        if (input.has("yrc")) {
            // 替换下一些神必空格
            String scroll = input.getAsJsonObject("yrc").get("lyric").getAsString().replaceAll(" ", " ").replaceAll(" ", " ").replaceAll("　", " ");;

            // 分割换行
            String[] split = scroll.split("\\n");
            if (split.length == 1)
                split = scroll.split("\\\\n");

            for (String s : split) {
                if (!s.startsWith("["))
                    continue;

                boolean cont = false;
                for (String s1 : deleteList) {
                    if (s.contains(s1)) {
                        cont = true;
                        break;
                    }
                }
                if (cont)
                    continue;

//                System.out.println(s);

                String s1 = getInnerString(s, "[", "]");
                String[] s1s = s1.split(",");
                long startDuration = Long.parseLong(s1s[0]);
                long duration = Long.parseLong(s1s[1]);

                String timings = s.substring(s.indexOf("]") + 1);

                MusicLyrics.ScrollTiming t = new MusicLyrics.ScrollTiming();
                t.start = startDuration;
                t.duration = duration;
                t.text = timings;

                List<Long> ti = new ArrayList<>();
                String ser = timings;
                // 累加一下方便以后用
                long sumDuration = 0;
                while (ser.contains("(")) {
                    String inn = getInnerString(ser, "(", ")");
//                    System.out.println(inn);
                    sumDuration += Long.parseLong(inn.split(",")[1]);
                    ti.add(sumDuration);
//                    System.out.println(ser.substring(ser.indexOf(")") + 1, ));

                    ser = ser.substring(inn.length() + 2);
                }

                String wordSplit = timings;

                while (wordSplit.contains("(")) {
                    wordSplit = wordSplit.replace(StringUtils.substringBetween(wordSplit, "(", ")"), "").replace("()", "%");
                }
                wordSplit = wordSplit.substring(1);
                t.totalLyric = wordSplit.replaceAll("%", "").trim();
                String[] spl = wordSplit.split("%");
                for (int i = 0; i < spl.length; i++) {
                    MusicLyrics.WordTiming tim = new MusicLyrics.WordTiming();
                    tim.word = spl[i];
                    tim.timing = ti.get(i);
                    t.timings.add(tim);
                }

                MusicLyrics.timings.add(t);
            }

        }

        return lyricBeans;
    }

    private static String getInnerString(String text, String left, String right) {
        int zLen;
        String result;
        zLen = left == null || left.isEmpty() ? 0 : ((zLen = text.indexOf(left)) > -1 ? zLen + left.length() : 0);
        int yLen = text.indexOf(right, zLen);
        if (yLen < 0 || right.isEmpty()) {
            yLen = text.length();
        }
        result = text.substring(zLen, yLen);
        return result;
    }

    static List<String> deleteList = Arrays.asList("编曲", "作曲", "作词", "編曲", "曲:", "词:", "曲：", "词：");

    private static List<LyricBean> _parse(String input) {
        deleteList = Arrays.asList("编曲", "作曲", "作词", "作者", "編曲");
        List<LyricBean> lyricBeans = new ArrayList<>();
        String[] split = input.split("\\n");
        if (split.length == 1)
            split = input.split("\\\\n");

        for (String s : split) {
            List<LyricBean> l = parseLine(s);

            if (l != null && !l.isEmpty()) {

                Iterator<LyricBean> it = l.iterator();

                while (it.hasNext()) {
                    LyricBean ll = it.next();

                    boolean shouldRemove = false;

                    for (String s1 : deleteList) {
                        if (ll.getLyric().contains(s1) && ll.getTimeStamp() <= 20000) {
//                            System.out.println(ll.getLyric() + ", " + s1);
                            shouldRemove = true;
                            break;
                        }
                    }

                    if (shouldRemove) {
                        it.remove();
                    }
                }

                lyricBeans.addAll(l);
            }
        }

        lyricBeans.sort((lrcBean, lrcBean2) -> (int) (lrcBean.getTimeStamp() - lrcBean2.getTimeStamp()));

        return lyricBeans;
    }

    private static List<LyricBean> fitWidth(List<LyricBean> lyricBeans) {
        ZFontRenderer fontRenderer = FontManager.pf25;

        List<LyricBean> add = new ArrayList<>();

        Iterator<LyricBean> iterator = lyricBeans.iterator();

        int count = 0;
        while (iterator.hasNext()) {
            LyricBean lrc = iterator.next();

            if (fontRenderer.isTextWidthLargerThanWidth(lrc.lyric, 350)) {
                iterator.remove();
                count -= 1;

                String[] lrcs = fontRenderer.fitWidth(lrc.lyric, 330);

                LyricBean lrcNext = null;

                if (count < lyricBeans.size() - 1) {
                    lrcNext = lyricBeans.get(count + 1);
                }

                long nextTs = lyricBeans.get(lyricBeans.size() - 1).timeStamp;

                if (lrcNext != null)
                    nextTs = lrcNext.timeStamp;

                long elapsedStamp = 0;

                double totalW = fontRenderer.getStringWidth(lrc.lyric);

//                System.out.println("Till Next: " + nextTs);
//                System.out.println("Available: " + (nextTs - lrc.timeStamp));

                for (int i = 0; i < lrcs.length; i++) {

                    double width = fontRenderer.getStringWidth(lrcs[i]);

                    /* + ((nextTs - lrc.timeStamp) * (width / totalW))*/
                    long timestamp = lrc.timeStamp + elapsedStamp;

                    if (i == 0) {
                        timestamp = lrc.timeStamp;
                    }

                    elapsedStamp += (nextTs - lrc.timeStamp) * (width / totalW);
//                    System.out.println("Elapse: " + (nextTs - lrc.timeStamp) * (width / totalW));


                    add.add(new LyricBean(timestamp, "FitWidth", lrcs[i]));
//                    System.out.println(lrcs[i] + ": " + (timestamp));
                }

            }

            count++;
        }

        lyricBeans.addAll(add);

        lyricBeans.sort((lrcBean, lrcBean2) -> (int) (lrcBean.getTimeStamp() - lrcBean2.getTimeStamp()));
        return lyricBeans;
    }

    private static String getInnerText(String left, String right, String text) {
        if (left.isEmpty() || right.isEmpty() || text.isEmpty()) {
            return "";
        }

        int i = text.indexOf(left);
        int j = text.indexOf(right, i);
        if (i == -1 || j == -1)
            return "";

        return text.substring(i + left.length(), j);
    }

    private static List<LyricBean> parseLine(String input) {
        if (input.isEmpty()) {
            return null;
        }
        // 去除空格
        input = input.trim();
        // 正则表达式，判断s中是否有[00:00.60]或[00:00.600]格式的片段
        Matcher lineMatcher = Pattern.
                compile("((\\[\\d{2}:\\d{2}\\.\\d{2,3}\\])+)(.+)").matcher(input);
        // 如果没有，返回null
        if (!lineMatcher.matches()) {
            return null;
        }
        // 得到时间标签
        String times = lineMatcher.group(1);
        // 得到歌词文本内容
        String text = lineMatcher.group(3);
        List<LyricBean> entryList = new ArrayList<>();
        Matcher timeMatcher = Pattern.compile("\\[(\\d\\d):(\\d\\d)\\.(\\d{2,3})\\]").matcher(times);
        while (timeMatcher.find()) {
            long min = Long.parseLong(timeMatcher.group(1));// 分
            long sec = Long.parseLong(timeMatcher.group(2));// 秒
            long mil = Long.parseLong(timeMatcher.group(3));// 毫秒
            // 转换为long型时间
            int scale_mil = mil > 100 ? 1 : 10;//如果毫秒是3位数则乘以1，反正则乘以10
            // 转换为long型时间
            long time =
                    min * 60000 +
                            sec * 1000 +
                            mil * scale_mil;
            // 最终解析得到一个list
            entryList.add(new LyricBean(time, times, text.replaceAll(" ", " ")));
        }
        return entryList;
    }
}
