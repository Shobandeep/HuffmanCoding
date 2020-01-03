import java.io.*;
import java.util.Scanner;

import static java.lang.System.exit;

public class HuffmanCoding {

    private int[] charCountAry;
    private String[] charCode;

    public HuffmanCoding(String input) {

        String debug = input + "_DeBug.txt";
        String compressed = input + "_Compressed.txt";
        String deCompressed = input + "_DeCompress.txt";

        try {
            // input file
            FileReader inputFile = new FileReader(input);

            //output files
            FileWriter debugFile = new FileWriter(debug);
            FileWriter compressedFile = new FileWriter(compressed);
            FileWriter deCompressedFile = new FileWriter(deCompressed);

            // create binary tree
            BinaryTree binaryTree = new BinaryTree(inputFile, debugFile);
            debugFile.close();
            inputFile.close();

            // compress file
            inputFile = new FileReader(input);
            encode(inputFile, compressedFile);
            compressedFile.close();
            inputFile.close();




            // reopen compressed file and decompress it
            FileReader myCompressedFile = new FileReader(compressed);
            decode(myCompressedFile, deCompressedFile, binaryTree);
            myCompressedFile.close();
            deCompressedFile.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    private class TreeNode {
        // variables
        String chStr;
        String code;
        int prob;
        TreeNode left;
        TreeNode right;
        TreeNode next;

        public TreeNode(String chStr, String code, int prob, TreeNode left, TreeNode right, TreeNode next) {
            this.chStr = chStr;
            this.code = code;
            this.prob = prob;
            this.left = left;
            this.right = right;
            this.next = next;
        }

        public void printNode(FileWriter writer) {
            String nextChar = this.next == null ? "NULL" : this.next.chStr;
            String leftChar = this.left == null ? "NULL" : this.left.chStr;
            String rightChar = this.right == null ? "NULL" : this.right.chStr;

            try {
                writer.append("(\"" + this.chStr + "\", " + this.prob + ", next: \"" + nextChar + "\", left: \"" + leftChar + "\", right: \"" + rightChar + "\")\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class LinkedList {
        // variables
        TreeNode head;

        // constuctor
        LinkedList() {
            // create dummy head
            head = new TreeNode("dummy", "", 0, null, null, null);
        }


        // insert new node in accending order
        public void insertNewNode(TreeNode newNode) {
            insert(findSpot(newNode), newNode);
        }

        // helper method for insertNewNode, finds insertion spot
        private TreeNode findSpot(TreeNode newNode) {
            TreeNode spot = head;

            while(spot.next != null && spot.next.prob < newNode.prob)
                spot = spot.next;

            return spot;
        }

        // helper method for insertNewNode, inserts given node after spot
        private void insert(TreeNode spot, TreeNode newNode) {
            newNode.next = spot.next;
            spot.next = newNode;
        }

        // method prints the contents of the linked list to a given file
        public void printList(FileWriter writer) {
            TreeNode spot = head;

            try {

                writer.append("listHead->");

                while(spot.next != null) {
                    writer.append("(\"" + spot.chStr + "\", " + spot.prob + ", \"" + spot.next.chStr + "\")->");
                    spot = spot.next;
                }

                writer.append("(\"" + spot.chStr + "\", " + spot.prob + ", " + "NULL)-> NULL\n");


            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    public class BinaryTree {
        // variables
        private TreeNode root;

        //constructor
        public BinaryTree(FileReader input, FileWriter writer) {
            // create charCountAry
            computeCharCounts(input);
            // print countAry
            printCountAry(writer);
            // initialize charCode
            charCode = new String[256];
            // this method will call constructHuffmanBinTree
            constructHuffmanLList(writer);

            // print trees

            try {
                writer.append("\npreOrder Traversal:\n");
                preOrderTraversal(root, writer);

                writer.append("\ninOrder Traversal:\n");
                inOrderTraversal(root, writer);

                writer.append("\npostOrder Traversal:\n");
                postOrderTraversal(root, writer);

            } catch (IOException e) {
                e.printStackTrace();
            }


            // generate codes
            constructCharCode(root, "");
        }

        private void constructHuffmanLList(FileWriter writer) {
            // initialize variables
            LinkedList list = new LinkedList();

            // create linked list of char /
            for(int i = 0;i < charCountAry.length; i++) {
                if(charCountAry[i] > 0) {
                    list.insertNewNode(new TreeNode(((char)i)+"", "", charCountAry[i], null, null, null));
                }
            }
            list.printList(writer);

            // construct the binary tree using the generated linked list
            constructHuffmanBinTree(list, writer);
        }

        private void constructHuffmanBinTree(LinkedList list, FileWriter writer) {
            // declare variables
            TreeNode listHead = list.head;
            TreeNode newNode;
            TreeNode temp1;
            TreeNode temp2;

            // continue until there is only one node left in the list
            while(listHead.next.next != null) {
                temp1 = listHead.next;
                temp2 = listHead.next.next;
                // combine the values of the two nodes after list head and insert into list
                newNode = new TreeNode(temp1.chStr+temp2.chStr, "", temp1.prob+temp2.prob, temp1, temp2, null);
                list.insertNewNode(newNode);
                // make head's next point to the 3rd node in the list
                listHead.next = listHead.next.next.next;

                //print solution to debug output file
                list.printList(writer);
            }

            // root will hold the remaining node
            root = listHead.next;
        }

        private void preOrderTraversal(TreeNode node, FileWriter writer) {
            if(isLeaf(node))
                node.printNode(writer);
            else {
                node.printNode(writer);
                preOrderTraversal(node.left, writer);
                preOrderTraversal(node.right, writer);
            }
        }

        private void inOrderTraversal(TreeNode node, FileWriter writer) {
            if(isLeaf(node))
                node.printNode(writer);
            else {
                inOrderTraversal(node.left, writer);
                node.printNode(writer);
                inOrderTraversal(node.right, writer);
            }
        }

        private void postOrderTraversal(TreeNode node, FileWriter writer) {
            if(isLeaf(node))
                node.printNode(writer);
            else {
                postOrderTraversal(node.left, writer);
                postOrderTraversal(node.right, writer);
                node.printNode(writer);
            }
        }

        // recursively generate char codes
        private void constructCharCode(TreeNode node, String code) {
            if(isLeaf(node)) {
                node.code = code;
                int index = (int)node.chStr.charAt(0);
                charCode[index] = code;
            }
            else {
                constructCharCode(node.left, code + "0");
                constructCharCode(node.right, code + "1");
            }
        }

        // returns true of a node is a leaf
        private boolean isLeaf(TreeNode node) {
            if(node.left == null && node.right == null) {
                return true;
            }
            return false;
        }
    }


    private void computeCharCounts(FileReader inFile) {
        // charCounts is initialized to 0 by default in java
        charCountAry = new int[256];
        char charIn;
        int index = 0;

        try {


            // read one char at a time
            while(index != -1) {

                index = inFile.read();
                charIn = (char)index;

                // make sure index is an ASCII value between 0 and 255
                // also make sure there is no space(\s), new line(\n) or carriage return(\r)
                if(index > 0 && index < 256)
                    charCountAry[index]++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void printCountAry(FileWriter writer) {
        try {
            for(int i = 0; i < charCountAry.length; i++) {
                if(charCountAry[i] != 0)
                    writer.write((char)i + " " + charCountAry[i] + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void encode(FileReader deCompressedFile, FileWriter compressedFile) {
        int index = 0;

        try {

            // read and encode one char at a time
            while(index != -1) {

                index = deCompressedFile.read();
                if(index != -1 && charCode[index] != null)
                    compressedFile.append(charCode[index]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void decode(FileReader compressedFile, FileWriter deCompressedFile, BinaryTree binaryTree) {
        TreeNode spot = binaryTree.root;
        Scanner scanner = new Scanner(compressedFile);
        String bits = scanner.next();
        int bit = -1;


        try {
            for(int i = 0; i < bits.length(); i++) {
                if(binaryTree.isLeaf(spot)) {
                    deCompressedFile.append(spot.chStr);
                    spot = binaryTree.root;
                }
                bit = Character.getNumericValue(bits.charAt(i));
                if(bit == 1)
                    spot = spot.right;
                else if(bit == 0)
                    spot = spot.left;
                else {
                    System.out.println("Error! The compress file contains invalid character!");
                    exit(1);
                }


            }

            if(!binaryTree.isLeaf(spot)) {
                System.out.println("Error:  The compressed file is corrupted!”");
                exit(1);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static void main(String[] args) {

        HuffmanCoding huffmanCoding = new HuffmanCoding(args[0]);

    }
}
