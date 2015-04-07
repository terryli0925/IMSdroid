package org.doubango.imsdroid.map;

import java.util.LinkedList;

public class RobotOperationMode {
    public static final int NONE = 0;
    public static final int MANUAL_MODE = 1;
    public static final int SEMI_AUTO_MODE = 2;
    public static final int AUTO_MODE = 3;

    public static LinkedList<int[][]> nextTargetQueue=new LinkedList<int[][]>();

    public static int getIndexInTrackList(int[][] target) {
        int index = -1;

        for (int i = 0; i < nextTargetQueue.size(); i++) {
            int tempTarget[][] = nextTargetQueue.get(i);
            if (tempTarget[0][0] == target[0][0] &&
                    tempTarget[0][1] == target[0][1]) {
                index = i;
                break;
            }
        }
        return index;
    }

}
