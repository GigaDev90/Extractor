/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

/**
 *
 * @author gabriele
 */
public class CouplePlus extends Couple{
    
    private double pred;
    
    public CouplePlus(String nodeA, String nodeB, double pred) {
        super(nodeA, nodeB);
        this.pred = pred;
    }

    public double getPred() {
        return pred;
    }

    public void setPred(double pred) {
        this.pred = pred;
    }
    
}
