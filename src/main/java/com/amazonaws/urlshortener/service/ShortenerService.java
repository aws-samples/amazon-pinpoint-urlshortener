package com.amazonaws.urlshortener.service;

import com.amazonaws.urlshortener.dto.ShortenerDTO;
import com.amazonaws.urlshortener.entity.ShortenerPOJO;
import com.amazonaws.urlshortener.repository.ShortenerRepository;
import org.springframework.stereotype.Service;
import com.amazonaws.urlshortener.service.Base62Conversion;
import javax.persistence.EntityNotFoundException;
import java.util.Date;
import java.util.Calendar;
import java.net.URI;
import java.net.URL;
import java.util.Random;
import com.amazonaws.urlshortener.repository.ShortenerAnalysisRepository;
import com.amazonaws.urlshortener.entity.ShortenerAnalysisPOJO;
import org.springframework.core.env.Environment;
import java.lang.Integer;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ShortenerService {
    @Autowired
    private Environment env;
    private final ShortenerRepository shortenerRepository;
    private final ShortenerAnalysisRepository shortenerAnalysisRepository;
    private final Base62Conversion conversion;
    public static boolean isValidURL(String url)
    {
        //Test is URL is valid or not
        try {
            if(url.length() <=255)
                {
                new URL(url).toURI();
                return true;
                }
            else 
            {
                System.out.println("URL Length not acceptable");
                return false;
            }
            
        }
          
        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            System.out.println("Not a valid URL");
            System.out.println("Exception"+e);
            return false;
        }
    }

    public ShortenerService(ShortenerRepository shortenerRepository, Base62Conversion base62Conversion,  ShortenerAnalysisRepository shortenerAnalysisRepository) {
        this.shortenerRepository = shortenerRepository;
        this.conversion = base62Conversion;
        this.shortenerAnalysisRepository = shortenerAnalysisRepository;

       
    }

    public String longToShort(ShortenerDTO request) {
        if (isValidURL(request.getLongUrl()))
        {
            var url = new ShortenerPOJO();
            String urlpassed = request.getLongUrl();
            url.setLongUrl(urlpassed);
            var entity1 =  shortenerRepository.findByUrl(urlpassed);
            System.out.println("value of entity1 for duplicity check is ="+entity1);
           // System.out.println("value of entity size is ="+entity1.getId());

            if(entity1 == null)
                {
                String retention = env.getProperty("urlshort.urlretention");  
                int ret=Integer.parseInt(retention);   
                System.out.println("Inside Encoding method - Long URL to be converted is ===>"+request.getLongUrl());
                Date dt = new Date();
                Calendar c = Calendar.getInstance(); 
                c.setTime(dt); 
                c.add(Calendar.DATE, ret); //retention for short URL after which we will purge
                dt = c.getTime();
                url.setExpiresDate(dt);
                url.setCreatedDate(new Date());
                System.out.println("just before save");
                System.out.println("id is this========>"+url.getId());
                Random random=new Random();
                int randnum =  10+random.nextInt(90);
                url.setRandNum(randnum);
                var entity = shortenerRepository.save(url);
                long eid = entity.getId();
                System.out.println("value of entity.getid is=="+eid);
                System.out.println("value of random number is=="+randnum);
                long result =  Long.valueOf(String.valueOf(randnum) + String.valueOf(eid));
                System.out.println("value of final number to be enncoded =="+result);
                var convertedurl = conversion.encode(result);
                System.out.println("Inside Encoding method - Converted Short URL is ===>"+convertedurl);
                return convertedurl;
                }
            else
            {   System.out.println("inside URL already present");
                int randnum = entity1.getRandNum();
                long eid = entity1.getId();
                long result =  Long.valueOf(String.valueOf(randnum) + String.valueOf(eid));
                System.out.println("value of final number to be enncoded =="+result);
                var convertedurl = conversion.encode(result);
                System.out.println("Inside Encoding method - Converted Short URL already present is ===>"+convertedurl);
                return convertedurl;
            }
        }
        else return "Error: Not a valid URL";
    }

    public String shortToLong(String shortUrl) {
        long id = conversion.decode(shortUrl);
        System.out.println("Full number after decoding ="+id);
        long new_id = Long.parseLong(Long.toString(id).substring(2));
        System.out.println("After remove random number after decoding ="+new_id);
        System.out.println("Inside Decoding method - Short URL to be converted is ===>"+shortUrl);
       
        var entity = shortenerRepository.findById(new_id)
                .orElseThrow(() -> new EntityNotFoundException("There is no entity with short URL" + shortUrl));

        if (entity.getExpiresDate() != null && entity.getExpiresDate().before(new Date())){
            shortenerRepository.delete(entity);
            throw new EntityNotFoundException("Short URL link has expired!");
        }
        var analysisPOJO = new ShortenerAnalysisPOJO();
        analysisPOJO.setLurl(new_id);
        analysisPOJO.setViewedDate(new Date());
        
        shortenerAnalysisRepository.save(analysisPOJO);
        System.out.println("Inside Decoding method - Long URL post conversion is ===>"+entity.getLongUrl());
        return entity.getLongUrl();
    }
}