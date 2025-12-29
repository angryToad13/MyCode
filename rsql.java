import cz.jirutka.rsql.parser.ast.*;
import java.util.*;

public class RsqlToMapVisitor implements RSQLVisitor<Void, Map<String, Object>> {

    @Override
    public Void visit(AndNode node, Map<String, Object> map) {
        node.getChildren().forEach(n -> n.accept(this, map));
        return null;
    }

    @Override
    public Void visit(OrNode node, Map<String, Object> map) {
        node.getChildren().forEach(n -> n.accept(this, map));
        return null;
    }

    @Override
    public Void visit(ComparisonNode node, Map<String, Object> map) {
        String key = node.getSelector();
        List<String> arguments = node.getArguments();

        if (node.getOperator().equals(RSQLOperators.IN)) {
            map.put(key, new ArrayList<>(arguments));
        } else {
            map.put(key, arguments.get(0));
        }
        return null;
    }
}


import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

public class RsqlParserUtil {

    public static Map<String, Object> parseToMap(String rsqlQuery) {
        Node rootNode = new RSQLParser().parse(rsqlQuery);
        Map<String, Object> result = new LinkedHashMap<>();
        rootNode.accept(new RsqlToMapVisitor(), result);
        return result;
    }
}
