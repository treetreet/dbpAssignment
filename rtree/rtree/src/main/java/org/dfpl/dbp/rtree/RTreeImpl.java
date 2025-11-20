package org.dfpl.dbp.rtree;

import java.util.*;


public class RTreeImpl implements RTree {

        // 요건 4-way R-Tree로 구현한다.
        // Maven Project로 만든다.
        // 기존의 R-Tree를 활용하지 않는다.
        // 여러분의 프로젝트에는 최소한의 dependency가 포함되어 있어야 함.
        // 멤버 변수의 활용은 어느정도 자유로움
        // 단, R-Tree 구현이어야 하고, 요행을 바라지 않는다.



        private int size=0;
        private RTreeNode root; // 트리의 루트 노드
        private final int M = 4; // 최대 용량 (4-way)
        private final int m = 2; // 최소 용량 (M/2)

        public RTreeImpl() {
            this.root = new RTreeNode(true);
        }



        public class Entry {
            //리프 노드에 들어가는 구조체
            private Rectangle mbr;
            private Point data;

            public Entry(Rectangle mbr, Point data) {
                this.mbr = mbr;
                this.data = data;
            }

            public Rectangle getMbr() {return mbr;}
            public Point getData() {return data;}
        }
        private class RTreeNode {
            //내부 노드에 들어가는 구조체
            Rectangle mbr;
            RTreeNode parent;
            boolean isLeaf;

            List<Entry> entries;     // 리프 노드일 때만 사용
            List<RTreeNode> children; // 내부 노드일 때만 사용

            //생성자
            public RTreeNode(boolean isLeaf) {
                this.isLeaf = isLeaf;
                this.parent = null;
                this.mbr = null; // MBR은 내용물이 추가/변경될 때 갱신

                if (isLeaf) {
                    this.entries = new ArrayList<>(M);
                    this.children = null;
                } else {
                    this.entries = null;
                    this.children = new ArrayList<>(M);
                }
            }

            public RTreeNode(RTreeNode origin) {
                this.mbr = origin.mbr;
                this.parent = origin.parent;
                this.isLeaf = origin.isLeaf;
                this.entries = origin.entries;
                this.children = origin.children;
            }

            //entry 추가 (루트)
            void addEntry(Entry entry) {
                this.entries.add(entry);
            }

            //자식 노드 추가
            void addChild(RTreeNode child) {
                this.children.add(child);
                child.parent = this;
            }

            boolean isFull() {
                if (isLeaf) {//4-way 기준
                    return entries.size() >= M;
                } else {
                    return children.size() >= M;
                }
            }

            void updateMbr() {
                if (isLeaf) {//리프노드면
                    if (entries.isEmpty()) {
                        this.mbr = null;
                        return;
                    }
                    Rectangle newMbr = entries.get(0).getMbr();//기존 mbr 가져옴
                    for (int i = 1; i < entries.size(); i++) {//모든 원소에 대해
                        newMbr = calculateUnion(newMbr, entries.get(i).getMbr()); //mbr 재구성
                    }
                    this.mbr = newMbr;//갱신
                } else {//내부면
                    if (children.isEmpty()) {
                        this.mbr = null;
                        return;
                    }
                    Rectangle newMbr = children.get(0).mbr;
                    for (int i = 1; i < children.size(); i++) {
                        newMbr = calculateUnion(newMbr, children.get(i).mbr);//자녀노드의 mbr을 추출해서 재구성
                    }
                    this.mbr = newMbr;//갱신
                }
            }
        }
        private class pqPoint implements Comparable<pqPoint> {
            //pqPoint는 pq에 들어갈 데이터 구조체,
            //Comparable은 큐에 들어갈 때 비교할 기준을 만들어줌.
            final Object item; // RTreeNode 또는 Entry
            final double distance;

            pqPoint(Object item, double distance) {
                this.item = item;
                this.distance = distance;
            }

            @Override
            public int compareTo(pqPoint other) {
                // distance 기준으로 오름차순 정렬
                return Double.compare(this.distance, other.distance);
            }
        }
        //point와 retangle의 거리 계산
        private double minDistSq(Rectangle r, Point p) {
            if (r == null) return Double.MAX_VALUE;

            double minX = Math.min(r.getLeftTop().getX(), r.getRightBottom().getX());
            double maxX = Math.max(r.getLeftTop().getX(), r.getRightBottom().getX());
            double minY = Math.min(r.getLeftTop().getY(), r.getRightBottom().getY());
            double maxY = Math.max(r.getLeftTop().getY(), r.getRightBottom().getY());

            double dx = 0;
            if (p.getX() < minX) {
                dx = minX - p.getX();
            } else if (p.getX() > maxX) {
                dx = p.getX() - maxX;
            }

            double dy = 0;
            if (p.getY() < minY) {
                dy = minY - p.getY();
            } else if (p.getY() > maxY) {
                dy = p.getY() - maxY;
            }

            return Math.sqrt((dx * dx) + (dy * dy));
        }
        //두 Rectangle의 MBR 업데이트
        private Rectangle calculateUnion(Rectangle r1, Rectangle r2) {
            //예외 처리
            if (r1 == null) return r2;
            if (r2 == null) return r1;

            // r1의min/max
            double r1MinX = Math.min(r1.getLeftTop().getX(), r1.getRightBottom().getX());
            double r1MinY = Math.min(r1.getLeftTop().getY(), r1.getRightBottom().getY());
            double r1MaxX = Math.max(r1.getLeftTop().getX(), r1.getRightBottom().getX());
            double r1MaxY = Math.max(r1.getLeftTop().getY(), r1.getRightBottom().getY());

            // r2의 min/max
            double r2MinX = Math.min(r2.getLeftTop().getX(), r2.getRightBottom().getX());
            double r2MinY = Math.min(r2.getLeftTop().getY(), r2.getRightBottom().getY());
            double r2MaxX = Math.max(r2.getLeftTop().getX(), r2.getRightBottom().getX());
            double r2MaxY = Math.max(r2.getLeftTop().getY(), r2.getRightBottom().getY());

            // 전체 min/max로 새 MBR 생성
            double minX = Math.min(r1MinX, r2MinX);
            double minY = Math.min(r1MinY, r2MinY);
            double maxX = Math.max(r1MaxX, r2MaxX);
            double maxY = Math.max(r1MaxY, r2MaxY);

            Point newLeftTop = new Point(minX, minY);
            Point newRightBottom = new Point(maxX, maxY);

            return new Rectangle(newLeftTop, newRightBottom);
        }
        @Override
        public Iterator<Point> nearest(Point source, int maxCount) {
            //결과 리스트
            List<Point> results = new ArrayList<>();

            if (isEmpty() || maxCount <= 0) {
                return results.iterator();
            }

            //최소 힙 생성
            PriorityQueue<pqPoint> pq = new PriorityQueue<>();

            //루트 삽입
            double rootDist = minDistSq(this.root.mbr, source);
            pq.add(new pqPoint(this.root, rootDist));

            // maxCount까지 반복
            while (!pq.isEmpty() && results.size() < maxCount) {

                pqPoint current = pq.poll(); //루트 꺼내기
                Object item = current.item;
                double distance = current.distance;

                if (item instanceof Entry) {
                    // Point면 결과에 추가
                    Entry entry = (Entry) item;
                    results.add(entry.getData());

                } else {
                    // RTreeNode면 내부 조회 필요
                    RTreeNode node = (RTreeNode) item;

                    if (node.isLeaf) {
                        // 리프면 Point List 꺼내서 큐에 추가
                        for (Entry entry : node.entries) {
                            double entryDist = entry.getData().distance(source);
                            pq.add(new pqPoint(entry, entryDist));
                        }
                    } else {
                        // 내부 노드면 자식 노드를 꺼내서 큐에 추가
                        for (RTreeNode child : node.children) {//노드의 자식 노드를 다 꺼냄
                            double childDist = minDistSq(child.mbr, source);//rectangle - point 거리 계산
                            pq.add(new pqPoint(child, childDist));//개중에 가장 가까운 애를 큐에 추가함
                        }
                    }
                }
            }
            //반복 후 결과 리스트 반환
            return results.iterator();
        }
        // Rectangle의 면적 계산 (MBR 크기)
        private double area(Rectangle r) {
            if (r == null) return 0;

            return Math.abs(r.getRightBottom().getX() - r.getLeftTop().getX()) *
                    Math.abs(r.getRightBottom().getY() - r.getLeftTop().getY());
        }

        // Leaf 그룹의 MBR 통합 계산
        private Rectangle makeMBR(List<Entry> list) {
            Rectangle m = list.get(0).getMbr();
            for (int i = 1; i < list.size(); i++) {
                m = calculateUnion(m, list.get(i).getMbr());
            }
            return m;
        }

        // Internal 그룹의 MBR 통합 계산
        private Rectangle makeMBRNode(List<RTreeNode> list) {
            Rectangle m = list.get(0).mbr;
            for (int i = 1; i < list.size(); i++) {
                m = calculateUnion(m, list.get(i).mbr);
            }
            return m;
        }

        // Leaf 노드를 Linear split 방식으로 2개의 그룹으로 분리하는 함수
        private void linearSplitEntries(List<Entry> original, List<Entry> g1, List<Entry> g2)
        {
            double maxDist = -1;
            Entry s1 = null, s2 = null;

            // 1) 가장 멀리 떨어진 두 Entry를 seed로 선택
            for (int i = 0; i < original.size(); i++) {
                for (int j = i + 1; j < original.size(); j++) {

                    double d = original.get(i).getData().distance(original.get(j).getData());

                    if (d > maxDist) {
                        maxDist = d;
                        s1 = original.get(i);
                        s2 = original.get(j);
                    }
                }
            }

            // seed 두 개를 각각 그룹에 넣기
            g1.add(s1);
            g2.add(s2);
            original.remove(s1);
            original.remove(s2);

            // 2) 나머지 Entry들을 증가 면적이 최소인 그룹에 배정
            for (Entry e : original) {
                double inc1 = area(calculateUnion(makeMBR(g1), e.getMbr()));
                double inc2 = area(calculateUnion(makeMBR(g2), e.getMbr()));

                if (inc1 < inc2) g1.add(e);
                else g2.add(e);
            }
        }

        // Internal 노드를 Linear split 방식으로 2개의 그룹으로 분리하는 함수
        private void linearSplitNodes(List<RTreeNode> original, List<RTreeNode> g1, List<RTreeNode> g2)
        {
            double maxDist = -1;
            RTreeNode s1 = null, s2 = null;

            // 1) MBR이 서로 가장 멀리 떨어진 두 child를 seed로 선택
            for (int i = 0; i < original.size(); i++) {
                for (int j = i + 1; j < original.size(); j++) {

                    double d = minDistSq(original.get(i).mbr, original.get(j).mbr.getLeftTop());

                    if (d > maxDist) {
                        maxDist = d;
                        s1 = original.get(i);
                        s2 = original.get(j);
                    }
                }
            }

            g1.add(s1);
            g2.add(s2);
            original.remove(s1);
            original.remove(s2);

            // 2) 나머지 child들을 증가 면적이 최소인 그룹에 추가
            for (RTreeNode n : original) {
                double inc1 = area(calculateUnion(makeMBRNode(g1), n.mbr));
                double inc2 = area(calculateUnion(makeMBRNode(g2), n.mbr));

                if (inc1 < inc2) g1.add(n);
                else g2.add(n);
            }
        }

        // 삽입할 Entry를 넣기 가장 적합한 Leaf 노드를 선택하는 함수
        // 규칙: MBR이 증가하는 면적이 가장 작은 child 방향으로 내려감
        private RTreeNode chooseLeaf(RTreeNode node, Entry entry) {

            // Leaf면 여기서 삽입하면 되므로 바로 반환
            if (node.isLeaf) return node;

            // 내부 노드일 경우 → 어떤 child로 내려갈 지 결정
            double bestIncrease = Double.MAX_VALUE;
            RTreeNode bestChild = null;

            for (RTreeNode child : node.children) {

                // 현재 child의 기존 MBR 면적
                double before = area(child.mbr);

                // child MBR에 entry의 MBR을 합친 후의 면적
                double after = area(calculateUnion(child.mbr, entry.getMbr()));

                // MBR 증가량
                double inc = after - before;

                // 증가량이 최소인 child 선택
                if (inc < bestIncrease) {
                    bestIncrease = inc;
                    bestChild = child;
                }
            }

            // 재귀적으로 Leaf까지 찾아 내려감
            return chooseLeaf(bestChild, entry);
        }

        // 노드가 가득 찼을 때(4개 초과) Split을 수행하는 함수
        // Leaf / Internal 각각에 맞는 split을 적용
        private void splitNode(RTreeNode node) {

            // 새로 만들어질 형제 노드
            RTreeNode newNode = new RTreeNode(node.isLeaf);

            if (node.isLeaf) {
                // Leaf일 경우 엔트리들을 2개 그룹으로 분리
                List<Entry> g1 = new ArrayList<>();
                List<Entry> g2 = new ArrayList<>();
                linearSplitEntries(node.entries, g1, g2);

                node.entries = g1;
                newNode.entries = g2;
            }
            else {
                // Internal 노드일 경우 자식 노드를 2개 그룹으로 분리
                List<RTreeNode> g1 = new ArrayList<>();
                List<RTreeNode> g2 = new ArrayList<>();
                linearSplitNodes(node.children, g1, g2);

                node.children = g1;
                newNode.children = g2;

                // parent 포인터 재설정
                for (RTreeNode c : g1) c.parent = node;
                for (RTreeNode c : g2) c.parent = newNode;
            }

            // 두 노드 각각 MBR 다시 계산
            node.updateMbr();
            newNode.updateMbr();

            // 루트가 Split된 경우 → 새로운 루트 생성
            if (node.parent == null) {
                RTreeNode newRoot = new RTreeNode(false); // 내부 노드
                newRoot.addChild(node);
                newRoot.addChild(newNode);
                this.root = newRoot;
                return;
            }

            // 루트가 아닌 경우 → 부모에 newNode 추가
            RTreeNode parent = node.parent;
            parent.addChild(newNode);
            parent.updateMbr();

            // 부모도 Overflow면 재귀적으로 Split 수행
            if (parent.isFull()) {
                splitNode(parent);
            }
        }

        @Override
        public void add(Point point) {
            // 삽입할 Entry 생성
            Rectangle rect = new Rectangle(point, point);
            Entry entry = new Entry(rect, point);

            // 1. 삽입할 리프 노드 찾기
            RTreeNode leaf = chooseLeaf(root, entry);

            // 2. 리프에 추가
            leaf.entries.add(entry);
            leaf.updateMbr();

            // 3. Overflow → split
            if (leaf.isFull()) {
                splitNode(leaf);
            }

            // 4. 루트 MBR 갱신
            root.updateMbr();

            this.size++;
        }

        @Override
        public Iterator<Point> search(Rectangle rectangle) {
            return null;
        }

        public boolean contains(RTreeNode node, Point point) {
            double minX = Math.min(node.mbr.getLeftTop().getX(), node.mbr.getRightBottom().getX());
            double maxX = Math.max(node.mbr.getLeftTop().getX(), node.mbr.getRightBottom().getX());
            double minY = Math.min(node.mbr.getLeftTop().getY(), node.mbr.getRightBottom().getY());
            double maxY = Math.max(node.mbr.getLeftTop().getY(), node.mbr.getRightBottom().getY());

            return point.getX() >= minX && point.getX() <= maxX && point.getY() >= minY && point.getY() <= maxY;
        }

        public void underflowRTree(RTreeNode parent)
        {
            //underflow 중단 조건
            if(isEmpty()) {
                root = null;
                return;
            }
            else if(parent == root) {
                return;
            }

            RTreeNode grandparent = parent.parent;
            if(parent.isLeaf) {
                //replace nodes
                if (parent.entries.size() < m) {
                    //save node
                    Entry node = parent.entries.get(0);
                    Entry aloneNode = new Entry(node.mbr, node.data);

                    //delete node
                    parent.entries.remove(node);
                    grandparent.children.remove(parent);

                    //reinsert
                    add(aloneNode.data);

                    //check underflow
                    if(grandparent == root && grandparent.children.size() < m) {
                        //root has one child (자식이 root 자리를 계승)
                        root = root.children.get(0);
                        root.parent = null;
                    }
                    else if(grandparent.children.size() < m) {
                        underflowRTree(grandparent);
                    }
                }
                //not leaf
                else {
                    //replace nodes
                    if(parent.children.size() < m) {
                        //save node
                        RTreeNode node = parent.children.get(0);
                        RTreeNode aloneNode = new RTreeNode(node);

                        //delete node
                        parent.children.remove(node);
                        grandparent.children.remove(parent);

                        //add(aloneNode);

                        //check underflow
                        if(grandparent == root && grandparent.children.size() < m) {
                            //root has one child
                            root = root.children.get(0);
                            root.parent = null;
                        }
                        else if(grandparent.children.size() < m) {
                            underflowRTree(grandparent);
                        }
                    }
                }
            }
        }

        @Override
        public void delete(Point point) {
            Queue<RTreeNode> queue = new LinkedList<>();
            queue.add(root);

            //Queue를 이용한 BFS 탐색
            while (!queue.isEmpty()) {
                RTreeNode current = queue.poll();

                //리프 노드일 경우 : 동일한 point가 있는지 탐색
                if (current.isLeaf) {
                    Iterator<Entry> it = current.entries.iterator();
                    while (it.hasNext()) {
                        Entry entry = it.next();

                        //동일 point 탐색
                        if (!entry.getData().equals(point)) continue;
                        it.remove();

                        // underflow일 경우
                        if(current.entries.size() <= m - 1)
                        {
                            underflowRTree(current);
                        }
                        else
                        {
                            current.updateMbr();
                        }
                        this.size--;

                        return; // 삭제 완료
                    }
                }
                //리프 노드가 아닐 경우 : 해당 영역이 point를 포함하는지 탐색
                else{
                    if (!contains(current, point)) continue;

                    //포함한다면 Queue에 추가
                    for(RTreeNode children : current.children) {
                        queue.add(children);
                    }
                }
            }
        }

        @Override
        public boolean isEmpty() {
            if (size==0) return true;
            else return false;
        }
}
