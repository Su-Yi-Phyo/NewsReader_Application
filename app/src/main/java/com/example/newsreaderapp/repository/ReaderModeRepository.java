package com.example.newsreaderapp.repository;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;

/**
 * Minimal “reader mode” extractor.
 * NOTE: This is a synchronous method — call it OFF the main thread.
 */
public class ReaderModeRepository {

    private static final int TIMEOUT_MS = 15000;
    private static final String UA =
            "Mozilla/5.0 (Android) AppleWebKit/537.36 (KHTML, like Gecko) Chrome Mobile Safari/537.36";

    /** Fetches the URL and returns cleaned HTML (basic article content). */
    public String getReadableHtml(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent(UA)
                .timeout(TIMEOUT_MS)
                .get();

        // Remove non-content noise
        doc.select("script, style, noscript, iframe, form, header, footer, nav, aside, svg").remove();

        // Try common content containers
        Element content = firstNonNull(
                doc.selectFirst("article"),
                doc.selectFirst("main"),
                doc.selectFirst("[role=main]"),
                doc.selectFirst("div[id*=content]"),
                doc.selectFirst("div[class*=content]"),
                doc.selectFirst("div[class*=article]"),
                doc.selectFirst("section[class*=content]")
        );

        if (content == null) {
            // Fallback: pick the largest text block-ish div
            content = pickLargestTextBlock(doc);
            if (content == null) content = doc.body();
        }

        // Drop likely junk inside content
        content.select("ul.share, .share, .social, .comment, .comments, .related, .subscribe, .cookie, .newsletter").remove();

        // Build a very simple HTML doc (keeps basic tags like <p>, <img>, <h1-6>, <a>, etc.)
        String title = doc.title() != null ? doc.title() : "";
        String bodyInner = content.html();

        return "<!DOCTYPE html><html><head><meta charset=\"utf-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "<title>" + escape(title) + "</title>" +
                "<style>img{max-width:100%;height:auto;} body{font-family:sans-serif;line-height:1.6;padding:12px;} h1,h2,h3{line-height:1.25;}</style>" +
                "</head><body>" +
                "<h2>" + escape(title) + "</h2>" +
                bodyInner +
                "</body></html>";
    }

    private Element firstNonNull(Element... els) {
        for (Element e : els) if (e != null) return e;
        return null;
    }

    private Element pickLargestTextBlock(Document doc) {
        Elements candidates = doc.select("article, main, [role=main], section, div");
        Element best = null;
        int bestScore = -1;
        for (Element el : candidates) {
            // crude heuristic: length of own text + number of <p> children
            int score = el.text().length() + (el.select("p").size() * 200);
            if (score > bestScore) {
                bestScore = score;
                best = el;
            }
        }
        return best;
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}