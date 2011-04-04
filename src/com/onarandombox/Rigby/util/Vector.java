package com.onarandombox.Rigby.util;

public class Vector {
    protected int x, y, z;

    public Vector(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public int getX() {
        return x;
    }

    public Vector setX(int x) {
        return new Vector(x, y, z);
    }

    public int getY() {
        return y;
    }

    public Vector setY(int y) {
        return new Vector(x, y, z);
    }

    public int getZ() {
        return z;
    }

    public Vector setZ(int z) {
        return new Vector(x, y, z);
    }
 
    
    /**
     * Get the distance away from a point.
     * 
     * @param pt
     * @return distance
     */
    public double distance(Vector pt) {
        return Math.sqrt(Math.pow(pt.x - x, 2) +
                Math.pow(pt.y - y, 2) +
                Math.pow(pt.z - z, 2));
    }

    /**
     * Checks to see if a vector is contained with another.
     * 
     * @param min
     * @param max
     * @return
     */
    public boolean containedWithin(Vector min, Vector max) {
        return x >= min.getX() && x <= max.getX()
                && y >= min.getY() && y <= max.getY()
                && z >= min.getZ() && z <= max.getZ();
    }
}