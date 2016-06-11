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
public class Couple {

    private String nodeA;
    private String nodeB;

    public Couple(String nodeA, String nodeB) {
        this.nodeA = nodeA;
        this.nodeB = nodeB;
    }

    public String getNodeA() {
        return nodeA;
    }

    public String getNodeB() {
        return nodeB;
    }

    @Override
    public String toString() {
        return nodeA + " " + nodeB;
    }

    @Override
    public boolean equals(Object c) {
        if (((Couple)c).getNodeA().equals(nodeA) && ((Couple)c).getNodeB().equals(nodeB)) {
            return true;
        } else if (((Couple)c).getNodeA().equals(nodeB) && ((Couple)c).getNodeB().equals(nodeA)) {
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.nodeA.hashCode() + this.nodeB.hashCode();
    }
}
