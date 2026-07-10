package com.heyanle.easybangumi4.plugin.source.js.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.util.ArrayList;
import java.util.List;

public class XPathUtils {

    public static ArrayList<JXNode> nodes(Object root, String xpath) {
        try {
            List<JXNode> nodes = select(root, xpath);
            return new ArrayList<>(nodes);
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static String text(Object root, String xpath) {
        ArrayList<JXNode> nodes = nodes(root, xpath);
        if (nodes.isEmpty()) {
            return "";
        }
        return textSelf(nodes.get(0));
    }

    public static String attr(Object root, String xpath, String attr) {
        ArrayList<JXNode> nodes = nodes(root, xpath);
        if (nodes.isEmpty()) {
            return "";
        }
        return attrSelf(nodes.get(0), attr);
    }

    public static String textSelf(Object node) {
        JXNode jxNode = toJXNode(node);
        if (jxNode == null) {
            return "";
        }
        if (jxNode.isElement()) {
            return jxNode.asElement().text().trim();
        }
        return jxNode.toString().trim();
    }

    public static String attrSelf(Object node, String attr) {
        JXNode jxNode = toJXNode(node);
        if (jxNode == null || !jxNode.isElement()) {
            return "";
        }
        return jxNode.asElement().attr(attr);
    }

    public static String firstImage(Object root) {
        Element element = toElement(root);
        if (element == null) {
            return "";
        }

        // Try to find a poster image within the element
        String result = findImageInElement(element);
        if (result != null && !result.isEmpty()) {
            return result;
        }

        // If nothing found, try the parent element (handles ylsp-style sites
        // where searchList selects a title div inside a card container)
        Element parent = element.parent();
        if (parent != null) {
            result = findImageInElement(parent);
            if (result != null && !result.isEmpty()) {
                return result;
            }
            // Try grandparent for deeply nested cases
            Element grandparent = parent.parent();
            if (grandparent != null) {
                result = findImageInElement(grandparent);
                if (result != null && !result.isEmpty()) {
                    return result;
                }
            }
        }

        return "";
    }

    /**
     * Core image-finding logic applied to a single element.
     */
    private static String findImageInElement(Element element) {
        // 1. Standard OG meta tags (highest priority)
        Element ogImage = element.selectFirst(
            "meta[property=og:image], meta[name=og:image], meta[name=twitter:image]");
        if (ogImage != null) {
            String content = ogImage.attr("content");
            if (content != null && !content.trim().isEmpty()) {
                return content.trim();
            }
        }

        // 2. itemprop=image meta (used by some Chinese sites) but validate it
        Element itempropImage = element.selectFirst("meta[itemprop=image]");
        if (itempropImage != null) {
            String content = itempropImage.attr("content");
            if (content != null && !content.trim().isEmpty() && !isProbablyLogo(content)) {
                return content.trim();
            }
        }

        // 3. Try common poster/cover CSS selectors used by Chinese anime sites
        String src = tryPosterSelectors(element);
        if (src != null && !src.trim().isEmpty()) {
            return src.trim();
        }

        // 4. Iterate all img tags, prefer lazy-loaded poster images,
        //    skip obvious logos/favicons/placeholders
        Elements allImgs = element.select("img");
        String fallbackSrc = null;
        for (Element img : allImgs) {
            String imgSrc = imageSrc(img);
            if (imgSrc == null || imgSrc.trim().isEmpty()) {
                continue;
            }
            if (isProbablyLogo(imgSrc)) {
                continue;
            }
            // Prefer images with data-original or data-src (typical for posters)
            if (img.hasAttr("data-original") || img.hasAttr("data-src")) {
                return imgSrc.trim();
            }
            if (fallbackSrc == null) {
                fallbackSrc = imgSrc.trim();
            }
        }

        // 4.5 Check <a> tags with poster attributes (e.g. stui templates,
        //     gpjda where poster is CSS background on <a> via data-original)
        Elements anchorTags = element.select("a[data-original], a[data-src]");
        for (Element anchor : anchorTags) {
            String anchorSrc = imageSrc(anchor);
            if (anchorSrc != null && !anchorSrc.trim().isEmpty() && !isProbablyLogo(anchorSrc)) {
                return anchorSrc.trim();
            }
        }

        // 5. Fallback to best non-logo img found
        if (fallbackSrc != null && !fallbackSrc.trim().isEmpty()) {
            return fallbackSrc;
        }

        // 6. Last resort: first img (original behavior)
        Element image = element.selectFirst("img");
        return imageSrc(image);
    }

    /**
     * Try to find a poster/cover image using common CSS selectors
     * used by Chinese anime streaming sites.
     */
    private static String tryPosterSelectors(Element element) {
        String[] posterSelectors = {
            // === <img> tag selectors ===
            ".author-img",                    // 7sefun
            ".module-item-pic img",           // 9ciyuan, anime7, mxdm, ylsp etc.
            ".module-item-pic img.lazy",      // lazy variant
            ".module-item-pic img.lazyload",  // lazy variant
            ".detail-pic img",                // generic detail pic
            ".vod_img img",                   // legacy vod template
            ".thumb img",                     // baimao/bmmdm
            ".video-pic img",                 // generic
            ".videoimg",                      // 7sefun search items
            "img.lazy",                       // lazy-load wrappers
            "img.lazyload",                   // lazy-load wrappers

            // === <a> tag selectors (posters as CSS background via data-original) ===
            ".stui-vodlist__thumb",           // gpjda, other stui template sites
            ".stui-vodlist__thumb.lazyload",  // gpjda lazy variant
            ".module-card-item-poster",       // ylsp, other module-card sites
            ".module-item-cover img",         // ylsp cover child img
            "a.cover",                        // mxdm, dcc3 variant
            "a.cover img.lazy",              // mxdm variant
        };
        for (String selector : posterSelectors) {
            try {
                Element poster = element.selectFirst(selector);
                if (poster != null) {
                    String src = imageSrc(poster);
                    if (src != null && !src.trim().isEmpty() && !isProbablyLogo(src)) {
                        return src.trim();
                    }
                }
            } catch (Exception ignored) {
                // Some selectors may throw in edge cases, continue
            }
        }
        return null;
    }

    /**
     * Check if a URL is likely a site logo, favicon, or placeholder.
     */
    private static boolean isProbablyLogo(String url) {
        if (url == null) return true;
        String lower = url.toLowerCase();
        if (lower.contains("/favicon")) return true;
        if (lower.endsWith(".ico") && (lower.contains("logo") || lower.contains("favicon"))) return true;
        if (lower.contains("/pc/img/")) return true;
        if (lower.contains("/static/images/")) return true;
        if (lower.contains("/mxtheme/images/")) return true;
        if (lower.contains("touxiang") || lower.contains("user.png") || lower.contains("user.jpg")) return true;
        return false;
    }

    public static String title(Object root) {
        Element element = toElement(root);
        if (element == null) {
            return "";
        }
        Element metaTitle = element.selectFirst("meta[property=og:title], meta[name=og:title], meta[name=twitter:title]");
        if (metaTitle != null) {
            String content = metaTitle.attr("content");
            if (content != null && !content.trim().isEmpty()) {
                return content.trim();
            }
        }
        Element title = element.selectFirst("h1, h2, title");
        if (title == null) {
            return "";
        }
        return title.text().trim();
    }

    private static String imageSrc(Element image) {
        if (image == null) {
            return "";
        }
        String[] attrs = new String[]{
                "data-original",
                "data-src",
                "data-lazy-src",
                "data-url",
                "data-img",
                "src"
        };
        for (String attr : attrs) {
            String value = image.attr(attr);
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    public static String value(Object root, String xpath) {
        ArrayList<JXNode> nodes = nodes(root, xpath);
        if (nodes.isEmpty()) {
            return "";
        }
        return nodes.get(0).toString();
    }

    public static String valueSelf(Object node) {
        JXNode jxNode = toJXNode(node);
        return jxNode == null ? "" : jxNode.toString();
    }

    private static List<JXNode> select(Object root, String xpath) {
        if (root instanceof JXNode) {
            List<JXNode> nodes = ((JXNode) root).sel(xpath);
            return nodes == null ? new ArrayList<>() : nodes;
        }

        if (root instanceof Document) {
            return JXDocument.create((Document) root).selN(xpath);
        }

        if (root instanceof Element) {
            return JXDocument.create(new Elements((Element) root)).selN(xpath);
        }

        return new ArrayList<>();
    }

    private static JXNode toJXNode(Object node) {
        if (node instanceof JXNode) {
            return (JXNode) node;
        }
        if (node instanceof Element) {
            return JXNode.create(node);
        }
        if (node == null) {
            return null;
        }
        return JXNode.create(node.toString());
    }

    private static Element toElement(Object node) {
        if (node instanceof Document) {
            return (Document) node;
        }
        if (node instanceof Element) {
            return (Element) node;
        }
        JXNode jxNode = toJXNode(node);
        if (jxNode != null && jxNode.isElement()) {
            return jxNode.asElement();
        }
        return null;
    }
}
