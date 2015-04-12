package Marty.company;

import java.util.Date;

/**
 * Created by marty.farley on 4/12/2015.
 */
public class Beehive {

    private Date dateCollected;
    private String location;
    private double weight;

    public Beehive(String location, Date date, float weight){
        this.location = location;
        this.dateCollected = date;
        this.weight = weight;
    }

    @Override
    public String toString(){

        return "Beehive " + "\n" +
                "Location " + "\n" +
                "Date Honey Collected" + "\n" +
                "Honey Weight" + "\n";
    }
}
