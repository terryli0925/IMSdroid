package org.doubango.imsdroid.map;

import java.util.LinkedList;

public class RobotOperationMode {
    public static final int NONE = 0;
    public static final int MANUAL_MODE = 1;
    public static final int SEMI_AUTO_MODE = 2;
    public static final int AUTO_MODE = 3;

    public static final String ACTION_TARGET_ADD = "TARGET_ADD";
    public static final String ACTION_TARGET_REMOVE = "TARGET_REMOVE";

    public static LinkedList<int[][]> targetQueue=new LinkedList<int[][]>();

    public static int getIndexInTrackList(int[][] target) {
        int index = -1;

        for (int i = 0; i < targetQueue.size(); i++) {
            int tempTarget[][] = targetQueue.get(i);
            if (tempTarget[0][0] == target[0][0] &&
                    tempTarget[0][1] == target[0][1]) {
                index = i;
                break;
            }
        }
        return index;
    }

}
