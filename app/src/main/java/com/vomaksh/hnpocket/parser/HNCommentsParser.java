package com.vomaksh.hnpocket.parser;

import android.graphics.Color;

import com.vomaksh.hnpocket.App;
import com.vomaksh.hnpocket.Settings;
import com.vomaksh.hnpocket.model.HNComment;
import com.vomaksh.hnpocket.model.HNPostComments;
import com.vomaksh.hnpocket.util.HNHelper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Set;

public class HNCommentsParser extends BaseHTMLParser<HNPostComments> {

    @Override
    public HNPostComments parseDocument(Element doc) throws Exception {
        if (doc == null)
            return new HNPostComments();

        ArrayList<HNComment> comments = new ArrayList<HNComment>();

        Elements tableRows = doc.select("table tr table tr:has(table)");

        String currentUser = Settings.getUserName(App.getInstance());

        String text = null;
        String author = null;
        int level = 0;
        String timeAgo = null;
        String url = null;
        Boolean isDownvoted = false;
        String upvoteUrl = null;
        String downvoteUrl = null;

        boolean endParsing = false;
        for (int row = 0; row < tableRows.size(); row++) {
            Element mainRowElement = tableRows.get(row).select("td:eq(2)").first();
            Element rowLevelElement = tableRows.get(row).select("td:eq(0)").first();
            if (mainRowElement == null)
                continue;

            Element mainCommentDiv = mainRowElement.select("div.commtext").first();
            if (mainCommentDiv == null)
                continue;

            // Parse the class attribute to get the comment color
            int commentColor = getCommentColor(mainCommentDiv.classNames());

            // In order to eliminate whitespace at the end of multi-line comments,
            // <p> tags are replaced with double <br/> tags.
            text = mainCommentDiv.html()
                    .replace("<span> </span>", "")
                    .replace("<p>", "<br/><br/>")
                    .replace("</p>", "");

            Element comHeadElement = mainRowElement.select("span.comhead").first();
            author = comHeadElement.select("a[href*=user]").text();
            timeAgo = comHeadElement.select("a[href*=item").text();//getFirstTextValueInElementChildren(comHeadElement);
//            if (timeAgoRaw.length() > 0)
//                timeAgo = timeAgoRaw.substring(0, timeAgoRaw.indexOf("|"));
            Element urlElement = comHeadElement.select("a[href*=item]").first();
            if (urlElement != null)
                url = urlElement.attr("href");

            String levelSpacerWidth = rowLevelElement.select("img").first().attr("width");
            if (levelSpacerWidth != null)
                level = Integer.parseInt(levelSpacerWidth) / 40;

            Elements voteElements = tableRows.get(row).select("td:eq(1) a");
            upvoteUrl = getVoteUrl(voteElements.first());

            // We want to test for size because unlike first() calling .get(1)
            // Will throw an error if there are not two elements
            if (voteElements.size() > 1)
                downvoteUrl = getVoteUrl(voteElements.get(1));

            comments.add(new HNComment(timeAgo, author, url, text, commentColor, level, isDownvoted, upvoteUrl, downvoteUrl));

            if (endParsing)
                break;
        }

        // Just using table:eq(0) would return an extra table, so we use
        // get(0) instead, which only returns only the one we want
        Element header = doc.select("body table:eq(0)  tbody > tr:eq(2) > td:eq(0) > table").get(0);
        String headerHtml = null;

        // Five table rows is what it takes for the title, post information
        // And other boilerplate stuff.  More than five means we have something
        // Special
        if (header.select("tr").size() > 5) {
            HeaderParser headerParser = new HeaderParser();
            headerHtml = headerParser.parseDocument(header);
        }


        return new HNPostComments(comments, headerHtml, currentUser);
    }

    /**
     * Parses out the url for voting from a given element
     * @param voteElement The element from which to parse out the voting url
     * @return The relative url to vote in the given direction for that comment
     */
    private String getVoteUrl(Element voteElement) {
        if (voteElement != null) {
            return voteElement.attr("href").contains("auth=") ?
                HNHelper.resolveRelativeHNURL(voteElement.attr("href")) : null;
        }

        return null;
    }

    private int getCommentColor(Set<String> classNames) {
        if (classNames.contains("c00")) {
            return Color.BLACK;
        } else if (classNames.contains("c5a")) {
            return Color.rgb(0x5A, 0x5A, 0x5A);
        } else if (classNames.contains("c73")) {
            return Color.rgb(0x73, 0x73, 0x73);
        } else if (classNames.contains("c82")) {
            return Color.rgb(0x82, 0x82, 0x82);
        } else if (classNames.contains("c88")) {
            return Color.rgb(0x88, 0x88, 0x88);
        } else if (classNames.contains("c9c")) {
            return Color.rgb(0x9C, 0x9C, 0x9C);
        } else if (classNames.contains("cae")) {
            return Color.rgb(0xAE, 0xAE, 0xAE);
        } else if (classNames.contains("cbe")) {
            return Color.rgb(0xBE, 0xBE, 0xBE);
        } else if (classNames.contains("cce")) {
            return Color.rgb(0xCE, 0xCE, 0xCE);
        } else if (classNames.contains("cdd")) {
            return Color.rgb(0xDD, 0xDD, 0xDD);
        } else {
            return Color.BLACK;
        }
    }
}
