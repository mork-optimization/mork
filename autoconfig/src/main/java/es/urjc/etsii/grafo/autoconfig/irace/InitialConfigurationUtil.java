package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationChoice;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationNode;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationTree;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** Converts JSON component descriptions into irace's initial-configuration table. */
public final class InitialConfigurationUtil {
    static final int GENERATED_REAL_DIGITS = 2;

    private InitialConfigurationUtil() {
    }

    public static String toIraceTable(AutoconfigSearchSpace searchSpace, List<ComponentSpec> seeds) {
        if (seeds.isEmpty()) {
            throw new IllegalArgumentException("Initial configurations must contain at least one algorithm");
        }

        var names = new ArrayList<String>();
        for (String parameter : searchSpace.iraceParameters()) {
            names.add(parameter.split("\\s+", 2)[0]);
        }
        var knownNames = new HashSet<>(names);
        var table = new StringBuilder(String.join("\t", names)).append('\n');
        var rows = new HashSet<String>();
        for (int i = 0; i < seeds.size(); i++) {
            var values = new HashMap<String, String>();
            var seed = seeds.get(i);
            try {
                TreeNode root = findRoot(searchSpace.roots(), seed.component());
                values.put("ROOT", root.className());
                flatten(root, seed, "ROOT_" + root.className(), searchSpace.componentParameters(), values);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Initial configuration " + (i + 1) + ": " + e.getMessage(), e);
            }

            for (String name : values.keySet()) {
                if (!knownNames.contains(name)) {
                    throw new IllegalStateException("Generated search space does not contain parameter " + name);
                }
            }
            var cells = new ArrayList<String>(names.size());
            for (String name : names) {
                cells.add(quoteCell(values.getOrDefault(name, "NA")));
            }
            String row = String.join("\t", cells);
            if (!rows.add(row)) {
                throw new IllegalArgumentException("Duplicate initial configuration " + (i + 1));
            }
            table.append(row).append('\n');
        }
        return table.toString();
    }

    private static TreeNode findRoot(List<TreeNode> roots, String name) {
        for (TreeNode root : roots) {
            if (root.className().equals(name)) {
                return root;
            }
        }
        throw new IllegalArgumentException("Initial algorithm " + name + " is not in the autoconfig search space");
    }

    private static void flatten(
            TreeNode node,
            ComponentSpec spec,
            String path,
            Map<Class<?>, List<ComponentParameter>> componentParameters,
            Map<String, String> values
    ) {
        if (!node.className().equals(spec.component())) {
            throw new IllegalArgumentException("Expected component " + node.className() + " at " + path
                    + ", got " + spec.component());
        }
        List<ComponentParameter> parameters = componentParameters.get(node.clazz());
        if (parameters == null) {
            throw new IllegalStateException("Missing parameter metadata for " + node.className());
        }
        var accepted = new HashSet<String>();
        for (ComponentParameter parameter : parameters) {
            if (parameter.getType() == ParameterType.PROVIDED) {
                continue;
            }
            String name = parameter.getName();
            accepted.add(name);
            String parameterPath = path + "." + name;
            if (!spec.parameters().containsKey(name)) {
                throw new IllegalArgumentException("Missing initial configuration parameter " + parameterPath);
            }
            Object value = spec.parameters().get(name);
            if (parameter.combination()) {
                flattenCombination(node.combinations().get(name), value, parameterPath, componentParameters, values);
            } else if (parameter.recursive()) {
                ComponentSpec childSpec = requireComponent(value, parameterPath);
                TreeNode child = findChild(node.children().get(name), childSpec.component(), parameterPath);
                values.put(parameterPath, child.className());
                flatten(child, childSpec, parameterPath + "_" + child.className(), componentParameters, values);
            } else {
                values.put(parameterPath, scalarValue(parameter, value, parameterPath));
            }
        }
        for (String name : spec.parameters().keySet()) {
            if (!accepted.contains(name)) {
                throw new IllegalArgumentException("Parameter " + path + "." + name
                        + " is not tunable in the autoconfig search space");
            }
        }
    }

    private static void flattenCombination(
            CombinationTree combination,
            Object value,
            String path,
            Map<Class<?>, List<ComponentParameter>> componentParameters,
            Map<String, String> values
    ) {
        if (combination == null || !(value instanceof List<?> items)) {
            throw new IllegalArgumentException("Expected a component array at " + path);
        }
        if (items.size() < combination.min() || items.size() > combination.max()) {
            throw new IllegalArgumentException("Invalid number of components at " + path + ": " + items.size()
                    + ", expected " + combination.min() + " to " + combination.max());
        }
        if (combination.min() != combination.max()) {
            values.put(path + ".length", Integer.toString(items.size()));
        }
        CombinationNode current = combination.root();
        String selectorPath = path + ".item0";
        for (Object item : items) {
            ComponentSpec childSpec = requireComponent(item, selectorPath);
            CombinationChoice selected = null;
            if (current != null) {
                for (CombinationChoice choice : current.choices()) {
                    if (choice.component().className().equals(childSpec.component())) {
                        selected = choice;
                        break;
                    }
                }
            }
            if (selected == null) {
                throw new IllegalArgumentException("Component " + childSpec.component()
                        + " is not allowed at " + selectorPath);
            }
            String childPath = selectorPath + "_" + childSpec.component();
            values.put(selectorPath, childSpec.component());
            flatten(selected.component(), childSpec, childPath + ".component", componentParameters, values);
            current = selected.next();
            if (current != null) {
                selectorPath = childPath + ".item" + current.position();
            }
        }
    }

    private static TreeNode findChild(List<TreeNode> children, String name, String path) {
        if (children != null) {
            for (TreeNode child : children) {
                if (child.className().equals(name)) {
                    return child;
                }
            }
        }
        throw new IllegalArgumentException("Component " + name + " is not allowed at " + path);
    }

    private static ComponentSpec requireComponent(Object value, String path) {
        if (value instanceof ComponentSpec spec) {
            return spec;
        }
        throw new IllegalArgumentException("Expected a component at " + path);
    }

    private static String scalarValue(ComponentParameter parameter, Object value, String path) {
        if (value == null) {
            throw new IllegalArgumentException("Null value for initial configuration parameter " + path);
        }
        return switch (parameter.getType()) {
            case INTEGER -> integerValue(parameter, value, path);
            case REAL -> realValue(parameter, value, path);
            case CATEGORICAL, ORDINAL -> categoricalValue(parameter, value, path);
            default -> throw new IllegalStateException("Unexpected scalar parameter type " + parameter.getType());
        };
    }

    private static String integerValue(ComponentParameter parameter, Object value, String path) {
        try {
            long number = Long.parseLong(value.toString());
            Object[] domain = parameter.getValues();
            if (number < ((Number) domain[0]).longValue() || number > ((Number) domain[1]).longValue()) {
                throw new IllegalArgumentException("Value " + number + " is outside the domain of " + path);
            }
            return Long.toString(number);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected an integer at " + path + ": " + value, e);
        }
    }

    private static String realValue(ComponentParameter parameter, Object value, String path) {
        try {
            double number = Double.parseDouble(value.toString());
            Object[] domain = parameter.getValues();
            if (!Double.isFinite(number) || number < ((Number) domain[0]).doubleValue()
                    || number > ((Number) domain[1]).doubleValue()) {
                throw new IllegalArgumentException("Value " + value + " is outside the domain of " + path);
            }
            BigDecimal decimal = BigDecimal.valueOf(number);
            try {
                decimal = decimal.setScale(GENERATED_REAL_DIGITS, RoundingMode.UNNECESSARY);
            } catch (ArithmeticException e) {
                throw new IllegalArgumentException("Value " + value + " at " + path
                        + " has more than " + GENERATED_REAL_DIGITS + " decimal places used by irace", e);
            }
            return decimal.toPlainString();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Expected a real number at " + path + ": " + value, e);
        }
    }

    private static String categoricalValue(ComponentParameter parameter, Object value, String path) {
        String selected = value.toString();
        for (Object choice : parameter.getValues()) {
            if (choice.equals(selected)) {
                return IraceParameterValueUtil.encodeCategorical(selected);
            }
        }
        throw new IllegalArgumentException("Value " + selected + " is not in the domain of " + path);
    }

    private static String quoteCell(String value) {
        if (value.equals("NA")) {
            return value;
        }
        return "\"" + value.replace("\"", "\\\"") + "\"";
    }
}
