package org.dfpl.dbp.rtree;

import java.util.Iterator;

public class RTreeImpl implements RTree {

	// 요건 4-way R-Tree로 구현한다.
	// Maven Project로 만든다. 
	// 기존의 R-Tree를 활용하지 않는다. 
	// 여러분의 프로젝트에는 최소한의 dependency가 포함되어 있어야 함.
	// 멤버 변수의 활용은 어느정도 자유로움
	// 단, R-Tree 구현이어야 하고, 요행을 바라지 않는다. 

    private int size=0;

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
	public Iterator<Point> nearest(Point source, int maxCount) {
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
