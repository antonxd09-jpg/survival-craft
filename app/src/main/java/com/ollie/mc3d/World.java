package com.ollie.mc3d;

public class World {
    public static final int SIZE = 16; // Мир 16х16
    public static final int HEIGHT = 16;
    
    // 0 - воздух, 1 - трава, 2 - земля, 3 - камень
    public int[][][] blocks = new int[SIZE][HEIGHT][SIZE];

    public World() {
        generateFlatWorld();
    }

    private void generateFlatWorld() {
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                for (int y = 0; y < HEIGHT; y++) {
                    if (y < HEIGHT - 4) blocks[x][y][z] = 3; // Камень
                    else if (y < HEIGHT - 1) blocks[x][y][z] = 2; // Земля
                    else blocks[x][y][z] = 1; // Трава
                }
            }
        }
    }
}
