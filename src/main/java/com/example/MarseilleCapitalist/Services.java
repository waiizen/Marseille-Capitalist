package com.example.MarseilleCapitalist;

import com.example.MarseilleCapitalist.generated.World;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.*;

public class Services {

    File xmlFile = new File("resources/world.xml");
    JAXBContext jaxbContext;

    public World readWorldFromXml(String username){
        try
        {
            InputStream input = getClass().getClassLoader().getResourceAsStream(username+"-world.xml");
            if (input.available() == -1){
                input = getClass().getClassLoader().getResourceAsStream("world.xml");
            }
            jaxbContext = JAXBContext.newInstance(World.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            World world = (World) jaxbUnmarshaller.unmarshal(input);

            System.out.println(world);
            return world;
        }
        catch (JAXBException | IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void saveWorldToXml(World world, String username){
        try
        {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(World.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Store XML to File
            File file = new File("resources/"+username+"-world.xml");
            OutputStream output = new FileOutputStream(file);

            //Writes XML file to file-system
            jaxbMarshaller.marshal(world, file);
        }
        catch (JAXBException | FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public World getWorld(){
        return readWorldFromXml();
    }

}
