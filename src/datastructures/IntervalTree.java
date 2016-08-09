package datastructures;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * A balanced binary-search tree keyed by Interval objects.
 * <p>
 * The underlying data-structure is a red-black tree largely implemented from
 * CLRS (Introduction to Algorithms, 2nd edition) with the interval-tree
 * extensions mentioned in section 14.3
 * @param <I> - the type of Interval this tree contains
 */
public class IntervalTree<T extends Interval> {

    private Node root;
    private Node nil;

    /**
     * Constructs an empty IntervalTree.
     */
    public IntervalTree() {
        nil = new Node();
        root = nil;
    }
    
    /**
     * Constructs an IntervalTree containing a single node corresponding to
     * the given interval.
     * @param t - the interval to add to the tree
     */
    public IntervalTree(T t) {
        nil = new Node();
        root = new Node(t);
        root.blacken();
    }

    ///////////////////////////////////
    // Tree -- General query methods //
    ///////////////////////////////////
    
    /**
     * Whether this IntervalTree is empty or not.
     */
    public boolean isEmpty() {
        return root.isNil();
    }
    
    /**
     * The number of intervals stored in this IntervalTree.
     */
    public int size() {
        return root.size();
    }
    
    private Optional<Node> search(T t) {
        return root.search(t);
    }
    
    public boolean contains(T t) {
        return search(t).isPresent();
    }
    
    public Optional<T> minimum() {
        return root.minimumNode().map(n -> n.interval());
    }
    
    public Optional<T> maximum() {
        return root.maximumNode().map(n -> n.interval());
    }
    
    public Optional<T> successor(T t) {
        return search(t).flatMap(n -> n.successor()).map(n -> n.interval);
    }
    
    public Optional<T> predecessor(T t) {
        return search(t).flatMap(n -> n.predecessor()).map(n -> n.interval);
    }

    /**
     * An Iterator which traverses the tree in ascending order.
     */
    public Iterator<T> values() {
        return new TreeIterator(root);
    }
    
    public Iterator<T> overlappers(T t) {
        return root.overlappers(t);
    }
    
    public boolean overlaps(T t) {
        return root.anyOverlappingNode(t).isPresent();
    }
    
    public int numOverlappers(T t) {
        return root.numOverlappingNodes(t);
    }
    
    public Optional<T> minimumOverlapper(T t) {
        return root.minimumOverlappingNode(t).map(n -> n.interval());
    }
    
    ///////////////////////////////
    // Tree -- Insertion methods //
    ///////////////////////////////

    /**
     * Inserts the given value into the IntervalTree.
     * <p>
     * This method constructs a new Node containing the given value and places
     * it into the tree. If the value already exists within the tree, the tree
     * remains unchanged.
     * @param t - the value to place into the tree
     * @return if the value did not already exist, i.e., true if the tree was
     * changed, false if it was not
     */
    public boolean insert(T t) {
        
        Node z = new Node(t);
        Node y = nil;
        Node x = root;

        while (!x.isNil()) {                         // Traverse the tree down to a leaf.
            y = x;
            x.maxEnd = Math.max(x.maxEnd, z.maxEnd); // Update maxEnd on the way down.
            int cmp = z.compareTo(x);
            if (cmp == 0) {
                return false;                        // Value already in tree. Do nothing.
            }
            x = cmp == -1 ? x.left : x.right;
        }

        z.parent = y;
       
        if (y.isNil()) {
            root = z;
            root.blacken();
        } else {                      // Set the parent of n.
            int cmp = z.compareTo(y);
            if (cmp == -1) {
                y.left = z;
            } else {
                assert(cmp == 1);
                y.right = z;
            }
            
            z.left = nil;
            z.right = nil;
            z.redden();
            z.insertFixup();
        }
        return true;
    }
    
    //////////////////////////////
    // Tree -- Deletion methods //
    //////////////////////////////
    
    /**
     * Deletes the given value from the IntervalTree.
     * <p>
     * If the value does not exist in the tree, this method does nothing.
     * @param t - the value to delete from the tree
     */
    public void delete(T t) {
        root.search(t).ifPresent(k -> k.delete());
    }
    
    /**
     * Deletes the minimum value from this IntervalTree.
     * <p>
     * If there is no minimum value (that is, if the tree is empty), this
     * method does nothing.
     */
    public void deleteMin() {
        root.minimumNode().ifPresent(k -> k.delete());
    }
    
    /**
     * Deletes the maximum value from this IntervalTree.
     * <p>
     * If there is no maximum value (that is, if the tree is empty), this
     * method does nothing.
     */
    public void deleteMax() {
        root.maximumNode().ifPresent(k -> k.delete());
    }
    
    public void deleteOverlappers(T t) {
        // TODO Currently, this method creates a Set of Nodes, then
        // deletes them all at once. Can we change this?

        Set<Node> s = new HashSet<Node>();
        Iterator<Node> iter = new OverlappingNodeIterator(root, t);
        iter.forEachRemaining(s::add);
        s.forEach(n -> n.delete());
    }

    /**
     * A representation of a node of an interval tree.
     */
    public class Node implements Interval {
        
        /* Most of the "guts" of the interval tree are actually methods called
         * by nodes. For example, IntervalTree#delete(val) searches up the Node
         * containing val; then that Node deletes itself with Node#delete().
         */

        private T interval;
        private Node parent;
        private Node left;
        private Node right;
        private boolean isBlack;
        private int maxEnd;

        /**
         * Constructs a Node with no data.
         * <p>
         * This node has a null data field, is black, and has all pointers
         * pointing at itself. This is intended to be used as the sentinel
         * node in the tree ("nil" in CLRS).
         */
        private Node() {
            parent = this;
            left = this;
            right = this;
            blacken();
        }
        
        /**
         * Constructs a Node containing the given data.
         * @param data - the data to be contained within the node
         */
        public Node(T data) {
            interval = data;
            parent = nil;
            left = nil;
            right = nil;
            maxEnd = data.end();
            redden();
        }
        
        public T interval() {
            return interval;
        }
        
        @Override
        public int start() {
            return interval.start();
        }

        @Override
        public int end() {
            return interval.end();
        }
        
        ///////////////////////////////////
        // Node -- General query methods //
        ///////////////////////////////////
        
        /**
         * The number of elements in the subtree rooted at this Node.
         * <p>
         * Leaf nodes are not included in the number. The Node calling this
         * method is included in the number.
         */
        public int size() {
            return isNil() ? 0 : 1 + left.size() + right.size();
        }
        
        /**
         * Searches the subtree rooted at this Node for the given value.
         * @param t - the value to search for
         * @return an Optional containing, if it exists, the Node with the
         * given value; otherwise an empty Optional 
         */
        private Optional<Node> search(T t) {

            Node n = this;
            
            while (!n.isNil() && t.compareTo(n) != 0) {
                n = t.compareTo(n) == -1 ? n.left : n.right;
            }
            return n.isNil() ? Optional.empty() : Optional.of(n);
        }

        /**
         * Searches the subtree rooted at this Node for its minimum value.
         * @return an Optional containing, if it exists, the Node with the
         * minimum value; otherwise (that is, if this subtree is empty) an
         * empty Optional 
         */
        private Optional<Node> minimumNode() {
            
            Node n = this;
            
            while (!n.left.isNil()) {
                n = n.left;
            }
            return n.isNil() ? Optional.empty() : Optional.of(n);
        }

        /**
         * Searches the subtree rooted at this Node for its maximum value.
         * @return an Optional containing, if it exists, the Node with the
         * maximum value; otherwise (that is, if this subtree is empty) an
         * empty Optional 
         */
        private Optional<Node> maximumNode() {
            
            Node n = this;
            
            while (!n.right.isNil()) {
                n = n.right;
            }
            return n.isNil() ? Optional.empty() : Optional.of(n);
        }
        
        /**
         * The successor of this Node.
         * @return an Optional containing, if it exists, the Node succeeding
         * this one; otherwise (that is, if this Node contains the maximal
         * value in the tree) an empty Optional 
         */
        private Optional<Node> successor() {
            
            if (!right.isNil()) {
                return right.minimumNode();
            }
            
            Node x = this;
            Node y = parent;
            while (!y.isNil() && x == y.right) {
                x = y;
                y = y.parent;
            }
            
            return y.isNil() ? Optional.empty() : Optional.of(y);
        }

        /**
         * The predecessor of this Node.
         * @return an Optional containing, if it exists, the Node preceding
         * this one; otherwise (that is, if this Node contains the minimal
         * value in the tree) an empty Optional 
         */
        private Optional<Node> predecessor() {
            
            if (!left.isNil()) {
                return left.maximumNode();
            }
            
            Node x = this;
            Node y = parent;
            while (!y.isNil() && x == y.left) {
                x = y;
                y = y.parent;
            }
            
            return y.isNil() ? Optional.empty() : Optional.of(y);
        }
        
        ///////////////////////////////////////
        // Node -- Overlapping query methods //
        ///////////////////////////////////////
        
        /**
         * Returns a Node from this Node's subtree that overlaps the given
         * Interval.
         * <p>
         * The only guarantee of this method is that the returned Node overlaps
         * the Interval t. This method is meant to be a quick helper method to
         * determine if any overlap exists between an Interval and a tree. The
         * returned Node will be the first overlapping one found.
         * @param t - the given Interval
         * @return an Optional containing, if one exists, a Node from this
         * Node's subtree that overlaps the Interval t; otherwise, an empty
         * Optional
         */
        private Optional<Node> anyOverlappingNode(T t) {
            Node x = this;
            while (!x.isNil() && !t.overlaps(x.interval)) {
                x = !x.left.isNil() && x.left.maxEnd > t.start() ? x.left : x.right;
            }
            return x.isNil() ? Optional.empty() : Optional.of(x);
        }
        
        /**
         * Returns the minimum Node from this Node's subtree that overlaps the
         * given Interval.
         * <p>
         * "Minimum" is determined by compareTo(), which should be implemented
         * as the Interval interface extends the Comparable interface.
         * @param t - the given Interval
         * @return an Optional containing, if it exists, the minimum Node from
         * this Node's subtree that overlaps the Interval t; otherwise, an
         * empty Optional
         */
        private Optional<Node> minimumOverlappingNode(T t) {
            Node x = this;
            Node rtrn = nil;
            while (!x.isNil()) {
                if (x.interval.overlaps(t)) {
                    if (rtrn.isNil() || rtrn.end() > x.end()) {
                        rtrn = x;
                    }
                }
                x = !x.left.isNil() && x.left.maxEnd > t.start() ? x.left : x.right;
            }
            return rtrn == nil ? Optional.empty() : Optional.of(rtrn);
        }
        
        /**
         * An Iterator over all values in this Node's subtree that overlap the
         * given Interval t.
         * @param t - the overlapping Interval
         */
        private Iterator<T> overlappers(T t) {
            return new OverlapperIterator(this, t);
        }
        
        /**
         * The next Node (relative to this Node) which overlaps the given
         * Interval t
         * @param t - the overlapping Interval
         * @return an Optional containing, if it exists, the next Interval that
         * overlaps the Interval t; otherwise, an empty Optional
         */
        private Optional<Node> nextOverlappingNode(T t) {
            Node x = this;
            Optional<Node> rtrn = Optional.empty();

            // First, check the right subtree for its minimum overlapper.
            if (!right.isNil()) {
                rtrn = x.right.minimumOverlappingNode(t);
            }
            
            // If we didn't find it in the right subtree, walk up the tree and
            // check the parents of left-children as well as their right subtrees.
            while (!x.parent.isNil() && !rtrn.isPresent()) {
                if (x.isLeftChild()) {
                    rtrn = x.parent.overlaps(t) ? Optional.of(x.parent)
                                                : x.parent.right.minimumOverlappingNode(t);
                }
                x = x.parent;
            }
            return rtrn;
        }
        
        /**
         * The number of Nodes in this Node's subtree which overlap the given
         * Interval t.
         * <p>
         * This number includes this Node if this Node overlaps t. This method
         * iterates over all overlapping Nodes, so if you ultimately need to
         * inspect the Nodes, it will be more efficient to simply create the
         * Iterator yourself.
         * @param t - the overlapping Interval
         * @return the number of overlapping Nodes
         */
        private int numOverlappingNodes(T t) {
            int count = 0;
            Iterator<Node> iter = new OverlappingNodeIterator(this, t);
            while (iter.hasNext()) {
                iter.next();
                count++;
            }
            return count;
        }
        
        //////////////////////////////
        // Node -- Deletion methods //
        //////////////////////////////
        
        /**
         * Deletes this Node from its tree.
         * <p>
         * More specifically, removes the data held within this Node from the
         * tree. Depending on the structure of the tree at this Node, this
         * particular Node instance may not be removed; rather, a different
         * Node may be deleted and that Node's contents copied into this one,
         * overwriting the previous contents.
         */
        private void delete() {
            
            if (isNil()) {  // Can't delete the sentinel node.
                return;
            }
            
            Node y = this;

            if (hasTwoChildren()) {     // If the node to remove has two children,
                y = successor().get();  // copy the successor's data into it and
                copyData(y);            // remove the successor. The successor is
                maxEndFixup();          // guaranteed to both exist and have at most
            }                           // one child, so we've converted the two-
                                        // child case to a one- or no-child case.
            
            
            Node x = y.left.isNil() ? y.right : y.left;

            x.parent = y.parent;

            if (y.isRoot()) {
                root = x;
            } else if (y.isLeftChild()) {
                y.parent.left = x;
                y.maxEndFixup();
            } else {
                y.parent.right = x;
                y.maxEndFixup();
            }
            
            if (y.isBlack) {
                x.deleteFixup();
            }
        }
        
        ////////////////////////////////////////////////
        // Node -- Tree-invariant maintenance methods //
        ////////////////////////////////////////////////

        /**
         * Whether or not this Node is the root of its tree.
         */
        public boolean isRoot() {
            return (!isNil() && parent.isNil());
        }
        
        /**
         * Whether or not this Node is the sentinel node.
         */
        public boolean isNil() {
            return this == nil;
        }

        /**
         * Whether or not this Node is the left child of its parent.
         */
        public boolean isLeftChild() {
            return this == parent.left;
        }

        /**
         * Whether or not this Node is the right child of its parent.
         */
        private boolean isRightChild() {
            return this == parent.right;
        }

        /**
         * Whether or not this Node has no children, i.e., is a leaf.
         */
        private boolean hasNoChildren() {
            return left.isNil() && right.isNil();
        }

        /**
         * Whether or not this Node has two children, i.e., neither of its
         * children are leaves.
         */
        private boolean hasTwoChildren() {
            return !left.isNil() && !right.isNil();
        }
        
        /**
         * Sets this Node's color to black.
         */
        private void blacken() {
            isBlack = true;
        }
        
        /**
         * Sets this Node's color to red.
         */
        private void redden() {
            isBlack = false;
        }
        
        /**
         * Whether or not this Node's color is red.
         */
        private boolean isRed() {
            return !isBlack;
        }
        
        /**
         * A pointer to the grandparent of this Node.
         */
        private Node grandparent() {
            return parent.parent;
        }

        /**
         * Sets the maxEnd value for this Node.
         * <p>
         * The maxEnd value should be the highest of:
         * <ul>
         * <li>the end value of this node's data
         * <li>the maxEnd value of this node's left child, if not null
         * <li>the maxEnd value of this node's right child, if not null
         * </ul><p>
         * This method will be correct only if the left and right children have
         * correct maxEnd values.
         */
        private void resetMaxEnd() {
            int val = interval.end();
            if (!left.isNil()) {
                val = Math.max(val, left.maxEnd);
            }
            if (!right.isNil()) {
                val = Math.max(val, right.maxEnd);
            }
            maxEnd = val;
        }
        
        /**
         * Sets the maxEnd value for this Node, and all Nodes up to the root of
         * the tree.
         */
        private void maxEndFixup() {
            Node n = this;
            n.resetMaxEnd();
            while (!n.parent.isNil()) {
                n = n.parent;
                n.resetMaxEnd();
            }
        }
        
        /**
         * Performs a left-rotation on this Node.
         * @see - Cormen et al. "Introduction to Algorithms", 2nd ed, pp. 277-279.
         */
        private void leftRotate() {
            Node y = right;
            right = y.left;

            if (!y.left.isNil()) {
                y.left.parent = this;
            }
            
            y.parent = parent;
            
            if (parent.isNil()) {
                root = y;
            } else if (isLeftChild()) {
                parent.left = y;
            } else {
                parent.right = y;
            }
            
            y.left = this;
            parent = y;
            
            resetMaxEnd();
            y.resetMaxEnd();
        }
        
        /**
         * Performs a right-rotation on this Node.
         * @see - Cormen et al. "Introduction to Algorithms", 2nd ed, pp. 277-279.
         */
        private void rightRotate() {
            Node y = left;
            left = y.right;

            if (!y.right.isNil()) {
                y.right.parent = this;
            }
            
            y.parent = parent;
            
            if (parent.isNil()) {
                root = y;
            } else if (isLeftChild()) {
                parent.left = y;
            } else {
                parent.right = y;
            }
            
            y.right = this;
            parent = y;
            
            resetMaxEnd();
            y.resetMaxEnd();
        }

        /**
         * Copies the data from a Node into this Node.
         * @param o - the other Node containing the data to be copied
         */
        private void copyData(Node o) {
            interval = o.interval;
        }
        
        @Override
        public String toString() {
            if (isNil()) {
                return "nil";
            } else {
                String color = isBlack ? "black" : "red"; 
                return "start = " + start() +
                       "\nend = " + end() +
                       "\nmaxEnd = " + maxEnd +
                       "\ncolor = " + color;
            }
        }
        
        /**
         * Ensures that red-black constraints and interval-tree constraints are
         * maintained after an insertion.
         */
        private void insertFixup() {
            Node z = this;
            while (z.parent.isRed()) {
                if (z.parent.isLeftChild()) {
                    Node y = z.parent.parent.right;
                    if (y.isRed()) {
                        z.parent.blacken();
                        y.blacken();
                        z.grandparent().redden();
                        z = z.grandparent();
                    } else {
                        if (z.isRightChild()) {
                            z = z.parent;
                            z.leftRotate();
                        }
                        z.parent.blacken();
                        z.grandparent().redden();
                        z.grandparent().rightRotate();
                    }
                } else {
                    Node y = z.grandparent().left;
                    if (y.isRed()) {
                        z.parent.blacken();
                        y.blacken();
                        z.grandparent().redden();
                        z = z.grandparent();
                    } else {
                        if (z.isLeftChild()) {
                            z = z.parent;
                            z.rightRotate();
                        }
                        z.parent.blacken();
                        z.grandparent().redden();
                        z.grandparent().leftRotate();
                    }
                }
            }
            root.blacken();
        }
        
        /**
         * Ensures that red-black constraints and interval-tree constraints are
         * maintained after deletion.
         */
        private void deleteFixup() {
            Node x = this;
            while (!x.isRoot() && x.isBlack) {
                if (x.isLeftChild()) {
                    Node w = x.parent.right;
                    if (w.isRed()) {
                        w.blacken();
                        x.parent.redden();
                        x.parent.leftRotate();
                        w = x.parent.right;
                    }
                    if (w.left.isBlack && w.right.isBlack) {
                        w.redden();
                        x = x.parent;
                    } else {
                        if (w.right.isBlack) {
                            w.left.blacken();
                            w.redden();
                            w.rightRotate();
                            w = x.parent.right;
                        }
                        w.isBlack = x.parent.isBlack;
                        x.parent.blacken();
                        w.right.blacken();
                        x.parent.leftRotate();
                        x = root;
                    }
                } else {
                    Node w = x.parent.left;
                    if (w.isRed()) {
                        w.blacken();
                        x.parent.redden();
                        x.parent.rightRotate();
                        w = x.parent.left;
                    }
                    if (w.left.isBlack && w.right.isBlack) {
                        w.redden();
                        x = x.parent;
                    } else {
                        if (w.left.isBlack) {
                            w.right.blacken();
                            w.redden();
                            w.leftRotate();
                            w = x.parent.left;
                        }
                        w.isBlack = x.parent.isBlack;
                        x.parent.blacken();
                        w.left.blacken();
                        x.parent.rightRotate();
                        x = root;
                    }                    
                }
            }
            x.blacken();
        }
        
        ///////////////////////////////
        // Node -- Debugging methods //
        ///////////////////////////////
        
        /**
         * Whether or not the subtree rooted at this Node is a valid
         * binary-search tree.
         * @param min - a lower-bound Node
         * @param max - an upper-bound Node
         */
        private boolean isBST(Node min, Node max) {
            if (isNil()) {
                return true;   // Leaves are a valid BST, trivially.
            }
            if (min != null && compareTo(min) <= 0) {
                return false;  // This Node must be greater than min
            }
            if (max != null && compareTo(max) >= 0) {
                return false;  // and less than max.
            }
            
            // Children recursively call method with updated min/max.
            return left.isBST(min, this) && right.isBST(this, max);
        }
        
        /**
         * Whether or not the subtree rooted at this Node is balanced.
         * <p>
         * Balance determination is done by calculating the black-height.
         * @param black - the expected black-height of this subtree
         */
        private boolean isBalanced(int black) {
            if (isNil()) {
                return black == 0;  // Leaves have a black-height of zero,
            }                       // even though they are black.
            if (isBlack) {
                black--;
            }
            return left.isBalanced(black) && right.isBalanced(black);
        }
        
        /**
         * Whether or not the subtree rooted at this Node has a valid
         * red-coloring.
         * <p>
         * A red-black tree has a valid red-coloring if every red node has two
         * black children.
         */
        private boolean hasValidRedColoring() {
            if (isNil()) {
                return true;
            } else if (isBlack) {
                return left.hasValidRedColoring() &&
                        right.hasValidRedColoring();
            } else {
                return left.isBlack && right.isBlack &&
                        left.hasValidRedColoring() &&
                        right.hasValidRedColoring();
            }
        }
        
        /**
         * Whether or not the subtree rooted at this Node has consistent maxEnd
         * values.
         * <p>
         * The maxEnd value of an interval-tree Node is equal to the maximum of
         * the end-values of all intervals contained in the Node's subtree.
         */
        private boolean hasConsistentMaxEnds() {

            if (isNil()) {                                    // 1. sentinel node
                return true;
            }
            
            if (hasNoChildren()) {                            // 2. leaf node
                return maxEnd == end();
            } else {
                boolean consistent = maxEnd >= end();
                if (hasTwoChildren()) {                       // 3. two children
                    return consistent &&
                           maxEnd >= left.maxEnd &&
                           maxEnd >= right.maxEnd &&
                           left.hasConsistentMaxEnds() &&
                           right.hasConsistentMaxEnds();
                } else if (left.isNil()) {                    // 4. one child -- right
                    return consistent &&
                           maxEnd >= right.maxEnd &&
                           right.hasConsistentMaxEnds();
                } else {
                    return consistent &&                      // 5. one child -- left
                           maxEnd >= left.maxEnd &&
                           left.hasConsistentMaxEnds();
                }
            }
        }
    }
    
    ///////////////////////
    // Tree -- Iterators //
    ///////////////////////
    
    /**
     * An Iterator which walks along the tree nodes in ascending order.
     */
    private class TreeNodeIterator implements Iterator<Node> {

        private Optional<Node> next;
        
        private TreeNodeIterator(Node root) {
            next = root.isNil() ? Optional.empty() : root.minimumNode();
        }
        
        @Override
        public boolean hasNext() {
            return next.isPresent();
        }

        @Override 
        public Node next() {
            Node nextNode = next.orElseThrow(() -> new NoSuchElementException("Interval tree has no more elements"));
            next = nextNode.successor();
            return nextNode;
        }   
    }

    /**
     * An Iterator which walks along the tree values in ascending order.
     */
    private class TreeIterator implements Iterator<T> {
        
        private TreeNodeIterator nodeIter;
        
        private TreeIterator(Node root) {
            nodeIter = new TreeNodeIterator(root);
        }

        @Override
        public boolean hasNext() {
            return nodeIter.hasNext();
        }

        @Override
        public T next() {
            return nodeIter.next().interval;
        }
    }
    
    private class OverlappingNodeIterator implements Iterator<Node> {
        
        private Optional<Node> next;
        private T interval;
        
        private OverlappingNodeIterator(Node root, T t) {
            interval = t;
            next = root.minimumOverlappingNode(interval);
        }
        
        @Override
        public boolean hasNext() {
            return next.isPresent();
        }
        
        @Override
        public Node next() {
            Node nextNode = next.orElseThrow(() -> new NoSuchElementException("Interval tree has no more overlapping nodes."));
            next = nextNode.nextOverlappingNode(interval);
            return nextNode;
        }
    }
    
    private class OverlapperIterator implements Iterator<T> {
        
        private OverlappingNodeIterator nodeIter;
        
        private OverlapperIterator(Node root, T t) {
            nodeIter = new OverlappingNodeIterator(root, t);
        }

        @Override
        public boolean hasNext() {
            return nodeIter.hasNext();
        }

        @Override
        public T next() {
            return nodeIter.next().interval;
        }
    }

    ///////////////////////////////
    // Tree -- Debugging methods //
    ///////////////////////////////
    
    /**
     * Whether or not this IntervalTree is a valid binary-search tree.
     * <p>
     * This method will return false if any Node is less than its left child
     * or greater than its right child.
     * <p>
     * This method is used for debugging only, and its access is changed in
     * testing.
     */
    @SuppressWarnings("unused")
    private boolean isBST() {
        return root.isBST(null, null);
    }

    /**
     * Whether or not this IntervalTree is balanced.
     * <p>
     * This method will return false if all of the branches (from root to leaf)
     * do not contain the same number of black nodes. (Specifically, the
     * black-number of each branch is compared against the black-number of the
     * left-most branch.)
     * <p>
     * This method is used for debugging only, and its access is changed in
     * testing.
     */
    @SuppressWarnings("unused")
    private boolean isBalanced() { 
        int black = 0;
        Node x = root;
        while (!x.isNil()) {
            if (x.isBlack) {
                black++;
            }
            x = x.left;
        }
        return root.isBalanced(black);
    }
    
    /**
     * Whether or not this IntervalTree has a valid red coloring.
     * <p>
     * This method will return false if all of the branches (from root to leaf)
     * do not contain the same number of black nodes. (Specifically, the
     * black-number of each branch is compared against the black-number of the
     * left-most branch.)
     * <p>
     * This method is used for debugging only, and its access is changed in
     * testing.
     */
    @SuppressWarnings("unused")
    private boolean hasValidRedColoring() {
        return root.hasValidRedColoring();
    }
    
    /**
     * Whether or not this IntervalTree has consistent maxEnd values.
     * <p>
     * This method will only return true if each Node has a maxEnd value equal
     * to the highest interval end value of all the intervals in its subtree.
     * <p>
     * This method is used for debugging only, and its access is changed in
     * testing.
     */
    @SuppressWarnings("unused")
    private boolean hasConsistentMaxEnds() {
        return root.hasConsistentMaxEnds();
    }
}