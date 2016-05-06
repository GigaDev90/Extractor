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
            return nodeA+" "+nodeB;
        }
        
        public boolean equals(Couple c) {
            if ( c.getNodeA().equals(nodeA) && c.getNodeB().equals(nodeB)) {
                return true;
            } else if (c.getNodeA().equals(nodeB) && c.getNodeB().equals(nodeA)) {
                return true;
            }
            
            return false;
        }
    }
