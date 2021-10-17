// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.urlshortener.controller;

import com.amazonaws.urlshortener.dto.ShortenerDTO;
import com.amazonaws.urlshortener.service.ShortenerService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import java.net.URI;
import com.amazonaws.urlshortener.service.SendMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import java.util.*;
import java.util.regex.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@RestController
@EnableAutoConfiguration
@RequestMapping("/api/v1")
public class ShortenerController {

    @Autowired
    private Environment env;
    private final ShortenerService shortenerService;
    private final SendMessage sendMessage = new SendMessage();
   


    public ShortenerController(ShortenerService shortenerService) {
        this.shortenerService = shortenerService;
    }

  
    @ApiOperation(value = "Enter URL to Shorten", notes = "Method converts long url to short url")
    @PostMapping("create-short-url")
    public String longToShort(@RequestParam String longurltoconvert, @RequestParam String phonenumber) {
      
        ShortenerDTO shortenerDTO = new ShortenerDTO();
        shortenerDTO.setLongUrl(longurltoconvert);
        String shorturl = shortenerService.longToShort(shortenerDTO);
        String envdomain = env.getProperty("urlshort.domainname");
        String envcountrycode = env.getProperty("urlshort.countrycode");
        String region = env.getProperty("region");
        System.out.println("region inside long to short==>+"+region);
        System.out.println("index of error ="+shorturl.indexOf("Error:"));
        
        if (shorturl.indexOf("Error:") == -1)
        {
          String urltobesent = envdomain+shorturl+"/";
          String shorturlforreturn = "Your Short URL is " + urltobesent + "  In this "+envdomain+" is your domain and "+shorturl + " is your Short URL Code";
          

          String sendSMSBoolean = env.getProperty("urlshort.smsflag");
          String appId = env.getProperty("urlshort.pinpointappid");
          String senderid = env.getProperty("urlshort.pinpointsenderid");
          String registeredkeyword = env.getProperty("urlshort.pinpointregisteredkeyword");
          String templateId = env.getProperty("urlshort.pinpointtemplateId");
          String entityId = env.getProperty("urlshort.pinpointentityId");
          System.out.println("vaule of sendSMSBoolean="+sendSMSBoolean);
          System.out.println("vaule of appId="+appId);
          System.out.println(appId != null && !appId.trim().isEmpty());
          
          if((sendSMSBoolean.equals("true")) && (appId != null && !appId.trim().isEmpty()))
          {
            System.out.println("sendSMSBoolean value passed is ="+sendSMSBoolean);
          phonenumber = envcountrycode+phonenumber;
          boolean flag = sendMessage.sendSMS(phonenumber, urltobesent, appId, senderid, registeredkeyword, region, templateId, entityId);
          System.out.println("SMS sending status ="+flag);
          }
          return shorturlforreturn;
        }
        else
          return shorturl;
        
    }

    @ApiOperation(value = "Enter URL to Shorten", notes = "Method converts long url to short url")
    @PostMapping("shorten-url-within-message")
    public String longToShort(@RequestParam String messagewithurl, @RequestParam String shortflag, @RequestParam String phonenumber) {
        ShortenerDTO shortenerDTO = new ShortenerDTO();
        System.out.println("inside method with value of shortflag=="+shortflag);
        String shorturl = "";
        String urltobesent = "";
        String shorturlforreturn ="";
        String smstexttosend = "";
        String envdomain = env.getProperty("urlshort.domainname");
        String envcountrycode = env.getProperty("urlshort.countrycode");
        String region = env.getProperty("region");
        System.out.println("region inside long to short==>+"+region);
        if (Boolean.parseBoolean(shortflag) == true)
        {
        //String messagewithurl= "test user this is your test shorturl https://timesofindia.indiatimes.com please use this to register";  
          String[] arrofstr = messagewithurl.split(" ");
          StringJoiner responsestr = new StringJoiner(" ");
          List<String> wordsList = Arrays.asList("http:", "https:", "www.");
          for (String a : arrofstr)
          {
            if(wordsList.stream().anyMatch(a::contains))
            {
              shortenerDTO.setLongUrl(a);
              shorturl = shortenerService.longToShort(shortenerDTO);
              System.out.println("Short url ==>"+shorturl);
              System.out.println("index of error ="+shorturl.indexOf("Error:"));
              if (shorturl.indexOf("Error:") == -1)
              {
                urltobesent = envdomain+shorturl+"/";
                responsestr.add(urltobesent);
                shorturlforreturn = "Your Short URL is "+envdomain+shorturl + "  In this " +envdomain+" is your domain and "+shorturl + " is your Short URL Code";
              }
              else return shorturl;
            }  
            else 
            {
              responsestr.add(a);
              
            }
          } //for over
          System.out.println("outside for loop"+responsestr.toString());
          smstexttosend=responsestr.toString();
        } //if over flag is not to shorten the url
          else smstexttosend=messagewithurl;
        
         
          System.out.println("SMS to be sent ==>"+smstexttosend);
          String sendSMSBoolean = env.getProperty("urlshort.smsflag");
          String appId = env.getProperty("urlshort.pinpointappid");
          String senderid = env.getProperty("urlshort.pinpointsenderid");
          String registeredkeyword = env.getProperty("urlshort.pinpointregisteredkeyword");
          String templateId = env.getProperty("urlshort.pinpointtemplateId");
          String entityId = env.getProperty("urlshort.pinpointentityId");
          System.out.println("vaule of sendSMSBoolean="+sendSMSBoolean);
          System.out.println("vaule of appId="+appId);
          System.out.println(appId != null && !appId.trim().isEmpty());
          
          if((sendSMSBoolean.equals("true")) && (appId != null && !appId.trim().isEmpty()))
          {
          System.out.println("sendSMSBoolean value passed is ="+sendSMSBoolean);
          phonenumber = envcountrycode+phonenumber;
          boolean flag = sendMessage.sendSMS(phonenumber, smstexttosend, appId, senderid, registeredkeyword, region, templateId, entityId);
          System.out.println("SMS sending status ="+flag);
          }

         
          return smstexttosend;
        }
        
    

/*
    @ApiOperation(value = "Enter Short URL to get original Long URL", notes = "Method converts short url to long url")
    @GetMapping(value = "{shortUrl}")
  /* @Cacheable(value = "urls", key = "#shortUrl", sync = true)
    public ResponseEntity<Void> getAndRedirect(@PathVariable String shortUrl) {
        var url = urlService.getOriginalUrl(shortUrl);

      /*  return ResponseEntity.status(HttpStatus.FOUND) 
                .location(URI.create(url))
                .build();
    System.out.println("in the Get controller before redirecting for the url=="+url);
    HttpHeaders responseHeaders = new HttpHeaders();
     URI location = null;
    try{
    location = new URI(url);
    }
    catch (Exception e)
    {
        System.out.println("Exception==="+e);
    }
    responseHeaders.setLocation(location);
    ResponseEntity<Void> forwardResponseEntity = new ResponseEntity<Void>(responseHeaders, HttpStatus.MOVED_PERMANENTLY);
    return  forwardResponseEntity;
/*return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(url))
                .build();


    }

*/

    @GetMapping(value = "{urltoconvert}")
    public ResponseEntity<Void> shortToLong(@PathVariable String urltoconvert) {
        var url = shortenerService.shortToLong(urltoconvert);
        System.out.println("in the Get controller before redirecting for the url=="+url);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
/*
    @ApiOperation(value = "Enter Short URL to get original Long URL", notes = "Method converts short url to long url")
    @PostMapping("/geturl")
    @ResponseBody
  // @Cacheable(value = "urls", key = "#shortUrl", sync = true) 
    public ResponseEntity<Void> shortToLong(@RequestParam String urltoconvert) {

        var url = shortenerService.shortToLong(urltoconvert);

      //  return ResponseEntity.status(HttpStatus.FOUND) 
        //        .location(URI.create(url))
          //      .build();
    System.out.println("in the Get controller before redirecting for the url=="+url);


    
    //HttpHeaders responseHeaders = new HttpHeaders();
     //URI location = null;
    //try{
    //location = new URI(url);
    //}
    //catch (Exception e)
    //{
      //  System.out.println("Exception==="+e);
    //}
    //responseHeaders.setLocation(location);
    //ResponseEntity<Void> forwardResponseEntity = new ResponseEntity<Void>(responseHeaders, HttpStatus.MOVED_PERMANENTLY);
    //return  forwardResponseEntity;
return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();


    }

*/    
}