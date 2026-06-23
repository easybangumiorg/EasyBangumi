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
        Element ogImage = element.selectFirst("meta[property=og:image], meta[name=og:image], meta[name=twitter:image]");
        if (ogImage != null) {
            String content = ogImage.attr("content");
            if (content != null && !content.trim().isEmpty()) {
                return content.trim();
            }
        }
        Element image = element.selectFirst("img");
        return imageSrc(image);
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
