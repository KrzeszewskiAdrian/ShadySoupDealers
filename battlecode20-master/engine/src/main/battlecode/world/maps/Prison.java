package battlecode.world.maps;

import battlecode.common.GameConstants;
import battlecode.world.MapBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Generate a map.
 */
public class Prison {

    // change this!!!
    public static final String mapName = "Prison";

    // don't change this!!
    public static final String outputDirectory = "engine/src/main/battlecode/world/resources/";

    private static int width;
    private static int height;

    private static boolean usesIndex;

    /**
     * @param args unused
     */
    public static void main(String[] args) {
        try {
            makeSimple();
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println("Generated a map!");
    }

    public static ArrayList<ArrayList<Boolean>> makemap(Boolean contents,int w,int h) {
        ArrayList<ArrayList<Boolean>> arr = new ArrayList<>();
        for (int i=0; i<h; i++) {
            ArrayList<Boolean> b = new ArrayList<>();
            for (int j=0; j<w; j++) {
                b.add(contents);
            }
            arr.add(b);
        }
        return arr;
    }

    public static int loc2index(int x, int y) {
        if (usesIndex) {
            return (height - y)*65 + x + 1;
        }
        return (height-1-y)*width + x;
    }

    public static void makeSimple() throws IOException {

        String ds = "64	64	r	50	500	-4	/* Symbols: 'x' for symmetry-inferred, 'w' for infinite-depth water, 'W' for W-depth water, 's' and 'S' for soup, 'c' for cow, 'h' for HQ. Append a number to set elevation. 'Order must be w,s,c,h; so Wsch10' is valid syntactically (but not logically: the HQ can't start in water or have a cow on it) */																																																										 "+
"indx	0	1	2	3	4	5	6	7	8	9	10	11	12	13	14	15	16	17	18	19	20	21	22	23	24	25	26	27	28	29	30	31	32	33	34	35	36	37	38	39	40	41	42	43	44	45	46	47	48	49	50	51	52	53	54	55	56	57	58	59	60	61	62	63 "+
"63	w	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"62	w	w	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"61	w	w	w	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"60	w	w	w	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"59	w	w	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"58	w	w	5	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"57	w	5	5	5	5	5	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"56	w	5	5	5	5	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"55	w	5	5	5	5	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"54	w	5	5	5	5	10	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"53	w	5	5	5	5	10	10	10	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"52	w	5	5	5	10	10	10	10	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"51	w	5	5	5	10	10	10	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"50	5	5	5	5	10	10	10	10	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"49	5	5	5	5	10	10	10	10	10	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"48	5	5	5	5	10	10	10	10	5	10	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"47	5	5	5	5	10	10	10	5	5	10	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"46	5	5	5	5	10	10	5	5	5	5	10	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"45	5	5	5	5	10	5	5	5	5	5	10	5	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"44	5	5	5	5	5	5	5	5	5	5	5	10	5	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"43	5	5	5	5	5	5	5	5	10	5	5	10	5	5	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"42	5	5	5	5	5	5	5	5	10	5	5	5	5	10	10	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"41	5	5	5	5	5	5	5	10	10	5	5	5	5	5	10	10	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"40	5	5	5	5	5	10	5	10	10	5	5	5	5	5	10	10	10	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"39	5	5	5	5	10	5	5	10	10	5	5	5	5	5	10	10	10	10	10	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"38	5	5	5	5	10	5	5	10	10	10	10	5	5	5	10	10	10	5	5	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"37	5	5	5	5	10	5	5	5	10	10	10	5	5	5	5	10	5	5	5	10	10	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"36	5	5	5	5	10	5	S10	S10	5	5	5	5	c0	c0	5	10	5	5	10	10	10	10	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"35	5	5	5	5	10	10	S10	S10	5	5	5	5	c0	c0	5	10	5	5	5	5	5	5	10	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"34	5	5	5	5	5	10	10	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	10	10	W	W	W	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"33	5	5	5	5	5	5	10	10	10	5	5	10	10	10	10	5	5	10	10	10	5	5	5	5	10	10	W	5	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"32	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	10	10	10	10	10	5	5	5	5	10	10	5	5	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"31	5	5	5	5	10	5	5	5	5	5	5	5	5	5	5	5	5	10	10	10	10	10	5	5	5	5	5	5	5	5	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"30	5	5	5	5	10	10	5	5	5	5	5	5	10	10	10	10	5	5	10	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"29	5	5	5	5	10	10	10	5	5	5	5	10	5	5	5	10	10	5	10	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"28	5	5	5	5	10	10	10	10	5	5	5	5	5	S10	S10	5	10	5	10	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"27	5	5	5	5	10	10	10	10	5	5	10	5	5	S10	s10	5	10	5	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"26	5	5	5	5	10	10	10	10	5	5	10	5	5	5	5	10	5	5	10	10	5	5	5	5	5	5	5	5	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"25	5	5	5	5	10	10	10	10	5	5	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"24	5	5	5	5	10	10	10	10	5	5	5	10	10	5	5	5	5	10	5	5	5	5	5	5	5	5	5	5	5	10	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"23	5	5	5	5	10	10	10	10	5	5	5	10	10	10	10	10	10	10	5	5	5	5	5	5	5	5	5	5	10	s5	10	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"22	5	5	5	5	10	10	10	5	5	5	10	10	10	10	10	10	10	10	5	5	5	5	5	5	5	5	5	10	S10	S10	10	10	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"21	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	10	10	5	5	5	5	5	5	5	5	5	10	10	S10	S10	s5	10	10	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"20	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	5	5	10	5	10	s5	s5	5	5	5	5	5	5	5	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"19	5	5	5	5	10	10	10	10	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	5	5	5	5	5	5	5	5	10	10	10	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"18	5	5	5	5	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"17	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	10	10	10	10	5	5	10	5	s5	s5	S15	5	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"16	5	5	s7	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	5	5	5	10	10	5	5	10	5	5	5	10	10	5	10	5	s5	S15	S15	S15	5	10	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"15	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	10	5	5	5	10	5	5	5	10	5	c0	c0	5	10	5	10	10	5	s5	S15	S15	5	10	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"14	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	5	5	5	10	10	5	5	5	5	c0	c0	5	10	5	10	10	10	5	s5	s5	5	10	W	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"13	5	5	S7	5	5	5	5	5	5	5	5	5	5	s7	5	5	5	10	5	5	5	10	5	5	5	10	10	5	5	5	5	5	5	5	10	5	5	10	10	10	5	5	10	5	10	W	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x	x "+
"12	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	5	5	5	5	5	10	10	10	5	5	10	10	10	5	5	5	5	10	10	10	10	5	5	5	10	W	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x	x "+
"11	5	5	5	5	5	5	5	5	5	5	5	S7	5	5	5	5	5	5	10	5	5	5	10	10	5	5	5	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	W	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x	x "+
"10	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	10	10	5	5	5	5	5	5	5	5	5	5	10	10	5	5	5	5	5	5	5	5	5	10	10	W	W	W	W	W	W	x	x	x	x	x	x	x	x	x	x "+
"9	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	10	10	5	5	5	5	5	5	10	10	10	10	10	10	10	10	10	5	5	5	5	5	5	10	10	W	W	W	W	W	W	W	x	x	x	x	x	x	x	x	x "+
"8	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	10	5	5	5	5	5	5	10	10	S10	S10	s5	10	10	5	5	10	10	10	10	5	5	5	10	10	10	W	W	W	W	W	W	W	x	x	x	x	x	x	x	x "+
"7	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	5	5	5	5	5	5	5	5	10	s10	S10	5	5	5	5	5	5	5	5	10	10	5	5	5	10	10	W	W	W	W	W	W	W	W	x	x	x	x	x	x	x "+
"6	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	5	5	5	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	10	5	5	W	W	W	W	W	W	5	x	x	x	x	x	x "+
"5	5	5	5	S7	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	5	5	10	10	10	10	10	5	5	5	10	10	10	10	5	5	10	10	5	5	5	5	5	5	10	10	5	5	5	W	W	W	W	5	5	5	x	x	x	x	x "+
"4	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	10	5	5	10	5	5	10	10	10	10	10	10	5	5	10	10	10	10	5	5	10	10	10	10	5	5	10	10	10	5	5	5	5	5	5	5	5	5	5	5	5	x	x	x	x "+
"3	5	5	5	5	5	S7	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	w	x	x	x "+
"2	5	5	h5	5	5	5	5	5	5	5	5	5	5	S7	5	5	s7	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	w	w	w	x	x "+
"1	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	w	w	w	w	w	w	x "+
"0	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	5	w	w	w	w	w	w	w	w	w";

        String[] splitDirt = ds.split("\\s+");

        width = Integer.parseInt(splitDirt[0]);
        height = Integer.parseInt(splitDirt[1]);
        MapBuilder mapBuilder = new MapBuilder(mapName, width, height, 43223);
        mapBuilder.setWaterLevel(0);
        String symmetry = splitDirt[2];
        switch (symmetry) {
            case "r":
                mapBuilder.setSymmetry(MapBuilder.MapSymmetry.rotational);
                break;
            case "h":
                mapBuilder.setSymmetry(MapBuilder.MapSymmetry.horizontal);
                break;
            case "v":
                mapBuilder.setSymmetry(MapBuilder.MapSymmetry.vertical);
                break;
            default:
                throw new RuntimeException("symmetry not specified in google sheets!!!");
        }
        int a = Integer.parseInt(splitDirt[3]);
        int b = Integer.parseInt(splitDirt[4]);
        int waterr = Integer.parseInt(splitDirt[5]);

        // check if there's a comment
        int startIndex = 6;
        while (splitDirt[startIndex].equals("/*")) {
            while (!splitDirt[startIndex].equals("*/")) {
                startIndex++;
            }
            startIndex++;
        }

        String[] dirtGrid = Arrays.copyOfRange(splitDirt, startIndex, splitDirt.length);

//        assert dirtGrid.length == width * height;

        if (dirtGrid[0].equals("indx")) {
            usesIndex = true;
        } else {
            usesIndex = false;
        }

        if (usesIndex) {
            assert dirtGrid.length == 65 * 65;
        } else {
            assert dirtGrid.length == width*height;
        }

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int idx = loc2index(x,y);
                if (dirtGrid[idx].equals("x"))
                    continue;
                if (dirtGrid[idx].startsWith("w")) {
                    mapBuilder.setSymmetricWater(x,y,true);
                    mapBuilder.setSymmetricDirt(x,y,GameConstants.MIN_WATER_ELEVATION);
                    dirtGrid[idx] = dirtGrid[idx].substring(1);
                }
                if (dirtGrid[idx].startsWith("W")) {
                    mapBuilder.setSymmetricWater(x,y,true);
                    mapBuilder.setSymmetricDirt(x,y,waterr);
                    dirtGrid[idx] = dirtGrid[idx].substring(1);
                }
                if (dirtGrid[idx].startsWith("s")) {
                    mapBuilder.setSymmetricSoup(x,y,a);
                    dirtGrid[idx] = dirtGrid[idx].substring(1);
                }
                if (dirtGrid[idx].startsWith("S")) {
                    mapBuilder.setSymmetricSoup(x,y,b);
                    dirtGrid[idx] = dirtGrid[idx].substring(1);
                }
                if (dirtGrid[idx].startsWith("c")) {
                    mapBuilder.addSymmetricCow(x,y);
                    dirtGrid[idx] = dirtGrid[idx].substring(1);
                }
                if (dirtGrid[idx].startsWith("h")) {
                    mapBuilder.addSymmetricHQ(x,y);
                    dirtGrid[idx] = dirtGrid[idx].substring(1);
                }
                try {
                    int d = Integer.parseInt(dirtGrid[idx]);
                    mapBuilder.setSymmetricDirt(x,y,d);
                } catch (NumberFormatException e) {
                    System.out.println("INvalid: " + dirtGrid[idx]);
                    System.out.println("Invalid dirt at position (" + x + "," + y + "). Ignoring this.");
                }
            }
        }




        mapBuilder.saveMap(outputDirectory);

    }
    public static void setWaterInCyl(MapBuilder mapBuilder, float centerX, float centerY, float radius) {
        for (int[] point : pointsInCyl(centerX, centerY, radius)) {
            mapBuilder.setSymmetricWater(point[0],point[1],true);
        }
    }
    public static void setHeightInCyl(MapBuilder mapbuilder, float centerX, float centerY, float radius, int height) {
        for (int [] point : pointsInCyl(centerX, centerY, radius)) {
            mapbuilder.setSymmetricDirt(point[0], point[1], height);
        }
    }

    public static void addRectangleDirt(MapBuilder mapBuilder, int xl, int yb, int xr, int yt, int v) {
        for (int i = xl; i < xr+1; i++) {
            for (int j = yb; j < yt+1; j++) {
                mapBuilder.setSymmetricDirt(i, j, v);
                if (((i + j) % 10) == 0 && j >= 35)
                    try {
                        mapBuilder.addSymmetricCow(i, j);
                    } catch (RuntimeException e) {}
            }
        }
    }
    public static void addRectangleWater(MapBuilder mapBuilder, int xl, int yb, int xr, int yt, int v) {
        for (int i = xl; i < xr+1; i++) {
            for (int j = yb; j < yt+1; j++) {
                mapBuilder.setSymmetricDirt(i, j, v);
                mapBuilder.setSymmetricWater(i, j, true);
            }
        }
    }
    public static void setSoupInCyl(MapBuilder mapbuilder, float centerX, float centerY, float radius, int soup) {
        for (int [] point : pointsInCyl(centerX, centerY, radius)) {
            mapbuilder.setSymmetricSoup(point[0], point[1], soup);
        }
    }
    public static int[][] pointsInCyl(float centerX, float centerY, float radius) {
        ArrayList<int[]> points = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= radius)
                    points.add(new int[]{x, y});
            }
        }
        return points.toArray(new int[0][]);
    }


    /*
     * Add a nice circular lake centered at (x,y).
     */
    public static void addSoup(MapBuilder mapBuilder, int x, int y, int r2, int v) {
        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int d = (xx-x)*(xx-x)/2 + (yy-y)*(yy-y);
                if (d <= r2) {
                    mapBuilder.setSymmetricSoup(xx, yy, v*(d+v));
                }
            }
        }
    }

    /*
     * Add a nice circular lake centered at (x,y).
     */
    public static void addLake(MapBuilder mapBuilder, int x, int y, int r2, int v) {
        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                int d = (xx-x)*(xx-x) + (yy-y)*(yy-y);
                if (d <= r2) {
                    mapBuilder.setSymmetricWater(xx, yy, true);
                    mapBuilder.setSymmetricDirt(xx, yy, v);
                }
            }
        }
        mapBuilder.setSymmetricDirt(x, y, GameConstants.MIN_WATER_ELEVATION);
    }
}
