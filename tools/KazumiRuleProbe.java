import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.io.FileReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class KazumiRuleProbe {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Usage: KazumiRuleProbe <rule.json> [keyword]");
        }
        String keyword = args.length > 1 ? args[1] : "进击";
        Rule rule = new Gson().fromJson(new FileReader(args[0]), Rule.class);
        String searchUrl = rule.searchURL.replace("@keyword", URLEncoder.encode(keyword, StandardCharsets.UTF_8.name()));
        Document searchDoc = Jsoup.connect(searchUrl)
                .userAgent(rule.userAgent == null || rule.userAgent.isEmpty() ? DEFAULT_UA : rule.userAgent)
                .referrer(rule.baseURL)
                .timeout(12000)
                .get();
        List<JXNode> items = JXDocument.create(searchDoc).selN(rule.searchList);
        String title = "";
        String href = "";
        String cover = "";
        int lineCount = 0;
        int episodeCount = 0;
        if (!items.isEmpty()) {
            JXNode first = items.get(0);
            title = text(first, rule.searchName);
            href = attr(first, rule.searchResult, "href");
            cover = firstImage(first);
            if (!href.isEmpty()) {
                String detailUrl = absolute(rule.baseURL, href);
                Document detailDoc = Jsoup.connect(detailUrl)
                        .userAgent(rule.userAgent == null || rule.userAgent.isEmpty() ? DEFAULT_UA : rule.userAgent)
                        .referrer(rule.baseURL)
                        .timeout(12000)
                        .get();
                List<JXNode> roads = JXDocument.create(detailDoc).selN(rule.chapterRoads);
                lineCount = roads.size();
                if (!roads.isEmpty()) {
                    episodeCount = roads.get(0).sel(rule.chapterResult).size();
                }
            }
        }
        System.out.println(rule.name + "\titems=" + items.size()
                + "\ttitle=" + title
                + "\thref=" + href
                + "\tcover=" + cover
                + "\tlines=" + lineCount
                + "\tepisodes=" + episodeCount);
    }

    private static String text(JXNode root, String xpath) {
        List<JXNode> nodes = root.sel(xpath);
        if (nodes == null || nodes.isEmpty()) return "";
        JXNode node = nodes.get(0);
        if (node.isElement()) return node.asElement().text().trim();
        return node.toString().trim();
    }

    private static String attr(JXNode root, String xpath, String attr) {
        List<JXNode> nodes = root.sel(xpath);
        if (nodes == null || nodes.isEmpty()) return "";
        JXNode node = nodes.get(0);
        if (!node.isElement()) return "";
        return node.asElement().attr(attr);
    }

    private static String firstImage(JXNode root) {
        if (!root.isElement()) return "";
        Element img = root.asElement().selectFirst("img");
        if (img == null) return "";
        String[] attrs = new String[]{"data-original", "data-src", "data-lazy-src", "data-url", "data-img", "src"};
        for (String attr : attrs) {
            String value = img.attr(attr);
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private static String absolute(String baseUrl, String href) {
        return URI.create(baseUrl).resolve(href).toString();
    }

    private static final String DEFAULT_UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36";

    private static class Rule {
        String name;
        @SerializedName("baseURL")
        String baseURL;
        String searchURL;
        String searchList;
        String searchName;
        String searchResult;
        String chapterRoads;
        String chapterResult;
        String userAgent;
    }
}
