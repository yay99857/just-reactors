package com.justreactors.client;

import net.minecraft.client.gui.GuiGraphics;

/** 16x16 pixel icons drawn with fills, for things that have no fitting item. */
public enum PixelIcon {
    BOLT(new String[]{
            "................",
            ".........oooooo.",
            "........oyyyyyo.",
            ".......oylyyyo..",
            "......oylyyyo...",
            ".....oylyyyo....",
            "....oylyyyoooo..",
            "...oylyyyyyyyo..",
            "..oooooyyyyyo...",
            "......oyyyyo....",
            ".....oyyydo.....",
            "....oyyddo......",
            "...oyddo........",
            "..oddo..........",
            "..ooo...........",
            "................"},
            'o', 0xFF2A1F05, 'y', 0xFFFFD43B, 'l', 0xFFFFF4B0, 'd', 0xFFE09A1A),
    FLAME(new String[]{
            ".......o........",
            "......oro.......",
            "......orro......",
            ".....orrro..o...",
            ".....orrdro.oo..",
            "....orrddrooro..",
            "...orrdddrrorro.",
            "...orddyddrrrro.",
            "..orrdyyyddrrro.",
            "..ordyyyyydrrro.",
            "..ordyywyyddrro.",
            "..orddywwyydrro.",
            "..orddywwwydro..",
            "...ordyywwydro..",
            "....orddyyddo...",
            ".....oooooooo..."},
            'o', 0xFF2B0B05, 'r', 0xFFE0352F, 'd', 0xFFF0862E, 'y', 0xFFFFD43B, 'w', 0xFFFFF7D6),
    THERMOMETER(new String[]{
            "......oooo......",
            ".....oggggo.....",
            ".....ogwggo.tt..",
            ".....ogwggo.....",
            ".....ogwggo.tt..",
            ".....orrrro.....",
            ".....orwrro.tt..",
            ".....orwrro.....",
            ".....orrrro.tt..",
            "....orrrrrro....",
            "...orrrrrrrro...",
            "...orwwrrrrro...",
            "...orwrrrrrro...",
            "...orrrrrrdro...",
            "....orrrdddo....",
            ".....oooooo....."},
            'o', 0xFF1B1D22, 'g', 0xFF9AA5B1, 'w', 0xFFF4F7FA, 'r', 0xFFE0352F, 'd', 0xFFA4221E, 't', 0xFFB9C2CE),
    RADIATION(new String[]{
            "....oooooooo....",
            "..ooyyyyyyyyoo..",
            ".oyykkyyyykkyyo.",
            ".oykkkyyyykkkyo.",
            "oykkkkyyyykkkkyo",
            "oykkkkkyykkkkkyo",
            "oyyyyyokkoyyyyyo",
            "oyyyyyykkyyyyyyo",
            "oyyyyyyyyyyyyyyo",
            "oyyyyykkkkyyyyyo",
            "oyyyykkkkkkyyyyo",
            ".oyyykkkkkkyyyo.",
            ".oyyyykkkkyyyyo.",
            "..ooyyyyyyyyoo..",
            "....oooooooo....",
            "................"},
            'o', 0xFF2A2205, 'y', 0xFFF2D13A, 'k', 0xFF1A1A1A);

    private final String[] rows;
    private final int[] colors = new int[128];

    PixelIcon(String[] rows, Object... palette) {
        this.rows = rows;
        for (int i = 0; i < palette.length; i += 2) {
            colors[(Character) palette[i]] = (Integer) palette[i + 1];
        }
    }

    public void draw(GuiGraphics g, int x, int y) {
        for (int r = 0; r < rows.length; r++) {
            String row = rows[r];
            int c = 0;
            while (c < row.length()) {
                char ch = row.charAt(c);
                if (ch == '.') {
                    c++;
                    continue;
                }
                int start = c;
                while (c < row.length() && row.charAt(c) == ch) c++;
                g.fill(x + start, y + r, x + c, y + r + 1, colors[ch]);
            }
        }
    }
}
