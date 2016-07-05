package datastructures;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    
    /**
     * Whether this IntervalTree is empty or not.
     */
    public boolean isEmpty() {
        return root.isNil();
    }
    
    public Optional<Node> search(T t) {
        return root.search(t);
    }
    
    /**
     * The Node containing the minimum value stored in this IntervalTree.
     * @return an Optional containing the minimum Node in this IntervalTree,
     * or an empty Optional if there is no such Node (that is, if the tree is
     * empty).
     */
    public Optional<Node> minimum() {
        return root.minimum();
    }
    
    /**
     * Deletes the minimum value from this IntervalTree.
     * <p>
     * If there is no minimum value (that is, if the tree is empty), this
     * method does nothing.
     */
    public void deleteMin() {
        root.minimum().ifPresent(k -> k.delete());
    }

    /**
     * The Node containing the maximum value stored in this IntervalTree.
     * @return an Optional containing the maximum Node in this IntervalTree,
     * or an empty Optional if there is no such Node (that is, if the tree is
     * empty).
     */
    public Optional<Node> maximum() {
        return root.maximum();
    }
    
    /**
     * Deletes the maximum value from this IntervalTree.
     * <p>
     * If there is no maximum value (that is, if the tree is empty), this
     * method does nothing.
     */
    public void deleteMax() {
        root.maximum().ifPresent(k -> k.delete());
    }
    
    /**
     * The number of intervals stored in this IntervalTree.
     */
    public int size() {
        return root.size();
    }
    
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
    
    /**
     * Deletes the given value from the IntervalTree.
     * <p>
     * If the value does not exist in the tree, this method does nothing.
     * @param t - the value to delete from the tree
     */
    public void delete(T t) {
        search(t).ifPresent(k -> k.delete());
    }
    
    /**
     * An iterator which traverses the tree in ascending order.
     */
    public Iterator<T> iterator() {
        return new TreeIterator(root);
    }

    /**
     * A representation of a node of an interval tree.
     */
    public class Node implements Comparable<Node> {
        
        /* Most of the "guts" of the interval tree are actually methods called
         * by nodes. For example, IntervalTree#delete(val) searches up the Node
         * containing val; then that Node deletes itself with Node#delete().
         */

        private T data;
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
            data = null;
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
            this.data = data;
            parent = nil;
            left = nil;
            right = nil;
            maxEnd = data.end();
            redden();
        }
        
        /**
         * Returns the data contained by this Node.
         */
        public T getValue() {
            return data;
        }
        
        /**
         * Whether or not this Node is the root of its tree.
         */
        public boolean isRoot() {
            return (!isNil() && parent.isNil());
        }
        
        /**
         * Searches the subtree rooted at this Node for the given value.
         * @param t - the value to search for
         * @return an Optional containing, if it exists, the Node with the
         * given value; otherwise an empty Optional 
         */
        private Optional<Node> search(T t) {

            Node n = this;
            
            while (!n.isNil() && t.compareTo(n.data) != 0) {
                n = t.compareTo(n.data) == -1 ? n.left : n.right;
            }
            return n.isNil() ? Optional.empty() : Optional.of(n);
        }

        /**
         * Searches the subtree rooted at this Node for its minimum value.
         * @return an Optional containing, if it exists, the Node with the
         * minimum value; otherwise (that is, if this subtree is empty) an
         * empty Optional 
         */
        private Optional<Node> minimum() {
            
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
        private Optional<Node> maximum() {
            
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
        public Optional<Node> successor() {
            
            if (!right.isNil()) {
                return right.minimum();
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
        public Optional<Node> predecessor() {
            
            if (!left.isNil()) {
                return left.maximum();
            }
            
            Node x = this;
            Node y = parent;
            while (!y.isNil() && x == y.left) {
                x = y;
                y = y.parent;
            }
            
            return y.isNil() ? Optional.empty() : Optional.of(y);
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
            int val = data.end();
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
         * Deletes this Node from its tree.
         * <p>
         * More specifically, removes the data held within this Node from the
         * tree. Depending on the structure of the tree at this Node, this
         * particular Node instance may not be removed; rather, a different
         * Node may be deleted and that Node's contents copied into this one,
         * overwriting the previous contents.
         */
        public void delete() {
            
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
        
        /**
         * Removes this Node from its tree.
         * <p>
         * More specifically, removes the data held within this Node from the
         * tree. Depending on the structure of the tree at this Node, this
         * particular Node instance may not be removed; rather, a different
         * Node may be deleted and that Node's contents copied into this one,
         * overwriting the previous contents.
         * <p>
         * This method is identical to delete().
         */
        public void remove() {
            delete();
        }
        
        /**
         * Copies the data from a Node into this Node.
         * @param o - the other Node containing the data to be copied
         */
        private void copyData(Node o) {
            data = o.data;
        }
        
        /**
         * Whether or not this Node is the sentinel node.
         */
        private boolean isNil() {
            return this == nil;
        }

        /**
         * Whether or not this Node is the left child of its parent.
         */
        private boolean isLeftChild() {
            return this == parent.left;
        }

        /**
         * Whether or not this Node is the right child of its parent.
         */
        private boolean isRightChild() {
            return this == parent.right;
        }

        /**
         * Whether or not this Node has no children, i.e., a leaf.
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
         * The number of elements in the subtree rooted at this Node.
         * <p>
         * Leaf nodes are not included in the number. The Node calling this
         * method is included in the number.
         */
        private int size() {
            return isNil() ? 0 : 1 + left.size() + right.size();
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

        @Override
        public int compareTo(IntervalTree<T>.Node o) {
            return data.compareTo(o.data);
        }
        
        @Override
        public String toString() {
            if (isNil()) {
                return "nil";
            } else {
                return "data = " + data.toString() + "\nmax = " + maxEnd;
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
                return maxEnd == data.end();
            } else {
                boolean consistent = maxEnd >= data.end();
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
    
    /**
     * An Iterator which walks along the tree in ascending order.
     */
    private class TreeIterator implements Iterator<T> {

        private Optional<Node> next;
        
        private TreeIterator(Node root) {
            next = root.isNil() ? Optional.empty() : root.minimum();
        }
        
        @Override
        public boolean hasNext() {
            return next.isPresent();
        }

        @Override 
        public T next() {
            Node nextNode = next.orElseThrow(() -> new NoSuchElementException("Interval tree has no more elements"));
            next = nextNode.successor();
            return nextNode.getValue();
        }   
    }

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