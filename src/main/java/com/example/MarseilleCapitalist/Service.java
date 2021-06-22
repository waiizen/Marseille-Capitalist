package com.example.MarseilleCapitalist;

import com.example.MarseilleCapitalist.generated.PallierType;
import com.example.MarseilleCapitalist.generated.ProductType;
import com.example.MarseilleCapitalist.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Service {

    File xmlFile = new File("resources/world.xml");
    JAXBContext jaxbContext;

    public World readWorldFromXml(String username){
        World world = new World();
        InputStream input = null;

        try {
            input = new FileInputStream(username + "-world.xml");
        } catch (FileNotFoundException ex) {
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        } finally {

            try {
                JAXBContext cont = JAXBContext.newInstance(World.class);
                Unmarshaller u = cont.createUnmarshaller();
                world = (World) u.unmarshal(input);

                System.out.println(world.getName());
                System.out.println("username : " + username);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        this.calculScore(world);
        return world;
    }

    public void saveWorldToXml(World world, String username){
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();

            m.marshal(world, new FileOutputStream(username + "-world.xml"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public World getWorld(String username){
        return readWorldFromXml(username);
    }


    public Boolean updateProduct(String username, ProductType newproduct) {
        World world = getWorld(username);
        ProductType product = findProductById(world, newproduct.getId());

        if (product == null) {return false;}

        int qtchange = newproduct.getQuantite() - product.getQuantite();
        if (qtchange > 0) {
            // soustraire de l'argent du joueur le cout de la quantité
            // achetée et mettre à jour la quantité de product
            // uN = u1 ((1-r^n)/(1-r))
            double argent = product.getCout() * Math.pow(product.getCroissance(), qtchange);
            double argent2 = world.getMoney() - argent;

            world.setMoney(argent2);

            int nbproduit = product.getQuantite() + qtchange;
            product.setQuantite(nbproduit);

        } else {
            // initialiser product.timeleft à product.vitesse
            // pour lancer la production}
            // sauvegarder les changements du monde
            long tl = product.getVitesse();
            product.setTimeleft(tl);
        }
        saveWorldToXml(world, username);
        System.out.println("saved world for "+username);
        return true;
    }

    public ProductType findProductById(World world, int id) {
        return world.getProducts().getProduct().get(id - 1);
    }

    public World calculScore(World world) {
        long currentTime = System.currentTimeMillis();
        long spentTime = currentTime - world.getLastupdate();
        double currentMoney = world.getMoney();
        double currentScore = world.getScore();
        world.setLastupdate(currentTime);
        for (ProductType p : world.getProducts().getProduct()) {
            if (p.isManagerUnlocked()) {
                double earnedMoney = Math.floor(spentTime / p.getVitesse()) * (p.getQuantite() * p.getRevenu()
                        * (1 + (world.getActiveangels() * world.getAngelbonus()) / 100));
                world.setMoney(currentMoney + earnedMoney);
                world.setScore(currentScore + earnedMoney);
                p.setTimeleft(spentTime % p.getVitesse());
            } else {
                if (p.getTimeleft() != 0) {
                    if (spentTime < p.getTimeleft()) {
                        p.setTimeleft(p.getTimeleft() - spentTime);
                    } else {
                        p.setTimeleft(0);
                        double earnedMoney = p.getQuantite() * p.getRevenu()
                                * (1 + (world.getActiveangels() * world.getAngelbonus()) / 100);
                        world.setMoney(currentMoney + earnedMoney);
                        world.setScore(currentScore + earnedMoney);
                    }
                }
            }
        }
        return world;
    }

    public Boolean updateManager(String username, PallierType newmanager) {
        World world = this.getWorld(username);
        world = this.calculScore(world);

        int managerIndex = 0;
        for (PallierType m : world.getManagers().getPallier()) {
            if (newmanager.getName().equals(m.getName())) {
                managerIndex = world.getManagers().getPallier().indexOf(m);
            }
        }

        PallierType manager = world.getManagers().getPallier().get(managerIndex);
        if (manager == null) {
            return false;
        } else {
            manager.setUnlocked(true);
        }

        // trouver le produit correspondant au manager
        ProductType product = world.getProducts().getProduct().get(manager.getIdcible() - 1);
        if (product == null) {
            return false;
        } else {
            product.setManagerUnlocked(true);
        }

        // soustraire de l'argent du joueur le cout du manager
        world.setMoney(world.getMoney() - manager.getSeuil());

        // sauvegarder les changements au monde
        this.saveWorldToXml(world, username);
        return true;
    }

}
