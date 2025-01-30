package utility;

import document.HtmlNode;

import java.util.ArrayList;

public class TreeUtil<T extends HtmlNode> {
    public ArrayList<T> toList(T node, ArrayList<T> list) {
        list.add(node);

        for (HtmlNode child : node.getChildren()) {
            toList((T) child, list);
        }

        return list;
    }
}
