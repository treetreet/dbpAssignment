package org.dfpl.dbp.rtree;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;


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




        @Override
        public void add(Point point) {

            //if added
            this.size++;
        }

        @Override
        public Iterator<Point> search(Rectangle rectangle) {
            return null;
        }

        @Override
        public void delete(Point point) {

            //if exist and deleted
            this.size--;
        }

        @Override
        public boolean isEmpty() {
            if (size==0) return true;
            else return false;
        }
}
