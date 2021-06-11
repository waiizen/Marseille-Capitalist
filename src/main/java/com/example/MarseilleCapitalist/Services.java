package com.example.MarseilleCapitalist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import generated.World;

import java.io.*;

public class Services {

    File xmlFile = new File("resources/world.xml");
    JAXBContext jaxbContext;

    public World readWorldFromXml(){
        try
        {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            jaxbContext = JAXBContext.newInstance(World.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            generated.World world = (World) jaxbUnmarshaller.unmarshal(input);

            System.out.println(world);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public void saveWorldToXml(World world){
        try
        {
            //Create JAXB Context
            JAXBContext jaxbContext = JAXBContext.newInstance(World.class);

            //Create Marshaller
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            //Required formatting??
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            //Store XML to File
            File file = new File("resources/world.xml");
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
