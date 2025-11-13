package com.example.newsreaderapp.repository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ReaderModeRepository {

    // Stronger headers help with anti-bot/CDN checks
    private static final String UA =
            "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119 Mobile Safari/537.36";

    /** Try to return *plain text* of the article (best for your TextView). */
    public String getReadableText(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent(UA)
                .referrer("https://www.google.com/")
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .timeout(12000)
                .maxBodySize(0) // allow large pages
                .get();

        // remove noise early
        doc.select("script, style, noscript, iframe, form, header, footer, nav, aside, svg, .ad, [class*=ad-], [id*=ad-], .advert, .share, .social, .cookie, .newsletter, .comments").remove();

        Element root = pickArticleRoot(url, doc);
        if (root == null) root = doc.body();

        // prefer structured blocks
        Elements blocks = root.select("p, h1, h2, h3, h4, li");
        StringBuilder sb = new StringBuilder();
        if (!blocks.isEmpty()) {
            for (Element el : blocks) {
                String t = el.text().trim();
                if (t.isEmpty()) continue;

                String tag = el.tagName();
                if (tag.matches("h[1-4]")) {
                    if (sb.length() > 0) sb.append("\n\n");
                    sb.append(t).append("\n");
                } else if ("li".equals(tag)) {
                    sb.append("• ").append(t).append("\n");
                } else { // paragraph
                    if (t.length() < 30) continue; // skip tiny crumbs
                    if (sb.length() > 0) sb.append("\n\n");
                    sb.append(t);
                }
            }
        }

        if (sb.length() < 200) {
            // fallback: dump all text from root
            String all = root.text().trim();
            if (!all.isEmpty()) return all;
        }
        return sb.toString();
    }

    /** Domain-specific roots first, then generic heuristics. */
    private Element pickArticleRoot(String url, Document doc) {
        String host = hostOf(url);

        // 1) domain overrides
        Element e = selectByDomain(host, doc);
        if (e != null && e.text().length() > 200) return e;

        // 2) generic common containers
        String[] sels = new String[]{
                "article[role=main]", "main article", "article",
                "[role=main]", "main",
                "div[itemprop=articleBody]",
                ".article-body", ".article-content", ".entry-content",
                ".post-content", ".story-body", ".content-body",
                "#article-body", "#main-content", ".main-content",
                "[class*=article-content]", "[class*=post-content]", "[id*=article-body]"
        };
        for (String s : sels) {
            Element cand = doc.selectFirst(s);
            if (cand != null && cand.text().length() > 200) return cand;
        }

        // 3) heuristic: largest text block
        return pickLargestTextBlock(doc);
    }

    private Element selectByDomain(String host, Document doc) {
        if (host == null) return null;

        // simple map of site-specific selectors (add as you encounter sites)
        Map<String, String[]> map = new HashMap<>();
        map.put("www.washingtonpost.com", new String[]{
                "article", "div[data-qa=article-body]", "div#main-content"
        });
        map.put("electrek.co", new String[]{
                "article", ".entry-content", ".single-content", "main article"
        });
        map.put("www.theringer.com", new String[]{
                "article", ".c-entry-content", ".article-body", "main article"
        });
        map.put("www.theverge.com", new String[]{
                "article", ".c-entry-content", "main article"
        });

        String[] sels = map.get(host);
        if (sels == null) return null;
        for (String s : sels) {
            Element cand = doc.selectFirst(s);
            if (cand != null && cand.text().length() > 200) return cand;
        }
        return null;
    }

    private Element pickLargestTextBlock(Document doc) {
        Elements candidates = doc.select("article, main, [role=main], section, div");
        Element best = null;
        int bestScore = -1;
        for (Element el : candidates) {
            int score = el.text().length() + (el.select("p").size() * 200);
            if (score > bestScore) { bestScore = score; best = el; }
        }
        return best;
    }

    private String hostOf(String url) {
        try { return URI.create(url).getHost(); } catch (Exception ignored) { return null; }
    }
}
