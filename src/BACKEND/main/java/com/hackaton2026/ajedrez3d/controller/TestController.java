package com.hackaton2026.ajedrez3d.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class TestController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

@GetMapping("/test")
public String sendTest() {
    List<Map<String, Object>> pieces = new ArrayList<>();
    
    pieces.add(Map.of("id","1","type","rey","color","white","position",Map.of("x",0,"y",0,"z",0)));
    pieces.add(Map.of("id","2","type","torre","color","white","position",Map.of("x",1,"y",0,"z",0)));
    pieces.add(Map.of("id","3","type","unicornio","color","white","position",Map.of("x",0,"y",0,"z",1)));
    pieces.add(Map.of("id","4","type","caballo","color","white","position",Map.of("x",1,"y",0,"z",1)));
    pieces.add(Map.of("id","5","type","alfil","color","white","position",Map.of("x",0,"y",1,"z",0)));

    messagingTemplate.convertAndSend("/topic/game/1", pieces);
    return "OK";
}


}