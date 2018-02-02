/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.open.kmi.squire.treequerypatterns;

import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementVisitorBase;

/**
 *
 * @author callocca
 */
public class ElementPathBlockVisitor extends ElementVisitorBase{

    private ElementPathBlock elementPathBlock=null;
    
    public ElementPathBlockVisitor(){
        super();
    }
    
    
    @Override
    public void visit(ElementPathBlock el) {
        this.elementPathBlock=el;
    }

    public ElementPathBlock getElementPathBlock() {
        return elementPathBlock;
    }
    
    

}
