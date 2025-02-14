package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private boolean stop;

    private boolean dead;


    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        stop = false;
        dead = false;
    }

    public void run() {
        while (!dead) {
            if (stop) {
                synchronized (updateCallback) {
                    try {
                        updateCallback.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                if (immortalsPopulation.size() > 1) {

                    Immortal im;

                    int myIndex = immortalsPopulation.indexOf(this);

                    int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                    //avoid self-fight
                    if (nextFighterIndex == myIndex) {
                        nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                    }
                    try {
                        im = immortalsPopulation.get(nextFighterIndex);

                        this.fight(im);
                    } catch (IndexOutOfBoundsException ex) {
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                } else {
                    updateCallback.processReport("The winner is: " + this + "\n");
                    break;
                }
            }
        }
    }

    public void fight(Immortal i2) {
        Immortal first, second;
        int thisHash = System.identityHashCode(this);
        int i2Hash = System.identityHashCode(i2);
        if (thisHash < i2Hash) {
            first = this;
            second = i2;
        } else {
            first = i2;
            second = this;
        }
        synchronized (first) {
            synchronized (second) {
                if (health != 0) {
                    if (i2.getHealth() > 0) {
                        i2.changeHealth(i2.getHealth() - defaultDamageValue);
                        this.health += defaultDamageValue;
                        updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
                        if (i2.getHealth() == 0) {
                            updateCallback.processReport(this + " killed:" + i2 + "\n");
                            immortalsPopulation.remove(i2);
                        }
                    } else {
                        updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                    }
                } else {
                    dead = true;
                }
            }
        }
    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

    public ImmortalUpdateReportCallback getUpdateCallback () {
        return updateCallback;
    }

    public void setStop (boolean newStop) {
        stop = newStop;
    }

    public void setDead (boolean newDead) {
        dead = newDead;
    }
}
