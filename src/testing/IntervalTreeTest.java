package testing;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import datastructures.Interval;
import datastructures.IntervalTree;

public class IntervalTreeTest {

    private IntervalTree<Impl> emptyTree;                 // an empty tree
    
    private IntervalTree<Impl> singletonTree;             // a tree with one node: [0, 10)
    private Impl singletonValue = new Impl(0, 10);
    
    private IntervalTree<Impl> p282Tree;                  // the tree in CLRS, 2nd ed, pg 282
    private IntervalTree<Impl>.Node n11;                  // see setup() for more information
    private IntervalTree<Impl>.Node n2;
    private IntervalTree<Impl>.Node n1;
    private IntervalTree<Impl>.Node n14;
    private IntervalTree<Impl>.Node n15;
    private IntervalTree<Impl>.Node n7;
    private IntervalTree<Impl>.Node n5;
    private IntervalTree<Impl>.Node n8;
    private IntervalTree<Impl>.Node n4;
    
    private Impl p282Root = new Impl(11);
    private Impl p282Min = new Impl(1);
    private Impl p282Max = new Impl(15);
    private Map<Impl, Impl> p282Successors;
    private Map<Impl, Impl> p282Predecessors;
    private int p282NumElems = 9;
    
    private IntervalTree<Impl> randomTree;              // a large random tree
    private Set<Impl> randomValues;
    private int numRandomElements = 1000;
    private int valueCeiling = 5000;
    
    private Method mIsBST;                              // private methods
    private Method mHasValidRedColoring;
    private Method mInsertFixup;
    private Method mIsBalanced;
    private Method mHasConsistentMaxEnds;
    
    private Field fNil;                                 // private fields
    private Field fRoot;
    private Field fLeft;
    private Field fRight;
    private Field fParent;
    private Field fIsBlack;
    private Field fMaxEnd;

    @Before
    public void setup() throws NoSuchMethodException, SecurityException,
    NoSuchFieldException, IllegalArgumentException, IllegalAccessException {

        ////////////////////////////////////////////////////////////////////
        // Make private methods and fields accessible for easier testing. //
        ////////////////////////////////////////////////////////////////////
        
        mIsBST = IntervalTree.class.getDeclaredMethod("isBST");
        mIsBST.setAccessible(true);
        
        mIsBalanced = IntervalTree.class.getDeclaredMethod("isBalanced");
        mIsBalanced.setAccessible(true);
        
        mHasValidRedColoring = IntervalTree.class.getDeclaredMethod("hasValidRedColoring");
        mHasValidRedColoring.setAccessible(true);
        
        mHasConsistentMaxEnds = IntervalTree.class.getDeclaredMethod("hasConsistentMaxEnds");
        mHasConsistentMaxEnds.setAccessible(true);
        
        mInsertFixup = IntervalTree.Node.class.getDeclaredMethod("insertFixup");
        mInsertFixup.setAccessible(true);

        fNil = IntervalTree.class.getDeclaredField("nil");
        fNil.setAccessible(true);        
        
        fRoot = IntervalTree.class.getDeclaredField("root");
        fRoot.setAccessible(true);

        fLeft = IntervalTree.Node.class.getDeclaredField("left");
        fLeft.setAccessible(true);

        fRight = IntervalTree.Node.class.getDeclaredField("right");
        fRight.setAccessible(true);

        fParent = IntervalTree.Node.class.getDeclaredField("parent");
        fParent.setAccessible(true);
        
        fIsBlack = IntervalTree.Node.class.getDeclaredField("isBlack");
        fIsBlack.setAccessible(true);
        
        fMaxEnd = IntervalTree.Node.class.getDeclaredField("maxEnd");
        fMaxEnd.setAccessible(true);
        
        ////////////////////
        // The easy trees //
        ////////////////////
        
        emptyTree = new IntervalTree<Impl>();
        
        singletonTree = new IntervalTree<Impl>(singletonValue);
        
        ////////////////////////////////////////////////////////////
        // Manually construct the tree in CLRS, 2nd ed., page 282 //
        ////////////////////////////////////////////////////////////
        
        /* Note: This tree has an invalid red-coloring. When the Node n4 calls
         * its insertFixup() method, the violation should be fixed. This tree
         * is constructed such that all three cases for insertFixup() occur.
         * 
         * Since this tree isn't constructed with the insert() method, this
         * tree is also used to test general methods like predecessor(), since
         * a faulty insert() method won't affect them.
         */
        p282Tree = new IntervalTree<Impl>();
        n11 = p282Tree.new Node(new Impl(11));
        n2  = p282Tree.new Node(new Impl(2));
        n1  = p282Tree.new Node(new Impl(1));
        n14 = p282Tree.new Node(new Impl(14));
        n15 = p282Tree.new Node(new Impl(15));
        n7  = p282Tree.new Node(new Impl(7));
        n5  = p282Tree.new Node(new Impl(5));
        n8  = p282Tree.new Node(new Impl(8));
        n4  = p282Tree.new Node(new Impl(4));

        fParent.set(n11, fNil.get(p282Tree));
        fParent.set(n2, n11);
        fParent.set(n1, n2);
        fParent.set(n7, n2);
        fParent.set(n5, n7);
        fParent.set(n8, n7);
        fParent.set(n4, n5);
        fParent.set(n14, n11);
        fParent.set(n15, n14);
        
        fLeft.set(n11, n2);
        fLeft.set(n2, n1);
        fLeft.set(n1, fNil.get(p282Tree));
        fLeft.set(n7, n5);
        fLeft.set(n5, n4);
        fLeft.set(n8, fNil.get(p282Tree));
        fLeft.set(n4, fNil.get(p282Tree));
        fLeft.set(n14, fNil.get(p282Tree));
        fLeft.set(n15, fNil.get(p282Tree));

        fRight.set(n11, n14);
        fRight.set(n2, n7);
        fRight.set(n1, fNil.get(p282Tree));
        fRight.set(n7, n8);
        fRight.set(n5, fNil.get(p282Tree));
        fRight.set(n8, fNil.get(p282Tree));
        fRight.set(n4, fNil.get(p282Tree));
        fRight.set(n14, n15);
        fRight.set(n15, fNil.get(p282Tree));
        
        fIsBlack.set(n11, true);
        fIsBlack.set(n2, false);
        fIsBlack.set(n14, true);
        fIsBlack.set(n1, true);
        fIsBlack.set(n7, true);
        fIsBlack.set(n15, false);
        fIsBlack.set(n5, false);
        fIsBlack.set(n8, false);
        fIsBlack.set(n4, false);
        
        fMaxEnd.set(n11, 16);
        fMaxEnd.set(n2, 9);
        fMaxEnd.set(n14, 16);
        fMaxEnd.set(n1, 2);
        fMaxEnd.set(n7, 9);
        fMaxEnd.set(n15, 16);
        fMaxEnd.set(n5, 6);
        fMaxEnd.set(n8, 9);
        fMaxEnd.set(n4, 5);
        
        fRoot.set(p282Tree, n11);
        
        p282Successors = new HashMap<Impl, Impl>();
        p282Successors.put(new Impl(1), new Impl(2));
        p282Successors.put(new Impl(2), new Impl(4));
        p282Successors.put(new Impl(4), new Impl(5));
        p282Successors.put(new Impl(5), new Impl(7));
        p282Successors.put(new Impl(7), new Impl(8));
        p282Successors.put(new Impl(8), new Impl(11));
        p282Successors.put(new Impl(11), new Impl(14));
        p282Successors.put(new Impl(14), new Impl(15));
        
        p282Predecessors = new HashMap<Impl, Impl>();
        p282Predecessors.put(new Impl(15), new Impl(14));
        p282Predecessors.put(new Impl(14), new Impl(11));
        p282Predecessors.put(new Impl(11), new Impl(8));
        p282Predecessors.put(new Impl(8), new Impl(7));
        p282Predecessors.put(new Impl(7), new Impl(5));
        p282Predecessors.put(new Impl(5), new Impl(4));
        p282Predecessors.put(new Impl(4), new Impl(2));
        p282Predecessors.put(new Impl(2), new Impl(1));

        ////////////////////////
        // Make a random tree //
        ////////////////////////
        
        randomTree = new IntervalTree<Impl>();
        randomValues = new HashSet<Impl>();
        Random rand = new Random();
        for (int i = 0; i < numRandomElements; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(valueCeiling);
                s = rand.nextInt(valueCeiling);
            }
            Impl interval = new Impl(r, s);
            randomValues.add(interval);
            randomTree.insert(interval);
        }
    }

    @Test
    public void testEmptyTreeIsEmpty() {
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testNotEmptyTreeIsNotEmpty() {
        assertThat(p282Tree.isEmpty(), is(false));
    }
    
    @Test
    public void testSizeEmptyTree() {
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testSizeSingletonTree() {
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeColor() throws IllegalArgumentException,
    IllegalAccessException {
        assertThat(fIsBlack.get(fRoot.get(singletonTree)), is(true));
    }
    
    @Test
    public void testSizep282Tree() {
        assertThat(p282Tree.size(), is(p282NumElems));
    }

    @Test
    public void testIsRootTrue() {
        assertThat(p282Tree.search(p282Root)
                           .orElseThrow(() -> new IllegalStateException("root not found"))
                           .isRoot(), is(true));
    }
    
    @Test
    public void testIsRootFalse() {
        assertThat(p282Tree.search(p282Min)
                           .orElseThrow(() -> new IllegalStateException("min not found"))
                           .isRoot(), is(false));
    }
    
    @Test
    public void testMaximum() {
        assertThat(p282Tree.maximum()
                           .orElseThrow(() -> new IllegalStateException("max not found"))
                           .getValue(), is(p282Max));
    }
    
    @Test
    public void testMinimum() {
        assertThat(p282Tree.minimum()
                           .orElseThrow(() -> new IllegalStateException("min not found"))
                           .getValue(), is(p282Min));
    }
    
    @Test
    public void testSuccessorOfMax() {
        assertThat(p282Tree.search(p282Max)
                           .orElseThrow(() -> new IllegalStateException("max not found"))
                           .successor()
                           .isPresent(), is(false));
    }
    
    @Test
    public void testSuccessors() {
        for (Entry<Impl, Impl> e : p282Successors.entrySet()) {
            assertThat(p282Tree.search(e.getKey())
                               .orElseThrow(() -> new IllegalStateException("value " + e.getKey() + " not found"))
                               .successor()
                               .orElseThrow(() -> new IllegalStateException("successor to " + e.getKey() + " not found"))
                               .getValue(), is(e.getValue()));
        }
    }
    
    @Test
    public void testPredecessorOfMin() {
        assertThat(p282Tree.search(p282Min)
                           .orElseThrow(() -> new IllegalStateException("min not found"))
                           .predecessor()
                           .isPresent(), is(false));
    }
    
    @Test
    public void testPredecessors() {
        for (Entry<Impl, Impl> e : p282Predecessors.entrySet()) {
            assertThat(p282Tree.search(e.getKey())
                               .orElseThrow(() -> new IllegalStateException("value " + e.getKey() + " not found"))
                               .predecessor()
                               .orElseThrow(() -> new IllegalStateException("predecessor to " + e.getKey() + " not found"))
                               .getValue(), is(e.getValue()));
        }
    }
    
    @Test
    public void testEmptyTreeIsBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoring() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasConsistentMaxEnds() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoring() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasConsistentMaxEnds() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBSTAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(singletonValue);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(singletonValue);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(singletonValue);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasConsistentMaxEndsAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(singletonValue);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testp282TreeIsBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(p282Tree), is(true));
    }
    
    @Test
    public void testp282TreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(p282Tree), is(true));
    }
    
    @Test
    public void testp282TreeHasInvalidRedColoring() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(p282Tree), is(false));
    }
    
    @Test
    public void testp282TreeHasConsistentMaxEnds() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(p282Tree), is(true));
    }
    

    @Test
    public void testRandomTreeIsBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(randomTree), is(true));
    }
    
    @Test
    public void testRandomTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(randomTree), is(true));
    }
    
    @Test
    public void testRandomTreeHasValidRedColoring() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(randomTree), is(true));
    }
    
    @Test
    public void testRandomTreeHasConsistentMaxEnds() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(randomTree), is(true));
    }
    
    @Test
    public void testSizeAfterInsertionIntoEmptyTree() {
        emptyTree.insert(singletonValue);
        assertThat(emptyTree.size(), is(1));
    }
    
    @Test
    public void testSizeAfterInsertionSameValue() {
        singletonTree.insert(singletonValue);
        assertThat(singletonTree.size(), is(1));
    }

    @Test
    public void testSizeAfterInsertionDifferentStartSameEnd() {
        singletonTree.insert(new Impl(singletonValue.start + 1, singletonValue.end));
        assertThat(singletonTree.size(), is(2));
    }
    
    @Test
    public void testSizeAfterInsertionSameStartDifferentEnd() {
        singletonTree.insert(new Impl(singletonValue.start, singletonValue.end + 1));
        assertThat(singletonTree.size(), is(2));
    }
    
    @Test
    public void testIsEmptyAfterSingletonDeletion() {
        singletonTree.insert(singletonValue);
        singletonTree.maximum().ifPresent(d -> d.delete());
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSizeAfterEveryPossibleDeletion() {
        Set<Impl> s = new HashSet<Impl>(randomValues); // Make copy; otherwise ConcurrentModException
        Iterator<Impl> iter = randomValues.iterator();
        while (iter.hasNext()) {
            Impl i = iter.next();
            randomTree.search(i).ifPresent(k -> k.delete());
            s.remove(i);
            assertThat(randomTree.size(), is(s.size()));
        }
    }
    
    @Test
    public void testRemainsBstAfterEveryPossibleDeletion()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Iterator<Impl> iter = randomValues.iterator();
        while (iter.hasNext()) {
            Impl i = iter.next();
            randomTree.search(i).ifPresent(k -> k.delete());
            assertThat(mIsBST.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testRemainsBalancedAfterEveryPossibleDeletion()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Iterator<Impl> iter = randomValues.iterator();
        while (iter.hasNext()) {
            Impl i = iter.next();
            randomTree.search(i).ifPresent(k -> k.delete());
            assertThat(mIsBalanced.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testRetainsValidRedColoringAfterEveryPossibleDeletion()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Iterator<Impl> iter = randomValues.iterator();
        while (iter.hasNext()) {
            Impl i = iter.next();
            randomTree.search(i).ifPresent(k -> k.delete());
            assertThat(mHasValidRedColoring.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testRetainsConsistentMaxEndsAfterEveryPossibleDeletion()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Iterator<Impl> iter = randomValues.iterator();
        while (iter.hasNext()) {
            Impl i = iter.next();
            randomTree.search(i).ifPresent(k -> k.delete());
            assertThat(mHasConsistentMaxEnds.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testSizeAfterRepeatedInsertions() {
        int size = 0;
        Random rand = new Random();
        for (int i = 0; i < numRandomElements; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(valueCeiling);
                s = rand.nextInt(valueCeiling);
            }

            Impl interval = new Impl(r, s);
            if (emptyTree.insert(interval)) {
                 size++;   
            }
            assertThat(emptyTree.size(), is(size));
        }
    }
    
    @Test
    public void testRemainsBstAfterRepeatedInsertions()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomElements; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(valueCeiling);
                s = rand.nextInt(valueCeiling);
            }

            Impl interval = new Impl(r, s);
            emptyTree.insert(interval);
            assertThat(mIsBST.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testRetainsValidRedColoringAfterRepeatedInsertions()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomElements; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(valueCeiling);
                s = rand.nextInt(valueCeiling);
            }

            Impl interval = new Impl(r, s);
            emptyTree.insert(interval);
            assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testRetainsConsistentMaxEndsAfterRepeatedInsertions()
    throws IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomElements; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(valueCeiling);
                s = rand.nextInt(valueCeiling);
            }

            Impl interval = new Impl(r, s);
            emptyTree.insert(interval);
            assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testInsertFixupColoring() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        IntervalTree<Impl>.Node n = p282Tree.search(new Impl(4))
            .orElseThrow(() -> new IllegalStateException("Can't find node [4, 5)"));
        mInsertFixup.invoke(n);
        assertThat(fIsBlack.get(n11), is(false));
        assertThat(fIsBlack.get(n7), is(true));
        assertThat(fIsBlack.get(n8), is(true));
        assertThat(fIsBlack.get(n14), is(true));
        assertThat(fIsBlack.get(n15), is(false));
        assertThat(fIsBlack.get(n2), is(false));
        assertThat(fIsBlack.get(n1), is(true));
        assertThat(fIsBlack.get(n5), is(true));
        assertThat(fIsBlack.get(n4), is(false));
    }
    
    @Test
    public void testInsertFixupParents() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        IntervalTree<Impl>.Node n = p282Tree.search(new Impl(4))
            .orElseThrow(() -> new IllegalStateException("Can't find node [4, 5)"));
        mInsertFixup.invoke(n);
        assertThat(fParent.get(n11), is(n7));
        assertThat(fParent.get(n7), is(fNil.get(p282Tree)));
        assertThat(fParent.get(n8), is(n11));
        assertThat(fParent.get(n14), is(n11));
        assertThat(fParent.get(n15), is(n14));
        assertThat(fParent.get(n2), is(n7));
        assertThat(fParent.get(n1), is(n2));
        assertThat(fParent.get(n5), is(n2));
        assertThat(fParent.get(n4), is(n5));
    }
    
    @Test
    public void testInsertFixupLeftChildren() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        IntervalTree<Impl>.Node n = p282Tree.search(new Impl(4))
            .orElseThrow(() -> new IllegalStateException("Can't find node [4, 5)"));
        mInsertFixup.invoke(n);
        assertThat(fLeft.get(n11), is(n8));
        assertThat(fLeft.get(n7), is(n2));
        assertThat(fLeft.get(n8), is(fNil.get(p282Tree)));
        assertThat(fLeft.get(n14), is(fNil.get(p282Tree)));
        assertThat(fLeft.get(n15), is(fNil.get(p282Tree)));
        assertThat(fLeft.get(n2), is(n1));
        assertThat(fLeft.get(n1), is(fNil.get(p282Tree)));
        assertThat(fLeft.get(n5), is(n4));
        assertThat(fLeft.get(n4), is(fNil.get(p282Tree)));
    }
    
    @Test
    public void testInsertFixupRightChildren() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        IntervalTree<Impl>.Node n = p282Tree.search(new Impl(4))
            .orElseThrow(() -> new IllegalStateException("Can't find node [4, 5)"));
        mInsertFixup.invoke(n);
        assertThat(fRight.get(n11), is(n14));
        assertThat(fRight.get(n7), is(n11));
        assertThat(fRight.get(n8), is(fNil.get(p282Tree)));
        assertThat(fRight.get(n14), is(n15));
        assertThat(fRight.get(n15), is(fNil.get(p282Tree)));
        assertThat(fRight.get(n2), is(n5));
        assertThat(fRight.get(n1), is(fNil.get(p282Tree)));
        assertThat(fRight.get(n5), is(fNil.get(p282Tree)));
        assertThat(fRight.get(n4), is(fNil.get(p282Tree)));
    }
    
    @Test
    public void testNodeComparison() {
        IntervalTree<Impl>.Node a = p282Tree.new Node(new Impl(0));
        IntervalTree<Impl>.Node b = p282Tree.new Node(new Impl(0));
        assertThat(a.compareTo(b), is(0));
        assertThat(b.compareTo(a), is(0));
        assertThat(a.compareTo(a), is(0));
    }
    
    /**
     * Simple implementation of Interval for testing
     */
    private static class Impl implements Interval {

        private final int start;
        private final int end;
        
        public Impl(int start, int end) {
            this.start = start;
            this.end = end;
        }
        
        public Impl(int start) {
            this.start = start;
            end = start + 1;
        }
        
        @Override
        public int start() {
            return start;
        }

        @Override
        public int end() {
            return end;
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public boolean isAdjacent(Interval other) {
            return start == other.end() || end == other.start();
        }
        
        @Override
        public String toString() {
            return "start: " + start + " end: " + end;
        }
        
        @Override
        public boolean equals(Object other) {
            // No need for null check. The instanceof operator returns false if (other == null).
            if (!(other instanceof Impl)) {
                return false;
            }

            return start == ((Impl) other).start && end == ((Impl) other).end;
        }
        
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + start;
            result = 31 * result + end;
            return result;
        }
    }
}