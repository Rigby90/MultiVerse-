package com.onarandombox.Rigby.util;

public class Cuboid {
    /**
     * @return min point
     */
    //public Vector getMinimumPoint(int pos1X,double pos1Y,double pos1Z,double x,double y,double z) {
        //return new Vector(Math.min(pos1X, x),
                         //Math.min(pos1Y, y),
                         //Math.min(pos1Z, z));
    //}
    /**
     * @return max point
     */
    public Vector getMaximumPoint(int x,int y,int z,int x2,int y2,int z2) {
        return new Vector(Math.max(x, x2),
                         Math.max(y, y2),
                         Math.max(z, z2));
    }
    /**
     * Get the number of blocks in the region.
     * 
     * @return number of blocks
     */
    //public int getSize(int pos1X,int pos1Y,int pos1Z,int pos2X,int pos2Y,int pos2Z) {

        //Vector min = getMinimumPoint(pos1X, pos1Y, pos1Z, pos2X, pos2Y, pos2Z);
        //Vector max = getMaximumPoint(pos1X, pos1Y, pos1Z, pos2X, pos2Y, pos2Z);

        //return (int)((max.getX() - min.getX() + 1) *
                     //(max.getY() - min.getY() + 1) *
                     //(max.getZ() - min.getZ() + 1));
    //}
    /*
     * Get the number of blocks in a region.
     */
	public static int getSize(Vector max, Vector min) {
		return (int)((max.getX() - min.getX() + 1) *
                (max.getY() - min.getY() + 1) *
                (max.getZ() - min.getZ() + 1));
	}
}
