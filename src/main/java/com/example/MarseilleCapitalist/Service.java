package com.example.MarseilleCapitalist;

import com.example.MarseilleCapitalist.generated.PallierType;
import com.example.MarseilleCapitalist.generated.ProductType;
import com.example.MarseilleCapitalist.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.*;

public class Service {

    File xmlFile = new File("resources/world.xml");
    JAXBContext jaxbContext;

    /**
     * Permet de récupérer le monde à partir de l'xml
     *
     * @param username le joueur concerné par ce monde
     * @return le monde du joueur
     */
    public World readWorldFromXml(String username) {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.calculScore(world);

        System.out.println(world.getName());
        System.out.println("[ReadWorldFromXml] - [USERNAME] " + username);
        System.out.println("[ReadWorldFromXml] - [LastWorldMoney] " + world.getMoney());
        System.out.println("[ReadWorldFromXml] - [LastWorldScore] " + world.getScore());
        System.out.println("[ReadWorldFromXml] - [LastTotalAngels] : " + world.getActiveangels());
        return world;
    }

    /**
     * Permet de save le monde
     * @param world le monde à sauvegarder
     * @param username l'username du monde
     */
    public void saveWorldToXml(World world, String username) {
        try {
            JAXBContext cont = JAXBContext.newInstance(World.class);
            Marshaller m = cont.createMarshaller();
            m.marshal(world, new FileOutputStream(username + "-world.xml"));
            System.out.println("[SAVED WORLD] " +username+"-world.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get le monde à partir d'un pseudo
     * @param username le pseudo
     * @return le monde du pseudo
     */
    public World getWorld(String username) {
        return readWorldFromXml(username);
    }

    /**
     * Permet d'update le produit à chaque fabrication manuelle, chaque achat d'une quantité, chaque succès débloqué
     * @param username le pseudo du joueur
     * @param nouveauProduit le produit modifié
     * @return true si ok, false sinon
     */
    public Boolean updateProduct(String username, ProductType nouveauProduit) {
        System.out.println("[UpdateProduct]");
        World world = getWorld(username);
        ProductType product = findProductById(world, nouveauProduit.getId());
        // si le produit est null, alors il y a un problème
        if (product == null) {return false;}
        int qttChange = nouveauProduit.getQuantite() - product.getQuantite();
        // si il y a eu l'achat d'un seul produit
        if (qttChange == 1) {
            double argent = product.getCout() * Math.pow(product.getCroissance(), (nouveauProduit.getQuantite()-1));
            world.setMoney(world.getMoney() - argent); // on ajoute l'argent
            product.setQuantite(product.getQuantite() + qttChange); // on ajoute la quantité

            // Permet d'update les unlocks si besoin
            for (PallierType unlock : product.getPalliers().getPallier()) {
                if (!unlock.isUnlocked() && (unlock.getSeuil() <= product.getQuantite())) {
                    System.out.print("[UpdateProduct] - [UpdatePalier]");
                    unlock.setUnlocked(true);
                    switch (unlock.getTyperatio()) {
                        case VITESSE:
                            System.out.println(" - Vitesse");
                            product.setVitesse((int) (product.getVitesse() / unlock.getRatio()));
                            product.setTimeleft((int) (product.getTimeleft() / unlock.getRatio()));
                            break;
                        case GAIN:
                            System.out.println(" - Gain");
                            product.setRevenu(product.getRevenu() * unlock.getRatio());
                            break;
                        default:
                            System.out.println(" - Défaut");
                            break;
                    }
                }
            }
        // si il y a eu l'achat de plusieurs produits (avec le multiplicateur par exemple)
        } else if (qttChange > 1) {
            // cout d'achat de n produits
            double coutAchatNProduits = (((product.getCout() * Math.pow(product.getCroissance(), product.getQuantite())) * (1 - Math.pow(product.getCroissance(), qttChange))) / (1 - product.getCroissance()));
            world.setMoney(world.getMoney() - coutAchatNProduits); // on soustrait ce cout a l'argent
            int nbproduit = product.getQuantite() + qttChange;
            product.setQuantite(nbproduit);

            // Permet d'update les unlocks si besoin
            for (PallierType unlock : product.getPalliers().getPallier()) {
                if (!unlock.isUnlocked() && (unlock.getSeuil() <= product.getQuantite())) {
                    System.out.print("[UpdateProduct] - [UpdatePalier]");
                    unlock.setUnlocked(true);
                    switch (unlock.getTyperatio()) {
                        case VITESSE:
                            System.out.println(" - Vitesse");
                            product.setVitesse((int) (product.getVitesse() / unlock.getRatio()));
                            product.setTimeleft((int) (product.getTimeleft() / unlock.getRatio()));
                            break;
                        case GAIN:
                            System.out.println(" - Gain");
                            product.setRevenu(product.getRevenu() * unlock.getRatio());
                            break;
                        default:
                            System.out.println(" - Défaut");
                            break;
                    }
                }
            }
        } else {
            product.setTimeleft(product.getVitesse());
        }
        saveWorldToXml(world, username);
        System.out.println("[UpdateProduct] - [SAVED WORLD] " + username);
        System.out.println("[UpdateProduct] - [NEW MONEY] " + world.getMoney());
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
        System.out.println("[UpdateManager]");
        World world = this.getWorld(username);
        world = this.calculScore(world);

        int managerIndex = 0;
        for (PallierType manager : world.getManagers().getPallier()) {
            if (newmanager.getName().equals(manager.getName())) {
                managerIndex = world.getManagers().getPallier().indexOf(manager);
            }
        }

        // Parcourt chaque manager et active celui à activer
        PallierType manager = world.getManagers().getPallier().get(managerIndex);
        if (manager == null) return false;
        else {
            System.out.println("[UpdateManager] - [SetUnlocked] - "+manager.getName());
            manager.setUnlocked(true);
        }

        // Parcourt chaque produit et active celui à activer en fonction du manager
        ProductType product = world.getProducts().getProduct().get(manager.getIdcible() - 1);
        if (product == null) return false;
        else {
            System.out.println("[UpdateManager] - [SetProductManagerUnlocked] - "+product.getName());
            product.setManagerUnlocked(true);
        }

        world.setMoney(world.getMoney() - manager.getSeuil());
        this.saveWorldToXml(world, username);
        System.out.println("[UpdateProduct] - [SAVED WORLD] " + username);
        System.out.println("[UpdateProduct] - [NEW MONEY] " + world.getMoney());
        return true;
    }

    public Boolean updateUpgrade(String username, PallierType newupgrade) {
        System.out.println("[UpdateUpgrade]");
        World world = this.getWorld(username);
        world = this.calculScore(world);

        int upgradeIndex = 0;
        for (PallierType upgrade : world.getUpgrades().getPallier()) {
            if (newupgrade.getName().equals(upgrade.getName())) {
                upgradeIndex = world.getUpgrades().getPallier().indexOf(upgrade);
            }
        }

        // Parcourt chaque upgrade et unlock la bonne
        PallierType upgrade = world.getUpgrades().getPallier().get(upgradeIndex);
        if (upgrade == null) return false;
        else {
            System.out.println("[UpdateUpgrade] - [SetUnlocked] - "+upgrade.getName());
            upgrade.setUnlocked(true);
        }

        // Parcourt chaque produit et applique le bonus à ce produit
        ProductType product = world.getProducts().getProduct().get(upgrade.getIdcible() - 1);
        if (product == null) return false;
        else {
            switch (upgrade.getIdcible()) {
                // si bonus d'ange, alors bonus sur le monde
                case -1:
                    System.out.println("[UpdateUpgrade] - [WorldUpgrade]");
                    world.setAngelbonus((int) (world.getAngelbonus() + upgrade.getRatio()));
                    break;
                // si bonus à tous les prduits, alors l'applique a chaque produit
                case 0:
                    System.out.print("[UpdateUpgrade] - [AllProductUpgrade]");
                    for (ProductType productTmp : world.getProducts().getProduct()) {
                        switch (upgrade.getTyperatio()) {
                            case VITESSE:
                                System.out.println(" - Vitesse");
                                productTmp.setVitesse((int) (productTmp.getVitesse() / upgrade.getRatio()));
                                productTmp.setTimeleft((int) (productTmp.getTimeleft() / upgrade.getRatio()));
                                break;
                            case GAIN:
                                System.out.println(" - Gain");
                                productTmp.setRevenu(productTmp.getRevenu() * upgrade.getRatio());
                                break;
                            default:
                                System.out.println(" - Défaut");
                                break;
                        }
                    }
                    break;
                // sinon l'applique au produit concerné
                default:
                    System.out.print("[UpdateUpgrade] - [SingleProductUpgrade]");
                    switch (upgrade.getTyperatio()) {
                        case VITESSE:
                            System.out.println(" - Vitesse");
                            product.setVitesse((int) (product.getVitesse() / upgrade.getRatio()));
                            product.setTimeleft((int) (product.getTimeleft() / upgrade.getRatio()));
                            break;
                        case GAIN:
                            System.out.println(" - Gain");
                            product.setRevenu(product.getRevenu() * upgrade.getRatio());
                            break;
                        default:
                            System.out.println(" - Défaut");
                            break;
                    }
                    break;
            }
        }
        world.setMoney(world.getMoney() - upgrade.getSeuil());
        this.saveWorldToXml(world, username);
        System.out.println("[UpdateUpgrade] - [SAVED WORLD] " + username);
        System.out.println("[UpdateUpgrade] - [NEW MONEY] " + world.getMoney());
        return true;
    }

    public Boolean updateAngel(String username, int newAngel) {
        System.out.println("[UpdateAngel]");
        World world = this.getWorld(username);
        world = this.calculScore(world);
        world.setActiveangels(world.getActiveangels() + newAngel);
        this.saveWorldToXml(world, username);
        System.out.println("[UpdateAngel] - [SAVED WORLD] " + username);
        System.out.println("[UpdateAngel] - [NEW MONEY] " + world.getMoney());
        return true;
    }

    public boolean resetWorld(String username) {
        System.out.println("[ResetWorld]");
        World world = getWorld(username);
        double activeAngels = world.getActiveangels();
        double totalAngels = world.getTotalangels();
        double calculAnges = Math.floor(150 * Math.sqrt(world.getMoney() / (Math.pow(10, 15))));
        double newAngels = Math.floor((calculAnges > totalAngels) ? calculAnges - totalAngels : 0);

        File fichierJoueur = new File(username + "-world.xml");
        fichierJoueur.delete();
        System.out.println("[ResetWorld] - [DELETED] - "+username+"-world.xml");

        World newWorld = getWorld(username);
        System.out.println("[ResetWorld] - [CREATED] - "+username+"-world.xml");
        newWorld.setActiveangels(activeAngels + newAngels);
        newWorld.setTotalangels(totalAngels + newAngels);

        saveWorldToXml(newWorld, username);
        System.out.println("[ResetWorld] - [SAVED WORLD] " + username);
        System.out.println("[ResetWorld] - [NEW MONEY] " + world.getMoney());
        return true;
    }

}
