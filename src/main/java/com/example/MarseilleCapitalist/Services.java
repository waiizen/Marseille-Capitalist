package com.example.MarseilleCapitalist;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import generated.World;
import java.io.File;

public class Services {

    File xmlFile = new File("worldSchema.xml");
    JAXBContext jaxbContext;

    public World readWorldFromXml(){
        try
        {
            jaxbContext = JAXBContext.newInstance(World.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            generated.World employee = (World) jaxbUnmarshaller.unmarshal(xmlFile);

            System.out.println(employee);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }
    }

    public void saveWorldToXml(generated.World world){
        //TODO
    }

    public World getWorld(){
        //TODO
    }

}
