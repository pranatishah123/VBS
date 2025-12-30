package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.Logindto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController //controller, responsebody combine//
@CrossOrigin(origins = "*")//Allows requests from any frontend (React, Angular, etc.)//
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        History h1= new History();
        h1.setDescription("User self Created :"+user.getUsername());
        historyRepo.save(h1);
        userRepo.save(user);
        return "Signup Successful";
    }
    @PostMapping("/login")
    public String login(@RequestBody Logindto u)
    {
        User user=userRepo.findByUsername(u.getUsername());//checks username from database//
        if(user==null)
        {
            return "User not found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Password incorrect";
        }
        if(!u.getRole().equals(user.getRole()))
        {
            return"role not found";
        }
        return String.valueOf(user.getId());//returns id as response//
    }
    @GetMapping("/get-details/{id}")//get details of any id//
    public DisplayDto display(@PathVariable int id)//used in url typing id eg backend url+ id of that user in url//
    {
        User user= userRepo.findById(id).orElseThrow(()->new RuntimeException("user not found"));
        DisplayDto displayDto= new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;

    }
    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        History h1=new History();
        User user= userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Wrong id"));
        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            h1.setDescription("User changed from :"+user.getName()+ "to :"+obj.getValue());
            user.setName(obj.getValue());
        }
        else if((obj.getKey().equalsIgnoreCase("password")))
        {
            if(user.getPassword().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            h1.setDescription("User changed Password :" +user.getUsername());
            user.setPassword(obj.getValue());
        }
        else if((obj.getKey().equalsIgnoreCase("email")))
        {
            if(user.getEmail().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            User user2= userRepo.findByEmail(obj.getValue());
            if(user2!=null) return "Email already exists";
            h1.setDescription("User changed Email from :" +user.getEmail()+ "to: "+obj.getValue());
            user.setEmail(obj.getValue());
        }
        else {
            return " invalid key";
        }
        userRepo.save(user);
        historyRepo.save(h1);
        return "Update done Successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user, @PathVariable int adminId)
    {
        History h1= new History();
        h1.setDescription("User "+user.getUsername()+"Created by admin: "+adminId);
        historyRepo.save(h1);
        userRepo.save(user);
        return "Successfully added";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy, @RequestParam String order)//eg. dashboard?id=10, where ? is like not necessary//
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))// / means its compulsory to add in functions way, where its called path variable this both are forms of get mapping//
        {
            sort = Sort.by(sortBy).descending();
        }
        else
        {
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer", sort);//only show customers table and then sort by 3 things in ascending & descending order//
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword)// keyword is searchterm//
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword, "customer");
    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(@PathVariable int userId, @PathVariable int adminId)
    {
        History h1= new History();
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("wrong id"));
        if(user.getBalance()<0)
        {
            return "Balance should be zero";
        }
        h1.setDescription("User "+user.getUsername()+ "Deleted by admin :"+adminId);
        historyRepo.save(h1);
        userRepo.delete(user);
        return "user Deleted Successfully";

    }
}
