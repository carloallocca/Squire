/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package json.test;

//import com.google.gson.Gson;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.mksmart.squire.websquire.v1.resources.QueryStringScorePair;

/**
 *
 * @author carloallocca
 */
public class SetToJson {
    
    
    public static void main(String[] args) throws InterruptedException {

        SortedSet<String> recommandedQueryList = new TreeSet(); 
    
        recommandedQueryList.add("carlo");
        recommandedQueryList.add("mathieu");
        recommandedQueryList.add("alessandro");
        recommandedQueryList.add("enrico");
    
//        Gson gson = new Gson();
//  
//        // 2. Java object to JSON, and assign to a String
//        String jsonInString = gson.toJson(recommandedQueryList);
//        System.out.println(jsonInString);           
    }
    
}
