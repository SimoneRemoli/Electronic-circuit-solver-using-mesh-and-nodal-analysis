package com.example.servlet;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class CircuitTopology {

    static final class Node {
        final String id;
        final String label;
        final double x;
        final double y;

        Node(String id, String label, double x, double y) {
            this.id = id;
            this.label = label;
            this.x = x;
            this.y = y;
        }
    }

    static final class Branch {
        final String id;
        final String from;
        final String to;
        final String label;
        final List<String> components;
        final Map<String, Integer> currentDirections;
        final Map<String, Integer> voltagePolarities;
        final Map<String, Integer> meshCurrents;
        final Map<String, Integer> meshCurrentSourceSigns;
        final Map<String, Integer> meshVoltageSourceSigns;

        Branch(
                String id,
                String from,
                String to,
                String label,
                List<String> components,
                Map<String, Integer> currentDirections,
                Map<String, Integer> voltagePolarities,
                Map<String, Integer> meshCurrents,
                Map<String, Integer> meshCurrentSourceSigns,
                Map<String, Integer> meshVoltageSourceSigns
        ) {
            this.id = id;
            this.from = from;
            this.to = to;
            this.label = label;
            this.components = components;
            this.currentDirections = currentDirections;
            this.voltagePolarities = voltagePolarities;
            this.meshCurrents = meshCurrents;
            this.meshCurrentSourceSigns = meshCurrentSourceSigns;
            this.meshVoltageSourceSigns = meshVoltageSourceSigns;
        }
    }

    static final class DirectedBranchUse {
        final Branch branch;
        final String from;
        final String to;
        final int orientationSign;

        DirectedBranchUse(Branch branch, String from, String to, int orientationSign) {
            this.branch = branch;
            this.from = from;
            this.to = to;
            this.orientationSign = orientationSign;
        }
    }

    static final class Face {
        final List<DirectedBranchUse> boundary;
        final Set<String> components;
        final List<Node> polygon;
        final double centroidX;
        final double centroidY;
        final double area;

        Face(List<DirectedBranchUse> boundary, Set<String> components, List<Node> polygon, double centroidX, double centroidY, double area) {
            this.boundary = boundary;
            this.components = components;
            this.polygon = polygon;
            this.centroidX = centroidX;
            this.centroidY = centroidY;
            this.area = area;
        }
    }

    static final class Model {
        final Map<String, Node> nodes;
        final List<Branch> branches;
        final List<Face> faces;

        Model(Map<String, Node> nodes, List<Branch> branches, List<Face> faces) {
            this.nodes = nodes;
            this.branches = branches;
            this.faces = faces;
        }
    }

    private static final class DirectedEdge {
        final Branch branch;
        final String from;
        final String to;
        final double angle;

        private DirectedEdge(Branch branch, Node fromNode, Node toNode, String from, String to) {
            this.branch = branch;
            this.from = from;
            this.to = to;
            this.angle = Math.atan2(toNode.y - fromNode.y, toNode.x - fromNode.x);
        }
    }

    private CircuitTopology() {
    }

    static Model parse(String nodesData, String branchesData) {
        Map<String, Node> nodes = parseNodes(nodesData);
        List<Branch> branches = parseBranches(branchesData);
        if (nodes.isEmpty() || branches.isEmpty()) {
            return new Model(nodes, branches, new ArrayList<>());
        }
        return new Model(nodes, branches, detectFaces(nodes, branches));
    }

    static Map<Integer, Face> matchFacesToMeshes(List<Face> faces, String[][] associations) {
        Map<Integer, Face> out = new LinkedHashMap<>();
        List<Face> available = new ArrayList<>(faces);
        for (int meshIndex = 0; meshIndex < associations.length; meshIndex++) {
            Set<String> meshComponents = new LinkedHashSet<>();
            for (String code : associations[meshIndex]) {
                if (code != null && !code.isBlank()) {
                    meshComponents.add(code);
                }
            }
            Face bestFace = null;
            int bestScore = -1;
            for (Face face : available) {
                int score = 0;
                for (String code : meshComponents) {
                    if (face.components.contains(code)) {
                        score++;
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestFace = face;
                }
            }
            if (bestFace != null && bestScore > 0) {
                out.put(meshIndex, bestFace);
                available.remove(bestFace);
            }
        }
        return out;
    }

    static Map<Integer, Face> matchFacesToMeshesByMarkers(List<Face> faces, Map<Integer, double[]> markers) {
        Map<Integer, Face> out = new LinkedHashMap<>();
        List<Face> available = new ArrayList<>(faces);
        for (Map.Entry<Integer, double[]> entry : markers.entrySet()) {
            double[] point = entry.getValue();
            Face chosen = null;
            for (Face face : available) {
                if (contains(face.polygon, point[0], point[1])) {
                    chosen = face;
                    break;
                }
            }
            if (chosen == null && !available.isEmpty()) {
                chosen = nearestFace(available, point[0], point[1]);
            }
            if (chosen != null) {
                out.put(entry.getKey(), chosen);
                available.remove(chosen);
            }
        }
        return out;
    }

    static Map<Integer, double[]> parseMeshMarkers(String raw) {
        Map<Integer, double[]> out = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        String[] items = raw.split("\\|");
        for (String item : items) {
            if (item.isBlank()) continue;
            String[] parts = item.split("~", -1);
            if (parts.length < 3) continue;
            int meshIndex = Integer.parseInt(parts[0]);
            out.put(meshIndex, new double[]{
                    Double.parseDouble(parts[1]),
                    Double.parseDouble(parts[2])
            });
        }
        return out;
    }

    static Map<String, Integer> branchOrientationByMesh(Map<Integer, Face> faceByMesh) {
        Map<String, Integer> out = new LinkedHashMap<>();
        for (Map.Entry<Integer, Face> entry : faceByMesh.entrySet()) {
            Integer meshIndex = entry.getKey();
            for (DirectedBranchUse use : entry.getValue().boundary) {
                out.put(meshIndex + "|" + use.branch.id, use.orientationSign);
            }
        }
        return out;
    }

    private static Map<String, Node> parseNodes(String raw) {
        Map<String, Node> nodes = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return nodes;
        }
        String[] items = raw.split("\\|");
        for (String item : items) {
            if (item.isBlank()) continue;
            String[] parts = item.split("~", -1);
            if (parts.length < 4) continue;
            String id = decode(parts[0]);
            String label = decode(parts[3]);
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            nodes.put(id, new Node(id, label, x, y));
        }
        return nodes;
    }

    private static List<Branch> parseBranches(String raw) {
        List<Branch> branches = new ArrayList<>();
        if (raw == null || raw.isBlank()) {
            return branches;
        }
        String[] items = raw.split("\\|");
        for (String item : items) {
            if (item.isBlank()) continue;
            String[] parts = item.split("~", -1);
            if (parts.length < 5) continue;
            String id = decode(parts[0]);
            String from = decode(parts[1]);
            String to = decode(parts[2]);
            String componentsRaw = decode(parts[3]);
            String label = decode(parts[4]);
            String currentDirectionsRaw = parts.length > 5 ? decode(parts[5]) : "";
            String voltagePolaritiesRaw = parts.length > 6 ? decode(parts[6]) : "";
            String meshCurrentsRaw = parts.length > 7 ? decode(parts[7]) : "";
            String meshCurrentSourceSignsRaw = parts.length > 8 ? decode(parts[8]) : "";
            String meshVoltageSourceSignsRaw = parts.length > 9 ? decode(parts[9]) : "";
            List<String> components = new ArrayList<>();
            if (!componentsRaw.isBlank()) {
                for (String component : componentsRaw.split(",")) {
                    String trimmed = component.trim();
                    if (!trimmed.isEmpty()) {
                        components.add(trimmed);
                    }
                }
            }
            branches.add(new Branch(
                    id,
                    from,
                    to,
                    label,
                    components,
                    parseSigns(currentDirectionsRaw),
                    parseSigns(voltagePolaritiesRaw),
                    parseSigns(meshCurrentsRaw),
                    parseSigns(meshCurrentSourceSignsRaw),
                    parseSigns(meshVoltageSourceSignsRaw)
            ));
        }
        return branches;
    }

    private static Map<String, Integer> parseSigns(String raw) {
        Map<String, Integer> out = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) {
            return out;
        }
        for (String item : raw.split(",")) {
            String trimmed = item.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] parts = trimmed.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            String code = parts[0].trim();
            String signValue = parts[1].trim();
            if (code.isEmpty()) {
                continue;
            }
            out.put(code, "-1".equals(signValue) ? -1 : 1);
        }
        return out;
    }

    private static List<Face> detectFaces(Map<String, Node> nodes, List<Branch> branches) {
        Map<String, List<DirectedEdge>> outgoing = new HashMap<>();
        Map<String, DirectedEdge> directedEdges = new HashMap<>();

        for (Branch branch : branches) {
            Node fromNode = nodes.get(branch.from);
            Node toNode = nodes.get(branch.to);
            if (fromNode == null || toNode == null) {
                continue;
            }
            DirectedEdge forward = new DirectedEdge(branch, fromNode, toNode, branch.from, branch.to);
            DirectedEdge backward = new DirectedEdge(branch, toNode, fromNode, branch.to, branch.from);
            outgoing.computeIfAbsent(branch.from, ignored -> new ArrayList<>()).add(forward);
            outgoing.computeIfAbsent(branch.to, ignored -> new ArrayList<>()).add(backward);
            directedEdges.put(key(branch.id, branch.from, branch.to), forward);
            directedEdges.put(key(branch.id, branch.to, branch.from), backward);
        }

        for (List<DirectedEdge> edges : outgoing.values()) {
            edges.sort(Comparator.comparingDouble(edge -> edge.angle));
        }

        Set<String> visited = new LinkedHashSet<>();
        List<Face> faces = new ArrayList<>();

        for (DirectedEdge edge : directedEdges.values()) {
            String startKey = key(edge.branch.id, edge.from, edge.to);
            if (visited.contains(startKey)) {
                continue;
            }

            List<DirectedBranchUse> boundary = new ArrayList<>();
            List<Node> polygon = new ArrayList<>();
            DirectedEdge current = edge;
            int guard = 0;
            while (current != null && guard++ < directedEdges.size() + 5) {
                String currentKey = key(current.branch.id, current.from, current.to);
                if (visited.contains(currentKey) && !boundary.isEmpty()) {
                    break;
                }
                visited.add(currentKey);
                polygon.add(nodes.get(current.from));
                int sign = current.from.equals(current.branch.from) && current.to.equals(current.branch.to) ? 1 : -1;
                boundary.add(new DirectedBranchUse(current.branch, current.from, current.to, sign));
                DirectedEdge next = nextEdge(current, outgoing, directedEdges);
                if (next == null) {
                    boundary.clear();
                    break;
                }
                current = next;
                if (current.branch.id.equals(edge.branch.id) && current.from.equals(edge.from) && current.to.equals(edge.to)) {
                    break;
                }
            }

            if (boundary.size() < 3) {
                continue;
            }

            double area = polygonArea(polygon);
            Set<String> components = new LinkedHashSet<>();
            double centroidX = 0.0;
            double centroidY = 0.0;
            for (Node node : polygon) {
                centroidX += node.x;
                centroidY += node.y;
            }
            centroidX /= polygon.size();
            centroidY /= polygon.size();
            for (DirectedBranchUse use : boundary) {
                components.addAll(use.branch.components);
            }
            faces.add(new Face(boundary, components, new ArrayList<>(polygon), centroidX, centroidY, area));
        }

        if (faces.isEmpty()) {
            return faces;
        }

        Face outer = faces.get(0);
        for (Face face : faces) {
            if (Math.abs(face.area) > Math.abs(outer.area)) {
                outer = face;
            }
        }
        List<Face> innerFaces = new ArrayList<>();
        for (Face face : faces) {
            if (face != outer) {
                innerFaces.add(face);
            }
        }
        return innerFaces;
    }

    private static DirectedEdge nextEdge(DirectedEdge current, Map<String, List<DirectedEdge>> outgoing, Map<String, DirectedEdge> directedEdges) {
        List<DirectedEdge> edges = outgoing.get(current.to);
        if (edges == null || edges.isEmpty()) {
            return null;
        }
        String reverseKey = key(current.branch.id, current.to, current.from);
        DirectedEdge reverse = directedEdges.get(reverseKey);
        int index = edges.indexOf(reverse);
        if (index < 0) {
            return null;
        }
        int nextIndex = (index - 1 + edges.size()) % edges.size();
        return edges.get(nextIndex);
    }

    private static double polygonArea(List<Node> polygon) {
        double sum = 0.0;
        for (int i = 0; i < polygon.size(); i++) {
            Node a = polygon.get(i);
            Node b = polygon.get((i + 1) % polygon.size());
            sum += a.x * b.y - b.x * a.y;
        }
        return sum / 2.0;
    }

    private static boolean contains(List<Node> polygon, double x, double y) {
        boolean inside = false;
        for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
            Node a = polygon.get(i);
            Node b = polygon.get(j);
            boolean intersect = ((a.y > y) != (b.y > y))
                    && (x < (b.x - a.x) * (y - a.y) / ((b.y - a.y) == 0 ? 1e-9 : (b.y - a.y)) + a.x);
            if (intersect) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static Face nearestFace(List<Face> faces, double x, double y) {
        Face best = faces.get(0);
        double bestDistance = distanceSquared(best.centroidX, best.centroidY, x, y);
        for (Face face : faces) {
            double distance = distanceSquared(face.centroidX, face.centroidY, x, y);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = face;
            }
        }
        return best;
    }

    private static double distanceSquared(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return dx * dx + dy * dy;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String key(String branchId, String from, String to) {
        return branchId + "|" + from + "|" + to;
    }
}
