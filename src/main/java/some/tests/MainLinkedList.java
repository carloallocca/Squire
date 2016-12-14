/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package some.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import uk.ac.open.kmi.squire.core2.QueryAndContextNode;

/**
 *
 * @author carloallocca
 */
public class MainLinkedList {

    private static ArrayList<QueryAndContextNode> llist = new ArrayList();

    public static void main(String[] args) {

        float qrScore1 = (float) 3.5;
        float qrScore2 = (float) 5.5;
        float qrScore3 = (float) 8.5;
        float qrScore4 = (float) 1.5;
        float qrScore5 = (float) 9.5;
        float qrScore6 = (float) 4.5;

        float qrScore7 = (float) 6.5;

        QueryAndContextNode d1 = new QueryAndContextNode();
        d1.setqRScore(qrScore1);
        QueryAndContextNode d2 = new QueryAndContextNode();
        d2.setqRScore(qrScore2);
        QueryAndContextNode d3 = new QueryAndContextNode();
        d3.setqRScore(qrScore3);
        QueryAndContextNode d4 = new QueryAndContextNode();
        d4.setqRScore(qrScore4);
        QueryAndContextNode d5 = new QueryAndContextNode();
        d5.setqRScore(qrScore5);
        QueryAndContextNode d6 = new QueryAndContextNode();
        d6.setqRScore(qrScore6);

//        insertSortedLL(d1);
//        insertSortedLL(d2);
//        insertSortedLL(d3);
//        insertSortedLL(d4);
//        insertSortedLL(d5);
//        insertSortedLL(d6);

        insertSorted(d1);
        insertSorted(d2);
        insertSorted(d3);
        insertSorted(d4);
        insertSorted(d5);
        insertSorted(d6);




    }

    private static ArrayList<QueryAndContextNode> insertSortedLL(QueryAndContextNode d1) {

        llist.add(d1);
//        Collections.sort(llist, QueryAndContextNode.queryScoreComp);
        Collections.sort(llist, new QueryAndContextNode.QRScoreComparator());
        
        return llist;
    }
    
    private static void insertSorted(QueryAndContextNode d1) {

        llist.add(d1);
//        Collections.sort(llist, QueryAndContextNode.queryScoreComp);
        Collections.sort(llist, new QueryAndContextNode.QRScoreComparator());
        
    }
    
    
}
