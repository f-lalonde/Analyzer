package com.francislalonde;

import java.util.HashMap;

public class Classe extends Methode {

    private int WMC;

    private final HashMap<String, Methode> class_methods= new HashMap<>();

    public Classe(String name, int start, int end, int javadocLines){
        super(name, start, end, javadocLines);
        this.WMC = 0;

    }

    public void addMethod(String methodName, Methode methode){
        class_methods.put(methodName, methode);
    }

    public HashMap<String, Methode> getClass_methods() {
        return class_methods;
    }

    public Methode getMethod(String name) {
        return class_methods.get(name);
    }

    public void computeWMC(){
        class_methods.forEach((name, method) -> WMC = WMC + method.getCC());
    }

    public int getWMC() {
        return WMC;
    }

    @Override
    public void computeBC(){
        if(getWMC() == 0){
            setBC(-1);
        } else {
            setBC(getDC() / getWMC());
        }
    }

}

