package com.jvm.community.intervaltree;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IntervalSetTreeTest {

    private int intervalIdCap = 5;  // Intervals have IDs, [0..4]
    
    private IntervalSetTree<Impl> emptyTree;              // an empty tree
    
    private IntervalSetTree<Impl> singletonTree;          // a tree with one node:
    private Impl singletonValue = new Impl(0, 10);        // [0, 10)
    private Impl copyOfSingletonValue = new Impl(singletonValue);
    private Impl singletonValueDifferentId = new Impl(0, 10, 1);
    private Impl notSingletonValue = new Impl(0, 1);
    private Impl overlapsSingletonValue = new Impl(0, 3);
    private Impl adjacentSingletonValue = new Impl(singletonValue.end(), singletonValue.end() + 10);
    private Impl noOverlapSingletonValue = new Impl(20, 22);
    
    private IntervalSetTree<Impl> randomTree;
    private int randomUpperBound = 100;
    private int numRandomIntervals = 2000;
    private Set<Impl> randomIntervals;
    private Impl notRandomValue = new Impl(5000, 10000);
    private Impl overlapsRandomTree = new Impl(20, 40);
    
    private IntervalSetTree<Impl> gappedTree;  // A tree with a dead-zone in the
    private int gappedUpperBound = 3000;       // middle to test overlap methods
    private int gappedLowerBound = 4000;
    private int numGappedIntervals = 2500;     // in each section
    private Set<Impl> gappedIntervals;
   
    
    // Private debugging methods.
    private Method mIsBST;
    private Method mHasValidRedColoring;
    private Method mIsBalanced;
    private Method mHasConsistentMaxEnds;

    @Before
    public void setup() throws NoSuchMethodException, SecurityException, IllegalArgumentException {

        /////////////////////////////////////////////////////////
        // Make private methods accessible for easier testing. //
        /////////////////////////////////////////////////////////
        
        mIsBST = IntervalSetTree.class.getDeclaredMethod("isBST");
        mIsBST.setAccessible(true);
        
        mIsBalanced = IntervalSetTree.class.getDeclaredMethod("isBalanced");
        mIsBalanced.setAccessible(true);
        
        mHasValidRedColoring = IntervalSetTree.class.getDeclaredMethod("hasValidRedColoring");
        mHasValidRedColoring.setAccessible(true);
        
        mHasConsistentMaxEnds = IntervalSetTree.class.getDeclaredMethod("hasConsistentMaxEnds");
        mHasConsistentMaxEnds.setAccessible(true);
        
        emptyTree = new IntervalSetTree<Impl>();
        singletonTree = new IntervalSetTree<Impl>(singletonValue);
        
        randomTree = new IntervalSetTree<Impl>();
        randomIntervals = new HashSet<Impl>();
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            int n = rand.nextInt(intervalIdCap);
            
            randomIntervals.add(new Impl(r, s, n));
            randomTree.insert(new Impl(r, s, n));
        }
        
        gappedTree = new IntervalSetTree<Impl>();
        gappedIntervals = new HashSet<Impl>();

        for (int i = 0; i < numGappedIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(gappedUpperBound);
                s = rand.nextInt(gappedUpperBound);
            }
            int n = rand.nextInt(intervalIdCap);
            
            gappedIntervals.add(new Impl(r, s, n));
            gappedTree.insert(new Impl(r, s, n));
        }
        
        for (int i = 0; i < numGappedIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(gappedUpperBound) + gappedLowerBound;
                s = rand.nextInt(gappedUpperBound) + gappedLowerBound;
            }
            int n = rand.nextInt(intervalIdCap);
            
            gappedIntervals.add(new Impl(r, s, n));
            gappedTree.insert(new Impl(r, s, n));
        }
    }
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    //////////////////////
    // Empty tree tests //
    //////////////////////
    
    @Test
    public void testEmptyTreeIsEmpty() {
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeSize() {
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeContains() {
        assertThat(emptyTree.contains(new Impl(1, 5)), is(false));
    }
    
    @Test
    public void testEmptyTreeMinimum() {
        assertThat(emptyTree.minimum().hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeMaximum() {
        assertThat(emptyTree.maximum().hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeSuccessor() {
        assertThat(emptyTree.successors(new Impl(1, 2)).hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreePredecessor() {
        assertThat(emptyTree.predecessors(new Impl(1, 2)).hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeIteratorHasNext() {
        assertThat(emptyTree.iterator().hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeIteratorNext() {
        thrown.expect(NoSuchElementException.class);
        emptyTree.iterator().next();
    }
    
    @Test
    public void testEmptyTreeOverlaps() {
        assertThat(emptyTree.overlaps(new Impl(1, 10)), is(false));
    }
    
    @Test
    public void testEmptyTreeOverlappersHasNext() {
        assertThat(emptyTree.overlappers(new Impl(1, 3)).hasNext(), is(false));
    }
    
    @Test
    public void testEmptyTreeOverlappersNext() {
        thrown.expect(NoSuchElementException.class);
        emptyTree.overlappers(new Impl(1, 3)).next();
    }
    
    @Test
    public void testEmptyTreeNumOverlappers() {
        assertThat(emptyTree.numOverlappers(new Impl(1, 3)), is(0));
    }
    
    @Test
    public void testEmptyTreeIsValidBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoring() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeConsistentMaxEnds() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }

    @Test
    public void testEmptyTreeDelete() {
        assertThat(emptyTree.delete(new Impl(1, 2)), is(false));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDelete() {
        emptyTree.delete(new Impl(1, 2));
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDelete() {
        emptyTree.delete(new Impl(1, 2));
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeDeleteMin() {
        assertThat(emptyTree.deleteMin(), is(false));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDeleteMin() {
        emptyTree.deleteMin();
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDeleteMin() {
        emptyTree.deleteMin();
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeDeleteMax() {
        assertThat(emptyTree.deleteMax(), is(false));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDeleteMax() {
        emptyTree.deleteMax();
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDeleteMax() {
        emptyTree.deleteMax();
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeDeleteOverlappers() {
        emptyTree.deleteOverlappers(new Impl(1, 2));
    }
    
    @Test
    public void testEmptyTreeSizeAfterDeleteOverlappers() {
        emptyTree.deleteOverlappers(new Impl(1, 2));
        assertThat(emptyTree.size(), is(0));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterDeleteOverlappers() {
        emptyTree.deleteOverlappers(new Impl(1, 2));
        assertThat(emptyTree.isEmpty(), is(true));
    }
    
    @Test
    public void testEmptyTreeIsValidBSTAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalancedAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoringAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeConsistentMaxEndsAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.delete(new Impl(1, 3));
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeInsertion() {
        assertThat(emptyTree.insert(new Impl(1, 3)), is(true));
    }
    
    @Test
    public void testEmptyTreeSizeAfterInsertion() {
        emptyTree.insert(new Impl(1, 2));
        assertThat(emptyTree.size(), is(1));
    }
    
    @Test
    public void testEmptyTreeIsEmptyAfterInsertion() {
        emptyTree.insert(new Impl(1, 2));
        assertThat(emptyTree.isEmpty(), is(false));
    }
    
    @Test
    public void testEmptyTreeIsValidBSTAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mIsBST.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsBalancedAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mIsBalanced.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoringAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeHasConsistentMaxEndsAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        emptyTree.insert(new Impl(1, 3));
        assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
    }
    
    @Test
    public void testEmptyTreeIsValidBSTAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mIsBST.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testEmptyTreeIsBalancedAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mIsBalanced.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testEmptyTreeHasValidRedColoringAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mHasValidRedColoring.invoke(emptyTree), is(true));
        }
    }
    
    @Test
    public void testEmptyTreeHasConsistentMaxEndsAfterRepeatedInsertions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Random rand = new Random();
        for (int i = 0; i < numRandomIntervals; i++) {
            int r = 0;
            int s = 0;
            while (s <= r) {
                r = rand.nextInt(randomUpperBound);
                s = rand.nextInt(randomUpperBound);
            }
            
            emptyTree.insert(new Impl(r, s));
            assertThat(mHasConsistentMaxEnds.invoke(emptyTree), is(true));
        }
    }
    
    //////////////////////////
    // Singleton tree tests //
    //////////////////////////
    
    @Test
    public void testSingletonTreeIsEmpty() {
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeSize() {
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeContainsPositive() {
        assertThat(singletonTree.contains(copyOfSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeContainsDifferentBoundsNegative() {
        assertThat(singletonTree.contains(notSingletonValue), is(false));
    }
    
    @Test
    public void testSingletonTreeContainsSameBoundsNegative() {
        assertThat(singletonTree.contains(singletonValueDifferentId), is(false));
    }
    
    @Test
    public void testSingletonTreeMinimum() {
        assertThat(singletonTree.minimum().next(),
                is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeOnlyOneMinimum() {
        Iterator<Impl> i = singletonTree.minimum();
        i.next();
        assertThat(i.hasNext(), is(false));
    }
    
    @Test
    public void testSingletonTreeMaximum() {
        assertThat(singletonTree.maximum().next(),
                is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeOnlyOneMaximum() {
        Iterator<Impl> i = singletonTree.maximum();
        i.next();
        assertThat(i.hasNext(), is(false));
    }
    
    @Test
    public void testSingletonTreeSuccessor() {
        assertThat(singletonTree.successors(copyOfSingletonValue).hasNext(),
                is(false));
    }
    
    @Test
    public void testSingetonTreePredecessor() {
        assertThat(singletonTree.predecessors(copyOfSingletonValue).hasNext(),
                is(false));
    }
    
    @Test
    public void testSingletonTreeIteratorHasNext() {
        assertThat(singletonTree.iterator().hasNext(), is(true));
    }
    
    @Test
    public void testSingletonTreeIteratorNext() {
        assertThat(singletonTree.iterator().next(), is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeIteratorNextTwice() {
        thrown.expect(NoSuchElementException.class);
        Iterator<Impl> i = singletonTree.iterator();
        i.next();
        i.next();
    }
    
    @Test
    public void testSingletonTreeOverlapsPositive() {
        assertThat(singletonTree.overlaps(copyOfSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeOverlapsDifferentIdPositive() {
        assertThat(singletonTree.overlaps(singletonValueDifferentId), is (true));
    }
     
    @Test
    public void testSingletonTreeOverlapsNegative() {
        assertThat(singletonTree.overlaps(noOverlapSingletonValue), is(false));
    }
    
    @Test
    public void testSingletonTreeOverlapsAdjacent() {
        assertThat(singletonTree.overlaps(adjacentSingletonValue), is(false));
    }
    
    @Test
    public void testSingletonTreeOverlappersHasNext() {
        assertThat(singletonTree.overlappers(overlapsSingletonValue).hasNext(), is(true));
    }

    @Test
    public void testSingletonTreeOverlappersNext() {
        assertThat(singletonTree.overlappers(overlapsSingletonValue).next(), is(copyOfSingletonValue));
    }
    
    @Test
    public void testSingletonTreeOverlappersNextTwice() {
        thrown.expect(NoSuchElementException.class);
        Iterator<Impl> i = singletonTree.overlappers(overlapsSingletonValue);
        i.next();
        i.next();
    }
    
    @Test
    public void testSingletonTreeNumOverlappers() {
        assertThat(singletonTree.numOverlappers(overlapsSingletonValue), is(1));
    }
    
    @Test
    public void testSingletonTreeIsValidBST() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalanced() throws IllegalAccessException,
    IllegalArgumentException, InvocationTargetException {
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoring() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEnds() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }

    @Test
    public void testSingletonTreeDeletePositive() {
        assertThat(singletonTree.delete(copyOfSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteNegative() {
        assertThat(singletonTree.delete(new Impl(1, 5)), is(false));
    }
    
    @Test
    public void testSingletonTreeDeleteDifferentIdNegative() {
        assertThat(singletonTree.delete(singletonValueDifferentId), is(false));
    }
    
    @Test
    public void testSingletonTreeSizeAfterSuccessfulDeletion() {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeSizeAfterUnsuccessfulDeletion() {
        singletonTree.delete(noOverlapSingletonValue);
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeSizeAfterUnsuccessfulDeletionDifferentId() {
        singletonTree.delete(singletonValueDifferentId);
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterSuccessfulDeletion() {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterUnsuccessfulDeletion() {
        singletonTree.delete(noOverlapSingletonValue);
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterUnsuccessfulDeletionDifferentId() {
        singletonTree.delete(singletonValueDifferentId);
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeDeleteMin() {
        assertThat(singletonTree.deleteMin(), is(true));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteMin() {
        singletonTree.deleteMin();
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterDeleteMin() {
        singletonTree.deleteMin();
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteMax() {
        assertThat(singletonTree.deleteMax(), is(true));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteMax() {
        singletonTree.deleteMax();
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterDeleteMax() {
        singletonTree.deleteMax();
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteOverlappersPositive() {
        assertThat(singletonTree.deleteOverlappers(overlapsSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeDeleteOverlappersNegative() {
        assertThat(singletonTree.deleteOverlappers(noOverlapSingletonValue), is(false));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteOverlappersPositive() {
        singletonTree.deleteOverlappers(overlapsSingletonValue);
        assertThat(singletonTree.size(), is(0));
    }
    
    @Test
    public void testSingletonTreeSizeAfterDeleteOverlappersNegative() {
        singletonTree.deleteOverlappers(noOverlapSingletonValue);
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeIsEmptyAfterDeleteOverlappers() {
        singletonTree.deleteOverlappers(overlapsSingletonValue);
        assertThat(singletonTree.isEmpty(), is(true));
    }
    
    @Test
    public void testSingletonTreeIsNotEmptyAfterDeleteOverlappers() {
        singletonTree.deleteOverlappers(noOverlapSingletonValue);
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterFailedDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.delete(singletonValueDifferentId);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterFailedDeletion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.delete(singletonValueDifferentId);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterFailedDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.delete(singletonValueDifferentId);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.delete(copyOfSingletonValue);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterFailedDeletion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.delete(singletonValueDifferentId);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeInsertion() {
        assertThat(singletonTree.insert(noOverlapSingletonValue), is(true));
    }
    
    @Test
    public void testSingletonTreeRedundantInsertion() {
        assertThat(singletonTree.insert(copyOfSingletonValue), is(false));
    }
    
    @Test
    public void testSingletonTreeInsertionDifferentId() {
        assertThat(singletonTree.insert(singletonValueDifferentId), is(true));
    }
    
    @Test
    public void testSingletonTreeSizeAfterInsertion() {
        singletonTree.insert(noOverlapSingletonValue);
        assertThat(singletonTree.size(), is(2));
    }
    
    @Test
    public void testSingletonTreeSizeAfterRedundantInsertion() {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(singletonTree.size(), is(1));
    }
    
    @Test
    public void testSingletonTreeSizeAfterInsertionDifferentId() {
        singletonTree.insert(singletonValueDifferentId);
        assertThat(singletonTree.size(), is(2));
    }
    
    @Test
    public void testSingletonTreeIsNotEmptyAfterInsertion() {
        singletonTree.insert(noOverlapSingletonValue);
        assertThat(singletonTree.isEmpty(), is(false));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(noOverlapSingletonValue);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterInsertionDifferentId() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(singletonValueDifferentId);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsValidBSTAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mIsBST.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(noOverlapSingletonValue);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterInsertionDifferentId() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(singletonValueDifferentId);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeIsBalancedAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mIsBalanced.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(noOverlapSingletonValue);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterInsertionDifferentId() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(singletonValueDifferentId);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeHasValidRedColoringAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mHasValidRedColoring.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(noOverlapSingletonValue);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterInsertionDifferentId() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(singletonValueDifferentId);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    @Test
    public void testSingletonTreeConsistentMaxEndsAfterRedundantInsertion() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        singletonTree.insert(copyOfSingletonValue);
        assertThat(mHasConsistentMaxEnds.invoke(singletonTree), is(true));
    }
    
    ///////////////////////
    // Random tree tests //
    ///////////////////////
    
    @Test
    public void testRandomTreeIsNotEmpty() {
        assertThat(randomTree.isEmpty(), is(false));
    }
    
    @Test
    public void testRandomTreeSize() {
        assertThat(randomTree.size(), is(randomIntervals.size()));
    }
    
    @Test
    public void testRandomTreeContainsPositive() {
        randomTree.insert(new Impl(1000, 2000));
        assertThat(randomTree.contains(new Impl(1000, 2000)), is(true));
    }
    
    @Test
    public void testRandomTreeContainsNegative() {
        assertThat(randomTree.contains(notRandomValue), is(false));
    }
    
    @Test
    public void testRandomTreeContainsAllIntervals() {
        for (Impl i : randomIntervals) {
            assertThat(randomTree.contains(i), is(true));
        }
    }
    
    @Test
    public void testRandomTreeMinimum() {
        Set<Impl> treeMins = new HashSet<>();
        randomTree.minimum().forEachRemaining(treeMins::add);

        Impl firstTreeMin = treeMins.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("treeMins is empty"));
        
        Set<Impl> setMins = randomIntervals.stream()
                .filter((i) -> i.compareTo(firstTreeMin) == 0)
                .collect(Collectors.toCollection(HashSet::new));
        
        assertThat(setMins, is(treeMins));
    }
    
    @Test
    public void testRandomTreeMaximum() {
        Set<Impl> treeMaxes = new HashSet<>();
        randomTree.maximum().forEachRemaining(treeMaxes::add);

        Impl firstTreeMax = treeMaxes.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("treeMaxes is empty"));
        
        Set<Impl> setMaxes = randomIntervals.stream()
                .filter((i) -> i.compareTo(firstTreeMax) == 0)
                .collect(Collectors.toCollection(HashSet::new));
        
        assertThat(setMaxes, is(treeMaxes));
    }
    
    @Test
    public void testRandomTreePredecessorOfMinimum() {
        Impl minimum = randomTree.minimum().next();
        assertThat(randomTree.predecessors(minimum).hasNext(), is(false));
    }
    
    @Test
    public void testRandomTreePredecessorOfMaximum() {
        Set<Impl> treeMaxes = new HashSet<>();
        randomTree.maximum().forEachRemaining(treeMaxes::add);

        Impl firstTreeMax = treeMaxes.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("treeMaxes is empty"));
        
        Set<Impl> treePreds = new HashSet<>();
        randomTree.predecessors(firstTreeMax).forEachRemaining(treePreds::add);
        
        Impl firstTreePred = treePreds.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("treePreds is empty"));
        
        Set<Impl> setPreds = randomIntervals.stream()
                .filter((i) -> i.compareTo(firstTreePred) == 0)
                .collect(Collectors.toCollection(HashSet::new));
        
        assertThat(setPreds, is(treePreds));
    }
    
    @Test
    public void testRandomTreeSuccessorOfMaximum() {
        Impl maximum = randomTree.maximum().next();
        assertThat(randomTree.successors(maximum).hasNext(), is(false));
    }
    
    @Test
    public void testRandomTreeSuccessorOfMinimum() {
        Set<Impl> treeMins = new HashSet<>();
        randomTree.minimum().forEachRemaining(treeMins::add);

        Impl firstTreeMin = treeMins.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("treeMins is empty"));
        
        Set<Impl> treeSuccs = new HashSet<>();
        randomTree.successors(firstTreeMin).forEachRemaining(treeSuccs::add);
        
        Impl firstTreeSucc = treeSuccs.stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("treeSuccs is empty"));
        
        Set<Impl> setSuccs = randomIntervals.stream()
                .filter((i) -> i.compareTo(firstTreeSucc) == 0)
                .collect(Collectors.toCollection(HashSet::new));
        
        assertThat(setSuccs, is(treeSuccs));
    }
    
    @Test
    public void testRandomTreeIteratorNumberOfElements() {

        long count = StreamSupport.stream(randomTree.spliterator(), false)
                .count();
        
        assertThat((long) randomIntervals.size(), is(count));
    }

    @Test
    public void testRandomTreeIterable() {
        Set<Impl> s = new HashSet<>();
        randomTree.iterator().forEachRemaining(s::add);
        assertThat(s, is(randomIntervals));
    }
    
    @Test
    public void testRandomTreeOverlapsPositive() {        
        assertThat(randomTree.overlaps(overlapsRandomTree), is(true));
    }
    
    @Test
    public void testRandomTreeOverlapsNegative1() {
        Impl cmp = new Impl(randomUpperBound, randomUpperBound + 1000);
        assertThat(randomTree.overlaps(cmp), is(false));
    }
    
    @Test
    public void testRandomTreeOverlapsNegative2() {
        Impl cmp = new Impl(-1000, 0);
        assertThat(randomTree.overlaps(cmp), is(false));
    }
    
    @Test
    public void testRandomTreeMinOverlapperPositive() {

        Impl setMin = randomIntervals.stream()
                .filter(n -> n.overlaps(overlapsRandomTree))
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new IllegalStateException("Can't find any overlapper."));
        
        Set<Impl> setMins = randomIntervals.stream()
                .filter(n -> n.compareTo(setMin) == 0)
                .collect(Collectors.toCollection(HashSet::new));
        
        Set<Impl> treeMins = new HashSet<>();
        randomTree.minimumOverlappers(overlapsRandomTree)
                .forEachRemaining(treeMins::add);

        assertThat(treeMins, is(setMins));
    }
    
    @Test
    public void testRandomTreeMinOverlapperNegative() {
        Impl cmp = new Impl(-1000, 0);
        assertThat(randomTree.minimumOverlappers(cmp).hasNext(), is(false));
    }

    @Test
    public void testRandomTreeNumOverlappers() {
        Impl i = new Impl(1000, 2000);

        long count = StreamSupport.stream(randomTree.spliterator(), false)
                .filter(n -> n.overlaps(i))
                .count();
        
        assertThat((long) randomTree.numOverlappers(i), is(count));
    }
    
    @Test
    public void testRandomTreeSizeAfterDeleteOverlappers() {
        Impl i = new Impl(1000, 2000);
        
        long initSize = randomTree.size();
        long count = StreamSupport.stream(randomTree.spliterator(), false)
                .filter(n -> n.overlaps(i))
                .count();
        
        randomTree.deleteOverlappers(i);
        assertThat((long) randomTree.size(), is(initSize - count));
    }
    
    @Test
    public void testRandomTreeNoOverlappersAfterDeleteOverlappers() {

        assertThat(randomTree.overlaps(overlapsRandomTree), is(true));
        
        randomTree.deleteOverlappers(overlapsRandomTree);
        assertThat(randomTree.overlaps(overlapsRandomTree), is(false));

        for (Impl j : randomTree) {
            assertThat(j.overlaps(overlapsRandomTree), is(false));
        }
    }

    @Test
    public void testRandomTreeSizeAfterRepeatedDeletions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<Impl> randomIntervalList = new ArrayList<>(randomIntervals);
        Collections.shuffle(randomIntervalList);
          
        int count = randomIntervalList.size();        

        assertThat(randomTree.size(), is(count));
        
        for (Impl i : randomIntervalList) {
            if (randomTree.delete(i)) {
                count--;
            }
            assertThat(randomTree.size(), is(count));
        }
        assertThat(randomTree.isEmpty(), is(true));
    }
    
    @Test
    public void testRandomTreeIsValidBSTAfterRepeatedDeletions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<Impl> randomIntervalList = new ArrayList<>(randomIntervals);
        Collections.shuffle(randomIntervalList);
        
        for (Impl i : randomIntervalList) {
            randomTree.delete(i);
            assertThat(mIsBST.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testRandomTreeIsBalancedAfterRepeatedDeletions() throws
    IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<Impl> randomIntervalList = new ArrayList<>(randomIntervals);
        Collections.shuffle(randomIntervalList);
        
        for (Impl i : randomIntervalList) {
            randomTree.delete(i);
            assertThat(mIsBalanced.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testRandomTreeHasValidRedColoringAfterRepeatedDeletions() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        List<Impl> randomIntervalList = new ArrayList<>(randomIntervals);
        Collections.shuffle(randomIntervalList);
        
        for (Impl i : randomIntervalList) {
            randomTree.delete(i);
            assertThat(mHasValidRedColoring.invoke(randomTree), is(true));
        }
    }
    
    @Test
    public void testRandomTreeConsistentMaxEndsAfterRepeatedDeletions() throws
    IllegalAccessException, IllegalArgumentException,
    InvocationTargetException {
        List<Impl> randomIntervalList = new ArrayList<>(randomIntervals);
        Collections.shuffle(randomIntervalList);
        
        for (Impl i : randomIntervalList) {
            randomTree.delete(i);
            assertThat(mHasConsistentMaxEnds.invoke(randomTree), is(true));
        }
    }
    
    ///////////////////////
    // Gapped tree tests //
    ///////////////////////

    @Test
    public void testGappedTreeOverlapsPositive() {
        assertThat(gappedTree.overlaps(new Impl(0, gappedUpperBound)), is(true));
        assertThat(gappedTree.overlaps(
            new Impl(gappedLowerBound, gappedUpperBound + gappedLowerBound)), is(true));
        assertThat(gappedTree.overlaps(
            new Impl(0, gappedUpperBound + gappedLowerBound)), is(true));
    }
    
    @Test
    public void testGappedTreeOverlapsNegative() {
        assertThat(gappedTree.overlaps(new Impl(gappedUpperBound, gappedLowerBound)), is(false));
    }
    
    @Test
    public void testGappedTreeDeleteOverlappersPositive() {
        Impl firstInterval = new Impl(0, gappedUpperBound);
        Impl secondInterval = new Impl(gappedLowerBound, gappedUpperBound + gappedLowerBound);
        boolean first = gappedTree.deleteOverlappers(firstInterval);
        boolean second = gappedTree.deleteOverlappers(secondInterval);
        assertThat(first && second, is(true));
    }
    
    @Test
    public void testGappedTreeDeleteOverlappersNegative() {
        Impl interval = new Impl(gappedUpperBound, gappedLowerBound);
        assertThat(gappedTree.deleteOverlappers(interval), is(false));
    }
   
    /**
     * Simple implementation of Interval for testing
     */
    private static class Impl implements Interval {

        private final int start;
        private final int end;
        private final int id;
        
        public Impl(int start, int end) {
            this.start = start;
            this.end = end;
            this.id = 0;    // default ID is 0
        }

        public Impl(int start, int end, int id) {
            this.start = start;
            this.end = end;
            this.id = id;
        }
        
        public Impl(Impl i) {
            this.start = i.start();
            this.end = i.end();
            this.id = i.id();
        }
        
        @Override
        public int start() {
            return start;
        }

        @Override
        public int end() {
            return end;
        }
        
        public int id() {
            return id;
        }
        
        @Override
        public String toString() {
            return "start: " + start + " end: " + end + " id: " + id;
        }
        
        @Override
        public boolean equals(Object other) {
            // No need for null check. The instanceof operator returns false if (other == null).
            if (!(other instanceof Impl)) {
                return false;
            }

            return start == ((Impl) other).start &&
                   end == ((Impl) other).end &&
                   id == ((Impl) other).id;
        }
        
        @Override
        public int hashCode() {
            int result = 17;
            result = 31 * result + start;
            result = 31 * result + end;
            result = 31 * result + id;
            return result;
        }
    }
}