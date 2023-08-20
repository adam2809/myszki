package com.adamk.myszki;

/**
 * Created by adamk on 2018-01-21.
 */

public class Map{
    int[] boostsObstacles;
    int backgroundImageID;
    String name;
    Map(int backgroundImageID,String name,String boostsObstaclesString){

        boostsObstacles=new int[100];
        this.name=name;
        this.backgroundImageID=backgroundImageID;

    }
}
