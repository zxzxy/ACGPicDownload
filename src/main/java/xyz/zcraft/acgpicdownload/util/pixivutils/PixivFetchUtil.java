package xyz.zcraft.acgpicdownload.util.pixivutils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

public class PixivFetchUtil {
    private static final String TOP = "https://www.pixiv.net/ajax/top/illust?mode=all";
    private static final String RELATED = "https://www.pixiv.net/ajax/illust/%s/recommend/init?limit=%d";
    private static final String ARTWORK = "https://www.pixiv.net/artworks/";
    private static final String USER = "https://www.pixiv.net/ajax/user/%s/profile/all?";
    private static final String USER_TAGS = "https://www.pixiv.net/ajax/tags/frequent/illust?%s";
    private static final String USER_WORKS = "https://www.pixiv.net/ajax/user/%s/profile/illusts?%s&work_category=illust&is_first_page=1";
    private static final String DISCOVERY = "https://www.pixiv.net/ajax/discovery/artworks?mode=%s&limit=%d";
    private static final String GIF_DATA = "https://www.pixiv.net/ajax/illust/%s/ugoira_meta";
    private static final String PAGES = "https://www.pixiv.net/ajax/illust/%s/pages";
    private static final String SEARCH_TOP = "https://www.pixiv.net/ajax/search/top/%s";
    private static final String SEARCH_ILLUST = "https://www.pixiv.net/ajax/search/illustrations/%s?word=%s&mode=%s&p=%d";
    private static final String SEARCH_MANGA = "https://www.pixiv.net/ajax/search/manga/%s?word=%s&mode=%s&p=%d";

    private static final String RANKING = "https://www.pixiv.net/ranking.php";
    private static final String[] DISCOVERY_MODES = {"all", "safe", "r18"};

    public static PixivAccount getAccount(String cookieString, String proxyHost, Integer proxyPort) {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect("https://www.pixiv.net")
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        try {
            String text = Objects.requireNonNull(c.get().getElementById("meta-global-data")).attr("content");
            PixivAccount userData = JSONObject.parseObject(text).getJSONObject("userData").to(PixivAccount.class);
            if (userData.getName() != null && userData.getId() != null) {
                return userData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param keyword      ???????????????
     * @param cookieString ?????????cookie
     * @param proxyHost    ?????????????????????
     * @param proxyPort    ?????????????????????
     * @return ????????????
     * @throws IOException ???????????????
     */
    public static List<PixivArtwork> searchTopArtworks(String keyword, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(String.format(SEARCH_TOP, keyword).concat("?lang=").concat(getPixivLanguageTag()))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        String s = c.get().body().ownText();
        JSONObject body = JSONObject.parseObject(s).getJSONObject("body");

        JSONObject popular = body.getJSONObject("popular");

        LinkedList<PixivArtwork> artworks = new LinkedList<>();
        for (Object o : popular.getJSONArray("recent")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }
        for (Object o : popular.getJSONArray("permanent")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }
        for (Object o : body.getJSONObject("illustManga").getJSONArray("data")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }

        return artworks;
    }

    public static List<PixivArtwork> searchIllustArtworks(String keyword, int mode, int page, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        String url = String.format(SEARCH_ILLUST, keyword, keyword, DISCOVERY_MODES[mode], page).concat("&lang=").concat(getPixivLanguageTag());
        Connection c = Jsoup.connect(url)
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        String s = c.get().body().ownText();
        JSONObject body = JSONObject.parseObject(s).getJSONObject("body");

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        JSONObject popular = body.getJSONObject("popular");

        LinkedList<PixivArtwork> artworks = new LinkedList<>();
        for (Object o : popular.getJSONArray("recent")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }
        for (Object o : popular.getJSONArray("permanent")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }
        for (Object o : body.getJSONObject("illust").getJSONArray("data")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }

        return artworks;
    }

    public static List<PixivArtwork> searchMangaArtworks(String keyword, int mode, int page, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        String url = String.format(SEARCH_MANGA, keyword, keyword, DISCOVERY_MODES[mode], page).concat("&lang=").concat(getPixivLanguageTag());
        Connection c = Jsoup.connect(url)
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        String s = c.get().body().ownText();
        JSONObject body = JSONObject.parseObject(s).getJSONObject("body");

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        JSONObject popular = body.getJSONObject("popular");

        LinkedList<PixivArtwork> artworks = new LinkedList<>();
        for (Object o : popular.getJSONArray("recent")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }
        for (Object o : popular.getJSONArray("permanent")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }
        for (Object o : body.getJSONObject("manga").getJSONArray("data")) {
            PixivArtwork e = JSONObject.parseObject(o.toString(), PixivArtwork.class);
            e.setSearch(keyword);
            e.setFrom(From.Search);
            artworks.add(e);
        }

        return artworks;
    }

    /**
     * ???pixiv??????????????????????????????id
     *
     * @param major        ???????????????
     * @param minor        ????????????
     * @param cookieString ?????????cookie??????cookie????????????????????????403
     * @param proxyHost    ?????????????????????
     * @param proxyPort    ?????????????????????
     * @return ID??????
     * @throws IOException ??????????????????
     */
    public static LinkedList<String> getRankingIDs(@NotNull String major, String minor, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        String url = RANKING.concat("?mode=").concat(major).concat(minor != null && !minor.isEmpty() ? "&content=".concat(minor) : "");
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(url)
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        Elements rankingItems = c.get().body().getElementsByClass("ranking-item");
        LinkedList<String> ids = new LinkedList<>();
        for (Element rankingItem : rankingItems) {
            String href = rankingItem.getElementsByClass("ranking-image-item").get(0).getElementsByTag("a").get(0).attr("href");
            String id = href.substring(href.lastIndexOf("/") + 1);
            ids.add(id);
        }

        return ids;
    }

    /**
     * ????????????{@link PixivArtwork}???{@link GifData}
     *
     * @param artwork      ????????????
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @return Gif??????
     * @throws IOException ??????????????????
     */
    public static GifData getGifData(@NotNull PixivArtwork artwork, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(String.format(GIF_DATA, artwork.getId()).concat("?lang=")
                        .concat(getPixivLanguageTag()))
                .ignoreContentType(true)
                .cookies(cookie)
                .method(Method.GET)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }
        JSONObject body = JSONObject.parseObject(c.get().body().ownText()).getJSONObject("body");
        GifData gifData = body.to(GifData.class);
        artwork.setGifData(gifData);
        return gifData;
    }

    /**
     * ??????????????????????????????
     *
     * @param artwork      ????????????
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @return ??????????????????
     * @throws IOException ??????????????????
     */
    public static LinkedList<String> getFullPages(@NotNull PixivArtwork artwork, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(String.format(PAGES, artwork.getId()).concat("?lang=")
                        .concat(getPixivLanguageTag()))
                .ignoreContentType(true)
                .cookies(cookie)
                .method(Method.GET)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }
        LinkedList<String> urls = new LinkedList<>();
        JSONArray body = JSONObject.parseObject(c.get().body().ownText()).getJSONArray("body");
        for (int i = 0; i < body.size(); i++) {
            urls.add(body.getJSONObject(i).getJSONObject("urls").getString("original"));
        }
        return urls;
    }

    /**
     * ????????????id?????????
     *
     * @param id           ??????ID
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @return ????????????
     * @throws IOException ??????????????????
     */
    public static PixivArtwork getArtwork(@NotNull String id, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(getArtworkPageUrl(id))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }
        JSONObject jsonObject = JSONObject.parseObject(Objects.requireNonNull(c.get().head().getElementById("meta-preload-data")).attr("content")).getJSONObject("illust").getJSONObject(id);
        PixivArtwork pixivArtwork = jsonObject.to(PixivArtwork.class);
        pixivArtwork.setOrigJson(jsonObject);
        JSONArray jsonArray = jsonObject.getJSONObject("tags").getJSONArray("tags");
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            tags.add(jsonArray.getJSONObject(i).getString("tag"));
        }
        pixivArtwork.setTranslatedTags(tags);
        return pixivArtwork;
    }

    /**
     * ??????????????????????????????bookmarkCount???likeCount??????
     *
     * @param artwork      ?????????
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @throws IOException ??????????????????
     */
    public static void getFullData(@NotNull PixivArtwork artwork, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(getArtworkPageUrl(artwork.getId()))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }
        JSONObject jsonObject = JSONObject.parseObject(Objects.requireNonNull(c.get().head().getElementById("meta-preload-data")).attr("content")).getJSONObject("illust").getJSONObject(artwork.getId());
        artwork.setBookmarkCount(jsonObject.getInteger("bookmarkCount"));
        artwork.setLikeCount(jsonObject.getInteger("likeCount"));
    }

    /**
     * ???????????????
     *
     * @param mode         ??????(0=?????????1=?????????2=?????????)
     * @param limit        ?????????????????????1~100???
     * @param cookieString ?????????cookie????????????????????????????????????
     * @param proxyHost    ?????????????????????
     * @param proxyPort    ?????????????????????
     * @return ????????????
     * @throws IOException ??????????????????
     */
    public static List<PixivArtwork> getDiscovery(
            @Range(from = 0, to = 2) int mode,
            @Range(from = 0, to = 100) int limit,
            @NotNull String cookieString,
            String proxyHost,
            Integer proxyPort
    ) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(String.format(DISCOVERY, DISCOVERY_MODES[mode], limit).concat("&lang=").concat(getPixivLanguageTag()))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        return parseArtworks(c.get().body().ownText());
    }

    /**
     * ?????????????????????????????????
     *
     * @param uid       ??????UID
     * @param proxyHost ????????????
     * @param proxyPort ????????????
     * @return ???????????????ID
     * @throws IOException ??????????????????
     */
    public static Set<String> fetchUser(@NotNull String uid, String proxyHost, Integer proxyPort) throws IOException {
        Connection c = Jsoup.connect(String.format(USER, uid).concat("lang=").concat(getPixivLanguageTag()))
                .ignoreContentType(true)
                .method(Method.GET)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        JSONObject jsonObject = JSONObject.parseObject(c.get().body().ownText()).getJSONObject("body").getJSONObject("illusts");

        return new HashSet<>(jsonObject.keySet());
    }

    /**
     * ??????????????????????????????tag??????
     *
     * @param queryString ??????{@link #buildQueryString(Set)}????????????????????????
     * @param proxyHost   ????????????
     * @param proxyPort   ????????????
     * @return tag??????????????????
     * @throws IOException ??????????????????
     */
    public static HashMap<String, String> getUserTagTranslations(@NotNull String queryString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> tagTranslation = new HashMap<>();
        Connection c = Jsoup.connect(String.format(USER_TAGS, queryString))
                .ignoreContentType(true)
                .method(Method.GET)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        String s = c.get().body().ownText();
        for (Object o : JSONObject.parseObject(s).getJSONArray("body")) {
            if (o instanceof JSONObject obj) {
                tagTranslation.put(obj.getString("tag"), obj.getString("tag_translation"));
            }
        }

        return tagTranslation;
    }

    /**
     * ???????????????????????????
     *
     * @param queryString ??????{@link #buildQueryString(Set)}????????????????????????
     * @param uid         ??????UID
     * @param proxyHost   ????????????
     * @param proxyPort   ????????????
     * @return ????????????
     * @throws IOException ??????????????????
     */
    public static List<PixivArtwork> getUserArtworks(@NotNull String queryString, @NotNull String uid, String proxyHost, Integer proxyPort) throws IOException {
        LinkedList<PixivArtwork> artworks = new LinkedList<>();
        Connection c = Jsoup.connect(String.format(USER_WORKS, uid, queryString))
                .ignoreContentType(true)
                .method(Method.GET)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        JSONObject obj = JSONObject.parseObject(c.get().body().ownText()).getJSONObject("body").getJSONObject("works");

        HashMap<String, String> userTagTranslations = getUserTagTranslations(queryString, proxyHost, proxyPort);
        LinkedHashSet<String> translatedTags = new LinkedHashSet<>();
        for (String k : obj.keySet()) {
            JSONObject jsonObject = obj.getJSONObject(k);
            PixivArtwork a = jsonObject.to(PixivArtwork.class);
            for (Object originalTag : a.getOriginalTags()) {
                String s = Objects.requireNonNullElse(userTagTranslations.get(originalTag.toString()), originalTag.toString());
                translatedTags.add(s);
            }
            a.setTranslatedTags(translatedTags);
            a.setFrom(From.User);
            a.setOrigJson(jsonObject);
            artworks.add(a);
        }

        return artworks;
    }

    /**
     * ????????????????????????pixiv????????????????????????
     *
     * @return pixiv????????????????????????
     */
    public static String getPixivLanguageTag() {
        switch (Locale.getDefault().toLanguageTag().toLowerCase()) {
            case "zh-cn", "zh_cn", "zh" -> {
                return "zh";
            }
            case "zh_tw", "zh-tw" -> {
                return "zh_tw";
            }
            default -> {
                return "en";
            }
        }
    }

    /**
     * ????????????ID?????????????????????
     *
     * @param ids ??????id
     * @return ?????????????????????
     */
    public static List<String> buildQueryString(Set<String> ids) {
        LinkedList<String> list = new LinkedList<>();
        StringBuilder s = new StringBuilder();
        int count = 0;
        for (String t : ids) {
            if (count >= 48) {
                s.append("&lang=").append(getPixivLanguageTag());
                list.add(s.toString());
                count = 0;
                s = new StringBuilder();
            } else {
                s.append("&ids%5B%5D=").append(t);
                count++;
            }
        }
        if (s.length() > 0) {
            s.append("&lang=").append(getPixivLanguageTag());
            list.add(s.toString());
        }
        return list;
    }

    /**
     * ??????????????????
     *
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @return ????????????
     * @throws IOException ??????????????????
     */
    public static List<PixivArtwork> fetchMenu(String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(TOP.concat("&lang=").concat(getPixivLanguageTag()))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        return parseArtworks(c.get().body().ownText(), true);
    }

    /**
     * ??????json????????????
     *
     * @param jsonString ???json?????????
     * @param classify   ??????????????????
     * @return ???????????????
     */
    public static LinkedList<PixivArtwork> parseArtworks(String jsonString, boolean classify) {
        LinkedList<PixivArtwork> artworks = new LinkedList<>();
        JSONObject bodyObject = JSONObject.parse(jsonString).getJSONObject("body");
        JSONObject tran = bodyObject.getJSONObject("tagTranslation");
        JSONArray illust = bodyObject.getJSONObject("thumbnails").getJSONArray("illust");

        for (int i = 0; i < illust.size(); i++) {
            JSONObject jsonObject = illust.getJSONObject(i);
            PixivArtwork a = jsonObject.to(PixivArtwork.class);
            for (Object t : a.getOriginalTags()) {
                a.getTranslatedTags().add(translateTag(t.toString(), tran));
            }
            a.setOrigJson(jsonObject);
            artworks.add(a);
        }

        if (classify) classifyArtwork(artworks, bodyObject.getJSONObject("page"));

        return artworks;
    }

    /**
     * ??????json????????????
     *
     * @param jsonString ???json?????????
     * @return ???????????????
     */
    public static LinkedList<PixivArtwork> parseArtworks(String jsonString) {
        LinkedList<PixivArtwork> artworks = new LinkedList<>();
        JSONObject bodyObject = JSONObject.parse(jsonString).getJSONObject("body");
        JSONObject tran = bodyObject.getJSONObject("tagTranslation");
        JSONArray illust = bodyObject.getJSONObject("thumbnails").getJSONArray("illust");

        for (int i = 0; i < illust.size(); i++) {
            JSONObject jsonObject = illust.getJSONObject(i);
            PixivArtwork a = jsonObject.to(PixivArtwork.class);
            a.setOrigJson(jsonObject);
            a.setFrom(From.Discovery);
            for (Object t : a.getOriginalTags()) {
                a.getTranslatedTags().add(translateTag(t.toString(), tran));
            }
            artworks.add(a);
        }

        return artworks;
    }

    /**
     * ????????????????????????????????????tag????????????
     *
     * @param tag  ???tag
     * @param tran ?????????
     * @return ????????????tag??????tag
     */
    public static String translateTag(String tag, JSONObject tran) {
        String origLang = Locale.getDefault().toLanguageTag().toLowerCase();
        JSONObject tagObj = tran.getJSONObject(tag);
        if (tagObj == null) return tag;
        switch (origLang) {
            case "zh-cn", "zh_cn", "zh" -> {
                String s = Objects.requireNonNullElse(tagObj.getString("zh"), tag);
                return s.isEmpty() ? tag : s;
            }
            case "zh_tw", "zh-tw" -> {
                String s = Objects.requireNonNullElse(tagObj.getString("zh_tw"), tag);
                return s.isEmpty() ? tag : s;
            }
            case "en" -> {
                String s = Objects.requireNonNullElse(tagObj.getString("en"), tag);
                return s.isEmpty() ? tag : s;
            }
            default -> {
                return tag;
            }
        }
    }

    /**
     * ?????????????????????URL
     *
     * @param artwork      ????????????
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @return ?????????????????????????????????????????????????????????????????????????????????
     * @throws IOException ??????????????????
     * @deprecated ??????????????????????????????????????????????????????????????????{@link #getFullPages(PixivArtwork, String, String, Integer)}
     */
    @Deprecated
    public static String getImageUrl(PixivArtwork artwork, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(getArtworkPageUrl(artwork))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);
        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        return JSONObject.parseObject(Objects.requireNonNull(c.get().head().getElementById("meta-preload-data")).attr("content")).getJSONObject("illust").getJSONObject(artwork.getId()).getJSONObject("urls").getString("original");
    }

    /**
     * ??????????????????
     *
     * @param artwork      ?????????
     * @param limit        ?????????1~50???
     * @param cookieString ?????????cookie
     * @param proxyHost    ????????????
     * @param proxyPort    ????????????
     * @return ??????????????????
     * @throws IOException ??????????????????
     */
    public static List<PixivArtwork> getRelated(@NotNull PixivArtwork artwork, @Range(from = 1, to = 50) int limit, String cookieString, String proxyHost, Integer proxyPort) throws IOException {
        HashMap<String, String> cookie = parseCookie(cookieString);
        Connection c = Jsoup.connect(String.format(RELATED, artwork.getId(), limit))
                .ignoreContentType(true)
                .method(Method.GET)
                .cookies(cookie)
                .timeout(10 * 1000);

        if (proxyHost != null && proxyPort != null && proxyPort != 0) {
            c.proxy(proxyHost, proxyPort);
        }

        String jsonString = c.get().body().ownText();

        LinkedList<PixivArtwork> artworks = new LinkedList<>();

        JSONArray illusts = JSONObject.parse(jsonString).getJSONObject("body").getJSONArray("illusts");

        for (int i = 0; i < illusts.size(); i++) {
            JSONObject jsonObject = illusts.getJSONObject(i);
            PixivArtwork object = jsonObject.to(PixivArtwork.class);
            if (object.getTitle() == null) continue;
            object.setOrigJson(jsonObject);
            object.setFrom(From.Related);
            artworks.add(object);
        }

        return artworks;
    }

    /**
     * ??????????????????URL
     *
     * @param artwork ?????????
     * @return ????????????URL
     */
    public static String getArtworkPageUrl(@NotNull PixivArtwork artwork) {
        return ARTWORK + artwork.getId();
    }

    /**
     * ??????????????????URL
     *
     * @param id ??????id
     * @return ????????????URL
     */
    public static String getArtworkPageUrl(@NotNull String id) {
        return ARTWORK + id;
    }

    /**
     * ??????cookie???????????????cookie?????????
     *
     * @param cookieString cookie?????????
     * @return cookie?????????
     */
    public static HashMap<String, String> parseCookie(String cookieString) {
        if(cookieString == null) return new HashMap<>();
        String[] t = cookieString.split(";");
        HashMap<String, String> cookieMap = new HashMap<>();
        for (String t2 : t) {
            t2 = t2.trim();
            String[] t3 = t2.split("=");
            if (t3.length >= 2) {
                cookieMap.put(t3[0], t3[1]);
            }
        }
        return cookieMap;
    }

    /**
     * ???????????????
     *
     * @param orig     ????????????
     * @param pageJson ??????json
     */
    public static void classifyArtwork(List<PixivArtwork> orig, JSONObject pageJson) {
        LinkedList<String> recommendIDs = new LinkedList<>();
        LinkedList<String> recommendTagIDs = new LinkedList<>();
        LinkedList<String> recommendUserIDs = new LinkedList<>();
        LinkedList<String> followIDs = new LinkedList<>();

        for (Object o : pageJson.getJSONArray("follow")) {
            followIDs.add(o.toString());
        }

        for (Object o : pageJson.getJSONObject("recommend").getJSONArray("ids")) {
            recommendIDs.add(o.toString());
        }

        JSONArray recommendByTag = pageJson.getJSONArray("recommendByTag");
        for (int i = 0; i < recommendByTag.size(); i++) {
            for (Object ids : recommendByTag.getJSONObject(i).getJSONArray("ids")) {
                recommendTagIDs.add(ids.toString());
            }
        }

        JSONArray recommendUser = pageJson.getJSONArray("recommendUser");
        for (int i = 0; i < recommendUser.size(); i++) {
            for (Object ids : recommendUser.getJSONObject(i).getJSONArray("illustIds")) {
                recommendUserIDs.add(ids.toString());
            }
        }

        for (PixivArtwork artwork : orig) {
            String id = artwork.getId();
            if (followIDs.contains(id)) {
                artwork.setFrom(From.Follow);
            } else if (recommendIDs.contains(id)) {
                artwork.setFrom(From.Recommend);
            } else if (recommendTagIDs.contains(id)) {
                artwork.setFrom(From.RecommendTag);
            } else if (recommendUserIDs.contains(id)) {
                artwork.setFrom(From.RecommendUser);
            } else {
                artwork.setFrom(From.Other);
            }
        }
    }

    /**
     * ????????????
     *
     * @param orig          ????????????
     * @param limit         ????????????
     * @param follow        ??????????????????
     * @param recommend     ??????????????????
     * @param recommendTag  ??????????????????tag
     * @param recommendUser ????????????????????????
     * @param other         ??????????????????
     * @return ??????????????????
     */
    public static List<PixivArtwork> selectArtworks(List<PixivArtwork> orig, int limit, boolean follow, boolean recommend, boolean recommendTag, boolean recommendUser, boolean other) {
        LinkedList<PixivArtwork> art = new LinkedList<>();
        for (PixivArtwork artwork : orig) {
            if (limit > art.size()) {
                From from = artwork.getFrom();
                if (from == From.Other && other) art.add(artwork);
                if (from == From.Follow && follow) art.add(artwork);
                if (from == From.Recommend && recommend) art.add(artwork);
                if (from == From.RecommendTag && recommendTag) art.add(artwork);
                if (from == From.RecommendUser && recommendUser) art.add(artwork);
            } else {
                break;
            }
        }

        return art;
    }
}
