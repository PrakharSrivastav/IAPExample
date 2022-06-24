package no.sysco.cip.server.controller;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/hello", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloController {


    @GetMapping("/{name}")
    public ResponseEntity<String> hello(@PathVariable String name) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // System.out.println(principal.toString());

        return ResponseEntity.ok(String.format("hello %s [%s]", name, principal.getUsername()));
    }

}
